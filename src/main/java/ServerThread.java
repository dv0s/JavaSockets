import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                Thread.sleep(10000);
            }

        }catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
