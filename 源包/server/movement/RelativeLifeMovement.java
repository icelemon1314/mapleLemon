package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public class RelativeLifeMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond;
    private short fh;

    public Point getPixelsPerSecond() {
        return this.pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public int getFH() {
        return this.fh;
    }

    public void setFH(short fh) {
        this.fh = fh;
    }

    public RelativeLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
