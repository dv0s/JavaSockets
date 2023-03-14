package protocol.commands;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.enums.Invoker;
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
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Get(Invoker invoker, Path homeDirectory, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.clientIn = in;
        this.clientOut = out;
    }

    @Override
    public void handle(ArrayList<String> args) {
        // Eerst wat checks
        if(args.isEmpty()){
            System.out.println("No arguments found.");
            clientOut.println("No arguments found. correct usage: GET <filename>" + Constants.END_OF_TEXT);
            return;
        }

        String fileName = args.get(0);
        Path path = Paths.get(homeDirectory.toString() + File.separator + fileName);

        if(Files.notExists(path)){
            System.out.println("Requested file '" + args.get(0) + "' not found.");
            clientOut.println("Requested file '" + args.get(0) + "' not found." + Constants.END_OF_TEXT);
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

        clientOut.println(fileHeader);

        String clientInput;
        try{
            while((clientInput = clientIn.readLine()) != null){
                if(clientInput.equals("OK")){
                    System.out.println("OK Sign received");
                    try (ServerSocket fileTransferSocket = new ServerSocket(42068)){
                        Path serverFilePath = Paths.get(Constants.BASE_DIR + File.separator + "server");
                        clientOut.println("OPEN PORT 42068");

                        new FileTransferThread(fileHeader, serverFilePath, fileTransferSocket.accept()).start();
                    }
                }
            }
        }catch (IOException e){
            System.err.println(e.getMessage());
        }

        // Hier moet een transferThread worden geopend die naar de client toe stuurt.
        // Eerst moeten we het bestand opzoeken die gevraagd wordt.

        clientOut.println(output());
    }

    @Override
    public String output() {
        String output;
//        if (this.params.isEmpty()) {
//            output =  "Command 'GET' called";
//        }

        output = "Command 'GET' called with parameters";

        return output + Constants.END_OF_TEXT;
    }

    public void receiveFile(){

    }

    public void sendFile(){}
}
