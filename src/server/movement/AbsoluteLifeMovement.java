package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond;
    private Point offset;
    private short newfh, unk;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public Point getPixelsPerSecond() {
        return this.pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public Point getOffset() {
        return this.offset;
    }

    public void setOffset(Point wobble) {
        this.offset = wobble;
    }

    public int getNewFH() {
        return this.newfh;
    }

    public void setNewFH(short fh) {
        this.newfh = fh;
    }

    public void defaulted() {
        this.newfh = 0;
        unk = 0;
        this.pixelsPerSecond = new Point(0, 0);
        this.offset = new Point(0, 0);
    }

    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return unk;
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writePos(this.pixelsPerSecond);

        lew.writeShort(getDuration());
        lew.write(getNewstate());
        lew.writeShort(getNewFH());

    }
}
