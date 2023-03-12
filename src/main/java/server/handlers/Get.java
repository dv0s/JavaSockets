package server.handlers;

import protocol.enums.Constants;
import server.interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Get implements CommandHandler {
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
        System.out.println(Constants.BASE_DIR);
        System.out.println(params.get(0));
        Path check = Paths.get(Constants.BASE_DIR.toString() + File.separator + params.get(0));
        System.out.println(Files.exists(check));


        // Hier moet een transferThread worden geopend die naar de client toe stuurt.

        // Eerst moeten we het bestand opzoeken die gevraagd wordt.


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
