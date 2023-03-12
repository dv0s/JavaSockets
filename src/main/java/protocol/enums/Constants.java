package protocol.enums;

// Constants for this application
public enum Constants {
    // Values
    CHARSET{
        @Override
        public String toString() {
            return "UTF-8";
        }
    },
    HASHING_ALGORITHM{
        @Override
        public String toString() {
            return "MD5";
        }
    },
    END_OF_TEXT{
        @Override
        public String toString() {
            return "\u0003";
        }
    },
    END_OF_TRANSMISSION{
        @Override
        public String toString() {
            return "\u0004";
        }
    };
}
