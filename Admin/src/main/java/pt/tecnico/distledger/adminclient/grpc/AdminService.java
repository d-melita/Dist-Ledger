package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;

import java.util.HashMap;
import java.util.Map;

public class AdminService implements AutoCloseable {

    private final String service;
    private final NamingServerService namingServerService;
    private final Map<String, ManagedChannel> serverChannels;
    private final Map<String, AdminServiceGrpc.AdminServiceBlockingStub> serverStubs;

    public AdminService(String service, String ns_host, int ns_port) {
        this.service = service;
        this.namingServerService = new NamingServerService(ns_host, ns_port);
        this.serverChannels = new HashMap<>();
        this.serverStubs = new HashMap<>();
    }

    private void cacheStub(String server) {
        if (this.serverStubs.get(server) != null && this.serverChannels.get(server) != null)
            return;
        LookupResponse response = this.namingServerService.lookup(this.service, server);
        if (response.getHostsCount() == 0)
            throw new RuntimeException("Server not found");
        String host = response.getHosts(0);
        this.serverChannels.put(server, ManagedChannelBuilder.forTarget(host).usePlaintext().build());
        this.serverStubs.put(server, AdminServiceGrpc.newBlockingStub(serverChannels.get(server)));
    }

    private void invalidateAndCacheStub(String server) {
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
        this.serverChannels.forEach((k, v) -> v.shutdownNow());
        this.namingServerService.close();
    }
}
