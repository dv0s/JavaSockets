package protocol.data;

import java.util.Objects;

public class FileHeader{
    public String fileName;
    public String fileType;
    public long fileSize;
    public String hashAlgo;
    public String checkSum;

    public FileHeader(){}

    public FileHeader(String fileName, String fileType, long fileSize, String hashAlgo, String checkSum){
        super();

        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.hashAlgo = hashAlgo;
        this.checkSum = checkSum;
    }

    //region Getters

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public String getCheckSum() {
        return checkSum;
    }

    //endregion

    //region Setters

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    //endregion

    //region Methods

    public boolean compare(FileHeader o) {
        if (!Objects.equals(fileName, o.fileName))
            return false;
        if (!Objects.equals(fileSize, o.fileSize))
            return false;
        if (!Objects.equals(fileType, o.fileType))
            return false;
        if (!Objects.equals(checkSum, o.checkSum))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Fileheader\n" +
                "Filename: " + fileName + "\n" +
                "Filetype: " + fileType + "\n" +
                "Filesize: " + fileSize + "\n" +
                "HashAlgo: " + hashAlgo + "\n" +
                "CheckSum: " + checkSum + "\n";
    }

    //endregion
}
