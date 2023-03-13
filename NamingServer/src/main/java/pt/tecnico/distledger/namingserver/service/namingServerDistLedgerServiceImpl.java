package pt.tecnico.distledger.namingserver.service;

import pt.tecnico.distledger.namingserver.exceptions.*;
import io.grpc.Status;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.domain.NamingServer;

public class namingServerDistLedgerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {
    private final String DEFAULT_ERROR_MESSAGE = "Operation Failed";
    private NamingServer namingServer;

    public namingServerDistLedgerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }

    @Override
    public void registerServer(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        namingServer.register(request.getService(), request.getHost(), request.getQualifier());
    }
}