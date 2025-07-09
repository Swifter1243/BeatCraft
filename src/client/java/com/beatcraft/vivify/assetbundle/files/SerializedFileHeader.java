package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;

import java.io.IOException;
import java.nio.ByteOrder;

public class SerializedFileHeader {
    public int metadataSize;
    public int fileSize;
    public int version;
    public int dataOffset;
    public ByteOrder endian;
    public byte[] reserved;

    public SerializedFileHeader(EndianBinaryReader reader) throws IOException {
        metadataSize = reader.readInt();
        fileSize = reader.readInt();
        version = reader.readInt();
        dataOffset = reader.readInt();
    }

}
