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
        //Variables voor het kunnen lezen en schrijven naar client toe.
        String inputLine, outputLine;

        // Pak het bestand die je wilt versturen.
        Path myFile = FileSystems.getDefault().getPath(dir + File.separator + "send", fileName);

        // Probeer de transferFile methode uit te voeren. Daarna is dit proces afgerond.
        try {
            transferFile(dir, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    // De code van Server naar ServerThread verplaatst en in een methode gezet.
    // TODO: 11/06/2022 Geef TransferFile variable voor het versturen van het bestand
    public void transferFile(String dir, String file) throws IOException, NoSuchAlgorithmException {
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
        while ((count = in.read(buffer)) >= 0) {
            // Schrijf het buffer stukje naar de client stream.
            out.write(buffer, 0, count);
            // En wel direct
            out.flush();
        }

        // Als we klaar zijn, sluiten we de connectie met de client.
        in.close();
        socket.close();
    }

    public boolean receiveFile() throws IOException, NoSuchAlgorithmException {
        return false;
    }
}
