package com.beatcraft.vivify;

import com.beatcraft.vivify.assetbundle.files.ClassIdType;

public class VivifyController {
    // this asset loader code is a port of the python code from https://github.com/K0lb3/UnityPy/tree/master/UnityPy

    public static void init() {
        ClassIdType.init(); // setup lookup map
    }

}
