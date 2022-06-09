import Enums.MyState;
import Models.Response;

/// De Client gaat via dit protocol kunnen communiceren met de Server.
public class Protocol {

    private MyState myState = MyState.STARTING;
    private final String[] commands = {
            "CONNECT", // Maak verbinding met Server
            "LS", // Vraag lijst op van bestanden
            "COMPARE", // Check beide base dirs
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

    // Methode die huidige state kan veranderen.
    public void setState(MyState myState)
    {
        this.myState = myState;
    }

    // Methode die huidige state terug geeft.
    public MyState getState()
    {
        return myState;
    }

    // Methode om de input van de Client te kunnen verwerken.
    public String processInput(String myInput)
    {
        // Deze string zal het protocol gebruiken om terug te sturen naar de client.
        String myOutput = null;

        // State Starting betekend net opgestart en bijna klaar om te gaan.
        if(myState == MyState.STARTING){
            myOutput = "Ready for your command";
            myState = MyState.LISTENING;

        } else if(myState == MyState.LISTENING) {
            String[] input = myInput.split(" ", 2);
            String command = input[0];
            String[] args = null;
            if (input.length > 1) {
                args = input[1].split(" ");
            }

            Response res = Command(command, args);

            myState = res.myState;
            myOutput = res.message;

        } else if (myState == MyState.CONNECTING) {
            myOutput = "Awaiting at: localhost 42068";
            myState = MyState.AWAITING;

        } else if (myState == MyState.AWAITING) {
            // TODO: 06/06/2022 Logic voor het afvangen van client connectie
            myOutput = "Done for now.";
            myState = MyState.LISTENING;
            
        } else if(myState == MyState.CLOSING){
            myOutput = "Bye.";
            myState = MyState.STARTING;
        }

        return myOutput;
    }

    // Methode om commando's te kunnen verwerken. Ontvangt een commando met eventuele argumenten
    // Geeft response klasse terug
    public Response Command(String command, String[] args){
        // Maak het commando eerst schoon zodat het niet uit maakt hoe het wordt ingevoerd.
        command = command.strip().toUpperCase();

        // Maak een response klasse om terug te sturen.
        Response response = new Response(command, args, 0, null, null);

        // Voer ingevoerde commando uit.
        switch (command){
            // Connect: zou een nieuwe verbinding op moeten zetten om iets te kunnen doen?
            case "CONNECT":
                response.setResponse(100, myState, "Connecting...");
                break;

            case "OPEN":
                response.setResponse(100, MyState.LISTENING, "OPEN:localhost:42068");
                break;

            case "COMPARE":
                response.setResponse(100, myState, "Comparing...");
                break;

            case "SYNC":
                response.setResponse(100, myState, "Syncing...");
                break;

            // ls: zou een lijst van bestanden in de map moeten weergeven.
            case "LS":
                response.setResponse(100, myState, "Listing directory");
                break;

            // GET: Moet het bestand ophalen die is meegegeven. Krijgt een pad van het bestand en moet het in eigen
            // systeem opslaan op het pad waar de sync folder staat.
            case "GET":
                if(args.length <= 0){
                    response.setResponse(400, MyState.LISTENING, "Which file do you want to download?");
                    return response;
                }
                response.setResponse(100, MyState.LISTENING, "GET:" + args[0]);
                break;

            case "PUT":
                if(args.length <= 0){
                    response.setResponse(400, MyState.LISTENING, "Which file do you want to upload?");
                    return response;
                }
                response.setResponse(100, MyState.LISTENING, "PUT:" + args[0]);
                break;

            case "STATE":
                response.setResponse(100, myState, myState.toString());
                break;

            case "EXIT":
            case "QUIT":
            case "CLOSE":
                response.setResponse(100, MyState.CLOSING, "Bye.");
                break;

            default:
                response.setResponse(502, MyState.LISTENING, "Command not implemented.");
                break;
        }

        return response;
    }

}
