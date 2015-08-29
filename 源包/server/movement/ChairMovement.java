package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ChairMovement extends AbstractLifeMovement {

    private int newfh;
    private short unk;

    public ChairMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }
    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return unk;
    }

    public int getNewFH() {
        return this.newfh;
    }

    public void setNewFH(int fh) {
        this.newfh = fh;
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        //lew.writeShort(this.newfh);
        lew.writeShort(this.unk);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
