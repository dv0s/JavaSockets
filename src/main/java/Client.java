import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws IOException {

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
            // Open een stream voor het te ontvangen bestand waar we naartoe gaan schrijven (Output gaat naar
            // de andere kant toe).
            FileOutputStream fos = new FileOutputStream("D:\\Avans\\Socket\\received\\big_ass_file.zip");

            // Open een gebufferde stream voor het te ontvangen bestand (Welke we overigens weg kunnen laten).
            //BufferedOutputStream out = new BufferedOutputStream(fos);

            // Bepaal een buffer.
            byte[] buffer = new byte[16 * 1024];
            // Count variable voor de loop straks.
            int count;
            int i = 0;

            // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
            // de andere kant).
            InputStream in = serverSocket.getInputStream();

            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
            System.out.println("Write to file");
            while((count = in.read(buffer)) >= 0){
                System.out.println(i + ": " + count);
                // Schrijf naar het bestand stream toe.
                fos.write(buffer, 0, count);
                // En wel direct.
                fos.flush();
                i++;
            }

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
}
