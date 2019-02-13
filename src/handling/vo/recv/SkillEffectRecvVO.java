package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class SkillEffectRecvVO extends MaplePacketRecvVO {

    Integer skillId;
    Byte level;
    Byte display;
    Byte direction;
    Byte speed;
    Point position;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        // 33 39 41 40 00 05 A9 06
        skillId = slea.readInt();
        level = slea.readByte();
        display = slea.readByte();
        direction = slea.readByte();
        speed = slea.readByte();

        if (slea.available() == 4L) {
            position = slea.readPos();
        } else if (slea.available() == 8) {
            position = slea.readPos();
        } else {
            position = null;
        }
    }

    public Integer getSkillId() {
        return skillId;
    }

    public Byte getLevel() {
        return level;
    }

    public Byte getDisplay() {
        return display;
    }

    public Byte getDirection() {
        return direction;
    }

    public Point getPosition() {
        return position;
    }

    public Byte getSpeed() {
        return speed;
    }
}
