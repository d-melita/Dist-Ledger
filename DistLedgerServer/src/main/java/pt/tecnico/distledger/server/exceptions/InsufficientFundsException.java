package pt.tecnico.distledger.server.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String name) {
        super("Account " + name + " has insufficient funds for this operation");
    }
}
