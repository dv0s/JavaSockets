package protocol.handlers;

import protocol.enums.Invoker;
import protocol.threads.CommunicationThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class ConnectionHandler {

    public Invoker invoker;
    public Path homeDirectory;

    public Socket socket = null;

    public BufferedReader in = null;
    public PrintWriter out = null;

    public ConnectionHandler(Invoker invoker, Path homeDirectory){
        super();

        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
    }

    public ConnectionHandler establish(String[] args) throws IOException {

        if (invoker == Invoker.CLIENT){
            if(args.length != 2){
                System.err.println("Argument mismatch for setting up the connection!");
                System.exit(2);
            }

            String hostName = args[0];
            int portNumber = Integer.parseInt(args[1]);

            SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);

            socket = new Socket();
            socket.connect(socketAddress);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        }else{
            if(args.length != 1){
                System.err.println("Argument mismatch for setting up the connection!");
                System.exit(2);
            }

            int portNumber = Integer.parseInt(args[0]);
            boolean listening = true;

            try(ServerSocket serverSocket = new ServerSocket(portNumber)){

                System.out.println("Waiting for connections...");

                while(listening){
                    new CommunicationThread(homeDirectory, serverSocket.accept()).start();
                }

            }catch(IOException e){
                System.out.println("Exception caught when trying to listen on port " + portNumber + ".");
                System.out.println(e.getMessage());
            }
        }

        return this;
    }

    public void close() throws IOException {
        this.in.close();
        this.out.close();
        this.socket.close();

        this.out = null;
        this.in = null;
        this.socket = null;
    }
}
