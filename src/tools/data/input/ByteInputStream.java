package tools.data.input;

public abstract interface ByteInputStream {

    public abstract int readByte();

    public abstract long getBytesRead();

    public abstract long available();

    public abstract String toString(boolean paramBoolean);
}
