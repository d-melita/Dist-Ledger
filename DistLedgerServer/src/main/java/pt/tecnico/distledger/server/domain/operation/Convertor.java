package pt.tecnico.distledger.server.domain.operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class Convertor {
    public DistLedgerCommonDefinitions.Operation convertCreateOrDelete(Operation op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(op.getType())
        .setUserId(op.getAccount())
        .build();
    }

    public DistLedgerCommonDefinitions.Operation convertTransferTo(TransferOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(op.getType())
        .setUserId(op.getAccount())
        .setDestUserId(op.getDestAccount())
        .setAmount(op.getAmount())
        .build();
    }
}
