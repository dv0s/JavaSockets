package client.handlers;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection {

    public String hostName;
    public int portNumber;

    public Socket serverSocket = null;
    public ObjectOutputStream serverOut = null;
    public ObjectInputStream serverIn = null;

    public Connection(String hostName, int portNumber){
        super();
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public Connection establish() throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
        serverSocket = new Socket();

        serverSocket.connect(socketAddress);

        serverOut = new ObjectOutputStream(serverSocket.getOutputStream());
        serverIn = new ObjectInputStream(serverSocket.getInputStream());

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
