package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import constants.GameConstants;
import java.awt.Point;
import java.sql.Time;
import server.MapleStatEffect;
import tools.packet.SummonPacket;

public final class MapleSummon extends AnimatedMapleMapObject {

    private int ownerid;
    private int skillLevel;
    private int ownerLevel;
    private int skillId;
    private MapleMap map;
    private int hp = 1;
    private boolean changedMap = false;
    private SummonMovementType movementType;
    private int lastSummonTickCount;
    private byte Summon_tickResetCount;
    private long Server_ClientSummonTickDiff;
    private long lastAttackTime;
    private boolean isControl = false;
    private boolean isScream = false;
    private int SummonTime;
    private boolean isfaceleft;
    private long SummonStratTime;
    private int linkmonid = 0;

    public MapleSummon(MapleCharacter owner, MapleStatEffect effect, Point pos, SummonMovementType movementType) {
        this(owner, effect.getSourceId(), effect.getLevel(), pos, movementType);
    }

    public MapleSummon(MapleCharacter owner, int sourceid, int level, Point pos, SummonMovementType movementType) {
        this.map = owner.getMap();
        this.ownerid = owner.getId();
        this.ownerLevel = owner.getLevel();
        this.skillId = sourceid;
        this.skillLevel = level;
        this.movementType = movementType;
        this.SummonStratTime = System.currentTimeMillis();
        setPosition(pos);
        this.isfaceleft = owner.isFacingLeft();
        if (!is替身术()) {
            this.lastSummonTickCount = 0;
            this.Summon_tickResetCount = 0;
            this.Server_ClientSummonTickDiff = 0L;
            this.lastAttackTime = 0L;
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(SummonPacket.spawnSummon(this, true));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(SummonPacket.removeSummon(this, false));
    }

    public void updateMap(MapleMap map) {
        this.map = map;
    }

    public int getSummonTime() {
        return SummonTime;
    }

    public int SummonTime(int bufftime) {
        SummonTime = bufftime - (int) (System.currentTimeMillis() - SummonStratTime);
        return SummonTime;
    }

    public MapleCharacter getOwner() {
        return this.map.getCharacterById(this.ownerid);
    }

    public int getOwnerId() {
        return this.ownerid;
    }

    public boolean setControl(boolean ss) {//灵魂统治开关
        return this.isControl = ss;
    }

    public void setLinkmonid(int ss) {//灵魂统治开关
        this.linkmonid = ss;
    }

    public int getLinkmonid() {//灵魂统治开关
        return this.linkmonid;
    }

    public boolean getControl() {
        return isControl;
    }

    public boolean setScream(boolean ss) {
        return this.isScream = ss;
    }

    public boolean getScream() {
        return isScream;
    }

    public int getOwnerLevel() {
        return this.ownerLevel;
    }

    public int getSkillId() {
        return this.skillId;
    }

    public int getSkillLevel() {
        return this.skillLevel;
    }

    public int getSummonHp() {
        return this.hp;
    }

    public boolean isfacingleft() {
        return isfaceleft;
    }

    public void setSummonHp(int hp) {
        this.hp = hp;
    }

    public void addSummonHp(int delta) {
        this.hp += delta;
    }

    public boolean is替身术() {
        switch (this.skillId) {
            case 3221014:
            case 4341006:
                return true;
        }
        return false;
    }

    public boolean isMultiAttack() {//TODO 召唤兽是否是一次性攻击
        switch (this.skillId) {
            case 2111010:
                return false;
        }
        return (this.skillId == 61111002) || (this.skillId == 35111002) || (this.skillId == 35121003) || ((this.skillId != 33101008) && (this.skillId < 35000000)) || (this.skillId == 35111001) || (this.skillId == 35111009) || (this.skillId == 35111010);
    }

    public boolean is神箭幻影() {
        return this.skillId == 3221014;
    }

    public boolean is灵魂助力() {
        return this.skillId == 1301013;
    }

    public boolean is分身召唤() {
        return (this.skillId == 4341006) ;
    }


    public boolean is战法重生() {
        return this.skillId == 32111006;
    }

    public SummonMovementType getMovementType() {
        return this.movementType;
    }

    public byte getAttackType() {
        switch (this.skillId) {
            case 13111024:
            case 13120007:
            case 35111002:
            case 35111005:
            case 35121010:
                return 0;
            case 3221014:
            case 4111007:
            case 4211007:
            case 12111022:
                return 1;
            case 1301013:
                return 2;
            case 23111008:
            case 23111009:
            case 23111010:
            case 35111001:
            case 35111009:
            case 35111010:
                return 3;
            case 35121009:
                return 5;
            case 35121003:
                return 6;
            case 14111010:
                return 7;
            case 5210015:
            case 5210016:
            case 5210017:
            case 5210018:
                return 9;
        }
        return 1;
    }

    public byte getRemoveStatus() {
        switch (this.skillId) {
            case 5321003:
            case 33101008:
            case 35111002:
            case 35111005:
            case 35111011:
            case 35121009:
            case 35121010:
            case 35121011:
                return 5;
            case 23111008:
            case 23111009:
            case 23111010:
            case 35111001:
            case 35111009:
            case 35111010:
            case 35121003:
                return 10;
        }
        return 0;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public void CheckSummonAttackFrequency(MapleCharacter chr, int tickcount) {
        int tickdifference = tickcount - this.lastSummonTickCount;
        long STime_TC = System.currentTimeMillis() - tickcount;
        long S_C_Difference = this.Server_ClientSummonTickDiff - STime_TC;
        this.Summon_tickResetCount = (byte) (this.Summon_tickResetCount + 1);
        if (this.Summon_tickResetCount > 4) {
            this.Summon_tickResetCount = 0;
            this.Server_ClientSummonTickDiff = STime_TC;
        }
        this.lastSummonTickCount = tickcount;
    }

    public void CheckPVPSummonAttackFrequency(MapleCharacter chr) {
        long tickdifference = System.currentTimeMillis() - this.lastAttackTime;
        if (tickdifference < SkillFactory.getSummonData(this.skillId).delay) {
        }
        this.lastAttackTime = System.currentTimeMillis();
    }

    public boolean isChangedMap() {
        return this.changedMap;
    }

    public void setChangedMap(boolean cm) {
        this.changedMap = cm;
    }
}
