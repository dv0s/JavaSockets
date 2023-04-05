package nl.socketsoldiers.protocol.utils;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {
    public final ServerSocket commSocket;

    public ServerConnection(ServerSocket commSocket){
        this.commSocket = commSocket;
    }


}
