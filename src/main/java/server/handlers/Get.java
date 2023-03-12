package server.handlers;

import protocol.enums.Constants;
import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Get extends BaseHandler implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public Get(BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }

    @Override
    public void handle() {
        System.out.println(output());
        clientOut.println(output());
    }

    @Override
    public String output() {
        String output;
        if (this.params.isEmpty()) {
            output =  "Command 'GET' called";
        }

        output = "Command 'GET' called with parameters: " + this.params;

        return output + Constants.END_OF_TEXT;
    }
}
