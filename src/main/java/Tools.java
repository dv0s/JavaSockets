import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        };

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
        // Doorzoek alle bestanden in een bepaalde map
        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
            // Voor elk bestand, print voorlopig het pad naar console.
            paths
                    .filter(Files::isRegularFile)
                    .forEach(System.out::println);
        } catch (AccessDeniedException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void CheckRootDir(String dir){
        // For more info, look here : https://www.baeldung.com/java-nio2-file-attribute
//        Path path = Paths.get(dir);
//        if(!Files.exists(path))
//        {
//            try(
//                    FileAttribute attr = new FileAttribute {
//                        "Owner", "DvOs"
//            }
//                Files.createDirectories(Paths.get(dir));
//            ){
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}
