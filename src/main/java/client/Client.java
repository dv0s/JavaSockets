package client;

import client.handlers.Connection;
import protocol.Protocol;
import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.utils.Tools;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Tools.startScreen();

        if(args.length != 2)
        {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        System.out.println("File sync client started. v0.0.1");

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

            if(fromServer.contains(Constants.END_OF_TRANSMISSION.toString())){
                connection.close();
                break;
            }

            // Er moet een manier worden gevonden om te weten wanneer de client mag praten.
            if(fromServer.contains(Constants.END_OF_TEXT.toString())) {
                System.out.print("Command: ");

                fromUser = stdIn.readLine();

                if (fromUser != null) {
                    // Stuur de input door naar de Server.
                    connection.serverOut.println(fromUser);
                }
            }

            // TODO: Hier moet ook nog een lijst komen voor de Client om de commando's te verwerken.

            // Zodra we een Fileheader antwoord hebben ontvangen
            if(fromServer.contains("Fileheader")){
                String nextLine;
                String[] header;
                FileHeader fileHeader = new FileHeader();

                // Lees de regels van server om de header op te stellen
                while((nextLine = connection.serverIn.readLine()) != null){
                    System.out.println("Server: " + nextLine);
                    if(nextLine.equals("")){
                        break;
                    }

                    header = nextLine.split(":");

                    switch (header[0].trim()) {
                        case "Filename" -> fileHeader.setFileName(header[1].trim());
                        case "Filetype" -> fileHeader.setFileType(header[1].trim());
                        case "Filesize" -> fileHeader.setFileSize(Long.parseLong(header[1].trim()));
                        case "HashAlgo" -> fileHeader.setHashAlgo(header[1].trim());
                        case "CheckSum" -> fileHeader.setCheckSum(header[1].trim());
                    }

                }

                // TODO: 12/03/2023 Error handling voordat de bestandsoverdracht begint.

                System.out.print("File header received: \n" + fileHeader);
                connection.serverOut.println("OK");

                // Nog een loop voor het opzetten van de overdracht.
                while((nextLine = connection.serverIn.readLine()) != null){
                    if(nextLine.contains("OPEN")){
                        System.out.println("Probeer verbinding te maken.");
                    }
                }

            }
        }

    }
}
