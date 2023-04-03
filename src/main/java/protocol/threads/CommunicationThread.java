package protocol.threads;

import protocol.Protocol;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.utils.ConnectionSockets;
import protocol.utils.ServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class CommunicationThread extends Thread {

    private final ServerConnection connection;
    private final Socket commSocket;
    private final Socket dataSocket;
    private final Path homeDirectory;

    public final ConnectionSockets connectionSockets;

    public CommunicationThread(Path homeDirectory, ServerConnection connection) throws IOException {
        super();

        this.homeDirectory = homeDirectory;
        this.connection = connection;

        this.commSocket = connection.commSocket.accept();
        this.dataSocket = connection.dataSocket.accept();
        this.connectionSockets = new ConnectionSockets(commSocket, dataSocket);
    }

    public void run() {
        try (
                PrintWriter clientOut = new PrintWriter(commSocket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(commSocket.getInputStream()))
        ) {

            long clientId = Thread.currentThread().getId();
            System.out.println("ConnectionHandler established with Client: " + clientId);

            Protocol protocol = new Protocol(homeDirectory);

//            clientOut.println("ServerConnection established. Welcome client #" + clientId + Constants.Strings.END_OF_TEXT);
            protocol.processInput(Invoker.SERVER, Command.SYNC.toString(), connectionSockets); // TODO: FIX Client needs to make this call

            String inputLine;
            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println(clientId + " Client: " + inputLine);
                protocol.processInput(Invoker.SERVER, inputLine, connectionSockets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
