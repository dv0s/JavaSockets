package protocol.commands;

import protocol.enums.Constants;
import protocol.enums.Invoker;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Unknown {

    public final Invoker invoker;
    public final BufferedReader in;
    public final PrintWriter out;

    public Unknown(Invoker invoker, BufferedReader in, PrintWriter out){
        this.invoker = invoker;
        this.in = in;
        this.out = out;
    }

    public void handle() {
        out.println("COMMAND UNKNOWN" + output());

    }

    public String output(){
        return Constants.Strings.END_OF_TEXT.toString();
    }
}