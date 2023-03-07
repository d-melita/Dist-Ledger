package pt.tecnico.distledger.server.exceptions;

public class AccountHasBalanceException extends RuntimeException {
    public AccountHasBalanceException() {
        super("Cannot delete account, it has balance remaining");
    }

    public AccountHasBalanceException(String name) {
        super("Cannot delete account, " + name + " has balance remaining");
    }
}
