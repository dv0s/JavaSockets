package protocol;

import protocol.enums.Command;
import server.handlers.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Protocol {

    public void processInput(String input, BufferedReader clientIn, PrintWriter clientOut) {
        ArrayList<String> params = getParameters(input);

        Command command = Command.fromString(params.get(0));

        // Handle the commands
        switch (command) {
            case OPEN -> new Open(clientIn, clientOut).handle();
            case LS -> new List(clientIn, clientOut).handle();
            case DIR -> new Dir(clientIn, clientOut).handle();
            case GET -> new Get(clientIn, clientOut).handle();
            case PUT -> new Put(clientIn, clientOut).handle();
            case DELETE -> new Delete(clientIn, clientOut).handle();
            case SIZE -> new Size(clientIn, clientOut).handle();
            case PORT -> new Port(clientIn, clientOut).handle();
            case CLOSE -> new Close(clientIn, clientOut).handle();
            default -> {
                System.out.println("COMMAND UNKNOWN");
                clientOut.println("COMMAND UNKNOWN");
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
