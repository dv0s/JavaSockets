package protocol.data;

import java.util.Objects;

public class FileHeader implements Comparable<FileHeader>{
    public String fileName;
    public String fileType;
    public String fileSize;
    public String hashAlgo;
    public String checkSum;

    public FileHeader(){}

    public FileHeader(String fileName, String fileType, String fileSize, String hashAlgo, String checkSum){
        super();

        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.hashAlgo = hashAlgo;
        this.checkSum = checkSum;
    }

    // Getters & Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public void setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    @Override
    public int compareTo(FileHeader o) {
        if (!Objects.equals(fileName, o.fileName))
            return 0;
        if (!Objects.equals(fileSize, o.fileSize))
            return 0;
        if (!Objects.equals(fileType, o.fileType))
            return 0;
        if (!Objects.equals(checkSum, o.checkSum))
            return 0;

        return 1;
    }

    @Override
    public String toString() {
        return "Filename: " + fileName + "\n" +
                "Filetype: " + fileType + "\n" +
                "Filesize: " + fileSize + "\n" +
                "HashAlgo: " + hashAlgo + "\n" +
                "CheckSum: " + checkSum + "\n\n";
    }
}
