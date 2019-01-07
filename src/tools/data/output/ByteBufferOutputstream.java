package tools.data.output;

import org.apache.mina.core.buffer.IoBuffer;

public class ByteBufferOutputstream implements ByteOutputStream {

    private final IoBuffer bb;

    public ByteBufferOutputstream(IoBuffer bb) {
        this.bb = bb;
    }

    @Override
    public void writeByte(byte b) {
        this.bb.put(b);
    }
}
