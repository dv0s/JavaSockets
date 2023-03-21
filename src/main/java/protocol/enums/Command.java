package protocol.enums;

public enum Command {
    OPEN,
    LS,
    DIR,
    GET,
    PUT,
    DELETE,
    SIZE,
    PORT,
    CLOSE,
    UNKNOWN {
        @Override
        public String toString() {
            return "UNKNOWN\u0003";
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
            case 6 -> SIZE;
            case 7 -> PORT;
            case 8 -> CLOSE;
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
            case "SIZE" -> SIZE;
            case "PORT" -> PORT;
            case "CLOSE" -> CLOSE;
            default -> UNKNOWN;
        };
    }

}
