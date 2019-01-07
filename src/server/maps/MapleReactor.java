package server.maps;

import client.MapleClient;
import java.awt.Rectangle;
import scripting.reactor.ReactorScriptManager;
import server.Timer.MapTimer;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleReactor extends MapleMapObject {

    private final int rid;
    private final MapleReactorStats stats;
    private byte state = 0;
    private byte facingDirection = 0;
    private int delay = -1;
    private MapleMap map;
    private String name = "";
    private boolean timerActive = false;
    private boolean alive = true;
    private boolean custom = false;

    public MapleReactor(MapleReactorStats stats, int rid) {
        this.stats = stats;
        this.rid = rid;
    }

    public void setCustom(boolean c) {
        this.custom = c;
    }

    public boolean isCustom() {
        return this.custom;
    }

    public void setFacingDirection(byte facingDirection) {
        this.facingDirection = facingDirection;
    }

    public byte getFacingDirection() {
        return this.facingDirection;
    }

    public void setTimerActive(boolean active) {
        this.timerActive = active;
    }

    public boolean isTimerActive() {
        return this.timerActive;
    }

    public int getReactorId() {
        return this.rid;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return this.state;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return this.delay;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.REACTOR;
    }

    public int getReactorType() {
        return this.stats.getType(this.state);
    }

    public byte getTouch() {
        return this.stats.canTouch(this.state);
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public MapleMap getMap() {
        return this.map;
    }

    public Pair<Integer, Integer> getReactItem() {
        return this.stats.getReactItem(this.state);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.destroyReactor(this));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnReactor(this));
    }

    public void forceStartReactor(MapleClient c) {
        ReactorScriptManager.getInstance().act(c, this);
    }

    public void forceHitReactor(byte newState) {
        setState(newState);
        setTimerActive(false);
        this.map.broadcastMessage(MaplePacketCreator.triggerReactor(this, 0));
    }

    public void hitReactor(MapleClient c) {
        hitReactor(0, (short) 0, c);
    }

    public void forceTrigger() {
        this.map.broadcastMessage(MaplePacketCreator.triggerReactor(this, 0));
    }

    public void delayedDestroyReactor(long delay) {
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                MapleReactor.this.map.destroyReactor(MapleReactor.this.getObjectId());
            }
        }, delay);
    }

    public void hitReactor(int charPos, short stance, MapleClient c) {
        if ((this.stats.getType(this.state) < 999) && (this.stats.getType(this.state) != -1)) {
            byte oldState = this.state;
            if ((this.stats.getType(this.state) != 2) || ((charPos != 0) && (charPos != 2))) {
                this.state = this.stats.getNextState(this.state);

                if ((this.stats.getNextState(this.state) == -1) || (this.stats.getType(this.state) == 999)) {
                    if (((this.stats.getType(this.state) < 100) || (this.stats.getType(this.state) == 999)) && (this.delay > 0)) {
                        this.map.destroyReactor(getObjectId());
                    } else {
                        this.map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                    }

                    ReactorScriptManager.getInstance().act(c, this);
                } else {
                    boolean done = false;
                    this.map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                    if ((this.state == this.stats.getNextState(this.state)) || (this.rid == 2618000) || (this.rid == 2309000)) {
                        if (this.rid > 200011) {
                            ReactorScriptManager.getInstance().act(c, this);
                        }
                        done = true;
                    }
                    if (this.stats.getTimeOut(this.state) > 0) {
                        if ((!done) && (this.rid > 200011)) {
                            ReactorScriptManager.getInstance().act(c, this);
                        }
                        scheduleSetState(this.state, oldState, this.stats.getTimeOut(this.state));
                    }
                }
            }
        }
    }

    public Rectangle getArea() {
        int height = this.stats.getBR().y - this.stats.getTL().y;
        int width = this.stats.getBR().x - this.stats.getTL().x;
        int origX = getTruePosition().x + this.stats.getTL().x;
        int origY = getTruePosition().y + this.stats.getTL().y;
        return new Rectangle(origX, origY, width, height);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "反应堆 工作ID:" + getObjectId() + " ReactorID: " + this.rid + " 坐标: " + getPosition().x + "/" + getPosition().y + " 状态: " + this.state + " 类型: " + this.stats.getType(this.state);
    }

    public void delayedHitReactor(final MapleClient c, long delay) {
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                MapleReactor.this.hitReactor(c);
            }
        }, delay);
    }

    public void scheduleSetState(final byte oldState, final byte newState, long delay) {
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleReactor.this.state == oldState) {
                    MapleReactor.this.forceHitReactor(newState);
                }
            }
        }, delay);
    }
}
