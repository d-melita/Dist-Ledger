package pt.tecnico.distledger.server.domain.operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;

public abstract class Operation {
    private String account;
    private OperationType type;

    public Operation(String fromAccount, OperationType type) {
        this.account = fromAccount;
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public abstract DistLedgerCommonDefinitions.Operation convertToProto();

}
