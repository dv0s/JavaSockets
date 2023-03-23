package client;

import client.threads.ClientThread;
import client.threads.FileWatcherThread;
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
    public static void main(String[] args) {
        ClientThread clientThread = null;

        Tools.startScreen();

        if (args.length != 2) {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        System.out.println("File sync client started. v0.0.1");
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "client");

        clientThread = new ClientThread(args, homeDirectory);
        clientThread.start();
    }
}
