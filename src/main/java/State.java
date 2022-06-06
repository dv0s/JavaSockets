public enum State {
    IDLE {
        @Override
        public String toString() {
            return "Idle";
        }
    },
    WAITING{
        @Override
        public String toString() {
            return "Waiting";
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


    public static State fromInteger(int x)
    {
        switch(x){
            case 0:
                return IDLE;
            case 1:
                return WAITING;
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
