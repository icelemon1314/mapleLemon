package handling.channel.handler;

import client.*;
import handling.MaplePacketHandler;
import handling.vo.recv.DistributeSpRecvVO;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.EnumMap;
import java.util.Map;

public class DistributeSpHandler extends MaplePacketHandler<DistributeSpRecvVO> {


    @Override
    public void handlePacket(DistributeSpRecvVO recvVO, MapleClient c) {
        // 30 40 42 0F 00
        MapleCharacter chr = c.getPlayer();
        int skillid = recvVO.getSkillId();
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
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        for (Pair<String, Integer> ski : skill.getRequiredSkills()) {
            switch (ski.left) {
                case "level":
                    if (chr.getLevel() < ski.right) {
                        if (chr.isShowPacket()) {
                            chr.dropMessage(5, "加技能点错误 - 技能要求等级: " + ski.right + " 当前角色等级: " + chr.getLevel());
                        }
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    break;
                default:
                    int left = Integer.parseInt(ski.left);
                    if (chr.getSkillLevel(SkillFactory.getSkill(left)) < ski.right) {
                        if (chr.isShowPacket()) {
                            chr.dropMessage(5, "加技能点错误 - 前置技能: " + left + " - " + SkillFactory.getSkillName(left) + " 的技能等级不足.");
                        }
                        c.sendPacket(MaplePacketCreator.enableActions());
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
            c.sendPacket(MaplePacketCreator.enableActions());
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
            c.sendPacket(MaplePacketCreator.updatePlayerStats(statup, true, chr));
            chr.changeSingleSkillLevel(skill, (byte) (curLevel + 1), chr.getMasterLevel(skill));
        } else {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, "加技能点错误 - SP点数不足够或者技能不是改角色的技能.");
            }
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}
