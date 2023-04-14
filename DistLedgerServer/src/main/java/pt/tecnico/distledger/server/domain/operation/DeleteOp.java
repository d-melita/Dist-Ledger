package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.Serializer;

import java.util.List;

public class DeleteOp extends Operation {

    public DeleteOp(String account, List<Integer> prevTS, List<Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.serialize(this);
    }

    @Override
    public void executeOperation(ServerState state) {
        state.executeOperation(this);
    }

    @Override
    public String toString() {
        return "DeleteOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
