package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.OperationConverter;
import pt.tecnico.distledger.server.domain.ServerState;

public class DeleteOp extends Operation {

    public DeleteOp(String account) {
        super(account);
    }

    @Override
    public void convert(OperationConverter converter) {
        converter.convert(this);
    }

    @Override
    public void executeOperation(ServerState state) {
        // TODO: to avoid exposing server to Operation, we could create an Executor
        // class for this
        state.executeOperation(this);
    }

    @Override
    public String toString() {
        return "DeleteOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
