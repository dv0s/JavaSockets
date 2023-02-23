package Protocol.Enums;

public enum State {
    IDLE{
        @Override
        public String toString() { return "Idle"; }
    },
    LISTEN{
        @Override
        public String toString() { return "Listening"; }
    },
    WAIT{
        @Override
        public String toString() { return "Waiting"; }
    },
    BUSY{
        @Override
        public String toString() { return "Busy"; }
    },
    CONNECT{
        @Override
        public String toString() { return "Connecting"; }
    },
    CLOSE{
        @Override
        public String toString() { return "Closing"; }
    }
}
