package pt.tecnico.distledger.server;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

public class Serializer {
    public DistLedgerCommonDefinitions.Operation serialize(CreateOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_CREATE_ACCOUNT)
        .setUserId(op.getAccount())
        .addAllPrevTS(op.getPrevTS())
        .addAllTS(op.getTS())
        .build();
    }

    public DistLedgerCommonDefinitions.Operation serialize(DeleteOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_DELETE_ACCOUNT)
        .setUserId(op.getAccount())
        .build();
    }

    public DistLedgerCommonDefinitions.Operation serialize(TransferOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_TRANSFER_TO)
        .setUserId(op.getAccount())
        .setDestUserId(op.getDestAccount())
        .setAmount(op.getAmount())
        .addAllPrevTS(op.getPrevTS())
        .addAllTS(op.getTS())
        .build();
    }
}
