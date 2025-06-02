package com.beatcraft.vivify.assetbundle.util;

public enum FileType {
    AssetsFile(0),
    BundleFile(1),
    WebFile(2),
    ResourceFile(9),
    ZIP(10);

    private final int v;
    FileType(int v) {
        this.v = v;
    }
}
