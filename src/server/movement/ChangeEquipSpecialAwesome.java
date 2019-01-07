package server.movement;

import java.awt.Point;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ChangeEquipSpecialAwesome implements LifeMovementFragment {

    private final int type;
    private final int wui;

    public ChangeEquipSpecialAwesome(int type, int wui) {
        this.type = type;
        this.wui = wui;
    }
    
    @Override
    public Point getPosition() {
        return new Point(0, 0);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(this.type);
        lew.write(this.wui);
    }
}
