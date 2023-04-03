package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.handlers.ConnectionHandler;
import protocol.handlers.FileHandler;
import protocol.interfaces.ICommand;
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

public class Put implements ICommand {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final ConnectionHandler connection;
    public final Socket socket = null;
    public final BufferedReader in = null;
    public final PrintWriter out = null;

    public Put(Invoker invoker, Path homeDirectory, ConnectionHandler connection) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.connection = connection;
//        this.socket = socket;
//        this.in = in;
//        this.out = out;
    }

    public void handle(ArrayList<String> args) {
        if (args.isEmpty()) {
            System.out.println("No arguments found.");
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. correct usage: PUT <filename>" + Constants.Strings.END_OF_TEXT);
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
        out.println("PUT " + fileHeader + Constants.Strings.END_OF_TEXT + "\n");

        // Geef het commando door aan de server met de header erbij.

        // Wacht op bevestiging dat het goed is.
        String fromServer;
        try {
            while ((fromServer = in.readLine()) != null) {
                // If server contains 'Header received'
                if (fromServer.equals("200 HEADER RECEIVED")) {

                    try (ServerSocket fileTransferSocket = new ServerSocket(Integer.parseInt(Constants.Integers.DATA_PORT.toString()))) {
                        out.println(ResponseCode.SUCCESS + " OPEN " + Constants.Integers.DATA_PORT);

                        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                        new FileTransferThread(fileHeader, homeDirectory, fileTransferSocket.accept()).start();
                    }

                }

                if (fromServer.equals("200 FILE RECEIVED SUCCESSFUL")) {
                    out.println(ResponseCode.SUCCESS.getCode() + " File transfer complete." + Constants.Strings.END_OF_TEXT);
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
        if (!args.get(0).contains("FileHeader")) {
            out.println(ResponseCode.FAILURE + " First line is no file header. Exiting" + Constants.Strings.END_OF_TEXT);
            out.println(output());
            return;
        }

        // Zodra we een FileHeader antwoord hebben ontvangen
        String nextLine;
        String[] headerLines;
        FileHeader fileHeader = new FileHeader();


        headerLines = args.get(0).split(Constants.Strings.UNIT_SEPARATOR.toString());
        if(headerLines.length != 6){
            out.println(ResponseCode.FAILURE + " Missing header line(s). 6 expected, received: " + headerLines.length);
            out.println(output());
            return;
        }

        // Vul de header
        fileHeader.setFileName(headerLines[1].trim());
        fileHeader.setLastModified(headerLines[2].trim());
        fileHeader.setFileSize(Long.parseLong(headerLines[3].trim()));
        fileHeader.setHashAlgo(headerLines[4].trim());
        fileHeader.setCheckSum(headerLines[5].trim());

        out.println("200 HEADER RECEIVED");

        // Nog een loop voor het opzetten van de overdracht.
        while ((nextLine = in.readLine()) != null) {
            System.out.println("Client: " + nextLine);

            if (nextLine.contains(Constants.Strings.END_OF_TEXT.toString())) {
                out.println(output());
                break;
            }

            if (nextLine.contains("OPEN")) {
                String[] command = nextLine.split(" ");

                SocketAddress fileTransferSocketAddress = new InetSocketAddress(socket.getInetAddress().getHostName(), Integer.parseInt(command[2]));
                Socket fileTransferSocket = new Socket();

                // Bestand ontvangen via FileHandler.
                fileTransferSocket.connect(fileTransferSocketAddress);
                new FileHandler(fileTransferSocket, fileHeader, homeDirectory).receiveFile();

                // Bestand headers controleren of het bestand succesvol is overgebracht.
                FileHeader fileHeaderLocal = FileHandler.constructFileHeader(fileHeader.getFileName(), homeDirectory);
                if (fileHeaderLocal.compareCheckSum(fileHeader)) {
                    fileTransferSocket.close();
                    out.println(ResponseCode.SUCCESS.getCode() + " FILE RECEIVED SUCCESSFUL");
                } else {
                    out.println(ResponseCode.FAILURE.getCode() + "FILE CORRUPTED");
                    // TODO: FIX Hier moet de loop opnieuw beginnen zodra het bestand corrupted is.
                }
            }
        }
    }

    public String output() {
        return Constants.Strings.END_OF_TEXT.toString();
    }
}
