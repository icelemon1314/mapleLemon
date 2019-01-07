package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public abstract interface LifeMovementFragment {

    public abstract void serialize(MaplePacketLittleEndianWriter paramMaplePacketLittleEndianWriter);

    public abstract Point getPosition();
}
