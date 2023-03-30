package protocol.enums;

public enum Command {
    OPEN,
    LS,
    DIR,
    GET,
    PUT,
    DELETE,
    SYNC,
    SIZE,
    PORT,
    CLOSE,
    UNKNOWN {
        @Override
        public String toString() {
            return "Unknown command.\n" + Constants.END_OF_TEXT;
        }
    };

    public static Command fromInt(int x) {
        return switch (x) {
            case 0 -> OPEN;
            case 1 -> LS;
            case 2 -> DIR;
            case 3 -> GET;
            case 4 -> PUT;
            case 5 -> DELETE;
            case 6 -> SYNC;
            case 7 -> SIZE;
            case 8 -> PORT;
            case 9 -> CLOSE;
            default -> UNKNOWN;
        };
    }

    public static Command fromString(String command) {
        String str = command.trim().toUpperCase();

        return switch (str) {
            case "OPEN" -> OPEN;
            case "LS" -> LS;
            case "DIR" -> DIR;
            case "GET" -> GET;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            case "SYNC" -> SYNC;
            case "SIZE" -> SIZE;
            case "PORT" -> PORT;
            case "CLOSE" -> CLOSE;
            default -> UNKNOWN;
        };
    }

}
