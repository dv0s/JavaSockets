package server.handlers;

import protocol.enums.Constants;
import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Delete extends BaseHandler implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Delete(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    @Override
    public void handle() {
        clientOut.println(output());
    }

    @Override
    public String output() {
        String output = "Command 'DELETE' called";

        return output + Constants.END_OF_TEXT;
    }
}
