package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import static client.MapleJob.getJobName;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.SkillMacro;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ItemConstants;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import static handling.channel.handler.DamageParse.NotEffectforAttack;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Randomizer;
import server.ServerProperties;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball.MapleSnowballs;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import server.maps.MapleArrowsTurret;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.skill.冒险家.勇士;
import server.skill.冒险家.独行客;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.SkillPacket;

public class PlayerHandler {

    private static final Logger log = Logger.getLogger(PlayerHandler.class);
    public static int fox = 0;

    // 消耗斗气的技能
    public static int isFinisher(int skillid) {
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

    public static void ChangeKeymap(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if ((slea.available() > 8L) && (chr != null)) {
            slea.skip(4);
            int numChanges = slea.readInt();
            for (int i = 0; i < numChanges; i++) {
                int key = slea.readInt();
                byte type = slea.readByte();
                int action = slea.readInt();
                if ((type == 1) && (action >= 1000)) {
                    Skill skil = SkillFactory.getSkill(action);
                    if ((skil != null) && (((skil.isInvisible()) && (chr.getSkillLevel(skil) <= 0)) || (action % 10000 < 1000))) {
                        continue;
                    }
                }
                chr.changeKeybinding(key, type, action);
            }
        } else if (chr != null) {
            int type = slea.readInt();
            int data = slea.readInt();
            switch (type) {
                case 1:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122221));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122221)).setCustomData(String.valueOf(data));
                    }
                    break;
                case 2:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122223));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122223)).setCustomData(String.valueOf(data));
                    }
                    break;
                case 3:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(122224));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(122224)).setCustomData(String.valueOf(data));
                    }
            }
        }
    }

    // 坐椅子
    public static void UseChair(int itemId, MapleClient c, MapleCharacter chr) {
        // 19 0A 00
        // 19 FF FF 取消
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }

        chr.setChair(itemId);
        c.getSession().write(MaplePacketCreator.showChair(c.getPlayer().getId(),itemId));
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), itemId), false);
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void CancelChair(short id, MapleClient c, MapleCharacter chr) {
        if (id == -1) {
            chr.setChair(0);
            c.getSession().write(MaplePacketCreator.cancelChair(-1, chr.getId()));
            if (chr.getMap() != null) {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), 0), false);
            }
        } else {
            chr.setChair(id);
            c.getSession().write(MaplePacketCreator.cancelChair(id, chr.getId()));
        }
    }

    /**
     * 使用缩地石
     * @param slea
     * @param c
     * @param chr
     */
    public static void TrockAddMap(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte type = slea.readByte();
        if (type == 0) { // 删除地图
            int mapId = slea.readInt();
            chr.deleteFromRegRocks(mapId);
            c.getSession().write(MTSCSPacket.getTrockRefresh(chr, (byte)1, true));
        } else if (type == 1) {
            if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                chr.addRegRockMap();
                c.getSession().write(MTSCSPacket.getTrockRefresh(chr, (byte)1, false));
            } else {
                chr.dropMessage(1, "你可能没有保存此地图.");
            }
        }
    }

    public static void CharInfoRequest(int objectid, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleCharacter player = chr.getMap().getCharacterById(objectid);
        c.getSession().write(MaplePacketCreator.enableActions());
        if ((player != null)) {
            c.getSession().write(MaplePacketCreator.charInfo(player, chr.getId() == objectid));
        }
    }

    public static void UseItemEffect(int itemId, MapleClient c, MapleCharacter chr) {
        if (itemId == 0) {
            chr.setItemEffect(0);
        } else {
            Item toUse = chr.getInventory(MapleInventoryType.CASH).findById(itemId);
            if ((toUse == null) || (toUse.getItemId() != itemId) || (toUse.getQuantity() < 1)) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (itemId != 5510000) {
                chr.setItemEffect(itemId);
            }
        }
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.itemEffect(chr.getId(), itemId), false);
    }

    public static void UseTitleEffect(int itemId, MapleClient c, MapleCharacter chr) {
        if (itemId == 0) {
            chr.setTitleEffect(0);
            chr.getQuestRemove(MapleQuest.getInstance(GameConstants.称号));
        } else {
            Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
            if ((toUse == null) || (toUse.getItemId() != itemId) || (toUse.getQuantity() < 1)) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (itemId / 10000 == 370) {
                chr.setTitleEffect(itemId);
                chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.称号)).setCustomData(String.valueOf(itemId));
            }
        }
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showTitleEffect(chr.getId(), itemId), false);
    }

    public static void CancelItemEffect(int id, MapleCharacter chr) {
        chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1L);
    }

    public static void CancelBuffHandler(int sourceid, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        Skill skill = SkillFactory.getSkill(sourceid);
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(10, "收到取消技能BUFF 技能ID " + sourceid + " 技能名字 " + SkillFactory.getSkillName(sourceid));
        }

        if (skill == null) {
            return;
        }
        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0L);
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
        } else {
            if (sourceid == 3121013) {
                chr.getClient().getSession().write(MaplePacketCreator.skillCancel(chr, sourceid));
            } else {
                chr.cancelEffect(skill.getEffect(1), false, -1L);
            }
        }
        chr.cancelEffect(skill.getEffect(1), false, -1L);
    }

    public static void CancelMech(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.setnorba();
        int sourceid = slea.readInt();
        if ((sourceid % 10000 < 1000) && (SkillFactory.getSkill(sourceid) == null)) {
            sourceid += 1000;
        }
        Skill skill = SkillFactory.getSkill(sourceid);
        if (skill == null) {
            return;
        }
        MapleStatEffect eff = skill.getEffect(chr.getTotalSkillLevel(sourceid));
        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0L);
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
        }else {
            chr.cancelEffect(skill.getEffect(slea.readByte()), false, -1L);
        }
    }

    public static void QuickSlot(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.getQuickSlot().resetQuickSlot();
        for (int i = 0; i < 28; i++) {
            chr.getQuickSlot().addQuickSlot(i, slea.readInt());
        }
    }

    public static void SkillEffect(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 33 39 41 40 00 05 A9 06
        int skillId = slea.readInt();
        byte level = slea.readByte();
        byte display = slea.readByte();
        byte direction = slea.readByte();
//        byte speed = slea.readByte();
        Point position = null;
        if (slea.available() == 4L) {
            position = slea.readPos();
        } else if (slea.available() == 8) {
            position = slea.readPos();
        }
        Skill skill = SkillFactory.getSkill(skillId);
        if ((chr == null) || (skill == null) || (chr.getMap() == null)) {
            return;
        }
        int skilllevel_serv = chr.getTotalSkillLevel(skill);
        if ((skilllevel_serv > 0) && (skilllevel_serv == level) && (skill.isChargeSkill())) {
            chr.setKeyDownSkill_Time(System.currentTimeMillis());
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillEffect(chr.getId(), skillId, level, display, direction, (byte)1, position), false);
        }
    }

    public static void specialAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int pos_x = slea.readInt();
        int pos_y = slea.readInt();
        int pos_unk = slea.readInt();
        int display = slea.readInt();
        int skillId = slea.readInt();
        boolean isLeft = slea.readByte() > 0;
        int speed = slea.readInt();
        int tickCount = slea.readInt();
        Skill skill = SkillFactory.getSkill(skillId);
        int skilllevel = chr.getTotalSkillLevel(skill);
        if (chr.isShowPacket()) {
            System.err.println("[SpecialAttack] - 技能ID: " + skillId + " 技能等级: " + skilllevel);
        }
        if ((skill == null) || (skilllevel <= 0)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr, skillId, 1, chr.getLevel(), skilllevel), false);
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showSpecialAttack(chr.getId(), tickCount, pos_x, pos_y, display, skillId, skilllevel, isLeft, speed), chr.getTruePosition());
    }

    public static void AfterSkill(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int skillid = slea.readInt();
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
    }

    /**
     * 处理BUFF
     * @param slea
     * @param c
     * @param chr
     */
    public static void SpecialSkill(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 31 CE CC 10 00 01 80 00 00
        // 31 2B 46 0F 00 14 00 00
        // 31 BC BC 21 00 04 01 A6 86 01 00 58 02
        // 31 5A 43 23 00 14 49 FB 16 08  时空门
        // 31 39 41 40 00 05 00 00
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int skillid = slea.readInt();
        int skillLevel = slea.readByte();
        if (chr.isShowPacket()) {
            chr.dropMessage(5,"[SpecialSkill] - 技能ID: " + skillid + " 技能等级: " + skillLevel);
        }
        Point pos = null;
        if (slea.available() == 4) {
            pos = new Point(slea.readShort(), slea.readShort());
        }
        Skill skill = SkillFactory.getSkill(skillid);
        if ((skill == null)) {
            chr.dropMessage(5,"[SpecialSkill] -   不存在的技能ID" + skillid);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        skillLevel = chr.getTotalSkillLevel(skillid);
        MapleStatEffect effect = skill.getEffect(skillLevel); // 获取技能效果
        if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
            if (chr.skillisCooling(skillid)) {//TODO 修复客户端冷却时间不一致
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            c.getSession().write(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown(chr)));
            chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
        }
        FileoutputUtil.log("看是否有特需处理的BUFF");
        if (effect.is时空门()) {
            FileoutputUtil.log("释放时空们");
            if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                effect.applyTo(c.getPlayer(), pos);
            } else {
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
//            int mountid = MapleStatEffect.parseMountInfo(c.getPlayer(), skill.getId());
//            if ((mountid != 0) && (mountid != GameConstants.getMountItem(skill.getId(), chr)) && (!chr.isIntern()) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -122) == null)
//                    && (!GameConstants.isMountItemAvailable(mountid, chr.getJob()))) {
//                c.getSession().write(MaplePacketCreator.enableActions());
//                return;
//            }
            FileoutputUtil.log("释放技能效果！");
            effect.applyTo(chr, pos);
        }
    }

    /**
     * 处理攻击包
     * @param slea
     * @param c
     * @param header
     */
    public static void 攻击处理(SeekableLittleEndianAccessor slea, MapleClient c, RecvPacketOpcode header) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (chr.hasBlockedInventory() || chr.getMap() == null) {
            chr.dropMessage(5, "现在还不能进行攻击。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!chr.isAdmin() && chr.getMap().isMarketMap()) {
            chr.dropMessage(5, "在自由市场内无法使用技能。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }

        switch (header) {
            case CLOSE_RANGE_ATTACK://近战攻击
                PlayerHandler.closeRangeAttack(slea, c, chr);
                break;
            case RANGED_ATTACK://远程攻击
                PlayerHandler.rangedAttack(slea, c, chr);
                break;
            case MAGIC_ATTACK://魔法攻击
                PlayerHandler.MagicDamage(slea, c, chr);
                break;
            case SPECIAL_MAGIC_ATTACK:
                slea.skip(12);
                PlayerHandler.MagicDamage(slea, c, chr);
                break;
            case SUMMON_ATTACK:
                SummonHandler.SummonAttack(slea, c, chr);
                break;
        }
        chr.monsterMultiKill();
    }

    public static void closeRangeAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        PlayerHandler.closeRangeAttack(slea, c, chr, false);
    }

    public static void closeRangeAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean 被动攻击) {
        //获取攻击信息
        AttackInfo attack = DamageParse.parseCloseRangeAttack(slea, chr);
        if (attack == null) {
            chr.dropMessage(5, "攻击出现错误。");
            c.getSession().write(MaplePacketCreator.enableActions());
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
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }

            skillLevel = chr.getTotalSkillLevel(skill);
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "近距离攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                }
                FileoutputUtil.log(FileoutputUtil.SpecialSkill_log, "近距离攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (!chr.skillisCooling(attack.skillId)) {
                    c.getSession().write(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
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
            MapleSnowballs.hitSnowball(chr);
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
        if (被动攻击) {
            packet = MaplePacketCreator.passiveAttack(chr, skillLevel, 0, attack, false);
        } else {
            packet = MaplePacketCreator.closeRangeAttack(chr, skillLevel, 0, attack, false);
        }

        if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, packet, chr.getTruePosition());
        } else {
            chr.getMap().broadcastGMMessage(chr, packet, false);
        }

        //攻击伤害处理
        DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, 0);
    }

    /**
     * 远程攻击
     * @param slea
     * @param c
     * @param chr
     */
    public static void rangedAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        AttackInfo attack = DamageParse.parseRangedAttack(slea, chr);
        if (attack == null) {
            if (chr.isShowPacket()) {
                chr.dropSpouseMessage(25, "[RangedAttack] - 远距离攻击封包解析返回为空.");
            }
            c.getSession().write(MaplePacketCreator.enableActions());
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
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            skillLevel = chr.getTotalSkillLevel(attack.skillId);
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "近距离攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                }
                FileoutputUtil.log(FileoutputUtil.SpecialSkill_log, "远距离攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
                c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                c.getSession().write(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
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
                        c.getSession().write(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(1, item))));
                        bulletConsume = 0;
                        c.getSession().write(InventoryPacket.getInventoryStatus());
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

    /**
     * 魔法攻击
     * @param slea
     * @param c
     * @param chr
     */
    public static void MagicDamage(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        AttackInfo attack = DamageParse.parseMagicDamage(slea, chr);
        if (attack == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        Skill skill = SkillFactory.getSkill(attack.skillId);
        if (skill == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int skillLevel = chr.getTotalSkillLevel(skill);
        MapleStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
        if (effect == null) {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, "魔法攻击效果为空. 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
            }
            FileoutputUtil.log(FileoutputUtil.SpecialSkill_log, "魔法攻击效果为空 玩家[" + chr.getName() + " 职业: " + getJobName(chr.getJob()) + "(" + chr.getJob() + ")] 使用技能: " + skill.getId() + " - " + skill.getName() + " 技能等级: " + skillLevel);
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
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            c.getSession().write(MaplePacketCreator.skillCooldown(attack.skillId, effect.getCooldown(chr)));
            chr.addCooldown(attack.skillId, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
        }
        if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.magicAttack(chr, skillLevel, 0, attack), chr.getTruePosition());
        } else {
            chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.magicAttack(chr, skillLevel, 0, attack), false);
        }
        DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect, maxdamage);
    }

    /**
     * 玩家丢钱出来
     * @param meso
     * @param chr
     */
    public static void DropMeso(int meso, MapleCharacter chr) {
        if ((!chr.isAlive()) || (meso < 10) || (meso > 50000) || (meso > chr.getMeso())) {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.gainMeso(-meso, false, true);
        chr.getMap().spawnMesoDrop(meso, chr.getTruePosition(), chr, chr, true, (byte) 0);
        //chr.getCheatTracker().checkDrop(true);
    }

    public static void ChangeEmotion(int emote, MapleCharacter chr) {
        if (emote > 7) {
            int emoteid = 5159992 + emote;
            MapleInventoryType type = ItemConstants.getInventoryType(emoteid);
            if (chr.getInventory(type).findById(emoteid) == null) {
                return;
            }
        }
        if ((emote > 0) && (chr != null) && (chr.getMap() != null) && (!chr.isHidden())) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.facialExpression(chr, emote), false);
        }
    }

    /**
     * 自动恢复HP/MP
     * @param slea
     * @param chr
     */
    public static void Heal(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 2F 00 14 00 00 0A 00 00 00 00 8E 9D 5C 00
        if (chr == null) {
            return;
        }
        slea.skip(4);
        int healHP = slea.readShort();
        int healMP = slea.readShort();
        PlayerStats stats = chr.getStat();
        if (stats.getHp() <= 0 || healHP > 100 || healMP > 100) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((healHP != 0) && (chr.canHP(now + 1000L))) {
//            if (healHP > stats.getHealHP()) {
//                healHP = (int) stats.getHealHP();
//            }
            chr.addHP(healHP);
        }
        if ((healMP != 0) && (chr.canMP(now + 1000L))) {
            // @TODO 限制每次恢复的MP
//            if (healMP > stats.getHealMP()) {
//                healMP = (int) stats.getHealMP();
//            }
            chr.addMP(healMP);
        }
    }

    /**
     * 玩家移动
     * @param slea
     * @param c
     * @param chr
     */
    public static void MovePlayer(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        slea.readByte();
        Point Original_Pos = chr.getPosition();
        slea.readShort(); // position
        slea.readShort();
        List res;
        try {
            res = MovementParse.parseMovement(slea, 1, chr);
        } catch (ArrayIndexOutOfBoundsException e) {
            FileoutputUtil.log("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && (chr.getMap() != null)) {
            if (slea.available() != 10) {
                FileoutputUtil.log("玩家" + chr.getName() + "(" + MapleJob.getName(MapleJob.getById(chr.getJob())) + ") slea.available != 8 (角色移动出错) 剩余封包长度: " + slea.available());
                FileoutputUtil.log(FileoutputUtil.Movement_Char, "slea.available != 8 (角色移动出错) 封包: " + slea.toString(true));
                return;
            }
            MapleMap map = c.getPlayer().getMap();
            slea.skip(2);
            if (chr.isHidden()) {
                chr.setLastRes(res);
                chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), slea), false);
            } else {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), slea), false);
            }

            MovementParse.updatePosition(res, chr, 0);
            Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
        }
    }

    /**
     * 请求传送口数据
     * @param portal_name
     * @param c
     * @param chr
     */
    public static void ChangeMapSpecial(String portal_name, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MaplePortal portal = chr.getMap().getPortal(portal_name);
        if ((portal != null) && (!chr.hasBlockedInventory())) {
            portal.enterPortal(c);
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    /**
     * 更换地图
     * @param slea
     * @param c
     * @param chr
     */
    public static void ChangeMap(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 15 00 00 00 00 00 00 00 00 00 角色回城
        // 15 08 00 00 00 00 00 00 00 00 死亡后回程
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        if (slea.available() != 0L) {
            int type = slea.readByte();
            int targetid = slea.readInt();
            FileoutputUtil.log("换地图目标："+targetid);
            MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
            if ((targetid != -1) && (!chr.isAlive())) { // 角色死亡
                chr.setStance(0);
                if ((chr.getEventInstance() != null) && (chr.getEventInstance().revivePlayer(chr)) && (chr.isAlive())) {
                    FileoutputUtil.log("没有死亡但是要回程："+targetid);
                    return;
                }
                chr.getStat().setHp(50);
                MapleMap to = chr.getMap().getReturnMap();
                FileoutputUtil.log("死亡后准备回程：："+to.getId());
                chr.changeMap(to, to.getPortal(0));
            } else if (targetid != -1 && c.getPlayer().isGM()) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                MaplePortal pto = to.getPortal(0);
                chr.changeMap(to, pto);
            } else if ((targetid != -1) ) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                chr.changeMap(to, to.getPortal(0));
            } else if ((portal != null) && (!chr.hasBlockedInventory())) {
                FileoutputUtil.log("执行传送口脚本："+portal.getScriptName());
                portal.enterPortal(c);
            } else {
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        }
    }

    public static void InnerPortal(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
        int toX = slea.readShort();
        int toY = slea.readShort();

        if (portal == null) {
            return;
        }
        if ((portal.getPosition().distanceSq(chr.getTruePosition()) > 22500.0D) && (!chr.isGM())) {
            return;
        }
        chr.getMap().movePlayer(chr, new Point(toX, toY));
    }

    public static void snowBall(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static void leftKnockBack(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getMapId() / 10000 == 10906) {
            c.getSession().write(MaplePacketCreator.leftKnockBack());
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void ReIssueMedal(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int questId = slea.readShort();
        int itemId = slea.readInt();
        MapleQuest quest = MapleQuest.getInstance(questId);
        if ((((quest != null ? 1 : 0) & (quest.getMedalItem() > 0 ? 1 : 0)) != 0) && (chr.getQuestStatus(quest.getId()) == 2) && (quest.getMedalItem() == itemId)) {
            if (!chr.haveItem(itemId)) {
                int price = 100;
                int infoQuestId = 29949;
                String infoData = "count=1";
                if (chr.containsInfoQuest(infoQuestId, "count=")) {
                    String line = chr.getInfoQuest(infoQuestId);
                    String[] splitted = line.split("=");
                    if (splitted.length == 2) {
                        int data = Integer.parseInt(splitted[1]);
                        infoData = "count=" + data + 1;
                        if (data == 1) {
                            price = 1000;
                        } else if (data == 2) {
                            price = 10000;
                        } else if (data == 3) {
                            price = 100000;
                        } else {
                            price = 1000000;
                        }
                    } else {
                        chr.dropMessage(1, "重新领取勋章出现错误");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                }
                if (chr.getMeso() < price) {
                    chr.dropMessage(1, "本次重新需要金币: " + price + "\r\n请检查金币是否足够");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                chr.gainMeso(-price, true, true);
                MapleInventoryManipulator.addById(c, itemId, (short) 1, "");
                c.getSession().write(MaplePacketCreator.updateMedalQuestInfo((byte) 0, itemId));
            } else {
                c.getSession().write(MaplePacketCreator.updateMedalQuestInfo((byte) 3, itemId));
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void PlayerUpdate(MapleCharacter chr) {
//        boolean autoSave = true;
        if (chr == null || chr.getMap() == null) {
            return;
        }
            long startTime = System.currentTimeMillis();
            chr.saveToDB(false, false);
            if (chr.isShowPacket()) {
                chr.dropMessage(-11, "保存数据，耗时 " + (System.currentTimeMillis() - startTime) + " 毫秒");
            }
    }

    public static void LoadPlayerSuccess(MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        String msg = "欢迎来到#b蓝蜗牛区（仿官方）#k\r\n\r\n祝您玩的愉快！";
        c.getSession().write(MaplePacketCreator.sendHint(msg, 250, 5));
        int exp = c.getChannelServer().getExpRate(c.getWorld());
        if (exp > 1) {
            chr.dropSpouseMessage(20, "[系统提示] 当前服务器处于"+exp+"倍经验活动中，祝您玩的愉快！");
        }
        if (c.getChannelServer().getAutoGain() >= 2) {
            chr.dropSpouseMessage(25, "[系统提示] 在线时间奖励双倍活动正在举行.");
        }
        c.getSession().write(MaplePacketCreator.showPlayerCash(chr));
        /*if ((GameConstants.is新骑士团(chr.getJob())) && (chr.getLevel() >= 10)) {
         if (chr.getPQLog("骑士团能力修复", 1) == 0) {
         chr.resetStats(4, 4, 4, 4, true);
         chr.setPQLog("骑士团能力修复", 1);
         }
         if (chr.getPQLog("骑士团技能修复", 1) == 0) {
         chr.SpReset();
         chr.setPQLog("骑士团技能修复", 1);
         }
         }*/
//                if ((player.getJob() == 6001) && (player.getLevel() < 10)) {
//                    while (player.getLevel() < 10) {
//                        player.gainExp(5000, true, false, true);
//                    }
//                }

//                if (!player.isIntern()) {
//                    if ((c.getChannelServer().isCheckCash()) && ((player.getItemQuantity(4000463) >= 800) || (player.getCSPoints(-1) >= 900000)) && (player.getHyPay(3) < 200)) {
//                        String msgtext = "玩家 "+player.getName()+" 数据异常，服务器自动断开他的连接。"+" 国庆币数量: "+player.getItemQuantity(4000463)+" 点券总额: "+player.getCSPoints(-1);
//                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM Message] "+msgtext));
//                        FileoutputUtil.log("日志\\数据异常.log", msgtext);
//                        c.getSession().close(true);
//                    } else if ((c.getChannelServer().isCheckSp()) && (player.checkMaxStat())) {
//                        String msgtext = "玩家 "+player.getName()+"  属性点异常，服务器自动断开他的连接。当前角色总属性点为: "+player.getPlayerStats()+" 职业: "+player.getJob()+" 等级: "+player.getLevel();
//                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM Message] "+msgtext));
//                        FileoutputUtil.log("日志\\数据异常.log", msgtext);
//                        c.getSession().close(true);
//                    }
//                }
    }

    public static void ChangeMarketMap(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int chc = slea.readByte() + 1;
        int toMapId = slea.readInt();

        if ((toMapId >= 910000001) && (toMapId <= 910000022)) {
            if (c.getChannel() != chc) {
                if (chr.getMapId() != toMapId) {
                    MapleMap to = ChannelServer.getInstance(chc).getMapFactory().getMap(toMapId);
                    chr.setMap(to);
                    chr.changeChannel(chc);
                } else {
                    chr.changeChannel(chc);
                }
            } else {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(toMapId);
                chr.changeMap(to, to.getPortal(0));
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static boolean isBossMap(int mapid) {
        switch (mapid) {
            case 0:
            case 105100300:
            case 105100400:
            case 211070100:
            case 211070101:
            case 211070110:
            case 220080001:
            case 240040700:
            case 240060200:
            case 240060201:
            case 270050100:
            case 271040100:
            case 271040200:
            case 280030000:
            case 280030001:
            case 280030100:
            case 300030310:
            case 551030200:
            case 802000111:
            case 802000211:
            case 802000311:
            case 802000411:
            case 802000611:
            case 802000711:
            case 802000801:
            case 802000802:
            case 802000803:
            case 802000821:
            case 802000823:
                return true;
        }
        return false;
    }

    public static void showPlayerCash(SeekableLittleEndianAccessor slea, MapleClient c) {
        int accId = slea.readInt();
        int playerId = slea.readInt();
    }

    public static void quickBuyCashShopItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            //c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int accId = slea.readInt();
        int playerId = slea.readInt();
        int mode = slea.readInt();
        int cssn = slea.readInt();
        int toCharge = slea.readByte() == 1 ? 1 : 2;
        if ((chr.getId() != playerId) || (chr.getAccountID() != accId)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        switch (mode) {
            case 10:
                if ((chr.getCSPoints(toCharge) >= 600) && (chr.getStorage().getSlots() < 93)) {
                    chr.modifyCSPoints(toCharge, -600, false);
                    chr.getStorage().increaseSlots((byte) 4);
                    chr.getStorage().saveToDB();
                    c.getSession().write(MaplePacketCreator.playerCashUpdate(mode, toCharge, chr));
                } else {
                    chr.dropMessage(5, "扩充失败，点券余额不足或者仓库栏位已超过上限。");
                }
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                int iv = mode == 15 ? 5 : mode == 14 ? 4 : mode == 13 ? 3 : mode == 12 ? 2 : mode == 11 ? 1 : -1;
                if (iv > 0) {
                    MapleInventoryType tpye = MapleInventoryType.getByType((byte) iv);
                    if ((chr.getCSPoints(toCharge) >= 600) && (chr.getInventory(tpye).getSlotLimit() < 93)) {
                        chr.modifyCSPoints(toCharge, -600, false);
                        chr.getInventory(tpye).addSlot((byte) 4);
                        c.getSession().write(MaplePacketCreator.playerCashUpdate(mode, toCharge, chr));
                    } else {
                        chr.dropMessage(1, "扩充失败，点券余额不足或者栏位已超过上限。");
                    }
                } else {
                    chr.dropMessage(1, "扩充失败，扩充的类型不正确。");
                }
        }
    }

    public static void GUIDE_TRANSFER(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.skip(2);
        int mapid = slea.readInt();
        chr.changeMap(ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid));

        switch (mapid) {
            case 102000003:
                NPCScriptManager.getInstance().start(c, 10202);
                break;
        }
    }

    public static void SpawnArrowsTurret(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte side = slea.readByte();
        Point pos = new Point();
        pos.x = slea.readInt();
        pos.y = slea.readInt();

        for (MapleArrowsTurret AT : chr.getMap().getAllArrowsTurrets()) {
            if (AT.getOwnerId() == chr.getId()) {
                chr.getMap().removeMapObject(AT);
                chr.getMap().broadcastMessage(SkillPacket.cancelArrowsTurret(AT));
                break;
            }
        }
        MapleArrowsTurret tospawn = new MapleArrowsTurret(chr, side, pos);
        chr.getMap().spawnArrowsTurret(tospawn);
    }

    public static void ArrowsTurretAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int startime = (int) System.currentTimeMillis();
        int type = slea.readShort();
        if (type == 4) {
            slea.skip(4);//cid
            slea.skip(4);//unk
            slea.skip(4);//time?
            int skillid = slea.readInt();
            int side = slea.readByte();
            slea.skip(1);
            int slevel = slea.readShort();
            int unk = slea.readInt();
            Point pos = new Point();
            Point spoint = new Point();
            spoint.x = slea.readInt();
            spoint.y = slea.readInt();
            boolean sp = skillid != 3121013;
            if (sp) {
                for (MapleArrowsTurret AT : chr.getMap().getAllArrowsTurrets()) {
                    if (AT.getOwnerId() == chr.getId()) {
                        pos = AT.getPosition();
                        break;
                    }
                }
            } else {
                pos = chr.getOldPosition();
            }
            chr.getMap().broadcastMessage(chr, SkillPacket.ArrowsTurretAttack(chr.getId(), chr.getMapId(), slevel, pos, side, spoint, sp), false);
        }
    }

    public static void getMonoid(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        int count = slea.readInt();
        int skill = slea.readInt();
        List<Integer> oid = new ArrayList();
        for (int i = 0; i < count; i++) {
            slea.skip(5);
            oid.add(slea.readInt());
            slea.skip(4);
        }
        switch (skill) {
            default:
                FileoutputUtil.log(FileoutputUtil.获取未处理被动技能, "技能ID：" + skill + " 循环次数：" + count + " 封包: " + slea.toString(true));
        }
    }

    public static void UpdateMacrSkill(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        slea.skip(1);
        int index = slea.readByte();
        int skillid = slea.readInt();
        int old = 0;
        int t = 1;
        String newstr;
        if (slea.available() > 0) {
            old = slea.readByte();
            t = 2;
        }
        for (int i = 0; i < t; i++) {
            String date = chr.getQuestNAdd(MapleQuest.getInstance(52554)).getCustomData();
            if (date == null) {
                date = "cmd0=0;cmd1=0;cmd2=0";
            }
            String[] StrArray = date.split(";");
            index = i == 0 ? index : old;
            skillid = i == 0 ? skillid : 0;
            StrArray[index] = "cmd" + index + "=" + skillid;
            newstr = StrArray[0] + ";" + StrArray[1] + ";" + StrArray[2];
            chr.send(MaplePacketCreator.UpdateMacrSkill(index, skillid));
            MapleQuest.getInstance(52554).forceStart(chr, 0, newstr);
        }
    }

    /**
     *
     * @param slea
     * @param c
     */
    public static void handleCharInfo(SeekableLittleEndianAccessor slea, MapleClient c) {

    }
}
