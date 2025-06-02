package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.BeatCraft;
import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import com.beatcraft.vivify.assetbundle.Readable;
import com.beatcraft.vivify.assetbundle.util.FileType;
import com.beatcraft.vivify.assetbundle.util.NodeInfo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public abstract class UnityFile implements Readable {

    private String name;
    private HashMap<String, Readable> files = new HashMap<>();
    private boolean isDependency;
    private UnityFile parent;

    @Override
    public String getName() {
        return name;
    }

    public UnityFile(@Nullable UnityFile parent, @Nullable String name) {
        this(parent, name, false);
    }

    public UnityFile(@Nullable UnityFile parent, @Nullable String name, boolean isDependency) {
        this.parent = parent;
        this.name = name;
        this.isDependency = isDependency;
    }

    public List<Readable> getAssets() {
        if (this instanceof SerializedFile) {
            return List.of(this);
        }
        var out = new ArrayList<Readable>();
        for (var f : files.values()) {
            if (f instanceof BundleFile || f instanceof WebFile) {
                out.addAll(((UnityFile) f).getAssets());
            }
            else if (f instanceof SerializedFile) {
                out.add(f);
            }
        }
        return out;
    }


    public boolean isDependency() {
        return isDependency;
    }


    protected static FileType checkFileType(EndianBinaryReader reader) throws IOException {
        if (reader.length() < 20) {
            return FileType.ResourceFile;
        }

        var signature = reader.readCString(20);
        reader.seek(0);

        var b = String.valueOf(new char[]{0xfa, 0xfa, 0xfa, 0xfa, 0xfa, 0xfa, 0xfa, 0xfa});
        if (signature.matches("((Unity(Web|Raw|FS))|" + b + "{8})")) {
            return FileType.BundleFile;
        } else if (signature.startsWith("UnityWebData")) {
            return FileType.WebFile;
        } else if (signature.equals(Arrays.toString(new byte[]{'P', 'K', 0x03, 0x04}))) {
            return FileType.ZIP;
        } else {
            if (reader.length() < 128) {
                return FileType.ResourceFile;
            }

            var magic = reader.read(2);
            reader.seek(0);
            if (magic[0] == 0x1f && magic[1] == (byte) 0x8b) { // GZIP
                return FileType.WebFile;
            }
            reader.seek(0x20);
            magic = reader.read(6);
            reader.seek(0);
            if (Arrays.toString(magic).equals("brotli")) {
                return FileType.WebFile;
            }

            var oldEndian = reader.getEndian();

            var metadataSize = reader.readInt();
            var fileSize = reader.readInt();
            var version = reader.readInt();
            var dataOffset = reader.readInt();

            if (version >= 22) {
                var endian = (reader.read(1)[0] != 0) ? ">" : "<";
                reader.read(3);
                metadataSize = reader.readInt();
                fileSize = (int) reader.readLong();
                dataOffset = (int) reader.readLong();
                reader.readLong();
            }


            reader.setEndian(oldEndian);
            reader.seek(0);
            if (
                version < 0 || version > 100
                || fileSize < 0 || fileSize > reader.length()
                || metadataSize < 0 || metadataSize > reader.length()
                || dataOffset < 0 || dataOffset > reader.length()
                || fileSize < metadataSize
                || fileSize < dataOffset
            ) {
                return FileType.ResourceFile;
            } else {
                return FileType.AssetsFile;
            }

        }

    }

    private static final Pattern extensions = Pattern.compile("\\.(resS|resource|config|xml|dat)$");
    protected static Readable parseFile(EndianBinaryReader reader, UnityFile parent, String name, FileType type, boolean isDependency) throws IOException {
        if (type == null) {
            type = checkFileType(reader);
            BeatCraft.LOGGER.info("File type has been determined to be: {}", type);
        }
        if (type == FileType.AssetsFile && !extensions.matcher(name).find()) {
            return new SerializedFile(reader, parent, name, isDependency);
        } else if (type == FileType.BundleFile) {
            return new BundleFile(reader, parent, name, isDependency);
        } else if (type == FileType.WebFile) {
            return new WebFile(reader, parent, name, isDependency);
        } else {
            return reader;
        }

    }


    protected void readFiles(EndianBinaryReader reader, ArrayList<NodeInfo> files) throws IOException {
        for (var node : files) {
            reader.seek((int) node.offset());
            var name = node.name();
            var nodeData = reader.read((int) node.size());
            var nodeReader = new EndianBinaryReader(
                ByteBuffer.wrap(nodeData), ">", reader.baseOffset() + (int) node.offset()
            );

            var f = parseFile(
                nodeReader, this, name, null, false
            );

            f.setFlags(node.flags());
            this.files.put(name, f);
        }
    }

}
