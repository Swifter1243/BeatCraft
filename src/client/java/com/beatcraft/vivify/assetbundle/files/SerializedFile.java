package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import org.jetbrains.annotations.Nullable;

public class SerializedFile extends UnityFile {
    public SerializedFile(EndianBinaryReader reader, @Nullable UnityFile parent, @Nullable String name, boolean isDependency) {
        super(parent, name, isDependency);

    }

    @Override
    public void setFlags(int f) {

    }
}
