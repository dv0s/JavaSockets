package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.*;
import protocol.handlers.ConnectionHandler;
import protocol.handlers.FileHandler;
import protocol.interfaces.ICommand;
import protocol.threads.FileTransferThread;
import protocol.utils.ConnectionSockets;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Get {
    public final Invoker invoker;
    public final Path homeDirectory;
    public final ConnectionSockets connectionSockets;

    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public Get(Invoker invoker, Path homeDirectory, ConnectionSockets connectionSockets) throws IOException {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.connectionSockets = connectionSockets;

        this.socket = connectionSockets.commSocket;
        this.in = new BufferedReader(new InputStreamReader(connectionSockets.commSocket.getInputStream()));
        this.out = new PrintWriter(connectionSockets.commSocket.getOutputStream(), true);
    }

    public void handle(ArrayList<String> args) {
        // Eerst wat checks
        if (args.isEmpty()) {
            System.out.println("No arguments found.");
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. correct usage: GET <filename>" + Constants.Strings.END_OF_TEXT);
            return;
        }

        if (args.size() > 1) {
            System.out.println("Too many arguments found.");
            out.println(ResponseCode.ERROR.getCode() + " Too many arguments found. Expected one argument. correct usage: GET <filename>" + Constants.Strings.END_OF_TEXT);
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
        out.println(Command.GET + " " + args.get(0) + Constants.Strings.END_OF_TEXT);

        // Zodra we een FileHeader antwoord hebben ontvangen
        if ((fromServer = in.readLine()) != null) {
            if (fromServer.startsWith(ResponseCode.FAILURE.toString())) {
                System.err.println(fromServer);
                return;
            }

            if (fromServer.contains("FileHeader")) {
                String nextLine;
                String[] headerLines;
                FileHeader fileHeader = new FileHeader();

                headerLines = fromServer.split(Constants.Strings.UNIT_SEPARATOR.toString());
                if (headerLines.length != 6) {
                    out.println(ResponseCode.FAILURE + " Missing header line(s). 6 expected, received: " + headerLines.length);
                    return;

                }

                // Vul de header
                fileHeader.setFileName(headerLines[1].trim());
                fileHeader.setLastModified(headerLines[2].trim());
                fileHeader.setFileSize(Long.parseLong(headerLines[3].trim()));
                fileHeader.setHashAlgo(headerLines[4].trim());
                fileHeader.setCheckSum(headerLines[5].trim());

                out.println(ResponseCode.SUCCESS + " FILEHEADER RECEIVED");

                // Nog een loop voor het opzetten van de overdracht.
                while ((nextLine = in.readLine()) != null) {
                    System.out.println("Server: " + nextLine);

                    if (nextLine.contains(Constants.Strings.END_OF_TEXT.toString())) {
                        break;
                    }

                    if (nextLine.contains("OPEN")) {
                        String[] command = nextLine.split(" ");

                        new FileHandler(connectionSockets.dataSocket, fileHeader, homeDirectory).receiveFile();

                        // Bestand headers controleren of het bestand succesvol is overgebracht.
                        FileHeader fileHeaderLocal = FileHandler.constructFileHeader(fileHeader.getFileName(), homeDirectory);
                        if (fileHeaderLocal.compareCheckSum(fileHeader)) {
                            out.println(ResponseCode.SUCCESS.getCode() + " FILE RECEIVED SUCCESSFUL");
                        } else {
                            out.println(ResponseCode.FAILURE.getCode() + " FILE CORRUPTED");
                            // TODO: FIX Hier moet de loop opnieuw beginnen zodra het bestand corrupted is.
                        }
                    }
                }
            }
        }
    }

    public void handleServer(ArrayList<String> args) throws IOException {
        if (args.isEmpty()) {
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. Don't know what to do.");
            out.println(output());
            return;
        }

        String fileName = args.get(0).replace(Constants.Strings.END_OF_TEXT.toString(), "");

        // Server sends the file to client.
        Path path = Paths.get(homeDirectory + File.separator + fileName);

        // Eerst moeten we het bestand opzoeken die gevraagd wordt.
        if (Files.notExists(path)) {
            out.println(ResponseCode.FAILURE.getCode() + " Requested file '" + fileName + "' not found.");
            out.println(output());
            return;
        }

        FileHeader fileHeader = FileHandler.constructFileHeader(fileName, homeDirectory);

        // Nadat er wat werk klaar is gezet, geef dan responseCode
        out.println(ResponseCode.SUCCESS.getCode() + " " + fileHeader + Constants.Strings.END_OF_TEXT + "\n");

        String input;
        try {
            while ((input = in.readLine()) != null) {
                if (input.equals(ResponseCode.SUCCESS + " FILEHEADER RECEIVED")) {

                    out.println(ResponseCode.SUCCESS + " OPEN " + Constants.Integers.DATA_PORT);

                    // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                    new FileTransferThread(fileHeader, homeDirectory, connectionSockets.dataSocket).start();
                }

                if (input.equals(ResponseCode.SUCCESS + " FILE RECEIVED SUCCESSFUL")) {
                    out.println(ResponseCode.SUCCESS.getCode() + " File transfer complete." + output());
                    break;
                }

                if (input.startsWith(ResponseCode.FAILURE.toString())) {
                    System.err.println("Failure occurred while getting the file.");
                }

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        out.println(output());
    }

    public String output() {
        return Constants.Strings.END_OF_TEXT.toString();
    }
}
