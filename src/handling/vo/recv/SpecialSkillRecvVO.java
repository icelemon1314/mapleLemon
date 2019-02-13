package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class SpecialSkillRecvVO extends MaplePacketRecvVO {

    Integer skillId;
    Byte skillLevel;
    Point position;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        skillId = slea.readInt();
        skillLevel = slea.readByte();
        if (slea.available() == 4) {
            position = new Point(slea.readShort(), slea.readShort());
        }
    }

    public Integer getSkillId() {
        return skillId;
    }

    public Byte getSkillLevel() {
        return skillLevel;
    }

    public Point getPosition() {
        return position;
    }
}
