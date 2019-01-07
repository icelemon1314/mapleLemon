package tools.data.input;

import java.awt.Point;

public abstract interface LittleEndianAccessor {

    public abstract byte readByte();

    public abstract int readByteAsInt();

    public abstract char readChar();

    public abstract short readShort();

    public abstract int readUShort();

    public abstract int readInt();

    public abstract long readLong();

    public abstract void skip(int paramInt);

    public abstract byte[] read(int paramInt);

    public abstract float readFloat();

    public abstract double readDouble();

    public abstract String readAsciiString(int paramInt);

    public abstract String readMapleAsciiString();

    public abstract Point readPos();

    public abstract long getBytesRead();

    public abstract long available();

    public abstract String toString(boolean paramBoolean);
    
    public abstract String readNullTerminatedAsciiString();
}
