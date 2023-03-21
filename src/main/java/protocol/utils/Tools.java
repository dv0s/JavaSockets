package protocol.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Optional;

public class Tools {

    /**
     * Returns a checksum of given file and digest.
     *
     * @param digest
     * @param file
     * @return String
     * @throws IOException
     */
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        System.out.println("Calculating checksum. Please wait.");

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static Path initializeHomeDirectory(String homeDirectory) {
        Path path = null;
        try {
            path = Paths.get(homeDirectory);

            if (Files.notExists(path)) {
                Files.createDirectories(path);
                System.out.println("Base directory has been created. Location is: " + path);
            } else {
                System.out.println("Base directory location: " + path);
            }

        } catch (IOException e) {
            System.err.println("Failed to create home directory!");
            System.exit(1);
        }

        return path;

    }

    public static Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static void startScreen() {
        System.out.println("" +
                " _____            _        _     _____       _     _ _               \n" +
                "/  ___|          | |      | |   /  ___|     | |   | (_)              \n" +
                "\\ `--.  ___   ___| | _____| |_  \\ `--.  ___ | | __| |_  ___ _ __ ___ \n" +
                " `--. \\/ _ \\ / __| |/ / _ \\ __|  `--. \\/ _ \\| |/ _` | |/ _ \\ '__/ __|\n" +
                "/\\__/ / (_) | (__|   <  __/ |_  /\\__/ / (_) | | (_| | |  __/ |  \\__ \\\n" +
                "\\____/ \\___/ \\___|_|\\_\\___|\\__| \\____/ \\___/|_|\\__,_|_|\\___|_|  |___/\n" +
                "                                                                     \n");
    }
}
