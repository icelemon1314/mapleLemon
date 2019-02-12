package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SummonSubRecvVO extends MaplePacketRecvVO {

    Integer summonId;
    Integer skillId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        summonId = slea.readInt();
        if (slea.available() >= 4) {
            skillId = slea.readInt();
        }
    }

    public Integer getSummonId() {
        return summonId;
    }

    public Integer getSkillId() {
        return skillId;
    }
}
