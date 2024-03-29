package protocol.data;

import protocol.enums.Constants;

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FileMetaData {

    public String fileName;
    public LocalDateTime lastModified;

    public FileMetaData(String fileName, String lastModified){
        this.fileName = fileName;
        this.lastModified = convertStringToLocalDateTime(lastModified);
    }

    // region Getters

    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    // endregion

    public int compareDate(LocalDateTime oLastModified){
        if (lastModified.isEqual(oLastModified)) {
            return 0;
        }else if(lastModified.isAfter(oLastModified)){
            return 1;
        }
        return -1;
    }

    private LocalDateTime convertStringToLocalDateTime(String lastModified){
        LocalDateTime lastModifiedDateTime = LocalDateTime.parse(lastModified);
        Instant instant = lastModifiedDateTime.toInstant(ZoneOffset.UTC);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return fileName + Constants.Strings.UNIT_SEPARATOR + lastModified + Constants.Strings.FILE_SEPARATOR;
    }
}
