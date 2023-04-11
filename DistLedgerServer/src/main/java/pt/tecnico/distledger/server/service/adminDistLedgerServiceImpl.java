package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.OperationConverter;
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
                    convertOperationsToProto(state.getLedger()))
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

    private List<DistLedgerCommonDefinitions.Operation> convertOperationsToProto(List<Operation> operationList) {
        OperationConverter converter = new OperationConverter();
        return converter.convertToProto(operationList);
    }
}