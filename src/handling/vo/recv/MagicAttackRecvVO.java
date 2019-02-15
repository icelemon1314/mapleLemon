package handling.vo.recv;


import client.MapleClient;
import handling.channel.handler.AttackInfo;
import handling.channel.handler.DamageParse;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class MagicAttackRecvVO extends MaplePacketRecvVO {

    AttackInfo attackInfo;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        attackInfo = DamageParse.parseMagicDamage(slea);
    }

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }
}
