package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.OperationConverter;
import pt.tecnico.distledger.server.domain.ServerState;

public abstract class Operation {

    public enum OperationType {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        TRANSFER_TO
    }

    private String account;

    public Operation(String fromAccount) {
        this.account = fromAccount;
    }

    public String getAccount() {
        return account;
    }

    public abstract void convert(OperationConverter converter);

    public abstract void executeOperation(ServerState state);

    @Override
    public String toString() {
        return "Operation{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
