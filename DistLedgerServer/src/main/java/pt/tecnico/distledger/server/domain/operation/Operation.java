package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Serializer;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.List;
import java.util.ArrayList;

public abstract class Operation {
    private String account;

    private List<Integer> prevTS = new ArrayList<>();
    private List<Integer> TS = new ArrayList<>();
    private boolean isStable = false;

    public Operation(String fromAccount, List<Integer> prevTS, List<Integer> TS) {
        this.account = fromAccount;
        this.prevTS = prevTS;
        this.TS = TS;
    }

    public List<Integer> getPrevTS() {
        return this.prevTS;
    }

    public List<Integer> getTS() {
        return this.TS;
    }

    public void setTS(List<Integer> TS) {
        this.TS = TS;
        isStable = true;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public DistLedgerCommonDefinitions.Operation accept(Serializer serializer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Operation{" +
                "account='" + account + '\'' +
                '}';
    }
}
