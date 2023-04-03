package protocol.utils;

import java.net.Socket;

public class ConnectionSockets {
    public final Socket commSocket;
    public final Socket dataSocket;

    public ConnectionSockets(Socket commSocket, Socket dataSocket){
        this.commSocket = commSocket;
        this.dataSocket = dataSocket;
    }
}
