package protocol.threads;

import protocol.Protocol;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class CommunicationThread extends Thread {
    private final Socket socket;
    private final Path homeDirectory;

    public CommunicationThread(Path homeDirectory, Socket socket) {
        super();

        this.socket = socket;
        this.homeDirectory = homeDirectory;
    }

    public void run() {
        try (
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            long clientId = Thread.currentThread().getId();
            System.out.println("ConnectionHandler established with Client: " + clientId);

            Protocol protocol = new Protocol(homeDirectory);

//            clientOut.println("Connection established. Welcome client #" + clientId + Constants.END_OF_TEXT);
//            protocol.processInput(Invoker.SERVER, Command.SYNC.toString(), socket); // TODO: FIX Client needs to make this call

            String inputLine;
            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println(clientId + " Client: " + inputLine);
//                protocol.processInput(Invoker.SERVER, inputLine, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
