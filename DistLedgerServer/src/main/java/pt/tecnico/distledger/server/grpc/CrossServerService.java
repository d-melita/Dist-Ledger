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
import java.util.List;

public class CrossServerService implements AutoCloseable {

    private static final String SECONDARY_SERVER_QUALIFIER = "B";
    private String service;
    private NamingServerService namingServerService;

    public CrossServerService(String service, String ns_host, int ns_port) {
        this.service = service;
        this.namingServerService = new NamingServerService(ns_host, ns_port);
    }

    public void propagateState(List<Operation> ledger) {
        Convertor convertor = new Convertor();
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();

        for (Operation op : ledger) {
            ops.add(op.accept(convertor));
        }
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();
        LookupResponse response = namingServerService.lookup(service, SECONDARY_SERVER_QUALIFIER);

        if (response.getHostsCount() == 0) {
            // TO DO - create and throw exception
            throw new RuntimeException("No secondary server found");
        }

        for (String hostString : response.getHostsList()) {
            String host = hostString.split(":")[0];
            int port = Integer.parseInt(hostString.split(":")[1]);
            // TO DO - try catch (?)
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
            DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc
                    .newBlockingStub(channel);
            stub.propagateState(request);
        }
    }

    public void close() {
        namingServerService.close();
    }
}
