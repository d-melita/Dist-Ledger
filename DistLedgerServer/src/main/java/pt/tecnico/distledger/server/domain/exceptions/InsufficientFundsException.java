package pt.tecnico.distledger.server.domain.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String name) {
        super("Account " + name + " has insufficient funds for this operation");
        Logger.log(getMessage());
    }
}
