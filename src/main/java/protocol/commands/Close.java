package protocol.commands;

import protocol.enums.Command;
import protocol.enums.Constants;
import protocol.enums.Invoker;
import protocol.interfaces.ICommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Close implements ICommand {
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
        if (invoker == Invoker.SERVER) {
            out.println("Disconnecting from server...");
            out.println(output());
            out.close();

            try {
                in.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            out.println(Command.CLOSE);
        }
    }

    @Override
    public String output() {
        return Constants.Strings.END_OF_TRANSMISSION.toString();
    }
}
