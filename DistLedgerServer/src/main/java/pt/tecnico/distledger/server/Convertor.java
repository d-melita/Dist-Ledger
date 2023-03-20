package pt.tecnico.distledger.server;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

public class Convertor {
    public DistLedgerCommonDefinitions.Operation convert(CreateOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_CREATE_ACCOUNT)
        .setUserId(op.getAccount())
        .build();
    }

    public DistLedgerCommonDefinitions.Operation convert(DeleteOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_DELETE_ACCOUNT)
        .setUserId(op.getAccount())
        .build();
    }

    public DistLedgerCommonDefinitions.Operation convert(TransferOp op){
        return DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(OperationType.OP_TRANSFER_TO)
        .setUserId(op.getAccount())
        .setDestUserId(op.getDestAccount())
        .setAmount(op.getAmount())
        .build();
    }
}
