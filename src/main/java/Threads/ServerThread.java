package Threads;

import Enums.SocketMode;
import Models.FileHeader;
import Threads.TransferThread;
import Utils.Protocol;
import Utils.Tools;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

            // Start een nieuwe instantie van het Utils.Protocol
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

                // Commando GET
                if (outputLine.startsWith("GET")) {
                    String nextLine;

                    // Splits het commando van de argumenten
                    String[] command = outputLine.split(":");

                    // Als blijkt dat er geen bestandsnaam is meegegeven, geeft een melding terug naar de client
                    if(command.length == 1){
                        clientOut.println("ERR: No file given. Please provide a file name\nEND");
                        continue;
                    }

                    // Ga ervan uit dat de 2de string het bestand is wat gevraagd wordt.
                    File file;
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

                    // Genereer de checksum. In een try catch block om errors te kunnen afvangen.
                    String checksum;
                    try {
                        checksum = Tools.getFileChecksum(md5Digest, myFile.toFile());
                    }catch(FileNotFoundException | NoSuchFileException e){
                        clientOut.println("ERR: File \"" + file.getName() + "\" not found. Try a different file\nEND");
                        System.out.println(e);
                        continue;
                    }

                    // Nadat de checks zijn geweest, stuur het antwoord naar de Client
                    clientOut.println(outputLine);

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
                    clientOut.println(fh);

                    // Lees de volgende input van de gebruiker
                    while ((nextLine = clientIn.readLine()) != null){
                        // Als we van de client de melding krijgen dat de header ontvangen is.
                        if(nextLine.equals("HEADER_RECEIVED")){
                            // Geeft get signaal dat het zich moet voorbereiden op de transfer.
                            clientOut.println("PREPARE_FOR_TRANSFER");
                        }

                        // Als we van de client de melding krijgen dat het klaar is om een bestand te ontvangen.
                        if(nextLine.equals("READY_FOR_TRANSFER")){
                            // Als de socket bestaat dan open je alleen maar een nieuwe thread. Anders creeer je de socket.
                            try (ServerSocket transferSocket = new ServerSocket(42068)) {
                                // Geef door dat de socket open staat met hostname en port nummer
                                clientOut.println("OPEN:localhost:42068");
                                // Open een nieuwe transferThread en luister naar een verbinding
                                new TransferThread(SocketMode.SENDING, transferSocket.accept(), command[1]).start();

                            } catch (IOException e) {
                                // Anders melden we dat het niet gemaakt kan worden.
                                System.err.println("Could not create ServerSocket on port 42068: " + e.getMessage());
                            }

                            // Als de transfer thread zijn ding heeft gedaan, geven we aan dat het verzenden klaar is
                            clientOut.println("FILE_SEND_COMPLETE");
                        }

                        // Als we van de client de melding krijgen dat het bestand corrupt is.
                        if(nextLine.equals("RECEIVED_FILE_CORRUPT")){
                            // Geeft get signaal dat het zich moet voorbereiden op de transfer die we opnieuw versturen.
                            clientOut.println("PREPARE_FOR_TRANSFER");
                        }

                        // Als we van de client de melding krijgen dat het bestand goed is.
                        if(nextLine.equals("RECEIVED_FILE_VALID")){
                            // Geef aan af te sluiten.
                            clientOut.println("SHUTTING_DOWN");
                            // Geef stop woord door zodat de client weer een commando uit kan voeren.
                            clientOut.println("END");
                            break;
                        }
                    }
                }

                // Commando PUT
                if (outputLine.startsWith("PUT")) {

                }

                if (outputLine.startsWith("OPEN")) {

                    // Nadat de checks zijn geweest, stuur het antwoord naar de Client
                    clientOut.println(outputLine);

                    new TransferThread(SocketMode.SENDING, transferSocket.accept(), "not_used.jpg").start();
                    clientOut.println("END");
                }

                if (outputLine.startsWith("ERR")) {
                    clientOut.println(outputLine);
                }

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
