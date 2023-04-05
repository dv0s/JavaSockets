package nl.socketsoldiers.protocol.handlers;

import nl.socketsoldiers.protocol.data.FileHeader;
import nl.socketsoldiers.protocol.data.FileMetaData;
import nl.socketsoldiers.protocol.enums.Constants;
import nl.socketsoldiers.protocol.enums.ResponseCode;
import nl.socketsoldiers.protocol.utils.Tools;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

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

        LocalDateTime lastModifiedDateTime = LocalDateTime.parse(fileHeader.lastModified);
        Instant instant = lastModifiedDateTime.toInstant(ZoneOffset.UTC);
        Files.setLastModifiedTime(file, FileTime.from(instant));
    }
    //endregion

    //region Static Methods

    // Voor de argumenten "Wie, Wat, Waar" aan houden
    //  Dit wil zeggen: Socket, fileName, homeDirectory
    public static FileHeader constructFileHeader(String fileName, Path homeDirectory) {
        Path path = Paths.get(homeDirectory.toString() + File.separator + fileName);

        // Bestand klaar maken voor overdracht
        File file;
        MessageDigest md5Digest;
        String checkSum;
        Path sendFile;
        FileHeader fileHeader = new FileHeader();
        try {
            file = new File(path.toString());
            md5Digest = MessageDigest.getInstance(Constants.Strings.HASHING_ALGORITHM.toString());
            checkSum = Tools.getFileChecksum(md5Digest, file);

            sendFile = Paths.get(homeDirectory.toString(), file.getName());
            BasicFileAttributes attributes = Files.readAttributes(sendFile, BasicFileAttributes.class);

            // Standardize last modified date
            String lastModifiedDateTime = preparedDateTimeString(attributes.lastModifiedTime().toMillis());

            // Fill the file header
            fileHeader.setFileName(sendFile.getFileName().toString());
            fileHeader.setLastModified(lastModifiedDateTime);
            fileHeader.setFileSize(Files.size(sendFile));
            fileHeader.setHashAlgo(Constants.Strings.HASHING_ALGORITHM.toString());
            fileHeader.setCheckSum(checkSum);

        } catch (NullPointerException e) {
            System.err.print(e.getMessage());
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println(e.getMessage());
        }

        return fileHeader;
    }

    public static String directoryListAsString(Path homeDirectory) {

        ArrayList<String> fileList = directoryList(homeDirectory);

        if (!fileList.isEmpty()) {
            return String.join("\n", fileList);
        }

        return ResponseCode.FAILURE + " DIRECTORY EMPTY" + Constants.Strings.END_OF_TEXT;
    }

    public static ArrayList<String> directoryList(Path homeDirectory) {
        ArrayList<String> fileList = new ArrayList<>();

        try (Stream<Path> list = Files.list(homeDirectory)){
            list.forEach((file) -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                    FileMetaData fileMetaData = new FileMetaData(file.getFileName().toString(), preparedDateTimeString(attributes.lastModifiedTime().toMillis()));
                    fileList.add(fileMetaData.toString());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        } catch (IOException e) {
            System.out.print(e.getMessage());
        }

        return fileList;
    }

    public static ArrayList<String> compareContents(ArrayList<String> local, ArrayList<String> remote){
        ArrayList<String> localList = new ArrayList<>();
        ArrayList<String> remoteList = new ArrayList<>();

        local.forEach((rule) -> {
            String[] attr = rule.split(Constants.Strings.UNIT_SEPARATOR.toString());
            localList.add(attr[0] + Constants.Strings.UNIT_SEPARATOR + attr[2]);
        });

        // TODO: 22/03/2023 FIX Deze functie wordt waarschijnlijk niet afgemaakt, but just in case.
        return localList;
    }


    public static String preparedDateTimeString(long millis){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC).toString();
    }

    public static ArrayList<FileMetaData> convertToFileMetaDataList(ArrayList<String> list){
        ArrayList<FileMetaData> result = new ArrayList<>();
        list.forEach((file) -> {
            String sanitized = file.replace(Constants.Strings.FILE_SEPARATOR.toString(), "");
            String[] lines = sanitized.split(Constants.Strings.UNIT_SEPARATOR.toString());
            if(lines.length != 2){
                throw new RuntimeException("Couldn't convert to meta list: missing attributes");
            }
            FileMetaData fileMetaData = new FileMetaData(lines[0], lines[1]);
            result.add(fileMetaData);
        });
        return result;
    }
    //endregion
}
