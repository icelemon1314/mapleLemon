package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeSpRecvVO extends MaplePacketRecvVO {

    Integer skillId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        skillId = slea.readInt();
    }

    public Integer getSkillId() {
        return skillId;
    }
}
