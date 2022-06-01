import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerThread extends Thread{
    // Private socket voor de thread.
    private Socket socket;
    private int conn_count;

    // Constructor voor de server thread.
    public ServerThread(Socket socket, int conn_count){
        super();
        this.socket = socket;
        this.conn_count = conn_count;
    }

    public void run()
    {
        // Deze bekijken op een Windows Machine.
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

            // # Hier moet toch iets van een protocol komen.
            // Zolang als dat er een verbinding is
            while(true){
                // print welke client nummer actief is.
                System.out.println("Client "+ conn_count +" connectie is actief voor: " + System.nanoTime());
                // Zet een timer voor 100ms.
                Thread.sleep(100);
                // doe een file transfer.
                boolean done = TransferFile();
                // Klaar? Breek dan uit de lus.
                if(done){
                    break;
                }
            }

            System.out.println("File has been transferred!");


        }catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // De code van Server naar ServerThread verplaatst en in een methode gezet.
    public boolean TransferFile() throws IOException, NoSuchAlgorithmException {

        // Deze bekijken op een Windows Machine.
        String dir = String.valueOf(System.getProperty("user.home") +
                File.separator + "documents" +
                File.separator + "avans" +
                File.separator + "filesync");

        // Open een oneindige lus voor het versturen van het bestand zodra er een client verbinding heeft gemaakt.
        while (true) {
            System.out.println("Client Accepted");

            // Pak het bestand die je wilt versturen.
            Path myFile = FileSystems.getDefault().getPath(dir + File.separator + "send", "avatar.png");

            //## BEGIN CHECKSUM GEDEELTE https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
            // Bepaal het algoritme voor het hashen.
            MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
            // Genereer de checksum.
            String checksum = Tools.getFileChecksum(md5Digest, myFile.toFile());
            // Print de checksum uit.
            System.out.println("SHA-256 server checksum: " + checksum);
            //## EINDE CHECKSUM GEDEELTE

            // Maak een teller voor straks
            int count;
            // Definieer een buffer straks
            byte[] buffer = new byte[16 * 1024];

            int i = 0;

            // Hiermee kan de grootte van het bestand worden bepaald (Hebben we voor nu niet nodig).
//                byte[] myByteArray = new byte[(int) myFile.length()];

            // Zet een stream op waar we naartoe kunnen schrijven (Output gaat naar de andere kant toe).
            OutputStream out = socket.getOutputStream();

            // Lees het bestand uit in een gebufferde stream (Input krijgt van de andere kant, in dit geval van het
            // bestand wat eerder aangemaakt is).
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(String.valueOf(myFile)));

            // Hier wordt het interessant, we gaan lussen zolang dat wat we krijgen van het bestand niet groter
            // of gelijk is aan 0.
            while((count = in.read(buffer)) >= 0){
                // Schrijf het buffer stukje naar de client stream.
                out.write(buffer, 0, count);
                // En wel direct
                out.flush();
            }

            // Als we klaar zijn, sluiten we de connectie met de client.
            socket.close();

            // Breek uit de lus.
            break;
        }

        return true;
    }
}
