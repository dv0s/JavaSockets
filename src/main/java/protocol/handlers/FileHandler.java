package protocol.handlers;

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

public class FileHandler {

    public final FileHeader fileHeader;
    public final Path homeDirectory;

    public final Socket socket;
    public BufferedInputStream socketIn = null;
    public BufferedOutputStream socketOut = null;

    public FileHandler(Socket socket, FileHeader fileHeader, Path homeDirectory) throws IOException {
        super();

        this.socket = socket;
        this.fileHeader = fileHeader;
        this.homeDirectory = homeDirectory;

        setSocketIn(new BufferedInputStream(socket.getInputStream()));
        setSocketOut(new BufferedOutputStream(socket.getOutputStream()));
    }

    //region Getters

    public FileHeader getFileHeader() {
        return fileHeader;
    }

    public Path getHomeDirectory() {
        return homeDirectory;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedInputStream getSocketIn() {
        return socketIn;
    }

    public BufferedOutputStream getSocketOut() {
        return socketOut;
    }

    //endregion

    //region Setters

    public void setSocketIn(BufferedInputStream socketIn) {
        this.socketIn = socketIn;
    }

    public void setSocketOut(BufferedOutputStream socketOut) {
        this.socketOut = socketOut;
    }

    // endregion

    //region Methods

    public void sendFile() throws IOException {
        Path file = FileSystems.getDefault().getPath(homeDirectory.toString(), fileHeader.fileName);

        int count;
        byte[] buffer = new byte[16 * 1024];

        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(String.valueOf(file)));

        while ((count = fileIn.read(buffer)) >= 0) {
            socketOut.write(buffer, 0, count);
            socketOut.flush();
        }

        socketOut.close();
        socketIn.close();
        fileIn.close();
    }

    public void receiveFile() throws IOException {

        Path file = FileSystems.getDefault().getPath(homeDirectory.toString(), fileHeader.getFileName());

        int count;
        byte[] buffer = new byte[16 * 1024];

        FileOutputStream fileOut = new FileOutputStream(String.valueOf(file));

        while ((count = socketIn.read(buffer)) >= 0) {
            fileOut.write(buffer, 0, count);
            fileOut.flush();
        }

        fileOut.close();
        socketOut.close();
        socketIn.close();
    }
    //endregion

    //region Static Methods

    // Voor de argumenten "Wie, Wat, Waar" aan houden
    //  Dit wil zeggen: Socket, fileName, homeDirectory
    public static FileHeader constructFileHeader(String fileName, Path homeDirectory){
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

    //endregion
}
