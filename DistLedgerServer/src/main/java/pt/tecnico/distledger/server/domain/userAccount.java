package pt.tecnico.distledger.server.domain;

public class userAccount {
    
    String name;

    Integer balance;

    public userAccount(String name, Integer balance) {
        setName(name);
        setBalance(balance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public void addBalance(Integer balance) {
        this.balance += balance;
    }

    public void subBalance(Integer balance) {
        this.balance -= balance;
    }

    @Override
    public String toString() {
        return "userAccount{" +
                "name='" + name +
                ", balance=" + balance +
                '}';
    }
}
