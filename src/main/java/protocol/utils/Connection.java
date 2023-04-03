package protocol.utils;

import java.net.Socket;

public class Connection {
    public final Socket commSocket;
    public final Socket dataSocket;

    public Connection(Socket commSocket, Socket dataSocket){
        this.commSocket = commSocket;
        this.dataSocket = dataSocket;
    }
}
