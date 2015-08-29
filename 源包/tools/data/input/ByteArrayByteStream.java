package tools.data.input;

import java.io.IOException;
import tools.HexTool;

public class ByteArrayByteStream implements SeekableInputStreamBytestream {

    private int pos = 0;
    private long bytesRead = 0L;
    private final byte[] arr;

    public ByteArrayByteStream(byte[] arr) {
        this.arr = arr;
    }

    @Override
    public long getPosition() {
        return this.pos;
    }

    @Override
    public void seek(long offset)
            throws IOException {
        this.pos = (int) offset;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public int readByte() {
        this.bytesRead += 1L;
        return this.arr[(this.pos++)] & 0xFF;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean b) {
        String nows = "";
        if (this.arr.length - this.pos > 0) {
            byte[] now = new byte[this.arr.length - this.pos];
            System.arraycopy(this.arr, this.pos, now, 0, this.arr.length - this.pos);
            nows = HexTool.toString(now);
        }
        if (b) {
            return "\r\n所有: " + HexTool.toString(this.arr) + "\r\n现在: " + nows;
        }
        return "\r\n封包: " + nows;
    }

    @Override
    public long available() {
        return this.arr.length - this.pos;
    }
}
