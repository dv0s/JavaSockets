package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Open implements CommandHandler {
    public final BufferedReader in;
    public final PrintWriter out;

    public Open(BufferedReader clientIn, PrintWriter clientOut) {
        this.in = clientIn;
        this.out = clientOut;
    }

    @Override
    public void handle(ArrayList<String> args) {
        out.println(output());
    }

    @Override
    public String output() {
        String output = "Command 'OPEN' called";
        return output + Constants.END_OF_TEXT;
    }
}
