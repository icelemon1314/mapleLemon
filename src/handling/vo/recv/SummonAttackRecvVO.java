package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import server.life.MapleMonster;
import tools.AttackPair;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.List;

public class SummonAttackRecvVO extends MaplePacketRecvVO {

    Integer summonId;
    Byte animation;
    List<AttackPair> allDamage;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        summonId = slea.readInt();
        animation = slea.readByte();

        byte numAttacked = 1;
        byte numDamage = 1;
        Integer monsterOid;

        List<AttackPair> allDamage = new ArrayList();
        for (int i = 0; i < numAttacked; i++) {
            monsterOid = slea.readInt();
            slea.skip(13);
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < numDamage; j++) {
                int damge = slea.readInt();
                allDamageNumbers.add(new Pair(damge, false));
            }
            slea.skip(4);
            allDamage.add(new AttackPair(monsterOid, allDamageNumbers));
        }
    }

    public Integer getSummonId() {
        return summonId;
    }

    public Byte getAnimation() {
        return animation;
    }

    public List<AttackPair> getAllDamage() {
        return allDamage;
    }
}
