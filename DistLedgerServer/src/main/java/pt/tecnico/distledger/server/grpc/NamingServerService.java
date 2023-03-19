package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.SecondaryServerState;

public class NamingServerService implements AutoCloseable {
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private final ManagedChannel channel;

    public NamingServerService(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public ServerState connect(String service, String host, String qualifier) {
        ServerState state;
        if (lookup(service, qualifier).getHostsCount() == 0) {
            state = new ServerState();
        } else {
            state = new SecondaryServerState();
        }
        register(service, host, qualifier);
        return state;
    }

    public void register(String service, String host, String qualifier) {
        RegisterRequest request = RegisterRequest.newBuilder().setService(service).setHost(host)
                .setQualifier(qualifier).build();
        stub.registerServer(request);
    }

    public LookupResponse lookup(String service, String qualifier) {
        LookupRequest request = LookupRequest.newBuilder().setService(service).setQualifier(qualifier).build();
        LookupResponse response = stub.lookup(request);
        return response;
    }

    public void unregister(String service, String host) {
        DeleteRequest request = DeleteRequest.newBuilder().setService(service).setHost(host).build();
        stub.deleteServer(request);
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
