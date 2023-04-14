package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

import pt.tecnico.distledger.server.grpc.CrossServerService;
import pt.tecnico.distledger.utils.Logger;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.service.*;
import pt.tecnico.distledger.server.grpc.NamingServerService;

public class ServerMain {
    private static final String LOCALHOST = "localhost";
    private static final String SERVICE = "DistLedger";
    private static final int NS_PORT = 5001;
    private static final NamingServerService namingServerService = new NamingServerService(LOCALHOST, NS_PORT);
    private static CrossServerService crossServerService;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println(ServerMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<port> <qualifier>");
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];
        String host_address = LOCALHOST + ":" + port;
        crossServerService = new CrossServerService(namingServerService, SERVICE, host_address);

        ServerState state = null;
        try {
            int server_id = namingServerService.register(SERVICE, host_address, qualifier);
            state = new ServerState(server_id);
        } catch (Exception e) {
            System.out.println("Naming server not available");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        final BindableService userImpl = new userDistLedgerServiceImpl(state);
        Logger.log("userImpl created");
        final BindableService adminImpl = new adminDistLedgerServiceImpl(state, crossServerService);
        Logger.log("adminImpl created");
        final BindableService crossServerImpl = new CrossServerDistLedgerServiceImpl(state);
        Logger.log("crossServerImpl created");

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(adminImpl)
                .addService(userImpl)
                .addService(crossServerImpl)
                .build();
        Logger.log("Server created");

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nServer shut down");
            namingServerService.unregister(SERVICE, host_address);
            namingServerService.shutdown();
            crossServerService.shutdownAll();
        }));

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}
