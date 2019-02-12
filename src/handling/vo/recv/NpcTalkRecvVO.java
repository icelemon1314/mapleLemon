package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcTalkRecvVO extends MaplePacketRecvVO {

    Integer npcOid;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        npcOid = slea.readInt();
    }

    public Integer getNpcOid() {
        return npcOid;
    }
}
