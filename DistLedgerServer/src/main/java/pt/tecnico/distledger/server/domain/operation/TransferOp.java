package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.OperationConverter;
import pt.tecnico.distledger.server.domain.ServerState;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount) {
        super(fromAccount);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void convert(OperationConverter converter) {
        converter.convert(this);
    }

    @Override
    public void executeOperation(ServerState state) {
        // TODO: to avoid exposing server to Operation, we could create an Executor
        // class for this
        state.executeOperation(this);
    }

    @Override
    public String toString() {
        return "TransferOp{" +
                "account='" + getAccount() + '\'' +
                ", destAccount='" + getDestAccount() + '\'' +
                ", amount=" + getAmount() +
                '}';
    }
}
