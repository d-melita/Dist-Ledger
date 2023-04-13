package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.grpc.CrossServerService;
import pt.tecnico.distledger.server.Serializer;
import pt.tecnico.distledger.server.domain.ServerState;

import java.util.List;

import io.grpc.stub.StreamObserver;

public class adminDistLedgerServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    private final ServerState state;
    private CrossServerService crossServerService;
    private static final String ACTIVATION_FAILED = "Server activation failed";
    private static final String DEACTIVATION_FAILED = "Server deactivation failed";
    private static final String LEDGER_FAILED = "Getting ledger failed";
    private static final String GOSSIP_FAILED = "Gossip failed";

    public adminDistLedgerServiceImpl(ServerState state, CrossServerService crossServerService) {
        this.state = state;
        this.crossServerService = crossServerService;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        try {
            // activate server
            state.activate();
            // return response
            ActivateResponse response = ActivateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(ACTIVATION_FAILED).asRuntimeException());
        }
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        try {
            // deactivate server
            DeactivateResponse response = DeactivateResponse.newBuilder().build();
            // return response
            state.deactivate();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEACTIVATION_FAILED).asRuntimeException());
        }
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        // get operations from ledger
        try {
            // get ledger state
            LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(
                    serializeOperations(state.getLedger()))
                    .build();
            // return response
            getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(LEDGER_FAILED).asRuntimeException());
        }
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        try {
            crossServerService.propagateState(serializeOperations(state.getLedger()));
            GossipResponse response = GossipResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(GOSSIP_FAILED).asRuntimeException());
        }
    }

    private List<DistLedgerCommonDefinitions.Operation> serializeOperations(List<Operation> operationList) {
        Serializer serializer = new Serializer();
        return serializer.serializeOperations(operationList);
    }
}