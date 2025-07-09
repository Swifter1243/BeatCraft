package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import com.beatcraft.vivify.assetbundle.type_tree.TypeTreeNode;

import java.io.IOException;

public class SerializedType {
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
