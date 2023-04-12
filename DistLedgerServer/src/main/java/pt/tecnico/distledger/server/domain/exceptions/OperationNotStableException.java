package pt.tecnico.distledger.server.domain.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class OperationNotStableException extends RuntimeException {
    public OperationNotStableException() {
        super("Cannot perform operation, server is not stable");
        Logger.log(getMessage());
    }
}
