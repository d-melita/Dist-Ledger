package pt.tecnico.distledger.server.grpc;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.Convertor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;

public class CrossServerService {
    
    public CrossServerService() {
    }

    public void propagateState(String host, int port, List<Operation> ledger) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);

        Convertor convertor = new Convertor();
        List<DistLedgerCommonDefinitions.Operation> ops = new ArrayList<>();

        for (Operation op : ledger) {
            ops.add(op.accept(convertor));
        }

        LedgerState ledgerState = LedgerState.newBuilder().addAllLedger(ops).build();

        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledgerState).build();
        stub.propagateState(request);
    }
}
