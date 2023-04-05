package nl.socketsoldiers.protocol.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientConnection {
    public Socket commSocket;
    public final SocketAddress commAddress;
    public Socket dataSocket;
    public final SocketAddress dataAddress;

    public ClientConnection(Socket commSocket, SocketAddress commAddress, Socket dataSocket, SocketAddress dataAddress) {
        this.commSocket = commSocket;
        this.commAddress = commAddress;
        this.dataSocket = dataSocket;
        this.dataAddress = dataAddress;
    }

    public void connect(Socket socket, SocketAddress socketAddress) throws IOException {
        socket.connect(socketAddress);
    }

    public void close() throws IOException {
        this.commSocket.close();
        this.dataSocket.close();

        this.commSocket = null;
        this.dataSocket = null;
    }
}
