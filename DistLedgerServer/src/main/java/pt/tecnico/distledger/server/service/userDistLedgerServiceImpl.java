package pt.tecnico.distledger.server.service;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;

public class userDistLedgerServiceImpl extends UserServiceGrpc.UserServiceImplBase{
    private ServerState serverState = new ServerState();
}
