package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import server.maps.MapleSummon;

import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SummonPacket;

public class SummonDamageHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 54 FA FE 30 00 FF 00 00 00 00 04 87 01 00 00
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null)) {
            return;
        }
        MapleSummon summon = chr.getSummons().get(slea.readInt());
        if ((summon == null) || (summon.getOwnerId() != chr.getId())) {
            MapleLogger.info("找不到地图物体：召唤兽！");
            return;
        }
        int type = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.skip(1);
        boolean remove = false;
        if ((summon.is替身术()) && (damage > 0)) {
            summon.addSummonHp(-damage);
            if (summon.getSummonHp() <= 0) {
                remove = true;
            }
            chr.getMap().broadcastMessage(chr, SummonPacket.damageSummon(chr.getId(), summon.getSkillId(), damage, type, monsterIdFrom), summon.getTruePosition());
        }
        if (remove) {
            chr.dispelSkill(summon.getSkillId());
        }
    }
}
