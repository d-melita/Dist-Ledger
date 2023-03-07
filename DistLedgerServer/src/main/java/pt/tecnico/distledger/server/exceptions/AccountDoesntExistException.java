package pt.tecnico.distledger.server.exceptions;

public class AccountDoesntExistException extends RuntimeException {
    public AccountDoesntExistException() {
        super("Account doesn't exist");
    }

    public AccountDoesntExistException(String name) {
        super("Account " + name + " doesn't exist");
    }

    public AccountDoesntExistException(String from, String to) {
        super("Account " + from + " and " + to + " don't exist");
    }
}
