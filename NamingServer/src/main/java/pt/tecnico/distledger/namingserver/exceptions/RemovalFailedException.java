package pt.tecnico.distledger.namingserver.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class RemovalFailedException extends RuntimeException {
    public RemovalFailedException(String host) {
        super("Failed to remove " + host);
        Logger.log(getMessage());
    }
}
