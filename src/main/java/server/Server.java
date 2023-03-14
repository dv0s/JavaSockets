package server;

import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.utils.Tools;
import protocol.threads.CommunicationThread;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws IOException{
        Tools.startScreen();

        if(args.length != 1){
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        System.out.println("File sync server started. v0.0.1");
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "server");

        new ConnectionHandler(Invoker.SERVER, homeDirectory).establish(args);


    }
}
