package client;

import client.handlers.Connection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws IOException {

        if(args.length != 2)
        {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Connection connection = null;
        int attempts = 0;

        boolean connected = false;

        while (!connected) {
            try {
                connection = new Connection(hostName,portNumber).establish();
                connected = true;

            } catch (IOException ex) {
                try {
                    if (attempts < 10) {
                        attempts++;
                        System.out.println("Attempt " + attempts + " to connect.. please wait.");
                        Thread.sleep(2000);
                    } else {
                        System.err.println("Server doesn't seem te be up and running. Please try again later.");
                        System.exit(2);
                    }
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser;

        while((fromServer = connection.serverIn.readLine()) != null){
            System.out.println("Server: " + fromServer);

            if(fromServer.startsWith("END")){ // Moet later iets van \r\n zijn.
                connection.serverOut.println("Bye.");
                connection.close();
                break;
            }

            System.out.print("Command: ");
            // Onze blokkende command afwacht ding
            fromUser = stdIn.readLine();

            if (fromUser != null) {
                System.out.println("Client: " + fromUser);

                // Stuur de input door naar de Server.
                connection.serverOut.println(fromUser);
            }
        }

        //try(
                // Probeer een socket op te zetten naar de server die open zou moeten staan.
          //      Socket serverSocket = new Socket(hostName, portNumber)
        //){


           //FileOutputStream fos = new FileOutputStream("D:\\Avans\\Socket\\received\\big_ass_file.zip");

            // Open een gebufferde stream voor het te ontvangen bestand (Welke we overigens weg kunnen laten).
            //BufferedOutputStream out = new BufferedOutputStream(fos);

            // Bepaal een buffer.
          //  byte[] buffer = new byte[16 * 1024];
            // Count variable voor de loop straks.
//            int count;
  //          int i = 0;

            // Open de stream van de server waar de bytes van het bestand daalijk op binnen komen (Input krijgt van
            // de andere kant).
    //        InputStream in = serverSocket.getInputStream();

            // Hier wordt het interessant, we gaan op buffergrootte lussen zolang als dat er bytes binnen komen.
      //      System.out.println("Write to file");
//            while((count = in.read(buffer)) >= 0){
//                System.out.println(i + ": " + count);
//                // Schrijf naar het bestand stream toe.
//                fos.write(buffer, 0, count);
//                // En wel direct.
//                fos.flush();
//                i++;
//            }

            // Als alles klaar is, dan sluiten we het bestand stream.
        //    fos.close();

      //  }catch(UnknownHostException e){
      //      System.err.println("Don't know about host " + hostName);
      //      System.exit(1);
      //  }catch(IOException e){
      //      System.err.println("Couldn't get I/O for the connection to " + hostName);
      //      System.exit(1);
        //}

    }
}
