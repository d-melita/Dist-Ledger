package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public final class Convertor {
    public static DistLedgerCommonDefinitions.Operation convert(Operation operation) {
        switch (operation.getType()) {
            case CREATE_ACCOUNT:
                return DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(
                                DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
                        .setUserId(operation.getAccount())
                        .build();
            case DELETE_ACCOUNT:
                return DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(
                                DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT)
                        .setUserId(operation.getAccount())
                        .build();
            case TRANSFER_TO:
                TransferOp transferOp = (TransferOp) operation;
                return DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(
                                DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
                        .setUserId(operation.getAccount())
                        .setDestUserId(transferOp.getDestAccount())
                        .setAmount(transferOp.getAmount())
                        .build();
            default:
                throw new RuntimeException();
        }
    }

    public static Operation convert(DistLedgerCommonDefinitions.Operation operation) {
        switch (operation.getType()) {
            case OP_CREATE_ACCOUNT:
                return new CreateOp(operation.getUserId());
            case OP_DELETE_ACCOUNT:
                return new DeleteOp(operation.getUserId());
            case OP_TRANSFER_TO:
                return new TransferOp(operation.getUserId(), operation.getDestUserId(), operation.getAmount());
            default:
                throw new RuntimeException();
        }
    }
}
