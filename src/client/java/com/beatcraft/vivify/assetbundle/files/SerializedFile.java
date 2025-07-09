package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import com.beatcraft.vivify.assetbundle.type_tree.TypeTreeNode;
import com.beatcraft.vivify.assetbundle.unity_classes.UnityObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializedFile extends UnityFile {
    public EndianBinaryReader reader;
    public Ver4 version;
    public String unityVersion;
    public BuildType buildType;
    public int targetPlatform; // https://github.com/K0lb3/UnityPy/blob/master/UnityPy/enums/BuildTarget.py#L4
    public boolean enableTypeTree;
    public ArrayList<SerializedType> types = new ArrayList<>();
    public ArrayList<LocalSerializedObjectIdentifier> scriptTypes = new ArrayList<>();
    public ArrayList<FileIdentifier> externals = new ArrayList<>();
    public ArrayList<SerializedType> refTypes = new ArrayList<>();
    public HashMap<Integer, ObjectReader> objects = new HashMap<>();
    public int unknown = 0;
    public SerializedFileHeader header;
    public int mTargetPlatform;
    public int bigIdEnabled;
    public String userInfo;
    //private AssetBundle assetBundle;
    public HashMap<String, UnityObject> cache = new HashMap<>();


    public SerializedFile(EndianBinaryReader reader, @Nullable UnityFile parent, @Nullable String name, boolean isDependency) throws IOException {
        super(parent, name, isDependency);
        this.reader = reader;

        header = new SerializedFileHeader(reader);

        if (header.version >= 9) {
            header.endian = reader.readBoolean() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            header.reserved = reader.read(3);
            if (header.version >= 22) {
                header.metadataSize = reader.readInt();
                header.fileSize = (int) reader.readLong();
                header.dataOffset = (int) reader.readLong();
                unknown = (int) reader.readLong();
            }
        } else {
            reader.seek(header.fileSize - header.metadataSize);
            header.endian = reader.readBoolean() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        }

        reader.setEndian(header.endian);

        if (header.version >= 7) {
            unityVersion = reader.readCString();
            updateVersion();
        }

        if (header.version >= 8) {
            mTargetPlatform = targetPlatform = reader.readInt();
        }

        if (header.version >= 13) {
            enableTypeTree = reader.readBoolean();
        }

        var typeCount = reader.readInt();

        for (int i = 0; i < typeCount; i++) {
            types.add(new SerializedType(reader, this, false));
        }

        bigIdEnabled = 0;
        if (7 <= header.version && header.version < 14) {
            bigIdEnabled = reader.readInt();
        }

        var objectCount = reader.readInt();

        for (int i = 0; i < objectCount; i++) {
            var obj = new ObjectReader(this, reader);
            objects.put(obj.getPathId(), obj);
        }

        if (header.version >= 11) {
            var scriptCount = reader.readInt();
            for (int i = 0; i < scriptCount; i++) {
                scriptTypes.add(new LocalSerializedObjectIdentifier(header, reader));
            }
        }

        var externalsCount = reader.readInt();
        for (int i = 0; i < externalsCount; i++) {
            externals.add(new FileIdentifier(header, reader));
        }

        if (header.version >= 20) {
            var refTypeCount = reader.readInt();
            for (int i = 0; i < refTypeCount; i++) {
                refTypes.add(new SerializedType(reader, this, true));
            }
        }

        if (header.version >= 5) {
            userInfo = reader.readCString();
        }

        for (var obj : objects.values()) {
            // TODO: continue from here: https://github.com/K0lb3/UnityPy/blob/master/UnityPy/files/SerializedFile.py#L342
        }


    }

    private void updateVersion() {

    }

    @Override
    public void setFlags(int f) {

    }
}
