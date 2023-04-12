package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private boolean isActive = true;
    private Map<String, Integer> accounts;
    private final List<Operation> ledger;
    private List<Integer> replicaTS = new ArrayList<>();
    private static final String BROKER = "broker";
    // TODO: remove this when we have a better way to identify the replica
    private String qualifier;

    public ServerState(String qualifier) {
        Logger.log("Initializing ServerState");
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<>();
        Logger.log("Creating Broker Account");
        this.addAccount(BROKER, 1000);
        Logger.log("Broker Account created");
        Logger.log("ServerState initialized");
        this.replicaTS.add(0);
        this.replicaTS.add(0);
        // TODO: remove this when we have a better way to identify the replica
        this.qualifier = qualifier;
    }

    public synchronized void addOperation(Operation op) {
        Logger.log("Adding operation " + op.toString() + " to ledger");
        this.ledger.add(op);
        Logger.log("Operation added");
    }

    public void updateReplicaTS() {
        if (qualifier.equals("A")) {
            replicaTS.set(0, replicaTS.get(0) + 1);
        } else {
            replicaTS.set(1, replicaTS.get(1) + 1);
        }
    }

    private boolean operationIsStable(List<Integer> prevTS) {
        for (int i = 0; i < prevTS.size(); i++){
            if (prevTS.get(i) > getReplicaTS().get(i))
                return false;
        }
        return true;
    }

    // User Interface Operations

    public synchronized void createAccount(String name, List<Integer> prevTS) {
        Logger.log("Creating account \'" + name + "\'");
        Logger.log("User prevTS is: " + prevTS);
        Logger.log("Replica TS is: " + getReplicaTS());
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (accountExists(name)) {
            throw new AccountAlreadyExistsException(name);
        }
        CreateOp op = new CreateOp(name, prevTS, null);
        updateReplicaTS();
        if (operationIsStable(prevTS)) {
            op.setTS(getReplicaTS());
            addAccount(name);
        }
        addOperation(op);
        Logger.log("Account \'" + name + "\' created");
        Logger.log("At the end, Replica TS is: " + getReplicaTS());
    }

    public synchronized void deleteAccount(String name, List<Integer> prevTS) {
        Logger.log("Deleting account \'" + name + "\'");
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (name.equals(BROKER)) {
            throw new DeleteBrokerAccountException(name);
        }
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        if (getAccountBalance(name, getReplicaTS()) > 0) {
            throw new AccountHasBalanceException(name);
        }
        DeleteOp op = new DeleteOp(name, prevTS, null);
        if (operationIsStable(prevTS)) {
            op.setTS(getReplicaTS());
            removeAccount(name);
        }
        addOperation(op);
        Logger.log("Account \'" + name + "\' deleted");
    }

    public synchronized void transferTo(String from, String to, Integer amount, List<Integer> prevTS) {
        Logger.log("Transferring " + amount + " from \'" + from + "\' to \'" + to + "\'");
        Logger.log("User prevTS is: " + prevTS);
        Logger.log("Replica TS is: " + getReplicaTS());
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        if (!accountExists(from) && !accountExists(to)) {
            throw new AccountDoesntExistException(from, to);
        }
        if (!accountExists(from)) {
            throw new AccountDoesntExistException(from);
        }
        if (!accountExists(to)) {
            throw new AccountDoesntExistException(to);
        }
        if (!accountHasBalance(from, amount)) {
            throw new InsufficientFundsException(from);
        }
        TransferOp op = new TransferOp(from, to, amount, prevTS, null);
        updateReplicaTS();
        if (operationIsStable(prevTS)) {
            op.setTS(getReplicaTS());
            updateAccount(from, -amount);
            updateAccount(to, amount);
        }
        addOperation(op);
        Logger.log("Transfer completed");
        Logger.log("At the end, Replica TS is: " + getReplicaTS());
    }

    public synchronized Integer getAccountBalance(String name, List<Integer> prevTS) {
        Logger.log("Getting balance of account \'" + name + "\'");
        Logger.log("User prevTS is: " + prevTS);
        Logger.log("Replica TS is: " + getReplicaTS());
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (!operationIsStable(prevTS)) {
            throw new OperationNotStableException();
        }
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        Logger.log("At the end, Replica TS is: " + getReplicaTS());
        return accounts.get(name);
    }

    // Admin interface operations

    public synchronized void activate() {
        Logger.log("Admin activating server");
        this.isActive = true;
        Logger.log("Server activated");
    }

    public synchronized void deactivate() {
        Logger.log("Admin deactivating server");
        this.isActive = false;
        Logger.log("Server deactivated");
    }

    public List<Operation> getLedgerState() {
        Logger.log("Admin Getting ledger");
        return getLedger();
    }

    // Propagate ledger operations

    public void propagateState(List<Operation> ledger) {
        Logger.log("Propagating state");
        for (Operation op : ledger) {
            Logger.log("Op TS:" + op.getTS());
            Logger.log("Replica TS:" + getReplicaTS());
            if (replicaTSBiggerThan(op.getTS())) // duplicate operation
                continue;
            if (replicaTSBiggerThan(op.getPrevTS())) { // operation is stable and can be executed
                op.executeOperation(this);
                updateReplicaTSAfterGossip(op);
            }
            addOperation(op);
        }
        Logger.log("State propagated, now going to execute ledger");
        boolean noMoreExecutions = false;
        // now we will go through all operations in the ledger and execute them if they
        // are stable, updating the replicaTS after each execution
        while (!noMoreExecutions) {
            noMoreExecutions = true;
            for (Operation op : getLedger()) {
                Logger.log("Checking operation " + op.toString() + " for execution");
                if (replicaTSBiggerThan(op.getTS()))
                    continue; // ignore operations already executed
                
                if (replicaTSBiggerThan(op.getPrevTS())) { // prevTS < replicaTS -> we can execute the operation
                    op.executeOperation(this);
                    // set operation TS if it is null
                    if (op.getTS() == null) { // if the operation wasn't stable when it was added to the ledger
                        updateReplicaTS();
                        op.setTS(getReplicaTS());
                    } else {
                        updateReplicaTSAfterGossip(op);
                    }
                    noMoreExecutions = false; // if we can permorm an operation, we need to go through the ledger again
                }
            }
        }
    }

    public void executeOperation(CreateOp op) {
        Logger.log("Executing create operation");
        addAccount(op.getAccount());
    }

    public void executeOperation(DeleteOp op) {
        Logger.log("Executing delete operation");
        removeAccount(op.getAccount());
    }

    public void executeOperation(TransferOp op) {
        Logger.log("Executing transfer operation");
        updateAccount(op.getAccount(), -op.getAmount());
        updateAccount(op.getDestAccount(), op.getAmount());
    }

    // Getters and Setters

    private synchronized void addAccount(String name) {
        this.accounts.put(name, 0);
    }

    private synchronized void addAccount(String name, int amount) {
        this.accounts.put(name, amount);
    }

    private synchronized void removeAccount(String name) {
        this.accounts.remove(name);
    }

    private synchronized void updateAccount(String name, int amount) {
        accounts.put(name, accounts.get(name) + amount);
    }

    public List<Operation> getLedger() {
        // create a copy of the ledger to avoid concurrent modification
        List<Operation> ledgerCopy = new CopyOnWriteArrayList<>();
        ledgerCopy.addAll(ledger);
        return ledgerCopy;
    }

    public synchronized List<Integer> getReplicaTS() {
        return this.replicaTS;
    }

    // Checker methods

    public synchronized boolean isActive() {
        return this.isActive;
    }

    private void updateReplicaTSAfterGossip(Operation op) {
        // TODO: change this for multiple replicas
        if (this.qualifier.equals("A")) {
            this.replicaTS.set(1, op.getTS().get(1));
        } else if (this.qualifier.equals("B")) {
            this.replicaTS.set(0, op.getTS().get(0));
        }
    }

    public boolean replicaTSBiggerThan(List<Integer> prevTS) {
        // server TS > operation prevTS
        if (prevTS == null) {
            return true;
        }
        for (int i = 0; i < prevTS.size(); i++) {
            if (prevTS.get(i) > this.replicaTS.get(i)) {
                return false;
            }
        }
        return true;
    }

    private synchronized boolean accountExists(String name) {
        return accounts.get(name) != null;
    }

    private synchronized boolean accountHasBalance(String name, int amount) {
        return accounts.get(name) >= amount;
    }

    // TODO - CHECK IF OPERATION IS STABLE AND CAN BE PERFORMED (CHECK TSs)

    @Override
    public synchronized String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                ", accounts=" + accounts +
                '}';
    }
}
