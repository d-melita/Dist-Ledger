package pt.tecnico.distledger.server.domain.operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

public class DeleteOp extends Operation {

    public DeleteOp(String account, OperationType type) {
        super(account, type);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation convertToProto() {
        return pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(getType())
                .setUserId(getAccount())
                .build();
    }

}
