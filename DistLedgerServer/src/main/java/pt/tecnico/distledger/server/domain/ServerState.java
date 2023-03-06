package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.domain.userAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServerState {
    private List<Operation> ledger;

    Map<userAccount, Integer> accounts;

    private boolean active = false;

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

    public void createAccount(String name) {
        userAccount account = new userAccount(name, INITIAL_BALANCE);
        addAccount(account);
        CreateOp op = new CreateOp(name);
        addOperation(op);
    }

    private void addAccount(userAccount account) {
        this.accounts.put(account, account.getBalance());
    }

    private void updateAccountBalance(userAccount account, Integer balance) {
        this.accounts.put(account, balance); // put replaces the value if the key already exists
    }

    public void deleteAccount(String name) {
        for (Map.Entry<userAccount, Integer> entry : this.accounts.entrySet()) {
            if (entry.getKey().getName().equals(name) && entry.getValue() == 0) {
                this.accounts.remove(entry.getKey());
            }
        }
        // account does not exist -> throw exception TODO

        DeleteOp op = new DeleteOp(name);
        addOperation(op);
    }

    public Integer getAccountBalance(String name) {
        for (Map.Entry<userAccount, Integer> entry : this.accounts.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                return entry.getValue();
            }
        }
        // account does not exist -> throw exception TODO
        return null;
    }

    public void transfer(String from, String to, Integer amount) {
        // verificar se tem dinheiro suficiente
        // Integer fromBalance = getAccountBalance(from);
        // if (fromBalance < amount) {
        //     System.out.println("Not enough money");
        //     return;
        // }
        // verificar se conta destino existe
        // if (getAccountBalance(to) == null) {
        //     System.out.println("Destination account does not exist");
        //     return;
        // }

        // update accounts
        for (Map.Entry<userAccount, Integer> entry : this.accounts.entrySet()) {
            if (entry.getKey().getName().equals(from)) {
                updateAccountBalance(entry.getKey(), entry.getValue() - amount);
            }

            if (entry.getKey().getName().equals(to)) {
                updateAccountBalance(entry.getKey(), entry.getValue() + amount);
            }
        }

        // add operation to ledger
        TransferOp op = new TransferOp(from, to, amount);
        addOperation(op);
        
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public List<Operation> getLedger() {
        return this.ledger;
    }

    public Map<userAccount, Integer> getAccounts() {
        return this.accounts;
    }

    public void printLedger() {
        for (Operation op : this.ledger) {
            System.out.println(op.toString());
        }
    }

    public void printAccounts() {
        for (Map.Entry<userAccount, Integer> entry : this.accounts.entrySet()) {
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
