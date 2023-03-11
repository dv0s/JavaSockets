package client.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection {

    public String hostName;
    public int portNumber;

    public Socket serverSocket = null;
    public Socket fileWatcherSocket = null;
    public PrintWriter serverOut = null;
    public BufferedReader serverIn = null;

    public Connection(String hostName, int portNumber){
        super();
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public Connection establish() throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
        SocketAddress fileWatcherAddress = new InetSocketAddress(hostName, 1234);

        serverSocket = new Socket();
        fileWatcherSocket = new Socket();

        serverSocket.connect(socketAddress);

        // Connect to the client address in another socket
        fileWatcherSocket.connect(fileWatcherAddress);

        serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
        serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

        return this;
    }

    public void close() throws IOException {
        this.serverIn.close();
        this.serverOut.close();
        this.serverSocket.close();

        this.serverOut = null;
        this.serverIn = null;
        this.serverSocket = null;
    }
}
