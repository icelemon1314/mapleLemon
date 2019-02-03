package handling.vo;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public abstract class MaplePacketRecvVO {

    public abstract void decodePacket(SeekableLittleEndianAccessor msg, MapleClient c);

    // public abstract void encodePacket(SeekableLittleEndianAccessor slea, MapleClient c);
}
