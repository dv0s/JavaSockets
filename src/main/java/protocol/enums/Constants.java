package protocol.enums;

import java.io.File;

// Constants for this application
public enum Constants {
    // Values
    CHARSET {
        @Override
        public String toString() {
            return "UTF-8";
        }
    },
    HASHING_ALGORITHM {
        @Override
        public String toString() {
            return "MD5";
        }
    },
    END_OF_TEXT {
        @Override
        public String toString() {
            return "\u0003";
        }
    },
    END_OF_TRANSMISSION {
        @Override
        public String toString() {
            return "\u0004";
        }
    },
    FILE_SEPARATOR {
        @Override
        public String toString() {
            return "\u001C";
        }
    },
    UNIT_SEPARATOR {
        @Override
        public String toString() {
            return "\u001F";
        }
    },
    VERSION {
        @Override
        public String toString() {
            return "v0.0.2";
        }
    },
    BASE_DIR {
        @Override
        public String toString() {
            return System.getProperty("user.home") +
                    File.separator + "documents" +
                    File.separator + "filesync";
        }

    };
}
