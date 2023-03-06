package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;

public class AdminService {

    private final int port;
    private final String host;

    public AdminService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public ManagedChannel newChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    public AdminServiceGrpc.AdminServiceBlockingStub newBlockingStub(ManagedChannel channel) {
        return AdminServiceGrpc.newBlockingStub(channel);
    }
    
    public void activate() {
        ManagedChannel channel = newChannel(this.host, this.port);
        ActivateRequest request = ActivateRequest.getDefaultInstance();
        try {
            AdminServiceGrpc.AdminServiceBlockingStub stub = newBlockingStub(channel);
            ActivateResponse response = stub.activate(request);
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void deactivate() {
        ManagedChannel channel = newChannel(this.host, this.port);
        DeactivateRequest request = DeactivateRequest.getDefaultInstance();
        try {
            AdminServiceGrpc.AdminServiceBlockingStub stub = newBlockingStub(channel);
            DeactivateResponse response = stub.deactivate(request);
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void dump() {
        ManagedChannel channel = newChannel(this.host, this.port);
        getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();

        try {
            AdminServiceGrpc.AdminServiceBlockingStub stub = newBlockingStub(channel);
            getLedgerStateResponse response = stub.getLedgerState(request);
            System.out.println("OK");
            System.out.println(response.getLedgerState().toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
