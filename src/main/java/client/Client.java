package client;

import client.handlers.Connection;
import protocol.returnobjects.Message;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if(args.length != 2)
        {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Connection connection = null;
        int attempts = 0;

        boolean connected = false;

        while (!connected) {
            try {
                connection = new Connection(hostName,portNumber).establish();
                connected = true;

            } catch (IOException ex) {
                try {
                    if (attempts < 10) {
                        attempts++;
                        System.out.println("Attempt " + attempts + " to connect.. please wait.");
                        Thread.sleep(2000);
                    } else {
                        System.err.println("Server doesn't seem te be up and running. Please try again later.");
                        System.exit(2);
                    }
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        Message fromUser, fromServer;

        while((fromServer = (Message)connection.serverIn.readObject()) != null){
            System.out.println("Server: " + fromServer.message);

            if(fromServer.message.equals("END")){ // Moet later iets van \r\n zijn.
                fromUser = new Message("Bye.", true);
                connection.serverOut.writeObject(fromUser);
                connection.close();
                break;
            }

            // Er moet een manier worden gevonden om te weten wanneer de client mag praten.
            if(fromServer.messageEnd){

                System.out.print("Command: ");
                // Onze blokkende command afwacht ding
                String command = stdIn.readLine();

                if (command != null) {
                    System.out.println("Client: " + command);
                    fromUser = new Message(command, true);
                    // Stuur de input door naar de Server.
                    connection.serverOut.writeObject(fromUser);
                }
            }
        }

    }
}
