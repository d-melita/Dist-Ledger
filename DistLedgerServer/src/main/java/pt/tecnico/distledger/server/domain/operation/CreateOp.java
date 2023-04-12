package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.Serializer;

import java.util.List;

public class CreateOp extends Operation {

    public CreateOp(String account, List<Integer> prevTS, List<Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.serialize(this);
    }

    @Override
    public void executeOperation(ServerState state) {
        // TODO: to avoid exposing server to Operation, we could create an Executor
        // class for this
        state.executeOperation(this);
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
