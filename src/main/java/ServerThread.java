import Enums.MyState;
import Enums.SocketMode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerThread extends Thread{
    // Private socket voor de thread. Deze krijgt de Thread van de Server klasse.
    private Socket socket;
    private ServerSocket transferSocket;

    // Constructor voor de server thread.
    public ServerThread(Socket socket){
        super();
        this.socket = socket;
    }

    public void run()
    {
        // Initieer de begin map waar de bestanden komen te staan.
        String dir = String.valueOf(System.getProperty("user.home") +
                File.separator + "documents" +
                File.separator + "avans" +
                File.separator + "filesync");

        try(
                // Output stream naar client toe.
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                // Input stream van client.
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ){

            //Variables voor het kunnen lezen en schrijven naar client toe.
            String inputLine, outputLine;

            System.out.println("Listening...");

            // Start een nieuwe instantie van het Protocol
            Protocol p = new Protocol();
            // Zet de huidige outputLine naar niks via het protocol
            outputLine = p.processInput(null);
            // Stuur het naar de client toe.
            clientOut.println(outputLine);

            // While lus die kijkt naar wat de client naar ons stuurt zolang de connectie bestaat.
            while((inputLine = clientIn.readLine()) != null){
                System.out.println("Client: " + inputLine);
                // Zet antwoord klaar vanuit het protocol om naar client te sturen.
                outputLine = p.processInput(inputLine);
                // Stuur het antwoord naar de Client
                clientOut.println(outputLine);

                // Commando GET
                if(outputLine.startsWith("GET")){
                    // Splits het commando van de argumenten
                    String[] command = outputLine.split(":");
                    // Ga ervan uit dat de 2de string het bestand is wat gevraagd wordt.
                    File file = new File(dir + File.separator + "send"+ File.separator  + command[1]);

                    // Als de socket bestaat dan open je alleen maar een nieuwe thread. Anders creeer je de socket.
//                    if(Tools.isSocketAlive(42068)){
//                        clientOut.println("OPEN:localhost:42068");
//                        new TransferThread(SocketMode.SENDING, transferSocket.accept(), file).start();
//                    }else {
                        // Open een nieuwe transferThread
                        try (ServerSocket transferSocket = new ServerSocket(42068)) {
                            clientOut.println("OPEN:localhost:42068");
                            new TransferThread(SocketMode.SENDING, transferSocket.accept(), file).start();
                            clientOut.println("END");

                        } catch (IOException e) {
                            System.err.println("Could not create ServerSocket on port 42068: " + e.getMessage());
//                        socket.close();
//                        ServerSocket transferSocket = new ServerSocket(42068);
//                        clientOut.println("OPEN:localhost:42068");
                        }
//                    }
                }

                // TODO: 06/06/2022 Hier moet de Thread gaan kijken naar welke acties er uitgevoerd moeten worden op commando.
                if(outputLine.startsWith("OPEN"))
                {
                    new TransferThread(SocketMode.SENDING, transferSocket.accept(), new File("")).start();
                    clientOut.println("END");
                }
                // TODO: 06/06/2022 Hier moet logica komen die een nieuwe connectie open zet.
                //  Zodra die beschikbaar is moet een seintje aan de client worden gegeven waar het verbinding mee moet maken.

                // Als het antwoord van de server "Bye." is, dan gaan we uit de loop en sluiten we de connectie.
                if(outputLine.equals("Bye."))
                    break;


            }

            // Sluit de connectie.
            socket.close();
            // Probeer de thread te sluiten.
            interrupt();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
