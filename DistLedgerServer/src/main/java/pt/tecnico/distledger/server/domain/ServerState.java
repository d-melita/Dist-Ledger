package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.exceptions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerState {
    private ArrayList<Operation> ledger;

    HashMap<String, Integer> accounts;

    private boolean isActive = true;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.accounts.put("broker", 1000);
    }

    public void addOperation(Operation op) {
        this.ledger.add(op);
    }

    // User Interface Operations
    public void createAccount(String name) {
        if (!isActive) throw new ServerUnavailableException();
        if (accountExists(name)) {
            throw new AccountAlreadyExistsException(name);
        }
        accounts.put(name, 0);
        addOperation(new CreateOp(name, OperationType.OP_CREATE_ACCOUNT));
    }

    public void deleteAccount(String name) {
        if (!isActive) throw new ServerUnavailableException();
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        if (getAccountBalance(name) > 0) {
            throw new AccountHasBalanceException(name);
        }
        accounts.remove(name);
        addOperation(new DeleteOp(name, OperationType.OP_DELETE_ACCOUNT));
    }

    public void transfer(String from, String to, Integer amount) {
        if (!isActive) throw new ServerUnavailableException();
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
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(to, accounts.get(to) + amount);
        addOperation(new TransferOp(from, to, amount, OperationType.OP_TRANSFER_TO));
    }

    public Integer getAccountBalance(String name) {
        if (!isActive) throw new ServerUnavailableException();
        int balance = 0;
        if (!accountExists(name)) {
            throw new AccountDoesntExistException(name);
        }
        return accounts.get(name);
    }

    public boolean accountExists(String name) {
        return accounts.get(name) != null;
    }

    public boolean accountHasBalance(String name, int amount) {
        return accounts.get(name) >= amount;
    }

    // Admin interface operations

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public ArrayList<Operation> getLedger() {
        return this.ledger;
    }

    @Override
    public String toString() {
        return "ServerState{" +
                "ledger=" + ledger +
                ", accounts=" + accounts +
                '}';
    }

}
