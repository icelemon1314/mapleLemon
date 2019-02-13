package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.MaplePacketHandler;
import handling.vo.recv.SpecialSkillRecvVO;
import server.MapleStatEffect;
import server.maps.FieldLimitType;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class SpecialSkillHandler extends MaplePacketHandler<SpecialSkillRecvVO> {


    @Override
    public void handlePacket(SpecialSkillRecvVO recvVO, MapleClient c) {
        // 31 CE CC 10 00 01 80 00 00
        // 31 2B 46 0F 00 14 00 00
        // 31 BC BC 21 00 04 01 A6 86 01 00 58 02
        // 31 5A 43 23 00 14 49 FB 16 08  时空门
        // 31 39 41 40 00 05 00 00
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        int skillid = recvVO.getSkillId();
        int skillLevel = recvVO.getSkillLevel();
        if (chr.isShowPacket()) {
            chr.dropMessage(5,"[SpecialSkill] - 技能ID: " + skillid + " 技能等级: " + skillLevel);
        }
        Point pos = recvVO.getPosition();
        Skill skill = SkillFactory.getSkill(skillid);
        if ((skill == null)) {
            chr.dropMessage(5,"[SpecialSkill] -   不存在的技能ID" + skillid);
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        skillLevel = chr.getTotalSkillLevel(skillid);
        MapleStatEffect effect = skill.getEffect(skillLevel); // 获取技能效果
        if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
            if (chr.skillisCooling(skillid)) {//TODO 修复客户端冷却时间不一致
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
//            c.sendPacket(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown(chr)));
            chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
        }
        MapleLogger.info("看是否有特需处理的BUFF");
        if (effect.is时空门()) {
            MapleLogger.info("释放时空们");
            if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                effect.applyTo(c.getPlayer(), pos);
            } else {
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        } else {
//            int mountid = MapleStatEffect.parseMountInfo(c.getPlayer(), skill.getId());
//            if ((mountid != 0) && (mountid != GameConstants.getMountItem(skill.getId(), chr)) && (!chr.isIntern()) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -122) == null)
//                    && (!GameConstants.isMountItemAvailable(mountid, chr.getJob()))) {
//                c.sendPacket(MaplePacketCreator.enableActions());
//                return;
//            }
            MapleLogger.info("释放技能效果！");
            effect.applyTo(chr, pos);
        }
    }
}
