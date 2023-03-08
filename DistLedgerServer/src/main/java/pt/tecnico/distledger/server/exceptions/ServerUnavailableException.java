package pt.tecnico.distledger.server.exceptions;

public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException() {
        super("Server is unavailable");
    }
}
