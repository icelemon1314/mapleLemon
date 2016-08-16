package scripting.event;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import javax.script.Invocable;
import javax.script.ScriptException;
import org.apache.log4j.Logger;
import server.Randomizer;
import server.Timer.EventTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.squad.MapleSquad;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

public class EventManager {

    private static final int[] eventChannel = new int[2];
    private final Invocable iv;
    private final int channel;
    private final Map<String, EventInstanceManager> instances = new WeakHashMap();
    private final Properties props = new Properties();
    private final String name;
    private static final Logger log = Logger.getLogger(EventManager.class);

    public EventManager(ChannelServer cserv, Invocable iv, String name) {
        this.iv = iv;
        this.channel = cserv.getChannel();
        this.name = name;
    }

    public void cancel() {
        try {
            this.iv.invokeFunction("cancelSchedule", new Object[]{(Object) null});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : cancelSchedule:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : cancelSchedule:\r\n").append(ex).toString());
        }
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay) {
        return EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{(Object) null});
                } catch (ScriptException | NoSuchMethodException ex) {
                    EventManager.log.error("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                    FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                }
            }
        }, delay);
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
        return EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{eim});
                } catch (ScriptException | NoSuchMethodException ex) {
                    EventManager.log.error("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                    FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                }
            }
        }, delay);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {
            @Override
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{(Object) null});
                } catch (ScriptException | NoSuchMethodException ex) {
                    EventManager.log.error("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                    FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\r\n" + ex);
                }
            }
        }, timestamp);
    }

    public int getCurentMin(){
        Calendar cal = Calendar.getInstance();
        return cal.get(12);
    }

    public void log(String str){
        FileoutputUtil.log(str);
    }

    public int getChannel() {
        return this.channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }

    public EventInstanceManager getInstance(String name) {
        return (EventInstanceManager) this.instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(this.instances.values());
    }

    /**
     * 创建一个实例
     * @param name
     * @return
     */
    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name, this.channel);
        this.instances.put(name, ret);
        return ret;
    }

    public void disposeInstance(String name) {
        this.instances.remove(name);
        if ((getProperty("state") != null) && (this.instances.isEmpty())) {
            setProperty("state", "0");
        }
        if ((getProperty("leader") != null) && (this.instances.isEmpty()) && (getProperty("leader").equals("false"))) {
            setProperty("leader", "true");
        }
        if (this.name.equals("CWKPQ")) {
            MapleSquad squad = ChannelServer.getInstance(this.channel).getMapleSquad("CWKPQ");
            if (squad != null) {
                squad.clear();
                squad.copy();
            }
        }
    }

    public Invocable getIv() {
        return this.iv;
    }

    public void setProperty(String key, String value) {
        this.props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return this.props.getProperty(key);
    }

    public final Properties getProperties() {
        return this.props;
    }

    public String getName() {
        return this.name;
    }

    public void startInstance() {
        try {
            this.iv.invokeFunction("setup", new Object[]{(Object) null});
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
        }
    }

    public void startInstance_Solo(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerPlayer(chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
        }
    }

    public void startInstance(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerCarnivalParty(chr, chr.getMap(), (byte) 0);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
        }
    }

    public void startInstance_Party(String mapid, MapleCharacter chr, int maxlevel) {
        try {
            int averageLevel = 0;
            int size = 0;
            for (MaplePartyCharacter mpc : chr.getParty().getMembers()) {
                if (mpc.isOnline() && mpc.getMapid() == chr.getMap().getId() && mpc.getChannel() == chr.getMap().getChannel()) {
                    averageLevel += mpc.getLevel();
                    size++;
                }
            }
            if (size <= 0) {
                return;
            }
            averageLevel /= size;
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid, Math.min(maxlevel, averageLevel <= 0 ? chr.getLevel() : averageLevel)});
            eim.registerParty(chr.getParty(), chr.getMap());
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
        }
    }

    public void startInstance_Party(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerParty(chr.getParty(), chr.getMap());
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\r\n").append(ex).toString());
        }
    }

    public void startInstance(MapleCharacter character, String leader) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerPlayer(character);
            eim.setProperty("leader", leader);
            eim.setProperty("guildid", String.valueOf(character.getGuildId()));
            setProperty("guildid", String.valueOf(character.getGuildId()));
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-Guild:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-Guild:\r\n").append(ex).toString());
        }
    }

    public void startInstance_CharID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{character.getId()});
            eim.registerPlayer(character);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\r\n").append(ex).toString());
        }
    }

    public void startInstance_CharMapID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{character.getId(), character.getMapId()});
            eim.registerPlayer(character);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\r\n").append(ex).toString());
        }
    }

    public void startInstance(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager)  this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerPlayer(character);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-character:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-character:\r\n").append(ex).toString());
        }
    }

    public void startInstance(MapleParty party, MapleMap map) {
        startInstance(party, map, 255);
    }

    public void startInstance(MapleParty party, MapleMap map, int maxLevel) {
        try {
            int averageLevel = 0;
            int size = 0;
            for (MaplePartyCharacter mpc : party.getMembers()) {
                if (mpc.isOnline() && mpc.getMapid() == map.getId() && mpc.getChannel() == map.getChannel()) {
                    averageLevel += mpc.getLevel();
                    size++;
                }
            }
            if (size <= 0) {
                return;
            }
            averageLevel /= size;
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{Math.min(maxLevel, averageLevel), party.getId()});
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-partyid:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-partyid:\r\n").append(ex).toString());
        } catch (NoSuchMethodException ex) {
            startInstance_NoID(party, map, ex);
        }
    }

    public void startInstance_NoID(MapleParty party, MapleMap map) {
        startInstance_NoID(party, map, null);
    }

    public void startInstance_NoID(MapleParty party, MapleMap map, Exception old) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerParty(party, map);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-party:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-party:\r\n").append(ex).append("\r\n").append(old == null ? "no old exception" : old).toString());
        }
    }

    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            this.iv.invokeFunction("setup", new Object[]{eim});
            eim.setProperty("leader", leader);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-leader:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-leader:\r\n").append(ex).toString());
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map) {
        startInstance(squad, map, -1);
    }

    public void startInstance(MapleSquad squad, MapleMap map, int questID) {
        if (squad.getStatus() == 0) {
            return;
        }
        if (!squad.getLeader().isGM()) {
            int mapid = map.getId();
            int chrSize = 0;
            for (String chr : squad.getMembers()) {
                MapleCharacter player = squad.getChar(chr);
                if ((player != null) && (player.getMapId() == mapid)) {
                    chrSize++;
                }
            }
            if (chrSize < squad.getType().i) {
                squad.getLeader().dropMessage(5, new StringBuilder().append("远征队中人员少于 ").append(squad.getType().i).append(" 人，无法开始远征任务。注意必须队伍中的角色在线且在同一地图。当前人数: ").append(chrSize).toString());
                return;
            }
            if ((this.name.equals("CWKPQ")) && (squad.getJobs().size() < 5)) {
                squad.getLeader().dropMessage(5, "远征队中成员职业的类型小于5种，无法开始远征任务。");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{squad.getLeaderName()});
            eim.registerSquad(squad, map, questID);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\r\n").append(ex).toString());
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map, String bossid) {
        startInstance(squad, map, bossid, true);
    }

    public void startInstance(MapleSquad squad, MapleMap map, String bossid, boolean checkSize) {
        if (squad.getStatus() == 0) {
            return;
        }
        if ((!squad.getLeader().isGM()) && (checkSize)) {
            int mapid = map.getId();
            int chrSize = 0;
            for (String chr : squad.getMembers()) {
                MapleCharacter player = squad.getChar(chr);
                if ((player != null) && (player.getMapId() == mapid)) {
                    chrSize++;
                }
            }
            if (chrSize < squad.getType().i) {
                squad.getLeader().dropMessage(5, new StringBuilder().append("远征队中人员少于 ").append(squad.getType().i).append(" 人，无法开始远征任务。注意必须队伍中的角色在线且在同一地图。当前人数: ").append(chrSize).toString());
                return;
            }
            if ((this.name.equals("CWKPQ")) && (squad.getJobs().size() < 5)) {
                squad.getLeader().dropMessage(5, "远征队中成员职业的类型小于5种，无法开始远征任务。");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{squad.getLeaderName()});
            eim.registerSquad(squad, map, bossid);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\r\n").append(ex).toString());
            FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\r\n").append(ex).toString());
        }
    }

    public void warpAllPlayer(int from, int to) {
        MapleMap tomap = getMapFactory().getMap(to);
        MapleMap frommap = getMapFactory().getMap(from);
        List<MapleCharacter> list = frommap.getCharactersThreadsafe();
        if ((tomap != null) && (list != null) && (frommap.getCharactersSize() > 0)) {
            for (MapleMapObject mmo : list) {
                ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }

    public MapleMapFactory getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    public OverrideMonsterStats newMonsterStats() {
        return new OverrideMonsterStats();
    }

    public List<MapleCharacter> newCharList() {
        return new ArrayList();
    }

    public MapleMonster getMonster(int id) {
        return MapleLifeFactory.getMonster(id);
    }

    public MapleReactor getReactor(int id) {
        return new MapleReactor(MapleReactorFactory.getReactor(id), id);
    }

    public byte[] sendBoat(boolean isEnter){
        return MaplePacketCreator.boatPacket(isEnter);
    }

    public byte[] sendMonsterBoat(boolean isEnter){return MaplePacketCreator.MonsterBoat(isEnter);}

    public byte[] musicChange(String name){
        return MaplePacketCreator.musicChange(name);
    }

    public void broadcastYellowMsg(String msg) {
        getChannelServer().broadcastPacket(MaplePacketCreator.yellowChat(msg));
    }

    public void broadcastServerMsg(String msg) {
        getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, msg));
    }

    public void broadcastServerMsg(int type, String msg, boolean weather) {
        if (!weather) {
            getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
        } else {
            for (MapleMap load : getMapFactory().getAllMaps()) {
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }
    }

    public boolean scheduleRandomEvent() {
        boolean omg = false;
        for (int i = 0; i < eventChannel.length; i++) {
            omg |= scheduleRandomEventInChannel(eventChannel[i]);
        }
        return omg;
    }

    public boolean scheduleRandomEventInChannel(int chz) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if ((cs == null) || (cs.getEvent() > -1)) {
            return false;
        }
        MapleEventType t = null;
        while (t == null) {
            for (MapleEventType x : MapleEventType.values()) {
                if ((Randomizer.nextInt(MapleEventType.values().length) == 0) && (x != MapleEventType.OxQuiz)) {
                    t = x;
                    break;
                }
            }
        }
        String msg = MapleEvent.scheduleEvent(t, cs);
        if (msg.length() > 0) {
            broadcastYellowMsg(msg);
            return false;
        }
        EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (cs.getEvent() >= 0) {
                    MapleEvent.setEvent(cs, true);
                }
            }
        }, 180000L);

        return true;
    }

    public void setWorldEvent() {
        for (int i = 0; i < eventChannel.length; i++) {
            eventChannel[i] = (Randomizer.nextInt(ChannelServer.getAllInstances().size() - 4) + 2 + i);
        }
    }

}
