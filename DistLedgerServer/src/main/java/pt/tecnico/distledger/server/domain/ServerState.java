package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.userAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServerState {
    private List<Operation> ledger;

    Map<userAccount, Integer> accounts;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
    }

    public void addOperation(Operation op) {
        this.ledger.add(op);
    }

    public void addAccount(userAccount account) {
        this.accounts.put(account, account.getBalance());
    }

    public void updateAccount(userAccount account, Integer balance) {
        this.accounts.put(account, balance); // put replaces the value if the key already exists
    }

    public Integer getAccountBalance(userAccount account) {
        return this.accounts.get(account);
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
