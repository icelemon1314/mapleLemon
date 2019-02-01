package handling.vo;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public abstract class MaplePacketSendVO {

    protected Byte opcode;

    protected MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    protected void encodeOpcode() {
        mplew.write(opcode);
    }
}
