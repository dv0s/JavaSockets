package protocol.commands;

import protocol.Protocol;
import protocol.data.FileHeader;
import protocol.data.FileMetaData;
import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.handlers.FileHandler;
import protocol.interfaces.ICommand;
import protocol.utils.ConnectionSockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;

public class Sync implements ICommand {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final ConnectionSockets connectionSockets;
    public Socket socket = null;
    public BufferedReader in = null;
    public PrintWriter out = null;

    public Sync(Invoker invoker, Path homeDirectory, ConnectionSockets connectionSockets) throws IOException {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.connectionSockets = connectionSockets;
        this.socket = connectionSockets.commSocket;
        this.in = new BufferedReader(new InputStreamReader(connectionSockets.commSocket.getInputStream()));
        this.out = new PrintWriter(connectionSockets.commSocket.getOutputStream(), true);
    }

    public void handle(ArrayList<String> args) {
        if (invoker == Invoker.CLIENT) {
            try {
                handleClient(args);
            } catch (IOException e){
                System.err.println(e.getMessage());
            }

        } else {
            out.println(Command.LS + output());

            try {
                handleServer(args);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void handleClient(ArrayList<String> args) throws IOException {

    }

    public void handleServer(ArrayList<String> args) throws IOException {
        // Deze methode moet een lijst ontvangen van alle bestanden die de andere kant heeft,
        //  daarna zelf gaan checken of ze overeen komen. De marge die tussen de tijd van de bestanden zit is een seconde
        String nextLine;
        ArrayList<String> remoteDirectory = new ArrayList<>();
        ArrayList<String> localDirectory = FileHandler.directoryList(homeDirectory);

        while ((nextLine = in.readLine()) != null) {
            remoteDirectory.add(nextLine.replace(Constants.Strings.END_OF_TEXT.toString(), ""));

            if (nextLine.contains(Constants.Strings.END_OF_TEXT.toString())) {
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

            ArrayList<FileMetaData> remoteMetaList = FileHandler.convertToFileMetaDataList(remoteDirectoryCopy);
            ArrayList<FileMetaData> localMetaList = FileHandler.convertToFileMetaDataList(localDirectoryCopy);

            // Hier alvast een lijst opstellen met get en put voor bestanden die aan weerskanten niet bestaan.

            // Vul de beide lijsten met items die niet op local staan
            localMetaList.forEach((localItem) -> {
                FileMetaData remoteItem = remoteMetaList.stream()
                        .filter(remote -> localItem.getFileName().equals(remote.fileName))
                        .findAny().orElse(null);

                if (remoteItem != null) {
                    // Als die bestaat, gooi hem dan gelijk in een van de 2 lijsten.
                    int compareInt = localItem.compareDate(remoteItem.lastModified);

                    if (compareInt > 0) {
                        putList.add(localItem.toString());
                    } else if (compareInt < 0) {
                        getList.add(remoteItem.toString());
                    }
                } else {
                    putList.add(localItem.toString());
                }
            });

            // Zoek en pak items die wel op remote staan, en niet op local voor de get list.
            remoteMetaList.forEach((remoteItem) -> {
                FileMetaData localItem = localMetaList.stream()
                        .filter(local -> remoteItem.getFileName().equals(local.fileName))
                        .findAny().orElse(null);

                if (localItem == null) {
                    getList.add(remoteItem.toString());
                }
            });

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

            for (String putItem :
                    putList) {
                System.out.println("Putting: " + putItem);

                // voer put commando uit
                String[] itemProperties = putItem.split(Constants.Strings.UNIT_SEPARATOR.toString());
                ArrayList<String> params = new ArrayList<>();
                params.add(itemProperties[0]);

                new Put(Invoker.CLIENT, homeDirectory, connectionSockets).handle(params);
            }

            for (String getItem :
                    getList) {
                System.out.println("Getting: " + getItem);
                // voer get commando uit
                String[] itemProperties = getItem.split(Constants.Strings.UNIT_SEPARATOR.toString());
                ArrayList<String> params = new ArrayList<>();
                params.add(itemProperties[0]);

                new Get(Invoker.CLIENT, homeDirectory, connectionSockets).handle(params);
            }
        }

        out.println(output());
    }

    public String output() {
        return Constants.Strings.END_OF_TEXT.toString();
    }
}
