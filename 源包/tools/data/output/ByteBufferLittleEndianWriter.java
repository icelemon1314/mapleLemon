package tools.data.output;

import org.apache.mina.core.buffer.IoBuffer;

public class ByteBufferLittleEndianWriter extends GenericLittleEndianWriter {

    private IoBuffer bb;

    public ByteBufferLittleEndianWriter() {
        this(50, true);
    }

    public ByteBufferLittleEndianWriter(int size) {
        this(size, false);
    }

    public ByteBufferLittleEndianWriter(int initialSize, boolean autoExpand) {
        this.bb = IoBuffer.allocate(initialSize);
        this.bb.setAutoExpand(autoExpand);
        setByteOutputStream(new ByteBufferOutputstream(bb));
    }

    public IoBuffer getFlippedBB() {
        return this.bb.flip();
    }

    public IoBuffer getByteBuffer() {
        return this.bb;
    }
}
