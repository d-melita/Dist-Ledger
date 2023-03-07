package pt.tecnico.distledger.server.exceptions;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String name) {
        super("Account " + name + " already exists");
    }
}
