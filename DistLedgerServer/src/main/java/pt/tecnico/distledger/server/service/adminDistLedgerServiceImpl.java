package pt.tecnico.distledger.server.service;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;

public class adminDistLedgerServiceImpl extends AdminServiceGrpc.AdminServiceImplBase{
    private ServerState serverState = new ServerState();
}