package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import java.util.HashMap;
import java.util.Map;

public class UserService implements AutoCloseable {

    private String service;
    private NamingServerService namingServerService;
    private Map<String, ManagedChannel> serverChannels;
    private Map<String, UserServiceGrpc.UserServiceBlockingStub> serverStubs;

    public UserService(String service, String ns_host, int ns_port) {
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
        String sv[] = response.getHosts(0).split(":");
        String host = sv[0];
        int port = Integer.parseInt(sv[1]);
        this.serverChannels.put(server, ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
        this.serverStubs.put(server, UserServiceGrpc.newBlockingStub(serverChannels.get(server)));
    }

    private void invalidateAndCacheStub(String server) {
        this.serverChannels.remove(server);
        this.serverStubs.remove(server);
        cacheStub(server);
    }

    public void createAccount(String server, String username) {
        try {
            cacheStub(server);
            this.serverStubs.get(server).createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            this.serverStubs.get(server).createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
        }
    }

    public void deleteAccount(String server, String username) {
        try {
            cacheStub(server);
            this.serverStubs.get(server).deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            this.serverStubs.get(server).deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
        }
    }

    public BalanceResponse balance(String server, String username) {
        try {
            cacheStub(server);
            return this.serverStubs.get(server).balance(BalanceRequest.newBuilder().setUserId(username).build());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            return this.serverStubs.get(server).balance(BalanceRequest.newBuilder().setUserId(username).build());
        }
    }

    public void transferTo(String server, String from, String dest, int amount) {
        try {
            cacheStub(server);
            this.serverStubs.get(server).transferTo(
                    TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
        } catch (Exception e) {
            invalidateAndCacheStub(server);
            this.serverStubs.get(server).transferTo(
                    TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
        }
    }

    @Override
    public void close() {
        this.serverChannels.forEach((k, v) -> v.shutdownNow());
        this.namingServerService.close();
    }
}
