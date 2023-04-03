package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Delete implements ICommand {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Delete(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    public void handle(ArrayList<String> args) {
        clientOut.println(output());
    }

    public String output() {
        String output = "Command 'DELETE' called";

        return output + Constants.Strings.END_OF_TEXT;
    }
}
