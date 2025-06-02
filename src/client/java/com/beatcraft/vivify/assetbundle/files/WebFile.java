package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import org.jetbrains.annotations.Nullable;

public class WebFile extends UnityFile {
    public WebFile(EndianBinaryReader reader, @Nullable UnityFile parent, @Nullable String name, boolean isDependency) {
        super(parent, name, isDependency);
    }

    @Override
    public void setFlags(int f) {

    }
}
