package handling.vo;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public abstract class MaplePacketRecvVO {

    protected SeekableLittleEndianAccessor rawPacketMsg;

    public abstract void decodePacket(SeekableLittleEndianAccessor msg, MapleClient c);

    public SeekableLittleEndianAccessor getRawPacketMsg() {
        return rawPacketMsg;
    }
}
