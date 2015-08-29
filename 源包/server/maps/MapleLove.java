package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import tools.MaplePacketCreator;

public class MapleLove extends MapleMapObject {

    private final Point pos;
    private final MapleCharacter owner;
    private final String text;
    private final int ft;
    private final int itemid;

    public MapleLove(MapleCharacter owner, Point pos, int ft, String text, int itemid) {
        this.owner = owner;
        this.pos = pos;
        this.text = text;
        this.ft = ft;
        this.itemid = itemid;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.LOVE;
    }

    @Override
    public Point getPosition() {
        return this.pos.getLocation();
    }

    public MapleCharacter getOwner() {
        return this.owner;
    }

    public int getItemId() {
        return this.itemid;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        c.getSession().write(MaplePacketCreator.spawnLove(getObjectId(), this.itemid, this.owner.getName(), this.text, this.pos, this.ft));
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        c.getSession().write(MaplePacketCreator.removeLove(getObjectId(), this.itemid));
    }
}
