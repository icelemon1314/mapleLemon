package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class HealOverTimeRecvVO extends MaplePacketRecvVO {

    Short healHp;
    Short healMp;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 2F 00 14 00 00 0A 00 00 00 00 8E 9D 5C 00
        slea.skip(4);
        healHp = slea.readShort();
        healMp = slea.readShort();
        slea.skip(1);
        Integer time = slea.readInt();
    }

    public Short getHealHp() {
        return healHp;
    }

    public Short getHealMp() {
        return healMp;
    }
}
