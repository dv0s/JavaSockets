package protocol;

import protocol.commands.*;
import protocol.enums.Command;
import protocol.enums.Invoker;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static protocol.enums.Command.UNKNOWN;

public class Protocol {

    private final Path homeDirectory;

    public Protocol(Path homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public void processInput(Invoker invoker, String input, Socket socket, BufferedReader in, PrintWriter out) {
        ArrayList<String> params = getParameters(input);

        // Get command enum, then remove command from the ArrayList
        Command command = Command.fromString(params.get(0));
        params.remove(0);

        // Handle the commands
        switch (command) {
//            case OPEN -> new Open(invoker, homeDirectory, socket, in, out).handle(params);
            case LS -> new List(invoker, homeDirectory, socket, in, out).handle(params);
            case GET -> new Get(invoker, homeDirectory, socket, in, out).handle(params);
            case PUT -> new Put(invoker, homeDirectory, socket, in, out).handle(params);
            case DELETE -> new Delete(in, out, params).handle(params); // TODO: Command moet nog worden gemaakt.
//            case SIZE -> new Size(in, out, params).handle();
//            case PORT -> new Port(in, out, params).handle(params);
            case SYNC -> new Sync(invoker, homeDirectory, socket, in, out).handle(params);
            case CLOSE -> new Close(invoker, in, out).handle(params);
            default -> new Unknown(invoker, in, out).handle();
        }
    }

    public void processErrorHandling() {
        System.err.println("Error occurred.. We need to do something here.");
    }

    public String output(Command input) {
        return input.toString();
    }

    public ArrayList<String> getParameters(String input) {
        String[] params = input.split(" ");
        return new ArrayList<>(Arrays.asList(params));
    }
}
