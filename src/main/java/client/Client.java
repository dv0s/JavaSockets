package client;

import client.handlers.Connection;

import java.io.*;
import java.nio.file.*;

import static client.handlers.FileWatcher.clientDir;
import static java.nio.file.StandardWatchEventKinds.*;

public class Client {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Connection connection = null;

        // Define fileWatcher
        WatchService watchService = FileSystems.getDefault().newWatchService();

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
        boolean fileWatcherIsBusy = false;
        boolean poll = true;

        while ((fromServer = connection.serverIn.readLine()) != null) {
            System.out.println("Server: " + fromServer);

            // Server close
            if (fromServer.contains("\u0004")) {
                connection.close();
                break;
            }

            // TODO:: Check if fileWatcher detect a change. if so Client isn't able to talk
            clientDir().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            try {
                // TODO:: Client is hanging in this while loop
                while (poll) {
                    WatchKey key = watchService.take();
                    Path path = (Path) key.watchable();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        fileWatcherIsBusy = true;
                        File file = path.resolve((Path) event.context()).toFile();

                        // Send the file to the client
                        if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                            System.out.println("ENTRY_CREATE - File : " + file.getName());
                            connection.serverOut.println("ENTRY_CREATE - File : " + file.getName() + "\u0003");
                        }

                        // Delete file from the client
                        if (event.kind() == ENTRY_DELETE) {
                            System.out.println("ENTRY_DELETE - File : " + file.getName());
                            connection.serverOut.println("ENTRY_DELETE - File : " + file.getName() + "\u0003");
                        }
                    }

                    poll = key.reset();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!fileWatcherIsBusy) {
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
}
