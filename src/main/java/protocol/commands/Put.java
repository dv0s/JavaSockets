package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.handlers.FileHandler;
import protocol.interfaces.CommandHandler;
import protocol.threads.FileTransferThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;

public class Put implements CommandHandler {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public Put(Invoker invoker, Path homeDirectory, Socket socket, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ArrayList<String> args) {
        if (args.isEmpty()) {
            System.out.println("No arguments found.");
            // TODO: FIX Misschien dat er voor de server ook een error commando mag komen zodat die het terug kan sturen naar de client
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. correct usage: PUT <filename>" + Constants.END_OF_TEXT);
            return;
        }

        // Work around om de juiste richting op te sturen is door de invoker te switchen tijdens de command call.
        if (invoker.equals(Invoker.CLIENT)) {
            try {
                handleClient(args);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try {
                handleServer(args);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        // Hier moet een transferThread worden geopend die van de client ontvangt.
//        out.println(output());
    }

    public void handleClient(ArrayList<String> args) throws IOException {
        // Stel een file header op voor het te verzenden bestand.
        FileHeader fileHeader = FileHandler.constructFileHeader(args.get(0), homeDirectory);
        out.println("PUT " + fileHeader);

        // Geef het commando door aan de server met de header erbij.

        // Wacht op bevestiging dat het goed is.
        String fromServer;
        try {
            while ((fromServer = in.readLine()) != null) {
                // If server contains 'Header received'
                if (fromServer.equals("200 HEADER RECEIVED")) {

                    try (ServerSocket fileTransferSocket = new ServerSocket(42068)) {
                        out.println("OPEN PORT 42068"); // TODO: FIX Using a fixed port for now.

                        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                        new FileTransferThread(fileHeader, homeDirectory, fileTransferSocket.accept()).start();
                    }

                }

                if (fromServer.equals("200 FILE RECEIVED SUCCESSFUL")) {
                    out.println(ResponseCode.SUCCESS.getCode() + " File transfer complete." + Constants.END_OF_TEXT);
//                    out.println(Constants.END_OF_TEXT); //TODO: FIX this will start the cycle again, but needs to be fixed.
                    break;
                }

                if (fromServer.startsWith("5")) {
                    System.err.println("Failure occurred");
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        //  Mocht het niet goed zijn, probeer dan opnieuw.
        // TODO: FIX Dit stukje uitwerken.
    }

    public void handleServer(ArrayList<String> args) throws IOException {
        if (!args.get(0).equals("Fileheader")) {
            out.println(ResponseCode.FAILURE + " First line is no file header. Exiting" + Constants.END_OF_TEXT);
        }

        // Zodra we een FileHeader antwoord hebben ontvangen
        String nextLine;
        String[] header;
        FileHeader fileHeader = new FileHeader();

        // Lees de regels van server om de header op te stellen
        while ((nextLine = in.readLine()) != null) {
            System.out.println("Client: " + nextLine);
            if (nextLine.equals("")) {
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

        out.println("200 HEADER RECEIVED");

        // Nog een loop voor het opzetten van de overdracht.
        while ((nextLine = in.readLine()) != null) {
            System.out.println("Client: " + nextLine);

            if (nextLine.contains(Constants.END_OF_TEXT.toString())) {
                out.println(output());
                break;
            }

            if (nextLine.contains("OPEN")) {
                String[] command = nextLine.split(" ");

                // TODO: FIX Dit moet worden opgezet via de connectionHandler
                SocketAddress fileTransferSocketAddress = new InetSocketAddress(socket.getInetAddress().getHostName(), Integer.parseInt(command[2]));
                Socket fileTransferSocket = new Socket();

                // Bestand ontvangen via FileHandler.
                fileTransferSocket.connect(fileTransferSocketAddress);
                new FileHandler(fileTransferSocket, fileHeader, homeDirectory).receiveFile();

                // Bestand headers controleren of het bestand succesvol is overgebracht.
                FileHeader fileHeaderLocal = FileHandler.constructFileHeader(fileHeader.getFileName(), homeDirectory);
                if (fileHeaderLocal.compare(fileHeader)) {
                    fileTransferSocket.close();
                    out.println(ResponseCode.SUCCESS.getCode() + " FILE RECEIVED SUCCESSFUL");
                } else {
                    out.println(ResponseCode.FAILURE.getCode() + "FILE CORRUPTED");
                    // TODO: FIX Hier moet de loop opnieuw beginnen zodra het bestand corrupted is.
                }
            }
        }
    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}
