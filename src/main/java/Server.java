import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {

        // Check argumenten voor het starten van de applicatie, in dit geval hostname en poortnummer
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        // Maak van de argumenten variablen waarmee je kan werken.
        int portNumber = Integer.parseInt(args[0]);

        System.out.println("Socket initialized! Listening...");
        try (
                // Probeer een socket op te zetten waar de server op luistert naar clients.
                ServerSocket serverSocket = new ServerSocket(portNumber);
                // En wacht tot er een verbinding binnenkomt.
                Socket clientSocket = serverSocket.accept();
        ) {


            // Open een oneindige lus voor het versturen van het bestand zodra er een client verbinding heeft gemaakt.
            while (true) {
                System.out.println("Client Accepted");

                // Pak het bestand die je wilt versturen.
                File myFile = new File("D:\\Avans\\Socket\\send\\BIGASSFILE.zip");

                // Maak een teller voor straks
                int count;
                // Definieer een buffer straks
                byte[] buffer = new byte[16 * 1024];

                int i = 0;

                // Hiermee kan de grootte van het bestand worden bepaald (Hebben we voor nu niet nodig).
//                byte[] myByteArray = new byte[(int) myFile.length()];

                // Zet een stream op waar we naartoe kunnen schrijven (Output gaat naar de andere kant toe).
                OutputStream out = clientSocket.getOutputStream();

                // Lees het bestand uit in een gebufferde stream (Input krijgt van de andere kant, in dit geval van het
                // bestand wat eerder aangemaakt is).
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));

                // Hier wordt het interessant, we gaan lussen zolang dat wat we krijgen van het bestand niet groter
                // of gelijk is aan 0.
                while((count = in.read(buffer)) >= 0){
                    System.out.println(i + ": " + count);
                    // Schrijf het buffer stukje naar de client stream.
                    out.write(buffer, 0, count);
                    // En wel direct
                    out.flush();
                    i++;
                }

                // Als we klaar zijn, sluiten we de connectie met de client.
                clientSocket.close();

//                System.out.println("Read length: " + myFile.length());
//                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
//
//                System.out.println("Initiated BufferedInputStream");
//                bis.read(myByteArray, 0, myByteArray.length);
//
//                System.out.println("Read the file");
//                OutputStream os = clientSocket.getOutputStream();
//
//                System.out.println("Get output stream and write to stream");
//                os.write(myByteArray, 0, myByteArray.length);
//
//                System.out.println("Flush it");
//                os.flush();
//
//                System.out.println("And close");
//                os.close();
//
//                clientSocket.close();

                // Breek uit de lus.
                break;
            }

            System.out.println("Transfer should be completed.");

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
