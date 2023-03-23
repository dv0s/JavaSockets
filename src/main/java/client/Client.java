package client;

import client.threads.ClientThread;
import client.threads.FileWatcherThread;
import protocol.Protocol;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.utils.Tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Client {
    public static void main(String[] args) {
        ClientThread clientThread;
        FileWatcherThread fileWatcherThread;
        ConnectionHandler serverConnection;
        Protocol protocol;

        Tools.startScreen();

        if (args.length != 2) {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        System.out.println("File sync client started. v0.0.1");
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "client");

        int attempts = 0;
        boolean connected = false;

        while (!connected) {
            try {
                // Gooi de argumenten door naar connection handler, en laat die het maar verder afhandelen.
                serverConnection = new ConnectionHandler(Invoker.CLIENT, homeDirectory).establish(args);
                connected = true;

                protocol = new Protocol(homeDirectory);

                // Start the fileWatcher thread
                fileWatcherThread = new FileWatcherThread();
                fileWatcherThread.start();

                // Start the client thread
                clientThread = new ClientThread(serverConnection, protocol);
                clientThread.start();
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
    }
}
