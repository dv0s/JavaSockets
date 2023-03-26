package protocol.data;

import protocol.enums.Constants;

import java.util.Objects;

public class FileHeader{
    public String fileName;
    public String lastModified;
    public long fileSize;
    public String hashAlgo;
    public String checkSum;

    public FileHeader(){}

    public FileHeader(String fileName, String lastModified, long fileSize, String hashAlgo, String checkSum){
        super();

        this.fileName = fileName;
        this.lastModified = lastModified;
        this.fileSize = fileSize;
        this.hashAlgo = hashAlgo;
        this.checkSum = checkSum;
    }

    //region Getters

    public String getFileName() {
        return fileName;
    }

    public String getLastModified() {
        return lastModified;
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

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
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
        if (!Objects.equals(lastModified, o.lastModified))
            return false;
        if (!Objects.equals(checkSum, o.checkSum))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "FileHeader" + Constants.UNIT_SEPARATOR +
                fileName + Constants.UNIT_SEPARATOR +
                lastModified + Constants.UNIT_SEPARATOR +
                fileSize + Constants.UNIT_SEPARATOR +
                hashAlgo + Constants.UNIT_SEPARATOR +
                checkSum;
    }

    //endregion
}
