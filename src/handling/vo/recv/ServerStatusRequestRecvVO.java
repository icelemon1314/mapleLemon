package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ServerStatusRequestRecvVO extends MaplePacketRecvVO {

    Byte serverId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c)
    {
        serverId = slea.readByte();
    }

    public Byte getServerId() {
        return serverId;
    }
}
