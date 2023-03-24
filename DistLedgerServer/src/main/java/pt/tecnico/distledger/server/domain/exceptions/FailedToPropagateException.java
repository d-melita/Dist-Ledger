package pt.tecnico.distledger.server.domain.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class FailedToPropagateException extends RuntimeException {
    public FailedToPropagateException() {
        super("Fallback server isn't available, can't propagate state");
        Logger.log("Server unavailable");
    }
}
