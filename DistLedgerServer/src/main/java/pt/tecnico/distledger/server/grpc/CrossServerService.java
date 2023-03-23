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
import java.util.List;

public class CrossServerService {

    public CrossServerService() {
    }

    public void propagateState(List<Operation> ledger, List<String> hosts) {
        Convertor convertor = new Convertor();
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();

        for (Operation op : ledger) {
            ops.add(op.accept(convertor));
        }
        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();

        for (String host : hosts) {
            // TO DO - try catch (?)
            ManagedChannel channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build();
            DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc
                    .newBlockingStub(channel);
            stub.propagateState(request);
            channel.shutdown();
        }
    }
}
