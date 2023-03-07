package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

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
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        createBrokerAccount();
    }

    // TODO - CHECK IF STATE IS UNAVAILABLE - IF SO, RESPOND
    // TO CLIENT WITH UNAVAILABLE MESSAGE

    private void createBrokerAccount() {
        userAccount broker = new userAccount(BROKER_NAME, BROKER_INITIAL_BALANCE);
        addAccount(broker);
    }

    public void addOperation(Operation op) {
        this.ledger.add(op);
    }

    // User Interface Operations
    public void createAccount(String name) {
        userAccount account = new userAccount(name, INITIAL_BALANCE);
        addAccount(account);
        CreateOp op = new CreateOp(name, OperationType.OP_CREATE_ACCOUNT);
        addOperation(op);
    }

    public void deleteAccount(String name) {
        if (!accountExists(name)) {
            System.out.println("Account does not exist");
            return;
        }
        if (getAccountBalance(name) > 0){
            System.out.println("Account has balance, cannot remove");
            return;
        }
        accounts.remove(name);
        DeleteOp op = new DeleteOp(name, OperationType.OP_DELETE_ACCOUNT);
        addOperation(op);
    }

    public void transfer(String from, String to, Integer amount) {
        if (!accountExists(from) || !accountExists(to)) {
            // TODO - THROW EXCEPTION
            System.out.println("Account does not exist");
            return;
        }
        if (!accountHasBalance(from, amount)) {
            // TODO - THROW EXCEPTION
            System.out.println("Insufficient funds");
            return;
        }
        updateAccountBalance(accounts.get(from), accounts.get(from).getBalance() - amount);
        updateAccountBalance(accounts.get(to), accounts.get(to).getBalance() + amount);
        TransferOp op = new TransferOp(from, to, amount, OperationType.OP_TRANSFER_TO);
        addOperation(op);
    }

    public Integer getAccountBalance(String name) {
        int balance = 0;
        if (!accountExists(name)) {
            // TODO - THROW EXCEPTION
            System.out.println("Account does not exist");
            return balance;
        }
    return accounts.get(name).getBalance();
    }

    // User Interface Operations - Helper Methods
    private void addAccount(userAccount account) {
        this.accounts.put(account.getName(), account);
    }

    private void updateAccountBalance(userAccount account, Integer balance) {
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
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public List<Operation> getLedger() {
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
