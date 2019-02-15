package handling.vo.recv;


import client.MapleClient;
import handling.channel.handler.AttackInfo;
import handling.channel.handler.DamageParse;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class RangeAttackRecvVO extends MaplePacketRecvVO {

    AttackInfo attackInfo;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        attackInfo = DamageParse.parseRangedAttack(slea);

    }

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }
}
