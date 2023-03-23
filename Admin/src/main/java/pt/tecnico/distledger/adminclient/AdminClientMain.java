package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.utils.Logger;

public class AdminClientMain {
    private final static String SERVICE = "DistLedger";
    private final static String NS_HOST = "localhost";
    private final static int NS_PORT = 5001;

    public static void main(String[] args) {

        if (args.length != 0) {
            System.err.println("Usage: mvn exec:java");
            return;
        }

        try (var adminService = new AdminService(SERVICE, NS_HOST, NS_PORT)) {
            CommandParser parser = new CommandParser(adminService);
            Logger.log("AdminClientMain, Starting command parser");
            parser.parseInput();
        }
    }
}
