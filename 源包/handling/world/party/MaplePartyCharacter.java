package handling.world.party;

import client.MapleCharacter;
import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import server.maps.MapleDoor;

public class MaplePartyCharacter implements Serializable {

    private static final long serialVersionUID = 6215463252132450750L;
    private String name;
    private int id;
    private int level;
    private int channel;
    private int jobid;
    private int mapid;
    private int doorTown = 999999999;
    private int doorTarget = 999999999;
    private int doorSkill = 0;
    private MapleCharacter chr;
    private Point doorPosition = new Point(0, 0);
    private boolean online;

    public MaplePartyCharacter(MapleCharacter maplechar) {
        name = maplechar.getName();
        level = maplechar.getLevel();
        channel = maplechar.getClient().getChannel();
        id = maplechar.getId();
        jobid = maplechar.getJob();
        mapid = maplechar.getMapId();
        online = true;
        chr = maplechar;

        List doors = maplechar.getDoors();
        if (doors.size() > 0) {
            MapleDoor door = (MapleDoor) doors.get(0);
            doorTown = door.getTown().getId();
            doorTarget = door.getTarget().getId();
            doorSkill = door.getSkill();
            doorPosition = door.getTargetPosition();
        } else {
            doorPosition = maplechar.getPosition();
        }
    }

    public MaplePartyCharacter() {
        name = "";
    }

    public int getLevel() {
        return level;
    }

    public MapleCharacter getChr() {
        return chr;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getMapid() {
        return mapid;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobid;
    }

    public int getDoorTown() {
        return doorTown;
    }

    public int getDoorTarget() {
        return doorTarget;
    }

    public int getDoorSkill() {
        return doorSkill;
    }

    public Point getDoorPosition() {
        return doorPosition;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MaplePartyCharacter other = (MaplePartyCharacter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
