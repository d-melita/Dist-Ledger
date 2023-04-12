package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

import java.util.ArrayList;
import java.util.List;

public class Serializer {
    List<DistLedgerCommonDefinitions.Operation> protoOperations = new ArrayList<>();

    public List<DistLedgerCommonDefinitions.Operation> serializOperations(List<Operation> ledgerOperations) {
        for (Operation operation : ledgerOperations) {
            operation.serialize(this);
        }
        return protoOperations;
    }

    public void serialize(CreateOp operation) {
        // create ProtoOperation from Operation
        DistLedgerCommonDefinitions.Operation protoOperation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(OperationType.OP_CREATE_ACCOUNT)
                .setUserId(operation.getAccount())
                .build();
        protoOperations.add(protoOperation);
    }

    public void serialize(DeleteOp operation) {
        // create ProtoOperation from Operation
        DistLedgerCommonDefinitions.Operation protoOperation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(OperationType.OP_DELETE_ACCOUNT)
                .setUserId(operation.getAccount())
                .build();
        protoOperations.add(protoOperation);
    }

    public void serialize(TransferOp operation) {
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