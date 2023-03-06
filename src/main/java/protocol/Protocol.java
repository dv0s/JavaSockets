package protocol;

import protocol.enums.Command;
import protocol.returnobjects.Message;
import server.handlers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Protocol {

    public void processInput(String input, ObjectInputStream clientIn, ObjectOutputStream clientOut) throws IOException {
        ArrayList<String> params = getParameters(input);

        // Get command enum, then remove command from the ArrayList
        Command command = Command.fromString(params.get(0));
        params.remove(0);

        // Handle the commands
        switch (command) {
            case OPEN -> new Open(clientIn, clientOut, params).handle();
            case LS -> new List(clientIn, clientOut, params).handle();
            case DIR -> new Dir(clientIn, clientOut, params).handle();
            case GET -> new Get(clientIn, clientOut, params).handle();
            case PUT -> new Put(clientIn, clientOut, params).handle();
            case DELETE -> new Delete(clientIn, clientOut, params).handle();
            case SIZE -> new Size(clientIn, clientOut, params).handle();
            case PORT -> new Port(clientIn, clientOut, params).handle();
            case CLOSE -> new Close(clientIn, clientOut).handle();
            default -> {
                System.out.println("COMMAND UNKNOWN");
                clientOut.writeObject(new Message("COMMAND UNKNOWN", true));
            }
        }
    }

    public String output(Command input) {
        return input.toString();
    }

    private ArrayList<String> getParameters(String input) {
        String[] params = input.split(" --");
        return new ArrayList<>(Arrays.asList(params));
    }

    private void Stop() {
    }
}
