package tools.data.output;

import constants.ServerConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.charset.Charset;

import tools.HexTool;

public class GenericLittleEndianWriter implements LittleEndianWriter {

    private static final Charset ASCII = Charset.forName(ServerConstants.MAPLE_TYPE.getAscii());
    private ByteOutputStream bos;

    protected void setByteOutputStream(ByteOutputStream bos) {
        this.bos = bos;
    }

    public GenericLittleEndianWriter() {
    }

    public GenericLittleEndianWriter(ByteOutputStream bos) {
        this.bos = bos;
    }

    private void baosWrite(byte b) {
        bos.writeByte(b);
    }

    @Override
    public void writeZero(int i) {
        for (int x = 0; x < i; x++) {
            baosWrite((byte) 0);
        }
//        write(new byte[i]);
    }

    @Override
    public void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            baosWrite(b[x]);
        }
    }

    @Override
    public void write(byte b) {
        baosWrite(b);
    }

    @Override
    public void write(int b) {
        baosWrite((byte) b);
    }

    @Override
    public void writeShort(short i) {
        baosWrite((byte) (i & 0xFF));
        baosWrite((byte) (i >>> 8 & 0xFF));
    }

    @Override
    public void writeShort(int i) {
        baosWrite((byte) (i & 0xFF));
        baosWrite((byte) (i >>> 8 & 0xFF));
    }

    @Override
    public void writeInt(int i) {
        baosWrite((byte) (i & 0xFF));
        baosWrite((byte) (i >>> 8 & 0xFF));
        baosWrite((byte) (i >>> 16 & 0xFF));
        baosWrite((byte) (i >>> 24 & 0xFF));
    }

    @Override
    public void writeReversedInt(long l) {
        baosWrite((byte) (int) (l >>> 32 & 0xFF));
        baosWrite((byte) (int) (l >>> 40 & 0xFF));
        baosWrite((byte) (int) (l >>> 48 & 0xFF));
        baosWrite((byte) (int) (l >>> 56 & 0xFF));
    }

    @Override
    public void writeAsciiString(String s) {
        write(s.getBytes(ASCII));
    }

    @Override
    public void writeAsciiString(String s, int max) {
        if (s.getBytes(ASCII).length > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.getBytes(ASCII).length; i < max; i++) {
            baosWrite((byte) 0);
        }
    }

    @Override
    public void writeMapleNameString(String s) {
        if (s.getBytes().length > 12) {
            s = s.substring(0, 12);
        }
        writeAsciiString(s);
        for (int x = s.getBytes().length; x < 12; x++) {
            baosWrite((byte) 0);
        }
    }

    @Override
    public void writeMapleAsciiString(String s) {
        if (s == null) {
            writeShort(0);
            return;
        }
        writeShort((short) s.getBytes(ASCII).length);
        writeAsciiString(s);
    }

    public void writeMapleAsciiString(String[] s) {
        int len = 0;
        for (String str : s) {
            if (str != null) {
                len += str.getBytes(ASCII).length;
            }
        }
        if (len < 1) {
            writeShort(0);
            return;
        }
        len += s.length - 1;
        writeShort((short) len);
        for (int i = 0; i < s.length; i++) {
            if (s[i] != null) {
                writeAsciiString(s[i]);
            }
            if (i < s.length - 1) {
                write(0);
            }
        }
    }

    @Override
    public void writePos(Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    /**
     * 输出一个矩形区域
     * @param s
     */
    @Override
    public void writeRect(Rectangle s) {
        writeInt(s.x);
        writeInt(s.y);
        writeInt(s.x + s.width);
        writeInt(s.y + s.height);
    }

    @Override
    public void writeLong(long l) {
        baosWrite((byte) (int) (l & 0xFF));
        baosWrite((byte) (int) (l >>> 8 & 0xFF));
        baosWrite((byte) (int) (l >>> 16 & 0xFF));
        baosWrite((byte) (int) (l >>> 24 & 0xFF));
        baosWrite((byte) (int) (l >>> 32 & 0xFF));
        baosWrite((byte) (int) (l >>> 40 & 0xFF));
        baosWrite((byte) (int) (l >>> 48 & 0xFF));
        baosWrite((byte) (int) (l >>> 56 & 0xFF));
    }

    @Override
    public void writeReversedLong(long l) {
        baosWrite((byte) (int) (l >>> 32 & 0xFF));
        baosWrite((byte) (int) (l >>> 40 & 0xFF));
        baosWrite((byte) (int) (l >>> 48 & 0xFF));
        baosWrite((byte) (int) (l >>> 56 & 0xFF));
        baosWrite((byte) (int) (l & 0xFF));
        baosWrite((byte) (int) (l >>> 8 & 0xFF));
        baosWrite((byte) (int) (l >>> 16 & 0xFF));
        baosWrite((byte) (int) (l >>> 24 & 0xFF));
    }

    @Override
    public void writeBool(boolean b) {
        write(b ? 1 : 0);
    }
}
