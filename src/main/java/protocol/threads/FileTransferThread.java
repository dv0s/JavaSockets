package protocol.threads;

import protocol.data.FileHeader;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.utils.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// This Thread will only be created after the header has been received through the communication channel.
public class FileTransferThread extends Thread{
    public Command command;
    public FileHeader fileHeader;

    public Socket socket = null;
    public PrintWriter socketOut = null;
    public BufferedReader socketIn = null;

    public FileTransferThread(Command command, FileHeader fileHeader){
        super();

        this.command = command;
        this.fileHeader = fileHeader;
    }

    public void run(){
        // Check if the request is a GET or PUT
        if(command == Command.GET){
            // Prepare to receive a stream.
                // Check the directory where the file needs to go
                // Read the file header
                // If header is complete
                    // Send OK Sign
                // If not
                    // Send Incomplete message

                //
        }else if(command == Command.PUT){
            // Prepare to send a stream.
                // Check the directory where the file resides
                // Prepare the file header
                // Send over the header to the other side
            // When we get an OK sign for receiving the header, start sending the file
            // Then we listen for another OK sign that everything is has been received correctly
        }

        // If it has been successful, send end of transmission
        // If not, restart te process
    }

    public void checkFile(File file){
    }

    public int compareHeaders(FileHeader fileHeader, File file) throws NoSuchAlgorithmException, IOException {
        FileHeader localFileHeader = constructHeader(file);
        return fileHeader.compareTo(localFileHeader);
    }

    public FileHeader constructHeader(File file) throws NoSuchAlgorithmException, IOException {
        FileHeader fileHeader = new FileHeader();

        fileHeader.fileName = file.getName();
        fileHeader.fileType = "TYPE";
        fileHeader.hashAlgo = Constants.HASHING_ALGORITHM.toString();

        MessageDigest md5Digest = MessageDigest.getInstance(Constants.HASHING_ALGORITHM.toString());

        fileHeader.fileSize = Tools.getFileChecksum(md5Digest, file);

        return fileHeader;
    }

    // This will be the buffered sending
    public void startTransfer(){

    }

}
