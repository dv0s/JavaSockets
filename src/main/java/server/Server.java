package server;

import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.handlers.FileHandler;
import protocol.utils.Tools;

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

        System.out.println("File sync server started. " + Constants.VERSION);
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "server");

        ArrayList<String> fileList = FileHandler.directoryList(homeDirectory);
        fileList.forEach(System.out::println);

        new ConnectionHandler(Invoker.SERVER, homeDirectory).establish(args);
    }
}
