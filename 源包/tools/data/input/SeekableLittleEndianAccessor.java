package tools.data.input;

public abstract interface SeekableLittleEndianAccessor extends LittleEndianAccessor {

    public abstract void seek(long paramLong);

    public abstract long getPosition();
}
