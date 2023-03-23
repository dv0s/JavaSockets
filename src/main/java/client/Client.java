package client;

import client.handlers.FileWatcher;
import protocol.Protocol;
import protocol.enums.Constants;
import protocol.enums.Invoker;
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

        System.out.println("File sync client started. v0.0.1");
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

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser;

        // Start fileWatcher in separate thread
        FileWatcher fileWatcher = new FileWatcher(serverConnection, protocol);
        fileWatcher.start();

        while ((fromServer = serverConnection.in.readLine()) != null) {
            System.out.println("Server: " + fromServer);

//            protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);

            // TODO: 19/03/2023 Hier moeten we nog wat mee doen i.v.m. de afgesproken response codes in het protocol.
            // Response codes gebruiken als afgesproken in protocol.
//            if(fromServer.startsWith(String.valueOf(ResponseCode.SUCCESS.getCode()))){
//                protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);
//            }
//
//            if(fromServer.startsWith(String.valueOf(ResponseCode.FAILURE.getCode()))){
//                protocol.processErrorHandling();
//                // Error afhandeling;
//            }
//
//            if(fromServer.startsWith(String.valueOf(ResponseCode.ERROR.getCode()))){
//                protocol.processErrorHandling();
//                // Error afhandeling;
//            }

            // Als de server het signaal geeft dat het klaar is met praten
            if (fromServer.contains(Constants.END_OF_TEXT.toString())) {
                System.out.print("Command: ");
                fromUser = stdIn.readLine();

                if (fromUser != null) {
                    protocol.processInput(Invoker.CLIENT, fromUser, serverConnection.socket, serverConnection.in, serverConnection.out);
                }
            }

            if (fromServer.contains(Constants.END_OF_TRANSMISSION.toString())) {
                // Close fileWatcher
                fileWatcher.interrupt();

                // Close the connection.
                serverConnection.close();
                break;
            }

        }

    }
}
