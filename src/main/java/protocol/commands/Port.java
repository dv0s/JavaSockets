package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Port implements ICommand {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Port(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
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
        String output = "Command 'PORT' called";
        return output + Constants.END_OF_TEXT;
    }
}
