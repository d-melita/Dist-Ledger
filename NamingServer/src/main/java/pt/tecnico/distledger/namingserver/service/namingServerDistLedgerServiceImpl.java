package pt.tecnico.distledger.namingserver.service;

import pt.tecnico.distledger.namingserver.exceptions.*;
import pt.tecnico.distledger.namingserver.domain.NamingServer;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class namingServerDistLedgerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {
    private final String DEFAULT_ERROR_MESSAGE = "Operation Failed";
    private NamingServer namingServer;

    public namingServerDistLedgerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }

    @Override
    public void registerServer(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            namingServer.register(request.getService(), request.getHost(), request.getQualifier());
            responseObserver.onNext(RegisterResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (RegistryFailedException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        try {
            List<String> hosts = namingServer.lookup(request.getService(), request.getQualifier());
            LookupResponse.Builder response = LookupResponse.newBuilder();
            for (String host : hosts) {
                response.addHosts(host);
            }
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }

    @Override
    public void deleteServer(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            namingServer.delete(request.getService(), request.getHost());
            responseObserver.onNext(DeleteResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (RemovalFailedException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(DEFAULT_ERROR_MESSAGE).asRuntimeException());
        }
    }
}