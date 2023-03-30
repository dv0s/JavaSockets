package protocol.data;

import protocol.enums.Constants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FileMetaData {

    public String fileName;
    public LocalDateTime lastModified;
    public long fileSize;

    public FileMetaData(String fileName, String lastModified, String fileSize){
        this.fileName = fileName;
        this.lastModified = convertStringToLocalDateTime(lastModified);
        this.fileSize = Long.parseLong(fileSize);
    }

    // region Getters
    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public long getFileSize() {
        return fileSize;
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

    public int compareSize(long oFileSize){
        if(fileSize == oFileSize){
            return 0;
        } else if (fileSize > oFileSize) {
            return 1;
        }

        return -1;
    }

    private LocalDateTime convertStringToLocalDateTime(String lastModified){
        DateTimeFormatter formatter =DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT.toString());
        return LocalDateTime.parse(lastModified, formatter);
    }


}
