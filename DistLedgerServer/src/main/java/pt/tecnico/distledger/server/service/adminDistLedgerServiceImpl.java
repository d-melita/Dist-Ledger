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

public class adminDistLedgerServiceImpl extends AdminServiceGrpc.AdminServiceImplBase{
    ServerState state;

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
                    .onError(Status.UNKNOWN.withDescription("Server activation failed").asRuntimeException());
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
                    .onError(Status.UNKNOWN.withDescription("Server deactivation failed").asRuntimeException());
        }
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        // get operations from ledger
        try {
        Convertor convertor = new Convertor();
        List<Operation> operations = state.getLedger();
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();

        for (Operation op : operations) {
            ops.add(op.accept(convertor));
        }

        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build(); 
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription("Getting ledger failed").asRuntimeException());
        }
    }
}