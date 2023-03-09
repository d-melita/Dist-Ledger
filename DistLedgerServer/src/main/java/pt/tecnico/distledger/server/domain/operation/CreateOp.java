package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class CreateOp extends Operation {

    public CreateOp(String account, OperationType type) {
        super(account, type);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(Convertor convertor) {
        return convertor.convert(this);
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                ", type=" + getType() +
                '}';
    }
}
