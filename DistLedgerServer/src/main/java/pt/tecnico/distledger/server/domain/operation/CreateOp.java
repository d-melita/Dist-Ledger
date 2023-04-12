package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Convertor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.List;

public class CreateOp extends Operation {

    public CreateOp(String account, List<Integer> prevTS) {
        super(account, prevTS);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(Convertor convertor) {
        return convertor.convert(this);
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
