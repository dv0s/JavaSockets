package protocol.utils;

import protocol.enums.Invoker;

import java.net.Socket;

public class ConnectionSockets {

    public final Invoker invoker;
    public final Socket commSocket;
    public Socket dataSocket;

    public ConnectionSockets(Invoker invoker, Socket commSocket, Socket dataSocket){
        this.invoker = invoker;
        this.commSocket = commSocket;
        this.dataSocket = dataSocket;
    }

    public static void reInitiate(){

    }
}
