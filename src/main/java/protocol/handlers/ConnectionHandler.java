package protocol.handlers;

import protocol.enums.Constants;
import protocol.threads.CommunicationThread;
import protocol.threads.FileTransferThread;
import protocol.utils.ClientConnection;
import protocol.utils.ServerConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Path;

public class ConnectionHandler {

    public Path homeDirectory;

    public Socket commSocket = null;
    public Socket dataSocket = null;

    public ConnectionHandler(Path homeDirectory) {
        super();

        this.homeDirectory = homeDirectory;
    }

    // TODO: FIX ServerConnection handler moet verantwoordelijk worden voor de OPEN commando
    //  Optie bij maken dat data socket ook constant open staat en waar verschillende nieuwe threads voor bestanden bij worden aangemaakt
    public ConnectionHandler establish(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Argument mismatch for setting up the connection!");
            System.exit(2);
        }

        String hostName = args[0];

        SocketAddress socketAddress = new InetSocketAddress(hostName, Constants.Integers.COMM_PORT.getValue());

        commSocket = new Socket();
        commSocket.connect(socketAddress);

        socketAddress = new InetSocketAddress(hostName, Constants.Integers.DATA_PORT.getValue());

        dataSocket = new Socket();
        dataSocket.connect(socketAddress);

        // These will be instantiated within the commands.
//        in = new BufferedReader(new InputStreamReader(commSocket.getInputStream()));
//        out = new PrintWriter(commSocket.getOutputStream(), true);

        return this;
    }

    public static ServerConnection setupServer(){
        System.out.println("Setting up sockets...");

        ServerSocket serverCommSocket;
        try {
            serverCommSocket = new ServerSocket(Constants.Integers.COMM_PORT.getValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ServerConnection(serverCommSocket);
    }

    public static ClientConnection setupClient(String hostName){
        System.out.println("Setting up sockets...");

        SocketAddress commAddress = new InetSocketAddress(hostName, Constants.Integers.COMM_PORT.getValue());
        Socket commSocket = new Socket();

        SocketAddress dataAddress = new InetSocketAddress(hostName, Constants.Integers.DATA_PORT.getValue());
        Socket dataSocket = new Socket();

        return new ClientConnection(commSocket, commAddress, dataSocket, dataAddress);
    }

    public void close() throws IOException {
        this.commSocket.close();
        this.dataSocket.close();

        this.commSocket = null;
        this.dataSocket = null;
    }
}
