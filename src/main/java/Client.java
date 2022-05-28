import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;

public class Client {

    public static void main(String[] args) throws IOException {

        // Deze bekijken op een Windows Machine.
        String dir = String.valueOf(new StringBuilder().append(System.getProperty("user.home"))
                .append(File.separator).append("documents")
                .append(File.separator).append("avans")
                .append(File.separator).append("filesync"));

        System.out.println("User home dir is: " + dir);

        // Bekijk alles in het mapje. Dit kan weer handig zijn voor het uitlezen en bepalen welke bestanden er
        // gesynct kan worden.
        ScanDir(dir);

        // Check argumenten voor het starten van de applicatie, in dit geval hostname en poortnummer
        if(args.length != 2)
        {
            System.err.println("Usage: java Server <hostname> <port number>");
            System.exit(1);
        }

        // Maak van de argumenten variablen waarmee je kan werken.
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try(
                // Probeer een socket op te zetten naar de server die open zou moeten staan.
                Socket serverSocket = new Socket(hostName, portNumber);
        ){
            // Maak een nieuw bestand object aan. Dit doen we omdat we dan meer gegevens uit kunnen lezen van wat
            // we precies gaan versturen. Dit wordt gedaan via de java.nio.files package.
            Path path = FileSystems.getDefault().getPath(dir + File.separator + "catch", "big_ass_file2.zip");

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
            InputStream in = serverSocket.getInputStream();

            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
            System.out.println("Write first file");
            while((count = in.read(buffer)) >= 0){
                System.out.println(i + ": " + count);
                // Schrijf naar het bestand stream toe.
                fos.write(buffer, 0, count);
                // En wel direct.
                fos.flush();
                i++;
            }

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
            fos.close();

        }catch(UnknownHostException e){
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }catch(IOException e){
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

    }



    static void ScanDir(String dir)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(System.out::println);
        } catch (AccessDeniedException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void CheckRootDir(String dir){
        // For more info, look here : https://www.baeldung.com/java-nio2-file-attribute
//        Path path = Paths.get(dir);
//        if(!Files.exists(path))
//        {
//            try(
//                    FileAttribute attr = new FileAttribute {
//                        "Owner", "DvOs"
//            }
//                Files.createDirectories(Paths.get(dir));
//            ){
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}
