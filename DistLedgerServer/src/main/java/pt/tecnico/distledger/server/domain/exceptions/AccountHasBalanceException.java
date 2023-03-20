package pt.tecnico.distledger.server.domain.exceptions;

import pt.tecnico.distledger.utils.Logger;

public class AccountHasBalanceException extends RuntimeException {
    public AccountHasBalanceException() {
        super("Cannot delete account, it has balance remaining");
        Logger.log(getMessage());
    }

    public AccountHasBalanceException(String name) {
        super("Cannot delete account, " + name + " has balance remaining");
        Logger.log(getMessage());
    }
}
