package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.MaplePacketHandler;
import server.MapleStatEffect;
import server.Randomizer;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SummonPacket;

public class SummonSubHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        MapleSummon sum = chr.getSummons().get(slea.readInt());
        if (sum == null || (sum.getOwnerId() != chr.getId()) || (sum.getSkillLevel() <= 0) || (!chr.isAlive())) {
            return;
        }
        switch (sum.getSkillId()) {
            case 1301013:
                Skill bHealing = SkillFactory.getSkill(slea.readInt());
                int bHealingLvl = chr.getTotalSkillLevel(bHealing);
                if ((bHealingLvl <= 0) || (bHealing == null)) {
                    return;
                }
                MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                if (bHealing.getId() == 1310016) {
                    healEffect.applyTo(chr);
                } else if (bHealing.getId() == 1301013) {
                    if (!chr.canSummon(healEffect.getX() * 1000)) {
                        return;
                    }
                    int healHp = Math.min(1000, healEffect.getHp() * chr.getLevel());
                    chr.addHP(healHp);
                }
                chr.getClient().sendPacket(MaplePacketCreator.showOwnBuffEffect(sum.getSkillId(), 3, chr.getLevel(), bHealingLvl));//2+1 119
                chr.getMap().broadcastMessage(SummonPacket.summonSkill(chr.getId(), sum.getSkillId(), bHealing.getId() == 1301013 ? 5 : Randomizer.nextInt(3) + 6));
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr, sum.getSkillId(), 3, chr.getLevel(), bHealingLvl), false);//2+1 119
        }
    }
}
