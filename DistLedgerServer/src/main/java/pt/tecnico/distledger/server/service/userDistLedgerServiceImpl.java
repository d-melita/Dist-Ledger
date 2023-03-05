package pt.tecnico.distledger.server.service;

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
        CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
        state.createAccount(request.getUserId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
        state.deleteAccount(request.getUserId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        BalanceResponse response = BalanceResponse.newBuilder().setValue(state.getAccountBalance(request.getUserId())).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        TransferToResponse response = TransferToResponse.newBuilder().build();
        state.transfer(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}