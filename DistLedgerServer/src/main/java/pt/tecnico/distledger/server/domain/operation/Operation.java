package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.Serializer;
import pt.tecnico.distledger.server.domain.ServerState;

import java.util.List;
import java.util.ArrayList;

public abstract class Operation {

    public enum OperationType {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        TRANSFER_TO
    }

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

    public void setTS(int index, List<Integer> ReplicaTS) {
        this.TS = new ArrayList<>();
        System.out.println("Setting TS for " + this.account + " to " + ReplicaTS.get(index));
        for (int i = 0; i < this.prevTS.size(); i++) {
            this.TS.add(this.prevTS.get(i));
        }
        this.TS.set(index, ReplicaTS.get(index));
        isStable = true;
    }

    public String getAccount() {
        return this.account;
    }

    public abstract void serialize(Serializer serializer);

    public abstract void executeOperation(ServerState state);

    @Override
    public String toString() {
        return "Operation{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
