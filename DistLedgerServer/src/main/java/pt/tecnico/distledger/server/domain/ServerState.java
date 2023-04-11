package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.grpc.CrossServerService;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.tecnico.distledger.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private final List<Operation> ledger;

    Map<String, Integer> accounts;

    private boolean isActive = true;

    private List<Integer> replicaTS = new ArrayList<>();

    private String qualifier;
    private String secondaryServer;

    private CrossServerService crossServerService;
    private final NamingServerService namingServerService;

    private static final String SERVICE = "DistLedger";
    private static final String BROKER = "broker";

    public ServerState(NamingServerService namingServerService) {
        Logger.log("Initializing ServerState");
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<>();
        Logger.log("Creating Broker Account");
        this.accounts.put(BROKER, 1000);
        Logger.log("Broker Account created");
        this.namingServerService = namingServerService;
        Logger.log("ServerState initialized");
        this.replicaTS.add(0);
        this.replicaTS.add(0);
    }

    public ServerState(NamingServerService namingServerService, CrossServerService crossServerService,
            String qualifier) {
        this(namingServerService);
        this.crossServerService = crossServerService;
        this.qualifier = qualifier;
        setSecondaryServer(qualifier);
    }

    public void setSecondaryServer(String qualifier) {
        if (qualifier.equals("A")) {
            this.secondaryServer = "B";
        } else {
            this.secondaryServer = "A";
        }
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
        for (Integer i : prevTS) {
            if (i > replicaTS.get(prevTS.indexOf(i))) {
                return false;
            }
        }
        return true;
    }

    // User Interface Operations
    public synchronized void createAccount(String name, List<Integer> prevTS) {
        Logger.log("Creating account \'" + name + "\'");
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
        addOperation(new CreateOp(name, prevTS, null));
        Logger.log("Account \'" + name + "\' created");
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

    public synchronized void transfer(String from, String to, Integer amount, List<Integer> prevTS) {
        Logger.log("Transferring " + amount + " from \'" + from + "\' to \'" + to + "\'");
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
    }

    public synchronized Integer getAccountBalance(String name, List<Integer> prevTS) {
        Logger.log("Getting balance of account \'" + name + "\'");
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (!operationIsStable(prevTS)) {
            throw new OperationNotStableException();
        }
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        return accounts.get(name);
    }

    public synchronized boolean accountExists(String name) {
        return accounts.get(name) != null;
    }

    public synchronized boolean accountHasBalance(String name, int amount) {
        return accounts.get(name) >= amount;
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

    public synchronized boolean isActive() {
        return this.isActive;
    }

    public List<Operation> getLedger() {
        Logger.log("Admin getting ledger");
        // create a copy of the ledger to avoid concurrent modification
        List<Operation> ledgerCopy = new CopyOnWriteArrayList<>();
        ledgerCopy.addAll(ledger);
        return ledgerCopy;
    }

    public synchronized void setLedger(List<Operation> ledger) {
        Logger.log("Admin setting ledger");
        this.ledger.clear();
        this.ledger.addAll(ledger);
        Logger.log("Ledger set");
    }

    public void gossip() {
        Logger.log("Propagating state to other servers");
        try {
            List<String> hosts = namingServerService.lookup(SERVICE, secondaryServer).getHostsList();
            crossServerService.propagateState(getLedger(), hosts, this.replicaTS);
        } catch (Exception e) {
            throw new FailedToPropagateException();
        }
    }

    private void updateReplicaTSAfterGossip(Operation op) {
        if (this.qualifier.equals("A")){
            this.replicaTS.set(1, op.getTS().get(1));
        }
        else if (this.qualifier.equals("B")){
            this.replicaTS.set(0, op.getTS().get(0));
        }
    }

    public boolean isBiggerTS(List<Integer> prevTS) {
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

    public void executeOperation(Operation op) {
        if (op instanceof CreateOp) {
            addAccount(((CreateOp) op).getAccount());
        } else if (op instanceof DeleteOp) {
            removeAccount(((DeleteOp) op).getAccount());
        } else if (op instanceof TransferOp) {
            TransferOp transferOp = (TransferOp) op;
            updateAccount(transferOp.getAccount(), -transferOp.getAmount());
            updateAccount(transferOp.getDestAccount(), transferOp.getAmount());
        }
    }

    public void propagateState(List<Operation> ledger) {
        Logger.log("Propagating state");
        for (Operation op : ledger) {
            if (isBiggerTS(op.getTS())) // duplicate operation
                continue;
            if (isBiggerTS(op.getPrevTS()) && !isBiggerTS(op.getTS())) { // operation is stable and can be executed
                executeOperation(op);
                updateReplicaTSAfterGossip(op);
            }
            addOperation(op);
        }
        boolean noMoreExecutions = false;
        // now we will go through all operations in the ledger and execute them if they are stable, updating the replicaTS after each execution
        while (!noMoreExecutions) {
            noMoreExecutions = true;
            for (Operation op : getLedger()) {
                if (isBiggerTS(op.getPrevTS())) {
                    executeOperation(op);
                    // updateReplicaTS
                    // set operation TS if it is null
                    if (op.getTS() == null) {
                        updateReplicaTS();
                        op.setTS(getReplicaTS());
                    }
                    else {
                        updateReplicaTSAfterGossip(op);
                    }
                    noMoreExecutions = false; // if we can permorm an operation, we need to go through the ledger again
                    
                }
            }
        }
    }

    public synchronized void addAccount(String name) {
        this.accounts.put(name, 0);
    }

    public synchronized void removeAccount(String name) {
        this.accounts.remove(name);
    }

    public synchronized void updateAccount(String name, int amount) {
        accounts.put(name, accounts.get(name) + amount);
    }

    public synchronized List<Integer> getReplicaTS() {
        return this.replicaTS;
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
