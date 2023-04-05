package nl.socketsoldiers.protocol.threads;

import nl.socketsoldiers.protocol.data.FileHeader;
import nl.socketsoldiers.protocol.enums.TransferDirection;
import nl.socketsoldiers.protocol.handlers.FileHandler;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

// This Thread will only be created after the header has been received through the communication channel.
public class FileTransferThread extends Thread {

    public FileHeader fileHeader;
    public Path homeDirectory;
    public Socket socket;


    public FileTransferThread(FileHeader fileHeader, Path homeDirectory, Socket socket) throws IOException {
        super();

        this.fileHeader = fileHeader;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
    }

    public void run() {
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

    }

}
