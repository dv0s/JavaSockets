package protocol.commands;

import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

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

        // TODO: FIX Als client LS commando geeft, dan moet het eerst aan de server laten weten
        //  dat het een lijst wilt opsturen. Maar als de server het commando geeft, dan moet de client
        //  gelijk daarop antwoorden.

        if (invoker == Invoker.SERVER) {
            out.println(Command.LS);

            try{
                handleServer();
            } catch(IOException e){
                System.out.println(e.getMessage());
            }
        } else {

            out.println(generateDirectoryListAsString(homeDirectory) + output());
        }
    }

    public void handleServer() throws IOException{
        // Deze methode moet een lijst ontvangen van alle bestanden die de andere kant heeft,
        //  daarna zelf gaan checken of ze overeen komen. De marge die tussen de tijd van de bestanden zit is een seconde
        String nextLine;
        java.util.List<String> remoteDirectory = null;
        java.util.List<String> localDirectory = null;
        while((nextLine = in.readLine()) != null){
            remoteDirectory.add(nextLine);

            if(nextLine.contains(Constants.END_OF_TEXT.toString())){
                break;
            }
        }


    }

    private String generateDirectoryListAsString(Path homeDirectory){
        ArrayList<String> fileList = new ArrayList<>();
        try {
            Files.list(homeDirectory).forEach((file) -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                    fileList.add(file.getFileName() + "\u001f" + Files.size(file) + "\u001f" + attributes.lastModifiedTime());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });

        } catch (IOException e) {
            System.out.print(e.getMessage());
        }

        if(!fileList.isEmpty()) {
            return String.join("\n", fileList);
        }

        return "ERROR";
    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}
