package protocol.commands;

import protocol.Protocol;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;

public class Sync implements ICommand {

    public final Invoker invoker;
    public final Path homeDirectory;
    public final Socket socket;
    public final BufferedReader in;
    public final PrintWriter out;

    public Sync(Invoker invoker, Path homeDirectory, Socket socket, BufferedReader in, PrintWriter out){
        this.invoker = invoker;
        this.homeDirectory = homeDirectory;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ArrayList<String> args) {
        System.out.println("Sync command has been called.");
        if(invoker == Invoker.CLIENT){
            handleClient(args);
        }else{
            handleServer(args);
        }
    }

    public void handleClient(ArrayList<String> args){

    }

    public void handleServer(ArrayList<String> args){

    }

    @Override
    public String output() {
        return Constants.END_OF_TEXT.toString();
    }
}
