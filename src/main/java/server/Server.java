package server;

import server.threads.CommunicationThread;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException{

        if(args.length != 1){
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try(
            ServerSocket serverSocket = new ServerSocket(portNumber);
        ){

            while(listening){
                new CommunicationThread(serverSocket.accept()).start();
            }

        }catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " + portNumber + ".");
            System.out.println(e.getMessage());
        }
    }
}
