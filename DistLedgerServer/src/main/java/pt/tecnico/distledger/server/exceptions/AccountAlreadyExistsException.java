package pt.tecnico.distledger.server.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String name) {
        super("Account " + name + " already exists");
        Logger.log(getMessage());
    }
}
