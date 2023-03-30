package protocol.commands;

import protocol.data.FileMetaData;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.FileHandler;
import protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;

public class List implements ICommand {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final BufferedReader in;
    public final PrintWriter out;
    public final ArrayList<String> params;

    public List(Invoker invoker, Path homeDirectory, BufferedReader in, PrintWriter out, ArrayList<String> params) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.in = in;
        this.out = out;
        this.params = params;
    }


    @Override
    public void handle(ArrayList<String> args) {
        System.out.println(homeDirectory.toString());

        if (invoker == Invoker.SERVER) {
            out.println(Command.LS + output());

            try {
                handleServer();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            out.println(FileHandler.directoryListAsString(homeDirectory) + output());
        }
    }

    public void handleServer() throws IOException {
        // Deze methode moet een lijst ontvangen van alle bestanden die de andere kant heeft,
        //  daarna zelf gaan checken of ze overeen komen. De marge die tussen de tijd van de bestanden zit is een seconde
        String nextLine;
        ArrayList<String> remoteDirectory = new ArrayList<>();
        ArrayList<String> localDirectory = FileHandler.directoryList(homeDirectory);

        while ((nextLine = in.readLine()) != null) {
            remoteDirectory.add(nextLine.replace(Constants.END_OF_TEXT.toString(), ""));

            if (nextLine.contains(Constants.END_OF_TEXT.toString())) {
                break;
            }
        }

        if (localDirectory.equals(remoteDirectory)) {
            System.out.println("Both directories are equal.");
        } else {

            // Op het moment dat er een verschil in beide mappen ontdekt is, moeten beide lijsten worden mee gestuurd
            // naar de sync functie die door de server wordt aangeroepen. Dit zal een serie van Get en Put commando's
            // over en weer gooien zodat de mappen weer gelijk zijn aan elkaar.
            System.out.println("Discrepancy detected.");

            ArrayList<FileMetaData> putList = new ArrayList<>();
            ArrayList<FileMetaData> getList = new ArrayList<>();
            ArrayList<FileMetaData> delList = new ArrayList<>();

            ArrayList<String> remoteDirectoryCopy = (ArrayList<String>) remoteDirectory.clone();
            ArrayList<String> localDirectoryCopy = (ArrayList<String>) localDirectory.clone();
            ArrayList<String> differenceDirectory = new ArrayList<>();


            System.out.println();
            System.out.println("Local Directory:");
            localDirectory.forEach(System.out::println);

            System.out.println();
            System.out.println("Remote Directory:");
            remoteDirectory.forEach(System.out::println);

            System.out.println();
            System.out.println("Differences:");
            remoteDirectoryCopy.removeAll(localDirectory);
            localDirectoryCopy.removeAll(remoteDirectory);

            System.out.println("--Remote");
            remoteDirectoryCopy.forEach(System.out::println);

            System.out.println("--Local");
            localDirectoryCopy.forEach(System.out::println);

            ArrayList<FileMetaData> remoteMetaList = convertToFileMetaDataList(remoteDirectoryCopy);
            ArrayList<FileMetaData> localMetaList = convertToFileMetaDataList(localDirectoryCopy);


            //https://stackoverflow.com/questions/57252497/java-8-streams-compare-two-lists-object-values-and-add-value-to-new-list

        }

        out.println(output());

    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }

    public ArrayList<FileMetaData> convertToFileMetaDataList(ArrayList<String> list){
        ArrayList<FileMetaData> result = new ArrayList<>();
        list.forEach((file) -> {
            String sanitized = file.replace(Constants.FILE_SEPARATOR.toString(), "");
            String[] lines = sanitized.split(Constants.UNIT_SEPARATOR.toString());

            FileMetaData fileMetaData = new FileMetaData(lines[0], lines[1], lines[2]);
            result.add(fileMetaData);
        });
        return result;
    }
}
