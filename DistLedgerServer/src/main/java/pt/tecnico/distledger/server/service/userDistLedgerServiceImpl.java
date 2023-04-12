package pt.tecnico.distledger.server.service;

import java.util.List;

import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exceptions.*;

public class userDistLedgerServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final ServerState state;
    private static final String DEFAULT_ERROR_MESSAGE = "Operation Failed";
    private static final String INVALID_ARGUMENT_MESSAGE = "Invalid arguments";

    public userDistLedgerServiceImpl(ServerState state) {
        this.state = state;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        // check for invalid arguments
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        // try to create account
        try {
            state.createAccount(request.getUserId(), request.getPrevTSList());
            List<Integer> replicaTS = state.getReplicaTS();
            CreateAccountResponse response = CreateAccountResponse.newBuilder().addAllTS(replicaTS).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountAlreadyExistsException e) {
            // if account already exists, return ALREADY_EXISTS error
            responseObserver
                    .onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            // if server is unavailable, return UNAVAILABLE error
            responseObserver
                    .onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (SecondaryServerWriteOperationException e) {
            // try to write in a secondary server, return PERMISSION_DENIED error
            // TODO: remove for gossip
            responseObserver
                    .onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            // we tried to propagate the state to the other servers, but failed
            // TODO: remove for gossip
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        // check for invalid arguments
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        // try to delete account
        try {
            // Not supported in this version
            // state.deleteAccount(request.getUserId(), request.getPrevTSList());
            // return response
            DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            // if account doesn't exist, return NOT_FOUND error
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (DeleteBrokerAccountException | SecondaryServerWriteOperationException e) {
            // if we try to delete a broker account or write in a secondary server, return
            // PERMISSION_DENIED error
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            // if server is unavailable, return UNAVAILABLE error
            responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (AccountHasBalanceException e) {
            // if account has balance, return FAILED_PRECONDITION error
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            // we tried to propagate the state to the other servers, but failed
            // TODO: remove for gossip
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        // check for invalid arguments
        if (request.getUserId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        // try to get balance
        try {
            int balance = state.getAccountBalance(request.getUserId(), request.getPrevTSList());
            List<Integer> replicaTS = state.getReplicaTS();
            BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).addAllValueTS(replicaTS).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            // if account doesn't exist, return NOT_FOUND error
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            // if server is unavailable, return UNAVAILABLE error
            responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (FailedToPropagateException | OperationNotStableException e) {
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        // check for invalid arguments
        if (request.getAccountFrom().isEmpty() || request.getAccountTo().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(
                    INVALID_ARGUMENT_MESSAGE).asRuntimeException());
            return;
        }
        // try to transfer
        try {
            state.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount(),
                    request.getPrevTSList());
            List<Integer> replicaTS = state.getReplicaTS();
            TransferToResponse response = TransferToResponse.newBuilder().addAllTS(replicaTS).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountDoesntExistException e) {
            // if account doesn't exist, return NOT_FOUND error
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (InsufficientFundsException e) {
            // if account doesn't have enough funds, return FAILED_PRECONDITION error
            responseObserver
                    .onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            // if server is unavailable, return UNAVAILABLE error
            responseObserver
                    .onError(Status.UNAVAILABLE.asRuntimeException());
        } catch (InvalidAmountException e) {
            // if amount is invalid, return INVALID_ARGUMENT error
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (SecondaryServerWriteOperationException e) {
            // try to write in a secondary server, return PERMISSION_DENIED error
            // TODO: remove for gossip
            responseObserver
                    .onError(Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (FailedToPropagateException e) {
            // we tried to propagate the state to the other servers, but failed
            // TODO: remove for gossip
            responseObserver
                    .onError(Status.ABORTED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver
                    .onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }
}