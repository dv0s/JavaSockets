package client;

import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.Protocol;
import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.utils.Tools;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Tools.startScreen();

        if(args.length != 2)
        {
            System.err.println("Usage: java client.Client <hostname> <port number>");
            System.exit(1);
        }

        System.out.println("File sync client started. v0.0.1");
        Path homeDirectory = Tools.initializeHomeDirectory(Constants.BASE_DIR + File.separator + "client");

        ConnectionHandler serverConnection = null;

        int attempts = 0;
        boolean connected = false;

        while (!connected) {


            try {
                // Gooi de argumenten door naar connection handler, en laat die het maar verder afhandelen.
                serverConnection = new ConnectionHandler(Invoker.CLIENT, homeDirectory).establish(args);
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

        Protocol protocol = new Protocol(homeDirectory);

        BufferedReader stdIn = new BufferedReader((new InputStreamReader(System.in)));
        String fromServer, fromUser = null;

        while((fromServer = serverConnection.in.readLine()) != null){
            System.out.println("Server: " + fromServer);

            // Close the connection.
            if(fromServer.contains(Constants.END_OF_TRANSMISSION.toString())){
                serverConnection.close();
                break;
            }

            // Response codes gebruiken als afgesproken in protocol.
            if(fromServer.startsWith("2")){
                protocol.processInput(Invoker.CLIENT, fromServer, serverConnection.in, serverConnection.out);
                // Commando uitvoeren
            }

            if(fromServer.startsWith("5")){
                protocol.processErrorHandling();
                // Error afhandeling;
            }

            // Zodra we een Fileheader antwoord hebben ontvangen
            if(fromServer.contains("Fileheader")){
                String nextLine;
                String[] header;
                FileHeader fileHeader = new FileHeader();

                // Lees de regels van server om de header op te stellen
                while((nextLine = serverConnection.in.readLine()) != null){
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
                serverConnection.out.println("OK");

                // Nog een loop voor het opzetten van de overdracht.
                while((nextLine = serverConnection.in.readLine()) != null){
                    if(nextLine.contains("OPEN")){
                        System.out.println("Probeer verbinding te maken.");
                        String[] command = nextLine.split(" ");
                        Path path = Paths.get(Constants.BASE_DIR + File.separator + "client");

                        SocketAddress fileTransferSocketAddress = new InetSocketAddress(serverConnection.socket.getInetAddress().getHostName(), Integer.parseInt(command[2]));
                        Socket fileTransferSocket = new Socket();

                        fileTransferSocket.connect(fileTransferSocketAddress);

                        BufferedInputStream fileTransferIn = new BufferedInputStream(fileTransferSocket.getInputStream());

                        Path file = FileSystems.getDefault().getPath(String.valueOf(path), fileHeader.getFileName());

                        int count;
                        byte[] buffer = new byte[16 * 1024];

                        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(file));

                        while ((count = fileTransferIn.read(buffer)) >= 0) {
                            fileOutputStream.write(buffer, 0, count);
                            fileOutputStream.flush();
                        }

                        fileOutputStream.close();
                    }
                }
            }

            // Als de server het signaal geeft dat het klaar is met praten
            if(fromServer.contains(Constants.END_OF_TEXT.toString())) {
                System.out.print("Command: ");

                fromUser = stdIn.readLine();

                if (fromUser != null) {
                    serverConnection.out.println(fromUser);
                }
            }
        }

    }
}
