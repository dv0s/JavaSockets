package server.threads;
import protocol.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationThread extends Thread {
    private final Socket socket;

    public CommunicationThread(Socket socket) {
        super();
        this.socket = socket;
    }

    public void run() {
        try (
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String inputLine, outputLine;
            long clientId = Thread.currentThread().getId();

            System.out.println("Connection established with Client: " + clientId);

            Protocol protocol = new Protocol();
            outputLine = "Hello\u0003";

            clientOut.println(outputLine);

            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println(clientId + " Client: " + inputLine);
                protocol.processInput(inputLine, clientIn, clientOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
