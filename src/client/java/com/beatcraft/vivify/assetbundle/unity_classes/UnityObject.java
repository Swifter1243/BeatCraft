package com.beatcraft.vivify.assetbundle.unity_classes;

import com.beatcraft.vivify.assetbundle.files.ObjectReader;
import com.beatcraft.vivify.assetbundle.files.SerializedFile;

public abstract class UnityObject {
    private ObjectReader objectReader = null;

    public void setObjectReader(ObjectReader reader) {
        objectReader = reader;
    }

    public SerializedFile getAssetsFile() {
        if (objectReader != null) {
            return objectReader.getAssetsFile();
        }
        return null;
    }

}
