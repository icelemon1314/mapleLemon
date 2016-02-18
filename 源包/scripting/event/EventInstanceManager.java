package scripting.event;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.SkillFactory;
import handling.channel.ChannelServer;
import handling.world.WrodlPartyService;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartySearch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.ScriptException;
import org.apache.log4j.Logger;
import server.MapleCarnivalParty;
import server.MapleItemInformationProvider;
import server.Timer.EventTimer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import server.squad.MapleSquad;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.UIPacket;

public class EventInstanceManager {

    private List<MapleCharacter> chars = new LinkedList();
    private List<Integer> dced = new LinkedList();
    private List<MapleMonster> mobs = new LinkedList();
    private Map<Integer, Integer> killCount = new HashMap();
    private final EventManager em;
    private final int channel;
    private final String name;
    private Properties props = new Properties();
    private long timeStarted = 0L;
    private long eventTime = 0L;
    private List<Integer> mapIds = new LinkedList();
    private List<Boolean> isInstanced = new LinkedList();
    private ScheduledFuture<?> eventTimer;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = this.mutex.readLock();
    private final Lock wL = this.mutex.writeLock();
    private boolean disposed = false;
    private static final Logger log = Logger.getLogger(EventInstanceManager.class);

    public EventInstanceManager(EventManager em, String name, int channel) {
        this.em = em;
        this.name = name;
        this.channel = channel;
    }

    /**
     * 注册角色
     * @param chr
     */
    public void registerPlayer(MapleCharacter chr) {
        if ((this.disposed) || (chr == null)) {
            return;
        }
        try {
            this.wL.lock();
            try {
                this.chars.add(chr);
            } finally {
                this.wL.unlock();
            }
            chr.setEventInstance(this);
            this.em.getIv().invokeFunction("playerEntry", new Object[]{this, chr});
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerEntry:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerEntry:\r\n").append(ex).toString());
        }
    }

