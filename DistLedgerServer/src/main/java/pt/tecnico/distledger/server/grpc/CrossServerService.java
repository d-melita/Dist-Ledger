package pt.tecnico.distledger.server.grpc;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.LookupResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrossServerService implements AutoCloseable {
    private final String service;
    private final String host_adress;
    NamingServerService namingServerService;
    private final Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubs;
    private final Map<String, ManagedChannel> channels;

    public CrossServerService(NamingServerService namingServerService, String service, String host_adress) {
        stubs = new HashMap<>();
        channels = new HashMap<>();
        this.namingServerService = namingServerService;
        this.service = service;
        this.host_adress = host_adress;
    }

    public void propagateState(List<DistLedgerCommonDefinitions.Operation> operationList) {
        // send response
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(operationList).build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();
        for (String host : searchForServers()) {
            if (host.equals(host_adress)) {
                continue;
            }
            if (stubs.containsKey(host)) {
                stubs.get(host).propagateState(request);
            } else {
                channels.put(host, ManagedChannelBuilder.forTarget(host).usePlaintext().build());
                stubs.put(host, DistLedgerCrossServerServiceGrpc.newBlockingStub(channels.get(host)));
                stubs.get(host).propagateState(request);
            }
        }
    }

    private List<String> searchForServers() {
        List<String> servers = new ArrayList<>();
        LookupResponse hosts = namingServerService.lookup(service);
        for (String host : hosts.getHostsList()) {
            servers.add(host);
        }
        return servers;
    }

    @Override
    public void close() {
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
        namingServerService.close();
    }
}
