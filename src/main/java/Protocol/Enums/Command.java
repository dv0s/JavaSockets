package Protocol.Enums;

public enum Command {
    OPEN{
        @Override
        public String toString() { return "OPEN"; }
    },
    LS{
        @Override
        public String toString() { return "LIST"; }
    },
    DIR{
        @Override
        public String toString() { return "LIST"; }
    },
    GET{
        @Override
        public String toString() { return "GET"; }
    },
    PUT{
        @Override
        public String toString() { return "PUT"; }
    },
    DELETE{
        @Override
        public String toString() { return "DELETE"; }
    },
    SIZE{
        @Override
        public String toString() { return "SIZE"; }
    },
    PORT{
        @Override
        public String toString() { return "PORT"; }
    },
    CLOSE{
        @Override
        public String toString() { return "CLOSE"; }
    };

    public static Command fromInt(int x){
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
            default -> null;
        };
    }
    public static Command fromString(String command){
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
            default -> null;
        };
    }
}
