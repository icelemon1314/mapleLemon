package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public class TeleportMovement extends AbsoluteLifeMovement {

    public TeleportMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writePos(getPixelsPerSecond());
        lew.write(getNewstate());
    }
}
