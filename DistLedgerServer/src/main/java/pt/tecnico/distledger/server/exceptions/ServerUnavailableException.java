package pt.tecnico.distledger.server.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException() {
        super("UNAVAILABLE");
        Logger.log("Server unavailable");
    }
}
