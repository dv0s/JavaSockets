package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.threads.FileTransferThread;
import protocol.utils.Tools;
import protocol.interfaces.CommandHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

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
        if(args.isEmpty()){
            System.out.println("No arguments found.");
            out.println(ResponseCode.FAILURE.getCode() + " No arguments found. correct usage: GET <filename>" + Constants.END_OF_TEXT);
            return;
        }

        // Work around om de juiste richting op te sturen is door de invoker te switchen tijdens de command call.
        if(invoker.equals(Invoker.CLIENT)){
            try {
                handleClient(args);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }else{
            handleServer(args);
        }

    }

    @Override
    public String output() {
        String output = "Command 'GET' called with parameters";
        return output + Constants.END_OF_TEXT;
    }

    public void handleClient(ArrayList<String> args) throws IOException {
        System.out.println("Handle Client method called. passed args:");
        args.forEach(System.out::println);
        String fromServer;

        // Zodra we een Fileheader antwoord hebben ontvangen
        if((fromServer = in.readLine()) != null){ // TODO: FIX Err... this won't end well.
            if(fromServer.contains("Fileheader")){
                String nextLine;
                String[] header;
                FileHeader fileHeader = new FileHeader();

                // Lees de regels van server om de header op te stellen
                while((nextLine = in.readLine()) != null){
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
                out.println("200 FILEHEADER RECEIVED");

                // Nog een loop voor het opzetten van de overdracht.
                while((nextLine = in.readLine()) != null){
                    System.out.println("Server: " + nextLine);

                    if(nextLine.contains(Constants.END_OF_TEXT.toString())){
                        break;
                    }

                    if(nextLine.contains("OPEN")){
                        String[] command = nextLine.split(" ");

                        SocketAddress fileTransferSocketAddress = new InetSocketAddress(socket.getInetAddress().getHostName(), Integer.parseInt(command[2]));
                        Socket fileTransferSocket = new Socket();

                        fileTransferSocket.connect(fileTransferSocketAddress);

                        // After connecting to the file transfer socket, call on the file header to read the input
                        //  and write it to the file
                        BufferedInputStream fileTransferIn = new BufferedInputStream(fileTransferSocket.getInputStream());

                        Path file = FileSystems.getDefault().getPath(homeDirectory.toString(), fileHeader.getFileName());

                        // TODO: FIX This code needs to be handled by the file handler.
                        int count;
                        byte[] buffer = new byte[16 * 1024];

                        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(file));

                        while ((count = fileTransferIn.read(buffer)) >= 0) {
                            fileOutputStream.write(buffer, 0, count);
                            fileOutputStream.flush();
                        }

                        fileOutputStream.close();

                        FileHeader fileHeaderLocal = constructFileHeader(fileHeader.getFileName());
                        if(fileHeaderLocal.compare(fileHeader)){
                            fileTransferSocket.close();
                            out.println(ResponseCode.SUCCESS.getCode() + " FILE RECEIVED SUCCESSFUL");
                        }else{
                            out.println(ResponseCode.FAILURE.getCode() + "FILE CORRUPTED");
                            // TODO: FIX Hier moet de loop opnieuw beginnen zodra het bestand corrupted is.
                        }
                    }
                }
            }
        }
    }

    public void handleServer(ArrayList<String> args){
        if(args.isEmpty()){
            out.println(ResponseCode.ERROR.getCode() + " No arguments found. Don't know what to do" + Constants.END_OF_TEXT);
        }

        String fileName = args.get(0);
        FileHeader fileHeader = constructFileHeader(args.get(0));

        // Nadat er wat werk klaar is gezet, geef dan responseCode
        out.println(ResponseCode.SUCCESS.getCode() + " " + fileHeader);

        // Server sends the file to client.
        sendFile(fileHeader);
    }

    public void receiveFile(FileHeader fileHeader){

    }

    // TODO: FIX This should also be placed in file handler
    public FileHeader constructFileHeader(String fileName){
        Path path = Paths.get(homeDirectory.toString() + File.separator + fileName);

        // Bestand klaar maken voor overdracht
        File file;
        MessageDigest md5Digest;
        String checkSum;
        Path sendFile;
        FileHeader fileHeader = new FileHeader();
        try{
            file = new File(path.toString());
            md5Digest = MessageDigest.getInstance(Constants.HASHING_ALGORITHM.toString());
            checkSum = Tools.getFileChecksum(md5Digest, file);
            sendFile = Paths.get(homeDirectory.toString(), file.getName());

            // Fill the file header
            fileHeader.setFileName(sendFile.getFileName().toString());
            fileHeader.setFileType(Tools.getExtensionByStringHandling(sendFile.getFileName().toString()).toString());
            fileHeader.setFileSize(Files.size(sendFile));
            fileHeader.setHashAlgo(Constants.HASHING_ALGORITHM.toString());
            fileHeader.setCheckSum(checkSum);

        }catch (NullPointerException e){
            System.err.print(e.getMessage());
        }catch(NoSuchAlgorithmException | IOException e){
            System.err.println(e.getMessage());
        }

        return fileHeader;
    }

    public void sendFile(FileHeader fileHeader){
        Path path = Paths.get(homeDirectory.toString() + File.separator + fileHeader.getFileName());

        // Eerst moeten we het bestand opzoeken die gevraagd wordt.
        if(Files.notExists(path)){
            out.println(ResponseCode.FAILURE.getCode() + " Requested file '" + fileHeader.getFileName() + "' not found." + Constants.END_OF_TEXT);
            return;
        }

        String input;
        try{
            while((input = in.readLine()) != null){
                if(input.equals("200 FILEHEADER RECEIVED")){ // TODO: FIX Don't trust magic strings.

                    try (ServerSocket fileTransferSocket = new ServerSocket(42068)){
                        out.println("OPEN PORT 42068"); // TODO: FIX Fixed port for now.

                        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                        new FileTransferThread(fileHeader, homeDirectory, fileTransferSocket.accept()).start();
                    }
                }

                if(input.equals("200 FILE RECEIVED SUCCESSFUL")){
                    out.println(ResponseCode.SUCCESS.getCode() + " File transfer complete." + Constants.END_OF_TEXT);
                    out.println(Constants.END_OF_TEXT); //TODO: FIX this will start the cycle again, but needs to be fixed.
                    break;
                }

                if(input.startsWith("5")){
                    System.err.println("Failure occurred");
                }

            }

        }catch (IOException e){
            System.err.println(e.getMessage());
        }

//        out.println(output());
    }
}
