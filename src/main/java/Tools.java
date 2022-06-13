import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.stream.Stream;

public class Tools {
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

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
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static void ScanDir(String dir)
    {
        // Bestaat de map?
        Path of = Path.of(dir);
        if(Files.notExists(of)) {
            System.out.println("Path of " + dir + " doesn't exists");
            return;
        }

        // Doorzoek alle bestanden in een bepaalde map
        try (Stream<Path> paths = Files.walk(of)) {
            // Voor elk bestand, print voorlopig het pad naar console.
            paths.filter(Files::isRegularFile).forEach(System.out::println);
        } catch (AccessDeniedException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSocketAlive(int port)
    {
        boolean isAlive = false;
        SocketAddress socketAddress = new InetSocketAddress(port);
        Socket socket = new Socket();
        int timeout = 2000;

        try{
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;

        } catch (IOException e) {
            System.out.println("IOException: unable to connect to " + port + ": " + e);
        }

        return isAlive;
    }

    public static boolean isSocketAlive(String hostname, int port)
    {
        boolean isAlive = false;
        SocketAddress socketAddress = new InetSocketAddress(hostname, port);
        Socket socket = new Socket();
        int timeout = 2000;

        try{
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;

        } catch (IOException e) {
            System.out.println("IOException: unable to connect to " + port + ": " + e);
        }

        return isAlive;
    }
}
