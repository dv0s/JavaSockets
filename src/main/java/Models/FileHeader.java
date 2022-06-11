package Models;

import java.util.List;
import java.util.Objects;

// File header klasse waar de gegevens van het "te versturen" bestand in worden verzonden van punt A naar punt B.
public class FileHeader implements Comparable<FileHeader>{
    public String name;
    public long size;
    public String extension;
    public String checksumAlgorithm;
    public String checksum;

    public FileHeader(){}

    public FileHeader(String name, String extension, long size, String checksumAlgorithm, String checksum)
    {
        super();
        this.name = name;
        this.size = size;
        this.extension = extension;
        this.checksumAlgorithm = checksumAlgorithm;
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getExtension() {
        return extension;
    }

    public String getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public int compareTo(FileHeader o) {
        if (!Objects.equals(name, o.name))
            return 0;
        if (!Objects.equals(size, o.size))
            return 0;
        if (!Objects.equals(extension, o.extension))
            return 0;
        if (!Objects.equals(checksum, o.checksum))
            return 0;

        return 1;
    }

    @Override
    public String toString()
    {
        return "FileHeader:" + this.name +
                ":" + this.extension +
                ":" + this.size +
                ":" + this.checksumAlgorithm +
                ":" + this.checksum;
    }

    public FileHeader createFromString(String str)
    {
        String[] values = str.split(":");
        return new FileHeader(values[1],values[2],Long.parseLong(values[3]),values[4],values[5]);
    }
}
