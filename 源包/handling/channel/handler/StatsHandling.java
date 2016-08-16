package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import constants.GameConstants;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class StatsHandling {

    /**
     * 分配能力点
     * @param slea
     * @param c
     * @param chr
     */
    public static void DistributeAP(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 2E 40 00 00 00
        Map statupdate = new EnumMap(MapleStat.class);
        c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));

        PlayerStats stat = chr.getStat();
        int job = chr.getJob();
        if (chr.getRemainingAp() > 0) {
            switch (slea.readInt()) {
                case 0x40:
                    stat.setStr((short) (stat.getStr() + 1), chr);
                    statupdate.put(MapleStat.力量, (long) stat.getStr());
                    break;
                case 0x80:
                    stat.setDex((short) (stat.getDex() + 1), chr);
                    statupdate.put(MapleStat.敏捷, (long) stat.getDex());
                    break;
                case 0x100:
                    stat.setInt((short) (stat.getInt() + 1), chr);
                    statupdate.put(MapleStat.智力, (long) stat.getInt());
                    break;
                case 0x200:
                    stat.setLuk((short) (stat.getLuk() + 1), chr);
                    statupdate.put(MapleStat.运气, (long) stat.getLuk());
                    break;
                case 0x800:
                    int maxhp = stat.getMaxHp();
                    if (GameConstants.is新手职业(job)) {
                        maxhp += Randomizer.rand(8, 12);
                    } else if (((job >= 100) && (job <= 132)) ) {
                        maxhp += Randomizer.rand(36, 42);
                    } else if (((job >= 200) && (job <= 232))) {
                        maxhp += Randomizer.rand(10, 20);
                    } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434))) {
                        maxhp += Randomizer.rand(16, 20);
                    } else if (((job >= 510) && (job <= 512)) ) {
                        maxhp += Randomizer.rand(28, 32);
                    } else if (((job >= 500) && (job <= 532)) ) {
                        maxhp += Randomizer.rand(18, 22);
                    } else {
                        maxhp += Randomizer.rand(18, 26);
                    }
                    maxhp = Math.min(chr.getMaxHpForSever(), Math.abs(maxhp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxHp(maxhp, chr);
                    statupdate.put(MapleStat.MAXHP, (long) maxhp);
                    break;
                case 0x2000:
                    int maxmp = stat.getMaxMp();
                    if ((chr.getHpApUsed() >= 10000) || (stat.getMaxMp() >= chr.getMaxMpForSever())) {
                        return;
                    }
                    if (GameConstants.is新手职业(job)) {
                        maxmp += Randomizer.rand(6, 8);
                    } else {
                        if (((job >= 200) && (job <= 232))) {
                            maxmp += Randomizer.rand(38, 40);
                        } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)) || ((job >= 500) && (job <= 532))) {
                            maxmp += Randomizer.rand(10, 12);
                        } else if (((job >= 100) && (job <= 132))) {
                            maxmp += Randomizer.rand(6, 9);
                        } else {
                            maxmp += Randomizer.rand(6, 12);
                        }
                    }
                    maxmp = Math.min(chr.getMaxMpForSever(), Math.abs(maxmp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxMp(maxmp, chr);
                    statupdate.put(MapleStat.MAXMP, (long) maxmp);
                    break;
                default:
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(new EnumMap(MapleStat.class), true, chr));
                    return;
            }
            chr.setRemainingAp((short) (chr.getRemainingAp() - 1));
            statupdate.put(MapleStat.AVAILABLEAP, (long) chr.getRemainingAp());
            c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));
        }
    }

    /**
     * 添加技能点
     * @param slea
     * @param c
     * @param chr
     */
    public static void DistributeSP(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 30 40 42 0F 00
        int skillid = slea.readInt();
        boolean isBeginnerSkill = false;
        int remainingSp = chr.getRemainingSp();
        if (skillid == 1000 || skillid == 1001 || skillid == 1002) {
            boolean resistance = (skillid / 10000 == 3000) || (skillid / 10000 == 3001);
            int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + 1000));
            int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + 1001));
            int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + (resistance ? 2 : 1002)));
            remainingSp = Math.min(chr.getLevel() - 1, resistance ? 9 : 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
            isBeginnerSkill = true;
        }
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, "加技能点错误 - 技能为空 当前技能ID: " + skillid);
            }
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        for (Pair<String, Integer> ski : skill.getRequiredSkills()) {
            switch (ski.left) {
                case "level":
                    if (chr.getLevel() < ski.right) {
                        if (chr.isShowPacket()) {
                            chr.dropMessage(5, "加技能点错误 - 技能要求等级: " + ski.right + " 当前角色等级: " + chr.getLevel());
                        }
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    break;
                default:
                    int left = Integer.parseInt(ski.left);
                    if (chr.getSkillLevel(SkillFactory.getSkill(left)) < ski.right) {
                        if (chr.isShowPacket()) {
                            chr.dropMessage(5, "加技能点错误 - 前置技能: " + left + " - " + SkillFactory.getSkillName(left) + " 的技能等级不足.");
                        }
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    break;
            }
            }
            int maxlevel = skill.getMaxLevel();
            int curLevel = chr.getSkillLevel(skill);

            if ((skill.isInvisible()) && (curLevel == 0) && (((maxlevel < 10) && (!isBeginnerSkill)))) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "加技能点错误 - 3");
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (chr.isShowPacket()) {
                chr.dropMessage(5, "开始加技能点 - 技能ID: " + skillid + "当前技能等级: " + curLevel + " 该技能最大等级: " + maxlevel);
            }
            if ((remainingSp >= 0) && (curLevel + 1 <= maxlevel) && (skill.canBeLearnedBy(chr.getJob()))) {
                if (!isBeginnerSkill) {
                    chr.setRemainingSp(chr.getRemainingSp() - 1);
                }
                Map statup = new EnumMap(MapleStat.class);
                statup.put(MapleStat.AVAILABLESP, (long)chr.getRemainingSp());
                c.getSession().write(MaplePacketCreator.updatePlayerStats(statup, true, chr));
                chr.changeSingleSkillLevel(skill, (byte) (curLevel + 1), chr.getMasterLevel(skill));
            } else {
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "加技能点错误 - SP点数不足够或者技能不是改角色的技能.");
                }
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        }
}
