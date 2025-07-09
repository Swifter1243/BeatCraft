package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import org.joml.Vector2i;

import java.util.Optional;

public class ObjectReader {

    public EndianBinaryReader reader;
    public byte[] data;
    public Ver4 version;
    public int version2;
    public BuildTarget platform;
    public BuildType buildType;
    public int pathId;
    public Vector2i byteStartOffset;
    public int byteStart;
    public Vector2i byteSizeOffset;
    public int byteSize;
    public int typeId;
    public SerializedType serializedType;
    public int classId;
    public ClassIdType type;
    public Integer isDestroyed;
    public Integer isStripped;


    public ObjectReader(SerializedFile file, EndianBinaryReader reader) {

    }

    public SerializedFile getAssetsFile() {
        return null;
    }

    public int getPathId() {
        return 0;
    }

}
