package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.tecnico.distledger.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServerState {
    private List<Operation> ledger;

    Map<String, userAccount> accounts;

    private boolean active = true;

    static final String UNAVAILABLE = "UNAVAILABLE";
    static final String BROKER_NAME = "broker";
    static final Integer BROKER_INITIAL_BALANCE = 1000;
    static final Integer INITIAL_BALANCE = 0;

    public ServerState() {
        Logger.log("Initializing ServerState");
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        createBrokerAccount();
        Logger.log("ServerState initialized");
    }

    // TODO - CHECK IF STATE IS UNAVAILABLE - IF SO, RESPOND
    // TO CLIENT WITH UNAVAILABLE MESSAGE

    private void createBrokerAccount() {
        Logger.log("Creating Broker Account");
        userAccount broker = new userAccount(BROKER_NAME, BROKER_INITIAL_BALANCE);
        addAccount(broker);
        Logger.log("Broker Account created");
    }

    public void addOperation(Operation op) {
        Logger.log("Adding operation " + op.toString() + " to ledger");
        this.ledger.add(op);
        Logger.log("Operation added");
    }

    // User Interface Operations
    public void createAccount(String name) {
        Logger.log("Creating account " + name);
        if (accountExists(name)) {
            throw new AccountAlreadyExistsException(name);
        }
        userAccount account = new userAccount(name, INITIAL_BALANCE);
        addAccount(account);
        CreateOp op = new CreateOp(name, OperationType.OP_CREATE_ACCOUNT);
        addOperation(op);
        Logger.log("Account " + name + " created");
    }

    public void deleteAccount(String name) {
        Logger.log("Deleting account " + name);
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        if (getAccountBalance(name) > 0) {
            throw new AccountHasBalanceException(name);
        }
        accounts.remove(name);
        DeleteOp op = new DeleteOp(name, OperationType.OP_DELETE_ACCOUNT);
        addOperation(op);
        Logger.log("Account " + name + " deleted");
    }

    public void transfer(String from, String to, Integer amount) {
        Logger.log("Transferring " + amount + " from " + from + " to " + to);
        if (!accountExists(from) && !accountExists(to)) {
            throw new AccountDoesntExistException(from, to);
        } else if (!accountExists(from)) {
            throw new AccountDoesntExistException(from);
        } else if (!accountExists(to)) {
            throw new AccountDoesntExistException(to);
        }
        if (!accountHasBalance(from, amount)) {
            throw new InsufficientFundsException(from);
        }
        updateAccountBalance(accounts.get(from), accounts.get(from).getBalance() - amount);
        updateAccountBalance(accounts.get(to), accounts.get(to).getBalance() + amount);
        TransferOp op = new TransferOp(from, to, amount, OperationType.OP_TRANSFER_TO);
        addOperation(op);
        Logger.log("Transfer completed");
    }

    public Integer getAccountBalance(String name) {
        Logger.log("Getting balance of account " + name);
        int balance = 0;
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        return accounts.get(name).getBalance();
    }

    // User Interface Operations - Helper Methods
    private void addAccount(userAccount account) {
        Logger.log("Adding account " + account.getName() + " to server");
        this.accounts.put(account.getName(), account);
        Logger.log("Account " + account.getName() + " added to server");
    }

    private void updateAccountBalance(userAccount account, Integer balance) {
        Logger.log("Updating balance of account " + account.getName() + " to " + balance);
        account.setBalance(balance);
    }

    public boolean accountExists(String name) {
        return accounts.get(name) != null;
    }

    public boolean accountHasBalance(String name, Integer amount) {
        return accounts.get(name).getBalance() >= amount;
    }

    // Admin interface operations

    public void activate() {
        Logger.log("Admin activating server");
        this.active = true;
    }

    public void deactivate() {
        Logger.log("Admin deactivating server");
        this.active = false;
    }

    public List<Operation> getLedger() {
        Logger.log("Admin getting ledger");
        return this.ledger;
    }

    public boolean isActive() {
        return this.active;
    }

    public Map<String, userAccount> getAccounts() {
        return this.accounts;
    }

    public void printLedger() {
        for (Operation op : this.ledger) {
            System.out.println(op.toString());
        }
    }

    public void printAccounts() {
        for (Map.Entry<String, userAccount> entry : this.accounts.entrySet()) {
            System.out.println(entry.getKey().toString() + " " + entry.getValue());
        }
    }

    @Override
    public String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                ", accounts=" + accounts +
                '}';
    }

}
