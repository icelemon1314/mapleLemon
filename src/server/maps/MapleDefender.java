package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.ScheduledFuture;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.MaplePacketCreator;

public class MapleDefender extends MapleMapObject {

    private Rectangle mistPosition;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist;
    private boolean isPoisonMist;
    private boolean isRecoverMist;
    private int skillDelay;
    private int skilllevel;
    private int ownerId;
    private int mistType;
    private ScheduledFuture<?> schedule = null;
    private ScheduledFuture<?> poisonSchedule = null;
    private boolean isHolyFountain;
    private int healCount;
    private boolean givebuff = false;
    private boolean isfaceleft = false;

    public MapleDefender(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.ownerId = mob.getId();
        this.skill = skill;
        this.skilllevel = skill.getSkillLevel();
        this.isMobMist = true;
        this.isPoisonMist = true;
        this.isRecoverMist = false;
        this.mistType = 0;
        this.skillDelay = 0;
    }

    public MapleDefender(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.ownerId = owner.getId();
        this.source = source;
        this.skillDelay = 10;
        this.isMobMist = false;
        this.isPoisonMist = false;
        this.isRecoverMist = false;
        this.healCount = 0;
        this.isHolyFountain = false;
        this.isfaceleft = owner.isFacingLeft();
        this.skilllevel = owner.getTotalSkillLevel(SkillFactory.getSkill(source.getSourceId()));
        switch (source.getSourceId()) { //TODO 可以在这里添加 MIST 技能类型
            case 2311011:
                this.mistType = 0;
                this.healCount = source.getY();
                this.isHolyFountain = true;
                break;
            case 4121015:
                this.mistType = 0;
                break;
            case 4221006:
                this.mistType = 3;
                this.skillDelay = 3;
                this.isPoisonMist = true;
                break;
            case 1076:
            case 2100010:
            case 2111003:
                this.mistType = 0;
                this.isPoisonMist = true;
                break;
        }
    }

    public MapleDefender(Rectangle mistPosition, MapleCharacter owner) {
        this.mistPosition = mistPosition;
        this.ownerId = owner.getId();
        this.source = new MapleStatEffect();
        this.source.setSourceId(2111003);
        this.skilllevel = 30;
        this.mistType = 0;
        this.isMobMist = false;
        this.isPoisonMist = false;
        this.skillDelay = 10;
        this.isfaceleft = owner.isFacingLeft();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return this.mistPosition.getLocation();
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(this.source.getSourceId());
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

    public boolean isfaceleft() {
        return this.isfaceleft;
    }

    public boolean isMobMist() {
        return this.isMobMist;
    }

    public boolean isPoisonMist() {
        return this.isPoisonMist;
    }

    public boolean isGivebuff() {
        return this.givebuff;
    }

    public boolean isRecoverMist() {
        return this.isRecoverMist;
    }

    public boolean isHolyFountain() {
        return this.isHolyFountain;
    }

    public int getHealCount() {
        return isHolyFountain() ? this.healCount : 0;
    }

    public void setHealCount(int count) {
        this.healCount = count;
    }

    public int getMistType() {
        return this.mistType;
    }

    public int getSkillDelay() {
        return this.skillDelay;
    }

    public int getSkillLevel() {
        return this.skilllevel;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public MobSkill getMobSkill() {
        return this.skill;
    }

    public Rectangle getBox() {
        return this.mistPosition;
    }

    public MapleStatEffect getSource() {
        return this.source;
    }

    @Override
    public void setPosition(Point position) {
    }

    public byte[] fakeSpawnData(int level) {
        return MaplePacketCreator.spawnMist(this);
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        c.getSession().write(MaplePacketCreator.spawnMist(this));
        if (!this.isMobMist && this.getSourceSkill().getId() == 36121007) {
            c.getSession().write(MaplePacketCreator.showChair(this.getOwnerId(), 3010587));
        }
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        c.getSession().write(MaplePacketCreator.removeMist(getObjectId(), false));
    }

    public boolean makeChanceResult() {
        return this.source.makeChanceResult();
    }
}
