package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.ArrayList;
import java.util.List;

public class Deserializer {
    public Operation deserialize(DistLedgerCommonDefinitions.Operation op) {
        switch (op.getType()) {
            case OP_CREATE_ACCOUNT:
                return new CreateOp(op.getUserId(), op.getPrevTSList(), op.getTSList());
            case OP_DELETE_ACCOUNT:
                return new DeleteOp(op.getUserId(), op.getPrevTSList(), op.getTSList());
            case OP_TRANSFER_TO:
                return new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount(), op.getPrevTSList(),
                        op.getTSList());
            default:
                return null;
        }
    }

    public List<Operation> deserialize(List<DistLedgerCommonDefinitions.Operation> ops) {
        List<Operation> deserializedOps = new ArrayList<>();
        for (DistLedgerCommonDefinitions.Operation op : ops) {
            deserializedOps.add(deserialize(op));
        }
        return deserializedOps;
    }
}