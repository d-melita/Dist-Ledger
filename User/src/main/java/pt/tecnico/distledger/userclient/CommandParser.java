package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import pt.tecnico.distledger.utils.Logger;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;
    private List<Integer> prevTS = new ArrayList<>();

    public CommandParser(UserService userService) {
        this.userService = userService;
        prevTS.add(0);
        prevTS.add(0);
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case DELETE_ACCOUNT:
                        //this.deleteAccount(line);
                        System.out.println("deleteAccount not possible in phase 3\n");
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        System.out.println("Unknown command: " + cmd);
                        this.printUsage();
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
                System.out.println();
            }
        }
    }

    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        Logger.log("Creating account for user \'" + username + "\' on server " + server + "...");
        List<Integer> tempTS = userService.createAccount(server, username, prevTS).getTSList();
        updateTS(tempTS);
        System.out.println("OK\n");
        Logger.log("Account created for user \'" + username + "\'");
    }

    private void deleteAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        Logger.log("Deleting account for user \'" + username + "\'");
        userService.deleteAccount(server, username);
        System.out.println("OK\n");
        Logger.log("Account deleted for user \'" + username + "\'");
    }


    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        Logger.log("Getting balance for user \'" + username + "\' on server " + server + "...");

        BalanceResponse response = userService.balance(server, username, prevTS);
        int balance = response.getValue();
        List<Integer> tempTS = response.getValueTSList();
        updateTS(tempTS);
        System.out.println("OK");
        if (balance > 0) {
            Logger.log("Balance for user \'" + username + "\' is:");
            System.out.println(balance);
        }
        System.out.println();
    }

    private void transferTo(String line){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        Logger.log("Transferring " + amount + " from user \'" + from + "\' to user \'" + dest + "\'");
        List<Integer> tempTS = userService.transferTo(server, from, dest, amount, prevTS).getTSList();
        updateTS(tempTS);
        System.out.println("OK\n");
    }

    private void updateTS(List<Integer> tempTS){
        for(int i = 0; i < prevTS.size(); i++){
            if(tempTS.get(i) > prevTS.get(i)){
                prevTS.set(i, tempTS.get(i));
            }
        }
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- deleteAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}
