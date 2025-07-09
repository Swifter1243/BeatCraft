package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;

import java.io.IOException;

public class LocalSerializedObjectIdentifier {
    private int serializedIndex;
    private int fileIdentifier;

    public LocalSerializedObjectIdentifier(SerializedFileHeader header, EndianBinaryReader reader) throws IOException {
        serializedIndex = reader.readInt();
        if (header.version < 14) {
            fileIdentifier = reader.readInt();
        } else {
            reader.align(4);
            fileIdentifier = (int) reader.readLong();
        }
    }

}
