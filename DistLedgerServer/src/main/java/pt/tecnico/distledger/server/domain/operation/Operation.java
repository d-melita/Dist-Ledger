package pt.tecnico.distledger.server.domain.operation;

public abstract class Operation {

    public enum OperationType {
        CREATE_ACCOUNT,
        DELETE_ACCOUNT,
        TRANSFER_TO
    }

    private String account;

    public Operation(String fromAccount) {
        this.account = fromAccount;
    }

    public String getAccount() {
        return account;
    }

    public abstract OperationType getType();

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "account='" + account + '\'' +
                '}';
    }
}
