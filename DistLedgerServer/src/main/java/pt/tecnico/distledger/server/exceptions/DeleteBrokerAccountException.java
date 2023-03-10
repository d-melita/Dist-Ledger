package pt.tecnico.distledger.server.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class DeleteBrokerAccountException extends RuntimeException {
    public DeleteBrokerAccountException(String name) {
        super("Account " + name + " cannot be deleted");
        Logger.log(getMessage());
    }
}
