package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.events.MapleEventType;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import static client.MapleJob.getJobName;

public class MagicAttackHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (chr.hasBlockedInventory() || chr.getMap() == null) {
            chr.dropMessage(5, "现在还不能进行攻击。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (!chr.isAdmin() && chr.getMap().isMarketMap()) {
            chr.dropMessage(5, "在自由市场内无法使用技能。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }

        MagicDamage(slea, c, chr);
        chr.monsterMultiKill();
    }

    /**
     * 魔法攻击
     * @param slea
     * @param c
     * @param chr
     */
    public void MagicDamage(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        AttackInfo attack = DamageParse.parseMagicDamage(slea, chr);
        if (attack == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        Skill skill = SkillFactory.getSkill(attack.skillId);
        if (skill == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int skillLevel = chr.getTotalSkillLevel(skill);
        MapleStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
        if (effect == null) {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, "魔法攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
            }
            MapleLogger.info("魔法攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
            return;
        }
        attack = DamageParse.Modify_AttackCrit(attack, chr, 3, effect);
        if (GameConstants.isEventMap(chr.getMapId())) {
            for (MapleEventType t : MapleEventType.values()) {
                MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                if ((e.isRunning()) && (!chr.isGM())) {
                    for (int i : e.getType().mapids) {
                        if (chr.getMapId() == i) {
                            chr.dropMessage(5, "无法在这个地方使用.");
                            return;
                        }
                    }
                }
            }
        }
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage() * (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skillId)) / 100.0D;
        if (GameConstants.isPyramidSkill(attack.skillId)) {
            maxdamage = 1.0D;
        } else if ((GameConstants.is新手职业(skill.getId() / 10000)) && (skill.getId() % 10000 == 1000)) {
            maxdamage = 40.0D;
        }
        if (effect.getCooldown(chr) > 0) {
            if (chr.skillisCooling(attack.skillId)) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
//            c.sendPacket(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
            chr.addCooldown(attack.skillId, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
        }
        if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.magicAttack(chr, skillLevel, 0, attack), chr.getTruePosition());
        } else {
            chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.magicAttack(chr, skillLevel, 0, attack), false);
        }
        DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect, maxdamage);
    }
}
