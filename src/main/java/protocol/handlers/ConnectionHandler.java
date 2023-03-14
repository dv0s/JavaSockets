package protocol.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectionHandler {

    public String hostName;
    public int portNumber;

    public Socket serverSocket = null;
    public PrintWriter serverOut = null;
    public BufferedReader serverIn = null;

    public ConnectionHandler(String hostName, int portNumber){
        super();
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public ConnectionHandler establish() throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
        serverSocket = new Socket();

        serverSocket.connect(socketAddress);

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