import java.io.*;
import java.net.Socket;

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
        try(
                // Output stream naar client toe.
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true);
                // Input stream van client.
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ){

            //Variables voor het kunnen lezen en schrijven naar client toe.
            String inputLine, outputLine;

            //Hier moet toch iets van een protocol komen.
            while(true){
                System.out.println("Client "+ conn_count +" connectie is actief voor: " + System.nanoTime());
                Thread.sleep(100);
                boolean done = TransferFile();
                if(done){
                    break;
                }
            }

            System.out.println("File has been transferred!");


        }catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // De code van Server naar ServerThread verplaatst en in een methode gezet.
    public boolean TransferFile() throws IOException {

        // Open een oneindige lus voor het versturen van het bestand zodra er een client verbinding heeft gemaakt.
        while (true) {
            System.out.println("Client Accepted");

            // Pak het bestand die je wilt versturen.
            File myFile = new File("D:\\Avans\\Socket\\send\\BIGASSFILE.zip");

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
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));

            // Hier wordt het interessant, we gaan lussen zolang dat wat we krijgen van het bestand niet groter
            // of gelijk is aan 0.
            while((count = in.read(buffer)) >= 0){
                System.out.println(i + ": " + count);
                // Schrijf het buffer stukje naar de client stream.
                out.write(buffer, 0, count);
                // En wel direct
                out.flush();
                i++;
            }

            // Als we klaar zijn, sluiten we de connectie met de client.
            socket.close();

            // Breek uit de lus.
            break;
        }

        return true;
    }
}
