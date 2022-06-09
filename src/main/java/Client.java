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

    // TODO: TransferClient maken!!!
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

                if (fromServer.startsWith("OPEN")) {
                    String[] a = fromServer.split(":");
                    try (
                            Socket transferSocket = new Socket(a[1], Integer.parseInt(a[2]))
                    ) {

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
                        FileOutputStream fos = new FileOutputStream(String.valueOf(path));

                        // Open een gebufferde stream voor het te ontvangen bestand (Welke we overigens weg kunnen laten).
                        //BufferedOutputStream out = new BufferedOutputStream(fos);

                        // Count variable voor de loop straks.
                        int count;
                        int i = 0;
                        // Bepaal een buffer.
                        byte[] buffer = new byte[16 * 1024];

                        // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
                        // de andere kant).
                        InputStream in = transferSocket.getInputStream();

                        // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
                        System.out.println("Write first file");
                        while ((count = in.read(buffer)) >= 0) {
                            // Schrijf naar het bestand stream toe.
                            fos.write(buffer, 0, count);
                            // En wel direct.
                            fos.flush();
                        }

                        //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
                        // Bepaal het algoritme voor het hashen.
                        MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
                        // Genereer de checksum.
                        String checksum = Tools.getFileChecksum(md5Digest, path.toFile());
                        // Print de checksum uit.
                        System.out.println("SHA-256 client checksum: " + checksum);
                        //## EINDE CHECKSUM GEDEELTE

                        transferSocket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }

                // TODO: 06/06/2022 Hier moet de client een commando krijgen wat hij moet uitvoeren.
                if (fromServer.equalsIgnoreCase("Opening new connection.")) {
                    System.out.println("Ah sweet! I'll wait!");
                    continue;
                }

                // Zodra de server "Bye." zegt, dan sluiten we de connectie en de loop.
                if (fromServer.equals("Bye.")) {
                    serverOut.println("Bye.");
                    serverSocket.close();
                    break;
                }

                // TODO: 07/06/2022 Hier kan nog iets om heen om de input van de user pas mogelijk te maken voordat de server klaar is met alles te melden.
//                if (fromServer == null) {
                // Pak wat er is ingevoerd in de command line
                System.out.print("Command:");
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);

                    // Stuur de input door naar de Server.
                    serverOut.println(fromUser);
                }
//                }
            }
// Deze code hoeven we nu nog even niet te gebruiken.
//            // Maak een nieuw bestand object aan. Dit doen we omdat we dan meer gegevens uit kunnen lezen van wat
//            // we precies gaan versturen. Dit wordt gedaan via de java.nio.files package.
//            String fn = "bigpackage.zip";
//            String des = baseDir + File.separator + "catch";
//
//            // Maak eerst tijdelijke bestanden en mappen aan, voor het geval dat ze dus niet bestaan.
//            if(Files.notExists(Paths.get(des + File.separator + fn)))
//            {
//                // Pak het pad dat je wilt hebben.
//                Path baseDir = Paths.get(des);
//
//                // Plak daar de bestandsnaam aan vast via de resolve methode. Deze werkt voor directories als er geen
//                // extensie aanwezig is. Ander is het een bestand.
//                Path fileLocation = baseDir.resolve(fn);
//                System.out.println(fileLocation);
//
//                // Maak het bestand aan op het volledige pad dat is gegeven met de resolve functie hierboven.
//                Files.createFile(fileLocation);
//
//            }
//
//            // Nu dat het bestand is aangemaakt, kunnen we het pad pakken. Dit kan dan op een slimmere manier worden gedaan.
//            Path path = FileSystems.getDefault().getPath(des, fn);
//
//
//
//            //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
//            // Bepaal het algoritme voor het hashen.
//            MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
//            // Genereer de checksum.
//            String checksum = Tools.getFileChecksum(md5Digest, path.toFile());
//            // Print de checksum uit.
//            System.out.println("SHA-256 client checksum: " + checksum);
//            //## EINDE CHECKSUM GEDEELTE
//
//            // Als het pad niet bestaat..
//            if(!Files.exists(path))
//            {
//                // Maak de mappen dan aan.
//                Files.createDirectories(path.getParent());
//            }
//
//            // Open een stream voor het te ontvangen bestand waar we naartoe gaan schrijven (Output gaat naar
//            // de andere kant toe).
//            FileOutputStream fos = new FileOutputStream(String.valueOf(path));
//
//            // Open een gebufferde stream voor het te ontvangen bestand (Welke we overigens weg kunnen laten).
//            //BufferedOutputStream out = new BufferedOutputStream(fos);
//
//            // Count variable voor de loop straks.
//            int count;
//            int i = 0;
//            // Bepaal een buffer.
//            byte[] buffer = new byte[16 * 1024];
//
//            // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
//            // de andere kant).
//            InputStream in = serverSocket.getInputStream();
//
//            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
//            System.out.println("Write first file");
//            while((count = in.read(buffer)) >= 0){
//                // Schrijf naar het bestand stream toe.
//                fos.write(buffer, 0, count);
//                // En wel direct.
//                fos.flush();
//            }
// TOT HIER.

            // Of... We lossen het op met de java.nio.* package (Wat dus niet gelukt was..)

//            // Maak een nieuw bestand object aan. Dit doen we omdat we dan meer gegevens uit kunnen lezen van wat
//            // we precies gaan versturen. Dit wordt gedaan via de java.nio.files package.
//            Path path = FileSystems.getDefault().getPath("D:\\Avans\\Socket\\received", "big_ass_file2.zip");
//
//            // Start een nieuwe RandomAccessFile die gebruik maakt van "Path" om te kunnen lezen en schrijven
//            RandomAccessFile raf = new RandomAccessFile(String.valueOf(path), "rw");
//
//            // Pak de channel van raf.
//            FileChannel fc = raf.getChannel();
//
//            // Pak de channel van serverSocket
//            SocketChannel channel = serverSocket.getChannel();
//
//            // Voor FileChannel moeten we op een andere manier een buffer aanleveren.
//            // Deze verwacht ook een int en geeft ook een int terug.
//            ByteBuffer buf = ByteBuffer.allocate(16 * 1024);
//
//            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
//            System.out.println("Write second file");
//            // Lees van de input stream de buffer.
//            while((count = channel.read(buf)) >= 0){
//                System.out.println(i + " File Channel: " + count);
//                // Flip de ByteBuffer zodat deze geleegd kan worden.
//                buf.flip();
//                // Schrijf naar het bestand stream toe.
//                fc.write(buf);
//                // En wel direct.
//                buf.clear();
//                i++;
//            }

            // Als alles klaar is, dan sluiten we het bestand stream.
//            fos.close();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } /*catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }*/

    }

}
