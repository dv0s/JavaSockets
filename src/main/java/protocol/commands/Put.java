package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Put implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Put(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }
    @Override
    public void handle(ArrayList<String> args) {

        // Hier moet een transferThread worden geopend die van de client ontvangt.
        clientOut.println(output());
    }

    @Override
    public String output() {
        String output = "Command 'PUT' called";
        return output + Constants.END_OF_TEXT;
    }
}
