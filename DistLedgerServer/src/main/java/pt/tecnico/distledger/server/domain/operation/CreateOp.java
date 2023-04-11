package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Serializer;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.List;

public class CreateOp extends Operation {

    public CreateOp(String account, List<Integer> prevTS, List<Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(Serializer serializer) {
        return serializer.serialize(this);
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
