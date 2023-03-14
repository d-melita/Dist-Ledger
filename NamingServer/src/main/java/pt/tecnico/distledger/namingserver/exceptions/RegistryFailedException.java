package pt.tecnico.distledger.namingserver.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class RegistryFailedException extends RuntimeException {
    public RegistryFailedException(String host) {
        super("Failed to register " + host);
        Logger.log(getMessage());
    }
}
