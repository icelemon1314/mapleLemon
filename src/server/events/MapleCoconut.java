package server.events;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import server.Timer.EventTimer;
import tools.MaplePacketCreator;

public class MapleCoconut extends MapleEvent {

    private final List<MapleCoconuts> coconuts = new LinkedList();
    private final int[] coconutscore = new int[2];
    private int countBombing = 0;
    private int countFalling = 0;
    private int countStopped = 0;

    public MapleCoconut(int channel, MapleEventType type) {
        super(channel, type);
    }

    @Override
    public void finished(MapleCharacter chr) {
        //TODO FIX THIS
    }

    @Override
    public void reset() {
        super.reset();
        resetCoconutScore();
    }

    @Override
    public void unreset() {
        super.unreset();
        resetCoconutScore();
        setHittable(false);
    }

    @Override
    public void onMapLoad(MapleCharacter chr) {
        super.onMapLoad(chr);
        chr.getClient().getSession().write(MaplePacketCreator.coconutScore(getCoconutScore()));
    }

    public MapleCoconuts getCoconut(int id) {
        if (id >= this.coconuts.size()) {
            return null;
        }
        return (MapleCoconuts) this.coconuts.get(id);
    }

    public List<MapleCoconuts> getAllCoconuts() {
        return this.coconuts;
    }

    public void setHittable(boolean hittable) {
        for (MapleCoconuts nut : this.coconuts) {
            nut.setHittable(hittable);
        }
    }

    public int getBombings() {
        return this.countBombing;
    }

    public void bombCoconut() {
        this.countBombing -= 1;
    }

    public int getFalling() {
        return this.countFalling;
    }

    public void fallCoconut() {
        this.countFalling -= 1;
    }

    public int getStopped() {
        return this.countStopped;
    }

    public void stopCoconut() {
        this.countStopped -= 1;
    }

    public int[] getCoconutScore() {
        return this.coconutscore;
    }

    public int getMapleScore() {
        return this.coconutscore[0];
    }

    public int getStoryScore() {
        return this.coconutscore[1];
    }

    public void addMapleScore() {
        this.coconutscore[0] += 1;
    }

    public void addStoryScore() {
        this.coconutscore[1] += 1;
    }

    public void resetCoconutScore() {
        this.coconutscore[0] = 0;
        this.coconutscore[1] = 0;
        this.countBombing = 80;
        this.countFalling = 401;
        this.countStopped = 20;
        this.coconuts.clear();
        for (int i = 0; i < 506; i++) {
            this.coconuts.add(new MapleCoconuts());
        }
    }

    @Override
    public void startEvent() {
        reset();
        setHittable(true);
        getMap(0).broadcastMessage(MaplePacketCreator.serverMessageNotice("The event has started!!"));
        getMap(0).broadcastMessage(MaplePacketCreator.hitCoconut(true, 0, 0));
        getMap(0).broadcastMessage(MaplePacketCreator.getClock(300));

        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleCoconut.this.getMapleScore() == MapleCoconut.this.getStoryScore()) {
                    MapleCoconut.this.bonusTime();
                } else {
                    for (MapleCharacter chr : MapleCoconut.this.getMap(0).getCharactersThreadsafe()) {
                        if (chr.getTeam() == (MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore() ? 0 : 1)) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    MapleCoconut.this.warpOut();
                }
            }
        }, 300000L);
    }

    public void bonusTime() {
        getMap(0).broadcastMessage(MaplePacketCreator.getClock(60));
        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleCoconut.this.getMapleScore() == MapleCoconut.this.getStoryScore()) {
                    for (MapleCharacter chr : MapleCoconut.this.getMap(0).getCharactersThreadsafe()) {
                        chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                        chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                    }
                    MapleCoconut.this.warpOut();
                } else {
                    for (MapleCharacter chr : MapleCoconut.this.getMap(0).getCharactersThreadsafe()) {
                        if (chr.getTeam() == (MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore() ? 0 : 1)) {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/victory"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Victory"));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.showEffect("event/coconut/lose"));
                            chr.getClient().getSession().write(MaplePacketCreator.playSound("Coconut/Failed"));
                        }
                    }
                    MapleCoconut.this.warpOut();
                }
            }
        }, 60000L);
    }

    public void warpOut() {
        setHittable(false);
        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                for (MapleCharacter chr : MapleCoconut.this.getMap(0).getCharactersThreadsafe()) {
                    if (((MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore()) && (chr.getTeam() == 0)) || ((MapleCoconut.this.getStoryScore() > MapleCoconut.this.getMapleScore()) && (chr.getTeam() == 1))) {
                        MapleEvent.givePrize(chr);
                    }
                    MapleCoconut.this.warpBack(chr);
                }
                MapleCoconut.this.unreset();
            }
        }, 10000L);
    }

    public static class MapleCoconuts {

        private int hits = 0;
        private boolean hittable = false;
        private boolean stopped = false;
        private long hittime = System.currentTimeMillis();

        public void hit() {
            this.hittime = (System.currentTimeMillis() + 1000L);
            this.hits += 1;
        }

        public int getHits() {
            return this.hits;
        }

        public void resetHits() {
            this.hits = 0;
        }

        public boolean isHittable() {
            return this.hittable;
        }

        public void setHittable(boolean hittable) {
            this.hittable = hittable;
        }

        public boolean isStopped() {
            return this.stopped;
        }

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        public long getHitTime() {
            return this.hittime;
        }
    }
}
