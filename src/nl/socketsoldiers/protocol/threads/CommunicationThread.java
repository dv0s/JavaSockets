package nl.socketsoldiers.protocol.threads;

import nl.socketsoldiers.protocol.Protocol;
import nl.socketsoldiers.protocol.enums.Command;
import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.Invoker;
import nl.socketsoldiers.protocol.utils.ConnectionSockets;
import nl.socketsoldiers.protocol.utils.ServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class CommunicationThread extends Thread {

    private final ServerConnection connection;
    private final Socket commSocket;
    private final Path homeDirectory;

    public final ConnectionSockets connectionSockets;

    public CommunicationThread(Path homeDirectory, ServerConnection connection) throws IOException {
        super();

        this.homeDirectory = homeDirectory;
        this.connection = connection;

        this.commSocket = connection.commSocket.accept();
        this.connectionSockets = new ConnectionSockets(Invoker.SERVER, commSocket);
    }

    public void run() {
        try (
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
