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

            ArrayList<String> remoteDirectoryCopy = (ArrayList<String>) remoteDirectory.clone();
            ArrayList<String> localDirectoryCopy = (ArrayList<String>) localDirectory.clone();
            ArrayList<String> differenceDirectory = new ArrayList<>();

            // Printing
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
            // Printing end

            ArrayList<String> putList = new ArrayList<>();
            ArrayList<String> getList = new ArrayList<>();
            ArrayList<String> delList = new ArrayList<>();

            ArrayList<FileMetaData> remoteMetaList = convertToFileMetaDataList(remoteDirectoryCopy);
            ArrayList<FileMetaData> localMetaList = convertToFileMetaDataList(localDirectoryCopy);

            // Hier alvast een lijst opstellen met get en put voor bestanden die aan weerskanten niet bestaan.

            // Vul de beide lijsten met items die niet op local staan
            localMetaList.forEach((localItem) -> {
                FileMetaData remoteItem = remoteMetaList.stream()
                        .filter(remote -> localItem.getFileName().equals(remote.fileName))
                        .findAny().orElse(null);

                if(remoteItem != null) {
                    // Als die bestaat, gooi hem dan gelijk in een van de 2 lijsten.
                    int compareInt = localItem.compareDate(remoteItem.lastModified);

                    if (compareInt > 0) {
                        putList.add(localItem.toString());
                    } else if (compareInt < 0) {
                        getList.add(remoteItem.toString());
                    }
                }else{
                    putList.add(localItem.toString());
                }
            });

            // Zoek en pak items die wel op remote staan, en niet op local voor de get list.
            remoteMetaList.forEach((remoteItem) -> {
                FileMetaData localItem = localMetaList.stream()
                        .filter(local -> remoteItem.getFileName().equals(local.fileName))
                        .findAny().orElse(null);

                if(localItem == null){
                    getList.add(remoteItem.toString());
                }
            });







//            for(String localDirectoryCopyEntry : localDirectoryCopy){
//                String[] properties = localDirectoryCopyEntry.split(Constants.UNIT_SEPARATOR.toString());
//                String fileName = properties[0];
//
//                remoteMetaList.forEach((item) -> {
//                    if(!item.getFileName().contains(fileName)){
//                        getList.add(localDirectoryCopyEntry);
//                    }
//                });
//            }
//
//            // vul de putList met items die niet op remote staan
//            for(String remoteDirecotryCopyEntry : remoteDirectoryCopy){
//                String[] properties = remoteDirecotryCopyEntry.split(Constants.UNIT_SEPARATOR.toString());
//                String fileName = properties[0];
//
//                localMetaList.forEach((item) -> {
//                    if(!item.getFileName().contains(fileName)){
//                        putList.add(remoteDirecotryCopyEntry);
//                    }
//                });
//            }
//
//            // compare local directory with remote and determine if the file is older or not.
//            localMetaList.forEach(localItem -> {
//                FileMetaData remoteItem = null;
//
//                // Eerst zoeken we het item op door de lokale lijst te doorlopen
//                for(FileMetaData remoteMetaItem : remoteMetaList){
//                    // Als er een match is, dan slaan we het item op.
//                    if(remoteMetaItem.getFileName().equals(localItem.getFileName())){
//                        remoteItem = remoteMetaItem;
//                    }
//                }
//
//                // Daarna gaan we bepalen in welke commando bus het moet komen.
//                if(remoteItem != null){
//                    int compareInt = localItem.compareDate(remoteItem.lastModified);
//
//                    if(compareInt > 0){
//                        putList.add(localItem.toString());
//                    }else if(compareInt < 0){
//                        getList.add(remoteItem.toString());
//                    }
//                }
//            });

            System.out.println();
            System.out.println("Processing lists");
            System.out.println("--Put list");
            putList.forEach(System.out::println);

            System.out.println("--Get list");
            getList.forEach(System.out::println);

            System.out.println("--Del list");
            delList.forEach(System.out::println);

            //https://stackoverflow.com/questions/57252497/java-8-streams-compare-two-lists-object-values-and-add-value-to-new-list
            // Verwijderen moet sowieso gedaan worden tijdens de sessie. Niet bij het opnieuw ophalen.
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
