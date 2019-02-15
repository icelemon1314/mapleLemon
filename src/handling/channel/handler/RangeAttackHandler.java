package handling.channel.handler;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import handling.vo.recv.RangeAttackRecvVO;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.events.MapleEventType;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;

import java.util.Collections;

import static client.MapleJob.getJobName;

public class RangeAttackHandler extends MaplePacketHandler<RangeAttackRecvVO> {


    @Override
    public void handlePacket(RangeAttackRecvVO recvVO, MapleClient c) {
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


        rangedAttack(recvVO, c, chr);

        chr.monsterMultiKill();
    }

    /**
     * 远程攻击
     * @param c
     * @param chr
     */
    public void rangedAttack(RangeAttackRecvVO recvVO, MapleClient c, MapleCharacter chr) {
        AttackInfo attack = recvVO.getAttackInfo();
        if (attack == null) {
            if (chr.isShowPacket()) {
                chr.dropSpouseMessage(25, "[RangedAttack] - 远距离攻击封包解析返回为空.");
            }
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int bulletCount = 1;
        int skillLevel = 0;
        MapleStatEffect effect = null;
        Skill skill = null;
        boolean noBullet = GameConstants.isCastJob(chr.getJob());

        if (attack.skillId != 0) {
            skill = SkillFactory.getSkill(attack.skillId); //暂时这样修改
            if (skill == null) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            skillLevel = chr.getTotalSkillLevel(attack.skillId);
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "近距离攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                }
                MapleLogger.info("远距离攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if (GameConstants.isEventMap(chr.getMapId())) {
                for (MapleEventType eventType : MapleEventType.values()) {
                    MapleEvent event = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(eventType);
                    if ((event.isRunning()) && (!chr.isGM())) {
                        for (int i : event.getType().mapids) {
                            if (chr.getMapId() == i) {
                                chr.dropMessage(5, "无法在这个地方使用.");
                                return;
                            }
                        }
                    }
                }
            }
            bulletCount = Math.max(effect.getBulletCount(chr), effect.getAttackCount(chr));
            if ((effect.getCooldown(chr) > 0) && (((attack.skillId != 35111004) && (attack.skillId != 35121013)))) {
                if (chr.skillisCooling(attack.skillId)) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
//                c.sendPacket(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
                chr.addCooldown(attack.skillId, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
            }
        }
        attack = DamageParse.Modify_AttackCrit(attack, chr, 2, effect);
        boolean mirror = chr.getBuffedValue(MapleBuffStat.影分身) != null;
        bulletCount *= (mirror ? 2 : 1);
        int projectile = 0;
        int visProjectile = 0;
        if (noBullet && (chr.getBuffedValue(MapleBuffStat.无形箭弩) == null)) {
            Item item = chr.getInventory(MapleInventoryType.USE).getItem(attack.starSlot);
            if (item == null) {
                return;
            }
            projectile = item.getItemId();
            if (attack.cashSlot > 0) {
                if (chr.getInventory(MapleInventoryType.CASH).getItem(attack.cashSlot) == null) {
                    return;
                }
                visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem(attack.cashSlot).getItemId();
            } else {
                visProjectile = projectile;
            }

            int bulletConsume = bulletCount;
            if ((effect != null) && (effect.getBulletConsume() != 0)) {
                bulletConsume = effect.getBulletConsume() * (mirror ? 2 : 1);
            }
            if ((chr.getJob() == 412) && (bulletConsume > 0) && (item.getQuantity() < MapleItemInformationProvider.getInstance().getSlotMax(projectile))) {
                Skill expert = SkillFactory.getSkill(4110012);
                if (chr.getTotalSkillLevel(expert) > 0) {
                    MapleStatEffect eff = expert.getEffect(chr.getTotalSkillLevel(expert));
                    if (eff.makeChanceResult()) {
                        item.setQuantity((short) (item.getQuantity() + 1));
                        c.sendPacket(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(1, item))));
                        bulletConsume = 0;
                        c.sendPacket(InventoryPacket.getInventoryStatus());
                    }
                }
            }
            if (bulletConsume > 0) {
                boolean useItem = true;
                if (chr.getBuffedValue(MapleBuffStat.子弹数量) != null) {
                    int count = chr.getBuffedIntValue(MapleBuffStat.子弹数量) - bulletConsume;
                    if (count >= 0) {
                        chr.setBuffedValue(MapleBuffStat.子弹数量, count);
                        useItem = false;
                    } else {
                        chr.cancelEffectFromBuffStat(MapleBuffStat.子弹数量);
                        bulletConsume += count;
                    }
                }

                if ((useItem) && (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true))) {
                    chr.dropMessage(5, "您的箭/子弹/飞镖不足。");
                    return;
                }
            }
        }

        int projectileWatk = 0;
        if (projectile != 0) {
            projectileWatk = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
        }

        PlayerStats statst = chr.getStat();

        double basedamage = statst.getCurrentMaxBaseDamage() + statst.calculateMaxProjDamage(projectileWatk, chr);

        switch (attack.skillId) {
            case 3101005:
                if (effect == null) {
                    break;
                }
                basedamage *= effect.getX() / 100.0D;
        }

        if (effect != null) {
            basedamage *= (effect.getDamage() + statst.getDamageIncrease(attack.skillId)) / 100.0D;
            long money = effect.getMoneyCon();
            if (money != 0) {
                if (money > chr.getMeso()) {
                    money = chr.getMeso();
                }
                chr.gainMeso(-money, false);
            }
        }
        if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.rangedAttack(chr, skillLevel, visProjectile, attack), chr.getTruePosition());
        } else {
            chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.rangedAttack(chr, skillLevel, visProjectile, attack), false);
        }
        DamageParse.applyAttack(attack, skill, chr, bulletCount, basedamage, effect, mirror ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED, visProjectile);
    }
}
