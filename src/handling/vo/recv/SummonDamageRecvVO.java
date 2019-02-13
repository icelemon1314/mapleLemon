package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SummonDamageRecvVO extends MaplePacketRecvVO {

    Integer summonId;
    Byte type;
    Integer damage;
    Integer monsterIdFrom;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        summonId = slea.readInt();

        type = slea.readByte();
        damage = slea.readInt();
        monsterIdFrom = slea.readInt();
        slea.skip(1);
    }

    public Integer getSummonId() {
        return summonId;
    }

    public Byte getType() {
        return type;
    }

    public Integer getDamage() {
        return damage;
    }

    public Integer getMonsterIdFrom() {
        return monsterIdFrom;
    }
}
