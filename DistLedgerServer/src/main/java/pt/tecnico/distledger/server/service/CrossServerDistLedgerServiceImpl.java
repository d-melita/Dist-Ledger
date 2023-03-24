package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import pt.tecnico.distledger.utils.Logger;

import io.grpc.stub.StreamObserver;

public class CrossServerDistLedgerServiceImpl
        extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private final ServerState state;
    private static final String SECONDARY_SERVER_NOT_ACTIVE = "Secondary server is not active";
    private static final String INVALID_OPERATION_TYPE = "Invalid operation type";
    private static final String FAILED = "Failed to propagate state";

    public CrossServerDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        Logger.log("Received propagate state request");
        if (!state.isActive()) {
            responseObserver.onError(Status.UNAVAILABLE.withDescription(SECONDARY_SERVER_NOT_ACTIVE).asRuntimeException());
            return;
        }
        try {
            Operation operation;
            for (DistLedgerCommonDefinitions.Operation op : request.getState().getLedgerList()) {
                switch (op.getType()) {
                    case OP_CREATE_ACCOUNT:
                        operation = new CreateOp(op.getUserId());
                        state.addAccount(op.getUserId());
                        break;
                    case OP_DELETE_ACCOUNT:
                        operation = new DeleteOp(op.getUserId());
                        state.removeAccount(op.getUserId());
                        break;
                    case OP_TRANSFER_TO:
                        operation = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount());
                        state.updateAccount(op.getUserId(), -op.getAmount());
                        state.updateAccount(op.getDestUserId(), op.getAmount());
                        break;
                    default:
                        responseObserver.onError(
                                Status.INVALID_ARGUMENT.withDescription(INVALID_OPERATION_TYPE).asRuntimeException());
                        return;
                }
                state.addOperation(operation);
            }
            responseObserver.onNext(PropagateStateResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(FAILED).asRuntimeException());
        }
    }
}
