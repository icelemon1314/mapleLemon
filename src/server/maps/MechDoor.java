package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import tools.packet.SkillPacket;

public class MechDoor extends MapleMapObject {

    private final int owner;
    private final int partyid;
    private final int id;

    public MechDoor(MapleCharacter owner, Point pos, int id) {
        this.owner = owner.getId();
        this.partyid = (owner.getParty() == null ? 0 : owner.getParty().getId());
        setPosition(pos);
        this.id = id;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(SkillPacket.spawnMechDoor(this, false));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(SkillPacket.removeMechDoor(this, false));
    }

    public int getOwnerId() {
        return this.owner;
    }

    public int getPartyId() {
        return this.partyid;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
