package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import handling.MaplePacketHandler;
import handling.vo.recv.CancelBuffRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelBuffHandler extends MaplePacketHandler<CancelBuffRecvVO> {


    @Override
    public void handlePacket(CancelBuffRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int sourceid = recvVO.getSourceId();
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
                chr.getClient().sendPacket(MaplePacketCreator.skillCancel(chr, sourceid));
            } else {
                chr.cancelEffect(skill.getEffect(1), false, -1L);
            }
        }
        chr.cancelEffect(skill.getEffect(1), false, -1L);
    }
}
