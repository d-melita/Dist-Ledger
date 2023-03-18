package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService implements AutoCloseable {

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private final ManagedChannel channel;
    private final String service = "DistLedger";
    private ManagedChannel sv_channel;
    private UserServiceGrpc.UserServiceBlockingStub sv_stub;

    public UserService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = NamingServerServiceGrpc.newBlockingStub(this.channel);
    }

    private void connectToServer(String server) {
        LookupResponse response = stub.lookup(LookupRequest.newBuilder().setService(service).setQualifier(server).build());
        if (response.getHostsCount() == 0)
            throw new RuntimeException("Server not found");
        String sv[] = response.getHosts(0).split(":");
        String host = sv[0];
        int port = Integer.parseInt(sv[1]);
        this.sv_channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.sv_stub = UserServiceGrpc.newBlockingStub(sv_channel);
    }

    public void createAccount(String server, String username) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return;
        }
        sv_stub.createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
    }

    public void deleteAccount(String server, String username) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return;
        }
        sv_stub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
    }

    public BalanceResponse balance(String server, String username) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return null;
        }
        return sv_stub.balance(BalanceRequest.newBuilder().setUserId(username).build());
    }

    public void transferTo(String server, String from, String dest, int amount) {
        try {
            connectToServer(server);
        } catch (RuntimeException e) {
            System.out.println("Server not found");
            return;
        }
        sv_stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
    }

    @Override
    public void close() {
        this.channel.shutdownNow();
        this.sv_channel.shutdownNow();
    }
}
