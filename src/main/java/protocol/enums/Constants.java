package protocol.enums;

import java.io.File;

// Constants for this application
public class Constants {
    public enum Strings {
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
                return "v0.1.2";
            }
        },
        DATETIME_FORMAT {
            @Override
            public String toString() {
                return "yyyy-MM-dd'T'HH:mm:ss";
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

    public enum Integers {
        COMM_PORT(42069) {
            @Override
            public String toString() {
                return "42069";
            }
        },
        DATA_PORT(42068) {
            @Override
            public String toString() {
                return "42068";
            }
        };

        private final int value;

        Integers(int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }
}
