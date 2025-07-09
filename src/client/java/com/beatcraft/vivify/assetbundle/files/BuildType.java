package com.beatcraft.vivify.assetbundle.files;

public enum BuildType {
    Alpha,
    Patch,
    Other;

    public BuildType fromString(String type) {
        if (type.equals("a")) return Alpha;
        if (type.equals("p")) return Patch;
        return Other;
    }
}
