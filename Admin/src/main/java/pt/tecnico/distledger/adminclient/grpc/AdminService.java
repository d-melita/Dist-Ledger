package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminService {

    private final AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void activate() {
        try {
            stub.activate(ActivateRequest.getDefaultInstance());
            System.out.println("OK\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void deactivate() {
        try {
            stub.deactivate(DeactivateRequest.getDefaultInstance());
            System.out.println("OK\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void dump() {
        try {
            getLedgerStateResponse response = stub.getLedgerState(getLedgerStateRequest.getDefaultInstance());
            System.out.println("OK");
            System.out.println(response.toString() + "\n");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
