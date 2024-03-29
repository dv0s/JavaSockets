package client;

import client.handlers.FileWatcherHandler;
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

        System.out.println("File sync client started. " + Constants.Strings.VERSION);
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.Strings.BASE_DIR + File.separator + "client");

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
        protocol.processInput(Invoker.CLIENT, Command.SYNC.toString(), serverConnection.socket, serverConnection.in, serverConnection.out);

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser;

        while ((fromServer = serverConnection.in.readLine()) != null) {
            if (!fromServer.contains(Constants.Strings.END_OF_TEXT.toString()) && !fromServer.contains(Constants.Strings.END_OF_TRANSMISSION.toString())) {
                System.out.println("Server: " + fromServer);
            }

            // Close the connection. Check this one first in case of connection with other party throwing both the control characters.
            if (fromServer.contains(Constants.Strings.END_OF_TRANSMISSION.toString())) {
                serverConnection.close();
                break;
            }

            // Als de server het signaal geeft dat het klaar is met praten
            if (fromServer.contains(Constants.Strings.END_OF_TEXT.toString())) {
                if (fromServer.equals(Constants.Strings.END_OF_TEXT.toString()) || fromServer.equals("COMMAND UNKNOWN" + Constants.Strings.END_OF_TEXT)) {

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

                        // Als de server een commando geeft, moeten wij als client dit oppakken als server.
                        if (fromServer.startsWith(Command.PUT.toString()) || fromServer.startsWith(Command.GET.toString())) {
                            protocol.processInput(Invoker.SERVER, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);
                        } else {
                            // Anders voeren we het uit als client.
                            protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);

                        }
                    }

                }
            }
        }

        FileWatcherHandler fileWatcherHandler = new FileWatcherHandler(args);
        fileWatcherHandler.run();
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
