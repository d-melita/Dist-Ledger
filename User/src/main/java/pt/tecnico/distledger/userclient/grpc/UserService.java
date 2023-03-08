package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService {

    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(String host, int port){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void createAccount(String server, String username) {
        stub.createAccount(UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build());
        System.out.println("OK\n");
    }

    public void deleteAccount(String server, String username) {
        stub.deleteAccount(UserDistLedger.DeleteAccountRequest.newBuilder().setUserId(username).build());
        System.out.println("OK\n");
    }

    public void balance(String server, String username) {
        UserDistLedger.BalanceResponse response = stub.balance(UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build());
        System.out.println("OK");
        if (response.getAmount() != 0)
            System.out.println(response.getAmount());
        System.out.println();
    }

    public void transferTo(String server, String from, String dest, int amount) {
        stub.transferTo(UserDistLedger.TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
        System.out.println("OK\n");
    }
}
