package pt.tecnico.distledger.server.service;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.domain.operation.Operation;

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
        ActivateResponse response = ActivateResponse.newBuilder().build();
        state.activate();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        DeactivateResponse response = DeactivateResponse.newBuilder().build();
        state.deactivate();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        // get operations from ledger
        List<Operation> operations = state.getLedger();
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();

        for (Operation op : operations) {
            if (op.getType() == OperationType.OP_TRANSFER_TO){
                DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                    .setType(op.getType())
                    .setUserId(op.getAccount())
                    .setDestUserId(((TransferOp) op).getDestAccount())
                    .setAmount(((TransferOp) op).getAmount())
                    .build();
                ops.add(operation);
            }
            else if (op.getType() == OperationType.OP_CREATE_ACCOUNT || op.getType() == OperationType.OP_DELETE_ACCOUNT){
                DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(op.getType())
                .setUserId(op.getAccount())
                .build();
                ops.add(operation);
            }
            
        }

        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build(); 
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}