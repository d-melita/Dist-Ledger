package pt.tecnico.distledger.server.service;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;
import pt.tecnico.distledger.server.grpc.CrossServerService;

public class userDistLedgerServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final ServerState state;
    private final CrossServerService crossServerService;
    private static final String DEFAULT_ERROR_MESSAGE = "Operation Failed";
    private static final String INVALID_ARGUMENT_MESSAGE = "Invalid arguments";

    public userDistLedgerServiceImpl(ServerState state, CrossServerService crossServerService) {
        this.state = state;
        this.crossServerService = crossServerService;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        try {
            state.createAccount(request.getUserId());
            crossServerService.propagateState(state.getLedger());
            CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountAlreadyExistsException e) {
            responseObserver
                    .onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            responseObserver
                    .onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (SecondaryServerWriteOperationException e) {
            responseObserver
                    .onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        try {
            state.deleteAccount(request.getUserId());
            crossServerService.propagateState(state.getLedger());
            DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (DeleteBrokerAccountException | SecondaryServerWriteOperationException e) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (AccountHasBalanceException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        try {
            int balance = state.getAccountBalance(request.getUserId());
            BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (FailedToPropagateException e) {
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        if (request.getAccountFrom().isEmpty() || request.getAccountTo().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        try {
            state.transfer(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
            crossServerService.propagateState(state.getLedger());
            TransferToResponse response = TransferToResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (InsufficientFundsException e) {
            responseObserver
                    .onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            responseObserver
                    .onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (InvalidAmountException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (SecondaryServerWriteOperationException e) {
            responseObserver
                    .onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }
}