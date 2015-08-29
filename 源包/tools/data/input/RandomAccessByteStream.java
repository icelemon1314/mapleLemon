package tools.data.input;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessByteStream implements SeekableInputStreamBytestream {

    private final RandomAccessFile raf;
    private long read = 0L;

    public RandomAccessByteStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public int readByte() {
        try {
            int temp = this.raf.read();
            if (temp == -1) {
                throw new RuntimeException("EOF");
            }
            this.read += 1L;
            return temp;
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void seek(long offset)
            throws IOException {
        this.raf.seek(offset);
    }

    @Override
    public long getPosition()
            throws IOException {
        return this.raf.getFilePointer();
    }

    @Override
    public long getBytesRead() {
        return this.read;
    }

    @Override
    public long available() {
        try {
            return this.raf.length() - this.raf.getFilePointer();
        } catch (IOException e) {
            System.err.println("ERROR" + e);
        }
        return 0L;
    }

    @Override
    public String toString(boolean b) {
        return toString();
    }
}
