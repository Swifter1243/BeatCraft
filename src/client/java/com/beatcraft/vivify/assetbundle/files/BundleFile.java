package com.beatcraft.vivify.assetbundle.files;

import com.beatcraft.BeatCraft;
import com.beatcraft.vivify.assetbundle.EndianBinaryReader;
import com.beatcraft.vivify.assetbundle.Readable;
import com.beatcraft.vivify.assetbundle.util.*;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.tukaani.xz.LZMAInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BundleFile extends UnityFile {

    protected record UnityVersion(int year, int major, int minor) {
        protected static UnityVersion from(String ver) {
            var parts = ver.split("\\.");

            return new UnityVersion(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2].split("[a-z]")[0])
            );
        }

        protected boolean lt(int otherYear, int otherMajor, int otherMinor) {
            if (year < otherYear) return true;
            if (year > otherYear) return false;
            if (major < otherMajor) return true;
            if (major > otherMajor) return false;
            return (minor < otherMinor);
        }
    }

    public static BundleFile tryLoadBundle(String path) {
        try (var raf = new RandomAccessFile(path, "r")) {
            return new BundleFile(new EndianBinaryReader(raf, ">", 0), null, path, false);
        } catch (IOException e) {
            BeatCraft.LOGGER.info("Failed to load asset bundle", e);
        }

        return null;
    }

    private int formatVersion;
    private String signature;
    private String engineVersion;
    private String playerVersion;
    private int flags;
    private boolean usesBlockAlignment = false;
    private int blockInfoFlags = 0;

    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }

    protected BundleFile(EndianBinaryReader reader, UnityFile parent, String name, boolean isDependency) throws IOException {
        super(parent, name, isDependency);
        signature = reader.readCString();
        formatVersion = reader.readInt();
        playerVersion = reader.readCString();
        engineVersion = reader.readCString();

        if (signature.equals("UnityFS")) {
            readFS(reader);
        }

    }

    private static final int UsesAssetBundleEncryptionOld = 0x200;
    private static final int UsesAssetBundleEncryption = 0x1400;

    private static boolean isZeros(byte[] bytes) {
        for (var b : bytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private void readFS(EndianBinaryReader reader) throws IOException {

        var size = reader.readLong();

        var raw = reader.readInt();
        var compressedSize = Integer.toUnsignedLong(raw);
        var uncompressedSize = Integer.toUnsignedLong(reader.readInt());
        var flags = reader.readInt();
        BeatCraft.LOGGER.info("size: {}  raw: {}  long: {}  decomp: {}  flags: {}", size, raw, compressedSize, uncompressedSize, Integer.toBinaryString(flags));

        var version = UnityVersion.from(engineVersion);

        var oldFlags = (version.year < 2020)
            || (version.year == 2020 && version.lt(2020, 3, 24))
            || (version.year == 2021 && version.lt(2021, 3, 2))
            || (version.year == 2022 && version.lt(2022, 1, 1));

        if (
            (oldFlags && ((flags & UsesAssetBundleEncryptionOld) > 0)) ||
            ((!oldFlags) && ((flags & UsesAssetBundleEncryption) > 0))
        ) {
            throw new IOException("Bundle is encrypted and cannot be loaded");
        }

        BeatCraft.LOGGER.info("Format version: {}", formatVersion);

        if (formatVersion >= 7) {
            reader.align(16);
            BeatCraft.LOGGER.info("Aligned cursor to {}", reader.pos());
            usesBlockAlignment = true;
        } else { // vivify uses 2021.3.16f1 or 2019.4.28f1 so this else-if is always true if reached
            var preAlign = reader.pos();
            var align_data = reader.read((int) (16 - preAlign % 16) % 16);
            if (!isZeros(align_data)) {
                reader.seek((int) preAlign);
            } else {
                usesBlockAlignment = true;
            }
        }

        var start = reader.pos();
        byte[] blocksInfoBytes;
        if ((flags & 0x80) > 0) { // BlocksInfoAtEnd
            BeatCraft.LOGGER.info("Block info is at end");
            reader.seek((int) (reader.length() - compressedSize));
            blocksInfoBytes = reader.read((int) compressedSize);
            reader.seek((int) start);
        } else {
            blocksInfoBytes = reader.read((int) compressedSize);
        }

        BeatCraft.LOGGER.info("Compressed data: {}", blocksInfoBytes);
        blocksInfoBytes = decompressData(blocksInfoBytes, (int) uncompressedSize, flags);

        BeatCraft.LOGGER.info("Decompressed data! {}", blocksInfoBytes);

        var blocksInfoReader = new EndianBinaryReader(ByteBuffer.wrap(blocksInfoBytes), ">", (int) start);

        var uncompressedHash = blocksInfoReader.read(16);
        var blocksInfoCount = blocksInfoReader.readInt();

        var blocksInfo = new ArrayList<BlockInfo>();
        for (int i = 0; i < blocksInfoCount; i++) {
            blocksInfo.add(new BlockInfo(
                Integer.toUnsignedLong(blocksInfoReader.readInt()),
                Integer.toUnsignedLong(blocksInfoReader.readInt()),
                Short.toUnsignedInt(blocksInfoReader.readShort())
            ));
        }

        var nodeCount = blocksInfoReader.readInt();
        var directoryInfo = new ArrayList<NodeInfo>();
        for (int i = 0; i < nodeCount; i++) {
            directoryInfo.add(new NodeInfo(
                blocksInfoReader.readLong(),
                blocksInfoReader.readLong(),
                blocksInfoReader.readInt(),
                blocksInfoReader.readCString()
            ));
        }

        if (!blocksInfo.isEmpty()) {
            blockInfoFlags = blocksInfo.getFirst().flags();
        }

        // 0x200: BlockInfoNeedPaddingAtStart
        if ((!oldFlags) && ((flags & 0x200) != 0)) {
            reader.align(16);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (BlockInfo blockInfo : blocksInfo) {
            byte[] compressedData = reader.read((int) blockInfo.compressedSize());
            byte[] decompressedData = decompressData(compressedData, blockInfo.uncompressedSize(), blockInfo.flags());
            outputStream.write(decompressedData);
        }

        byte[] result = outputStream.toByteArray();

        var blocksReader = new EndianBinaryReader(ByteBuffer.wrap(result), ">", blocksInfoReader.realOffset());

        // return directoryInfo, blocksReader
        readFiles(blocksReader, directoryInfo);

        for (var info : directoryInfo) {
            BeatCraft.LOGGER.info("Decompressed asset: '{}'", info.name());
        }

    }


    private byte[] decompressData(byte[] compressed, long uncompressedSize, long flags) throws IOException {
        var comp_flags = flags & 0x3F;

        var comp = CompressionType.fromInt((int) comp_flags);

        BeatCraft.LOGGER.info("Compression type: {}", comp);

        switch (comp) {
            case NONE -> {
                return compressed;
            }
            case LZHAM -> {
                throw new IOException("Decompressor not implemented: " + comp);
            }
            case LZMA -> {
                BeatCraft.LOGGER.info("Trying to decompress {} to {}", compressed.length, uncompressedSize);

                ByteArrayInputStream compressedStream = new ByteArrayInputStream(compressed);
                byte[] props = new byte[5];
                if (compressedStream.read(props) != 5) {
                    throw new IOException("Not enough data for LZMA props");
                }

                byte propsByte = props[0];
                int dictSize = (props[1] & 0xFF) |
                    ((props[2] & 0xFF) << 8) |
                    ((props[3] & 0xFF) << 16) |
                    ((props[4] & 0xFF) << 24);

                var cut = new byte[compressed.length-5];
                System.arraycopy(compressed, 5, cut, 0, compressed.length - 5);
                compressedStream = new ByteArrayInputStream(cut);

                LZMAInputStream lzmaIn = new LZMAInputStream(
                    compressedStream,
                    -1,
                    propsByte,
                    dictSize
                );

                byte[] out = new byte[(int) uncompressedSize];
                int offset = 0;
                int bytesRead;
                while ((bytesRead = lzmaIn.read(out, offset, out.length - offset)) != -1) {
                    offset += bytesRead;
                    if (offset >= out.length) break;
                }
                lzmaIn.close();

                return out;
            }
            case LZ4, LZ4HC -> {
                var decomp = new BlockLZ4CompressorInputStream(new ByteArrayInputStream(compressed));
                var out = new byte[Math.toIntExact(uncompressedSize)];
                int offset = 0;
                int bytesRead;
                while ((bytesRead = decomp.read(out, offset, out.length - offset)) != -1) {
                    offset += bytesRead;
                    if (offset >= out.length) {
                        break;
                    }
                }
                decomp.close();
                return out;
            }
        }

        return compressed;
    }




}
