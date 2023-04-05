package nl.socketsoldiers.server;

import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.handlers.ConnectionHandler;
import nl.socketsoldiers.protocol.handlers.FileHandler;
import nl.socketsoldiers.protocol.threads.CommunicationThread;
import nl.socketsoldiers.protocol.utils.ServerConnection;
import nl.socketsoldiers.protocol.utils.Tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) throws IOException {
        Tools.startScreen();

        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        System.out.println("File sync server started. " + Constants.Strings.VERSION);
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.Strings.BASE_DIR + File.separator + "server");

        ArrayList<String> fileList = FileHandler.directoryList(homeDirectory);
        fileList.forEach(System.out::println);

        ServerConnection connection = ConnectionHandler.setupServer();

        while(true){
            new CommunicationThread(homeDirectory, connection).start();
        }

    }
}
