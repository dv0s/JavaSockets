package protocol.enums;

public enum State {
    IDLE {
        @Override
        public String toString() {
            return "IDLE";
        }
    },
    LISTEN {
        @Override
        public String toString() {
            return "LISTENING";
        }
    },
    WAIT {
        @Override
        public String toString() {
            return "WAITING";
        }
    },
    BUSY {
        @Override
        public String toString() {
            return "BUSY";
        }
    },
    CONNECT {
        @Override
        public String toString() {
            return "CONNECTING";
        }
    },
    CLOSE {
        @Override
        public String toString() {
            return "CLOSING";
        }
    },
    SENDING {
        @Override
        public String toString() {
            return "SENDING";
        }
    },
    RECEIVING {
        @Override
        public String toString() {
            return super.toString();
        }
    };

    public static State fromInt(int x) {
        return switch (x) {
            case 0 -> IDLE;
            case 1 -> LISTEN;
            case 2 -> WAIT;
            case 3 -> BUSY;
            case 4 -> CONNECT;
            case 5 -> CLOSE;
            case 6 -> SENDING;
            case 7 -> RECEIVING;
            default -> null;
        };

    }

    public static State fromString(String str) {
        return switch (str) {
            case "IDLE" -> IDLE;
            case "LISTEN" -> LISTEN;
            case "WAIT" -> WAIT;
            case "BUSY" -> BUSY;
            case "CONNECT" -> CONNECT;
            case "CLOSE" -> CLOSE;
            case "SENDING" -> SENDING;
            case "RECEIVING" -> RECEIVING;
            default -> null;
        };
    }
}
