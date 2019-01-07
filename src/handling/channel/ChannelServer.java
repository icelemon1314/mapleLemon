package handling.channel;

import client.MapleCharacter;
import constants.ServerConstants;
import constants.WorldConstants;
import database.DatabaseConnection;
import handling.MapleServerHandler;
import handling.login.LoginServer;
import handling.mina.MapleCodecFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import scripting.event.EventScriptManager;
import server.ManagerSin;
import server.ServerProperties;
import server.events.MapleCoconut;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.events.MapleSurvival;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.shops.HiredMerchant;
import server.shops.HiredMerchantSave;
import server.squad.MapleSquad;
import server.squad.MapleSquadType;
import tools.ConcurrentEnumMap;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

public class ChannelServer {

    public static long serverStartTime;
    private int cashRate;
    private int expRate;
    private int dropRate;
    private int traitRate;
    private int stateRate;
    private int statLimit;
    private int createGuildCost;
    private int globalRate;
    private int autoGain;
    private int autoNx;
    private int merchantTime;
    private short port;
    private static final short DEFAULT_PORT = 7575;
    private final int channel;
    private int running_MerchantID = 0;
    private int flags = 0;
    private int sharePrice = 0;
    private String ip;
    private String serverName;
    private boolean shutdown = false;
    private boolean finishedShutdown = false;
    private boolean MegaphoneMuteState = false;
    private boolean adminOnly = false;
    private boolean canPvp = false;
    private boolean shieldWardAll = false;
    private boolean autoPoints = false;
    private boolean checkSp = false;
    private boolean checkCash = false;
    private boolean useMapScript = false;//?
    private PlayerStorage players;
    private IoAcceptor acceptor;
    private final MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private String eventSMs;
    private final AramiaFireWorks works = new AramiaFireWorks();
    private static final Map<Integer, ChannelServer> instances = new HashMap();
    private final Map<MapleSquadType, MapleSquad> mapleSquads = new ConcurrentEnumMap(MapleSquadType.class);
    private final Map<Integer, HiredMerchant> merchants = new HashMap();
    private final List<PlayerNPC> playerNPCs = new LinkedList();
    private ReentrantReadWriteLock merchLock = null;
    private ReentrantReadWriteLock.ReadLock mcReadLock = null;
    private ReentrantReadWriteLock.WriteLock mcWriteLock = null;
    private int eventmap = -1;
    private final Map<MapleEventType, MapleEvent> events = new EnumMap(MapleEventType.class);
    private String ShopPack;
    Connection shareCon;
    private static final Logger log = Logger.getLogger(ChannelServer.class);
    private final ManagerSin a = new ManagerSin();

    private ChannelServer(int channel) {
        this.channel = channel;
        mapFactory = new MapleMapFactory(channel);

        merchLock = new ReentrantReadWriteLock(true);
        mcReadLock = merchLock.readLock();
        mcWriteLock = merchLock.writeLock();
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet(instances.keySet());
    }

