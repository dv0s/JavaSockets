package protocol.commands;

import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Close implements CommandHandler {
    public final Invoker invoker;
    public final BufferedReader in;
    public final PrintWriter out;

    public Close(Invoker invoker, BufferedReader in, PrintWriter out) {
        this.invoker = invoker;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ArrayList<String> args) {
        if(invoker == Invoker.SERVER){

            out.println(output());
            out.close();

            try {
                in.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public String output() {
        return "Disconnecting from server..." + Constants.END_OF_TRANSMISSION;
    }
}
