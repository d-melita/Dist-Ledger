package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.tecnico.distledger.server.Deserializer;
import pt.tecnico.distledger.utils.Logger;

import java.util.List;

import io.grpc.stub.StreamObserver;

public class CrossServerDistLedgerServiceImpl
        extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private final ServerState state;
    private static final String SECONDARY_SERVER_NOT_ACTIVE = "Secondary server is not active";
    private static final String FAILED = "Failed to propagate state";

    public CrossServerDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        Logger.log("Received propagate state request");
        // check if server is active
        if (!state.isActive()) {
            responseObserver
                    .onError(Status.UNAVAILABLE.withDescription(SECONDARY_SERVER_NOT_ACTIVE).asRuntimeException());
            return;
        }
        // receive ledger state
        try {
            // set ledger state on server
            state.propagateState(deserializeRequestOperationList(request));
            // return response
            responseObserver.onNext(PropagateStateResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(FAILED).asRuntimeException());
        }
    }

    private List<Operation> deserializeRequestOperationList(PropagateStateRequest request) {
        Deserializer deserializer = new Deserializer();
        return deserializer.deserialize(request.getState().getLedgerList());
    }
}
