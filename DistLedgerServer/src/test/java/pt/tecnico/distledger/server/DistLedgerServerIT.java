package pt.tecnico.distledger.server;

import io.grpc.*;
import org.junit.jupiter.api.*;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

import pt.tecnico.distledger.server.service.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DistLedgerServerIT {

    final static int PORT = 50051;

    Server server;
    ManagedChannel channel;
    AdminServiceGrpc.AdminServiceBlockingStub adminStub;
    UserServiceGrpc.UserServiceBlockingStub userStub;
    String userId1 = "user1";

    @BeforeEach
    public void setUp() throws IOException{
        ServerState state = new ServerState();
        BindableService adminService = new adminDistLedgerServiceImpl(state);
        BindableService userService = new userDistLedgerServiceImpl(state);
        this.server = ServerBuilder.forPort(PORT)
                .addService(adminService)
                .addService(userService)
                .build();
        this.server.start();
        System.out.println("Server started, listening on " + PORT);
        this.channel = ManagedChannelBuilder.forAddress("localhost", PORT)
                .usePlaintext()
                .build();
        this.adminStub = AdminServiceGrpc.newBlockingStub(channel);
        this.userStub = UserServiceGrpc.newBlockingStub(channel);
        userStub.createAccount(CreateAccountRequest.newBuilder().setUserId(userId1).build());
    }

    @AfterEach
    public void tearDown() {
        this.channel.shutdownNow();
        this.server.shutdownNow();
    }

    @Test
    public void testCreateUser() {
        assertEquals(0, userStub.balance(BalanceRequest.newBuilder().setUserId(userId1).build()).getAmount());
    }

    @Test
    public void testTransferToUserFromBroker() {
        userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom("broker").setAccountTo(userId1).setAmount(100).build());
        assertEquals(100, userStub.balance(BalanceRequest.newBuilder().setUserId(userId1).build()).getAmount());
    }

    @Test
    public void testTransferToUserFromUser() {
        String userId2 = "user2";
        userStub.createAccount(CreateAccountRequest.newBuilder().setUserId(userId2).build());
        userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom("broker").setAccountTo(userId1).setAmount(100).build());
        userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom(userId1).setAccountTo(userId2).setAmount(40).build());
        assertEquals(40, userStub.balance(BalanceRequest.newBuilder().setUserId(userId2).build()).getAmount());
        assertEquals(60, userStub.balance(BalanceRequest.newBuilder().setUserId(userId1).build()).getAmount());
    }

    @Test
    public void deleteAccount() {
        userStub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(userId1).build());
        assertThrows(StatusRuntimeException.class, () -> userStub.balance(BalanceRequest.newBuilder().setUserId(userId1).build()));
    }

    @Test
    public void deleteAccountWithBalance() {
        userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom("broker").setAccountTo(userId1).setAmount(100).build());
        assertThrows(StatusRuntimeException.class, () -> userStub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(userId1).build()));
    }

    @Test
    public void createDuplicateAccount() {
        assertThrows(StatusRuntimeException.class, () -> userStub.createAccount(CreateAccountRequest.newBuilder().setUserId(userId1).build()));
    }

    @Test
    public void transferToInsufficientFunds() {
        String userId2 = "user2";
        userStub.createAccount(CreateAccountRequest.newBuilder().setUserId(userId2).build());
        userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom("broker").setAccountTo(userId1).setAmount(10).build());
        assertThrows(StatusRuntimeException.class, () -> userStub.transferTo(TransferToRequest.newBuilder().setAccountFrom(userId1).setAccountTo(userId2).setAmount(40).build()));
    }
}
