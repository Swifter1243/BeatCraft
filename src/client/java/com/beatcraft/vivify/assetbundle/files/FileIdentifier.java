package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;

import java.io.IOException;
import java.nio.file.Path;

public class FileIdentifier {
    private String path;
    private String tempEmpty = null;
    private byte[] guid = null;
    private Integer type = null;

    public String getName() {
        return Path.of(path).getFileName().toString();
    }

    public FileIdentifier(SerializedFileHeader header, EndianBinaryReader reader) throws IOException {
        if (header.version >= 6) {
            tempEmpty = reader.readCString();
        }
        if (header.version >= 5) {
            guid = reader.read(16);
            type = reader.readInt();
        }
        path = reader.readCString();
    }

}
