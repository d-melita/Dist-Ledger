package pt.tecnico.distledger.namingserver;

import java.io.IOException;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.utils.Logger;
import pt.tecnico.distledger.namingserver.domain.NamingServer;
import pt.tecnico.distledger.namingserver.service.namingServerDistLedgerServiceImpl;

public class NamingServerMain {

    public static void main(String[] args) throws IOException, InterruptedException{

        System.out.println(NamingServerMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<port>");
            return;
        }

        final int port = Integer.parseInt(args[0]);

        NamingServer namingServer = new NamingServer();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(new namingServerDistLedgerServiceImpl(namingServer))
                .build();
        Logger.log("Server created");

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();

    }
}
