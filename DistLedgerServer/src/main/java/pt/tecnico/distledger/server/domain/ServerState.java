package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.tecnico.distledger.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerState {
    private final List<Operation> ledger;

    Map<String, Integer> accounts;

    private boolean isActive = true;

    public ServerState() {
        Logger.log("Initializing ServerState");
        this.ledger = new CopyOnWriteArrayList<>();
        this.accounts = new ConcurrentHashMap<>();
        Logger.log("Creating Broker Account");
        this.accounts.put("broker", 1000);
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
        Logger.log("Creating account " + name);
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (accountExists(name)) {
            throw new AccountAlreadyExistsException(name);
        }
        accounts.put(name, 0);
        addOperation(new CreateOp(name, OperationType.OP_CREATE_ACCOUNT));
        Logger.log("Account " + name + " created");
    }

    public synchronized void deleteAccount(String name) {
        Logger.log("Deleting account " + name);
        if (!isActive) {
            throw new ServerUnavailableException();
        }
        if (name.equals("broker")) {
            throw new DeleteBrokerAccountException(name);
        }
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        if (getAccountBalance(name) > 0) {
            throw new AccountHasBalanceException(name);
        }
        accounts.remove(name);
        addOperation(new DeleteOp(name, OperationType.OP_DELETE_ACCOUNT));
        Logger.log("Account " + name + " deleted");
    }

    public synchronized void transfer(String from, String to, Integer amount) {
        Logger.log("Transferring " + amount + " from " + from + " to " + to);
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
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(to, accounts.get(to) + amount);
        addOperation(new TransferOp(from, to, amount, OperationType.OP_TRANSFER_TO));
        Logger.log("Transfer completed");
    }

    public synchronized Integer getAccountBalance(String name) {
        Logger.log("Getting balance of account " + name);
        if (!isActive) {
            throw new ServerUnavailableException();
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
    }

    public synchronized void deactivate() {
        Logger.log("Admin deactivating server");
        this.isActive = false;
    }

    public synchronized boolean isActive() {
        return this.isActive;
    }

    public synchronized List<Operation> getLedger() {
        Logger.log("Admin getting ledger");
        return this.ledger;
    }

    @Override
    public synchronized String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                ", accounts=" + accounts +
                '}';
    }
}
