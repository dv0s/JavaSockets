package server.threads;

import protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class CommunicationThread extends Thread {
    private Socket socket;
    private ServerSocket transferSocket;

    public CommunicationThread(Socket socket) {
        super();
        this.socket = socket;
    }

    public void run() {
        try (
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String inputLine, outputLine;

            System.out.println("Listening...");

            Protocol protocol = new Protocol();
            outputLine = protocol.processInput(null);

            clientOut.println(outputLine);

            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println("Client: " + inputLine);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
