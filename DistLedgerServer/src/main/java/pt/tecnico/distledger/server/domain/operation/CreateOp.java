package pt.tecnico.distledger.server.domain.operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

public class CreateOp extends Operation {

    public CreateOp(String account, OperationType type) {
        super(account, type);
    }

}
