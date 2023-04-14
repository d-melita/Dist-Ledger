package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;

public class NamingServerService implements AutoCloseable {
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private final ManagedChannel channel;

    public NamingServerService(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public LookupResponse lookup(String service, String qualifier) {
        LookupRequest request = LookupRequest.newBuilder().setService(service).setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        return response;
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
