import Enums.MyState;
import Enums.SocketMode;
import Models.FileHeader;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerThread extends Thread {
    // Private socket voor de thread. Deze krijgt de Thread van de Server klasse.
    private Socket socket;
    private ServerSocket transferSocket;

    // Constructor voor de server thread.
    public ServerThread(Socket socket) {
        super();
        this.socket = socket;
    }

    public void run() {
        // Initieer de begin map waar de bestanden komen te staan.
        String dir = String.valueOf(System.getProperty("user.home") +
                File.separator + "documents" +
                File.separator + "avans" +
                File.separator + "filesync");

        try (
                // Output stream naar client toe.
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                // Input stream van client.
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {

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
            while ((inputLine = clientIn.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                // Zet antwoord klaar vanuit het protocol om naar client te sturen.
                outputLine = p.processInput(inputLine);
                // Stuur het antwoord naar de Client
                clientOut.println(outputLine);

                // TODO: 13/06/2022 Ontvang de get command.
                //  Zoek naar het bestand of je die hebt.
                //  Als je die gevonden hebt, stuur via de server thread de header door naar de client
                //  Als de client aangeeft de header te hebben ontvangen, spin dan een nieuwe transfer thread op
                //  TransferThread doet alleen maar het versturen (voor nu nog naar een richting)
                //  Client checkt of het bestand niet corrupt is, zo ja, geef door aan server.
                //  Als server hoort dat het corrupt is, start het proces opnieuw op.
                //  Zo niet, ga terug naar luister status en wacht op nieuw commando.

                // Commando GET
                if (outputLine.startsWith("GET")) {
                    String nextLine;

                    // Splits het commando van de argumenten
                    String[] command = outputLine.split(":");
                    // Ga ervan uit dat de 2de string het bestand is wat gevraagd wordt.
                    File file = null;
                    try {
                        file = new File(dir + File.separator + "send" + File.separator + command[1]);
                    }catch (NullPointerException e){
                        System.err.print(e.getMessage());
                        continue;
                    }

                    // Pak het bestand die je wilt versturen.
                    Path myFile = FileSystems.getDefault().getPath(dir + File.separator + "send", file.getName());

                    //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
                    // Bepaal het algoritme voor het hashen.
                    MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
                    // Genereer de checksum.
                    String checksum = Tools.getFileChecksum(md5Digest, myFile.toFile());
                    // Print de checksum uit.
                    System.out.println("SHA-256 server checksum: " + checksum);
                    //## EINDE CHECKSUM GEDEELTE

                    // Nu dat we een checksum hebben kunnen we een FileHeader maken die we door kunnen sturen naar de client.
                    FileHeader fh = new FileHeader(
                            myFile.getFileName().toString(),
                            FilenameUtils.getExtension(myFile.getFileName().toString()),
                            Files.size(myFile),
                            "SHA-256",
                            checksum
                    );

                    // Geef de header door aan de client
                    System.out.println("ServerThread sent to client: " + fh);
                    clientOut.println(fh);

                    // TODO: 13/06/2022 COMMENTS PLAATSEN HIER!!! en de code verder opruimen
                    // Lees de volgende input van de gebruiker
                    while ((nextLine = clientIn.readLine()) != null){

                        if(nextLine.equals("HEADER_RECEIVED")){
                            System.out.println("Transfer sent to client: PREPARE_FOR_TRANSFER");
                            clientOut.println("PREPARE_FOR_TRANSFER");
                        }

                        if(nextLine.equals("READY_FOR_TRANSFER")){
                            System.out.println("Transfer sent to client: GOING TO SEND THE FILE");
                            // Als de socket bestaat dan open je alleen maar een nieuwe thread. Anders creeer je de socket.
                            // Open een nieuwe transferThread
                            try (ServerSocket transferSocket = new ServerSocket(42068)) {
                                clientOut.println("OPEN:localhost:42068");
                                new TransferThread(SocketMode.SENDING, transferSocket.accept(), file).start();

                            } catch (IOException e) {
                                System.err.println("Could not create ServerSocket on port 42068: " + e.getMessage());
                            }

                            System.out.println("File sent to client");
                            clientOut.println("FILE_SEND_COMPLETE");
                        }

                        if(nextLine.equals("RECEIVED_FILE_CORRUPTED")){
                            System.out.println("Transfer sent to client: RETRY_GOING TO SEND THE FILE");
                            // Als de socket bestaat dan open je alleen maar een nieuwe thread. Anders creeer je de socket.
                            // Open een nieuwe transferThread
                            try (ServerSocket transferSocket = new ServerSocket(42068)) {
                                clientOut.println("OPEN:localhost:42068");
                                new TransferThread(SocketMode.SENDING, transferSocket.accept(), file).start();

                            } catch (IOException e) {
                                System.err.println("Could not create ServerSocket on port 42068: " + e.getMessage());
                            }

                            System.out.println("TransferThread: Bytes are sent");
                            clientOut.println("FILE_SEND_COMPLETE");
                            System.out.println("TransferThread: FILE_SEND_COMPLETE sent to client");

                        }

                        if(nextLine.equals("RECEIVED_FILE_VALID")){
                            clientOut.println("SHUTTING_DOWN");
                            clientOut.println("END");
                            break;
                        }
                    }
                }

                // TODO: 06/06/2022 Hier moet de Thread gaan kijken naar welke acties er uitgevoerd moeten worden op commando.
                if (outputLine.startsWith("OPEN")) {
                    new TransferThread(SocketMode.SENDING, transferSocket.accept(), new File("")).start();
                    clientOut.println("END");
                }
                // TODO: 06/06/2022 Hier moet logica komen die een nieuwe connectie open zet.
                //  Zodra die beschikbaar is moet een seintje aan de client worden gegeven waar het verbinding mee moet maken.

                // Als het antwoord van de server "Bye." is, dan gaan we uit de loop en sluiten we de connectie.
                if (outputLine.equals("Bye."))
                    break;
                }

            // Sluit de connectie.
            socket.close();
            // Probeer de thread te sluiten.
            interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
