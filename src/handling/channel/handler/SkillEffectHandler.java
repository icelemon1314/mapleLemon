package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.MaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class SkillEffectHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 33 39 41 40 00 05 A9 06
        MapleCharacter chr = c.getPlayer();
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
}
