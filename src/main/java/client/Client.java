package client;

import protocol.Protocol;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.handlers.ConnectionHandler;
import protocol.utils.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class Client {
    public static void main(String[] args) throws IOException {
        Tools.startScreen();

        if (args.length != 2) {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        System.out.println("File sync client started. " + Constants.VERSION);
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "client");

        ConnectionHandler serverConnection = null;

        int attempts = 0;
        boolean connected = false;

        while (!connected) {


            try {
                // Gooi de argumenten door naar connection handler, en laat die het maar verder afhandelen.
                serverConnection = new ConnectionHandler(Invoker.CLIENT, homeDirectory).establish(args);
                connected = true;

            } catch (IOException ex) {
                try {
                    if (attempts < 10) {
                        attempts++;
                        System.out.println("Attempt " + attempts + " to connect.. please wait.");
                        Thread.sleep(2000);
                    } else {
                        System.err.println("Server doesn't seem te be up and running. Please try again later.");
                        System.exit(2);
                    }
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }

        Protocol protocol = new Protocol(homeDirectory);

        // Send the sync command upon connection
//        protocol.processInput(Invoker.CLIENT, Command.SYNC.toString(), serverConnection.socket, serverConnection.in, serverConnection.out);

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser;

        while ((fromServer = serverConnection.in.readLine()) != null) {
            if (!fromServer.contains(Constants.END_OF_TEXT.toString()) && !fromServer.contains(Constants.END_OF_TRANSMISSION.toString())) {
                System.out.println("Server: " + fromServer);
            }

            // Close the connection. Check this one first in case of connection with other party throwing both the control characters.
            if (fromServer.contains(Constants.END_OF_TRANSMISSION.toString())) {
                serverConnection.close();
                break;
            }

            // Als de server het signaal geeft dat het klaar is met praten
            if (fromServer.contains(Constants.END_OF_TEXT.toString())) {
                if (fromServer.equals(Constants.END_OF_TEXT.toString())) {

                    fromUser = input(stdIn);
                    protocol.processInput(Invoker.CLIENT, fromUser, serverConnection.socket, serverConnection.in, serverConnection.out);

                } else {

                    if (fromServer.startsWith(String.valueOf(ResponseCode.FAILURE.getCode()))) {
                        protocol.processErrorHandling();
                        // Error afhandeling;
                    } else if (fromServer.startsWith(String.valueOf(ResponseCode.ERROR.getCode()))) {
                        protocol.processErrorHandling();
                        // Error afhandeling;
                    } else {
                        // We gaan er eigenlijk altijd wel van uit dat het response om een succesvolle gaat.
                        // TODO: FIX Als de server iets wilt uitvoeren als client, dan moet client het aanpakken als server.
                        if(fromServer.startsWith(Command.PUT.toString()) || fromServer.startsWith(Command.GET.toString())){ // TODO: FIX This is a cheat. Needs to be dynamic.
                            protocol.processInput(Invoker.SERVER, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);
                        }else{
                            protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);

                        }
                    }

                }
            }
        }

    }

    private static String input(BufferedReader stdIn) throws IOException {
        System.out.print("Command: ");
        String fromUser = stdIn.readLine();

        if (fromUser.isBlank()) {
            System.out.println("Input is needed. Please try again");
            fromUser = input(stdIn);
        }

        return fromUser;
    }
}
