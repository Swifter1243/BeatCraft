package com.beatcraft.vivify.assetbundle;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EndianBinaryReader implements Readable {

    private BackendWrapper reader;
    private ByteOrder endian;
    private long length;
    private long position;
    private long baseOffset;

    @Override
    public void setFlags(int f) {

    }

    @Override
    public String getName() {
        return "<Binary Reader>";
    }

    public EndianBinaryReader(RandomAccessFile file, String endian, int offset) {
        this(BackendWrapper.from(file), endian, offset);
    }

    public EndianBinaryReader(ByteBuffer buffer, String endian, int offset) {
        this(BackendWrapper.from(buffer), endian, offset);
    }

    private EndianBinaryReader(BackendWrapper reader, String endian, int offset) {
        this.reader = reader;
        this.endian = endian.equals(">") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        baseOffset = offset;
    }

    private byte[] reorder(byte[] bytes) {
        if (endian == ByteOrder.LITTLE_ENDIAN) {
            var out = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                out[i] = bytes[bytes.length - 1 - i];
            }
            return out;
        }
        return bytes;
    }

    public byte[] read(int length) throws IOException {
        return reader.read(length);
    }

    public boolean readBoolean() throws IOException {
        return reader.read(1)[0] != 0;
    }

    public String readCString(int maxLength) throws IOException {
        var sb = new StringBuilder();

        while (sb.length() < maxLength && pos() != length()) {
            var b = read(1);
            if (b.length == 0) {
                throw new EOFException("Unterminated string");
            }
            if (b[0] == 0) {
                break;
            }
            sb.append((char) b[0]);
        }

        return sb.toString();
    }

    public String readCString() throws IOException {
        return readCString(32767);
    }

    public short readShort() throws IOException {
        var bytes = reorder(read(2));

        short ret = 0;
        for (byte b : bytes) {
            ret <<= 8;
            ret |= (short) Byte.toUnsignedInt(b);
        }
        return ret;
    }

    public int readInt() throws IOException {
        var bytes = reorder(read(4));

        int ret = 0;
        for (byte b : bytes) {
            ret <<= 8;
            ret |= Byte.toUnsignedInt(b);
        }
        return ret;
    }

    public long readLong() throws IOException {
        var bytes = reorder(read(8));

        long ret = 0;
        for (byte b : bytes) {
            ret <<= 8;
            ret |= Byte.toUnsignedInt(b);
        }
        return ret;
    }

    public int[] readIntArray(int length) throws IOException {
        if (length < 0) {
            length = readInt();
        }
        var out = new int[length];
        for (int i = 0; i < length; i++) {
            out[i] = readInt();
        }
        return out;
    }

    public int[] readIntArray() throws IOException {
        return readIntArray(-1);
    }

    public void align(int alignment) throws IOException {
        reader.align(alignment);
    }

    public long pos() throws IOException {
        return reader.pos();
    }

    public void seek(int pos) throws IOException {
        reader.seek(pos);
    }

    public long length() throws IOException {
        return reader.length();
    }

    public int realOffset() throws IOException {
        return (int) (baseOffset + reader.pos());
    }

    public int baseOffset() {
        return (int) baseOffset;
    }

    public ByteOrder getEndian() {
        return endian;
    }

    public void setEndian(ByteOrder endian) {
        this.endian = endian;
    }

}

interface BackendWrapper {
    static BackendWrapper from(RandomAccessFile file) {
        return new FileReaderBackend(file);
    }
    static BackendWrapper from(ByteBuffer buffer) {
        return new BufferReaderBackend(buffer);
    }

    byte[] read(int length) throws IOException;
    void align(int alignment) throws IOException;
    long pos() throws IOException;
    void seek(int pos) throws IOException;
    long length() throws IOException;
}

class FileReaderBackend implements BackendWrapper {
    private final RandomAccessFile file;
    protected FileReaderBackend(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public byte[] read(int length) throws IOException {
        var out = new byte[length];
        file.read(out);
        return out;
    }

    @Override
    public void align(int alignment) throws IOException {
        var cur = file.getFilePointer();
        cur += (alignment - cur % alignment) % alignment;
        file.seek(cur);
    }

    @Override
    public long pos() throws IOException {
        return file.getFilePointer();
    }

    @Override
    public void seek(int pos) throws IOException {
        file.seek(pos);
    }

    @Override
    public long length() throws IOException {
        return file.length();
    }
}

class BufferReaderBackend implements BackendWrapper {
    private final ByteBuffer buffer;
    protected BufferReaderBackend(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public byte[] read(int length) throws IOException {
        var out = new byte[length];
        buffer.get(out);
        return out;
    }

    @Override
    public void align(int alignment) throws IOException {
        var cur = buffer.position();
        cur += (alignment - cur % alignment) % alignment;
        buffer.position(cur);
    }

    @Override
    public long pos() throws IOException {
        return buffer.position();
    }

    @Override
    public void seek(int pos) throws IOException {
        buffer.position(pos);
    }

    @Override
    public long length() throws IOException {
        return buffer.limit();
    }
}

