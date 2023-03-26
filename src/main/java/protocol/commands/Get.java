package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.handlers.FileHandler;
import protocol.interfaces.CommandHandler;
import protocol.returnobjects.Response;
import protocol.threads.FileTransferThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Get implements CommandHandler {
    public final Invoker invoker;
    public final Path homeDirectory;
    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public Get(Invoker invoker, Path homeDirectory, Socket socket, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ArrayList<String> args) {
        // Eerst wat checks
        if (args.isEmpty()) {
            // TODO: FIX Misschien dat er voor de server ook een error commando mag komen zodat die het terug kan sturen naar de client
            System.out.println("No arguments found.");
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. correct usage: GET <filename>" + Constants.END_OF_TEXT);
            return;
        }

        if (args.size() > 1) {
            // TODO: FIX Misschien dat er voor de server ook een error commando mag komen zodat die het terug kan sturen naar de client
            System.out.println("Too many arguments found.");
            out.println(ResponseCode.ERROR.getCode() + " Too many arguments found. Expected one argument. correct usage: GET <filename>" + Constants.END_OF_TEXT);
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

    }

    public void handleClient(ArrayList<String> args) throws IOException {
        String fromServer;

        // Should output "GET <filename>"
        out.println(Command.GET + " " + args.get(0));

        // Zodra we een FileHeader antwoord hebben ontvangen
        if ((fromServer = in.readLine()) != null) {
            if (fromServer.contains("FileHeader")) {
                String nextLine;
                String[] headerLines;
                FileHeader fileHeader = new FileHeader();

                headerLines = fromServer.split(Constants.UNIT_SEPARATOR.toString());
                if(headerLines.length != 6){
                    out.println(ResponseCode.FAILURE + " Missing header line(s). 6 expected, received: " + headerLines.length);
                    return;

                }

                // Vul de header
                fileHeader.setFileName(headerLines[1].trim());
                fileHeader.setLastModified(headerLines[2].trim());
                fileHeader.setFileSize(Long.parseLong(headerLines[3].trim()));
                fileHeader.setHashAlgo(headerLines[4].trim());
                fileHeader.setCheckSum(headerLines[5].trim());

                out.println("200 FILEHEADER RECEIVED");

                // Nog een loop voor het opzetten van de overdracht.
                while ((nextLine = in.readLine()) != null) {
                    System.out.println("Server: " + nextLine);

                    if (nextLine.contains(Constants.END_OF_TEXT.toString())) {
                        break;
                    }

                    if (nextLine.contains("OPEN")) {
                        String[] command = nextLine.split(" ");

                        // TODO: FIX Dit moet worden opgezet via de connectionHandler
                        SocketAddress fileTransferSocketAddress = new InetSocketAddress(socket.getInetAddress().getHostName(), Integer.parseInt(command[1]));
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
        }
    }

    public void handleServer(ArrayList<String> args) throws IOException {
        if (args.isEmpty()) {
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. Don't know what to do");
        }

        String fileName = args.get(0);

        // Server sends the file to client.
        Path path = Paths.get(homeDirectory + File.separator + fileName);

        // Eerst moeten we het bestand opzoeken die gevraagd wordt.
        if (Files.notExists(path)) {
            out.println(ResponseCode.FAILURE.getCode() + " Requested file '" + fileName + "' not found.");
            return;
        }

        FileHeader fileHeader = FileHandler.constructFileHeader(fileName, homeDirectory);

        // Nadat er wat werk klaar is gezet, geef dan responseCode
        out.println(ResponseCode.SUCCESS.getCode() + " " + fileHeader + Constants.END_OF_TEXT + "\n");

        String input;
        try {
            while ((input = in.readLine()) != null) {
                if (input.equals("200 FILEHEADER RECEIVED")) { // TODO: FIX Don't trust magic strings.

                    try (ServerSocket fileTransferSocket = new ServerSocket(42068)) {
                        out.println("OPEN 42068"); // TODO: FIX Using a fixed port for now.

                        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                        new FileTransferThread(fileHeader, homeDirectory, fileTransferSocket.accept()).start();
                    }
                }

                if (input.equals("200 FILE RECEIVED SUCCESSFUL")) {
                    out.println(ResponseCode.SUCCESS.getCode() + " File transfer complete." + output());
                    break;
                }

                if (input.startsWith("5")) {
                    System.err.println("Failure occurred");
                }

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        out.println(output());
    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}
