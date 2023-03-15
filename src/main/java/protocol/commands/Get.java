package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.enums.ResponseCode;
import protocol.threads.FileTransferThread;
import protocol.utils.Tools;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Get implements CommandHandler {
    public final Invoker invoker;
    public final Path homeDirectory;
    public final BufferedReader in;
    public final PrintWriter out;

    public Get(Invoker invoker, Path homeDirectory, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
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
            handleClient(args);
        }else{
            handleServer(args);
        }

    }

    @Override
    public String output() {
        String output = "Command 'GET' called with parameters";
        return output + Constants.END_OF_TEXT;
    }

    public void handleClient(ArrayList<String> args){
        System.out.println("Handle Client method called. passed args:");
        args.forEach(System.out::println);
//        for (String arg : args) {
//            System.out.println(arg);
//        }
    }

    public void handleServer(ArrayList<String> args){
        out.println(ResponseCode.SUCCESS.getCode() + " Ready to send some shit here." + Constants.END_OF_TEXT);


        FileHeader fileHeader = constructFileHeader(args[0]);

        // Server sends the file to client.
        sendFile(args);
    }

    public void receiveFile(FileHeader fileHeader){

    }

    public FileHeader constructFileHeader(String fileName){

    }

    public void sendFile(ArrayList<String> args){
        String fileName = args.get(0);
        Path path = Paths.get(homeDirectory.toString() + File.separator + fileName);

        // Eerst moeten we het bestand opzoeken die gevraagd wordt.
        if(Files.notExists(path)){
            out.println(ResponseCode.FAILURE.getCode() + " Requested file '" + args.get(0) + "' not found." + Constants.END_OF_TEXT);
            return;
        }

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
            sendFile = Paths.get(Constants.BASE_DIR + File.separator + "server", file.getName());

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

        // Nadat er wat werk klaar is gezet, geef dan responseCode
        out.println(ResponseCode.SUCCESS.getCode() + " " + fileHeader);

        String input;
        try{
            while((input = in.readLine()) != null){
                if(input.equals("OK")){
                    System.out.println("OK Sign received");

                    try (ServerSocket fileTransferSocket = new ServerSocket(42068)){
                        Path serverFilePath = Paths.get(Constants.BASE_DIR + File.separator + "server");
                        out.println("OPEN PORT DATA 42068");

                        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
                        new FileTransferThread(fileHeader, serverFilePath, fileTransferSocket.accept()).start();
                    }

                }
            }

        }catch (IOException e){
            System.err.println(e.getMessage());
        }

        out.println(output());
    }
}
