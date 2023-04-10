package pt.tecnico.distledger.server.grpc;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.Convertor;

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
    private static final String SERVICE = "DistLedger";
    private static final String SECONDARY_QUALIFIER = "B";
    NamingServerService namingServerService;
    private final Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubs;
    private final Map<String, ManagedChannel> channels;

    public CrossServerService(NamingServerService namingServerService) {
        stubs = new HashMap<>();
        channels = new HashMap<>();
        this.namingServerService = namingServerService;
    }

    public void propagateState(List<Operation> operationList) {
        List<DistLedgerCommonDefinitions.Operation> operations = new ArrayList<>();
        for (Operation operation : operationList) {
            operations.add(Convertor.convert(operation));
        }
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(operations).build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();
        LookupResponse hosts = namingServerService.lookup(SERVICE, SECONDARY_QUALIFIER);
        for (String host : hosts.getHostsList()) {
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
    }
}
