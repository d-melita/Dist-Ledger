package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;

import java.util.HashMap;
import java.util.Map;

public class AdminService implements AutoCloseable {

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;
    private final ManagedChannel namingServerChannel;
    private final String service = "DistLedger";
    private Map<String, ManagedChannel> serverChannels;
    private Map<String, AdminServiceGrpc.AdminServiceBlockingStub> serverStubs;

    public AdminService(String host, int port) {
        this.namingServerChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(this.namingServerChannel);
        this.serverChannels = new HashMap<>();
        this.serverStubs = new HashMap<>();
    }

    private void cacheStub(String server){
        if (this.serverStubs.get(server) != null && this.serverChannels.get(server) != null)
            return;
        LookupResponse response = namingServerStub.lookup(LookupRequest.newBuilder().setService(service).build());
        if (response.getHostsCount() == 0)
            throw new RuntimeException("Server not found");
        String sv[] = response.getHosts(0).split(":");
        String host = sv[0];
        int port = Integer.parseInt(sv[1]);
        this.serverChannels.put(server, ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
        this.serverStubs.put(server, AdminServiceGrpc.newBlockingStub(serverChannels.get(server)));
    }

    private void invalidateAndCacheStub(String server){
        this.serverChannels.remove(server);
        this.serverStubs.remove(server);
        cacheStub(server);
    }

    public void activate(String server) {
        try {
            cacheStub(server);
            this.serverStubs.get(server).activate(ActivateRequest.getDefaultInstance());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            this.serverStubs.get(server).activate(ActivateRequest.getDefaultInstance());
        }
    }

    public void deactivate(String server) {
        try {
            cacheStub(server);
            this.serverStubs.get(server).deactivate(DeactivateRequest.getDefaultInstance());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            this.serverStubs.get(server).deactivate(DeactivateRequest.getDefaultInstance());
        }
    }

    public getLedgerStateResponse dump(String server) {
        try {
            cacheStub(server);
            return this.serverStubs.get(server).getLedgerState(getLedgerStateRequest.getDefaultInstance());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            return this.serverStubs.get(server).getLedgerState(getLedgerStateRequest.getDefaultInstance());
        }
    }

    @Override
    public void close() {
        this.namingServerChannel.shutdownNow();
        this.serverChannels.forEach((k, v) -> v.shutdownNow());
    }
}
