package pt.tecnico.distledger.server.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class AccountDoesntExistException extends RuntimeException {
    public AccountDoesntExistException() {
        super("Account doesn't exist");
        Logger.log(getMessage());
    }

    public AccountDoesntExistException(String name) {
        super("Account " + name + " doesn't exist");
        Logger.log(getMessage());
    }

    public AccountDoesntExistException(String from, String to) {
        super("Account " + from + " and " + to + " don't exist");
        Logger.log(getMessage());
    }
}
