package protocol.utils;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {
    public final ServerSocket commSocket;
    public final ServerSocket dataSocket;

    public ServerConnection(ServerSocket commSocket, ServerSocket dataSocket){
        this.commSocket = commSocket;
        this.dataSocket = dataSocket;
    }


}
