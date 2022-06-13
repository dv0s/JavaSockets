import Enums.SocketMode;
import Models.FileHeader;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

public class TransferThread extends Thread {
    private final SocketMode mode;
    private final Socket socket;
    private final File file;

    public TransferThread(SocketMode mode, Socket socket, File file) {
        super();
        this.mode = mode;
        this.socket = socket;
        this.file = file;
   }

    @Override
    public void run() {
        // Bepaal het pad waar vandaan verzonden wordt.
        String dir = String.valueOf(System.getProperty("user.home") +
                File.separator + "documents" +
                File.separator + "avans" +
                File.separator + "filesync");

        // Bepaal de afbeelding.
        String fileName = "avatar.png";

        try (
                // Output stream naar client toe.
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                // Input stream van client.
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            //Variables voor het kunnen lezen en schrijven naar client toe.
            String inputLine, outputLine;

            // Pak het bestand die je wilt versturen.
            Path myFile = FileSystems.getDefault().getPath(dir + File.separator + "send", fileName);

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
            System.out.println("Transfer sent to client: " + fh);
            clientOut.println(fh);

            while((inputLine = clientIn.readLine()) != null){
                System.out.println("Client: " + inputLine);

                if(inputLine.equals("HEADER_RECEIVED")){
                    System.out.println("Transfer sent to client: PREPARE_FOR_TRANSFER");
                    clientOut.println("PREPARE_FOR_TRANSFER");
                }

                if(inputLine.equals("READY_FOR_TRANSFER")){
                    System.out.println("Transfer sent to client: GOING TO SEND THE FILE");
                    transferFile(dir, fileName);

                    System.out.println("File sent to client");
                    clientOut.println("FILE_SEND_COMPLETE");
                }

                if(inputLine.equals("RECEIVED_FILE_CORRUPTED")){
                    while (true) {
                        // doe een file transfer.
                        boolean done = transferFile(dir, fileName);
                        // Klaar? Breek dan uit de lus.
                        if (done) {
                            break;
                        }
                    }
                    System.out.println("TransferThread: Bytes are sent");
                    clientOut.println("FILE_SEND_COMPLETE");
                    System.out.println("TransferThread: FILE_SEND_COMPLETE sent to client");

                }

                if(inputLine.equals("RECEIVED_FILE_VALID")){
                    System.out.println("Transfer sent to client: SHUTTING DOWN");

                    clientOut.println("SHUTTING_DOWN");
                    clientIn.close();

                    break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // De code van Server naar ServerThread verplaatst en in een methode gezet.
    // TODO: 11/06/2022 Geef TransferFile variable voor het versturen van het bestand
    public boolean transferFile(String dir, String file) throws IOException, NoSuchAlgorithmException {
        // Pak het bestand die je wilt versturen.
        Path myFile = FileSystems.getDefault().getPath(dir + File.separator + "send", file);

        // Maak een teller voor straks
        int count;
        // Definieer een buffer straks
        byte[] buffer = new byte[16 * 1024];

        // Hiermee kan de grootte van het bestand worden bepaald (Hebben we voor nu niet nodig).
        byte[] myByteArray = new byte[(int) Files.size(myFile)];

        // Zet een stream op waar we naartoe kunnen schrijven (Output gaat naar de andere kant toe).
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        // Lees het bestand uit in een gebufferde stream (Input krijgt van de andere kant, in dit geval van het
        // bestand wat eerder aangemaakt is).
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(String.valueOf(myFile)));

        // Hier wordt het interessant, we gaan lussen zolang dat wat we krijgen van het bestand niet groter
        // of gelijk is aan 0.
        System.out.println("TransferThread.transferFile: Getting ready to send");
        while ((count = in.read(buffer)) >= 0) {
            // Schrijf het buffer stukje naar de client stream.
            out.write(buffer, 0, count);
            // En wel direct
            out.flush();
        }

        System.out.println("TransferThread.transferFile: Bytes are sent. Closing File input stream");

        // Als we klaar zijn, sluiten we de connectie met de client.
//        in.close();
//        socket.close();

        return true;
    }

    public boolean receiveFile() throws IOException, NoSuchAlgorithmException {
        return false;
    }
}
