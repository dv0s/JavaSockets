package server;

import protocol.enums.Constants;
import protocol.utils.Tools;
import server.threads.CommunicationThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws IOException{
        System.out.println("" +
                " _____            _        _     _____       _     _ _               \n" +
                "/  ___|          | |      | |   /  ___|     | |   | (_)              \n" +
                "\\ `--.  ___   ___| | _____| |_  \\ `--.  ___ | | __| |_  ___ _ __ ___ \n" +
                " `--. \\/ _ \\ / __| |/ / _ \\ __|  `--. \\/ _ \\| |/ _` | |/ _ \\ '__/ __|\n" +
                "/\\__/ / (_) | (__|   <  __/ |_  /\\__/ / (_) | | (_| | |  __/ |  \\__ \\\n" +
                "\\____/ \\___/ \\___|_|\\_\\___|\\__| \\____/ \\___/|_|\\__,_|_|\\___|_|  |___/\n" +
                "                                                                     \n");

        if(args.length != 1){
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        System.out.println("File sync server started. v0.0.1");

        try{
            Path path = Paths.get(Constants.BASE_DIR.toString());

            if(Files.notExists(path)){
                Files.createDirectories(path);
                System.out.println("Base directory has been created. Location is: " + path);
            } else {
                System.out.println("Base directory location: " + path);
            }

        }catch(IOException e){
            System.err.println("Failed to create directory!");
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try(
            ServerSocket serverSocket = new ServerSocket(portNumber);
        ){

            System.out.println("Waiting for connections...");

            while(listening){
                new CommunicationThread(serverSocket.accept()).start();
            }

        }catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " + portNumber + ".");
            System.out.println(e.getMessage());
        }
    }
}
