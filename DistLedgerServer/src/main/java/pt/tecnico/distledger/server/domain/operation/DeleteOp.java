package pt.tecnico.distledger.server.domain.operation;

public class DeleteOp extends Operation {

    public DeleteOp(String account) {
        super(account);
    }

    @Override
    public OperationType getType() {
        return OperationType.DELETE_ACCOUNT;
    }

    @Override
    public String toString() {
        return "DeleteOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
