package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.Convertor;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;

public class adminDistLedgerServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    private final ServerState state;
    private static final String ACTIVATION_FAILED = "Server activation failed";
    private static final String DEACTIVATION_FAILED = "Server deactivation failed";
    private static final String LEDGER_FAILED = "Getting ledger failed";

    public adminDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        try {
            state.activate();
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
            DeactivateResponse response = DeactivateResponse.newBuilder().build();
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
            LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(convertOperations(state.getLedger()))
                    .build();
            getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(LEDGER_FAILED).asRuntimeException());
        }
    }

    private List<DistLedgerCommonDefinitions.Operation> convertOperations(List<Operation> operations) {
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();
        for (Operation op : operations) {
            ops.add(Convertor.convert(op));
        }
        return ops;
    }
}