package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;

public class userDistLedgerServiceImpl extends UserServiceGrpc.UserServiceImplBase{
    private ServerState state;

    public userDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        if (state.accountExists(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Account already exists").asRuntimeException());
            return;
        }
        CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
        state.createAccount(request.getUserId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        if (!state.accountExists(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Account does not exist").asRuntimeException());
            return;
        }
        if (state.getAccountBalance(request.getUserId()) > 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Account has balance").asRuntimeException());
            return;
        }
        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
        state.deleteAccount(request.getUserId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        if (!state.accountExists(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Account does not exist").asRuntimeException());
            return;
        }
        BalanceResponse response = BalanceResponse.newBuilder().setAmount(state.getAccountBalance(request.getUserId())).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        if (!state.accountExists(request.getAccountFrom()) || !state.accountExists(request.getAccountTo())){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Account does not exist").asRuntimeException());
            return;
        }
        if (!state.accountHasBalance(request.getAccountFrom(), request.getAmount())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not enough funds").asRuntimeException());
            return;
        }
        TransferToResponse response = TransferToResponse.newBuilder().build();
        state.transfer(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}