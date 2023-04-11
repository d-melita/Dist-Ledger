package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

import java.util.ArrayList;
import java.util.List;

public class OperationConverter {
    List<DistLedgerCommonDefinitions.Operation> protoOperations = new ArrayList<>();

    public List<DistLedgerCommonDefinitions.Operation> convertToProto(List<Operation> ledgerOperations) {
        for (Operation operation : ledgerOperations) {
            operation.convert(this);
        }
        return protoOperations;
    }

    public List<Operation> convertToLedger(List<DistLedgerCommonDefinitions.Operation> protoOperations) {
        List<Operation> ledgerOperations = new ArrayList<>();
        for (DistLedgerCommonDefinitions.Operation protoOperation : protoOperations) {
            switch (protoOperation.getType()) {
                case OP_CREATE_ACCOUNT:
                    ledgerOperations.add(new CreateOp(protoOperation.getUserId()));
                    break;
                case OP_DELETE_ACCOUNT:
                    ledgerOperations.add(new DeleteOp(protoOperation.getUserId()));
                    break;
                case OP_TRANSFER_TO:
                    ledgerOperations.add(new TransferOp(protoOperation.getUserId(), protoOperation.getDestUserId(),
                            protoOperation.getAmount()));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid operation type");
            }
        }
        return ledgerOperations;
    }

    public void convert(CreateOp operation) {
        // create ProtoOperation from Operation
        DistLedgerCommonDefinitions.Operation protoOperation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(OperationType.OP_CREATE_ACCOUNT)
                .setUserId(operation.getAccount())
                .build();
        protoOperations.add(protoOperation);
    }

    public void convert(DeleteOp operation) {
        // create ProtoOperation from Operation
        DistLedgerCommonDefinitions.Operation protoOperation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(OperationType.OP_DELETE_ACCOUNT)
                .setUserId(operation.getAccount())
                .build();
        protoOperations.add(protoOperation);
    }

    public void convert(TransferOp operation) {
        // create ProtoOperation from Operation
        DistLedgerCommonDefinitions.Operation protoOperation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(OperationType.OP_TRANSFER_TO)
                .setUserId(operation.getAccount())
                .setDestUserId(operation.getDestAccount())
                .setAmount(operation.getAmount())
                .build();
        protoOperations.add(protoOperation);
    }

}
