package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;

public class AdminService implements AutoCloseable {

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private final ManagedChannel channel;
    private final String service = "DistLedger";
    private ManagedChannel sv_channel;
    private AdminServiceGrpc.AdminServiceBlockingStub sv_stub;

    public AdminService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = NamingServerServiceGrpc.newBlockingStub(this.channel);
    }

    private void connectToServer(String server) {
        LookupResponse response = stub.lookup(LookupRequest.newBuilder().setService(service).setQualifier(server).build());
        if (response.getHostsCount() == 0)
            throw new RuntimeException("Server not found");
        String sv[] = response.getHosts(0).split(":");
        String host = sv[0];
        int port = Integer.parseInt(sv[1]);
        this.sv_channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.sv_stub = AdminServiceGrpc.newBlockingStub(sv_channel);
    }

    public void activate(String server) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return;
        }
        sv_stub.activate(ActivateRequest.getDefaultInstance());
    }

    public void deactivate(String server) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return;
        }
        sv_stub.deactivate(DeactivateRequest.getDefaultInstance());
    }

    public getLedgerStateResponse dump(String server) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return null;
        }
        return sv_stub.getLedgerState(getLedgerStateRequest.getDefaultInstance());
    }

    @Override
    public void close() {
        this.channel.shutdownNow();
        this.sv_channel.shutdownNow();
    }
}
