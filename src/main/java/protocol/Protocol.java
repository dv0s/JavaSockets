package protocol;

import protocol.commands.*;
import protocol.enums.Command;
import protocol.enums.Invoker;
import protocol.handlers.ConnectionHandler;
import protocol.utils.ConnectionSockets;

import java.io.BufferedReader;
import java.io.IOException;
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

    public void processInput(Invoker invoker, String input, ConnectionSockets connectionSockets) throws IOException {
        ArrayList<String> params = getParameters(input);

        // Get command enum, then remove command from the ArrayList
        Command command = Command.fromString(params.get(0));
        params.remove(0);

        // Handle the commands
        switch (command) {
//            case OPEN -> new Open(invoker, homeDirectory, connectionSockets).handle(params); // TODO: Command moet nog worden gemaakt.
            case LS -> new List(invoker, homeDirectory, connectionSockets).handle(params);
            case GET -> new Get(invoker, homeDirectory, connectionSockets).handle(params);
            case PUT -> new Put(invoker, homeDirectory, connectionSockets).handle(params);
//            case DELETE -> new Delete().handle(params); // TODO: Command moet nog worden gemaakt.
//            case SIZE -> new Size().handle(params);
//            case PORT -> new Port().handle(params);
            case SYNC -> new Sync(invoker, homeDirectory, connectionSockets).handle(params);
            case CLOSE -> new Close(invoker, connectionSockets).handle(params);
            default -> new Unknown(invoker, connectionSockets).handle();
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

    private void Stop() {
    }
}
