package pt.tecnico.distledger.server.service;

import java.util.ArrayList;
import java.util.List;

import io.grpc.Status;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import io.grpc.stub.StreamObserver;

public class CrossServerDistLedgerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
    
    private ServerState state;

    public CrossServerDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        List<Operation> newLedger = new ArrayList<>();
        for (DistLedgerCommonDefinitions.Operation op : request.getState().getLedgerList()){
            switch (op.getType()) {
                case OP_CREATE_ACCOUNT:
                    newLedger.add(new CreateOp(op.getUserId()));
                    break;
                case OP_DELETE_ACCOUNT:
                    newLedger.add(new DeleteOp(op.getUserId()));
                    break;
                case OP_TRANSFER_TO:
                    newLedger.add(new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount()));
                    break;
                default:
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid operation type").asRuntimeException());
                    return;
            }
        }
        state.setLedger(newLedger);
        responseObserver.onNext(PropagateStateResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
