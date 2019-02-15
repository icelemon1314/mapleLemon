package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class TakeDamageRecvVO extends MaplePacketRecvVO {

    Byte damageType;
    Integer damage;
    Integer damageByMonsterOid;


    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 1E
        // FF
        // 07 00 00 00
        // 00 A2 86 01
        // 00 35 FC 01 00 00 00
        //TODO 修复反射伤害给怪物 还有其他的掉血类型
        // 1E FE 19 00 00 00 00 00 高处掉落下来扣血
        // @TODO make sure the struct
        damageType = slea.readByte();
        damage = slea.readInt();
        if (damageType == -1) { // 怪物伤害
            slea.readByte();
            damageByMonsterOid = slea.readInt();
        } else if (damageType == -2) { // 高处掉落扣血
            slea.readShort();
        }
    }

    public Byte getDamageType() {
        return damageType;
    }

    public Integer getDamage() {
        return damage;
    }

    public Integer getDamageByMonsterOid() {
        return damageByMonsterOid;
    }
}
