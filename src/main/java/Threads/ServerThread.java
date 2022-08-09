package Threads;

import Enums.SocketMode;
import Models.FileHeader;
import Threads.TransferThread;
import Utils.Protocol;
import Utils.Tools;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.*;
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

                // region CLIENT COMMAND GET
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
                                new TransferThread(SocketMode.SENDING, transferSocket.accept(), "send", command[1]).start();

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
                // endregion

                // region CLIENT COMMAND PUT
                // Commando PUT
                if (outputLine.startsWith("PUT")) {
                    FileHeader rfh = null;
                    FileOutputStream fos;
                    String nextLine;

                    // Splits het commando van de argumenten
                    String[] command = outputLine.split(":");

                    // Als blijkt dat er geen bestandsnaam is meegegeven, geeft een melding terug naar de client
                    if(command.length == 1){
                        clientOut.println("ERR: No file given. Please provide a file name\nEND");
                        continue;
                    }
                    // Geef terug wat we gaan doen.
                    clientOut.println(outputLine);

                    // Give the command to send the file header.
                    clientOut.println("REQUEST_FILEHEADER");

                    // Then, start a new loop for the commands coming.
                    while((nextLine = clientIn.readLine()) != null){
                        System.out.println("Client: " + nextLine);

                        // Als de client aangeeft om te stoppen, stop dan direct.
                        if(nextLine.startsWith("END")){
                            clientOut.println("END");
                            break;
                        }

                        // If the client is sending the file header, save it for later.
                        if(nextLine.startsWith("FileHeader")){
                            rfh = new FileHeader().createFromString(nextLine);
                            clientOut.println("HEADER_RECEIVED");
                        }

                        // Client geeft aan dat we ons klaar moeten maken voor bestand overdracht.
                        if(nextLine.startsWith("PREPARE_FOR_TRANSFER")){
                            // Maak een nieuw bestand object aan. Dit doen we omdat we dan meer gegevens uit kunnen lezen van wat
                            // we precies gaan versturen. Dit wordt gedaan via de java.nio.files package.
                            String des = dir + File.separator + "send";

                            // Maak eerst tijdelijke bestanden en mappen aan, voor het geval dat ze dus niet bestaan.
                            if (Files.notExists(Paths.get(des + File.separator + command[1]))) {
                                // Plak daar de bestandsnaam aan vast via de resolve methode. Deze werkt voor directories als er geen
                                // extensie aanwezig is. Ander is het een bestand.
                                Path fileLocation = Paths.get(des + File.separator).resolve(command[1]);

                                // Maak het bestand aan op het volledige pad dat is gegeven met de resolve functie hierboven.
                                Files.createFile(fileLocation);

                            }

                            // Nu dat het bestand is aangemaakt, kunnen we het pad pakken. Dit kan dan op een slimmere manier worden gedaan.
                            Path path = FileSystems.getDefault().getPath(des, command[1]);

                            // TODO: 14/06/2022 FIX ME Duplicate code
                            // Als het pad niet bestaat..
                            if (!Files.exists(path)) {
                                // Maak de mappen dan aan.
                                Files.createDirectories(path.getParent());
                            }

                            // Open een stream voor het te ontvangen bestand waar we naartoe gaan schrijven (Output gaat naar
                            // de andere kant toe).
                            fos = new FileOutputStream(String.valueOf(path));

                            // Geef aan de server door dat we klaar staan voor het overbrengen.
                            clientOut.println("READY_FOR_TRANSFER");

                            // Probeer als client de connectie op te zetten naar de server.
                            // Blijf het proberen totdat je een verbinding hebt.
                            boolean transferConnected = false;
                            Socket transferSocket = null;
                            while (!transferConnected) {
                                try {
                                    // Deze socket is voor het doorsturen en ontvangen van commando's
                                    // Maak een socketAddress klasse aan.
                                    SocketAddress transferSocketAddress = new InetSocketAddress(socket.getInetAddress().getHostName(), 42068);

                                    // Initieer een socket instantie
                                    transferSocket = new Socket();

                                    // En probeer verbinding te maken.
                                    transferSocket.connect(transferSocketAddress);

                                    // En als er een connectie is, breek uit de loop.
                                    transferConnected = true;

                                } catch (IOException ex) {
                                    // Mocht de verbinding niet tot stand mogen komen, probeer dan opnieuw.
                                    try {
                                        System.out.println("Attempting to connect to transfer socket.. please wait.");
                                        Thread.sleep(2000);
                                    } catch (InterruptedException exc) {
                                        throw new RuntimeException(exc);
                                    }
                                }
                            }

                            // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
                            // de andere kant).
                            BufferedInputStream in = new BufferedInputStream(transferSocket.getInputStream());

                            // Count variabele voor de loop straks.
                            int count;

                            // Bepaal een buffer.
                            byte[] buffer = new byte[16 * 1024];

                            System.out.print("Processing.");
                            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
                            while ((count = in.read(buffer)) >= 0) {
                                System.out.print(".");
                                // Schrijf naar het bestand stream toe.
                                fos.write(buffer, 0, count);
                                // En wel direct.
                                fos.flush();
                            }

                            // Na het lezen en wegschrijven van de bytes sluiten we de streams.
                            fos.close();
                            transferSocket.close();

                            //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
                            // Bepaal het algoritme voor het hashen.
                            MessageDigest md5Digest = null;
                            try {
                                md5Digest = MessageDigest.getInstance("SHA-256");
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }

                            // Genereer de checksum.
                            String checksum = Tools.getFileChecksum(md5Digest, path.toFile());
                            // Print de checksum uit.
                            System.out.println("SHA-256 client checksum: " + checksum);

                            //## EINDE CHECKSUM GEDEELTE
                            // TODO: 14/06/2022 Check is nu alleen nog op checksum. Dit moet uiteindelijk op header.
                            if (checksum.equals(rfh != null ? rfh.getChecksum() : null)) {
                                clientOut.println("RECEIVED_FILE_VALID");
                            } else {
                                System.err.println("Received file is not identical");
                                clientOut.println("RECEIVED_FILE_CORRUPT");
                            }

                        }

                        // Client shutting down, send END message for new commands.
                        if(nextLine.equals("SHUTTING_DOWN")){
                            clientOut.println("END");
                            break;
                        }
                    }

                    continue;
                }
                //endregion

                if (outputLine.startsWith("OPEN")) {

                    // Nadat de checks zijn geweest, stuur het antwoord naar de Client
                    clientOut.println(outputLine);

                    new TransferThread(SocketMode.SENDING, transferSocket.accept(), "send", "not_used.jpg").start();
                    clientOut.println("END");
                }

                if (outputLine.startsWith("ERR")) {
                    clientOut.println(outputLine);
                }

                if (outputLine.startsWith("END"))
                {
                    clientOut.println("END");
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
