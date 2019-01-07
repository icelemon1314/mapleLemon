/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import tools.packet.SkillPacket;

/**
 * 箭矢炮盘类
 * @author Fairyms
 */
public final class MapleArrowsTurret extends AnimatedMapleMapObject {

    private final int ownerid;
    private final int skillLevel;
    private final int ownerLevel;
    private final int skillId;
    private final byte side;
    private final MapleMap map;
    private ScheduledFuture<?> schedule = null;
    private ScheduledFuture<?> poisonSchedule = null;


    /**
     * 箭矢炮盘
     * @param owner
     * @param side
     * @param pos
     */
    
    public MapleArrowsTurret(MapleCharacter owner,  byte side ,Point pos) {
        this.map = owner.getMap();
        this.ownerid = owner.getId();
        this.ownerLevel = owner.getLevel();
        this.skillId = 3121013;
        this.skillLevel = owner.getSkillLevel(skillId);
        this.side = side;
        setPosition(pos);
    }

    public MapleCharacter getOwner() {
        return this.map.getCharacterById(this.ownerid);
    }

    public int getOwnerId() {
        return this.ownerid;
    }

    public int getOwnerLevel() {
        return this.ownerLevel;
    }

    public int getSkillId() {
        return this.skillId;
    }
    public int getSide() {
        return this.side;
    }

    public MapleMap getMap() {
        return this.map;
    }
        
    public int getSkillLevel() {
        return this.skillLevel;
    }
        public void setSchedule(ScheduledFuture<?> s) {
        this.schedule = s;
    }

    public ScheduledFuture<?> getSchedule() {
        return this.schedule;
    }

    public void setPoisonSchedule(ScheduledFuture<?> s) {
        this.poisonSchedule = s;
    }

    public ScheduledFuture<?> getPoisonSchedule() {
        return this.poisonSchedule;
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.ARROWS_TURRET;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(SkillPacket.spawnArrowsTurret(this));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(SkillPacket.cancelArrowsTurret(this));
    }
}
