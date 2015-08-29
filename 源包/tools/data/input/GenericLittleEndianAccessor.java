package tools.data.input;

import constants.ServerConstants;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class GenericLittleEndianAccessor implements LittleEndianAccessor {

    private final ByteInputStream bs;

    public GenericLittleEndianAccessor(ByteInputStream bs) {
        this.bs = bs;
    }

    @Override
    public int readByteAsInt() {
        return this.bs.readByte();
    }

    @Override
    public byte readByte() {
        return (byte) this.bs.readByte();
    }

    @Override
    public int readInt() {
        int byte1 = this.bs.readByte();
        int byte2 = this.bs.readByte();
        int byte3 = this.bs.readByte();
        int byte4 = this.bs.readByte();
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    @Override
    public short readShort() {
        int byte1 = this.bs.readByte();
        int byte2 = this.bs.readByte();
        return (short) ((byte2 << 8) + byte1);
    }

    @Override
    public int readUShort() {
        int quest = readShort();
        if (quest < 0) {
            quest += 65536;
        }
        return quest;
    }

    @Override
    public char readChar() {
        return (char) readShort();
    }

    @Override
    public long readLong() {
        long byte1 = this.bs.readByte();
        long byte2 = this.bs.readByte();
        long byte3 = this.bs.readByte();
        long byte4 = this.bs.readByte();
        long byte5 = this.bs.readByte();
        long byte6 = this.bs.readByte();
        long byte7 = this.bs.readByte();
        long byte8 = this.bs.readByte();
        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readAsciiString(int n) {
        byte[] ret = new byte[n];
        for (int x = 0; x < n; x++) {
            ret[x] = readByte();
        }
        try {
            return new String(ret, ServerConstants.MAPLE_TYPE.getAscii());
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
        }
        return "";
    }

    @Override
    public long getBytesRead() {
        return this.bs.getBytesRead();
    }

    @Override
    public String readMapleAsciiString() {
        return readAsciiString(readShort());
    }

    @Override
    public Point readPos() {
        int x = readShort();
        int y = readShort();
        return new Point(x, y);
    }

    @Override
    public byte[] read(int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    @Override
    public void skip(int num) {
        for (int x = 0; x < num; x++) {
            readByte();
        }
    }

    @Override
    public long available() {
        return this.bs.available();
    }

    @Override
    public String toString() {
        return this.bs.toString();
    }

    @Override
    public String toString(boolean b) {
        return this.bs.toString(b);
    }
    
    /**
     * Reads a null-terminated string from the stream.
     *
     * @return The string read.
     */
    @Override
    public final String readNullTerminatedAsciiString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b;
        while (true) {
            b = readByte();
            if (b == 0) {
                break;
            }
            baos.write(b);
        }
        byte[] buf = baos.toByteArray();
        char[] chrBuf = new char[buf.length];
        for (int x = 0; x < buf.length; x++) {
            chrBuf[x] = (char) buf[x];
        }
        return String.valueOf(chrBuf);
    }
}
