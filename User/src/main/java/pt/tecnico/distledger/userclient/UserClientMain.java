package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.tecnico.distledger.utils.Logger;

public class UserClientMain {
    private final static String SERVICE = "DistLedger";
    private final static String NS_HOST = "localhost";
    private final static int NS_PORT = 5001;

    public static void main(String[] args) {
        System.out.println(UserClientMain.class.getSimpleName());

        try (var userService = new UserService(SERVICE, NS_HOST, NS_PORT)) {
            CommandParser parser = new CommandParser(userService);
            Logger.log("UserClientMain, Starting command parser");
            parser.parseInput();
        }
    }
}
