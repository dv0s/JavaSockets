package protocol;

import protocol.enums.Command;

import java.util.logging.Handler;

public class Protocol {

    public Command processInput(String input){
        Command command = Command.fromString(input);
        // Hier moet dan iets worden gedaan om de Handle binnen server aan te spreken.

        return command;
    }

    public String ouput(Command input){
        return input.toString();
    }

    private void Stop(){}
}
