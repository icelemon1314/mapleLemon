package server.maps;

import client.MapleClient;
import constants.GameConstants;
import java.awt.Point;

public abstract class MapleMapObject {

    private final Point position = new Point();
    private int objectId;

    public Point getPosition() {
        return new Point(position);
    }

    public Point getTruePosition() {
        return position;
    }

    public void setPosition(Point positions) {
        position.x = positions.x;
        position.y = positions.y;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int id) {
        objectId = id;
    }

    public Double getRange() {
        return GameConstants.maxViewRangeSq();
    }

    public abstract MapleMapObjectType getType();

    public abstract void sendSpawnData(MapleClient paramMapleClient);

    public abstract void sendDestroyData(MapleClient paramMapleClient);
}
