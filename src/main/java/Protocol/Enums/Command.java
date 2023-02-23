package Protocol.Enums;

public enum Command {
    OPEN{
        @Override
        public String toString() { return "Open"; }
    },
    LS{
        @Override
        public String toString() { return "List"; }
    },
    DIR{
        @Override
        public String toString() { return "List"; }
    },
    GET{
        @Override
        public String toString() { return "Get"; }
    },
    PUT{
        @Override
        public String toString() { return "Put"; }
    },
    DELETE{
        @Override
        public String toString() { return "Delete"; }
    },
    SIZE{
        @Override
        public String toString() { return "Size"; }
    },
    PORT{
        @Override
        public String toString() { return "Port"; }
    },
    CLOSE{
        @Override
        public String toString() { return "Close"; }
    }
}
