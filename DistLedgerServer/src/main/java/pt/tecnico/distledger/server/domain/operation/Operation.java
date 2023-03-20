package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Convertor;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public abstract class Operation {
    private String account;

    public Operation(String fromAccount) {
        this.account = fromAccount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public DistLedgerCommonDefinitions.Operation accept(Convertor convertor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Operation{" +
                "account='" + account + '\'' +
                '}';
    }
}
