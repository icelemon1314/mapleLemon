package handling.vo.recv;


import client.MapleClient;
import handling.channel.handler.AttackInfo;
import handling.channel.handler.DamageParse;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CloseRangeAttackRecvVO extends MaplePacketRecvVO {

    AttackInfo attackInfo;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        //获取攻击信息
        attackInfo = DamageParse.parseCloseRangeAttack(slea);
    }

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }
}
