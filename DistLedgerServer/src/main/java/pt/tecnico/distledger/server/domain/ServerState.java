package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private boolean isActive = true;
    private Map<String, Integer> accounts;
    private final List<Operation> ledger;
    private List<Integer> replicaTS = new ArrayList<>();
    private List<Integer> valueTS = new ArrayList<>();
    private final int replicaId;
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
        this.replicaId = this.qualifier.equals("A") ? 0 : 1;
    }

    public synchronized void addOperation(Operation op) {
        Logger.log("Adding operation " + op.toString() + " to ledger");
        this.ledger.add(op);
        Logger.log("Operation added");
    }

    public void updateReplicaTS() {
            replicaTS.set(this.replicaId, this.replicaTS.get(this.replicaId) + 1);
    }

    public void mergeReplicaTS(List<Integer> TS) {
        for (int i = 0; i < TS.size(); i++) {
            if (TS.get(i) > this.replicaTS.get(i)) {
                this.replicaTS.set(i, TS.get(i));
            }
        }
    }

    public void updateValueTS() {
        valueTS.set(this.replicaId, this.valueTS.get(this.replicaId) + 1);
    }

    public void mergeValueTS(List<Integer> TS) {
        for (int i = 0; i < TS.size(); i++) {
            if (TS.get(i) > this.valueTS.get(i)) {
                this.valueTS.set(i, TS.get(i));
            }
        }
    }

    private boolean TSBiggerThan(List<Integer> TS1, List<Integer> TS2) {
        for (int i = 0; i < TS1.size(); i++) {
            if (TS1.get(i) < TS2.get(i)) {
                return false;
            }
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
        if (TSBiggerThan(this.replicaTS, prevTS)) {
            updateValueTS();
            addAccount(name);
        }
        op.setTS(this.replicaId, this.replicaTS);
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
        if (TSBiggerThan(this.replicaTS, prevTS)) {
            updateValueTS();
            removeAccount(name);
        }
        op.setTS(this.replicaId, this.replicaTS);
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
        if (TSBiggerThan(this.replicaTS, prevTS)) {
            updateValueTS();
            updateAccount(from, -amount);
            updateAccount(to, amount);
        }
        op.setTS(this.replicaId, this.replicaTS);
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
        if (!TSBiggerThan(this.replicaTS, prevTS)) {
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
        for (Operation op : ledger) {
            if (TSBiggerThan(this.replicaTS, op.getTS())) // duplicate operation
                continue;
            addOperation(op);
            mergeReplicaTS(op.getTS());
        }
        Logger.log("State propagated, now going to execute ledger");
        boolean noMoreExecutions = false;
        // now we will go through all operations in the ledger and execute them if they
        // are stable, updating the replicaTS after each execution
        while (!noMoreExecutions) {
            noMoreExecutions = true;
            for (Operation op : getLedger()) {
                Logger.log("Checking operation " + op.toString() + " for execution");
                if (TSBiggerThan(this.valueTS, op.getTS()))
                    continue; // ignore operations already executed
                if (TSBiggerThan(this.valueTS, op.getPrevTS())) { // prevTS < valueTS -> we can execute the operation
                    op.executeOperation(this);
                    // set operation TS if it is uninitialized
                    mergeValueTS(op.getTS());
                    noMoreExecutions = false; // if we can perform an operation, we need to go through the ledger again
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

    /**
     * public void gossip() {
     * Logger.log("Propagating state to other servers");
     * try {
     * List<String> hosts = namingServerService.lookup(SERVICE,
     * secondaryServer).getHostsList();
     * crossServerService.propagateState(getLedger(), hosts, this.replicaTS);
     * } catch (Exception e) {
     * throw new FailedToPropagateException();
     * }
     * }
     */

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
