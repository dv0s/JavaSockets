package server.threads;

import protocol.Protocol;
import protocol.enums.Command;
import server.handlers.*;

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
            outputLine = "Hello";

            clientOut.println(outputLine);

            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println(Thread.currentThread().getId() + " Client: " + inputLine);

                Command command = protocol.processInput(inputLine);

                // Handle the commands
                switch (command){
                    case GET -> new Get().handle();
                    case CLOSE -> new Close().handle();
                }

                outputLine = protocol.ouput(protocol.processInput(inputLine));
                clientOut.println(outputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
