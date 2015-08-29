package tools.data.output;

import java.awt.Point;
import java.awt.Rectangle;

public abstract interface LittleEndianWriter {

    public abstract void writeZero(int paramInt);

    public abstract void write(byte[] paramArrayOfByte);

    public abstract void write(byte paramByte);

    public abstract void write(int paramInt);

    public abstract void writeInt(int paramInt);

    public abstract void writeReversedInt(long paramLong);

    public abstract void writeShort(short paramShort);

    public abstract void writeShort(int paramInt);

    public abstract void writeLong(long paramLong);

    public abstract void writeReversedLong(long paramLong);

    public abstract void writeAsciiString(String paramString);

    public abstract void writeAsciiString(String paramString, int paramInt);

    public abstract void writeMapleNameString(String paramString);

    public abstract void writePos(Point paramPoint);

    public abstract void writeRect(Rectangle paramRectangle);

    public abstract void writeMapleAsciiString(String paramString);

    public abstract void writeBool(boolean paramBoolean);
}
