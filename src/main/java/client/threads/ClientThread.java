package client.threads;

import protocol.Protocol;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ClientThread extends Thread {
    private String[] args = null;

    private final Path homeDirectory;

    public ClientThread(String[] args, Path homeDirectory) {
        this.args = args;
        this.homeDirectory = homeDirectory;
    }

    @Override
    public void run() {
        super.run();

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

        try {
            while ((fromServer = serverConnection.in.readLine()) != null) {
                System.out.println("Server: " + fromServer);

                // Als de server het signaal geeft dat het klaar is met praten
                if (fromServer.contains(Constants.END_OF_TEXT.toString())) {
                    System.out.print("Command: ");
                    fromUser = stdIn.readLine();

                    if (fromUser != null) {
                        // process input
                        protocol.processInput(Invoker.CLIENT, fromUser, serverConnection.socket, serverConnection.in, serverConnection.out);
                    }
                }

                if (fromServer.contains(Constants.END_OF_TRANSMISSION.toString())) {
                    // Close the connection.
                    serverConnection.close();
                    break;
                }

            }

            interrupt();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }
}
