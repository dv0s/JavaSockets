package server.threads;
import protocol.Protocol;
import protocol.data.ResponseBody;
import protocol.data.ResponseHeader;
import protocol.returnobjects.Message;
import protocol.returnobjects.Response;

import java.io.*;
import java.net.Socket;

public class CommunicationThread extends Thread {
    private final Socket socket;

    public CommunicationThread(Socket socket) {
        super();
        this.socket = socket;
    }

    public void run() {
        try (
                ObjectOutputStream clientOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream clientIn = new ObjectInputStream(socket.getInputStream())
        ) {
            Message inputObject, outputObject;
            long clientId = Thread.currentThread().getId();

            System.out.println("Connection established with Client: " + clientId);

            Protocol protocol = new Protocol();
            outputObject = new Message("Hello", true);

            clientOut.writeObject(outputObject);

            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while ((inputObject = (Message) clientIn.readObject()) != null) {
                System.out.println(clientId + " Client: " + inputObject);
                protocol.processInput(inputObject.message, clientIn, clientOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
