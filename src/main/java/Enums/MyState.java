package Enums;

public enum MyState {
    IDLE {
        @Override
        public String toString() {
            return "Idle";
        }
    },
    STARTING {
        @Override
        public String toString() {
            return "Starting";
        }
    },
    WAITING{
        @Override
        public String toString() {
            return "Waiting";
        }
    },
    AWAITING{
        @Override
        public String toString() {
            return "Awaiting";
        }
    },
    LISTENING{
        @Override
        public String toString() {
            return "Listening";
        }
    },
    CONNECTING{
        @Override
        public String toString() {
            return "Connecting";
        }
    },
    BUSY{
        @Override
        public String toString() {
            return "Busy";
        }
    },
    CLOSING{
        @Override
        public String toString() {
            return "Closing";
        }
    };


    public static MyState fromInteger(int x)
    {
        switch(x){
            case 0:
                return IDLE;
            case 1:
                return STARTING;
            case 2:
                return LISTENING;
            case 3:
                return BUSY;
            case 4:
                return CLOSING;
        }

        return null;
    }
}
