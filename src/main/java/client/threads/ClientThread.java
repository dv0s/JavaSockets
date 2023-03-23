package client.threads;

import protocol.Protocol;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientThread extends Thread {
    private final ConnectionHandler serverConnection;

    private final Protocol protocol;

    public ClientThread(ConnectionHandler serverConnection, Protocol protocol) {
        this.serverConnection = serverConnection;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        super.run();
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
