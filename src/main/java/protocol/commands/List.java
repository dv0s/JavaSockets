package protocol.commands;

import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;

public class List implements CommandHandler {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final BufferedReader clientIn;
    public final PrintWriter clientOut;
    public final ArrayList<String> params;

    public List(Invoker invoker, Path homeDirectory, BufferedReader clientIn, PrintWriter clientOut, ArrayList<String> params) {
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.clientIn = clientIn;
        this.clientOut = clientOut;
        this.params = params;
    }


    @Override
    public void handle(ArrayList<String> args) {

        clientOut.println(output());
    }

    public void handleClient(ArrayList<String> args){

    }

    public void handleServer(ArrayList<String> args){

    }

    @Override
    public String output() {
        String output = "Command 'LIST' called";
        return output + Constants.END_OF_TEXT;
    }
}
