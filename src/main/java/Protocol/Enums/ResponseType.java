package Protocol.Enums;

public enum ResponseType {
    SUCCESS{
        @Override
        public String toString() { return "Success"; }
        public Integer toInt() {return 200; }
    },
    ERROR{
        @Override
        public String toString() { return "Error"; }
        public Integer toInt() {return 400; }
    },
    FAILURE{
        @Override
        public String toString() { return "Failure"; }
        public Integer toInt() {return 500; }
    }
}
