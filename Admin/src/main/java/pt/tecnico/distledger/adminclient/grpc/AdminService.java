package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminService {

    private final int port;
    private final String host;
    private ManagedChannel channel;
    private AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String host, int port) {
        this.host = host;
        this.port = port;
        setChannelandStub(host, port);
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public void setChannelandStub(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void activate() {
        ActivateRequest request = ActivateRequest.getDefaultInstance();
        try {
            ActivateResponse response = this.stub.activate(request);
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void deactivate() {
        DeactivateRequest request = DeactivateRequest.getDefaultInstance();
        try {
            DeactivateResponse response = this.stub.deactivate(request);
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void dump() {
        getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();

        try {
            getLedgerStateResponse response = this.stub.getLedgerState(request);
            System.out.println("OK");
            System.out.println(response.getLedgerState().toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
