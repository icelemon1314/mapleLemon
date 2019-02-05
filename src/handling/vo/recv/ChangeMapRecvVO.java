package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import server.MaplePortal;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapRecvVO extends MaplePacketRecvVO {

    Byte type;
    Integer targetId;
    String portalName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        type = slea.readByte();
        targetId = slea.readInt();
        MapleLogger.info("换地图目标："+targetId);
        portalName = slea.readMapleAsciiString();
    }

    public Byte getType() {
        return type;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public String getPortalName() {
        return portalName;
    }
}
