package com.beatcraft.vivify.assetbundle.util;

public enum CompressionType {
    NONE(0),
    LZMA(1),
    LZ4(2),
    LZ4HC(3),
    LZHAM(4)
    ;

    private int val;
    CompressionType(int v) {
        val = v;
    }

    public static CompressionType fromInt(int i) {
        for (var type : values()) {
            if (type.val == i) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown compression type: " + i);
    }
}
