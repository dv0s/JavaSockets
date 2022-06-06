import java.io.*;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException {

        // Check argumenten voor het starten van de applicatie, in dit geval hostname en poortnummer
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        // Maak van de argumenten variablen waarmee je kan werken.
        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try (
                // Probeer een socket op te zetten waar de server op luistert naar clients.
                ServerSocket serverSocket = new ServerSocket(portNumber);
        ) {
            int conn_count = 0;
            while(listening){
                conn_count++;
                // Start een thread waar een client op aanhaakt.
                new ServerThread(serverSocket.accept(), conn_count).start();
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