    /**
     * 玩家换地图
     * @param chr
     * @param mapid
     */
    public void changedMap(MapleCharacter chr, int mapid) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("changedMap", new Object[]{this, chr, mapid});
        } catch (NullPointerException npe) {
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : changedMap:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : changedMap:\r\n").append(ex).toString());
        }
    }

    public void timeOut(long delay, final EventInstanceManager eim) {
        if ((this.disposed) || (eim == null)) {
            return;
        }
        this.eventTimer = EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if ((EventInstanceManager.this.disposed) || (eim == null) || (EventInstanceManager.this.em == null)) {
                    return;
                }
                try {
                    EventInstanceManager.this.em.getIv().invokeFunction("scheduledTimeout", new Object[]{eim});
                } catch (ScriptException | NoSuchMethodException ex) {
                    EventInstanceManager.log.error("Event name" + EventInstanceManager.this.em.getName() + ", Instance name : " + EventInstanceManager.this.name + ", method Name : scheduledTimeout:\r\n" + ex);
                    FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Event name" + EventInstanceManager.this.em.getName() + ", Instance name : " + EventInstanceManager.this.name + ", method Name : scheduledTimeout:\r\n" + ex);
                }
            }
        }, delay);
    }

    public void stopEventTimer() {
        this.eventTime = 0L;
        this.timeStarted = 0L;
        if (this.eventTimer != null) {
            this.eventTimer.cancel(false);
        }
    }

    public void restartEventTimer(long time) {
        try {
            if (this.disposed) {
                return;
            }
            this.timeStarted = System.currentTimeMillis();
            this.eventTime = time;
            if (this.eventTimer != null) {
                this.eventTimer.cancel(false);
            }
            this.eventTimer = null;
            int timesend = (int) time / 1000;
            for (MapleCharacter chr : getPlayers()) {
                if (this.name.startsWith("PVP")) {
                    chr.getClient().getSession().write(MaplePacketCreator.getPVPClock(Integer.parseInt(getProperty("type")), timesend));
                } else {
                    chr.getClient().getSession().write(MaplePacketCreator.getClock(timesend));
                }
            }
            timeOut(time, this);
        } catch (NumberFormatException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : restartEventTimer:\r\n").append(ex).toString());
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        }
    }

    public void startEventTimer(long time) {
        restartEventTimer(time);
    }

    public void startEventClock(long time) {
        if (this.disposed) {
            return;
        }
        int timesend = (int) time / 1000;
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock(timesend));
        }
    }

    public void stopEventClock() {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(MaplePacketCreator.stopClock());
        }
    }

    public boolean isTimerStarted() {
        return (this.eventTime > 0L) && (this.timeStarted > 0L);
    }

    public long getTimeLeft() {
        return this.eventTime - (System.currentTimeMillis() - this.timeStarted);
    }

    public void registerParty(MapleParty party, MapleMap map) {
        if (this.disposed) {
            return;
        }
        for (MaplePartyCharacter pc : party.getMembers()) {
            registerPlayer(map.getCharacterById(pc.getId()));
        }
        PartySearch ps = WrodlPartyService.getInstance().getSearch(party);
        if (ps != null) {
            WrodlPartyService.getInstance().removeSearch(ps, "开始组队任务，组队广告已被删除。");
        }
    }

    public void unregisterPlayer(MapleCharacter chr) {
        if (this.disposed) {
            chr.setEventInstance(null);
            return;
        }
        this.wL.lock();
        try {
            unregisterPlayer_NoLock(chr);
        } finally {
            this.wL.unlock();
        }
    }

    private boolean unregisterPlayer_NoLock(MapleCharacter chr) {
        if (this.name.equals("CWKPQ")) {
            MapleSquad squad = ChannelServer.getInstance(this.channel).getMapleSquad("CWKPQ");
            if (squad != null) {
                squad.removeMember(chr.getName());
                if (squad.getLeaderName().equals(chr.getName())) {
                    this.em.setProperty("leader", "false");
                }
            }
        }
        chr.setEventInstance(null);
        if (this.disposed) {
            return false;
        }
        if (this.chars.contains(chr)) {
            this.chars.remove(chr);
            return true;
        }
        return false;
    }

    public boolean disposeIfPlayerBelow(byte size, int towarp) {
        if (this.disposed) {
            return true;
        }
        MapleMap map = null;
        if (towarp > 0) {
            map = getMapFactory().getMap(towarp);
        }

        this.wL.lock();
        try {
            if ((this.chars != null) && (this.chars.size() <= size)) {
                List<MapleCharacter> chrs = new LinkedList(this.chars);
                for (MapleCharacter chr : chrs) {
                    if (chr == null) {
                        continue;
                    }
                    unregisterPlayer_NoLock(chr);
                    if (towarp > 0 && map != null) {
                        chr.changeMap(map, map.getPortal(0));
                    }
                }
                dispose_NoLock();

                return true;
            }
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        } finally {
            this.wL.unlock();
        }
        return false;
    }

    public void saveBossQuest(int points) {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            MapleQuestStatus record = chr.getQuestNAdd(MapleQuest.getInstance(150001));
            if (record.getCustomData() != null) {
                record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
            } else {
                record.setCustomData(String.valueOf(points));
            }
            chr.modifyCSPoints(2, points / 5, true);
        }
    }

    public void saveNX(int points) {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.modifyCSPoints(2, points, true);
        }
    }

    public void EventGainNX() {
        if (this.disposed) {
            return;
        }
        int averlevel = 0;
        Iterator<MapleCharacter> partyer = getPlayers().iterator();
        while (partyer.hasNext()) {
            MapleCharacter i = partyer.next();
            averlevel += i.getLevel();
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.modifyCSPoints(1, averlevel / 250 * 1000, true);
        }
    }

    public List<MapleCharacter> getPlayers() {
        if (this.disposed) {
            return Collections.emptyList();
        }
        this.rL.lock();
        try {
            LinkedList localLinkedList = new LinkedList(this.chars);
            return localLinkedList;
        } finally {
            this.rL.unlock();
        }
    }

    public List<Integer> getDisconnected() {
        return this.dced;
    }

    public int getPlayerCount() {
        if (this.disposed) {
            return 0;
        }
        return this.chars.size();
    }

    /**
     * 注册一个怪物
     * @param mob
     */
    public void registerMonster(MapleMonster mob) {
        if (this.disposed) {
            return;
        }
        this.mobs.add(mob);
        mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) {
        mob.setEventInstance(null);
        if (this.disposed) {
            return;
        }
        if (this.mobs.contains(mob)) {
            this.mobs.remove(mob);
        }
        if (this.mobs.isEmpty()) {
            try {
                this.em.getIv().invokeFunction("allMonstersDead", new Object[]{this});
            } catch (ScriptException | NoSuchMethodException ex) {
                log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : allMonstersDead:\r\n").append(ex).toString());
                FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : allMonstersDead:\r\n").append(ex).toString());
            }
        }
    }

    public void playerKilled(MapleCharacter chr) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("playerDead", new Object[]{this, chr});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerDead:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerDead:\r\n").append(ex).toString());
        }
    }

    public boolean revivePlayer(MapleCharacter chr) {
        if (this.disposed) {
            return false;
        }
        try {
            Object b = this.em.getIv().invokeFunction("playerRevive", new Object[]{this, chr});
            if ((b instanceof Boolean)) {
                return ((Boolean) b);
            }
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerRevive:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerRevive:\r\n").append(ex).toString());
        }
        return true;
    }

    public void playerDisconnected(MapleCharacter chr, int idz) {
        if (this.disposed) {
            return;
        }
        byte ret;
        try {
            ret = ((Double) this.em.getIv().invokeFunction("playerDisconnected", new Object[]{this, chr})).byteValue();
        } catch (ScriptException | NoSuchMethodException e) {
            ret = 0;
        }

        this.wL.lock();
        try {
            if (this.disposed) {
                return;
            }
            if ((chr == null) || (chr.isAlive())) {
                this.dced.add(idz);
            }
            if (chr != null) {
                unregisterPlayer_NoLock(chr);
            }
            if (ret == 0) {
                if (getPlayerCount() <= 0) {
                    dispose_NoLock();
                }
            } else if (((ret > 0) && (getPlayerCount() < ret)) || ((ret < 0) && ((isLeader(chr)) || (getPlayerCount() < ret * -1)))) {
                List<MapleCharacter> chrs = new LinkedList(this.chars);
                for (MapleCharacter player : chrs) {
                    if (player.getId() != idz) {
                        removePlayer(player);
                    }
                }
                dispose_NoLock();
            }
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        } finally {
            this.wL.unlock();
        }
    }

    public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
        if (this.disposed) {
            return;
        }
        try {
            int inc = ((Double) this.em.getIv().invokeFunction("monsterValue", new Object[]{this, mob.getId()})).intValue();
            if ((this.disposed) || (chr == null)) {
                return;
            }
            Integer kc = this.killCount.get(Integer.valueOf(chr.getId()));
            if (kc == null) {
                kc = inc;
            } else {
                kc = kc + inc;
            }
            this.killCount.put(chr.getId(), kc);
            if ((chr.getCarnivalParty() != null) && ((mob.getStats().getPoint() > 0) || (mob.getStats().getCP() > 0))) {
                this.em.getIv().invokeFunction("monsterKilled", new Object[]{this, chr, mob.getStats().getCP() > 0 ? mob.getStats().getCP() : mob.getStats().getPoint()});
            }
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        }
    }

    public void monsterDamaged(MapleCharacter chr, MapleMonster mob, int damage) {
        if ((this.disposed) || (mob.getId() != 9700037)) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("monsterDamaged", new Object[]{this, chr, mob.getId(), damage});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        }
    }

    public void addPVPScore(MapleCharacter chr, int score) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("addPVPScore", new Object[]{this, chr, score});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em == null ? "null" : this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : monsterValue:\r\n").append(ex).toString());
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        }
    }

    public int getKillCount(MapleCharacter chr) {
        if (this.disposed) {
            return 0;
        }
        Integer kc = this.killCount.get(Integer.valueOf(chr.getId()));
        if (kc == null) {
            return 0;
        }
        return kc;
    }

    public void dispose_NoLock() {
        if ((this.disposed) || (this.em == null)) {
            return;
        }
        String emName = this.em.getName();
        try {
            this.disposed = true;
            for (MapleCharacter chr : this.chars) {
                chr.setEventInstance(null);
            }
            this.chars.clear();
            this.chars = null;
            if (this.mobs.size() >= 1) {
                for (MapleMonster mob : this.mobs) {
                    if (mob != null) {
                        mob.setEventInstance(null);
                    }
                }
            }
            this.mobs.clear();
            this.mobs = null;
            this.killCount.clear();
            this.killCount = null;
            this.dced.clear();
            this.dced = null;
            this.timeStarted = 0L;
            this.eventTime = 0L;
            this.props.clear();
            this.props = null;
            for (int i = 0; i < this.mapIds.size(); i++) {
                if ((this.isInstanced.get(i))) {
                    getMapFactory().removeInstanceMap((this.mapIds.get(i)));
                }
            }
            this.mapIds.clear();
            this.mapIds = null;
            this.isInstanced.clear();
            this.isInstanced = null;
            this.em.disposeInstance(this.name);
        } catch (Exception e) {
            log.error(new StringBuilder().append("Caused by : ").append(emName).append(" instance name: ").append(this.name).append(" method: dispose:").toString());
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, e);
        }
    }

    public void dispose() {
        this.wL.lock();
        try {
            dispose_NoLock();
        } finally {
            this.wL.unlock();
        }
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }

    public List<MapleMonster> getMobs() {
        return this.mobs;
    }

    public void broadcastPlayerMsg(int type, String msg) {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.dropMessage(type, msg);
        }
    }

    public List<Pair<Integer, String>> newPair() {
        return new ArrayList();
    }

    public void addToPair(List<Pair<Integer, String>> e, int e1, String e2) {
        e.add(new Pair(e1, e2));
    }

    public List<Pair<Integer, MapleCharacter>> newPair_chr() {
        return new ArrayList();
    }

    public void addToPair_chr(List<Pair<Integer, MapleCharacter>> e, int e1, MapleCharacter e2) {
        e.add(new Pair(e1, e2));
    }

    public void broadcastPacket(byte[] packet) {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(packet);
        }
    }

    public void broadcastTeamPacket(byte[] packet, int team) {
        if (this.disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            if (chr.getTeam() == team) {
                chr.getClient().getSession().write(packet);
            }
        }
    }

    /**
     * 创建一个地图实例
     * @param mapid
     * @return
     */
    public MapleMap createInstanceMap(int mapid) {
        if (this.disposed) {
            return null;
        }
        int assignedid = EventScriptManager.getNewInstanceMapId();
        this.mapIds.add(assignedid);
        this.isInstanced.add(true);
        return getMapFactory().CreateInstanceMap(mapid, true, true, true, assignedid);
    }

    public MapleMap createInstanceMapS(int mapid) {
        if (this.disposed) {
            return null;
        }
        int assignedid = EventScriptManager.getNewInstanceMapId();
        this.mapIds.add(assignedid);
        this.isInstanced.add(true);
        return getMapFactory().CreateInstanceMap(mapid, false, false, false, assignedid);
    }

    public MapleMap setInstanceMap(int mapid) {
        if (this.disposed) {
            return getMapFactory().getMap(mapid);
        }
        this.mapIds.add(mapid);
        this.isInstanced.add(false);
        return getMapFactory().getMap(mapid);
    }

    public MapleMapFactory getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    public MapleMap getMapInstance(int args) {
        if (this.disposed) {
            return null;
        }
        try {
            boolean instanced = false;
            int trueMapID;
            if (args >= this.mapIds.size()) {
                trueMapID = args;
            } else {
                trueMapID = (this.mapIds.get(args));
                instanced = (this.isInstanced.get(args));
            }
            MapleMap map;
            if (!instanced) {
                map = getMapFactory().getMap(trueMapID);
                if (map == null) {
                    return null;
                }
                if ((map.getCharactersSize() == 0)
                        && (this.em.getProperty("shuffleReactors") != null) && (this.em.getProperty("shuffleReactors").equals("true"))) {
                    map.shuffleReactors();
                }
            } else {
                map = getMapFactory().getInstanceMap(trueMapID);
                if (map == null) {
                    return null;
                }
                if ((map.getCharactersSize() == 0)
                        && (this.em.getProperty("shuffleReactors") != null) && (this.em.getProperty("shuffleReactors").equals("true"))) {
                    map.shuffleReactors();
                }
            }

            return map;
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.Event_ScriptEx_Log, ex);
        }
        return null;
    }

    public void schedule(final String methodName, long delay) {
        if (this.disposed) {
            return;
        }
        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if ((EventInstanceManager.this.disposed) || (EventInstanceManager.this == null) || (EventInstanceManager.this.em == null)) {
                    return;
                }
                try {
                    EventInstanceManager.this.em.getIv().invokeFunction(methodName, new Object[]{EventInstanceManager.this});
                } catch (NullPointerException npe) {
                } catch (ScriptException | NoSuchMethodException ex) {
                    EventInstanceManager.log.error("Event name" + EventInstanceManager.this.em.getName() + ", Instance name : " + EventInstanceManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                    FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Event name" + EventInstanceManager.this.em.getName() + ", Instance name : " + EventInstanceManager.this.name + ", method Name(schedule) : " + methodName + " :\n" + ex);
                }
            }
        }, delay*1000);
    }

    public String getName() {
        return this.name;
    }

    public void setProperty(String key, String value) {
        if (this.disposed) {
            return;
        }
        this.props.setProperty(key, value);
    }

    public Object setProperty(String key, String value, boolean prev) {
        if (this.disposed) {
            return null;
        }
        return this.props.setProperty(key, value);
    }

    public String getProperty(String key) {
        if (this.disposed) {
            return "";
        }
        return this.props.getProperty(key);
    }

    public Properties getProperties() {
        return this.props;
    }

    public void leftParty(MapleCharacter chr) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("leftParty", new Object[]{this, chr});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : leftParty:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : leftParty:\n").append(ex).toString());
        }
    }

    public void disbandParty() {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("disbandParty", new Object[]{this});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : disbandParty:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : disbandParty:\n").append(ex).toString());
        }
    }

    public void finishPQ() {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("clearPQ", new Object[]{this});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : clearPQ:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : clearPQ:\n").append(ex).toString());
        }
    }

    public void removePlayer(MapleCharacter chr) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("playerExit", new Object[]{this, chr});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerExit:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : playerExit:\n").append(ex).toString());
        }
    }

    public void registerCarnivalParty(MapleCharacter leader, MapleMap map, byte team) {
        if (this.disposed) {
            return;
        }
        leader.clearCarnivalRequests();
        List characters = new LinkedList();
        MapleParty party = leader.getParty();
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter c = map.getCharacterById(pc.getId());
            if (c != null) {
                characters.add(c);
                registerPlayer(c);
                c.resetCP();
            }
        }
        PartySearch ps = WrodlPartyService.getInstance().getSearch(party);
        if (ps != null) {
            WrodlPartyService.getInstance().removeSearch(ps, "The Party Listing has been removed because the Party Quest started.");
        }
        MapleCarnivalParty carnivalParty = new MapleCarnivalParty(leader, characters, team);
        try {
            this.em.getIv().invokeFunction("registerCarnivalParty", new Object[]{this, carnivalParty});
        } catch (ScriptException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : registerCarnivalParty:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : registerCarnivalParty:\n").append(ex).toString());
        } catch (NoSuchMethodException ex) {
        }
    }

    public void onMapLoad(MapleCharacter chr) {
        if (this.disposed) {
            return;
        }
        try {
            this.em.getIv().invokeFunction("onMapLoad", new Object[]{this, chr});
        } catch (ScriptException ex) {
            log.error(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : onMapLoad:\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : onMapLoad:\n").append(ex).toString());
        } catch (NoSuchMethodException ex) {
        }
    }

    public boolean isLeader(MapleCharacter chr) {
        return (chr != null) && (chr.getParty() != null) && (chr.getParty().getLeader().getId() == chr.getId());
    }

    public void registerSquad(MapleSquad squad, MapleMap map, int questID) {
        if (this.disposed) {
            return;
        }
        int mapid = map.getId();
        for (String chr : squad.getMembers()) {
            MapleCharacter player = squad.getChar(chr);
            if ((player != null) && (player.getMapId() == mapid)) {
                if (questID > 0) {
                    player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData(String.valueOf(System.currentTimeMillis()));
                }
                registerPlayer(player);
                if (player.getParty() != null) {
                    PartySearch ps = WrodlPartyService.getInstance().getSearch(player.getParty());
                    if (ps != null) {
                        WrodlPartyService.getInstance().removeSearch(ps, "开始组队任务，组队广告已被删除。");
                    }
                }
            }
        }
        squad.setStatus((byte) 2);
        squad.getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
    }

    public void registerSquad(MapleSquad squad, MapleMap map, String bossid) {
        if (this.disposed) {
            return;
        }
        int mapid = map.getId();
        for (String chr : squad.getMembers()) {
            MapleCharacter player = squad.getChar(chr);
            if ((player != null) && (player.getMapId() == mapid)) {
                if (bossid != null) {
                    player.setPQLog(bossid);
                }
                registerPlayer(player);
                if (player.getParty() != null) {
                    PartySearch ps = WrodlPartyService.getInstance().getSearch(player.getParty());
                    if (ps != null) {
                        WrodlPartyService.getInstance().removeSearch(ps, "开始组队任务，组队广告已被删除。");
                    }
                }
            }
        }
        squad.setStatus((byte) 2);
        squad.getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
    }

    public boolean isDisconnected(MapleCharacter chr) {
        if (this.disposed) {
            return false;
        }
        return this.dced.contains(chr.getId());
    }

    public void removeDisconnected(int id) {
        if (this.disposed) {
            return;
        }
        if (this.dced.contains(id)) {
            this.dced.remove(id);
        }
    }

    public EventManager getEventManager() {
        return this.em;
    }

    public void applyBuff(MapleCharacter chr, int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(chr);
    }

    public void applySkill(MapleCharacter chr, int id) {
        SkillFactory.getSkill(id).getEffect(1).applyTo(chr);
    }
}
