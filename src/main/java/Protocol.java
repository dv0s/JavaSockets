/// De Client gaat via dit protocol kunnen communiceren met de Server.
public class Protocol {

    private State state = State.WAITING;
    private final String[] commands = {
            "CONNECT", // Maak verbinding met Server
            "LS", // Vraag lijst op van bestanden
            "TYPE", // Verklaar bestand overdracht type
            "GET", // Download enkel bestand
            "PUT", // Upload enkel bestand
            "DELETE", // Verwijder enkel bestand
            "SIZE", // Vraag grootte van enkel bestand
            "PASV", // Zet verbinding in passive modus
            "PORT", // Definieer een adres en port waar de server verbinding mee zou moeten maken
            "CLOSE", // Sluit de verbinding
            "REIN", // Start de verbinding opnieuw op.
            "REST"}; // Hervat de overdracht vanaf een specifiek punt.

    // Methode om de input van de Client te kunnen verwerken.
    public String processInput(String myInput)
    {
        // Deze string zal het protocol gebruiken om terug te sturen naar de client.
        String myOutput = null;

        if(state == State.WAITING){
            myOutput = "Waiting for your command";
            state = State.LISTENING;

        }else if(state == State.LISTENING){
            String[] arr = myInput.split(" ", 2);
            String command = arr[0];
            String[] args = null;
            if(arr.length > 1) {
                args = arr[1].split(" ");
            }

            Response res = Command(command, args);

            state = res.state;
            myOutput = res.message;

        }else if(state == State.IDLE){
            myOutput = "Currently IDLE";
            state = State.WAITING;

        }else if(state == State.CLOSING){
            myOutput = "Bye.";
            state = State.WAITING;
        }

        return myOutput;
    }

    // Methode om commando's te kunnen verwerken. Ontvangt een commando met eventuele argumenten
    // Geeft response klasse terug
    public Response Command(String command, String[] args){
        // Maak het commando eerst schoon zodat het niet uit maakt hoe het wordt ingevoerd.
        command = command.strip().toUpperCase();

        // Initieer een response klasse om terug te sturen.
        Response response = new Response(command, args, 0, null, null);

        // Voer ingevoerde commando uit.
        switch (command){
            // Connect: zou een nieuwe verbinding op moeten zetten om iets te kunnen doen?
            case "CONNECT":
                response.setResponse(100, state, "Connecting...");
                break;

            // ls: zou een lijst van bestanden in de map moeten weergeven.
            case "LS":
                response.setResponse(100, state, "Listing directory");
                break;

            // GET: Moet het bestand ophalen die is meegegeven. Krijgt een pad van het bestand en moet het in eigen
            // systeem opslaan op het pad waar de sync folder staat.
            case "GET":
                if(args.length <= 0){
                    response.setResponse(400, State.LISTENING, "Which file do you want to download?");
                    return response;
                }
                response.setResponse(100, State.LISTENING, "GET:" + args[0]);
                break;

            case "PUT":
                if(args.length <= 0){
                    response.setResponse(400, State.LISTENING, "Which file do you want to upload?");
                    return response;
                }
                response.setResponse(100, State.LISTENING, "PUT:" + args[0]);
                break;

            case "STATE":
                response.setResponse(100, state, state.toString());
                break;

            case "CLOSE":
                response.setResponse(100, State.CLOSING, "Bye.");
                break;

            default:
                response.setResponse(502, State.LISTENING, "Command not implemented.");
                break;
        }

        return response;
    }


}