    public void loadEvents() {
        if (!events.isEmpty()) {
            return;
        }
        events.put(MapleEventType.CokePlay, new MapleCoconut(channel, MapleEventType.CokePlay));
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball));
        events.put(MapleEventType.Survival, new MapleSurvival(channel, MapleEventType.Survival));
    }

    public void run_startup_configurations() {
        setChannel(channel);
        try {
            cashRate = ServerProperties.getProperty("CASH_RATE", 1);
            expRate = ServerProperties.getProperty("EXP_RATE",1);
            setExpRate(expRate);
            dropRate = ServerProperties.getProperty("DROP_RATE",1);
            setDropRate(dropRate);
            globalRate = ServerProperties.getProperty("GLOBAL_RATE", 1);
            traitRate = ServerProperties.getProperty("TRAIT_RATE", 1);
            stateRate = ServerProperties.getProperty("STATE_RATE", 4);
            statLimit = ServerProperties.getProperty("statLimit", 999);
            autoNx = Integer.parseInt(ServerProperties.getProperty("autoNx", "10"));
            autoGain = Integer.parseInt(ServerProperties.getProperty("autoGain", "10"));
            createGuildCost = ServerProperties.getProperty("createGuildCost", 10000000);
            merchantTime = ServerProperties.getProperty("merchantTime", 24);
            serverName = ServerProperties.getProperty("serverName", "MapleStory");
            flags = ServerProperties.getProperty("flags", 0);
            adminOnly = ServerProperties.getProperty("admin", false);
            canPvp = ServerProperties.getProperty("canPvp", false);
            shieldWardAll = ServerProperties.getProperty("shieldWardAll", false);
            checkSp = ServerProperties.getProperty("checkSp", true);
            checkCash = ServerProperties.getProperty("checkCash", true);
            useMapScript = ServerProperties.getProperty("useMapScript", false);
            autoPoints = Boolean.parseBoolean(ServerProperties.getProperty("autoPoints", "false"));
            eventSMs = ServerProperties.getProperty("channel.events", "");
            eventSM = new EventScriptManager(this, eventSMs.split(","));
            port = Short.parseShort(ServerProperties.getProperty("channel.port" + channel, String.valueOf(DEFAULT_PORT + channel)));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        ip = (ServerProperties.getProperty("channel.interface", ServerConstants.IP) + ":" + port);

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        //Executor threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        acceptor.getFilterChain().addLast("exceutor", new ExecutorFilter(/*threadPool*/));
        //acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(threadPool));
        players = new PlayerStorage(channel);
        loadEvents(); // 事件脚本
        //loadShare();
        try {
            acceptor.setHandler(new MapleServerHandler(channel));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            FileoutputUtil.log("频道" + channel + " 正在监听" + port + "端口");
            eventSM.init();
        } catch (IOException e) {
            FileoutputUtil.log("无法绑定" + port + "端口 (频道: " + getChannel() + ")" + e);
        }
    }

    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        broadcastPacket(MaplePacketCreator.serverMessageNotice(" 游戏即将关闭维护..."));

        shutdown = true;
        FileoutputUtil.log("频道 " + channel + " 正在清理活动脚本...");

        eventSM.cancel();

        FileoutputUtil.log("频道 " + channel + " 正在保存所有角色数据...");

        getPlayerStorage().disconnectAll();

        FileoutputUtil.log("频道 " + channel + " 解除绑定端口...");

        acceptor.unbind();
        acceptor = null;

        instances.remove(channel);
        setFinishShutdown();
    }

    public void unbind() {
        acceptor.unbind();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public static ChannelServer newInstance(int channel) {
        return new ChannelServer(channel);
    }

    public static ChannelServer getInstance(int channel) {
        return (ChannelServer) instances.get(channel);
    }

    public void addPlayer(MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
    }

    public PlayerStorage getPlayerStorage() {
        if (players == null) {
            players = new PlayerStorage(channel);
        }
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        getPlayerStorage().deregisterPlayer(chr);
    }

    public void removePlayer(int idz, String namez) {
        getPlayerStorage().deregisterPlayer(idz, namez);
    }

    public void broadcastPacket(byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public void broadcastSmegaPacket(byte[] data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public void broadcastGMPacket(byte[] data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        instances.put(channel, this);
        LoginServer.addChannel(channel);
    }

    public static ArrayList<ChannelServer> getAllInstances() {
        return new ArrayList(instances.values());
    }

    public String getIP() {
        return ip;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void reloadEvents() {
        eventSM.cancel();
        eventSM = new EventScriptManager(this, ServerProperties.getProperty("channel.events", eventSMs).split(","));
        eventSM.init();
    }

    public int getExpRate(int world) {
        return WorldConstants.getById(world).getExp();
    }

    public void setExpRate(int expRate) {
        WorldConstants.EXP_RATE = expRate;
    }

    public int getCashRate() {
        return cashRate;
    }

    public void setCashRate(int cashRate) {
        this.cashRate = cashRate;
    }

    public int getMesoRate(int world) {
        return WorldConstants.getById(world).getMeso();
    }

    public void setMesoRate(int mesoRate) {
        WorldConstants.MESO_RATE = mesoRate;
    }

    public int getDropRate(int world) {
        return WorldConstants.getById(world).getDrop();
    }

    public void setDropRate(int dropRate) {
        WorldConstants.DROP_RATE = dropRate;
    }

    public int getGlobalRate() {
        if (globalRate <= 0) {
            return 1;
        }
        return globalRate;
    }

    public void setGlobalRate(int rate) {
        globalRate = rate;
    }

    public int getStatLimit() {
        return statLimit;
    }

    public void setStatLimit(int limit) {
        statLimit = limit;
    }

    public static void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();
        for (int i = 1; i <= Math.min(20, Integer.parseInt(ServerProperties.getProperty("channel.count", "0"))); i++) {
            newInstance(i).run_startup_configurations();
        }
    }

    public Map<MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap(mapleSquads);
    }

    public MapleSquad getMapleSquad(String type) {
        return getMapleSquad(MapleSquadType.valueOf(type.toLowerCase()));
    }

    public MapleSquad getMapleSquad(MapleSquadType type) {
        return (MapleSquad) mapleSquads.get(type);
    }

    public boolean addMapleSquad(MapleSquad squad, String type) {
        MapleSquadType types = MapleSquadType.valueOf(type.toLowerCase());
        if ((types != null) && (!mapleSquads.containsKey(types))) {
            mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }

    public boolean removeMapleSquad(MapleSquadType types) {
        if ((types != null) && (mapleSquads.containsKey(types))) {
            mapleSquads.remove(types);
            return true;
        }
        return false;
    }

    public int closeAllMerchant() {
        int ret = 0;
        mcWriteLock.lock();
        try {
            Iterator merchants_ = merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                HiredMerchant hm = (HiredMerchant) ((Map.Entry) merchants_.next()).getValue();
                HiredMerchantSave.QueueShopForSave(hm);
                hm.getMap().removeMapObject(hm);
                merchants_.remove();
                ret++;
            }
        } finally {
            mcWriteLock.unlock();
        }

        for (int i = 910000001; i <= 910000022; i++) {
            for (MapleMapObject mmo : mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                HiredMerchantSave.QueueShopForSave((HiredMerchant) mmo);
                ret++;
            }
        }
        return ret;
    }

    public void closeAllMerchants() {
        int ret = 0;
        long Start = System.currentTimeMillis();
        mcWriteLock.lock();
        try {
            Iterator hmit = merchants.entrySet().iterator();
            while (hmit.hasNext()) {
                ((HiredMerchant) ((Map.Entry) hmit.next()).getValue()).closeShop(true, false);
                hmit.remove();
                ret++;
            }
        } catch (Exception e) {
            log.error("关闭雇佣商店出现错误..." + e);
        } finally {
            mcWriteLock.unlock();
        }
        FileoutputUtil.log("频道 " + channel + " 共保存雇佣商店: " + ret + " | 耗时: " + (System.currentTimeMillis() - Start) + " 毫秒.");
    }

    public int addMerchant(HiredMerchant hMerchant) {
        mcWriteLock.lock();
        try {
            running_MerchantID += 1;
            merchants.put(running_MerchantID, hMerchant);
            int i = running_MerchantID;
            return i;
        } finally {
            mcWriteLock.unlock();
        }
    }

    public void removeMerchant(HiredMerchant hMerchant) {
        mcWriteLock.lock();
        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            mcWriteLock.unlock();
        }
    }

    public boolean containsMerchant(int accId) {
        boolean contains = false;
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.getOwnerAccId() == accId) {
                    contains = true;
                    break;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return contains;
    }

    public boolean containsMerchant(int accId, int chrId) {
        boolean contains = false;
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if ((hm.getOwnerAccId() == accId) && (hm.getOwnerId() == chrId)) {
                    contains = true;
                    break;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return contains;
    }

    public List<HiredMerchant> searchMerchant(int itemSearch) {
        List list = new LinkedList();
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return list;
    }

    public HiredMerchant getHiredMerchants(int accId, int chrId) {
        mcReadLock.lock();
        try {
            for (HiredMerchant hm : merchants.values()) {
                if ((hm.getOwnerAccId() == accId) && (hm.getOwnerId() == chrId)) {
                    HiredMerchant localHiredMerchant1 = hm;
                    return localHiredMerchant1;
                }
            }
        } finally {
            mcReadLock.unlock();
        }
        return null;
    }

    public void toggleMegaphoneMuteState() {
        MegaphoneMuteState = (!MegaphoneMuteState);
    }

    public boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }

    public int getEvent() {
        return eventmap;
    }

    public void setEvent(int ze) {
        eventmap = ze;
    }

    public MapleEvent getEvent(MapleEventType t) {
        return (MapleEvent) events.get(t);
    }

    public Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs;
    }

    public void addPlayerNPC(PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            return;
        }
        playerNPCs.add(npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public void removePlayerNPC(PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            playerNPCs.remove(npc);
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String sn) {
        serverName = sn;
    }

    public String getTrueServerName() {
        return serverName.substring(0, serverName.length() - 3);
    }

    public int getPort() {
        return port;
    }

    public static Set<Integer> getChannelServer() {
        return new HashSet(instances.keySet());
    }

    public void setShutdown() {
        shutdown = true;
        FileoutputUtil.log("频道 " + channel + " 正在关闭和保存雇佣商店数据信息...");
    }

    public void setFinishShutdown() {
        finishedShutdown = true;
        FileoutputUtil.log("频道 " + channel + " 已关闭完成.");
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public static int getChannelCount() {
        return instances.size();
    }

    public int getTempFlag() {
        return flags;
    }

    public static Map<Integer, Integer> getChannelLoad() {
        Map ret = new HashMap();
        for (ChannelServer cs : instances.values()) {
            ret.put(cs.getChannel(), cs.getConnectedClients());
        }
        return ret;
    }

    public int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSmegaPacket(message);
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(message);
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 10);
    }

    public void startMapEffect(String msg, int itemId, int time) {
        for (MapleMap load : getMapFactory().getAllMaps()) {
            if (load.getCharactersSize() > 0) {
                load.startMapEffect(msg, itemId, time);
            }
        }
    }

    public AramiaFireWorks getFireWorks() {
        return works;
    }

    public int getTraitRate() {
        return traitRate;
    }

    public void saveAll() {
        int nos = 0;
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr != null) {
                nos++;
                chr.saveToDB(false, false);
            }
        }
        FileoutputUtil.log("[自动保存] 已经将频道 " + channel + " 的 " + nos + " 个玩家的数据自动保存到数据中.");
    }

    public int getAutoGain() {
        return autoGain;
    }

    public void setAutoGain(int rate) {
        autoGain = rate;
    }

    public void AutoGain(int rate) {
        mapFactory.getMap(910000000).AutoGain(rate, 50);
    }

    public int getAutoNx() {
        return autoNx;
    }

    public void setAutoNx(int rate) {
        autoNx = rate;
    }

    public void AutoNx(int rate) {
        mapFactory.getMap(910000000).AutoNx(rate, isAutoPoints());
    }

    public boolean isCanPvp() {
        return canPvp;
    }

    public boolean isShieldWardAll() {
        return shieldWardAll;
    }

    public void setShieldWardAll(boolean all) {
        shieldWardAll = all;
    }

    public int getStateRate() {
        return stateRate;
    }

    public void setStateRate(int stateRate) {
        this.stateRate = stateRate;
    }

    public int getCreateGuildCost() {
        return createGuildCost;
    }

    public static MapleCharacter getCharacterById(int id) {
        for (ChannelServer cserv_ : getAllInstances()) {
            MapleCharacter ret = cserv_.getPlayerStorage().getCharacterById(id);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public static MapleCharacter getCharacterByName(String name) {
        for (ChannelServer cserv_ : getAllInstances()) {
            MapleCharacter ret = cserv_.getPlayerStorage().getCharacterByName(name);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public int getSharePrice() {
        return sharePrice;
    }

    public void loadShare() {
        if ((channel != 1) || (finishedShutdown)) {
            return;
        }
        shareCon = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = shareCon.prepareStatement("SELECT * FROM shares WHERE channelid = ?")) {
                ps.setInt(1, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sharePrice = rs.getInt("currentprice");
                    } else {
                        throw new RuntimeException("[EXCEPTION] 无法加载股票数据.");
                    }
                    FileoutputUtil.log("目前的股票价格: " + sharePrice);
                }
                ps.close();
            }
        } catch (SQLException e) {
            log.error("ERROR Load Shares", e);
        }
    }

    public void increaseShare(int share) {
        if ((channel != 1) || (finishedShutdown)) {
            return;
        }
        sharePrice += share;
        try {
            try (PreparedStatement ps = shareCon.prepareStatement("UPDATE shares SET currentprice = ? WHERE channelid = 1")) {
                ps.setInt(1, sharePrice);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("ERROR Increase Shares", e);
        }
    }

    public void decreaseShare(int share) {
        if ((channel != 1) || (finishedShutdown)) {
            return;
        }
        sharePrice -= share;
        if (sharePrice < 0) {
            sharePrice = 0;
        }
        try {
            try (PreparedStatement ps = shareCon.prepareStatement("UPDATE shares SET currentprice = ? WHERE channelid = 1")) {
                ps.setInt(1, sharePrice);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("ERROR Decrease Shares", e);
        }
    }

    public void saveShares() {
        if ((channel != 1) || (finishedShutdown)) {
            return;
        }
        try {
            try (PreparedStatement ps = shareCon.prepareStatement("UPDATE shares SET currentprice = ? WHERE channelid = 1")) {
                ps.setInt(1, sharePrice);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("ERROR Save Shares", e);
        }
    }

    public int getMerchantTime() {
        return merchantTime;
    }

    public boolean isCheckSp() {
        return checkSp;
    }

    public boolean isCheckCash() {
        return checkCash;
    }

    public boolean isUseMapScript() {
        return useMapScript;
    }

    public boolean isAutoPoints() {
        return autoPoints;
    }
}
