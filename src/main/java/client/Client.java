package client;

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

        while ((fromServer = serverConnection.in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
            System.out.println("We're in the first loop!");

//            // Response codes gebruiken als afgesproken in protocol.
//            if(fromServer.startsWith("2")){
//                protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);
//            }
//
//            if(fromServer.startsWith("5")){
//                protocol.processErrorHandling();
//                // Error afhandeling;
//            }

            // Als de server het signaal geeft dat het klaar is met praten
            if (fromServer.contains(Constants.END_OF_TEXT.toString())) {
                System.out.print("Command: ");
                fromUser = stdIn.readLine();

                if (fromUser != null) {
                    // TODO: FIX Dit moet worden gedaan in protocol.processInput.
                    //  En als de server dan eerst met een command komt, dan moet daar ook nog op gecheckt worden.
//                    serverConnection.out.println(fromUser);

                    // Not so sure about this placement.
                    protocol.processInput(Invoker.CLIENT, fromUser, serverConnection.socket, serverConnection.in, serverConnection.out);
                }
            } else {
                protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.socket, serverConnection.in, serverConnection.out);
            }

            // Close the connection.
            if (fromServer.contains(Constants.END_OF_TRANSMISSION.toString())) {
                serverConnection.close();
                break;
            }

        }

    }
}
