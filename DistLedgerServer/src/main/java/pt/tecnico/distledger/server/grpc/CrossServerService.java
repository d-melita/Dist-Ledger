package pt.tecnico.distledger.server.grpc;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.Convertor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrossServerService {

    private final Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubs;
    private final Map<String, ManagedChannel> channels;
    private final Convertor convertor;

    public CrossServerService() {
        convertor = new Convertor();
        stubs = new HashMap<>();
        channels = new HashMap<>();
    }

    public void propagateState(Operation op, List<String> hosts, List<Integer> replicaTS) {
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();
        ops.add(op.accept(convertor));
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).addAllReplicaTS(replicaTS).build();

        for (String host : hosts) {
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
