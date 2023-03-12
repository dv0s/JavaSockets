package protocol.threads;

import protocol.data.FileHeader;
import protocol.enums.Constants;
import protocol.utils.Tools;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// This Thread will only be created after the header has been received through the communication channel.
public class FileTransferThread extends Thread{
    public FileHeader fileHeader;
    public Path path;

    public Socket socket;
    public BufferedOutputStream socketOut = null;
    public BufferedInputStream socketIn = null;

    public FileTransferThread(FileHeader fileHeader, Path path, Socket socket) throws IOException {
        super();

        this.fileHeader = fileHeader;
        this.path = path;
        this.socket = socket;

        setSocketIn(new BufferedInputStream(socket.getInputStream()));
        setSocketOut(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void setSocketOut(BufferedOutputStream socketOut){
        this.socketOut = socketOut;
    }

    public void setSocketIn(BufferedInputStream socketIn){
        this.socketIn = socketIn;
    }

    public void run(){
        System.out.println("FileTransferThread has been started!");

        try {
            transfer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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

    public void checkFile(File file){
    }

    public int compareHeaders(FileHeader fileHeader, File file) throws NoSuchAlgorithmException, IOException {
        FileHeader localFileHeader = constructHeader(file);
        return fileHeader.compareTo(localFileHeader);
    }

    public FileHeader constructHeader(File file) throws NoSuchAlgorithmException, IOException {
        Path sendFile = Paths.get(Constants.BASE_DIR + File.separator + "server", file.getName());

        FileHeader fileHeader = new FileHeader();

        fileHeader.setFileName(file.getName());
        fileHeader.setFileType(Tools.getExtensionByStringHandling(file.getName()).toString());
        fileHeader.setFileSize(Files.size(sendFile));
        fileHeader.setHashAlgo(Constants.HASHING_ALGORITHM.toString());

        MessageDigest md5Digest = MessageDigest.getInstance(Constants.HASHING_ALGORITHM.toString());

        fileHeader.setCheckSum(Tools.getFileChecksum(md5Digest, file));

        return fileHeader;
    }

    // This will be the buffered sending
    public void transfer() throws IOException {
        Path file = FileSystems.getDefault().getPath(String.valueOf(path), fileHeader.fileName);

        int count;
        byte[] buffer = new byte[16 * 1024];

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(String.valueOf(file)));

        while ((count = in.read(buffer)) >= 0) {
            socketOut.write(buffer, 0, count);
            socketOut.flush();
        }

        in.close();
    }

    // Dit is wat er gedaan moet worden aan de kant van de ontvanger
    public void receive() throws IOException {
        Path file = FileSystems.getDefault().getPath(String.valueOf(path), fileHeader.fileName);

        int count;
        byte[] buffer = new byte[16 * 1024];

        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(file));

        while ((count = socketIn.read(buffer)) >= 0) {
            fileOutputStream.write(buffer, 0, count);
            fileOutputStream.flush();
        }

        fileOutputStream.close();
    }
}
