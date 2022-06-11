import Models.FileHeader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class Client {

    private static String baseDir = String.valueOf(System.getProperty("user.home") +
            File.separator + "documents" +
            File.separator + "avans" +
            File.separator + "filesync");


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

        try (
                // Probeer een socket op te zetten naar de server die open zou moeten staan.
                // Deze socket is voor het doorsturen en ontvangen van commando's
                Socket serverSocket = new Socket(hostName, portNumber);

                // Omdat we commando's heen en weer moeten sturen, hebben een twee streams nodig.
                PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
                BufferedReader serverIn = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()))
        ) {
            // Zet een BufferdReader op voor de console waar wij onze commando's daadwerkelijk in typen.
            BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
            // Definieer twee strings
            String fromServer, fromUser;

            // TODO: 06/06/2022 Er moet een manier zijn om deze loop te skippen zodra de server nog meer te melden heeft.
            while ((fromServer = serverIn.readLine()) != null) {
                System.out.println("Server: " + fromServer);

                // TODO: 11/06/2022 First open te connection

                // TODO: 11/06/2022 Next receive the header and store it for later

                // TODO: 11/06/2022 Start the transaction and check if the file received is valid.

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
                            System.out.println("Transfer: " + fromTransfer);
                            FileHeader rfh = new FileHeader();
                            FileOutputStream fos = null;

                            if (fromTransfer.startsWith("FileHeader")) {
                                rfh.createFromString(fromTransfer);
                                transferOut.println("HEADER_RECEIVED");
                            }

                            if (fromTransfer.equals("PREPARE_FOR_TRANSFER")) {
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


                                // Als het pad niet bestaat..
                                if (!Files.exists(path)) {
                                    // Maak de mappen dan aan.
                                    Files.createDirectories(path.getParent());
                                }

                                // Open een stream voor het te ontvangen bestand waar we naartoe gaan schrijven (Output gaat naar
                                // de andere kant toe).
                                fos = new FileOutputStream(String.valueOf(path));
                                transferOut.println("READY_FOR_TRANSFER");

                                // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
                                // de andere kant).
                                BufferedInputStream in = new BufferedInputStream(transferSocket.getInputStream());
                                System.out.println("Getting the file");

                                // Count variable voor de loop straks.
                                int count;
                                // Bepaal een buffer.
                                byte[] buffer = new byte[16 * 1024];

                                // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
                                // TODO: 11/06/2022 Transactie gaat goed, maar de buffer schijnt niet leeg te worden.
                                    // De laatste 20 bits blijven over en daarna blijft de loop hangen.
                                while ((count = in.read(buffer)) >= 0) {
                                    System.out.println(count);
                                    // Schrijf naar het bestand stream toe.
                                    fos.write(buffer, 0, count);
                                    // En wel direct.
                                    fos.flush();
                                }

                                fos.close();

                                System.out.println("Generating the checksum");
                                //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
                                // Bepaal het algoritme voor het hashen.
                                MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
                                // Genereer de checksum.
                                String checksum = Tools.getFileChecksum(md5Digest, path.toFile());
                                // Print de checksum uit.
                                System.out.println("SHA-256 client checksum: " + checksum);
                                //## EINDE CHECKSUM GEDEELTE

                                transferOut.println("RECEIVED_FILE_VALID");
                                System.out.println("RECEIVED_FILE_VALID sent to transferStream");
                                // TODO: 11/06/2022 maak eigen fileHeader 
                                // TODO: 11/06/2022 vergelijk met de received file header 
                                // TODO: 11/06/2022 Handel vergelijking af door gelijk opnieuw te proberen of dankjewel te zeggen.
                            }

                            if (fromTransfer.equals("SHUTTING_DOWN"))
                            {
                                transferSocket.close();
                                break;
                            }
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
                    fromUser = stdIn.readLine();
                    if (fromUser != null) {
                        System.out.println("Client: " + fromUser);

                        // Stuur de input door naar de Server.
                        serverOut.println(fromUser);
                    }
                }
            }

        } catch (
                UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (
                IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } catch (
                NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

}
