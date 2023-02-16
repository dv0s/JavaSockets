import Enums.SocketMode;
import Models.FileHeader;
import Threads.TransferThread;
import Utils.Tools;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class Client {

    private final static String baseDir = System.getProperty("user.home") +
            File.separator + "documents" +
            File.separator + "avans" +
            File.separator + "filesync";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        System.out.println("User home dir is: " + baseDir);

        // Bekijk alles in het mapje. Dit kan weer handig zijn voor het uitlezen en bepalen welke bestanden er
        // gesynct kan worden.
//        Utils.Tools.ScanDir(baseDir);

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
                    if (attempts < 10) {
                        attempts++;
                        System.out.println("Attempt " + attempts + " to connect.. please wait.");
                        Thread.sleep(2000);
                    } else {
                        // Anders sluiten we af, en proberen we het later opnieuw.
                        System.err.println("Server doesn't seem te be up and running. Please try again later.");
                        System.exit(2);
                    }
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }

        // Zet een BufferedReader op voor de console waar wij onze commando's daadwerkelijk in typen.
        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        // Definieer twee strings
        String fromServer, fromUser;

        while ((fromServer = serverIn.readLine()) != null) {
            System.out.println("Server: " + fromServer);

            // Bestand opvragen aan de server.
            if (fromServer.startsWith("GET")) {
                // Split de input.
                String[] input = fromServer.split(":", 2);
                // Eerste onderdeel is het commando.
                String command = input[0];
                // initieer arguments.
                String[] arguments = null;
                // Als er meer onderdelen van de string is gevonden, dan zijn dat de argumenten.
                if (input.length > 1) {
                    // splits de string op whitespace om meerdere argumenten te kunnen afvangen.
                    arguments = input[1].split(" ");
                }

                // Als er geen argumenten zijn, dan moet je stoppen.
                if (arguments == null || arguments.length == 0) {
                    serverOut.println("END");
                    continue;
                }

                // Voer getFile uit.
                getFile(serverOut, serverIn, serverSocket, baseDir, arguments[0]);
            }

            // Bestand versturen naar de server.
            if (fromServer.startsWith("PUT")) {
                System.out.println("Going for the put route.");
                // Split de input.
                String[] input = fromServer.split(":", 2);
                // Eerste onderdeel is het commando.
                String command = input[0];
                // initieer arguments.
                String[] arguments = null;
                // Als er meer onderdelen van de string is gevonden, dan zijn dat de argumenten.
                if (input.length > 1) {
                    // splits de string op whitespace om meerdere argumenten te kunnen afvangen.
                    arguments = input[1].split(" ");
                }

                // Als er geen argumenten zijn, dan moet je stoppen.
                if (arguments == null || arguments.length == 0) {
                    serverOut.println("END");
                    continue;
                }

                // Check of het bestand bestaat
                File sendFile = new File(baseDir + File.separator + "catch" + File.separator + arguments[0]);
                if(!sendFile.exists()){
                    serverOut.println("END");
                    continue;
                }else {
                    // Voer putFile uit.
                    putFile(serverOut, serverIn, serverSocket, baseDir, arguments[0]);
                }
            }

            if(fromServer.startsWith("SYNC")){
                System.out.println("Going for the sync route.");
                // Maak van de directory een File object
                File dir = new File(baseDir + File.separator + "catch");

                System.out.println(Objects.requireNonNull(dir.listFiles()));

                ObjectOutputStream serverObjOut = new ObjectOutputStream(serverSocket.getOutputStream());
                serverObjOut.writeObject(Objects.requireNonNull(dir.listFiles()));
                // Voor elk bestand in de directory...
                for (File file: Objects.requireNonNull(dir.listFiles())
                     ) {
                    // Maak er een path object van
                    Path path = FileSystems.getDefault().getPath(file.getPath());
                    // Lees de basic file attributen uit
                    BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
                    // print de lastAccessTime
                    System.out.println(basicView.readAttributes());
                }

                // Tools.ScanDir(baseDir + File.separator + "catch");
                // Als het commando voor Sync wordt gegeven, dan moeten er een aantal dingen gebeuren.
                // De client moet een lijst opstellen van alle bestanden die het heeft, en de server ook.
                // Die twee lijsten moeten met elkaar vergeleken worden en daar vandaan moeten de bestanden
                // naar de juiste kant worden gestuurd.
                //
                // Wat ook een optie is, is om eerst alles naar de server op te sturen, en daarna de verschillen met
                // de client te bekijken en daarvan de missende bestanden opsturen.
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
                        System.out.println("Threads.ServerThread: " + fromTransfer);
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

            // Als de server aangeeft dat er een error is, kunnen we een nieuwe commando doorgeven.
            if (fromServer.startsWith("ERR")) {
                // Doe niets. Mochten we later wat willen doen hiermee, dan is hier de opening.
                continue;
            }
        }
    }

    public static boolean getFile(PrintWriter serverOut, BufferedReader serverIn, Socket socket, String baseDir, String file) throws IOException {
        FileHeader rfh = null;
        FileOutputStream fos;
        String nextLine;
        while ((nextLine = serverIn.readLine()) != null) {

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
                String des = baseDir + File.separator + "catch";

                // Maak eerst tijdelijke bestanden en mappen aan, voor het geval dat ze dus niet bestaan.
                if (Files.notExists(Paths.get(des + File.separator + file))) {
                    // Plak daar de bestandsnaam aan vast via de resolve methode. Deze werkt voor directories als er geen
                    // extensie aanwezig is. Ander is het een bestand.
                    Path fileLocation = Paths.get(des + File.separator).resolve(file);

                    // Maak het bestand aan op het volledige pad dat is gegeven met de resolve functie hierboven.
                    Files.createFile(fileLocation);

                }

                // Nu dat het bestand is aangemaakt, kunnen we het pad pakken. Dit kan dan op een slimmere manier worden gedaan.
                Path path = FileSystems.getDefault().getPath(des, file);

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
                    serverOut.println("RECEIVED_FILE_VALID");
                } else {
                    System.err.println("Received file is not identical");
                    serverOut.println("RECEIVED_FILE_CORRUPT");
                }
            }

            if (nextLine.equals("SHUTTING_DOWN")) {
                break;
            }

        }
        return true;
    }

    public static boolean putFile(PrintWriter serverOut, BufferedReader serverIn, Socket socket, String baseDir, String fileName) throws IOException, NoSuchAlgorithmException {
        String nextLine;

        while ((nextLine = serverIn.readLine()) != null) {
            System.out.println("Server: " + nextLine);

            File file;
            // maak een fileheader string aan
            if (nextLine.startsWith("REQUEST_FILEHEADER")) {
                try {
                    file = new File(baseDir + File.separator + "catch" + File.separator + fileName);
                } catch (NullPointerException e) {
                    System.err.print(e.getMessage());
                    continue;
                }

                // Pak het bestand die je wilt versturen.
                Path myFile = FileSystems.getDefault().getPath(baseDir + File.separator + "catch", file.getName());

                //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
                // Bepaal het algoritme voor het hashen.
                MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");

                // Genereer de checksum. In een try catch block om errors te kunnen afvangen.
                String checksum;
                try {
                    checksum = Tools.getFileChecksum(md5Digest, myFile.toFile());
                } catch (FileNotFoundException | NoSuchFileException e) {
                    serverOut.println("END: File \"" + file.getName() + "\" not found. Try a different file.");
                    break;
                }

                // Print de checksum uit. (debugging)
                System.out.println("SHA-256 server checksum: " + checksum);

                // Nu dat we een checksum hebben kunnen we een FileHeader maken die we door kunnen sturen naar de client.
                FileHeader fh = new FileHeader(
                        myFile.getFileName().toString(),
                        FilenameUtils.getExtension(myFile.getFileName().toString()),
                        Files.size(myFile),
                        "SHA-256",
                        checksum
                );

                // Geef de header door aan de server.
                serverOut.println(fh);
            }

            // Zodra de server heeft aangegeven dat de header ontvangen is, gaan we het bestand over brengen.
            if (nextLine.equals("HEADER_RECEIVED")) {
                serverOut.println("PREPARE_FOR_TRANSFER");
            }

            // Als we van de server de melding krijgen dat het klaar is om een bestand te ontvangen.
            if (nextLine.equals("READY_FOR_TRANSFER")) {
                // Als de socket bestaat dan open je alleen maar een nieuwe thread. Anders creeer je de socket.
                try (ServerSocket transferSocket = new ServerSocket(42068)) {
                    // Geef door dat de socket open staat met hostname en port nummer
                    serverOut.println("OPEN:localhost:42068");
                    // Open een nieuwe transferThread en luister naar een verbinding
                    new TransferThread(SocketMode.SENDING, transferSocket.accept(), "catch", fileName).start();

                } catch (IOException e) {
                    // Anders melden we dat het niet gemaakt kan worden.
                    System.err.println("Could not create ServerSocket on port 42068: " + e.getMessage());
                }

                // Als de transfer thread zijn ding heeft gedaan, geven we aan dat het verzenden klaar is
                serverOut.println("FILE_SEND_COMPLETE");
            }

            // Als we van de client de melding krijgen dat het bestand corrupt is.
            if (nextLine.equals("RECEIVED_FILE_CORRUPT")) {
                // Geeft get signaal dat het zich moet voorbereiden op de transfer die we opnieuw versturen.
                serverOut.println("PREPARE_FOR_TRANSFER");
            }

            // Als we van de client de melding krijgen dat het bestand goed is.
            if (nextLine.equals("RECEIVED_FILE_VALID")) {
                // Geef aan af te sluiten.
                serverOut.println("SHUTTING_DOWN");
                break;
            }
        }
        return true;
    }
}
