package protocol.utils;

import protocol.enums.Constants;
import protocol.enums.Invoker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectionSockets {

    public final Invoker invoker;
    public final Socket commSocket;
    public Socket dataSocket;

    public ConnectionSockets(Invoker invoker, Socket commSocket, Socket dataSocket){
        this.invoker = invoker;
        this.commSocket = commSocket;
        this.dataSocket = dataSocket;
    }

    public void reInitiateDataSocket() throws IOException {
        if(invoker == Invoker.CLIENT){
            if(this.dataSocket.isClosed()) {
                System.out.println("Rebuilding data line");

                SocketAddress dataAddress = new InetSocketAddress(this.commSocket.getInetAddress(), Constants.Integers.DATA_PORT.getValue());
                this.dataSocket.connect(dataAddress);
            }
        }else{
            if(this.dataSocket.isClosed()) {
                System.out.println("Rebuilding data line");

                ServerSocket dataSocket = new ServerSocket(Constants.Integers.DATA_PORT.getValue());
                this.dataSocket = dataSocket.accept();
            }
        }
    }
}
