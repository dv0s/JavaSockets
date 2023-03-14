package protocol.commands;

import protocol.enums.Constants;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Close implements CommandHandler {
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;

    public Close(BufferedReader clientIn, PrintWriter clientOut) {
        this.clientIn = clientIn;
        this.clientOut = clientOut;
    }

    @Override
    public void handle(ArrayList<String> args) {
        clientOut.println(output());

        try {
            clientIn.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clientOut.close();
    }

    @Override
    public String output() {
        return "Disconnecting from server..." + Constants.END_OF_TRANSMISSION;
    }
}
