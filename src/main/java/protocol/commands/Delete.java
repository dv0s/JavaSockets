package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Delete implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Delete(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    @Override
    public void handle(ArrayList<String> args) {
        clientOut.println(output());
    }

    @Override
    public String output() {
        String output = "Command 'DELETE' called";

        return output + Constants.END_OF_TEXT;
    }
}