package protocol.commands;

import protocol.enums.Constants;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Size{
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Size(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    public void handle() {
        clientOut.println(output());
    }

    public String output() {
        String output = "Command 'SIZE' called";
        return output + Constants.Strings.END_OF_TEXT;
    }
}
