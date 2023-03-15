package protocol;

import protocol.commands.*;
import protocol.enums.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static protocol.enums.Command.*;

public class Protocol {

    private final Path homeDirectory;

    public Protocol(Path homeDirectory){
        this.homeDirectory = homeDirectory;
    }

    public void processInput(Invoker invoker, String input, BufferedReader in, PrintWriter out) {
        ArrayList<String> params = getParameters(input);

        // Get command enum, then remove command from the ArrayList
        Command command = Command.fromString(params.get(0));
        params.remove(0);

        // Handle the commands
        switch (command) {
            case OPEN -> new Open(in, out).handle(params);
            case LS -> new List(in, out, params).handle(params);
            case GET -> new Get(invoker, homeDirectory, in, out).handle(params);
            case PUT -> new Put(in, out, params).handle(params);
            case DELETE -> new Delete(in, out, params).handle(params);
            case SIZE -> new Size(in, out, params).handle(params);
            case PORT -> new Port(in, out, params).handle(params);
            case CLOSE -> new Close(in, out).handle(params);
            default -> {
                System.out.println(UNKNOWN);
                out.println(UNKNOWN);
            }
        }
    }

    public void processErrorHandling(){
        System.err.println("Error occurred.. We need to do something here.");
    }

    public String output(Command input) {
        return input.toString();
    }

    public ArrayList<String> getParameters(String input) {
        String[] params = input.split(" ");
        return new ArrayList<>(Arrays.asList(params));
    }

    private void Stop() {
    }
}
