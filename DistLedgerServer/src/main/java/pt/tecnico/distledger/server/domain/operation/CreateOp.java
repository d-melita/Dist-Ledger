package pt.tecnico.distledger.server.domain.operation;

public class CreateOp extends Operation {
    public CreateOp(String account) {
        super(account);
    }

    @Override
    public OperationType getType() {
        return OperationType.CREATE_ACCOUNT;
    }

    @Override
    public String toString() {
        return "CreateOp{" +
                "account='" + getAccount() + '\'' +
                '}';
    }
}
