package protocol.commands;

import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.handlers.FileHandler;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class List implements CommandHandler {

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

        // TODO: Sever is de enige die dit commando aanroept naar de client toe.
        //  De client zal hier een opdracht voor krijgen op het moment dat het vraagt om te syncen.

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

        // TODO: FIX De check hier moet een lijst terug geven met bestanden die niet overeen komen
        //  En dan eventueel gelijk syncen als je kan.
        if (localDirectory.equals(remoteDirectory)) {
            System.out.println("Both directories are equal.");
        } else {
            System.out.println("Discrepancy detected.");

//            localDirectory.removeAll(remoteDirectory);

            System.out.println("Local Directory:");
            localDirectory.forEach(System.out::println);

            System.out.println("Remote Directory:");
            remoteDirectory.forEach(System.out::println);
        }

        out.println(output());

    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}
