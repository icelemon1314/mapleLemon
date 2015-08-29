package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import server.MaplePortal;
import tools.MaplePacketCreator;
import tools.packet.PartyPacket;

public class MapleDoor extends MapleMapObject {

    private final WeakReference<MapleCharacter> owner;
    private final MapleMap town;
    private final MaplePortal townPortal;
    private final MapleMap target;
    private final int skillId;
    private final int ownerId;
    private final Point targetPosition;
    public boolean first = true;

    public MapleDoor(MapleCharacter owner, Point targetPosition, int skillId) {
        super();
        this.owner = new WeakReference(owner);
        this.ownerId = owner.getId();
        this.target = owner.getMap();
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = this.target.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
    }

    public MapleDoor(MapleDoor origDoor) {
        super();
        this.owner = new WeakReference(origDoor.owner.get());
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = new Point(origDoor.targetPosition);
        this.skillId = origDoor.skillId;
        this.ownerId = origDoor.ownerId;
        setPosition(this.townPortal.getPosition());
    }

    public int getSkill() {
        return this.skillId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    private MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<>();

        for (final MaplePortal port : town.getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        Collections.sort(freePortals, new Comparator<MaplePortal>() {
            @Override
            public int compare(MaplePortal o1, MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                }
                if (o1.getId() == o2.getId()) {
                    return 0;
                }
                return 1;
            }
        });
        for (MapleMapObject obj : this.town.getAllDoorsThreadsafe()) {
            MapleDoor door = (MapleDoor) obj;
            if ((door.getOwner() != null) && (door.getOwner().getParty() != null) && (getOwner() != null) && (getOwner().getParty() != null) && (getOwner().getParty().getId() == door.getOwner().getParty().getId())) {
                return null;
            }
            freePortals.remove(door.getTownPortal());
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return (MaplePortal) freePortals.iterator().next();
    }

    @Override
    public void sendSpawnData(MapleClient client) { //召唤时空门
        if ((getOwner() == null) || (this.target == null) || (client.getPlayer() == null)) {
            return;
        }
        if ((this.target.getId() == client.getPlayer().getMapId()) || (getOwnerId() == client.getPlayer().getId()) || ((getOwner() != null) && (getOwner().getParty() != null) && (client.getPlayer().getParty() != null) && (getOwner().getParty().getId() == client.getPlayer().getParty().getId()))) {
            client.getSession().write(MaplePacketCreator.spawnDoor(getOwnerId(), getSkill(), this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition(), this.target.getId() == client.getPlayer().getMapId() ? first : false));
            if ((getOwner() != null) && (getOwner().getParty() != null) && (client.getPlayer().getParty() != null) && ((getOwnerId() == client.getPlayer().getId()) || (getOwner().getParty().getId() == client.getPlayer().getParty().getId()))) {
                client.getSession().write(PartyPacket.partyPortal(this.town.getId(), this.target.getId(), this.skillId, this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition(), first));
            }
            client.getSession().write(MaplePacketCreator.spawnPortal(this.town.getId(), this.target.getId(), this.skillId, this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition()));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if ((client.getPlayer() == null) || (getOwner() == null) || (this.target == null)) {
            return;
        }
        if ((this.target.getId() == client.getPlayer().getMapId()) || (getOwnerId() == client.getPlayer().getId()) || ((getOwner() != null) && (getOwner().getParty() != null) && (client.getPlayer().getParty() != null) && (getOwner().getParty().getId() == client.getPlayer().getParty().getId()))) {
            client.getSession().write(MaplePacketCreator.removeDoor(getOwnerId(), false));
            if ((getOwner() != null) && (getOwner().getParty() != null) && (client.getPlayer().getParty() != null) && ((getOwnerId() == client.getPlayer().getId()) || (getOwner().getParty().getId() == client.getPlayer().getParty().getId()))) {
                client.getSession().write(PartyPacket.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
            }
            client.getSession().write(MaplePacketCreator.spawnPortal(999999999, 999999999, 0, null));
        }
    }

    public void warp(MapleCharacter chr, boolean toTown) {
        if ((chr.getId() == getOwnerId()) || ((getOwner() != null) && (getOwner().getParty() != null) && (chr.getParty() != null) && (getOwner().getParty().getId() == chr.getParty().getId()))) {
            if (!toTown) {
                chr.changeMap(this.target, this.target.findClosestPortal(this.getTargetPosition()));
            } else {
                chr.changeMap(this.town, this.townPortal);
            }
        } else {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public MapleCharacter getOwner() {
        return (MapleCharacter) this.owner.get();
    }

    public MapleMap getTown() {
        return this.town;
    }

    public MaplePortal getTownPortal() {
        return this.townPortal;
    }

    public MapleMap getTarget() {
        return this.target;
    }

    public Point getTargetPosition() {
        return this.targetPosition;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
