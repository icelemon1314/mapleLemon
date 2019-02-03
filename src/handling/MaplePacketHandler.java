package handling;

import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public abstract class MaplePacketHandler<T> {
    public abstract void handlePacket(T recvVo, MapleClient c);
}
