package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService implements AutoCloseable {

    private final UserServiceGrpc.UserServiceBlockingStub stub;
    private final ManagedChannel channel;

    public UserService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(this.channel);
    }

    public void createAccount(String server, String username) {
        stub.createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
    }

    public void deleteAccount(String server, String username) {
        stub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
    }

    public BalanceResponse balance(String server, String username) {
        return stub.balance(BalanceRequest.newBuilder().setUserId(username).build());
    }

    public void transferTo(String server, String from, String dest, int amount) {
        stub.transferTo(
                TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
    }

    @Override
    public void close() {
        this.channel.shutdownNow();
    }
}
