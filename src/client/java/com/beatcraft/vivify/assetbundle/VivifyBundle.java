package com.beatcraft.vivify.assetbundle;

import com.beatcraft.exceptions.FileFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class VivifyBundle {
    private String signature;
    private int formatVersion;
    private String unityVersion;
    private String generatorVersion;

    private long fileSize;

    private static String readString(RandomAccessFile bundle) throws IOException {
        var s = new StringBuilder();

        while (true) {
            var b = bundle.read();
            if (b == -1) {
                throw new IOException("End of data stream reached while reading string");
            } else if (b < 0 || 127 < b) {
                throw new IOException("Invalid unicode while reading string");
            } else if (b == 0) {
                break;
            }
            s.append((char) b);
        }
        return s.toString();
    }

    public boolean isUnityFs() {
        return signature.equals("UnityFs");
    }

    public boolean isUnityRaw() {
        return signature.equals("UnityRaw");
    }

    public boolean isUnityWeb() {
        return signature.equals("UnityWeb");
    }

    public VivifyBundle(RandomAccessFile bundle) throws IOException {
        bundle.seek(0);
        String header;
        try {
            header = readString(bundle);
        } catch (IOException e) {
            throw new FileFormatException("File is not a valid vivify bundle", e);
        }

        if (!header.startsWith("Unity")) {
            throw new FileFormatException("File is not a valid vivify bundle");
        }

        signature = header;
        formatVersion = bundle.readInt();
        unityVersion = readString(bundle);
        generatorVersion = readString(bundle);

        if (isUnityFs()) {
            loadUnityFs(bundle);
        } else if (isUnityRaw() || isUnityWeb()) {
            loadUnityRaw(bundle);
        } else {
            throw new FileFormatException("Unrecognized bundle format: " + signature);
        }
    }


    private void loadUnityFs(RandomAccessFile bundle) throws IOException {
        fileSize = bundle.readLong();
    }


    private void loadUnityRaw(RandomAccessFile bundle) {

    }

}
