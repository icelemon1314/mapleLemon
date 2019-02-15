package handling.channel.handler;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import handling.vo.recv.CloseRangeAttackRecvVO;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball;
import server.skill.冒险家.勇士;
import server.skill.冒险家.独行客;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import static client.MapleJob.getJobName;
import static handling.channel.handler.DamageParse.NotEffectforAttack;

public class CloseRangeAttackHandler extends MaplePacketHandler<CloseRangeAttackRecvVO> {


    @Override
    public void handlePacket(CloseRangeAttackRecvVO recvVO, MapleClient c) {
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
        closeRangeAttack(recvVO, c, chr, false);
        chr.monsterMultiKill();
    }

    public void closeRangeAttack(CloseRangeAttackRecvVO recvVO, MapleClient c, MapleCharacter chr, boolean 被动攻击) {
        //获取攻击信息
        AttackInfo attack = recvVO.getAttackInfo();
        if (attack == null) {
            chr.dropMessage(5, "攻击出现错误。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        boolean mirror = chr.getBuffedValue(MapleBuffStat.影分身) != null;
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage();
        // 盾牌
        Item shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        int attackCount = (shield != null) && (shield.getItemId() / 10000 == 134) ? 2 : 1;
        int skillLevel = 0;
        MapleStatEffect effect = null;
        Skill skill = null;

        //判断是不为普通攻击?
        if (attack.skillId != 0) {
            skill = SkillFactory.getSkill(attack.skillId);
            if (skill == null) {
                chr.dropMessage(5, "获取技能失败！"+attack.skillId);
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }

            skillLevel = chr.getTotalSkillLevel(skill);
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "近距离攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                }
                MapleLogger.info("近距离攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            attackCount = effect.getAttackCount(chr);

            if (GameConstants.isEventMap(chr.getMapId())) {
                for (MapleEventType t : MapleEventType.values()) {
                    MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                    if (e.isRunning() && !chr.isGM()) {
                        for (int i : e.getType().mapids) {
                            if (chr.getMapId() == i) {
                                chr.dropMessage(5, "无法在这个地方使用.");
                                return;
                            }
                        }
                    }
                }
            }

//            if (attack.skillId == 1321013) {
//                maxdamage += chr.getStat().getCurrentMaxHp();
//            }
//
//            if (attack.skillId == 4100012 || attack.skillId == 4120019) {
//                maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skillId) + effect.getX() * chr.getLevel()) / 100.0D;
//            } else {
//                maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skillId)) / 100.0D;
//            }

            if (attack.skillId == 独行客.金钱炸弹) {
                chr.handleMesosbomb(attack, 0);
            }

            if (effect.getCooldown(chr) > 0 && !被动攻击) {
                if (chr.skillisCooling(attack.skillId) && NotEffectforAttack(attack.skillId)) {
                    chr.dropMessage(5, "技能由于冷却时间限制，暂时无法使用。");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if (!chr.skillisCooling(attack.skillId)) {
//                    c.sendPacket(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
                    chr.addCooldown(attack.skillId, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
                }
            }
        }

        //最后处理伤害信息
        attack = DamageParse.Modify_AttackCrit(attack, chr, 1, effect);

        //伤害次数最后计算
        attackCount *= mirror ? 2 : 1;

        //活动，攻击雪球，普通攻击
        if ((chr.getMapId() == 109060000 || chr.getMapId() == 109060002 || chr.getMapId() == 109060004) && attack.skillId == 0) {
            MapleSnowball.MapleSnowballs.hitSnowball(chr);
        }

        //消耗斗气的技能
        if (isFinisher(attack.skillId) > 0) {
            int numFinisherOrbs = 0;
            Integer comboBuff = chr.getBuffedValue(MapleBuffStat.斗气集中);
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff - 1;
            }
            if (numFinisherOrbs <= 0) {
                return;
            }
            chr.handleOrbconsume(isFinisher(attack.skillId));
            maxdamage *= numFinisherOrbs;
        }

        //给地图上的玩家显示当前玩家使用技能效果
        byte[] packet;
        packet = MaplePacketCreator.closeRangeAttack(chr, skillLevel, 0, attack, false);

        if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, packet, chr.getTruePosition());
        } else {
            chr.getMap().broadcastGMMessage(chr, packet, false);
        }

        //攻击伤害处理
        DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, 0);
    }

    // 消耗斗气的技能
    public int isFinisher(int skillid) {
        switch (skillid) {
            case 勇士.黑暗之剑:
            case 勇士.黑暗之斧:
                return 2;
            case 勇士.气绝剑:
            case 勇士.气绝斧:
                return 4;
        }
        return 0;
    }
}
