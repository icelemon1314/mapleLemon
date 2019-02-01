package handling;

import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public abstract class MaplePacketHandler {

    public void handlePacket(MaplePacketRecvVO recvVo, MapleClient c){}

    public abstract void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c);
}
