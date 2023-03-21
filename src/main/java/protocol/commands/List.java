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
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
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
    public void handle(ArrayList<String> args){
        System.out.println(homeDirectory.toString());

        // List commando moet ervoor zorgen dat de andere een lijst van bestanden teruggeeft die het in het bezit heeft.
        // Server

        if(invoker == Invoker.CLIENT){
            out.println(Command.LS);
        }else{
            try {
                Files.list(homeDirectory).forEach((file) -> {

                    // Define variables
                    BasicFileAttributes attributes;
                    try {
                        attributes = Files.readAttributes(file, BasicFileAttributes.class);
                        out.println(
                                file + "\u001f" +
                                file.getFileName()+"\u001f"+
                                        Files.size(file)+"\u001f"+
                                        attributes.lastAccessTime()+"\u001f"+attributes.lastModifiedTime()
                                        +"\u001c"
                        );

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });

            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }

        out.println(output());
    }

    @Override
    public String output() {
        String output = "Command 'LIST' called";
        return output + Constants.END_OF_TEXT;
    }
}
