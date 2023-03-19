package protocol.threads;

import protocol.data.FileHeader;
import protocol.handlers.FileHandler;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

// This Thread will only be created after the header has been received through the communication channel.
public class FileTransferThread extends Thread{

    public FileHeader fileHeader;
    public Path homeDirectory;
    public Socket socket;


    public FileTransferThread(FileHeader fileHeader, Path homeDirectory, Socket socket) throws IOException {
        super();

        this.fileHeader = fileHeader;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
    }

    public void run(){
        System.out.println("FileTransferThread has been started!");

        try {
            FileHandler fileHandler = new FileHandler(socket, fileHeader, homeDirectory);
            fileHandler.sendFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("FileTransferThread has transferred and should close now.");
        // Close the thread after transfer.
        Thread.currentThread().interrupt();

        // Check if the request is a GET or PUT
            // Prepare to receive a stream.
                // Check the directory where the file needs to go
                // Read the file header
                // If header is complete
                    // Send OK Sign
                // If not
                    // Send Incomplete message

            // Prepare to send a stream.
                // Check the directory where the file resides
                // Prepare the file header
                // Send over the header to the other side
            // When we get an OK sign for receiving the header, start sending the file
            // Then we listen for another OK sign that everything is has been received correctly

        // If it has been successful, send end of transmission
        // If not, restart te process
    }

}
