package client;

import client.handlers.Connection;
import client.handlers.FileWatcher;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Connection connection = null;

        int attempts = 0;

        boolean connected = false;

        while (!connected) {
            try {
                connection = new Connection(hostName, portNumber).establish();
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

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser;

        // Start fileWatcher in separate thread
        Thread fileWatcher = new Thread(new FileWatcher());
        fileWatcher.start();

        // Communication
        while ((fromServer = connection.serverIn.readLine()) != null) {
            System.out.println("Server: " + fromServer);

            // Server close
            if (fromServer.contains("\u0004")) {
                connection.close();
                break;
            }

            // Client communication with the server
            if (fromServer.contains("\u0003")) {

                System.out.print("Command: ");
                fromUser = stdIn.readLine(); // Blockade

                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);

                    // Send client input to the server
                    connection.serverOut.println(fromUser);
                }
            }
        }

    }
}
