package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminService implements AutoCloseable {

    private final AdminServiceGrpc.AdminServiceBlockingStub stub;
    private final ManagedChannel channel;

    public AdminService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(this.channel);
    }

    public void activate() {
        stub.activate(ActivateRequest.getDefaultInstance());
    }

    public void deactivate() {
        stub.deactivate(DeactivateRequest.getDefaultInstance());
    }

    public getLedgerStateResponse dump() {
        return stub.getLedgerState(getLedgerStateRequest.getDefaultInstance());
    }

    @Override
    public void close() {
        this.channel.shutdownNow();
    }
}
