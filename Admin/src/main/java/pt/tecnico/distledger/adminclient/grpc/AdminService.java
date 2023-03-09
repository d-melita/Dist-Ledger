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
        stub.activate(ActivateRequest.getDefaultInstance());
    }

    public void deactivate() {
        stub.deactivate(DeactivateRequest.getDefaultInstance());
    }

    public getLedgerStateResponse dump() {
        return stub.getLedgerState(getLedgerStateRequest.getDefaultInstance());
    }

}
