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

class SerializedFileHeader {
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

class LocalSerializedObjectIdentifier {
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

class FileIdentifier {
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

enum BuildType {
    Alpha,
    Patch,
    Other;

    public BuildType fromString(String type) {
        if (type.equals("a")) return Alpha;
        if (type.equals("p")) return Patch;
        return Other;
    }
}

class SerializedType {
    private int classId;
    private Boolean isStrippedType = null;
    private int scriptTypeIndex = -1;
    private byte[] scriptId = null;
    private byte[] oldTypeHash = null;
    private TypeTreeNode node = null;

    private String mClassName = null;
    private String mNamespace = null;
    private String mAssemblyName = null;

    private int[] typeDependencies = null;

    public SerializedType(EndianBinaryReader reader, SerializedFile file, boolean isRefType) throws IOException {
        var version = file.header.version;
        classId = reader.readInt();

        if (version >= 16) {
            isStrippedType = reader.readBoolean();
        }

        if (version >= 17) {
            scriptTypeIndex = reader.readShort();
        }

        if (version >= 13) {
            if (
                (isRefType && scriptTypeIndex >= 0)
                || (version < 16 && classId < 0)
                || (version >= 16 && classId == 114)
            ) {
                scriptId = reader.read(16);
            }
            oldTypeHash = reader.read(16);
        }

        if (file.enableTypeTree) {
            if (version >= 12 || version == 10) {
                node = TypeTreeNode.parseBlob(reader, version);
            } else {
                node = TypeTreeNode.parse(reader, version);
            }

            if (version >= 21) {
                if (isRefType) {
                    mClassName = reader.readCString();
                    mNamespace = reader.readCString();
                    mAssemblyName = reader.readCString();
                } else {
                    typeDependencies = reader.readIntArray();
                }
            }
        }

    }

}

record Ver4(int a, int b, int c, int d) {

}

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
