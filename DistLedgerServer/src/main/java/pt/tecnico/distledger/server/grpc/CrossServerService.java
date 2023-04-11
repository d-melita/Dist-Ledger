package pt.tecnico.distledger.server.grpc;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.OperationConverter;
import pt.tecnico.distledger.server.domain.operation.Operation;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.LookupResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrossServerService {
    private final String service;
    NamingServerService namingServerService;
    private final Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubs;
    private final Map<String, ManagedChannel> channels;

    public CrossServerService(NamingServerService namingServerService, String service) {
        stubs = new HashMap<>();
        channels = new HashMap<>();
        this.namingServerService = namingServerService;
        this.service = service;
    }

    public void propagateState(List<Operation> operationList) {
        // send response
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(convertOperationsToProto(operationList))
                .build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();
        // TODO: we are propagating to ourselves, we should not
        for (String host : searchForServers()) {
            if (stubs.containsKey(host)) {
                stubs.get(host).propagateState(request);
            } else {
                channels.put(host, ManagedChannelBuilder.forTarget(host).usePlaintext().build());
                stubs.put(host, DistLedgerCrossServerServiceGrpc.newBlockingStub(channels.get(host)));
                stubs.get(host).propagateState(request);
            }
        }
    }

    public void shutdownAll() {
        for (ManagedChannel channel : channels.values()) {
            channel.shutdown();
        }
        namingServerService.shutdown();
    }

    private List<String> searchForServers() {
        List<String> servers = new ArrayList<>();
        LookupResponse hosts = namingServerService.lookup(service);
        for (String host : hosts.getHostsList()) {
            servers.add(host);
        }
        return servers;
    }

    private List<DistLedgerCommonDefinitions.Operation> convertOperationsToProto(List<Operation> operationList) {
        OperationConverter converter = new OperationConverter();
        return converter.convertToProto(operationList);
    }
}
