package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private final List<Operation> ledger;

    Map<String, Integer> accounts;

    private boolean isActive = true;
    private static final String BROKER = "broker";

    public ServerState() {
        Logger.log("Initializing ServerState");
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<>();
        Logger.log("Creating Broker Account");
        this.addAccount(BROKER, 1000);
        Logger.log("Broker Account created");
        Logger.log("ServerState initialized");
    }

    public synchronized void addOperation(Operation op) {
        Logger.log("Adding operation " + op.toString() + " to ledger");
        this.ledger.add(op);
        Logger.log("Operation added");
    }

    // User Interface Operations

    public synchronized void createAccount(String name) {
        Logger.log("Creating account \'" + name + "\'");
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (accountExists(name)) {
            throw new AccountAlreadyExistsException(name);
        }
        addAccount(name);
        addOperation(new CreateOp(name));
        Logger.log("Account \'" + name + "\' created");
    }

    public synchronized void deleteAccount(String name) {
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
        if (getAccountBalance(name) > 0) {
            throw new AccountHasBalanceException(name);
        }
        removeAccount(name);
        addOperation(new DeleteOp(name));
        Logger.log("Account \'" + name + "\' deleted");
    }

    public synchronized void transferTo(String from, String to, Integer amount) {
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
        updateAccount(from, -amount);
        updateAccount(to, amount);
        addOperation(new TransferOp(from, to, amount));
        Logger.log("Transfer completed");
    }

    public synchronized Integer getAccountBalance(String name) {
        Logger.log("Getting balance of account \'" + name + "\'");
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
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

    public synchronized void receivePropagatedLedger(List<Operation> ledger) {
        Logger.log("Receiving setting ledger");
        this.ledger.clear();
        this.ledger.addAll(ledger);
        // TODO: for gossip we will probably need to change this method
        executeLedgerOperations();
        Logger.log("Ledger set");
    }

    private void executeLedgerOperations() {
        // TODO: for gossip we will probably need to change this method
        // TODO: this will execute all the ledger operations again, we should only
        // execute the new ones, as it is right now it is wrong
        // TODO: this might be cleaner and better/easier to implement with a dedicated
        // Executor class
        Logger.log("Executing ledger operations");
        for (Operation op : ledger) {
            op.executeOperation(this);
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

    // Checker methods

    public synchronized boolean isActive() {
        return this.isActive;
    }

    private synchronized boolean accountExists(String name) {
        return accounts.get(name) != null;
    }

    private synchronized boolean accountHasBalance(String name, int amount) {
        return accounts.get(name) >= amount;
    }

    @Override
    public synchronized String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                ", accounts=" + accounts +
                '}';
    }
}
