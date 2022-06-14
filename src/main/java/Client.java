import Models.FileHeader;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class Client {

    private static String baseDir = System.getProperty("user.home") +
            File.separator + "documents" +
            File.separator + "avans" +
            File.separator + "filesync";


    public static void main(String[] args) throws IOException {

        System.out.println("User home dir is: " + baseDir);

        // Bekijk alles in het mapje. Dit kan weer handig zijn voor het uitlezen en bepalen welke bestanden er
        // gesynct kan worden.
//        Tools.ScanDir(baseDir);

        // Check argumenten voor het starten van de applicatie, in dit geval hostname en poortnummer
        if (args.length != 2) {
            System.err.println("Usage: java Server <hostname> <port number>");
            System.exit(1);
        }

        // Maak van de argumenten variablen waarmee je kan werken.
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Maak variabelen aan die je later gaat gebruiken in de code.
        boolean connected = false;
        int attempts = 0;
        Socket serverSocket = null;
        PrintWriter serverOut = null;
        BufferedReader serverIn = null;

        // Probeer als client de connectie op te zetten naar de server.
        // Blijf het proberen totdat je een verbinding hebt.
        while (!connected) {
            try {
                // Deze socket is voor het doorsturen en ontvangen van commando's
                // Maak een socketAddress klasse aan.
                SocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);

                // Initieer een socket instantie
                serverSocket = new Socket();

                // En probeer verbinding te maken.
                serverSocket.connect(socketAddress);

                // Omdat we commando's heen en weer moeten sturen, hebben een twee streams nodig.
                serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
                serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

                // En als er een connectie is, breek uit de loop.
                connected = true;

            } catch (IOException ex) {
                // Mocht de verbinding niet tot stand mogen komen, probeer dan opnieuw.
                try {
                    // Probeer het voor 10 keer.
                    if(attempts < 10) {
                        attempts++;
                        System.out.println("Attempt "+ attempts +" to connect.. please wait.");
                        Thread.sleep(2000);
                    }else{
                        // Anders sluiten we af, en proberen we het later opnieuw.
                        System.err.println("Server doesn't seem te be up and running. Please try again later.");
                        System.exit(2);
                    }
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }

        // Zet een BufferdReader op voor de console waar wij onze commando's daadwerkelijk in typen.
        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        // Definieer twee strings
        String fromServer, fromUser;

        // TODO: 06/06/2022 Er moet een manier zijn om deze loop te skippen zodra de server nog meer te melden heeft.
        while ((fromServer = serverIn.readLine()) != null) {
            System.out.println("Server: " + fromServer);

            if (fromServer.startsWith("GET")){
                // TODO: 14/06/2022 Methode hiervan maken die eventueel statisch gebruikt kan worden.
                //  Socket, BaseDir(Path), File(name),

                FileHeader rfh = null;
                FileOutputStream fos;
                String nextLine;
                while((nextLine = serverIn.readLine()) != null){

                    // Als de server output begint met "FileHeader", dan moeten we die opslaan en aangeven dat we
                    // de header hebben ontvangen.
                    if (nextLine.startsWith("FileHeader")) {
                        // Sla de header op.
                        rfh = new FileHeader().createFromString(nextLine);
                        // Geef antwoord.
                        serverOut.println("HEADER_RECEIVED");
                    }

                    // Server geeft aan dat we ons klaar moeten maken voor bestand overdracht.
                    if (nextLine.equals("PREPARE_FOR_TRANSFER")) {
                        // Maak een nieuw bestand object aan. Dit doen we omdat we dan meer gegevens uit kunnen lezen van wat
                        // we precies gaan versturen. Dit wordt gedaan via de java.nio.files package.
                        String fn = "avatar.png";
                        String des = baseDir + File.separator + "catch";

                        // Maak eerst tijdelijke bestanden en mappen aan, voor het geval dat ze dus niet bestaan.
                        if (Files.notExists(Paths.get(des + File.separator + fn))) {
                            // Pak het pad dat je wilt hebben.
                            Path baseDir = Paths.get(des);

                            // Plak daar de bestandsnaam aan vast via de resolve methode. Deze werkt voor directories als er geen
                            // extensie aanwezig is. Ander is het een bestand.
                            Path fileLocation = baseDir.resolve(fn);

                            // Maak het bestand aan op het volledige pad dat is gegeven met de resolve functie hierboven.
                            Files.createFile(fileLocation);

                        }

                        // Nu dat het bestand is aangemaakt, kunnen we het pad pakken. Dit kan dan op een slimmere manier worden gedaan.
                        Path path = FileSystems.getDefault().getPath(des, fn);

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
                        serverOut.println("READY_FOR_TRANSFER");

                        // Probeer als client de connectie op te zetten naar de server.
                        // Blijf het proberen totdat je een verbinding hebt.
                        boolean transferConnected = false;
                        Socket transferSocket = null;
                        while (!transferConnected) {
                            try {
                                // Deze socket is voor het doorsturen en ontvangen van commando's
                                // Maak een socketAddress klasse aan.
                                SocketAddress transferSocketAddress = new InetSocketAddress(hostName, 42068);

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

                        // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
                        while ((count = in.read(buffer)) >= 0) {
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

                        //## EINDE CHECKSUM GEDEELTE
                        // TODO: 14/06/2022 Check is nu alleen nog op checksum. Dit moet uiteindelijk op header. 
                        if(checksum.equals(rfh != null ? rfh.getChecksum() : null)) {
                            serverOut.println("RECEIVED_FILE_VALID");
                        }else{
                            System.err.println("Received file is not identical");
                            serverOut.println("RECEIVED_FILE_CORRUPT");
                        }
                    }

                    if (nextLine.equals("SHUTTING_DOWN")) {
                        break;
                    }

                }

            }

            if (fromServer.startsWith("OPEN")) {

                String[] a = fromServer.split(":");
                try (
                        // Open een nieuwe socket voor bestandsoverdacht
                        Socket transferSocket = new Socket(a[1], Integer.parseInt(a[2]));

                        // Omdat we commando's heen en weer moeten sturen, hebben een twee streams nodig.
                        PrintWriter transferOut = new PrintWriter(transferSocket.getOutputStream(), true);
                        BufferedReader transferIn = new BufferedReader(
                                new InputStreamReader(transferSocket.getInputStream()))
                ) {
                    String fromTransfer;

                    while ((fromTransfer = transferIn.readLine()) != null) {
                        System.out.println("ServerThread: " + fromTransfer);
                        FileHeader rfh = new FileHeader();
                        FileOutputStream fos = null;


                    }
//                        _transferSocket.close();
                }
            }

            // Zodra de server "Bye." zegt, dan sluiten we de connectie en de loop.
            if (fromServer.equals("Bye.")) {
                serverOut.println("Bye.");
                serverSocket.close();
                break;
            }

            // Als de server aangeeft dat het klaar is, kunnen we een nieuwe commando doorgeven.
            if (fromServer.equals("END")) {
                // Pak wat er is ingevoerd in de command line
                System.out.print("Command: ");
                // TODO: 14/06/2022 Command not implemented set state correctly 
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);

                    // Stuur de input door naar de Server.
                    serverOut.println(fromUser);
                }
            }
        }
    }
}
