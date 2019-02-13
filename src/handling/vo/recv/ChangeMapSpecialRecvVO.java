package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapSpecialRecvVO extends MaplePacketRecvVO {

    String portalName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        portalName = slea.readMapleAsciiString();
    }

    public String getPortalName() {
        return portalName;
    }
}
