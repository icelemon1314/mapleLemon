package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.MaplePacketHandler;
import handling.vo.recv.SkillEffectRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class SkillEffectHandler extends MaplePacketHandler<SkillEffectRecvVO> {


    @Override
    public void handlePacket(SkillEffectRecvVO recvVO, MapleClient c) {
        // 33 39 41 40 00 05 A9 06
        MapleCharacter chr = c.getPlayer();
        int skillId = recvVO.getSkillId();
        byte level = recvVO.getLevel();
        byte display = recvVO.getDisplay();
        byte direction = recvVO.getDirection();
//        byte speed = slea.readByte();
        Point position = recvVO.getPosition();
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
}
