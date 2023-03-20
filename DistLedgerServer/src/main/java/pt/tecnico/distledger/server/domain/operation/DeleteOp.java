package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Convertor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class DeleteOp extends Operation {

    public DeleteOp(String account) {
        super(account);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation accept(Convertor convertor) {
        return convertor.convert(this);
    }

    @Override
    public String toString() {
        return "DeleteOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
