package client;

import client.inventory.Equip;
import client.inventory.ImpFlag;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.ItemLoader;
import client.inventory.MapleImp;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.inventory.MapleWeaponType;
import client.inventory.ModifyInventory;
import client.messages.PlayerGMRank;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.*;
import database.DatabaseConnection;
import database.DatabaseException;
import handling.channel.ChannelServer;
import handling.channel.handler.AttackInfo;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.PlayerBuffValueHolder;
import handling.world.World;
import handling.world.WorldBroadcastService;
import handling.world.WorldGuildService;
import handling.world.WorldMessengerService;
import handling.world.WorldSidekickService;
import handling.world.WrodlPartyService;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.messenger.MessengerRankingWorker;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.sidekick.MapleSidekick;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import scripting.event.EventInstanceManager;
import scripting.npc.NPCScriptManager;
import server.AutobanManager;
import server.ManagerSin;
import server.MapleCarnivalChallenge;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.RandomRewards;
import server.Randomizer;
import server.ServerProperties;
import server.StructSetItem;
import server.StructSetItemStat;
import server.Timer.BuffTimer;
import server.Timer.MapTimer;
import server.Timer.WorldTimer;
import server.cashshop.CashShop;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.AnimatedMapleMapObject;
import server.maps.FieldLimitType;
import server.maps.MapleDefender;
import server.maps.MapleDoor;
import server.maps.MapleExtractor;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SavedLocationType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shop.MapleShop;
import server.shops.IMaplePlayerShop;
import server.skill.冒险家.勇士;
import tools.AttackPair;
import tools.ConcurrentEnumMap;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;
import tools.packet.BuddyListPacket;
import tools.packet.BuffPacket;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.PartyPacket;
import tools.packet.PetPacket;
import tools.packet.PlayerShopPacket;
import tools.packet.SkillPacket;
import tools.packet.SummonPacket;
import tools.packet.UIPacket;

public class MapleCharacter extends AnimatedMapleMapObject implements Serializable {

    private static final long serialVersionUID = 845748950829L;
    public static final double MIN_VIEW_RANGE_SQ = 480000.0D, MAX_VIEW_RANGE_SQ = 786432.0D;
    private String name, chalktext, BlessOfFairy_Origin, BlessOfEmpress_Origin, teleportname;
    private long lastComboTime, lastfametime, keydown_skill, nextConsume, pqStartTime, lastDragonBloodTime, lastBerserkTime, lastRecoveryTime,
            lastSummonTime, mapChangeTime, lastFishingTime, lastFairyTime, lastHPTime, lastMPTime, lastFamiliarEffectTime, lastDOTTime,
            lastExpirationTime, lastBlessOfDarknessTime, lastMorphLostTime, lastRecoveryTimeEM;
    private byte gmLevel;
    private byte gender;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte guildrank = 5;
    private byte allianceRank = 5;
    private byte world;
    private byte fairyExp;
    private byte subcategory;
    private short level;
    private short mulung_energy;
    private short availableCP;
    private short fatigue;
    private short totalCP;
    private short hpApUsed;
    private short job;
    private short remainingAp;
    private short scrolledPosition;
    private int accountid;
    private int id;
    private int meso;
    private int hair;
    private int face;
    private int mapid;
    private int fame;
    private int totalWins;
    private int totalLosses;
    private int guildid = 0;
    private int fallcounter;
    private int maplepoints;
    private int acash;
    private int chair;
    private int itemEffect;
    private int points;
    private int vpoints;
    private int rank = 1;
    private int rankMove = 0;
    private int jobRank = 1;
    private int jobRankMove = 0;
    private int marriageId;
    private int marriageItemId;
    private int dotHP;
    private int currentrep;
    private int totalrep;
    private int coconutteam;
    private int followid;
    private int gachexp;
    private int challenge;
    private int guildContribution = 0;
    private long exp;
    private Point old;
    private MonsterFamiliar summonedFamiliar;
    private int[] wishlist;
    private int[] savedLocations;
    private int[] regrocks;
    private int remainingSp;
    private transient AtomicInteger inst;
    private transient AtomicInteger insd;
    private transient List<LifeMovementFragment> lastres;
    private List<Integer> lastmonthfameids;
    private List<MapleDoor> doors;
    private List<MechDoor> mechDoors;
    private MaplePet spawnPets;
    private MapleImp[] imps;
    private transient Set<MapleMonster> controlled;
    private transient Set<MapleMapObject> visibleMapObjects;
    private transient ReentrantReadWriteLock visibleMapObjectsLock;
    private transient ReentrantReadWriteLock summonsLock;
    private transient ReentrantReadWriteLock controlledLock;
    private final Map<MapleQuest, MapleQuestStatus> quests; // 任务数据
    private Map<Integer, String> questinfo;
    private Map<String, String> keyValue;
    private final Map<Skill, SkillEntry> skills;
    private transient ArrayList<Pair<MapleBuffStat, MapleBuffStatValueHolder>> effects;
    private transient Map<Integer, MapleCoolDownValueHolder> coolDowns;
    private transient Map<MapleDisease, MapleDiseaseValueHolder> diseases;
    private transient Map<Integer,MapleSummon> summons= new LinkedHashMap<Integer, MapleSummon>();
    private CashShop cs;
    private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    private transient MapleCarnivalParty carnivalParty;
    private BuddyList buddylist;
    private MapleClient client;
    private transient MapleParty party;
    private final PlayerStats stats;
    private transient MapleMap map;
    private transient MapleShop shop;
    private transient MapleExtractor extractor;
    private transient RockPaperScissors rps;
    private MapleSidekick sidekick;
    private Map<Integer, MonsterFamiliar> familiars;
    private MapleStorage storage;
    private transient MapleTrade trade;
    private MapleMount mount;
    private List<Integer> finishedAchievements;
    private MapleMessenger messenger;
    private byte petStore;
    private transient IMaplePlayerShop playerShop;
    private boolean invincible;
    private boolean canTalk;
    private boolean followinitiator;
    private boolean followon;
    private boolean smega;
    private boolean hasSummon;
    private MapleGuildCharacter mgc;
    private transient EventInstanceManager eventInstance;
    private MapleInventory[] inventory;
    private Battler[] battlers = new Battler[6];
    private List<Battler> boxed;
    private MapleKeyLayout keylayout;
    private MapleQuickSlot quickslot;
    private transient ScheduledFuture<?> mapTimeLimitTask;
    private transient List<Integer> pendingExpiration = null;
    private transient Map<Skill, SkillEntry> pendingSkills = null;
    private transient Map<Integer, Integer> linkMobs;
    private boolean changed_wishlist;
    private boolean changed_trocklocations;
    private boolean changed_achievements;
    private boolean changed_savedlocations;
    private boolean changed_pokemon;
    private boolean changed_questinfo;
    private boolean changed_keyValue;
    private static final Logger log = Logger.getLogger(MapleCharacter.class);
    private int aranCombo = 0;
    private int forceCounter = 0;
    private int decorate;
    private int titleEffect;
    private boolean isbanned = false;
    private int beans;
    private int warning;
    private int dollars;
    private int shareLots;
    private int apstorage;
    private int cardStack = 0;
    private int honor;
    private Timestamp createDate;
    private int morphCount = 0;
    private int powerCount = 0;
    private boolean usePower = false;
    private int love;
    private long lastlovetime;
    private Map<Integer, Long> lastdayloveids;
    private int playerPoints;
    private int playerEnergy;
    private int batterytime;
    private long runeresettime;
    private long userunenowtime;
    private boolean energyfull = false;
    private int runningDark = 1;
    private int runningDarkSlot;
    private int runningLight = 1;
    private int runningLightSlot;
    private boolean isSaveing;
    private long lastMonsterCombo;
    private int monsterCombo;
    public List<Long> killMonsterExps = new ArrayList();
    public int 卡图 = 0;
    public boolean enattacke = false;
    private int KillCount = 0;
    private int mod = 0;
    private long stifftime = 0;
    public Point norba;
    private long longintime = 0;
    public int Skillmode = 20040216;

    public void setenattacke(boolean bl) {
        this.enattacke = bl;
    }

    public long getlogintime() {
        return this.longintime;
    }

    public void setlogintime(long time) {
        this.longintime = time;
    }

    public boolean getenattacke() {
        return this.enattacke;
    }

    public void setnorba() {
        this.norba = this.old;
    }

    public void send(byte[] ob) { //给自己发包
        getClient().getSession().write(ob);
    }

    public void send_other(byte[] ob, boolean tome) { //给地图的其他玩家发包
        getMap().broadcastMessage(this, ob, tome);
    }

    public void setKillCount(int x) {
        this.KillCount = x;
    }

    public int getKillCount() {
        return this.KillCount;
    }

    public void setmod(int x) {
        this.mod = x;
    }

    public int getmod() {
        return this.mod;
    }

    public void setstifftime(long x) {
        this.stifftime = x;
    }

    public long getstifftime() {
        return this.stifftime;
    }

    private MapleCharacter(boolean ChannelServer) {
        setStance(0);
        setPosition(new Point(0, 0));
        this.inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            this.inventory[type.ordinal()] = new MapleInventory(type);
        }
        this.keyValue = new LinkedHashMap();
        this.questinfo = new LinkedHashMap();
        this.quests = new LinkedHashMap();
        this.skills = new LinkedHashMap();
        this.stats = new PlayerStats();
        this.remainingSp = 0;
        this.spawnPets = null;
        if (ChannelServer) {
            isSaveing = false;
            changed_achievements = false;
            changed_wishlist = false;
            changed_trocklocations = false;
            changed_savedlocations = false;
            changed_pokemon = false;
            changed_questinfo = false;
            changed_keyValue = false;
            scrolledPosition = 0;
            lastComboTime = 0L;
            lastMonsterCombo = 0;
            monsterCombo = 0;
            mulung_energy = 0;
            aranCombo = 0;
            forceCounter = 0;
            cardStack = 0;
            morphCount = 0;
            usePower = false;
            keydown_skill = 0L;
            nextConsume = 0L;
            pqStartTime = 0L;
            fairyExp = 0;
            mapChangeTime = 0L;
            lastRecoveryTime = 0L;
            lastDragonBloodTime = 0L;
            lastBerserkTime = 0L;
            lastFishingTime = 0L;
            lastFairyTime = 0L;
            lastHPTime = 0L;
            lastMPTime = 0L;
            lastFamiliarEffectTime = 0L;
            lastExpirationTime = 0L;
            lastBlessOfDarknessTime = 0L;
            lastMorphLostTime = 0L;
            lastRecoveryTimeEM = 0L;
            old = new Point(0, 0);
            coconutteam = 0;
            followid = 0;
            marriageItemId = 0;
            fallcounter = 0;
            challenge = 0;
            dotHP = 0;
            lastSummonTime = 0L;
            hasSummon = false;
            invincible = false;
            canTalk = true;
            followinitiator = false;
            followon = false;
            energyfull = false;
            linkMobs = new HashMap();
            finishedAchievements = new ArrayList();
            teleportname = "";
            smega = true;
            petStore = -1;
            wishlist = new int[12];
            regrocks = new int[5];
            imps = new MapleImp[3];
            boxed = new ArrayList();
            familiars = new LinkedHashMap();
            effects = new ArrayList();
            coolDowns = new LinkedHashMap();
            diseases = new ConcurrentEnumMap(MapleDisease.class);
            inst = new AtomicInteger(0);
            insd = new AtomicInteger(-1);
            keylayout = new MapleKeyLayout();
            quickslot = new MapleQuickSlot();
            doors = new ArrayList();
            mechDoors = new ArrayList();
            controlled = new LinkedHashSet();
            controlledLock = new ReentrantReadWriteLock();
            summons = new LinkedHashMap<Integer,MapleSummon>();
            summonsLock = new ReentrantReadWriteLock();
            visibleMapObjects = new LinkedHashSet();
            visibleMapObjectsLock = new ReentrantReadWriteLock();
            pendingCarnivalRequests = new LinkedList();

            savedLocations = new int[SavedLocationType.values().length];
            for (int i = 0; i < SavedLocationType.values().length; i++) {
                savedLocations[i] = -1;
            }
        }
    }

    public static MapleCharacter getDefault(MapleClient client, short[] stat) {
        if (stat.length < 4) {
            FileoutputUtil.log("[錯誤] 創建角色默認能力值出錯");
        }
        MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.exp = 0;
        ret.gmLevel = (byte) client.getGmLevel();
        ret.job = 0;
        ret.meso = 0;
        ret.level = 1;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.love = 0;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList((byte) 20);
        ret.stats.str = stat[0];
        ret.stats.dex = stat[1];
        ret.stats.int_ = stat[2];
        ret.stats.luk = stat[3];
        ret.stats.baseMaxHp = 50;
        ret.stats.baseHp = 50;
        ret.stats.baseMaxMp = 5;
        ret.stats.baseMp = 5;
        ret.gachexp = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    ret.client.setAccountName(rs.getString("name"));
                    ret.acash = rs.getInt("ACash");
                    ret.maplepoints = rs.getInt("mPoints");
                    ret.points = rs.getInt("points");
                    ret.vpoints = rs.getInt("vpoints");
                }
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Error getting character default").append(e).toString());
        }
        return ret;
    }

    /**
     * 还原玩家数据
     * @param ct
     * @param client
     * @param isChannel
     * @return
     */
    public static MapleCharacter ReconstructChr(CharacterTransfer ct, MapleClient client, boolean isChannel) {
        MapleCharacter ret = new MapleCharacter(true);
        ret.client = client;
        if (!isChannel) {
            ret.client.setChannel(ct.channel);
        }
        ret.id = ct.characterid;
        ret.name = ct.name;
        ret.level = ct.level;
        ret.fame = ct.fame;
        ret.love = ct.love;

        ret.stats.str = ct.str;
        ret.stats.dex = ct.dex;
        ret.stats.int_ = ct.int_;
        ret.stats.luk = ct.luk;
        ret.stats.baseMaxHp = ct.maxhp;
        ret.stats.baseMaxMp = ct.maxmp;
        ret.stats.baseHp = ct.hp;
        ret.stats.baseMp = ct.mp;

        ret.client.setGender(ct.gender);

        ret.chalktext = ct.chalkboard;
        ret.gmLevel = ct.gmLevel;
        ret.exp = ret.level >= ret.getMaxLevelForSever() ? 0 : ct.exp;
        ret.hpApUsed = ct.hpApUsed;
        ret.remainingSp = ct.remainingSp;
        ret.remainingAp = ct.remainingAp;
        ret.meso = ct.meso;
        ret.skinColor = ct.skinColor;
        ret.gender = ct.gender;
        ret.job = ct.job;
        ret.hair = ct.hair;
        ret.face = ct.face;
        ret.accountid = ct.accountid;
        ret.totalWins = ct.totalWins;
        ret.totalLosses = ct.totalLosses;
        client.setAccID(ct.accountid);
        ret.mapid = ct.mapid;
        ret.initialSpawnPoint = ct.initialSpawnPoint;
        ret.world = ct.world;
        ret.guildid = ct.guildid;
        ret.guildrank = ct.guildrank;
        ret.guildContribution = ct.guildContribution;
        ret.allianceRank = ct.alliancerank;
        ret.points = ct.points;
        ret.vpoints = ct.vpoints;
        ret.fairyExp = ct.fairyExp;
        ret.marriageId = ct.marriageId;
        ret.currentrep = ct.currentrep;
        ret.totalrep = ct.totalrep;
        ret.gachexp = ct.gachexp;
        ret.decorate = ct.decorate;
        ret.beans = ct.beans;
        ret.warning = ct.warning;

        ret.dollars = ct.dollars;
        ret.shareLots = ct.shareLots;

        ret.apstorage = ct.apstorage;

        ret.honor = ct.honor;
        ret.cardStack = ct.cardStack;
        ret.morphCount = ct.morphCount;
        ret.powerCount = ct.powerCount;
        ret.playerPoints = ct.playerPoints;
        ret.playerEnergy = ct.playerEnergy;
        ret.batterytime = ct.batterytime;
        ret.runeresettime = ct.runeresettime;
        ret.userunenowtime = ct.userunenowtime;
        ret.runningDark = ct.runningDark;
        ret.runningDarkSlot = ct.runningDarkSlot;
        ret.runningLight = ct.runningLight;
        ret.runningLightSlot = ct.runningLightSlot;
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        ret.fatigue = ct.fatigue;
        ret.buddylist = new BuddyList(ct.buddysize);
        ret.subcategory = ct.subcategory;

        if (ct.sidekick > 0) {
            ret.sidekick = WorldSidekickService.getInstance().getSidekick(ct.sidekick);
        }

        if (isChannel) {
            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) {
                ret.map = mapFactory.getMap(950000100);
            } else if ((ret.map.getForcedReturnId() != 999999999) && (ret.map.getForcedReturnMap() != null)) {
                ret.map = ret.map.getForcedReturnMap();
            }

            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0);
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());

            int messengerid = ct.messengerid;
            if (messengerid > 0) {
                ret.messenger = WorldMessengerService.getInstance().getMessenger(messengerid);
            }
        } else {
            ret.messenger = null;
        }
        int partyid = ct.partyid;
        if (partyid >= 0) {
            MapleParty party = WrodlPartyService.getInstance().getParty(partyid);
            if ((party != null) && (party.getMemberById(ret.id) != null)) {
                ret.party = party;
            }

        }

        for (Map.Entry qs : ct.Quest.entrySet()) {
            MapleQuestStatus queststatus_from = (MapleQuestStatus) qs.getValue();
            queststatus_from.setQuest((Integer) qs.getKey());
            ret.quests.put(queststatus_from.getQuest(), queststatus_from);
        }
        for (final Entry<Integer, SkillEntry> qs : ct.Skills.entrySet()) {
            ret.skills.put(SkillFactory.getSkill(qs.getKey()), qs.getValue());
        }
        for (Integer zz : ct.finishedAchievements) {
            ret.finishedAchievements.add(zz);
        }
        for (Iterator i$ = ct.boxed.iterator(); i$.hasNext();) {
            Object zz = i$.next();
            Battler zzz = (Battler) zz;
            zzz.setStats();
            ret.boxed.add(zzz);
        }
        ret.inventory = ((MapleInventory[]) (MapleInventory[]) ct.inventorys);
        ret.BlessOfFairy_Origin = ct.BlessOfFairy;
        ret.BlessOfEmpress_Origin = ct.BlessOfEmpress;
        ret.battlers = ((Battler[]) (Battler[]) ct.battlers);
        for (Battler b : ret.battlers) {
            if (b != null) {
                b.setStats();
            }
        }
        ret.petStore = ct.petStore;
        ret.keylayout = new MapleKeyLayout(ct.keymap);
        ret.quickslot = new MapleQuickSlot(ct.quickslot);
        ret.keyValue = ct.KeyValue;
        ret.questinfo = ct.InfoQuest;
        ret.familiars = ct.familiars;
        ret.savedLocations = ct.savedlocation;
        ret.wishlist = ct.wishlist;
        ret.regrocks = ct.regrocks;
        ret.buddylist.loadFromTransfer(ct.buddies);

        ret.keydown_skill = 0L;
        ret.lastfametime = ct.lastfametime;
        ret.lastmonthfameids = ct.famedcharacters;
        ret.lastlovetime = ct.lastLoveTime;
        ret.lastdayloveids = ct.loveCharacters;
        ret.storage = ((MapleStorage) ct.storage);
        ret.cs = ((CashShop) ct.cs);
        client.setAccountName(ct.accountname);
        ret.acash = ct.ACash;
        ret.maplepoints = ct.MaplePoints;

        ret.imps = ct.imps;
        ret.mount = new MapleMount(ret, ct.mount_itemid, PlayerStats.getSkillByJob(1004, ret.job), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
        ret.stats.recalcLocalStats(true, ret);
        client.setTempIP(ct.tempIP);

        return ret;
    }

    /**
     * 从数据库中读取角色信息
     * @param charid
     * @param client
     * @param channelserver
     * @return
     */
    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
        MapleCharacter ret = new MapleCharacter(channelserver);
        ret.client = client;
        ret.id = charid;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps, psd, pse;
        ResultSet rs, rsd;
        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("加载角色失败原因(角色没有找到).");
            }
            ret.name = rs.getString("name");
            ret.level = rs.getShort("level");
            ret.fame = rs.getInt("fame");
            ret.stats.str = rs.getShort("str");
            ret.stats.dex = rs.getShort("dex");
            ret.stats.int_ = rs.getShort("int");
            ret.stats.luk = rs.getShort("luk");
            ret.stats.baseMaxHp = rs.getInt("maxhp");
            ret.stats.baseMaxMp = rs.getInt("maxmp");
            ret.stats.baseHp = rs.getInt("hp");
            ret.stats.baseMp = rs.getInt("mp");
            ret.job = rs.getShort("job");
            ret.gmLevel = rs.getByte("gm");
            ret.exp = ret.level >= ret.getMaxLevelForSever() ? 0L : rs.getLong("exp");
            ret.hpApUsed = rs.getShort("hpApUsed");
            ret.remainingSp =   rs.getShort("sp");
            ret.remainingAp = rs.getShort("ap");
            ret.meso = rs.getInt("meso");
            if (ret.meso < 0) {
                ret.meso = 0;
            }
            ret.skinColor = rs.getByte("skincolor");
            ret.gender = rs.getByte("gender");
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.accountid = rs.getInt("accountid");
            if (client != null) {
                client.setAccID(ret.accountid);
            }
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = rs.getByte("spawnpoint");
            ret.world = rs.getByte("world");
            ret.guildid = rs.getInt("guildid");
            ret.guildrank = rs.getByte("guildrank");
            ret.allianceRank = rs.getByte("allianceRank");
            ret.guildContribution = rs.getInt("guildContribution");
            ret.totalWins = rs.getInt("totalWins");
            ret.totalLosses = rs.getInt("totalLosses");
            ret.currentrep = rs.getInt("currentrep");
            ret.totalrep = rs.getInt("totalrep");
            if (ret.guildid > 0 && client != null) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            ret.gachexp = rs.getInt("gachexp");
            ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
            ret.subcategory = rs.getByte("subcategory");
            ret.mount = new MapleMount(ret, 0, PlayerStats.getSkillByJob(1004, ret.job), (byte) 0, (byte) 1, 0);
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            ret.marriageId = rs.getInt("marriageId");
            ret.fatigue = rs.getShort("fatigue");
            ret.apstorage = rs.getInt("apstorage");
            if ((channelserver) && (client != null)) {
                MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) {
                    FileoutputUtil.log("不存在的地图："+ret.mapid);
                    ret.map = mapFactory.getMap(101000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                if (partyid >= 0) {
                    MapleParty party = WrodlPartyService.getInstance().getParty(partyid);
                    if ((party != null) && (party.getMemberById(ret.id) != null)) {
                        ret.party = party;
                    }
                }
                String pets = rs.getString("pets");
                ret.petStore = Byte.parseByte(pets);
                psd = con.prepareStatement("SELECT * FROM achievements WHERE accountid = ?");
                psd.setInt(1, ret.accountid);
                rsd = psd.executeQuery();
                while (rsd.next()) {
                    ret.finishedAchievements.add(rsd.getInt("achievementid"));
                }
                psd.close();

            }
            ps.close();
            ps = con.prepareStatement("SELECT * FROM character_keyvalue WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("key") == null) {
                    continue;
                }
                ret.keyValue.put(rs.getString("key"), rs.getString("value"));
            }
            ps.close();

            // 角色任务信息
            ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
            }
            ps.close();
            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("quest");
                MapleQuest q = MapleQuest.getInstance(id);
                byte stat = rs.getByte("status");
//                if ((stat == 1 || stat == 2) && ((channelserver && (q == null || q.isBlocked())) || (stat == 1 && channelserver && (!q.canStart(ret, null))))) {
//                    FileoutputUtil.log("已经完成的任务ID"+id);
//                    continue;
//                }
                MapleQuestStatus status = new MapleQuestStatus(q, stat);
                long cTime = rs.getLong("time");
                if (cTime > -1L) {
                    status.setCompletionTime(cTime * 1000L);
                }
                status.setForfeited(rs.getInt("forfeited"));
                status.setCustomData(rs.getString("customData"));
                ret.quests.put(q, status);
            }
            ps.close();

            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                }
                ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
                ps.close();
                for (Pair mit : ItemLoader.装备道具.loadItems(false, charid).values()) {
                    ret.getInventory((MapleInventoryType) mit.getRight()).addFromDB((Item) mit.getLeft());
                }
                ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ret.getClient().setAccountName(rs.getString("name"));
                    ret.getClient().setGender(rs.getByte("gender"));
                    ret.acash = rs.getInt("ACash");
                    ret.maplepoints = rs.getInt("mPoints");
                    ret.points = rs.getInt("points");
                    ret.vpoints = rs.getInt("vpoints");
                    if (rs.getTimestamp("lastlogon") != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
                    }
                    if (rs.getInt("banned") > 0) {
                        rs.close();
                        ps.close();
                        ret.getClient().getSession().close(true);
                        throw new RuntimeException("加载的角色为封号状态，服务端断开这个连接...");
                    }
                    psd = con.prepareStatement("UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id = ?");
                    psd.setInt(1, ret.accountid);
                    psd.executeUpdate();
                }
                ps.close();
                ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration, teachId, position FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int phantom = 0;
                while (rs.next()) {
                    int skid = rs.getInt("skillid");
                    Skill skil = SkillFactory.getSkill(skid);
                    int skl = rs.getInt("skilllevel");
                    byte msl = rs.getByte("masterlevel");
                    int teachId = rs.getInt("teachId");
                    byte position = rs.getByte("position");
//                    if (skil.is老技能()) {
//                        ret.changed_skills = true;
//                        continue;
//                    }
                    if ((skl > skil.getMaxLevel())) {
                        if ((skil.canBeLearnedBy(ret.job)) && (!skil.isSpecialSkill()) && (!skil.isAdminSkill())) {
                            ret.remainingSp += skl - skil.getMaxLevel();
                        }
                        skl = (byte) skil.getMaxLevel();
                    }
                    if (msl > skil.getMaxLevel()) {
                        msl = (byte) skil.getMaxLevel();
                    }
                    ret.skills.put(skil, new SkillEntry(skl, msl, rs.getLong("expiration"), teachId));
                }
                ps.close();
                ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                int maxlevel_ = 0;
                int maxlevel_2 = 0;
                while (rs.next()) {
                    if (rs.getInt("id") != charid) {
                        int maxlevel = rs.getShort("level") / 10;
                        if (maxlevel > 20) {
                            maxlevel = 20;
                        }
                        if ((maxlevel > maxlevel_) || (maxlevel_ == 0)) {
                            maxlevel_ = maxlevel;
                            ret.BlessOfFairy_Origin = rs.getString("name");
                        }
                    }
                }
                if (ret.BlessOfFairy_Origin == null) {
                    ret.BlessOfFairy_Origin = ret.name;
                }
//                ret.skills.put(SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job)), new SkillEntry(maxlevel_, (byte) 0, -1L, 0));
//                if (SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)) != null) {
//                    if (ret.BlessOfEmpress_Origin == null) {
//                        ret.BlessOfEmpress_Origin = ret.BlessOfFairy_Origin;
//                    }
//                    ret.skills.put(SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)), new SkillEntry(maxlevel_2, (byte) 0, -1L, 0));
//                }
                ps.close();
                psd = con.prepareStatement("SELECT * FROM familiars WHERE characterid = ?");
                psd.setInt(1, charid);
                rs = psd.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("expiry") <= System.currentTimeMillis()) {
                        continue;
                    }
                    ret.familiars.put(rs.getInt("familiar"), new MonsterFamiliar(charid, rs.getInt("id"), rs.getInt("familiar"), rs.getLong("expiry"), rs.getString("name"), rs.getInt("fatigue"), rs.getByte("vitality")));
                }
                psd.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                Map keyb = ret.keylayout.Layout();
                while (rs.next()) {
                    keyb.put(rs.getInt("key"), new Pair(rs.getByte("type"), rs.getInt("action")));
                }
                ps.close();
                ret.keylayout.unchanged();
                ps = con.prepareStatement("SELECT `index`, `key` FROM quickslot WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                List quickslots = ret.quickslot.Layout();
                while (rs.next()) {
                    quickslots.add(new Pair(rs.getInt("index"), rs.getInt("key")));
                }
                ps.close();
                ret.quickslot.unchanged();
                ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map");
                }
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0L;
                ret.lastmonthfameids = new ArrayList(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                }
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM lovelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 1");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastlovetime = 0L;
                ret.lastdayloveids = new LinkedHashMap();
                while (rs.next()) {
                    ret.lastlovetime = Math.max(ret.lastlovetime, rs.getTimestamp("when").getTime());
                    ret.lastdayloveids.put(rs.getInt("characterid_to"), rs.getTimestamp("when").getTime());
                }
                ps.close();
                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
                ret.cs = new CashShop(ret.accountid, charid, ret.getJob());
                ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int i = 0;
                while (rs.next()) {
                    ret.wishlist[i] = rs.getInt("sn");
                    i++;
                }
                while (i < 12) {
                    ret.wishlist[i] = 0;
                    i++;
                }
                ps.close();
                ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 28");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int r = 0;
                int reg = 0;
                int hyper = 0;
                while (rs.next()) {
                    int v = rs.getInt("vip");
                    int m = rs.getInt("mapid");
                    if (v == 0) {
                        ret.regrocks[reg] = m;
                        reg++;
                    }
                }
                while (reg < 5) {
                    ret.regrocks[reg] = 999999999;
                    reg++;
                }

                ps.close();
                ps = con.prepareStatement("SELECT * FROM imps WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.imps[r] = new MapleImp(rs.getInt("itemid"));
                    ret.imps[r].setLevel(rs.getByte("level"));
                    ret.imps[r].setState(rs.getByte("state"));
                    ret.imps[r].setCloseness(rs.getShort("closeness"));
                    ret.imps[r].setFullness(rs.getShort("fullness"));
                    r++;
                }
                ps.close();
                ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("在数据库中没有找到角色的坐骑信息...");
                }
                Item mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
                ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, 80001000, rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                ps.close();
                ps = con.prepareStatement("SELECT * FROM character_potionpots WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ps.close();
                if (client != null && client.getSendCrypto() != null) {
                    ret.stats.recalcLocalStats(true, ret);
                }
            } else {             // 角色列表
                for (Pair mit : ItemLoader.装备道具.loadItems(true, charid).values()) {
                    ret.getInventory((MapleInventoryType) mit.getRight()).addFromDB((Item) mit.getLeft());
                }
            }
        } catch (SQLException ess) {
            FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, ess);
            ret.getClient().getSession().close(true);
            throw new RuntimeException("加载角色数据信息出错.服务端断开这个连接...");
        }
        return ret;
    }

    public static void saveNewCharToDB(MapleCharacter chr) {
        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);

            ps = con.prepareStatement("INSERT INTO characters (level, str, dex, luk, `int`, hp, mp, maxhp, maxmp, sp, ap, skincolor, gender, job, hair, face, map, meso, party, buddyCapacity, pets, decorate, subcategory, gm, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, chr.level);
            PlayerStats stat = chr.stats;
            ps.setShort(2, stat.getStr());
            ps.setShort(3, stat.getDex());
            ps.setShort(4, stat.getInt());
            ps.setShort(5, stat.getLuk());
            ps.setInt(6, stat.getHp());
            ps.setInt(7, stat.getMp());
            ps.setInt(8, stat.getMaxHp());
            ps.setInt(9, stat.getMaxMp());
            ps.setShort(10, (short)chr.remainingSp);
            ps.setShort(11, chr.remainingAp);
            ps.setByte(12, chr.skinColor);
            ps.setByte(13, chr.gender);
            ps.setShort(14, chr.job);
            ps.setInt(15, chr.hair);
            ps.setInt(16, chr.face);
            ps.setInt(17, chr.mapid);
            ps.setLong(18, chr.meso);
            ps.setInt(19, -1);
            ps.setByte(20, chr.buddylist.getCapacity());
            ps.setString(21, "-1");
            ps.setInt(22, chr.decorate);
            ps.setInt(23, 0);
            ps.setByte(24, (byte) chr.getGMLevel());
            ps.setInt(25, chr.getAccountID());
            ps.setString(26, chr.name);
            ps.setByte(27, chr.world);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                chr.id = rs.getInt(1);
            } else {
                ps.close();
                rs.close();
                throw new DatabaseException("生成新角色到数据库出错...");
            }
            ps.close();
            rs.close();

            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, chr.id);
            for (MapleQuestStatus q : chr.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000L));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                rs.close();
            }
            ps.close();

            ps = con.prepareStatement("INSERT INTO character_keyvalue (`characterid`, `key`, `value`) VALUES (?, ?, ?)");
            ps.setInt(1, chr.id);
            for (Map.Entry key : chr.keyValue.entrySet()) {
                ps.setString(2, (String) key.getKey());
                ps.setString(3, (String) key.getValue());
                ps.execute();
            }
            ps.close();

            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 32);
            ps.setByte(3, (byte) 32);
            ps.setByte(4, (byte) 32);
            ps.setByte(5, (byte) 32);
            ps.setByte(6, (byte) 60);
            ps.execute();
            ps.close();

            ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 1);
            ps.setInt(3, 0);
            ps.setByte(4, (byte) 0);
            ps.execute();
            ps.close();

            int[] array1 = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 31, 33, 34, 35, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 50, 51, 52, 56, 57, 59, 60, 61, 62, 63, 64, 65};
            int[] array2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6};
            int[] array3 = {10, 12, 13, 18, 23, 28, 8, 5, 0, 4, 27, 30, 32, 1, 24, 19, 14, 15, 52, 2, 25, 17, 11, 3, 20, 26, 16, 22, 9, 50, 51, 6, 31, 29, 7, 33, 35, 53, 54, 100, 101, 102, 103, 104, 105, 106};

            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (int i = 0; i < array1.length; i++) {
                ps.setInt(2, array1[i]);
                ps.setInt(3, array2[i]);
                ps.setInt(4, array3[i]);
                ps.execute();
            }
            ps.close();

            List itemsWithType = new ArrayList();
            for (MapleInventory iv : chr.inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair(item, iv.getType()));
                }
            }
            ItemLoader.装备道具.saveItems(itemsWithType, chr.id);
            con.commit();
        } catch (SQLException | DatabaseException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, e);
            System.err.println("[charsave] Error saving character data");
            try {
                con.rollback();
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, ex);
                System.err.println("[charsave] Error Rolling Back");
            }
        } finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
            } catch (SQLException e) {
                FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, e);
                System.err.println("[charsave] Error going back to autocommit mode");
            }
        }
    }

    /**
     * 保存玩家数据
     * @param dc
     * @param fromcs
     */
    public void saveToDB(boolean dc, boolean fromcs) {
        if (this.isSaveing) {
            FileoutputUtil.log(MapleClient.getLogMessage(this, "正在保存数据，本次操作返回."));
            this.isSaveing = false;
            return;
        }
        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            this.isSaveing = true;
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);

            ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, gachexp = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, totalwins = ?, totallosses = ?, pvpExp = ?, pvpPoints = ?, decorate = ?, beans = ?, warning = ?, dollars = ?, sharelots = ?, apstorage = ?, honor = ?, love = ?, playerPoints = ?, playerEnergy = ?, pvpDeaths = ?, pvpKills = ?, pvpVictory = ?, batterytime = ?, exittime = ?, runeresettime = ?, userunenowtime = ?, name = ? WHERE id = ?", 1);
            ps.setInt(1, this.level);
            ps.setInt(2, this.fame);
            ps.setShort(3, this.stats.getStr());
            ps.setShort(4, this.stats.getDex());
            ps.setShort(5, this.stats.getLuk());
            ps.setShort(6, this.stats.getInt());
            ps.setLong(7, this.level >= getMaxLevelForSever() ? 0L : Math.abs(this.exp));
            ps.setInt(8, this.stats.getHp() < 1 ? 50 : this.stats.getHp());
            ps.setInt(9, this.stats.getMp());
            ps.setInt(10, this.stats.getMaxHp());
            ps.setInt(11, this.stats.getMaxMp());
            ps.setShort(12, (short)this.remainingSp);
            ps.setShort(13, this.remainingAp);
            ps.setByte(14, this.gmLevel);
            ps.setByte(15, this.skinColor);
            ps.setByte(16, this.gender);
            ps.setShort(17, this.job);
            ps.setInt(18, this.hair);
            ps.setInt(19, this.face);
            if ((!fromcs) && (this.map != null)) {
                if (this.map.getId() == 180000001) {
                    ps.setInt(20, 180000001);
                } else if ((this.map.getForcedReturnId() != 999999999) && (this.map.getForcedReturnMap() != null)) {
                    ps.setInt(20, this.map.getForcedReturnId());
                } else {
                    ps.setInt(20, this.stats.getHp() < 1 ? this.map.getReturnMapId() : this.map.getId());
                }
            } else {
                ps.setInt(20, this.mapid);
            }
            ps.setLong(21, this.meso);
            ps.setShort(22, this.hpApUsed);
            if (this.map == null) {
                ps.setByte(23, (byte) 0);
            } else {
                MaplePortal closest = this.map.findClosestSpawnpoint(getTruePosition());
                ps.setByte(23, (byte) (closest != null ? closest.getId() : 0));
            }
            ps.setInt(24, this.party == null ? -1 : this.party.getId());
            ps.setShort(25, (short) this.buddylist.getCapacity());
            StringBuilder petz = new StringBuilder();
            if ((this.spawnPets != null) && (this.spawnPets.getSummoned())) {
                this.spawnPets.saveToDb();
                petz.append(this.spawnPets.getInventoryPosition());
            } else {
                petz.append("-1");
            }
            ps.setString(26, petz.toString());
            ps.setByte(27, this.subcategory);
            ps.setInt(28, this.marriageId);
            ps.setInt(29, this.currentrep);
            ps.setInt(30, this.totalrep);
            ps.setInt(31, this.gachexp);
            ps.setShort(32, this.fatigue);
            ps.setInt(33, 0);
            ps.setInt(34, 0);
            ps.setInt(35, 0);
            ps.setInt(36, 0);
            ps.setInt(37, 0);
            ps.setInt(38, 0);
            ps.setInt(39, this.totalWins);
            ps.setInt(40, this.totalLosses);
            ps.setInt(41, 0);
            ps.setInt(42, 0);

            ps.setInt(43, this.decorate);

            ps.setInt(44, this.beans);

            ps.setInt(45, this.warning);

            ps.setInt(46, this.dollars);
            ps.setInt(47, this.shareLots);

            ps.setInt(48, this.apstorage);

            ps.setInt(49, this.honor);

            ps.setInt(50, this.love);

            ps.setInt(51, this.playerPoints);
            ps.setInt(52, this.playerEnergy);

            ps.setInt(53, 0);
            ps.setInt(54, 0);
            ps.setInt(55, 0);
            ps.setInt(56, this.batterytime);
            ps.setLong(57, 0);
            ps.setLong(58, this.runeresettime);
            ps.setLong(59, this.userunenowtime);
            ps.setString(60, this.name);
            ps.setInt(61, this.id);
            if (ps.executeUpdate() < 1) {
                ps.close();
                throw new DatabaseException(new StringBuilder().append("Character not in database (").append(this.id).append(")").toString());
            }
            ps.close();
            if (this.changed_pokemon) {
                ps = con.prepareStatement("DELETE FROM pokemon WHERE characterid = ? OR (accountid = ? AND active = 0)");
                ps.setInt(1, this.id);
                ps.setInt(2, this.accountid);
                ps.execute();
                ps.close();
                ps = con.prepareStatement("INSERT INTO pokemon (characterid, level, exp, monsterid, name, nature, active, accountid, itemid, gender, hpiv, atkiv, defiv, spatkiv, spdefiv, speediv, evaiv, acciv, ability) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (Battler macro : this.battlers) {
                    if (macro != null) {
                        ps.setInt(1, this.id);
                        ps.setInt(2, macro.getLevel());
                        ps.setInt(3, macro.getExp());
                        ps.setInt(4, macro.getMonsterId());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getNature().ordinal());
                        ps.setInt(7, 1);
                        ps.setInt(8, this.accountid);
                        ps.setInt(9, macro.getItem() == null ? 0 : macro.getItem().id);
                        ps.setByte(10, macro.getGender());
                        ps.setByte(11, macro.getIV(BattleConstants.PokemonStat.HP));
                        ps.setByte(12, macro.getIV(BattleConstants.PokemonStat.ATK));
                        ps.setByte(13, macro.getIV(BattleConstants.PokemonStat.DEF));
                        ps.setByte(14, macro.getIV(BattleConstants.PokemonStat.SPATK));
                        ps.setByte(15, macro.getIV(BattleConstants.PokemonStat.SPDEF));
                        ps.setByte(16, macro.getIV(BattleConstants.PokemonStat.SPEED));
                        ps.setByte(17, macro.getIV(BattleConstants.PokemonStat.EVA));
                        ps.setByte(18, macro.getIV(BattleConstants.PokemonStat.ACC));
                        ps.setByte(19, macro.getAbilityIndex());
                        ps.execute();
                    }
                }
                for (Battler macro : this.boxed) {
                    ps.setInt(1, this.id);
                    ps.setInt(2, macro.getLevel());
                    ps.setInt(3, macro.getExp());
                    ps.setInt(4, macro.getMonsterId());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getNature().ordinal());
                    ps.setInt(7, 0);
                    ps.setInt(8, this.accountid);
                    ps.setInt(9, macro.getItem() == null ? 0 : macro.getItem().id);
                    ps.setByte(10, macro.getGender());
                    ps.setByte(11, macro.getIV(BattleConstants.PokemonStat.HP));
                    ps.setByte(12, macro.getIV(BattleConstants.PokemonStat.ATK));
                    ps.setByte(13, macro.getIV(BattleConstants.PokemonStat.DEF));
                    ps.setByte(14, macro.getIV(BattleConstants.PokemonStat.SPATK));
                    ps.setByte(15, macro.getIV(BattleConstants.PokemonStat.SPDEF));
                    ps.setByte(16, macro.getIV(BattleConstants.PokemonStat.SPEED));
                    ps.setByte(17, macro.getIV(BattleConstants.PokemonStat.EVA));
                    ps.setByte(18, macro.getIV(BattleConstants.PokemonStat.ACC));
                    ps.setByte(19, macro.getAbilityIndex());
                    ps.execute();
                }
                ps.close();
            }

            deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            ps.setByte(2, getInventory(MapleInventoryType.EQUIP).getSlotLimit());
            ps.setByte(3, getInventory(MapleInventoryType.USE).getSlotLimit());
            ps.setByte(4, getInventory(MapleInventoryType.SETUP).getSlotLimit());
            ps.setByte(5, getInventory(MapleInventoryType.ETC).getSlotLimit());
            ps.setByte(6, getInventory(MapleInventoryType.CASH).getSlotLimit());
            ps.execute();
            ps.close();

            List itemsWithType = new ArrayList();

            // MapleInventory iv;
            for (MapleInventory iv : this.inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair(item, iv.getType()));
                }
            }
            ItemLoader.装备道具.saveItems(itemsWithType, this.id);

            if (this.changed_keyValue) {
                deleteWhereCharacterId(con, "DELETE FROM character_keyvalue WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO character_keyvalue (`characterid`, `key`, `value`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (Map.Entry key : this.keyValue.entrySet()) {
                    ps.setString(2, (String) key.getKey());
                    ps.setString(3, (String) key.getValue());
                    ps.execute();
                }
                ps.close();
            }

            if (this.changed_questinfo) {
                deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (Map.Entry q : this.questinfo.entrySet()) {
                    ps.setInt(2, ((Integer) q.getKey()));
                    ps.setString(3, (String) q.getValue());
                    ps.execute();
                }
                ps.close();
            }

            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, this.id);
            for (MapleQuestStatus q : this.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000L));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                rs.close();
            }
            ps.close();

            if (this.skills.size() >= 0) {
                deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration, teachId, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, this.id);
                for (Map.Entry skill : this.skills.entrySet()) {
                    ps.setInt(2, ((Skill) skill.getKey()).getId());
                    ps.setInt(3, ((SkillEntry) skill.getValue()).skillLevel);
                    ps.setByte(4, ((SkillEntry) skill.getValue()).masterlevel);
                    ps.setLong(5, ((SkillEntry) skill.getValue()).expiration);
                    ps.setInt(6, ((SkillEntry) skill.getValue()).teachId);
                    ps.setByte(7, ((SkillEntry) skill.getValue()).position);
                    ps.execute();
                }
                ps.close();
            }
            List<MapleCoolDownValueHolder> cd = getCooldowns();
            if ((dc) && (cd.size() > 0)) {
                ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                ps.setInt(1, getId());
                //   for (MapleCoolDownValueHolder cooling : coolDownInfo) {
                for (MapleCoolDownValueHolder cooling : cd) {
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.execute();
                }
                ps.close();
            }

            if (this.changed_savedlocations) {
                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (this.savedLocations[savedLocationType.getValue()] != -1) {
                        ps.setInt(2, savedLocationType.getValue());
                        ps.setInt(3, this.savedLocations[savedLocationType.getValue()]);
                        ps.execute();
                    }
                }
                ps.close();
            }

            if (this.changed_achievements) {
                ps = con.prepareStatement("DELETE FROM achievements WHERE accountid = ?");
                ps.setInt(1, this.accountid);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("INSERT INTO achievements(charid, achievementid, accountid) VALUES(?, ?, ?)");
                for (Integer achid : this.finishedAchievements) {
                    ps.setInt(1, this.id);
                    ps.setInt(2, achid);
                    ps.setInt(3, this.accountid);
                    ps.execute();
                }
                ps.close();
            }

            if (this.buddylist.changed()) {
                deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (BuddylistEntry entry : this.buddylist.getBuddies()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setInt(3, entry.isVisible() ? 0 : 1);
                    ps.execute();
                }
                ps.close();
                this.buddylist.setChanged(false);
            }

            ps = con.prepareStatement("UPDATE accounts SET `ACash` = ?, `mPoints` = ?, `points` = ?, `vpoints` = ? WHERE id = ?");
            ps.setInt(1, this.acash);
            ps.setInt(2, this.maplepoints);
            ps.setInt(3, this.points);
            ps.setInt(4, this.vpoints);
            ps.setInt(5, this.client.getAccID());
            ps.executeUpdate();
            ps.close();

            if (this.storage != null) {
                this.storage.saveToDB();
            }

            if (this.cs != null) {
                this.cs.save();
            }
            PlayerNPC.updateByCharId(this);

            this.keylayout.saveKeys(this.id);

            this.quickslot.saveQuickSlots(this.id);

            this.mount.saveMount(this.id);

            deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO familiars (characterid, expiry, name, fatigue, vitality, familiar) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            for (MonsterFamiliar f : this.familiars.values()) {
                ps.setLong(2, f.getExpiry());
                ps.setString(3, f.getName());
                ps.setInt(4, f.getFatigue());
                ps.setByte(5, f.getVitality());
                ps.setInt(6, f.getFamiliar());
                ps.executeUpdate();
            }
            ps.close();

            deleteWhereCharacterId(con, "DELETE FROM imps WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO imps (characterid, itemid, closeness, fullness, state, level) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            for (MapleImp imp : this.imps) {
                if (imp != null) {
                    ps.setInt(2, imp.getItemId());
                    ps.setShort(3, imp.getCloseness());
                    ps.setShort(4, imp.getFullness());
                    ps.setByte(5, imp.getState());
                    ps.setByte(6, imp.getLevel());
                    ps.executeUpdate();
                }
            }
            ps.close();

            if (this.changed_wishlist) {
                deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");
                for (int i = 0; i < getWishlistSize(); i++) {
                    ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
                    ps.setInt(1, getId());
                    ps.setInt(2, this.wishlist[i]);
                    ps.execute();
                    ps.close();
                }

            }

            deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
            for (int i = 0; i < this.regrocks.length; i++) {
                if (this.regrocks[i] != 999999999) {
                    ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid,vip) VALUES (?, ?, 0)");
                    ps.setInt(1, getId());
                    ps.setInt(2, this.regrocks[i]);
                    ps.execute();
                    ps.close();
                }
            }
            this.isSaveing = false;
            this.changed_wishlist = false;
            this.changed_trocklocations = false;
            this.changed_savedlocations = false;
            this.changed_pokemon = false;
            this.changed_questinfo = false;
            this.changed_achievements = false;
            this.changed_keyValue = false;
            con.commit();
        } catch (SQLException | DatabaseException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, e);
            log.error(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] 保存角色数据出现错误 .")).append(e).toString());
            try {
                con.rollback();
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, ex);
                log.error(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back")).append(ex).toString());
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (pse != null) {
                    pse.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
            } catch (SQLException e) {
                FileoutputUtil.outputFileError(FileoutputUtil.SQL_Ex_Log, e);
                log.error(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode")).append(e).toString());
            }
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, this.id);
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void deleteWhereCharacterId_NoLock(Connection con, String sql, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.execute();
        }
    }

    public PlayerStats getStat() {
        return this.stats;
    }

    public void cancelMapTimeLimitTask() {
        if (this.mapTimeLimitTask != null) {
            this.mapTimeLimitTask.cancel(false);
            this.mapTimeLimitTask = null;
        }
    }

    public void startMapTimeLimitTask(int time, final MapleMap to) {
        if (time <= 0) {
            time = 1;
        }
        this.client.getSession().write(MaplePacketCreator.getClock(time));
        final MapleMap ourMap = getMap();
        time *= 1000;
        this.mapTimeLimitTask = MapTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (ourMap.getId() == 180000001) {
                    MapleCharacter.this.getQuestNAdd(MapleQuest.getInstance(123455)).setCustomData(String.valueOf(System.currentTimeMillis()));
                    MapleCharacter.this.getQuestNAdd(MapleQuest.getInstance(123456)).setCustomData("0");
                }
                MapleCharacter.this.changeMap(to, to.getPortal(0));
            }
        }, time, time);
    }

    public Map<String, String> getKeyValue_Map() {
        return this.keyValue;
    }

    public String getKeyValue(String key) {
        if (this.keyValue.containsKey(key)) {
            return (String) this.keyValue.get(key);
        }
        return null;
    }

    public void setKeyValue(String key, String values) {
        this.keyValue.put(key, values);
        this.changed_keyValue = true;
    }
    public String getInfoQuest(int questid) {
        if (this.questinfo.containsKey(questid)) {
            return (String) this.questinfo.get(questid);
        }
        return "";
    }

    public String getInfoQuestStatS(int id, String stat) {
        String info = getInfoQuest(id);
        if ((info != null) && (info.length() > 0) && (info.contains(stat))) {
            int startIndex = info.indexOf(stat) + stat.length() + 1;
            int until = info.indexOf(";", startIndex);
            return info.substring(startIndex, until != -1 ? until : info.length());
        }
        return "";
    }

    public int getInfoQuestStat(int id, String stat) {
        String statz = getInfoQuestStatS(id, stat);
        return (statz == null) || ("".equals(statz)) ? 0 : Integer.parseInt(statz);
    }

    /*public void setInfoQuestStat(int id, String stat, int statData) {
     setInfoQuestStat(id, stat, statData);
     }*/
    public void setInfoQuestStat(int id, String stat, String statData) {
        String info = getInfoQuest(id);
        if ((info.length() == 0) || (!info.contains(stat))) {
        } else {
            String newInfo = new StringBuilder().append(stat).append("=").append(statData).toString();
            String beforeStat = info.substring(0, info.indexOf(stat));
            int from = info.indexOf(";", info.indexOf(stat) + stat.length());
            String afterStat = from == -1 ? "" : info.substring(from + 1);
        }
    }

    public boolean containsInfoQuest(int questid, String data) {
        if (questinfo.containsKey(questid)) {
            return ((String) questinfo.get(questid)).contains(data);
        }
        return false;
    }

    public int getNumQuest() {
        int i = 0;
        for (MapleQuestStatus q : this.quests.values()) {
            if (q.getStatus() == MapleQuestStatus.QUEST_COMPLETED) {
                i++;
            }
        }
        return i;
    }

    public byte getQuestStatus(int quest) {
        MapleQuest qq = MapleQuest.getInstance(quest);
        if (getQuestNoAdd(qq) == null) {
            return 0;
        }
        return getQuestNoAdd(qq).getStatus();
    }

    /**
     * 获取玩家的任务信息
     * @param quest
     * @return
     */
    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!this.quests.containsKey(quest)) {
            this.setQuestAdd(quest,(byte)0,""); // 一般是脚本任务
        }
        return this.quests.get(quest);
    }

    public boolean needQuestItem(int questId, int itemId) {
        if (questId <= 0) {
            return true;
        }
        MapleQuest quest = MapleQuest.getInstance(questId);
        return false;
    }

    public void setQuestAdd(MapleQuest quest, byte status, String customData) {
        if (!this.quests.containsKey(quest)) {
            MapleQuestStatus stat = new MapleQuestStatus(quest, status);
            stat.setCustomData(customData);
            this.quests.put(quest, stat);
        }
    }

    public MapleQuestStatus getQuestNAdd(MapleQuest quest) {
        if (!this.quests.containsKey(quest)) {
            MapleQuestStatus status = new MapleQuestStatus(quest, 0);
            this.quests.put(quest, status);
            return status;
        }
        return (MapleQuestStatus) this.quests.get(quest);
    }

    public MapleQuestStatus getQuestNoAdd(MapleQuest quest) {
        return (MapleQuestStatus) this.quests.get(quest);
    }

    public MapleQuestStatus getQuestRemove(MapleQuest quest) {
        return (MapleQuestStatus) this.quests.remove(quest);
    }

    public void updateQuest(MapleQuestStatus quest) {
        updateQuest(quest, false);
    }

    /**
     * 更新玩家任务信息
     * @param quest
     * @param update
     */
    public void updateQuest(MapleQuestStatus quest, boolean update) {
        this.quests.put(quest.getQuest(), quest);
        this.client.getSession().write(MaplePacketCreator.updateQuest(quest));
        if ((quest.getStatus() == 1) && (!update)) {
            this.client.getSession().write(MaplePacketCreator.updateQuestInfo(quest.getQuest().getId(), quest.getNpc(), quest.getStatus() == 1));
        }
    }

    public Map<Integer, String> getInfoQuest_Map() {
        return this.questinfo;
    }

    public Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
        return this.quests;
    }

    public ArrayList<Pair<MapleBuffStat, MapleBuffStatValueHolder>> getAllEffects() {
        return new ArrayList(this.effects);
    }

    public MapleBuffStatValueHolder getBuffStatValueHolder(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = null;
        for (Pair buffs : getAllEffects()) {
            if (buffs.getLeft() == stat) {
                mbsvh = (MapleBuffStatValueHolder) buffs.getRight();
            }
        }
        return mbsvh;
    }

    public Integer getBuffedValue(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.value;
    }

    public int getBuffedIntValue(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return 0;
        }
        return mbsvh.value;
    }

    public Integer getBuffedSkill_X(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getX();
    }

    public Integer getBuffedSkill_Y(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getY();
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if ((mbsvh == null) || (mbsvh.effect == null) || (skill == null)) {
            return false;
        }
        return (mbsvh.effect.isSkill()) && (mbsvh.effect.getSourceId() == skill.getId());
    }

    public boolean hasBuffSkill(int skillId) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        boolean find = false;
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if ((mbsvh.effect.isSkill()) && (mbsvh.effect.getSourceId() == skillId)) {
                find = true;
                break;
            }
        }
        allBuffs.clear();
        return find;
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    public int getTrueBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.isSkill() ? mbsvh.effect.getSourceId() : -mbsvh.effect.getSourceId();
    }

    public void setBuffedValue(MapleBuffStat stat, int value) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setSchedule(MapleBuffStat stat, ScheduledFuture<?> sched) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return;
        }
        mbsvh.schedule.cancel(false);
        mbsvh.schedule = sched;
    }

    public Long getBuffedStartTime(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.startTime;
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, int from) {
        registerEffect(effect, starttime, schedule, effect.getStatups(), false, effect.getDuration(), from);
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, List<Pair<MapleBuffStat, Integer>> statups, boolean silent, int localDuration, int from) {
        if (effect.is隐藏术()) {
            map.broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
        } else if (effect.is龙之力()) {
            prepareDragonBlood();
        } else if (effect.is团队治疗()) {
            prepareRecovery();
        } else if (effect.is重生契约()) {
            checkBerserk();
        } else if (effect.is骑兽技能_()) {
            getMount().startSchedule();
        }
        for (Pair statup : statups) {
            int value = ((Integer) statup.getRight());
            this.effects.add(new Pair(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, value, localDuration, from)));
        }
        if (!silent) {
            this.stats.recalcLocalStats(this);
        }
        if (isShowPacket()) {
            dropSpouseMessage(25, new StringBuilder().append("注册BUFF效果 - 当前BUFF总数: ").append(this.effects.size()).append(" 技能: ").append(effect.getSourceId()).toString());
        }
    }

    public int getPartyEffects() {
        MapleBuffStatValueHolder mbsvh = null;
        ArrayList partyEffects = new ArrayList();
        for (Pair buffs : getAllEffects()) {
            mbsvh = (MapleBuffStatValueHolder) buffs.getRight();
            if (mbsvh.effect.isPartyBuff()) {
                partyEffects.add(mbsvh.effect);
            }
        }
        return partyEffects.size();
    }

    public List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List bstats = new ArrayList();
        for (Pair stateffect : getAllEffects()) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) stateffect.getRight();
            if ((mbsvh.effect.sameSource(effect)) && ((startTime == -1L) || (startTime == mbsvh.startTime))) {
                bstats.add(stateffect.getLeft());
            }
        }
        return bstats;
    }

    public List<SpecialBuffInfo> getSpecialBuffInfo(MapleBuffStat stat) {
        List ret = new ArrayList();
        for (Pair stateffect : getAllEffects()) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) stateffect.getRight();
            int skillId = mbsvh.effect.getSourceId();
            if (stateffect.getLeft() == stat) {
                ret.add(new SpecialBuffInfo(mbsvh.effect.isSkill() ? skillId : -skillId, mbsvh.value, mbsvh.localDuration));
            }
        }
        return ret;
    }

    public List<SpecialBuffInfo> getSpecialBuffInfo(MapleBuffStat stat, int buffid, int value, int bufflength) {
        List ret = new ArrayList();
        ret.add(new SpecialBuffInfo(buffid, value, bufflength));
        for (Pair stateffect : getAllEffects()) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) stateffect.getRight();
            int skillId = mbsvh.effect.getSourceId();
            if ((stateffect.getLeft() == stat) && (skillId != Math.abs(buffid))) {
                ret.add(new SpecialBuffInfo(mbsvh.effect.isSkill() ? skillId : -skillId, mbsvh.value, mbsvh.localDuration));
            }
        }
        return ret;
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats, MapleStatEffect effect, boolean overwrite) {
        int effectSize = this.effects.size();
        ArrayList<Pair> effectsToRemove = new ArrayList();
        for (MapleBuffStat stat : stats) {
            for (Pair buffs : getAllEffects()) {
                if ((buffs.getLeft() == stat) && ((effect == null) || (((MapleBuffStatValueHolder) buffs.getRight()).effect.sameSource(effect)))) {
                    effectsToRemove.add(buffs);
                    MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) buffs.getRight();
                    if ((stat == MapleBuffStat.召唤兽) || (stat == MapleBuffStat.灵魂助力) || (stat == MapleBuffStat.攻击力增加)) {
                        int summonId = mbsvh.effect.getSourceId();
                        this.visibleMapObjectsLock.writeLock().lock();
                        this.summonsLock.writeLock().lock();
                        try {
                            for (MapleSummon summon : this.summons.values()) {
                                if ((summon.getSkillId() == summonId)|| (((summon.getSkillId() == summonId + 999) || (((summonId == 1085) || (summonId == 1087) || (summonId == 1090)) && (summon.getSkillId() == summonId - 999))))) {
                                    this.map.broadcastMessage(SummonPacket.removeSummon(summon, overwrite));
                                    this.map.removeMapObject(summon);
                                    this.visibleMapObjects.remove(summon);
                                    this.summons.remove(summon.getSkillId());
                                    /*if (summon.getSkillId() == 22171052) {
                                     client.getSession().write(MaplePacketCreator.spawnDragon(this.dragon));
                                     }*/
                                }
                            }
                        } finally {
                            this.summonsLock.writeLock().unlock();
                            this.visibleMapObjectsLock.writeLock().unlock();
                        }
                    } else if (stat == MapleBuffStat.龙之力) {
                        this.lastDragonBloodTime = 0L;
                    }
                }
            }

            // MapleBuffStat stat;
            int toRemoveSize = effectsToRemove.size();
            for (Pair toRemove : effectsToRemove) {
                if (this.effects.contains(toRemove)) {
                    if (((MapleBuffStatValueHolder) toRemove.getRight()).schedule != null) {
                        ((MapleBuffStatValueHolder) toRemove.getRight()).schedule.cancel(false);
                        ((MapleBuffStatValueHolder) toRemove.getRight()).schedule = null;
                    }
                    this.effects.remove(toRemove);
                }
            }
            effectsToRemove.clear();
            boolean ok = effectSize - this.effects.size() == toRemoveSize;
            if (isShowPacket()) {
                dropSpouseMessage(20, new StringBuilder().append("取消注册的BUFF效果 - 取消前BUFF总数: ").append(effectSize).append(" 当前BUFF总数 ").append(this.effects.size()).append(" 取消的BUFF数量: ").append(toRemoveSize).append(" 是否相同: ").append(ok).toString());
            }
            if (!ok) {
                FileoutputUtil.log(FileoutputUtil.SkillCancel_Error, new StringBuilder().append(getName()).append(" - ").append(getJobName()).append(" 取消BUFF出现错误 技能ID: ").append(effect != null ? Integer.valueOf(effect.getSourceId()) : "技能效果为空!").toString(), true);
            }
        }
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        if (effect == null) {
            return;
        }
        cancelEffect(effect, overwrite, startTime, effect.getStatups());
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime, List<Pair<MapleBuffStat, Integer>> statups) { //TODO 取消BUFF
        if (effect == null) {
            return;
        }
        List buffstats;
        if (!overwrite) {
            if (effect.isPartyBuff() && getParty() != null) {
                int from = getParty().cancelPartyBuff(effect.getSourceId(), getId());
                if (from > 0) {
                    getParty().getPartyBuffs(from);
                    MapleCharacter chr;
                    if (from != getId()) {
                        chr = getOnlineCharacterById(from);
                    } else {
                        chr = this;
                    }
                    if (chr != null) {
//                        MapleStatEffect.apply祈祷众生(chr);
                    }
                }
            }
            buffstats = getBuffStats(effect, startTime);
        } else {
            buffstats = new ArrayList(statups.size());
            for (Pair statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        if (ServerProperties.ShowPacket()) {
            FileoutputUtil.log(new StringBuilder().append("取消技能BUFF: - buffstats.size() ").append(buffstats.size()).toString());
        }
        deregisterBuffStats(buffstats, effect, overwrite);
        if (effect.is时空门()) {
            if (!getDoors().isEmpty()) {
                removeDoor();
                silentPartyUpdate();
            }
        }
        cancelPlayerBuffs(buffstats, overwrite);
        if ((!overwrite)
                && (effect.is隐藏术()) && (this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null)) {
            this.map.broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
        }

        /*if ((effect.getSourceId() == 35121013) && (!overwrite)) {
         SkillFactory.getSkill(35001002).getEffect(getTotalSkillLevel(35001002)).applyTo(this);
         }*/
        if (isShowPacket()) {
            dropMessage(5, new StringBuilder().append("取消BUFF效果 - 当前BUFF总数: ").append(this.effects.size()).append(" 技能: ").append(effect.getSourceId()).toString());
        }
//        if (!overwrite && effect.isPartyBuff() && getParty() == null && getSkillLevel(祭司.祈祷众生) > 0) {
//            if (getPartyEffects() > 1) {
////                MapleStatEffect.apply祈祷众生(this);
//            }
//        }
    }

    public void cancelBuffStats(MapleBuffStat[] stat) {
        List buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList, null, false);
        cancelPlayerBuffs(buffStatList, false);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair<MapleBuffStat, MapleBuffStatValueHolder> buffs : getAllEffects()) {
            if (buffs.getLeft() == stat) {
                allBuffs.add(buffs.getRight());
            }
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, -1L);
        }
        allBuffs.clear();
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat, int from) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            if ((buffs.getLeft() == stat) && (((MapleBuffStatValueHolder) buffs.getRight()).fromChrId == from)) {
                allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
            }
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, -1L);
        }
        allBuffs.clear();
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats, boolean overwrite) {
        boolean write = (this.client != null) && (this.client.getChannelServer() != null) && (this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null);
        if (write) {
            if (buffstats.contains(MapleBuffStat.召唤兽)) {
                buffstats.remove(MapleBuffStat.召唤兽);
                if (buffstats.size() <= 0) {
                    return;
                }
            }
            if (overwrite) {
                List buffStatX = new ArrayList();
                buffstats = buffStatX;
            }

            if (isShowPacket()) {
                dropMessage(5, new StringBuilder().append("取消BUFF效果 - 发送封包 - 是否注册BUFF时: ").append(overwrite).toString());
            }
            this.stats.recalcLocalStats(this);
            this.client.getSession().write(BuffPacket.cancelBuff(buffstats, this));
            this.map.broadcastMessage(this, BuffPacket.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public void dispel() {
        if (!isHidden()) {
            LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
            for (Pair buffs : getAllEffects()) {
                allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
            }

            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if ((mbsvh.effect.isSkill()) && (mbsvh.schedule != null) && (!mbsvh.effect.isMorph()) && (!mbsvh.effect.isGmBuff()) && (!mbsvh.effect.is骑兽技能()) && (!mbsvh.effect.isMechChange()) && (!mbsvh.effect.isNotRemoved())) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public void dispelSkill(int skillId) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if ((mbsvh.effect.isSkill()) && (mbsvh.effect.getSourceId() == skillId)) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void dispelSummons() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSummonMovementType() != null) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispelSummons(int skillId) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if ((mbsvh.effect.getSummonMovementType() != null) && (mbsvh.effect.getSourceId() == skillId)) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void dispelBuff(int buffId) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSourceId() == buffId) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void cancelAllBuffs_() {
        this.effects.clear();
    }

    public void cancelAllBuffs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelMorphs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 61111008:
                case 61120008:
                    return;
            }
            if (mbsvh.effect.isMorph()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public int getMorphState() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph()) {
                return mbsvh.effect.getSourceId();
            }
        }
        return -1;
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.localDuration, mbsvh.statup, mbsvh.fromChrId);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List ret = new ArrayList();
        Map alreadyDone = new HashMap();
        for (Pair mbsvh : getAllEffects()) {
            Pair key = new Pair(Integer.valueOf(((MapleBuffStatValueHolder) mbsvh.getRight()).effect.getSourceId()), Byte.valueOf(((MapleBuffStatValueHolder) mbsvh.getRight()).effect.getLevel()));
            if (alreadyDone.containsKey(key)) {
                ((PlayerBuffValueHolder) ret.get(((Integer) alreadyDone.get(key)))).statup.add(new Pair(mbsvh.getLeft(), ((MapleBuffStatValueHolder) mbsvh.getRight()).value));
            } else {
                alreadyDone.put(key, ret.size());
                ArrayList list = new ArrayList();
                list.add(new Pair(mbsvh.getLeft(), ((MapleBuffStatValueHolder) mbsvh.getRight()).value));
                ret.add(new PlayerBuffValueHolder(((MapleBuffStatValueHolder) mbsvh.getRight()).startTime, ((MapleBuffStatValueHolder) mbsvh.getRight()).effect, list, ((MapleBuffStatValueHolder) mbsvh.getRight()).localDuration, ((MapleBuffStatValueHolder) mbsvh.getRight()).fromChrId));
            }
        }
        for (Pair mbsvh : getAllEffects()) {
            if (((MapleBuffStatValueHolder) mbsvh.getRight()).schedule != null) {
                ((MapleBuffStatValueHolder) mbsvh.getRight()).schedule.cancel(false);
                ((MapleBuffStatValueHolder) mbsvh.getRight()).schedule = null;
            }
        }
        return ret;
    }

    public void cancelMagicDoor() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>();
        for (Pair buffs : getAllEffects()) {
            allBuffs.add((MapleBuffStatValueHolder) buffs.getRight());
        }
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.is时空门()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public boolean canDOT(long now) {
        return (this.lastDOTTime > 0L) && (this.lastDOTTime + 8000L < now);
    }

    public boolean hasDOT() {
        return this.dotHP > 0;
    }

    public void doDOT() {
        addHP(-(this.dotHP * 4));
        this.dotHP = 0;
        this.lastDOTTime = 0L;
    }

    public void setDOT(int d, int source, int sourceLevel) {
        this.dotHP = d;
        addHP(-(this.dotHP * 4));
        this.map.broadcastMessage(MaplePacketCreator.getPVPMist(this.id, source, sourceLevel, d));
        this.lastDOTTime = System.currentTimeMillis();
    }

    public void doDragonBlood() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.龙之力);
        if (bloodEffect == null) {
            this.lastDragonBloodTime = 0L;
            return;
        }
        prepareDragonBlood();
        if (this.stats.getHp() - bloodEffect.getStr() <= 1) {
            cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.龙之力});
        } else {
            addHP(-bloodEffect.getStr());
            this.client.getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 8, getLevel(), bloodEffect.getLevel()));//7+1 119
            this.map.broadcastMessage(this, MaplePacketCreator.showBuffeffect(this, bloodEffect.getSourceId(), 8, getLevel(), bloodEffect.getLevel()), false);//7+1 119
        }
    }

    public boolean canBlood(long now) {
        return (this.lastDragonBloodTime > 0L) && (this.lastDragonBloodTime + 4000L < now);
    }

    private void prepareDragonBlood() {
        this.lastDragonBloodTime = System.currentTimeMillis();
    }

    public void doRecovery() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.恢复效果);
        if (bloodEffect == null) {
            if (bloodEffect == null) {
                this.lastRecoveryTime = 0L;
            } else if (bloodEffect.getSourceId() == 35121005) {
                prepareRecovery();
                if (this.stats.getMp() >= bloodEffect.getU()) {
                    addMP(-bloodEffect.getU());
                }
            }
        } else {
            prepareRecovery();
            healHP(bloodEffect.getX());
        }
    }

    public boolean canRecover(long now) {
        return (this.lastRecoveryTime > 0L) && (this.lastRecoveryTime + 5000L < now);
    }

    private void prepareRecovery() {
        this.lastRecoveryTime = System.currentTimeMillis();
    }

    private void prepareRecoveryEM() {
        this.lastRecoveryTimeEM = System.currentTimeMillis();
    }

    public int getPowerCountByJob() {
        switch (getJob()) {
            case 3610:
                return 10;
            case 3611:
                return 15;
            case 3612:
                return 20;
        }
        return 5;
    }

    public int getPowerCount() {
        return this.powerCount < 0 ? 0 : this.powerCount;
    }

    public void addPowerCount(int delta) {
        Skill skill = SkillFactory.getSkill(30020232);
        int skilllevel = getTotalSkillLevel(skill);
        if (setPowerCount(getPowerCount() + delta)) {
            this.stats.recalcLocalStats(this);
            this.client.getSession().write(BuffPacket.updatePowerCount(30020232, Math.min(getPowerCount(), 20)));
        }
    }

    public boolean setPowerCount(int count) {
        int oldPower = this.powerCount;
        int tempPower = count;
        if (tempPower < 0) {
            tempPower = 0;
        }
        if (tempPower > getPowerCountByJob()) {
            tempPower = getPowerCountByJob();
        }
        this.powerCount = tempPower;
        return this.powerCount != oldPower || this.powerCount == oldPower;
    }

    public boolean isUsePower() {
        this.client.getSession().write(BuffPacket.startPower(this.usePower, this.batterytime));
        return this.usePower;
    }


    /**
     * 增加斗气值
     */
    public void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.斗气集中);
        Skill combos = SkillFactory.getSkill(勇士.斗气集中);

        MapleStatEffect ceffect;
        if (getSkillLevel(combos) > 0) {
            ceffect = combos.getEffect(getTotalSkillLevel(combos));
        } else {
            return;
        }
        if (orbcount < ceffect.getX() + 1) {
            if (ceffect.makeChanceResult()) {
                orbcount++;
            }

            List stat = Collections.singletonList(new Pair(MapleBuffStat.斗气集中, Integer.valueOf(orbcount)));
            setBuffedValue(MapleBuffStat.斗气集中, orbcount);
            int duration = ceffect.getDuration();
            duration += (int) (getBuffedStartTime(MapleBuffStat.斗气集中) - System.currentTimeMillis());
            this.client.getSession().write(BuffPacket.giveBuff(combos.getId(), duration, stat));
            this.map.broadcastMessage(this, BuffPacket.giveForeignBuff(getId(), stat, ceffect), false);
        }
    }

    /**
     * 消耗斗气值
     * @param howmany
     */
    public void handleOrbconsume(int howmany) {
        Skill combos = SkillFactory.getSkill(勇士.斗气集中);
        if (getSkillLevel(combos) <= 0) {
            return;
        }
        MapleStatEffect effect = getStatForBuff(MapleBuffStat.斗气集中);
        if (effect == null) {
            return;
        }
        List stat = Collections.singletonList(new Pair(MapleBuffStat.斗气集中, Integer.valueOf(Math.max(1, getBuffedValue(MapleBuffStat.斗气集中) - howmany))));
        setBuffedValue(MapleBuffStat.斗气集中, Math.max(1, getBuffedValue(MapleBuffStat.斗气集中) - howmany));
        int duration = effect.getDuration();
        duration += (int) (getBuffedStartTime(MapleBuffStat.斗气集中) - System.currentTimeMillis());
        this.client.getSession().write(BuffPacket.giveBuff(combos.getId(), duration, stat));
        this.map.broadcastMessage(this, BuffPacket.giveForeignBuff(getId(), stat, effect), false);
    }

    public int getLightTotal() {
        return this.runningLightSlot;
    }

    public int getLightType() {
        return this.runningLight;
    }

    public int getDarkTotal() {
        return this.runningDarkSlot;
    }

    public int getDarkType() {
        return this.runningDark;
    }


    public boolean canMorphLost(long now) {
        if ((getJob() >= 6100) && (getJob() <= 6112)) {
            return (this.lastMorphLostTime > 0L) && (this.morphCount > 0) && (this.lastMorphLostTime + 20000L < now);
        }
        return false;
    }

    public int getMorphCount() {
        return this.morphCount;
    }

    public void setMorphCount(int amount) {
        this.morphCount = amount;
        if (this.morphCount <= 0) {
            this.morphCount = 0;
        }
    }

    public void handleMorphCharge(int targets) {
        Skill mchskill = SkillFactory.getSkill(60000219);
        int skilllevel = getTotalSkillLevel(mchskill);
        if (skilllevel > 0) {
            MapleStatEffect mcheff = mchskill.getEffect(skilllevel);
            if ((targets > 0) && (mcheff != null)) {
                this.lastMorphLostTime = System.currentTimeMillis();
                int maxCount = getJob() == 6110 ? 400 : getJob() == 6100 ? 100 : 1000;
                if (this.morphCount < maxCount) {
                    this.morphCount += targets;
                    if (this.morphCount >= maxCount) {
                        this.morphCount = maxCount;
                    }
                }
            }
        }
    }

    public void silentEnforceMaxHpMp() {
        this.stats.setMp(this.stats.getMp());
        this.stats.setHp(this.stats.getHp());
    }

    public void enforceMaxHpMp() {
        Map statups = new EnumMap(MapleStat.class);
        if (this.stats.getMp() > this.stats.getCurrentMaxMp()) {
            this.stats.setMp(this.stats.getMp());
            statups.put(MapleStat.MP, (long) this.stats.getMp());
        }
        if (this.stats.getHp() > this.stats.getCurrentMaxHp()) {
            this.stats.setHp(this.stats.getHp());
            statups.put(MapleStat.HP, (long) this.stats.getHp());
        }
        if (statups.size() > 0) {
            this.client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, this));
        }
    }

    public MapleMap getMap() {
        return this.map;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public int getMapId() {
        if (this.map != null) {
            return this.map.getId();
        }
        return this.mapid;
    }

    public byte getInitialSpawnpoint() {
        return this.initialSpawnPoint;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getBlessOfFairyOrigin() {
        return this.BlessOfFairy_Origin;
    }

    public String getBlessOfEmpressOrigin() {
        return this.BlessOfEmpress_Origin;
    }

    public short getLevel() {
        return this.level;
    }

    public int getFame() {
        return this.fame;
    }

    public int getFallCounter() {
        return this.fallcounter;
    }

    public MapleClient getClient() {
        return this.client;
    }

    public void setClient(MapleClient client) {
        this.client = client;
    }

    public long getExp() {
        return this.exp;
    }

    public short getRemainingAp() {
        return this.remainingAp;
    }

    public int getRemainingSp() {
        return this.remainingSp;
    }

    public int getRemainingSp(int skillbook) {
        return this.remainingSp;
    }

    public short getHpApUsed() {
        return this.hpApUsed;
    }

    public boolean isHidden() {
        return getBuffSource(MapleBuffStat.隐身术) / 1000000 == 9;
    }

    public void setHpApUsed(short hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public byte getSkinColor() {
        return this.skinColor;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    public short getJob() {
        return this.job;
    }

    public byte getGender() {
        return this.gender;
    }

    public String getJobName() {
        return MapleJob.getName(MapleJob.getById((int) this.getJob()));
    }

    public byte getSecondGender() {
        return this.gender;
    }

    public byte getZeroLook() {
        if (getKeyValue("Zero_Look") == null) {
            setKeyValue("Zero_Look", "0");
        }
        return Byte.parseByte(getKeyValue("Zero_Look"));
    }

    public boolean isZeroSecondLook() {
        return getZeroLook() == 1;
    }

    public int getHair() {
        return this.hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public int getSecondHair() {
        return this.hair;
    }

    public void setSecondHair(int hair) {
        setKeyValue("Second_Hair", String.valueOf(hair));
    }

    public int getFace() {
        return this.face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getSecondFace() {
        return this.face;
    }

    public void setSecondFace(int face) {
        setKeyValue("Second_Face", String.valueOf(face));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFallCounter(int fallcounter) {
        this.fallcounter = fallcounter;
    }

    public Point getOldPosition() {
        return this.old;
    }

    public void setOldPosition(Point x) {
        this.old = x;
    }

    public void setRemainingAp(short remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public void setInvincible(boolean invinc) {
        this.invincible = invinc;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public BuddyList getBuddylist() {
        return this.buddylist;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void updateFame() {
        updateSingleStat(MapleStat.人气, this.fame);
    }

    public void updateHair(int hair) {
        setHair(hair);
        updateSingleStat(MapleStat.发型, hair);
        equipChanged();
    }

    public void updateFace(int face) {
        setFace(face);
        updateSingleStat(MapleStat.脸型, face);
        equipChanged();
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap maps = this.client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(maps, maps.getPortal(portal));
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(MapleMap to, Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this), null);
    }

    public void changeMap(MapleMap to) {
        changeMapInternal(to, to.getPortal(0).getPosition(), MaplePacketCreator.getWarpToMap(to, 0, this), to.getPortal(0));
    }

    public void changeMap(MapleMap to, MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), null);
    }

    public void changeMapPortal(MapleMap to, MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), pto);
    }

    private void changeMapInternal(final MapleMap to, final Point pos, byte[] warpPacket, final MaplePortal pto) {
        if (to == null) {
            FileoutputUtil.log("目标地图为空了，不能传送过去！");
            return;
        }
        final int nowmapid = this.map.getId();
        if (this.eventInstance != null) {
            this.eventInstance.changedMap(this, to.getId());
        }
        if (this.map.getId() == nowmapid) {
            boolean shouldChange = this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null;
            boolean shouldState = this.map.getId() == to.getId();
            if ((shouldChange) && (shouldState)) {
                to.setCheckStates(false);
            }
            this.map.removePlayer(this);
            if (shouldChange) {
                this.client.getSession().write(warpPacket);
                this.map = to;
                setPosition(pos);
                to.addPlayer(this);
                this.stats.relocHeal(this);
                if (shouldState) {
                    to.setCheckStates(true);
                }
                try {
                    int countRows = ManagerSin.jTable1.getRowCount();//获取当前表格总行数
                    for (int i = 0; i < countRows; i++) {
                        String sname = ManagerSin.jTable1.getValueAt(i, 1).toString();
                        if (sname.equals(this.getName()) && this.map != null) {
                            ((DefaultTableModel) ManagerSin.jTable1.getModel()).setValueAt(to.getMapName() + "(" + this.getMapId() + ")", i, 4);
                            ((DefaultTableModel) ManagerSin.jTable1.getModel()).setValueAt(this.getLevel(), i, 2);
                            break;
                        }
                    }
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.GUI_Ex_Log, e);
                }
            }
        }
    }

    public void cancelChallenge() {
        if ((this.challenge != 0) && (this.client.getChannelServer() != null)) {
            MapleCharacter chr = this.client.getChannelServer().getPlayerStorage().getCharacterById(this.challenge);
            if (chr != null) {
                chr.dropMessage(6, new StringBuilder().append(getName()).append(" 拒绝了您的请求.").toString());
                chr.setChallenge(0);
            }
            dropMessage(6, "您的请求被拒绝.");
            this.challenge = 0;
        }
    }

    public void leaveMap(MapleMap map) {
        this.controlledLock.writeLock().lock();
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            for (MapleMonster mons : this.controlled) {
                if (mons != null) {
                    mons.setController(null);
                    mons.setControllerHasAggro(false);
                    map.updateMonsterController(mons);
                }
            }
            this.controlled.clear();
            this.visibleMapObjects.clear();
        } finally {
            this.controlledLock.writeLock().unlock();
            this.visibleMapObjectsLock.writeLock().unlock();
        }
        if (this.chair != 0) {
            this.chair = 0;
        }
        cancelChallenge();
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        cancelMapTimeLimitTask();
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), this.client, this);
        }
    }

    public void changeJob(int newJob) {
        changeJob(newJob, true);
    }

    public void changeJob(int newJob, boolean gainsp) {
        try {
            cancelEffectFromBuffStat(MapleBuffStat.影分身);
            this.job = (short) newJob;
            updateSingleStat(MapleStat.职业, newJob);
            if ((!GameConstants.is新手职业(newJob)) && gainsp) {
                gainSP(1);
//                    this.remainingSp += (this.getLevel() == 10 ? 5 : 1);
//                    if (newJob % 10 >= 2) {
//                        this.remainingSp += 2;
//                    }
//                if (!isGM()) {
////                    resetStatsByJob(true);
////                    if (getLevel() > (newJob == 200 ? 8 : 10)) {
////                        if ((newJob % 100 == 0) && (newJob % 1000 / 100 > 0)) {
////                            this.remainingSp += 3 * (getLevel() - (newJob == 200 ? 8 : 10));
////                        }
////                    }
//                }
//                FileoutputUtil.log("SP能力点的值为："+this.remainingSp);
//                updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
            }

            int maxhp = this.stats.getMaxHp();
            int maxmp = this.stats.getMaxMp();

            switch (this.job) {
                case 100:
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case 200:
                    maxmp += Randomizer.rand(100, 150);
                    break;
                case 300:
                case 400:
                case 500:
                case 501:
                    maxhp += Randomizer.rand(100, 150);
                    maxmp += Randomizer.rand(25, 50);
                    break;
                case 110:
                case 120:
                case 130:
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case 210:
                case 220:
                case 230:
                    maxmp += Randomizer.rand(400, 450);
                    break;
                case 310:
                case 320:
                case 410:
                case 420:
                case 430:
                case 510:
                case 520:
                case 530:
                case 570:
                case 580:
                case 590:
                    maxhp += Randomizer.rand(200, 250);
                    maxhp += Randomizer.rand(150, 200);
                    break;
                case 800:
                case 900:
                    maxhp += 99999;
                    maxmp += 99999;
            }

            if (maxhp >= getMaxHpForSever()) {
                maxhp = getMaxHpForSever();
            }
            if (maxmp >= getMaxMpForSever()) {
                maxmp = getMaxMpForSever();
            }
            this.stats.setInfo(maxhp, maxmp, maxhp, maxmp);
            Map statup = new EnumMap(MapleStat.class);
            statup.put(MapleStat.MAXHP, (long) maxhp);
            statup.put(MapleStat.MAXMP, (long) maxmp);
            statup.put(MapleStat.HP, (long) maxhp);
            statup.put(MapleStat.MP, (long) maxmp);
            this.stats.recalcLocalStats(this);
            this.client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, this));
            this.map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0x0D), false);
            this.map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
            silentPartyUpdate();
            guildUpdate();
            sidekickUpdate();
            baseSkills();
            giveSubWeaponItem();
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
        }
    }

    public void giveSubWeaponItem() {
        Item toRemove = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        switch (this.job) {
            case 3100:
                if ((toRemove != null) && (toRemove.getItemId() == 1099001)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1099001, (short) -10);
                break;
            case 3110:
                if ((toRemove == null) || (toRemove.getItemId() == 1099002) || (toRemove.getItemId() != 1099001)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1099002, (short) -10);
                break;
            case 3111:
                if ((toRemove == null) || (toRemove.getItemId() == 1099003) || (toRemove.getItemId() != 1099002)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1099003, (short) -10);
                break;
            case 3112:
                if ((toRemove == null) || (toRemove.getItemId() == 1099004) || (toRemove.getItemId() != 1099003)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1099004, (short) -10, 1);
                break;
            case 3101:
                if ((toRemove == null) || (toRemove.getItemId() != 1099006)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1050249, (short) -5, false);
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1070029, (short) -7, false);
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1102505, (short) -9, false);
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1099006, (short) -10);
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1232001, (short) -11);
                    updateHair(getGender() == 0 ? 36460 : 37450);
                }
                if (haveItem(1142553)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142553, (short) 1, new StringBuilder().append("恶魔复仇者1转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3120:
                if ((toRemove != null) && (toRemove.getItemId() != 1099007) && (toRemove.getItemId() == 1099006)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1099007, (short) -10, 1);
                }
                if (haveItem(1142554)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142554, (short) 1, new StringBuilder().append("恶魔复仇者2转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3121:
                if ((toRemove != null) && (toRemove.getItemId() != 1099008) && (toRemove.getItemId() == 1099007)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1099008, (short) -10, 1);
                }
                if (haveItem(1142555)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142555, (short) 1, new StringBuilder().append("恶魔复仇者3转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3122:
                if ((toRemove != null) && (toRemove.getItemId() != 1099009) && (toRemove.getItemId() == 1099008)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1099009, (short) -10, 1);
                }
                if (haveItem(1142556)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142556, (short) 1, new StringBuilder().append("恶魔复仇者4转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3600:
                if ((toRemove == null) || (toRemove.getItemId() != 1353001)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1353001, (short) -10);
                }
                toRemove = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                if ((toRemove == null) || (toRemove.getItemId() != 1242001)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1242001, (short) -11);
                }
                removeAll(1242000, false, false);
                if (haveItem(1142575)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142575, (short) 1, new StringBuilder().append("尖兵1转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3610:
                if ((toRemove != null) && (toRemove.getItemId() != 1353002) && (toRemove.getItemId() == 1353001)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1353002, (short) -10);
                }
                if (haveItem(1142576)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142576, (short) 1, new StringBuilder().append("尖兵2转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3611:
                if ((toRemove != null) && (toRemove.getItemId() != 1353003) && (toRemove.getItemId() == 1353002)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1353003, (short) -10);
                }
                if (haveItem(1142577)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142577, (short) 1, new StringBuilder().append("尖兵3转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 3612:
                if ((toRemove != null) && (toRemove.getItemId() != 1353004) && (toRemove.getItemId() == 1353003)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1353004, (short) -10, 1);
                }
                if (haveItem(1142578)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142578, (short) 1, new StringBuilder().append("尖兵4转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 5100:
                if ((toRemove != null) && (toRemove.getItemId() == 1098000)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1098000, (short) -10);
                break;
            case 5110:
                if ((toRemove == null) || (toRemove.getItemId() == 1098001) || (toRemove.getItemId() != 1098000)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1098001, (short) -10);
                break;
            case 5111:
                if ((toRemove == null) || (toRemove.getItemId() == 1098002) || (toRemove.getItemId() != 1098001)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1098002, (short) -10);
                break;
            case 5112:
                if ((toRemove == null) || (toRemove.getItemId() == 1098003) || (toRemove.getItemId() != 1098002)) {
                    break;
                }
                MapleInventoryManipulator.addItemAndEquip(this.client, 1098003, (short) -10, 1);
                break;
            case 6100:
                if ((toRemove == null) || (toRemove.getItemId() != 1352500)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352500, (short) -10);
                }
                if (haveItem(1142484)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142484, (short) 1, new StringBuilder().append("狂龙1转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6110:
                if ((toRemove != null) && (toRemove.getItemId() != 1352501) && (toRemove.getItemId() == 1352500)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352501, (short) -10);
                }
                if (haveItem(1142485)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142485, (short) 1, new StringBuilder().append("狂龙2转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6111:
                if ((toRemove != null) && (toRemove.getItemId() != 1352502) && (toRemove.getItemId() == 1352501)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352502, (short) -10);
                }
                if (haveItem(1142486)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142486, (short) 1, new StringBuilder().append("狂龙3转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6112:
                if ((toRemove != null) && (toRemove.getItemId() != 1352503) && (toRemove.getItemId() == 1352502)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352503, (short) -10, 1);
                }
                if (haveItem(1142487)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142487, (short) 1, new StringBuilder().append("狂龙4转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6500:
                if ((toRemove == null) || (toRemove.getItemId() != 1352601)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352601, (short) -10);
                }
                if (haveItem(1142495)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142495, (short) 1, new StringBuilder().append("萝莉1转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6510:
                if ((toRemove != null) && (toRemove.getItemId() != 1352602) && (toRemove.getItemId() == 1352601)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352602, (short) -10);
                }
                if (haveItem(1142496)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142496, (short) 1, new StringBuilder().append("萝莉2转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6511:
                if ((toRemove != null) && (toRemove.getItemId() != 1352603) && (toRemove.getItemId() == 1352602)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352603, (short) -10);
                }
                if (haveItem(1142497)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142497, (short) 1, new StringBuilder().append("萝莉3转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                break;
            case 6512:
                if ((toRemove != null) && (toRemove.getItemId() != 1352604) && (toRemove.getItemId() == 1352603)) {
                    MapleInventoryManipulator.addItemAndEquip(this.client, 1352604, (short) -10, 1);
                }
                if (haveItem(1142498)) {
                    break;
                }
                MapleInventoryManipulator.addById(this.client, 1142498, (short) 1, new StringBuilder().append("萝莉4转赠送 时间 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        }
    }

    public void checkZeroItem() {
        if ((this.job != 10112) || (this.level < 100)) {
            return;
        }
        if (getKeyValue("Zero_Item") == null) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

            int[] toRemovePos = {-9, -5, -7};
            for (int pos : toRemovePos) {
                Item toRemove = getInventory(MapleInventoryType.EQUIPPED).getItem((short) pos);
                if (toRemove != null) {
                    MapleInventoryManipulator.removeFromSlot(this.client, MapleInventoryType.EQUIPPED, toRemove.getPosition(), toRemove.getQuantity(), false);
                }
            }

            int[][] equips = {{1003840, -1}, {1032202, -4}, {1052606, -5}, {1072814, -7}, {1082521, -8}, {1102552, -9}, {1113059, -12}, {1113060, -13}, {1113061, -15}, {1113062, -16}, {1122260, -17}, {1132231, -29}, {1152137, -30}};

            for (int[] i : equips) {
                if (ii.itemExists(i[0])) {
                    Equip equip = (Equip) ii.getEquipById(i[0]);
                    equip.setPosition( (byte) i[1]);
                    equip.setQuantity((short) 1);
                    equip.setGMLog("系统赠送");
                    forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIPPED);
                    this.client.getSession().write(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, equip))));
                }
            }
            equipChanged();
            MapleInventoryManipulator.addById(this.client, 1142634, (short) 1, "系统赠送");
            MapleInventoryManipulator.addById(this.client, 2001530, (short) 100, "系统赠送");

            Map list = new HashMap();
            int[] skillIds = {101000103, 101000203};
            for (int i : skillIds) {
                Skill skil = SkillFactory.getSkill(i);
                if ((skil != null) && (getSkillLevel(skil) <= 0)) {
                    list.put(skil, new SkillEntry(8, (byte) skil.getMaxLevel(), -1L));
                }
            }
            if (!list.isEmpty()) {
                changeSkillsLevel(list);
            }
            setKeyValue("Zero_Item", "True");
        }
    }

    public void checkZeroWeapon() {
        if ((this.job != 10112) || (this.level < 100)) {
            return;
        }
        Item lazuliItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        Item lapisItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        int lazuli = lazuliItem != null ? lazuliItem.getItemId() : 0;
        int lapis = lapisItem != null ? lapisItem.getItemId() : 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        equipChanged();
    }

    public int getZeroWeapon(boolean lapis) {
        int weapon = lapis ? 1562000 : 1572000;
        if (this.level < 110) {
            return weapon;
        }
        if ((this.level >= 110) && (this.level < 120)) {
            weapon = lapis ? 1562001 : 1572001;
        } else if ((this.level >= 120) && (this.level < 130)) {
            weapon = lapis ? 1562002 : 1572002;
        } else if ((this.level >= 130) && (this.level < 140)) {
            weapon = lapis ? 1562003 : 1572003;
        } else if ((this.level >= 140) && (this.level < 160)) {
            weapon = lapis ? 1562004 : 1572004;
        } else if ((this.level >= 160) && (this.level < 180)) {
            weapon = lapis ? 1562005 : 1572005;
        } else if ((this.level >= 180) && (this.level < 200)) {
            weapon = lapis ? 1562006 : 1572006;
        } else if (this.level >= 200) {
            weapon = lapis ? 1562007 : 1572007;
        }
        return weapon;
    }

    public void baseSkills() { //TODO 添加基础技能
        checkZeroItem();
        checkZeroWeapon();
        Map list = new HashMap();
        Iterator i$;
        if (GameConstants.getJobNumber(this.job) >= 3) {
            List baseSkills = SkillFactory.getSkillsByJob(this.job);
            if (baseSkills != null) {
                for (i$ = baseSkills.iterator(); i$.hasNext();) {
                    int i = ((Integer) i$.next());
                    Skill skil = SkillFactory.getSkill(i);

                    if ((skil != null) && (!skil.isInvisible()) && (getSkillLevel(skil) <= 0) && (getMasterLevel(skil) <= 0) && (skil.getMasterLevel() > 0)) {
                        list.put(skil, new SkillEntry(0, (byte) skil.getMasterLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((skil != null) && (skil.getName() != null) && (skil.getName().contains("冒险岛勇士")) && (getSkillLevel(skil) <= 0) && (getMasterLevel(skil) <= 0)) {
                        list.put(skil, new SkillEntry(0, (byte) 10, SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((skil != null) && (skil.getName() != null) && (skil.getName().contains("希纳斯的骑士")) && (getSkillLevel(skil) <= 0) && (getMasterLevel(skil) <= 0)) {
                        list.put(skil, new SkillEntry(0, (byte) 30, SkillFactory.getDefaultSExpiry(skil)));
                    }
                }
            }
        }

        if ((this.job >= 3300) && (this.job <= 3312)) {
            Skill skil = SkillFactory.getSkill(30001061);
            if ((skil != null) && (getSkillLevel(skil) <= 0)) {
                list.put(skil, new SkillEntry(skil.getMaxLevel(), (byte) skil.getMaxLevel(), -1L));
            }
        }

        if ((this.job >= 432) && (this.job <= 434)) {
            int[] fixskills = {4311003, 4321006, 4330009, 4341009, 4341002};
            for (int i : fixskills) {
                Skill skil = SkillFactory.getSkill(i);
                if ((skil != null) && (!skil.isInvisible())&& (skil.getMasterLevel() > 0)) {
                    if ((getSkillLevel(skil) <= 0) && (getMasterLevel(skil) <= 0)) {
                        list.put(skil, new SkillEntry(0, (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if (getMasterLevel(skil) <= skil.getMaxLevel()) {
                        list.put(skil, new SkillEntry((byte) getSkillLevel(skil), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    }
                }
            }
        }

        /*for (int i : SkillConstants.特性技能) {
         Skill skil = SkillFactory.getSkill(i);
         if ((skil != null) && (skil.canBeLearnedBy(getJob())) && (getSkillLevel(skil) <= 0)) {
         list.put(skil, new SkillEntry(1, (byte) 1, -1L));
         }
         }*/
        if (!list.isEmpty()) {
            changeSkillsLevel(list);
        }
    }

    public void gainAp(short ap) {
        this.remainingAp = (short) (this.remainingAp + ap);
        updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
    }

    public void gainSP(int sp) {
        this.remainingSp += sp;
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
    }

    public void gainSP(int sp, int skillbook) {
        this.remainingSp += sp;
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
    }

    public void resetSP(int sp) {
        this.remainingSp = sp;
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
    }

    public void resetAPSP() {
        resetSP(0);
        gainAp((short) (-this.remainingAp));
    }

    public List<Integer> getProfessions() {
        List prof = new ArrayList();
        for (int i = 9200; i <= 9204; i++) {
            if (getProfessionLevel(i * 10000) > 0) {
                prof.add(i);
            }
        }
        return prof;
    }

    public byte getProfessionLevel(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (byte) (ret >>> 24 & 0xFF);
    }

    public int getProfessionExp(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (ret & 0xFFFFFF);
    }

    public boolean addProfessionExp(int id, int expGain) {
        int ret = getProfessionLevel(id);
        if ((ret <= 0 || ret >= 12) && id >= 92020000 && id <= 92040000 || (ret <= 0 || ret >= 10) && id >= 92000000 && id <= 92010000) {
            return false;
        }

        int newExp = getProfessionExp(id) + expGain;
        if (newExp >= GameConstants.getProfessionEXP(ret)) {
            changeProfessionLevelExp(id, ret + 1, newExp - GameConstants.getProfessionEXP(ret));
            int traitGain = (int) Math.pow(2.0D, ret + 1);
            switch (id) {
                case 92000000://采药
                    break;
                case 92010000://采矿
                    break;
                case 92020000:
                case 92030000:
                case 92040000:
            }

            return true;
        }
        changeProfessionLevelExp(id, ret, newExp);
        return false;
    }

    public void changeProfessionLevelExp(int id, int level, int exp) {
        changeSingleSkillLevel(SkillFactory.getSkill(id), ((level & 0xFF) << 24) + (exp & 0xFFFF), id >= 92000000 && id <= 92010000 ? (byte) 10 : (byte) 12);
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel) {
        if (skill == null) {
            FileoutputUtil.log("技能为空，忽略！");
            return;
        }
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(int skillId, int newLevel, byte newMasterlevel) {
        Skill skill = SkillFactory.getSkill(skillId);
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
        Map list = new HashMap();
        boolean hasRecovery = false;
        boolean recalculate = false;
        if (changeSkillData(skill, newLevel, newMasterlevel, expiration)) {
            list.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration, getSkillTeachId(skill), getSkillPosition(skill)));
            if (GameConstants.isRecoveryIncSkill(skill.getId())) {
                hasRecovery = true;
            }
            if (skill.getId() < 80000000) {
                recalculate = true;
            }
        } else {
            FileoutputUtil.log("修改技能等级失败！");
            return;
        }
        this.client.getSession().write(MaplePacketCreator.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    public void changeSkillsLevel(Map<Skill, SkillEntry> skills) {
        if (skills.isEmpty()) {
            return;
        }
        Map list = new HashMap();
        boolean hasRecovery = false;
        boolean recalculate = false;
        for (Map.Entry data : skills.entrySet()) {
            if (changeSkillData((Skill) data.getKey(), ((SkillEntry) data.getValue()).skillLevel, ((SkillEntry) data.getValue()).masterlevel, ((SkillEntry) data.getValue()).expiration)) {
                list.put(data.getKey(), data.getValue());
                if (GameConstants.isRecoveryIncSkill(((Skill) data.getKey()).getId())) {
                    hasRecovery = true;
                }
                if (((Skill) data.getKey()).getId() < 80000000) {
                    recalculate = true;
                }
            }
        }
        if (list.isEmpty()) {
            return;
        }
        this.client.getSession().write(MaplePacketCreator.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    private void reUpdateStat(boolean hasRecovery, boolean recalculate) {
        if (hasRecovery) {
            this.stats.relocHeal(this);
        }
        if (recalculate) {
            this.stats.recalcLocalStats(this);
        }
    }

    public boolean changeSkillData(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
        if ((skill == null)) {
            FileoutputUtil.log("changeSkillData die()!!!");
            return false;
        }
        if ((newLevel == 0) && (newMasterlevel == 0)) {
            if (this.skills.containsKey(skill)) {
                this.skills.remove(skill);
            } else {
                return false;
            }
        } else {
            this.skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration, getSkillTeachId(skill), getSkillPosition(skill)));
        }
        return true;
    }

    public void changeSkillLevel_Skip(Map<Skill, SkillEntry> skill) {
        changeSkillLevel_Skip(skill, false);
    }

    public void changeSkillLevel_Skip(Map<Skill, SkillEntry> skill, boolean write) {
        if (skill.isEmpty()) {
            return;
        }
        Map newlist = new HashMap();
        for (Map.Entry date : skill.entrySet()) {
            if (date.getKey() == null) {
                continue;
            }
            newlist.put(date.getKey(), date.getValue());
            if ((((SkillEntry) date.getValue()).skillLevel == 0) && (((SkillEntry) date.getValue()).masterlevel == 0)) {
                if (skills.containsKey(date.getKey())) {
                    skills.remove(date.getKey());
                }
            } else {
                newlist.put(date.getKey(), date.getValue());
            }
        }
        if ((write) && (!newlist.isEmpty())) {
            this.client.getSession().write(MaplePacketCreator.updateSkills(newlist));
        }
    }

    public void changePetSkillLevel(Map<Skill, SkillEntry> skill) {
        if (skill.isEmpty()) {
            return;
        }
        Map newlist = new HashMap();
        for (Map.Entry date : skill.entrySet()) {
            if (date.getKey() == null) {
                continue;
            }
            if ((((SkillEntry) date.getValue()).skillLevel == 0) && (((SkillEntry) date.getValue()).masterlevel == 0)) {
                if (this.skills.containsKey(date.getKey())) {
                    this.skills.remove(date.getKey());
                    newlist.put(date.getKey(), date.getValue());
                }

            } else if (getSkillLevel((Skill) date.getKey()) != ((SkillEntry) date.getValue()).skillLevel) {
                this.skills.put((Skill) date.getKey(), (SkillEntry) date.getValue());
                newlist.put(date.getKey(), date.getValue());
            }
        }
//         if (!newlist.isEmpty()) {
//             for (Map.Entry date : newlist.entrySet()) {
//                 this.client.getSession().write(MaplePacketCreator.updatePetSkill(((Skill) date.getKey()).getId(), ((SkillEntry) date.getValue()).skillevel, ((SkillEntry) date.getValue()).masterlevel, ((SkillEntry) date.getValue()).expiration));
//                             }
//             reUpdateStat(false, true);
//                     }
    }

    public void changeTeachSkill(int skillId, int toChrId) {
        Skill skill = SkillFactory.getSkill(skillId);
        if (skill == null) {
            return;
        }
        this.client.getSession().write(MaplePacketCreator.updateSkill(skillId, toChrId, 1, -1L));
        this.skills.put(skill, new SkillEntry(this.getTotalSkillLevel(skillId), (byte) skill.getMasterLevel(), -1L, toChrId));
    }

    public void playerDead() {//TODO 角色死亡
        MapleBuffStat[] 复活BUFF = {};
        for (MapleBuffStat stat : 复活BUFF) {
            MapleStatEffect statss = getStatForBuff(stat);
            if (statss != null) {
                int 恢复血量百分比 = statss.getX() <= 0 ? 100 : statss.getX();
                int 冷却时间 = 0;
                冷却时间 = statss.getCooldown(this);
                dropMessage(5, "由于" + stat.name() + "的效果发动，无视本次死亡。");
                getStat().setHp(getStat().getMaxHp() / 100 * 恢复血量百分比);
                setStance(0);
                this.dispelDebuffs();
                cancelEffectFromBuffStat(stat);
                if (冷却时间 > 0 && !skillisCooling(statss.getSourceId())) {
                    getClient().getSession().write(MaplePacketCreator.skillCooldown(statss.getSourceId(), 冷却时间));
                    addCooldown(statss.getSourceId(), System.currentTimeMillis(), 冷却时间 * 1000);
                }
                return;
            }
        }
        cancelEffectFromBuffStat(MapleBuffStat.影分身);
        cancelEffectFromBuffStat(MapleBuffStat.最大体力);
        cancelEffectFromBuffStat(MapleBuffStat.最大魔力);
        cancelEffectFromBuffStat(MapleBuffStat.神圣之火_最大体力百分比);
        cancelEffectFromBuffStat(MapleBuffStat.神圣之火_最大魔力百分比);
        dispelSummons();
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        setPowerCount(0);
        this.dotHP = 0;
        this.lastDOTTime = 0L;
        this.morphCount = 0;
        if ((!GameConstants.is新手职业(this.job))) {
            int charms = getItemQuantity(5130000, false);
            if (charms > 0) {
                MapleInventoryManipulator.removeById(this.client, MapleInventoryType.CASH, 5130000, 1, true, false);
                charms--;
                if (charms > 255) {
                    charms = 255;
                }
                this.client.getSession().write(MTSCSPacket.useCharm((byte) charms, (byte) 0));
            } else {
                long expforlevel = getExpNeededForLevel();
                float diepercentage= 0.01F;;
                long v10 = this.exp - (long) (expforlevel * diepercentage);
                if (v10 < 0L) {
                    v10 = 0L;
                }
                this.exp = v10;
            }
            updateSingleStat(MapleStat.经验, this.exp);
        }
        this.cancelAllBuffs();
        cancelAllBuffs_();
    }

    public void updatePartyMemberHP() {
        int channel;
        if ((this.party != null) && (this.client.getChannelServer() != null)) {
            channel = this.client.getChannel();
            for (MaplePartyCharacter partychar : this.party.getMembers()) {
                if ((partychar != null) && (partychar.getMapid() == getMapId()) && (partychar.getChannel() == channel)) {
                    MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.getClient().getSession().write(PartyPacket.updatePartyMemberHP(getId(), this.stats.getHp(), this.stats.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void receivePartyMemberHP() {
        if (this.party == null) {
            return;
        }
        int channel = this.client.getChannel();
        for (MaplePartyCharacter partychar : this.party.getMembers()) {
            if ((partychar != null) && (partychar.getMapid() == getMapId()) && (partychar.getChannel() == channel)) {
                MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                if (other != null) {
                    this.client.getSession().write(PartyPacket.updatePartyMemberHP(other.getId(), other.getStat().getHp(), other.getStat().getCurrentMaxHp()));
                }
            }
        }
    }

    public void healHP(int delta) {
        addHP(delta);
        this.client.getSession().write(MaplePacketCreator.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, MaplePacketCreator.showHpHealed(getId(), delta), false);
    }

    public void healMP(int delta) {
        addMP(delta);
        this.client.getSession().write(MaplePacketCreator.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, MaplePacketCreator.showHpHealed(getId(), delta), false);
    }

    public void addHP(int delta) {
        if (this.stats.setHp(this.stats.getHp() + delta)) {
            updateSingleStat(MapleStat.HP, this.stats.getHp());
        }
    }

    public void addMP(int delta) {
        if (this.stats.setMp(this.stats.getMp() + delta)) {
            // @TODO 这里有BUG
            updateSingleStat(MapleStat.MP, this.stats.getMp());
        }
    }

    public void addDemonMp(int delta) {
        if ((delta > 0) && this.stats.setMp(this.stats.getMp() + delta)) {
            updateSingleStat(MapleStat.MP, this.stats.getMp());
        }
    }

    /**
     * 角色血蓝变动
     * @param hpDiff
     * @param mpDiff
     */
    public void addMPHP(int hpDiff, int mpDiff) {
        int alpha = Math.min(getStat().getCurrentMaxHp(), stats.getHp() + hpDiff);
        int beta = Math.min(getStat().getCurrentMaxMp(), stats.getMp() + mpDiff);
        Map statups = new EnumMap(MapleStat.class);
        if (alpha < 0) {
            alpha = 0;
        }
        if (beta < 0) {
            beta = 0;
        }
        if (stats.setMp(beta)) {
            statups.put(MapleStat.MP, (long) stats.getMp());
        }
        if (stats.setHp(alpha)) {
            statups.put(MapleStat.HP, (long) stats.getHp());
        }
        if (statups.size() > 0) {
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, this));
        }
    }

    public void updateSingleStat(MapleStat stat, long newval) {
        updateSingleStat(stat, newval, false);
    }

    public void updateSingleStat(MapleStat stat, long newval, boolean itemReaction) {
        Map statup = new EnumMap(MapleStat.class);
        statup.put(stat, newval);
        this.client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, itemReaction, this));
    }

    public void gainExp(int total) {
        gainExp(total,true,false,false);
    }

    public void gainExp(int total, boolean show, boolean inChat, boolean white) {
        try {
            long prevexp = getExp();
            long needed = getExpNeededForLevel();
            if (total > 0) {
                this.stats.checkEquipLevels(this, total);
            }
            if (this.level >= getMaxLevelForSever()) {
                setExp(0L);
            } else {
                boolean leveled = false;
                long tot = this.exp + total;
                if (tot >= needed) {
                    exp += total;
                    levelUp();
                    leveled = true;
                    if (this.level >= getMaxLevelForSever()) {
                        setExp(0L);
                    } else {
                        needed = getExpNeededForLevel();
                        if (this.exp >= needed) {
                            setExp(needed - 1L);
                        }
                    }
                } else {
                    exp += total;
                }
                if (total > 0) {
                    familyRep((int) prevexp, (int) needed, leveled);
                }
            }
            if (total != 0) {
                if (this.exp < 0L) {
                    if (total > 0) {
                        setExp(needed);
                    } else if (total < 0) {
                        setExp(0L);
                    }
                }
                updateSingleStat(MapleStat.经验, getExp());
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
        }
    }

    public void familyRep(int prevexp, int needed, boolean leveled) {

    }

    /**
     * 打怪后获得经验
     * @param gain
     * @param 显示
     * @param 最高伤害
     * @param pty
     * @param Class_Bonus_EXP
     * @param mob
     */
    public void gainExpMonster(long gain, boolean 显示, boolean 最高伤害, byte pty, int Class_Bonus_EXP, MapleMonster mob) {
        double 额外的经验值倍率 = 1.0D;
        MonsterStatusEffect ms = mob.getBuff(MonsterStatus.挑衅);
        if (ms != null) {
            额外的经验值倍率 *= 1.0D + (ms.getX() / 100.0D);
        }
        额外的经验值倍率 *= this.getStat().expBuff / 100;
        额外的经验值倍率 *= getEXPMod();
        额外的经验值倍率 *= getClient().getChannelServer().getExpRate(getClient().getWorld());
        int 额外的经验值 = 额外的经验值倍率 > 1.0D ? (int) (gain * (额外的经验值倍率 - 1.0D)) : 0;

        long Sidekick_Bonus_EXP = 0L;
        if (this.sidekick != null) {
            MapleCharacter side = this.map.getCharacterById(this.sidekick.getCharacter(this.sidekick.getCharacter(0).getId() == getId() ? 1 : 0).getId());
            if (side != null) {
                Sidekick_Bonus_EXP = gain / 2L;
            }
        }
        int 结婚奖励经验 = 0;
        if (this.marriageId > 0) {
            MapleCharacter marrChr = this.map.getCharacterById(this.marriageId);
            if (marrChr != null) {
                结婚奖励经验 = (int) (gain / 100.0D * 10.0D);
            }
        }
        int 组队经验 = 0;
        long prevexp = getExp();
        if (pty > 1) {
            int rate;
            if (mob.getStats().getPartyBonusRate() > 0) {
                rate = mob.getStats().getPartyBonusRate();
            } else if (map != null && map.getPartyBonusRate() > 0 && mob.getStats().isPartyBonus()) {
                rate = map.getPartyBonusRate();
            } else {
                rate = 5;
            }
            if (getParty() != null && rate > 5) {
                int i = 0;
                for (MaplePartyCharacter mpc : getParty().getMembers()) {
                    if (mpc.getMapid() == this.getMapId() && mpc.getId() != this.getId() && mpc.isOnline() && mpc.getJobId() / 10 == 23 && i < 4) {
                        i++;
                    }
                }
                rate += i * 20;
            }
            组队经验 = (int) ((float) gain * rate * (pty + (rate > 5 ? -1 : 1)) / 100.0D);
        }

        long total = gain + 额外的经验值 + 组队经验  + Sidekick_Bonus_EXP  + 结婚奖励经验;
        if ((gain > 0L) && (total < gain)) {
            total = 2147483647L;
        }
        if (total > 0L) {
            this.stats.checkEquipLevels(this, (int) total);
        }
        long needed = getExpNeededForLevel();
        if (this.level >= getMaxLevelForSever()) {
            setExp(0L);
        } else {
            boolean leveled = false;
            if ((this.exp + total >= needed) || (this.exp >= needed)) {
                exp += total;
                while (this.exp > needed) {
                    levelUp();
                    needed = getExpNeededForLevel();
                }
                leveled = true;
                if (this.level >= getMaxLevelForSever()) {
                    setExp(0L);
                } else {
                    needed = getExpNeededForLevel();
                    if (this.exp >= needed) {
                        setExp(needed);
                    }
                }
            } else {
                exp += total;
            }
            if (total > 0L) {
                familyRep((int) prevexp, (int) needed, leveled);
            }
        }
        if (total != 0L) {
            if (this.exp < 0L) {
                if (total > 0L) {
                    setExp(getExpNeededForLevel());
                } else if (total < 0L) {
                    setExp(0L);
                }
            }
            updateSingleStat(MapleStat.经验, getExp());
            if (显示) {
                Map expStatup = new EnumMap(MapleExpStat.class);
                if (组队经验 > 0) {
                    expStatup.put(MapleExpStat.组队经验, 组队经验);
                }
                if (结婚奖励经验 > 0) {
                    expStatup.put(MapleExpStat.结婚奖励经验, 结婚奖励经验);
                }
                this.client.getSession().write(MaplePacketCreator.GainEXP_Monster((int) total, 最高伤害, expStatup));
            }
        }
        killMonsterExps.add(gain);
    }

    public void forceReAddItem_NoUpdate(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
    }

    public void forceReAddItem(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.getSession().write(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, item))));
        }
    }

    public void forceUpdateItem(Item item) {
        forceUpdateItem(item, false);
    }

    public void forceUpdateItem(Item item, boolean updateTick) {
        List mods = new LinkedList();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        this.client.getSession().write(InventoryPacket.modifyInventory(updateTick, mods, this));
    }

    public void forceReAddItem_Book(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.getSession().write(MaplePacketCreator.upgradeBook(item, this));
        }
    }

    public void silentPartyUpdate() {
        if (this.party != null) {
            WrodlPartyService.getInstance().updateParty(this.party.getId(), PartyOperation.更新队伍, new MaplePartyCharacter(this));
        }
    }

    public boolean isSuperGM() {
        return this.gmLevel >= PlayerGMRank.SUPERGM.getLevel();
    }

    public boolean isIntern() {
        return this.gmLevel >= PlayerGMRank.INTERN.getLevel();
    }

    public boolean isGM() {
        return this.gmLevel >= PlayerGMRank.GM.getLevel();
    }

    public boolean isAdmin() {
        return this.gmLevel >= PlayerGMRank.ADMIN.getLevel();
    }

    public int getGMLevel() {
        return this.gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return this.gmLevel >= level;
    }

    public void setGmLevel(int level) {
        this.gmLevel = (byte) level;
    }

    public boolean isShowPacket() {
        return ServerProperties.ShowPacket();
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return this.inventory[type.ordinal()];
    }

    public MapleInventory[] getInventorys() {
        return this.inventory;
    }

    public boolean canExpiration(long now) {
        return (this.lastExpirationTime > 0L) && (this.lastExpirationTime + 60000L < now);
    }

    public void expirationTask() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(122700));

        List ret = new ArrayList();
        long currenttime = System.currentTimeMillis();
        List<Triple> tobeRemoveItem = new ArrayList();
        List<Item> tobeUnlockItem = new ArrayList();
        for (MapleInventoryType inv : MapleInventoryType.values()) {
            for (Item item : getInventory(inv)) {
                long expiration = item.getExpiration();
                if (((expiration != -1L) && (!ItemConstants.isPet(item.getItemId())) && (currenttime > expiration)) || (ii.isLogoutExpire(item.getItemId()))) {
                    if (ItemFlag.封印.check(item.getFlag())) {
                        tobeUnlockItem.add(item);
                    } else if (currenttime > expiration) {
                        tobeRemoveItem.add(new Triple(inv, item, true));
                    }
                } else if ((item.getItemId() == 5000054) && (item.getPet() != null) && (item.getPet().getSecondsLeft() <= 0)) {
                    tobeRemoveItem.add(new Triple(inv, item, false));
                } else if ((item.getPosition() == -38) && ((stat == null) || (stat.getCustomData() == null) || (Long.parseLong(stat.getCustomData()) < currenttime))) {
                    tobeRemoveItem.add(new Triple(inv, item, false));
                }
            }
        }
        for (Triple itemz : tobeRemoveItem) {
            Item item = (Item) itemz.getMid();
            if (item == null) {
                FileoutputUtil.log(FileoutputUtil.Item_Expire, new StringBuilder().append(getName()).append(" 检测道具已经过期，但道具为空，无法继续执行。").toString(), true);
                continue;
            }
            if (((Boolean) itemz.getRight())) {
                if (MapleInventoryManipulator.removeFromSlot(this.client, (MapleInventoryType) itemz.getLeft(), item.getPosition(), item.getQuantity(), false)) {
                    ret.add(item.getItemId());
                }
                if (itemz.getLeft() == MapleInventoryType.EQUIPPED) {
                    equipChanged();
                }
            } else if (item.getPosition() == -38) {
                short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                if (slot > -1) {
                    MapleInventoryManipulator.unequip(this.client, item.getPosition(), slot);
                }
            }
        }
        for (Item itemz : tobeUnlockItem) {
            itemz.setExpiration(-1L);
            itemz.setFlag((short) (byte) (itemz.getFlag() - ItemFlag.封印.getValue()));
            forceUpdateItem(itemz);
            dropMessage(6, new StringBuilder().append("封印道具[").append(ii.getName(itemz.getItemId())).append("]封印时间已过期。").toString());
        }
        this.pendingExpiration = ret;

        List<Skill> tobeRemoveSkill = new ArrayList();
        Map tobeRemoveList = new HashMap();
        for (Map.Entry skil : this.skills.entrySet()) {
            if ((((SkillEntry) skil.getValue()).expiration != -1L) && (currenttime > ((SkillEntry) skil.getValue()).expiration)) {
                tobeRemoveSkill.add((Skill) skil.getKey());
            }
        }

        for (Skill skil : tobeRemoveSkill) {
            tobeRemoveList.put(skil, new SkillEntry(0, (byte) 0, -1L));
            this.skills.remove(skil);
        }
        this.pendingSkills = tobeRemoveList;
        if ((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) < currenttime)) {
            this.quests.remove(MapleQuest.getInstance(7830));
            this.quests.remove(MapleQuest.getInstance(122700));
            this.client.getSession().write(MaplePacketCreator.pendantSlot(false));
        }

        Item itemFix = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -37);
        if ((itemFix != null) && (itemFix.getItemId() / 10000 != 119)) {
            short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
            if (slot > -1) {
                MapleInventoryManipulator.unequip(this.client, itemFix.getPosition(), slot);
                dropMessage(5, new StringBuilder().append("装备道具[").append(ii.getName(itemFix.getItemId())).append("]由于装备的位置错误已自动取下。").toString());
            }

        }

        Timestamp currentVipTime = new Timestamp(System.currentTimeMillis());

        if (!this.pendingExpiration.isEmpty()) {
            for (Integer itemId : this.pendingExpiration) {
                if (ii.isCash(itemId)) {
                    this.client.getSession().write(MaplePacketCreator.showCashItemExpired(itemId));
                } else {
                    this.client.getSession().write(MaplePacketCreator.showItemExpired(itemId));
                }
            }
        }
        this.pendingExpiration = null;

        if (!this.pendingSkills.isEmpty()) {
            this.client.getSession().write(MaplePacketCreator.updateSkills(this.pendingSkills));
//            this.client.getSession().write(MaplePacketCreator.showSkillExpired(this.pendingSkills));
        }
        this.pendingSkills = null;
        this.lastExpirationTime = System.currentTimeMillis();
    }

    public MapleShop getShop() {
        return this.shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public int[] getSavedLocations() {
        return this.savedLocations;
    }
	
	public int getSavedLocation(String type) {
        return this.savedLocations[SavedLocationType.fromString(type).getValue()];
    }

    public int getSavedLocation(SavedLocationType type) {
        return this.savedLocations[type.getValue()];
    }

    public void saveLocation(SavedLocationType type) {
        this.savedLocations[type.getValue()] = getMapId();
        this.changed_savedlocations = true;
    }

    public void saveLocation(String type) {
        this.savedLocations[SavedLocationType.fromString(type).getValue()] = getMapId();
        this.changed_savedlocations = true;
    }

    public void saveLocation(SavedLocationType type, int mapz) {
        this.savedLocations[type.getValue()] = mapz;
        this.changed_savedlocations = true;
    }

    public void clearSavedLocation(SavedLocationType type) {
        this.savedLocations[type.getValue()] = -1;
        this.changed_savedlocations = true;
    }

    public int getMeso() {
        return this.meso;
    }

    public void gainMeso(long gain, boolean show) {
        gainMeso(gain, show, false);
    }

    public void gainMeso(long gain, boolean show, boolean inChat) {
        if (this.meso + gain < 0) {
            this.client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        this.meso += gain;
        if (this.meso > 2147480000) {
            this.meso = 2147480000;
        }
        updateSingleStat(MapleStat.金币, this.meso, false);
        this.client.getSession().write(MaplePacketCreator.enableActions());
        if (show) {
            this.client.getSession().write(MaplePacketCreator.showMesoGain(gain, inChat));
        }
    }

    public int getAccountID() {
        return this.accountid;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        if (monster == null) {
            return;
        }
        monster.setController(this);
        this.controlledLock.writeLock().lock();
        try {
            this.controlled.add(monster);
        } finally {
            this.controlledLock.writeLock().unlock();
        }
        this.client.getSession().write(MobPacket.controlMonster(monster, false, aggro));
        monster.sendStatus(this.client);
    }

    public void stopControllingMonster(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        this.controlledLock.writeLock().lock();
        try {
            if (this.controlled.contains(monster)) {
                this.controlled.remove(monster);
            }
        } finally {
            this.controlledLock.writeLock().unlock();
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        } else {
            monster.switchController(this, true);
        }
    }

    public int getControlledSize() {
        return this.controlled.size();
    }

    public List<MapleQuestStatus> getStartedQuests() {
        List ret = new LinkedList();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == MapleQuestStatus.QUEST_STARTED) && (!q.getQuest().isBlocked())) {
                ret.add(q);
            }
        }
        return ret;
    }

    public void addQuest(MapleQuest quest){
        MapleQuestStatus status = new MapleQuestStatus(quest, MapleQuestStatus.QUEST_STARTED);
        status.setCustomData(quest.getStartStatus());
        this.quests.put(quest, status);
        updateQuest(status);
    }

    public MapleQuest getQuestInfoById(int questId) {
        for (MapleQuestStatus q : this.quests.values()) {
            if (q.getQuest().getId() == questId && (q.getStatus() == MapleQuestStatus.QUEST_STARTED) && (!q.getQuest().isBlocked())) {
                return q.getQuest();
            }
        }
        return null;
    }

    /**
     * 获得所有完成的任务
     * @return
     */
    public Map<Integer,MapleQuestStatus> getCompletedQuests() {
        Map<Integer,MapleQuestStatus> ret = new LinkedHashMap<>();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == MapleQuestStatus.QUEST_COMPLETED) && (!q.getQuest().isBlocked())) {
                ret.put(q.getQuest().getId(),q);
            }
        }
        return ret;
    }

    public List<Pair<Integer, Long>> getCompletedMedals() {
        List ret = new ArrayList();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == MapleQuestStatus.QUEST_COMPLETED) && (!q.getQuest().isBlocked()) && (q.getQuest().getMedalItem() > 0) && (ItemConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP)) {
                ret.add(new Pair(q.getQuest().getId(), q.getCompletionTime()));
            }
        }
        return ret;
    }

    public void mobKilled(int id, int skillID) {
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() != 1) || (!q.hasMobKills())) {
                continue;
            }
            if (q.mobKilled(id, skillID)) {
                if (q.getQuest().canComplete(this)) {
                    this.client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(this.skills);
    }

    public Map<Skill, SkillEntry> getSkills(boolean packet) {
        Map<String, Integer> oldlist = new LinkedHashMap(this.skills);
        Map newlist = new LinkedHashMap();
        for (Map.Entry skill : oldlist.entrySet()) {
            newlist.put(skill.getKey(), skill.getValue());
        }
        return newlist;
    }

    public int getAllSkillLevels() {
        int rett = 0;
        for (Map.Entry ret : this.skills.entrySet()) {
            if ((!((Skill) ret.getKey()).isSpecialSkill()) && (((SkillEntry) ret.getValue()).skillLevel > 0)) {
                rett += ((SkillEntry) ret.getValue()).skillLevel;
            }
        }
        return rett;
    }

    public long getSkillExpiry(Skill skill) {
        if (skill == null) {
            return 0L;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.skillLevel <= 0)) {
            return 0L;
        }
        return ret.expiration;
    }

    public int getSkillLevel(int skillid) {
        return getSkillLevel(SkillFactory.getSkill(skillid));
    }

    public int getSkillLevel(Skill skill) {
        if (skill == null) {
            return 0;
        }
        int skillLevel;
        if (getJob() >= skill.getId() / 10000 && getJob() < skill.getId() / 10000 + 3) {
            skillLevel = skill.getFixLevel();
        } else {
            skillLevel = 0;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if (ret == null || ret.skillLevel <= 0) {
            return skillLevel;
        } else {
            skillLevel += ret.skillLevel;
        }
        return skillLevel;
    }

    public int getTotalSkillLevel(int skillid) {
        int a = getTotalSkillLevel(SkillFactory.getSkill(skillid));
        return a;
    }

    public int getTotalSkillLevel(Skill skill) {
        if (skill == null) {
            return 0;
        }
        int skillLevel;
        if (getJob() >= skill.getId() / 10000 && getJob() < skill.getId() / 10000 + 3) {
            skillLevel = skill.getFixLevel();
        } else {
            skillLevel = 0;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if (ret == null || ret.skillLevel <= 0) {
            return skillLevel;
        } else {
            skillLevel += ret.skillLevel;
        }
        return Math.min(skill.getTrueMax(), skillLevel + this.stats.combatOrders + (skill.getMaxLevel() > 10 ? this.stats.incAllskill : 0) + this.stats.getSkillIncrement(skill.getId()));
    }

    public byte getMasterLevel(int skillId) {
        return getMasterLevel(SkillFactory.getSkill(skillId));
    }

    // 获得该技能的最大等级
    public byte getMasterLevel(Skill skill) {
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public int getSkillTeachId(int skillId) {
        return getSkillTeachId(SkillFactory.getSkill(skillId));
    }

    public int getSkillTeachId(Skill skill) {
        if (skill == null) {
            return 0;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.teachId == 0)) {
            return 0;
        }
        return ret.teachId;
    }

    public byte getSkillPosition(int skillId) {
        return getSkillPosition(SkillFactory.getSkill(skillId));
    }

    public byte getSkillPosition(Skill skill) {
        if (skill == null) {
            return -1;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.position == -1)) {
            return -1;
        }
        return ret.position;
    }

    public void levelUp() {
        levelUp(true);
    }

    /**
     * 玩家升级了
     * @param takeexp
     */
    public void levelUp(boolean takeexp) {
        try {
            int maxhp = stats.getMaxHp();
            int maxmp = stats.getMaxMp();
            if (GameConstants.is新手职业(job)) {
                maxhp += Randomizer.rand(12, 16);
                maxmp += Randomizer.rand(10, 12);
            } else if ((job >= 100) && (job <= 132)) {
                maxhp += Randomizer.rand(48, 52);
                maxmp += Randomizer.rand(4, 6);
            } else if ((job >= 200) && (job <= 232))  {
                maxhp += Randomizer.rand(10, 14);
                maxmp += Randomizer.rand(48, 52);
            }else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)))  {
                maxhp += Randomizer.rand(20, 24);
                maxmp += Randomizer.rand(14, 16);
            } else {
                maxhp += Randomizer.rand(24, 38);
                maxmp += Randomizer.rand(12, 24);
                if ((job != 800) && (job != 900) && (job != 910)) {
                    System.err.println(new StringBuilder().append("出现未处理的角色升级加血职业: ").append(job).toString());
                }
            }
            maxmp += stats.getTotalInt() / 10;
            if ((getSkillLevel(20040221) > 0)) {
                maxmp += Randomizer.rand(18, 22);
            }
            maxhp = Math.min(500000, Math.abs(maxhp));
            maxmp = Math.min(500000, Math.abs(maxmp));
            if (takeexp) {
                exp -= getExpNeededForLevel();
                if (exp < 0) {
                    exp = 0;
                }
            } else {
                setExp(0);
            }
            level += 1;
            if (level >= getMaxLevelForSever()) {
                setExp(0);
            }
            maxhp = Math.min(getMaxHpForSever(), Math.abs(maxhp));
            maxmp = Math.min(getMaxMpForSever(), Math.abs(maxmp));
            if ((level == 200) && (!isGM())) {
                StringBuilder sb = new StringBuilder("[祝贺] ");
                sb.append(getMedalText());
                sb.append(getName());
                sb.append("终于达到了200级.大家一起祝贺下吧。");
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverMessageNotice(sb.toString()));
            }
            Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
            statup.put(MapleStat.MAXHP, Long.valueOf(maxhp));
            statup.put(MapleStat.MAXMP, Long.valueOf(maxmp));
            statup.put(MapleStat.HP, Long.valueOf(maxhp));
            statup.put(MapleStat.MP, Long.valueOf(maxmp));
            statup.put(MapleStat.经验, exp);
            statup.put(MapleStat.等级, Long.valueOf(level));

            if ((isGM()) || (!GameConstants.is新手职业(job))) {
                remainingSp += 3;
            }
            remainingAp += 5;
            statup.put(MapleStat.AVAILABLEAP, Long.valueOf(remainingAp));
            statup.put(MapleStat.AVAILABLESP, Long.valueOf(remainingSp));
            stats.setInfo(maxhp, maxmp, maxhp, maxmp);
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, this));
            map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
            stats.recalcLocalStats(this);
            silentPartyUpdate();
            guildUpdate();
            sidekickUpdate();
            checkNewQuest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 升级后需要把新的任务放到角色的任务表里面
     */
    public void checkNewQuest(){
        Collection<MapleQuest> allQuestInfo = MapleQuest.getInstatce().getAllInstances();

        for (MapleQuest info : allQuestInfo) {
            if (!this.quests.containsKey(info.getId())) {
                if (info.canStart(this,info.getNpcId())) {
                    MapleQuest.getInstance(info.getId()).forceStart(this,info.getNpcId(),info.getStartStatus());
                }
            }
        }
    }

    public boolean isValidJob(int id) {
        return MapleCarnivalChallenge.getJobNameByIdNull(id) != null;
    }

    public void changeKeybinding(int key, byte type, int action) {
        if (type != 0) {
            this.keylayout.Layout().put(key, new Pair(type, action));
        } else {
            this.keylayout.Layout().remove(key);
        }
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    public MapleStorage getStorage() {
        return this.storage;
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.add(mo);
        } finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.remove(mo);
        } finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        this.visibleMapObjectsLock.readLock().lock();
        try {
            boolean bool = this.visibleMapObjects.contains(mo);
            return bool;
        } finally {
            this.visibleMapObjectsLock.readLock().unlock();
        }
    }

    public Collection<MapleMapObject> getAndWriteLockVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().lock();
        return this.visibleMapObjects;
    }

    public void unlockWriteVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().unlock();
    }

    public boolean isAlive() {
        return stats.getHp() > 0;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer().allowedToTarget(this)) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
        }
    }

    public void equipChanged() {
        if (this.map == null) {
            return;
        }
        this.map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        this.stats.recalcLocalStats(this);
        if (getMessenger() != null) {
            WorldMessengerService.getInstance().updateMessenger(getMessenger().getId(), getName(), this.client.getChannel());
        }
    }

    public void checkCopyItems() {
        List<Integer> equipOnlyIds = new ArrayList<>();
        Map checkItems = new HashMap();

        for (Item item : getInventory(MapleInventoryType.EQUIP).list()) {
            int equipOnlyId = item.getEquipOnlyId();
            if (equipOnlyId > 0) {
                if (checkItems.containsKey(equipOnlyId)) {
                    if (((Integer) checkItems.get(equipOnlyId)) == item.getItemId()) {
                        equipOnlyIds.add(equipOnlyId);
                    }
                } else {
                    checkItems.put(equipOnlyId, item.getItemId());
                }
            }
        }

        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            int equipOnlyId = item.getEquipOnlyId();
            if (equipOnlyId > 0) {
                if (checkItems.containsKey(equipOnlyId)) {
                    if (((Integer) checkItems.get(equipOnlyId)) == item.getItemId()) {
                        equipOnlyIds.add(equipOnlyId);
                    }
                } else {
                    checkItems.put(equipOnlyId, item.getItemId());
                }
            }
        }

        boolean autoban = false;
        for (Integer equipOnlyId : equipOnlyIds) {
            MapleInventoryManipulator.removeAllByEquipOnlyId(this.client, equipOnlyId);
            autoban = true;
        }
        if (autoban) {
            AutobanManager.getInstance().autoban(this.client, "无理由.");
        }
        checkItems.clear();
        equipOnlyIds.clear();
    }

    public List<MaplePet> getPets() {
        List ret = new ArrayList();
        for (Item item : getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getPet() != null) {
                ret.add(item.getPet());
            }
        }
        return ret;
    }

    public MaplePet getSpawnPets() {
        return this.spawnPets;
    }

    public MaplePet getSpawnPet() {
        return this.spawnPets;
    }


    public List<MaplePet> getSummonedPets() {
        List ret = new ArrayList();
        if ((this.spawnPets != null) && (this.spawnPets.getSummoned())) {
            ret.add(this.spawnPets);
        }
        return ret;
    }

    public void addSpawnPet(MaplePet pet) {
        if (this.spawnPets == null) {
            this.spawnPets = pet;
            pet.setSummoned(1);
            return;
        }
    }

    public void removeSpawnPet(MaplePet pet, boolean shiftLeft) {
        if ((this.spawnPets == null) || (this.spawnPets.getUniqueId() != pet.getUniqueId())) {
            return ;
        }
        this.spawnPets = null;
    }

    public void unequipAllSpawnPets() {
        if (this.spawnPets != null) {
            unequipSpawnPet(this.spawnPets, true, false);
        }
    }


    public void spawnPet(byte slot, boolean lead) {
        spawnPet(slot, lead, true);
    }

    /**
     * 召唤宠物
     * @param slot
     * @param lead
     * @param broadcast
     */
    public void spawnPet(byte slot, boolean lead, boolean broadcast) {
        Item item = getInventory(MapleInventoryType.CASH).getItem((short) slot);
        if ((item == null) || (!ItemConstants.isPet(item.getItemId()))) {
            dropMessage(1, "召唤失败，请你重新登陆");
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MaplePet pet = item.getPet();
        if ((pet == null) || ((item.getExpiration() != -1L) && (item.getExpiration() <= System.currentTimeMillis()))) {
            dropMessage(1, "召唤失败，找不到宠物");
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        //if ( pet != null) {
        //    unequipSpawnPet(pet, true, false);
        //} else {
            Point pos = getPosition();
            pet.setPos(pos);
            try {
                pet.setFh(getMap().getFootholds().findBelow(pos).getId());
            } catch (NullPointerException e) {
                pet.setFh(0);
            }
            pet.setStance(0);
            pet.setSummoned(1); //let summoned be true..
            pet.setCanPickup(true);
            addSpawnPet(pet);
            if (getMap() != null) {
//                client.getSession().write(PetPacket.updatePet(pet, getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
                getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, false), true);
//                client.getSession().write(PetPacket.loadExceptionList(this, pet));
//                client.getSession().write(PetPacket.petStatUpdate(this));
            }
        //}
        client.getSession().write(MaplePacketCreator.enableActions());
    }

    public void unequipSpawnPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
        if (getSpawnPet() != null) {
            getSpawnPet().setSummoned(0);
            getSpawnPet().saveToDb();
        }
        this.client.getSession().write(PetPacket.updatePet(pet, getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
        if (this.map != null) {
            this.map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
        }
        removeSpawnPet(pet, shiftLeft);

        this.client.getSession().write(MaplePacketCreator.enableActions());
    }



    public long getLastFameTime() {
        return this.lastfametime;
    }

    public List<Integer> getFamedCharacters() {
        return this.lastmonthfameids;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (this.lastfametime >= System.currentTimeMillis() - 86400000L) {
            return FameStatus.NOT_TODAY;
        }
        if ((from == null) || (this.lastmonthfameids == null) || (this.lastmonthfameids.contains(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }

    public void hasGivenFame(MapleCharacter to) {
        this.lastfametime = System.currentTimeMillis();
        this.lastmonthfameids.add(to.getId());
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
                ps.setInt(1, getId());
                ps.setInt(2, to.getId());
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("ERROR writing famelog for char ").append(getName()).append(" to ").append(to.getName()).append(e).toString());
        }
    }


    public MapleKeyLayout getKeyLayout() {
        return this.keylayout;
    }

    public MapleQuickSlot getQuickSlot() {
        return this.quickslot;
    }

    public MapleParty getParty() {
        if (this.party == null) {
            return null;
        }
        if (this.party.isDisbanded()) {
            this.party = null;
        }
        return this.party;
    }

    public byte getWorld() {
        return this.world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public EventInstanceManager getEventInstance() {
        return this.eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        this.doors.add(door);
    }

    public void clearDoors() {
        this.doors.clear();
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList(this.doors);
    }

    public void addMechDoor(MechDoor door) {
        this.mechDoors.add(door);
    }

    public void clearMechDoors() {
        this.mechDoors.clear();
    }

    public List<MechDoor> getMechDoors() {
        return new ArrayList(this.mechDoors);
    }

    public void setSmega() {
        if (this.smega) {
            this.smega = false;
            dropMessage(5, "You have set megaphone to disabled mode");
        } else {
            this.smega = true;
            dropMessage(5, "You have set megaphone to enabled mode");
        }
    }

    public boolean getSmega() {
        return this.smega;
    }

    public int getChair() {
        return this.chair;
    }

    public int getItemEffect() {
        return this.itemEffect;
    }

    public int getTitleEffect() {
        return this.titleEffect;
    }

    public void setChair(int chair) {
        this.chair = chair;
        this.stats.relocHeal(this);
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setTitleEffect(int titleEffect) {
        this.titleEffect = titleEffect;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public int getCurrentRep() {
        return this.currentrep;
    }

    public int getTotalRep() {
        return this.totalrep;
    }

    public int getTotalWins() {
        return this.totalWins;
    }

    public int getTotalLosses() {
        return this.totalLosses;
    }

    public void increaseTotalWins() {
        this.totalWins += 1;
    }

    public void increaseTotalLosses() {
        this.totalLosses += 1;
    }

    public int getGuildId() {
        return this.guildid;
    }

    public byte getGuildRank() {
        return this.guildrank;
    }

    public int getGuildContribution() {
        return this.guildContribution;
    }

    public void setGuildId(int newGuildId) {
        this.guildid = newGuildId;
        if (this.guildid > 0) {
            if (this.mgc == null) {
                this.mgc = new MapleGuildCharacter(this);
            } else {
                this.mgc.setGuildId(this.guildid);
            }
        } else {
            this.mgc = null;
            this.guildContribution = 0;
        }
    }

    public void setGuildRank(byte newRank) {
        this.guildrank = newRank;
        if (this.mgc != null) {
            this.mgc.setGuildRank(newRank);
        }
    }

    public void setGuildContribution(int newContribution) {
        this.guildContribution = newContribution;
        if (this.mgc != null) {
            this.mgc.setGuildContribution(newContribution);
        }
    }

    public MapleGuildCharacter getMGC() {
        return this.mgc;
    }

    public void setAllianceRank(byte newRank) {
        this.allianceRank = newRank;
        if (this.mgc != null) {
            this.mgc.setAllianceRank(newRank);
        }
    }

    public byte getAllianceRank() {
        return this.allianceRank;
    }

    public MapleGuild getGuild() {
        if (getGuildId() <= 0) {
            return null;
        }
        return WorldGuildService.getInstance().getGuild(getGuildId());
    }

    public void setJob(int jobId) {
        this.job = (short) jobId;
    }

    public void sidekickUpdate() {
        if (this.sidekick == null) {
            return;
        }
        this.sidekick.getCharacter(this.sidekick.getCharacter(0).getId() == getId() ? 0 : 1).update(this);
        if (!MapleSidekick.checkLevels(getLevel(), this.sidekick.getCharacter(this.sidekick.getCharacter(0).getId() == getId() ? 1 : 0).getLevel())) {
            this.sidekick.eraseToDB();
        }
    }

    public void guildUpdate() {
        if (this.guildid <= 0) {
            return;
        }
        this.mgc.setLevel(this.level);
        this.mgc.setJobId(this.job);
        WorldGuildService.getInstance().memberLevelJobUpdate(this.mgc);
    }

    public void saveGuildStatus() {
        MapleGuild.setOfflineGuildStatus(this.guildid, this.guildrank, this.guildContribution, this.allianceRank, this.id);
    }

    public void modifyCSPoints(int type, int quantity) {
        modifyCSPoints(type, quantity, false);
    }

    public boolean modifyCSPoints(int type, int quantity, boolean show) {
        switch (type) {
            case 1:
                if (this.acash + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "你没有足够的点卷.");
                    }
                    //ban(new StringBuilder().append(getName()).append(" 点券数量为负").toString(), false, true, false);
                    return false;
                }
                this.acash += quantity;
                break;
            case 2:
                if (this.maplepoints + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "你没有足够的抵用卷.");
                    }
                    //ban(new StringBuilder().append(getName()).append(" 抵用卷数量为负").toString(), false, true, false);
                    return false;
                }
                this.maplepoints += quantity;
                break;
        }

        if ((show) && (quantity != 0)) {
            dropMessage(0, new StringBuilder().append("你").append(quantity > 0 ? "获得了 " : "消耗了 ").append(Math.abs(quantity)).append(type == 1 ? " 点券。" : " 抵用券。").toString());
        }

        this.client.getSession().write(MaplePacketCreator.showCharCash(this));
        return true;
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return this.acash;
            case 2:
                return this.maplepoints;
            case -1:
                return this.acash + this.maplepoints;
            case 0:
        }
        return 0;
    }

    public boolean hasEquipped(int itemid) {
        return this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        int possesed = this.inventory[type.ordinal()].countById(itemid);
        if ((checkEquipped) && (type == MapleInventoryType.EQUIP)) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= quantity;
        }
        return possesed == quantity;
    }

    public boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, true, true);
    }

    public boolean haveItem(int itemid) {
        return haveItem(itemid, 1, true, true);
    }

    public int getItemQuantity(int itemid) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        return getInventory(type).countById(itemid);
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = this.inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public int getEquipId(byte slot) {
        MapleInventory equip = getInventory(MapleInventoryType.EQUIP);
        return equip.getItem((short) slot).getItemId();
    }

    public int getUseId(byte slot) {
        MapleInventory use = getInventory(MapleInventoryType.USE);
        return use.getItem((short) slot).getItemId();
    }

    public int getSetupId(byte slot) {
        MapleInventory setup = getInventory(MapleInventoryType.SETUP);
        return setup.getItem((short) slot).getItemId();
    }

    public int getCashId(byte slot) {
        MapleInventory cash = getInventory(MapleInventoryType.CASH);
        return cash.getItem((short) slot).getItemId();
    }

    public int getEtcId(byte slot) {
        MapleInventory etc = getInventory(MapleInventoryType.ETC);
        return etc.getItem((short) slot).getItemId();
    }

    public byte getBuddyCapacity() {
        return this.buddylist.getCapacity();
    }

    public void setBuddyCapacity(byte capacity) {
        this.buddylist.setCapacity(capacity);
        this.client.getSession().write(BuddyListPacket.updateBuddyCapacity(capacity));
    }

    public MapleMessenger getMessenger() {
        return this.messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public Map<Integer,MapleSummon> getSummonsReadLock() {
        this.summonsLock.readLock().lock();
        return this.summons;
    }

    public Map<Integer,MapleSummon> getSummons() {
        return this.summons;
    }

    public int getSummonsSize() {
        return this.summons.size();
    }

    public void unlockSummonsReadLock() {
        this.summonsLock.readLock().unlock();
    }

    public void addSummon(MapleSummon s) {
        this.summonsLock.writeLock().lock();
        try {
            this.summons.put(s.getSkillId(),s);
        } finally {
            this.summonsLock.writeLock().unlock();
        }
    }

    public void removeSummon(Integer summonID) {
        this.summonsLock.writeLock().lock();
        try {
            this.summons.remove(summonID);
        } finally {
            this.summonsLock.writeLock().unlock();
        }
    }

    public void addCooldown(int skillId, long startTime, long length) {
        if (isShowPacket()) {
            dropMessage(-10, "服务器管理员消除技能冷却时间(原时间:" + length / 1000 + "秒)");
            client.getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
        } else {
            this.coolDowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length));
        }
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
    }

    public boolean skillisCooling(int skillId) {
        return this.coolDowns.containsKey(skillId);
    }

    public void giveCoolDowns(int skillid, long starttime, long length) {
        addCooldown(skillid, starttime, length);
    }

    public void giveCoolDowns(List<MapleCoolDownValueHolder> cooldowns) {
        if (cooldowns != null) {
            for (MapleCoolDownValueHolder cooldown : cooldowns) {
                this.coolDowns.put(cooldown.skillId, cooldown);
            }
        } else {
            try {
                Connection con = DatabaseConnection.getConnection();
                ResultSet rs;
                try (PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?")) {
                    ps.setInt(1, getId());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0L) {
                            continue;
                        }
                        giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                    }
                    ps.close();
                }
                rs.close();
                deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
            } catch (SQLException e) {
                System.err.println("Error while retriving cooldown from SQL storage");
            }
        }
    }

    public int getCooldownSize() {
        return this.coolDowns.size();
    }

    public List<MapleCoolDownValueHolder> getCooldowns() {
        List ret = new ArrayList();
        for (MapleCoolDownValueHolder mc : this.coolDowns.values()) {
            if (mc != null) {
                ret.add(mc);
            }
        }
        return ret;
    }

    public void 减少冷却时间(int skillId, int time) {
        if (coolDowns.containsKey(skillId)) {
            long 冷却时间 = coolDowns.get(skillId).length;
            冷却时间 -= time * 1000;
            if (冷却时间 <= 0) {
                removeCooldown(skillId);
                client.getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
            } else {
                coolDowns.get(skillId).length = 冷却时间;
                client.getSession().write(MaplePacketCreator.skillCooldown(skillId, (int) (冷却时间 / 1000)));
            }
        }
    }

    public List<MapleDiseaseValueHolder> getAllDiseases() {
        return new ArrayList(this.diseases.values());
    }

    public boolean hasDisease(MapleDisease dis) {
        return this.diseases.containsKey(dis);
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        giveDebuff(disease, skill.getX(), skill.getDuration(), skill.getSkillId(), skill.getSkillLevel());
    }

    public void giveDebuff(MapleDisease disease, int x, long duration, int skillid, int level) {
        if ((this.map != null) && (!hasDisease(disease)) && Randomizer.nextInt(100) > Math.min(this.getStat().getAsrR(), 99)) {
            if (disease != MapleDisease.昏迷
                    && (getBuffSource(MapleBuffStat.增加物理防御) == 9001003 || getBuffSource(MapleBuffStat.魔法防御力) == 9001003
                    || getBuffSource(MapleBuffStat.回避率) == 9001003 || getBuffSource(MapleBuffStat.神圣之火_最大体力百分比) == 9001003
                    || getBuffSource(MapleBuffStat.神圣之火_最大魔力百分比) == 9001003 || getBuffSource(MapleBuffStat.移动速度) == 9001003
                    || getBuffSource(MapleBuffStat.攻击力增加) == 9001003 || getBuffSource(MapleBuffStat.魔法攻击力增加) == 9001003)) {
                return;
            }

            if ((this.stats.ASR > 0) && (Randomizer.nextInt(100) < this.stats.ASR)) {
                return;
            }
            this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration - this.stats.decreaseDebuff));
            this.client.getSession().write(BuffPacket.giveDebuff(disease, x, skillid, level, (int) duration));
            this.map.broadcastMessage(this, BuffPacket.giveForeignDebuff(this.id, disease, skillid, level, x), false);
            if ((x > 0) && (disease == MapleDisease.中毒)) {
                addHP((int) (-(x * ((duration - this.stats.decreaseDebuff) / 1000L))));
            }
        }
    }

    public void giveSilentDebuff(List<MapleDiseaseValueHolder> ld) {
        if (ld != null) {
            for (MapleDiseaseValueHolder disease : ld) {
                this.diseases.put(disease.disease, disease);
            }
        }
    }

    public boolean dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            this.client.getSession().write(BuffPacket.cancelDebuff(debuff));
            this.map.broadcastMessage(this, BuffPacket.cancelForeignDebuff(this.id, debuff), false);
            this.diseases.remove(debuff);
            return true;
        }
        return false;
    }

    public boolean dispelDebuffs() {
        List<MapleDisease> diseasess = new ArrayList<>(this.diseases.keySet());
        boolean success = false;
        for (MapleDisease d : diseasess) {
            success = success || dispelDebuff(d);
        }
        return success;
    }

    public void cancelAllDebuffs() {
        this.diseases.clear();
    }

    public int getDiseaseSize() {
        return this.diseases.size();
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public void sendNote(String to, String msg) {
        sendNote(to, msg, 0);
    }

    public void sendNote(String to, String msg, int fame) {
        MapleCharacterUtil.sendNote(to, getName(), msg, fame);
    }

    public void showNote() {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", 1005, 1008)) {
                ps.setString(1, getName());
                ResultSet rs = ps.executeQuery();
                rs.last();
                int count = rs.getRow();
                rs.first();
                this.client.getSession().write(MTSCSPacket.showNotes(rs, count));
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Unable to show note").append(e).toString());
        }
    }

    public void deleteNote(int id, int fame) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Unable to delete note").append(e).toString());
        }
    }

    public int getMulungEnergy() {
        return this.mulung_energy;
    }

    public void mulung_EnergyModify(boolean inc) {
        if (inc) {
            if (this.mulung_energy + 100 > 10000) {
                this.mulung_energy = 10000;
            } else {
                this.mulung_energy = (short) (this.mulung_energy + 100);
            }
        } else {
            this.mulung_energy = 0;
        }
        this.client.getSession().write(MaplePacketCreator.MulungEnergy(this.mulung_energy));
    }

    public void writeMulungEnergy() {
        this.client.getSession().write(MaplePacketCreator.MulungEnergy(this.mulung_energy));
    }

    public void writeEnergy(String type, String inc) {
        this.client.getSession().write(MaplePacketCreator.sendPyramidEnergy(type, inc));
    }

    public void writeStatus(String type, String inc) {
        this.client.getSession().write(MaplePacketCreator.sendGhostStatus(type, inc));
    }

    public void writePoint(String type, String inc) {
        this.client.getSession().write(MaplePacketCreator.sendGhostPoint(type, inc));
    }

    public int getAranCombo() {
        return this.aranCombo;
    }

    public void gainAranCombo(int count, boolean show) {
        int oldCombo = this.aranCombo;
        oldCombo += count;
        if (oldCombo < 0) {
            oldCombo = 0;
        }
        this.aranCombo = Math.min(30000, oldCombo);
        if (show) {
            this.client.getSession().write(MaplePacketCreator.ShowAranCombo(this.aranCombo));
        }
    }

    public long getLastComboTime() {
        return this.lastComboTime;
    }

    public void setLastComboTime(long time) {
        this.lastComboTime = time;
    }

    public long getKeyDownSkill_Time() {
        return this.keydown_skill;
    }

    public void setKeyDownSkill_Time(long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }

    public void checkBerserk() {
        if ((this.job != 132) || (this.lastBerserkTime < 0L) || (this.lastBerserkTime + 10000L > System.currentTimeMillis())) {
            return;
        }
        int skillId = 1320016;
        Skill BerserkX = SkillFactory.getSkill(skillId);
        int skilllevel = getTotalSkillLevel(BerserkX);
        if ((skilllevel >= 1) && (this.map != null)) {
            this.lastBerserkTime = System.currentTimeMillis();
            MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            this.stats.Berserk = (this.stats.getHp() * 100 / this.stats.getCurrentMaxHp() >= ampStat.getX());
            this.client.getSession().write(MaplePacketCreator.showOwnBuffEffect(skillId, 1, getLevel(), skilllevel, (byte) (this.stats.Berserk ? 1 : 0)));
            this.map.broadcastMessage(this, MaplePacketCreator.showBuffeffect(this, skillId, 1, getLevel(), skilllevel, (byte) (this.stats.Berserk ? 1 : 0)), false);
        } else {
            this.lastBerserkTime = -1L;
        }
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
        if (this.map != null) {
            this.map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
        }
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleMount getMount() {
        return this.mount;
    }

    public int[] getWishlist() {
        return this.wishlist;
    }

    public void clearWishlist() {
        for (int i = 0; i < 12; i++) {
            this.wishlist[i] = 0;
        }
        this.changed_wishlist = true;
    }

    public int getWishlistSize() {
        int ret = 0;
        for (int i = 0; i < 12; i++) {
            if (this.wishlist[i] > 0) {
                ret++;
            }
        }
        return ret;
    }

    public void setWishlist(int[] wl) {
        this.wishlist = wl;
        this.changed_wishlist = true;
    }

    public int[] getRegRocks() {
        return this.regrocks;
    }

    public int getRegRockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRegRocks(int map) {
        this.changed_trocklocations = true;
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] == map) {
                this.regrocks[i] = 999999999;
                break;
            }
        }
        // 重新排列下
        int[] tmp = new int[5];
        int j = 0;
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] != 999999999) {
                tmp[j] = this.regrocks[i];
                j++;
            }
        }
        // 补上
        if (j <4) {
            for (;j<=4;j++) {
                tmp[j] = 999999999;
            }
        }
        this.regrocks = tmp;
    }

    public void addRegRockMap() {
        if (getRegRockSize() >= 5) {
            return;
        }
        this.regrocks[getRegRockSize()] = getMapId();
        this.changed_trocklocations = true;
    }

    public boolean isRegRockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public List<LifeMovementFragment> getLastRes() {
        return this.lastres;
    }

    public void setLastRes(List<LifeMovementFragment> lastres) {
        this.lastres = lastres;
    }

    public void dropMessage(int type, String message) {
        if (type == -1) {
            this.client.getSession().write(MaplePacketCreator.serverMessageTop(message));
        } else if (type == -2) {
            this.client.getSession().write(PlayerShopPacket.shopChat(message, 0));
        } else if (type == -3) {
            this.client.getSession().write(MaplePacketCreator.getChatText(getId(), message, isSuperGM(), 0));
        } else if (type == -4) {
            this.client.getSession().write(MaplePacketCreator.getChatText(getId(), message, isSuperGM(), 1));
        } else if (type == -5) {
            this.client.getSession().write(MaplePacketCreator.spouseMessage(message, false));
        } else if (type == -6) {
            this.client.getSession().write(MaplePacketCreator.spouseMessage(message, true));
        } else if (type == -7) {
            this.client.getSession().write(UIPacket.getMidMsg(message, false, 0));
        } else if (type == -8) {
            this.client.getSession().write(UIPacket.getMidMsg(message, true, 0));
        } else if (type == -10) {
            this.client.getSession().write(MaplePacketCreator.getFollowMessage(message));
        } else if (type == -11) {
            this.client.getSession().write(MaplePacketCreator.yellowChat(message));
        } else {
            this.client.getSession().write(MaplePacketCreator.serverMessageRedText(message));
        }
    }

    public void dropSpouseMessage(int type, String message) {
        this.client.getSession().write(MaplePacketCreator.serverNotice(5, message));
    }

    public IMaplePlayerShop getPlayerShop() {
        return this.playerShop;
    }

    public void setPlayerShop(IMaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public int getConversation() {
        return inst.get();
    }

    public void setConversation(int inst) {
        this.inst.set(inst);
    }

    public int getDirection() {
        return insd.get();
    }

    public void setDirection(int inst) {
        this.insd.set(inst);
    }

    public MapleCarnivalParty getCarnivalParty() {
        return this.carnivalParty;
    }

    public void setCarnivalParty(MapleCarnivalParty party) {
        this.carnivalParty = party;
    }

    public void addCP(int ammount) {
        this.totalCP = (short) (this.totalCP + ammount);
        this.availableCP = (short) (this.availableCP + ammount);
    }

    public void useCP(int ammount) {
        this.availableCP = (short) (this.availableCP - ammount);
    }

    public int getAvailableCP() {
        return this.availableCP;
    }

    public int getTotalCP() {
        return this.totalCP;
    }

    public void resetCP() {
        this.totalCP = 0;
        this.availableCP = 0;
    }

    public void addCarnivalRequest(MapleCarnivalChallenge request) {
        this.pendingCarnivalRequests.add(request);
    }

    public MapleCarnivalChallenge getNextCarnivalRequest() {
        return (MapleCarnivalChallenge) this.pendingCarnivalRequests.pollLast();
    }

    public void clearCarnivalRequests() {
        this.pendingCarnivalRequests = new LinkedList();
    }

    public void setAchievementFinished(int id) {
        if (!this.finishedAchievements.contains(id)) {
            this.finishedAchievements.add(id);
            this.changed_achievements = true;
        }
    }

    public boolean achievementFinished(int achievementid) {
        return this.finishedAchievements.contains(achievementid);
    }

    public List<Integer> getFinishedAchievements() {
        return this.finishedAchievements;
    }

    public boolean getCanTalk() {
        return this.canTalk;
    }

    public void canTalk(boolean talk) {
        this.canTalk = talk;
    }

    public double getEXPMod() {
        return hasEXPCard();
    }

    public double hasEXPCard() {
        ArrayList<Integer> expCards = ItemConstants.get经验值卡();
        MapleInventory iv = getInventory(MapleInventoryType.ETC);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        double canuse = 1.0D;
        for (int i : expCards) {
            if (iv.countById(i) <= 0 || !ii.isExpOrDropCardTime(i) || (iv.findById(i) != null && iv.findById(i).getExpiration() == -1L && !isIntern())) {
                if (iv.findById(i) != null && iv.findById(i).getExpiration() == -1L && !isIntern()) {
                    dropMessage(5, ii.getName(i) + "属性错误，经验值加成无效。");
                }
                continue;
            }
            canuse = 2.0D;
        }
        return canuse;
    }

    public int getDropMod() {
        return hasDropCard();
    }

    public int hasDropCard() {
        int[] dropCards = {5360000, 5360014, 5360015, 5360016};
        MapleInventory iv = getInventory(MapleInventoryType.CASH);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int[] arr$ = dropCards;
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$++) {
            Integer id3 = arr$[i$];
            if ((iv.countById(id3) > 0)
                    && (ii.isExpOrDropCardTime(id3))) {
                return 2;
            }
        }

        return 1;
    }

    public int getCashMod() {
        return this.stats.cashMod;
    }

    public void setPoints(int p) {
        this.points = p;
    }

    public int getPoints() {
        return this.points;
    }

    public void setVPoints(int p) {
        this.vpoints = p;
    }

    public int getVPoints() {
        return this.vpoints;
    }

    public CashShop getCashInventory() {
        return this.cs;
    }

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(this.client, ItemConstants.getInventoryType(id), id, quantity, true, false);
        this.client.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) (-quantity), true));
    }

    public void removeAll(int id) {
        removeAll(id, true, false);
    }

    public void removeAll(int itemId, boolean show, boolean checkEquipped) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        int possessed = getInventory(type).countById(itemId);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
            if (show) {
                getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) (-possessed), true));
            }
        }
        if ((checkEquipped) && (type == MapleInventoryType.EQUIP)) {
            type = MapleInventoryType.EQUIPPED;
            possessed = getInventory(type).countById(itemId);
            if (possessed > 0) {
                MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
                if (show) {
                    getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) (-possessed), true));
                }
                equipChanged();
            }
        }
    }

    public void removeItem(int itemId) {
        removeItem(itemId, false);
    }

    public void removeItem(int itemId, boolean show) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (type == MapleInventoryType.EQUIP) {
            type = MapleInventoryType.EQUIPPED;
            int possessed = getInventory(type).countById(itemId);
            if (possessed > 0) {
                MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
                if (show) {
                    getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) (-possessed), true));
                }
                equipChanged();
            }
        }
    }

    public MapleRing getMarriageRing() {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        MapleRing mrings = null;
        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if (item.getRing() != null) {
                MapleRing ring = item.getRing();
                ring.setEquipped(true);
                if ((mrings == null) && (ItemConstants.is结婚戒指(item.getItemId()))) {
                    mrings = ring;
                }
            }
        }
        if (mrings == null) {
            iv = getInventory(MapleInventoryType.EQUIP);
            for (Item ite : iv.list()) {
                Equip item = (Equip) ite;
                if (item.getRing() != null) {
                    MapleRing ring = item.getRing();
                    ring.setEquipped(false);
                    if ((mrings == null) && (ItemConstants.is结婚戒指(item.getItemId()))) {
                        mrings = ring;
                    }
                }
            }
        }
        return mrings;
    }

    public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        List crings = new ArrayList();
        List frings = new ArrayList();
        List mrings = new ArrayList();

        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if ((item.getRing() != null) && (ItemConstants.isEffectRing(item.getItemId()))) {
                MapleRing ring = item.getRing();
                ring.setEquipped(true);
                if (equip) {
                    if (ItemConstants.is恋人戒指(item.getItemId())) {
                        crings.add(ring);
                    } else if (ItemConstants.is好友戒指(item.getItemId())) {
                        frings.add(ring);
                    } else if (ItemConstants.is结婚戒指(item.getItemId())) {
                        mrings.add(ring);
                    }
                } else if ((crings.isEmpty()) && (ItemConstants.is恋人戒指(item.getItemId()))) {
                    crings.add(ring);
                } else if ((frings.isEmpty()) && (ItemConstants.is好友戒指(item.getItemId()))) {
                    frings.add(ring);
                } else if ((mrings.isEmpty()) && (ItemConstants.is结婚戒指(item.getItemId()))) {
                    mrings.add(ring);
                }
            }
        }

        if (equip) {
            iv = getInventory(MapleInventoryType.EQUIP);
            for (Item ite : iv.list()) {
                Equip item = (Equip) ite;
                if ((item.getRing() != null) && (ItemConstants.isEffectRing(item.getItemId()))) {
                    MapleRing ring = item.getRing();
                    ring.setEquipped(false);
                    if (ItemConstants.is恋人戒指(item.getItemId())) {
                        crings.add(ring);
                    } else if (ItemConstants.is好友戒指(item.getItemId())) {
                        frings.add(ring);
                    } else if (ItemConstants.is结婚戒指(item.getItemId())) {
                        mrings.add(ring);
                    }
                }
            }
        }
        Collections.sort(frings, new MapleRing.RingComparator());
        Collections.sort(crings, new MapleRing.RingComparator());
        Collections.sort(mrings, new MapleRing.RingComparator());
        return new Triple(crings, frings, mrings);
    }

    public int getFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(getTruePosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public void startFairySchedule(boolean exp) {
        startFairySchedule(exp, false);
    }

    public void startFairySchedule(boolean exp, boolean equipped) {
        cancelFairySchedule((exp) || (this.stats.equippedFairy == 0));
        if (this.fairyExp <= 0) {
            this.fairyExp = (byte) this.stats.equippedFairy;
        }
        if ((equipped) && (this.fairyExp < this.stats.equippedFairy * 3) && (this.stats.equippedFairy > 0)) {
            dropMessage(5, new StringBuilder().append("您装备了精灵吊坠在1小时后经验获取将增加到 ").append(this.fairyExp + this.stats.equippedFairy).append(" %.").toString());
        }
        this.lastFairyTime = System.currentTimeMillis();
    }

    public boolean canFairy(long now) {
        return (this.lastFairyTime > 0L) && (this.lastFairyTime + 3600000L < now);
    }

    public boolean canHP(long now) {
        if (this.lastHPTime + 5000L < now) {
            this.lastHPTime = now;
            return true;
        }
        return false;
    }

    public boolean canMP(long now) {
        if (this.lastMPTime + 5000L < now) {
            this.lastMPTime = now;
            return true;
        }
        return false;
    }

    public boolean canHPRecover(long now) {
        if ((this.stats.hpRecoverTime > 0) && (this.lastHPTime + this.stats.hpRecoverTime < now)) {
            this.lastHPTime = now;
            return true;
        }
        return false;
    }

    public boolean canMPRecover(long now) {
        if ((this.stats.mpRecoverTime > 0) && (this.lastMPTime + this.stats.mpRecoverTime < now)) {
            this.lastMPTime = now;
            return true;
        }
        return false;
    }

    public void cancelFairySchedule(boolean exp) {
        this.lastFairyTime = 0L;
        if (exp) {
            this.fairyExp = 0;
        }
    }

    public void doFairy() {
        if ((this.fairyExp < this.stats.equippedFairy * 3) && (this.stats.equippedFairy > 0)) {
            this.fairyExp = (byte) (this.fairyExp + this.stats.equippedFairy);
            dropMessage(5, new StringBuilder().append("精灵吊坠经验获取增加到 ").append(this.fairyExp).append(" %.").toString());
        }
        if (getGuildId() > 0) {
            WorldGuildService.getInstance().gainGP(getGuildId(), 20, this.id);
        }
        startFairySchedule(false, true);
    }

    public byte getFairyExp() {
        return this.fairyExp;
    }

    public int getTeam() {
        return this.coconutteam;
    }

    public void setTeam(int v) {
        this.coconutteam = v;
    }

    public Map<Integer, Integer> getAllLinkMid() {
        return this.linkMobs;
    }

    public void setLinkMid(int lm, int x) {
        this.linkMobs.put(lm, x);
    }

    public int getDamageIncrease(int lm) {
        if (this.linkMobs.containsKey(lm)) {
            return (this.linkMobs.get(lm));
        }
        return 0;
    }

    public MapleExtractor getExtractor() {
        return this.extractor;
    }

    public void setExtractor(MapleExtractor me) {
        removeExtractor();
        this.extractor = me;
    }

    public void removeExtractor() {
        if (this.extractor != null) {
            this.map.broadcastMessage(MaplePacketCreator.removeExtractor(this.id));
            this.map.removeMapObject(this.extractor);
            this.extractor = null;
        }
    }

    public void spawnSavedPets() {
        if (this.petStore > -1) {
            spawnPet(this.petStore, false, false);
        }
        this.petStore = -1;
    }

    public byte getPetStores() {
        return this.petStore;
    }

    public byte getSubcategory() {
        return this.subcategory;
    }

    public void setSubcategory(int z) {
        this.subcategory = (byte) z;
    }

    public int itemQuantity(int itemid) {
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void setRPS(RockPaperScissors rps) {
        this.rps = rps;
    }

    public RockPaperScissors getRPS() {
        return this.rps;
    }

    public long getNextConsume() {
        return this.nextConsume;
    }

    public void setNextConsume(long nc) {
        this.nextConsume = nc;
    }

    public int getRank() {
        return this.rank;
    }

    public int getRankMove() {
        return this.rankMove;
    }

    public int getJobRank() {
        return this.jobRank;
    }

    public int getJobRankMove() {
        return this.jobRankMove;
    }

    public void changeChannel(int channel) {
        ChannelServer toch = ChannelServer.getInstance(channel);
        if ((channel == this.client.getChannel()) || (toch == null) || (toch.isShutdown())) {
            this.client.getSession().write(MaplePacketCreator.serverBlocked(1));
            return;
        }
        changeRemoval();
        ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
        if (getMessenger() != null) {
            WorldMessengerService.getInstance().silentLeaveMessenger(getMessenger().getId(), new MapleMessengerCharacter(this));
        }
        PlayerBuffStorage.addBuffsToStorage(getId(), getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(getId(), getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(getId(), getAllDiseases());
//        this.cancelAllBuffs();
        World.ChannelChange_Data(new CharacterTransfer(this), getId(), channel);
        ch.removePlayer(this);
        this.client.updateLoginState(3, this.client.getSessionIPAddress());
        String s = this.client.getSessionIPAddress();
        LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
        this.client.getSession().write(MaplePacketCreator.getChannelChange(this.client, Integer.parseInt(toch.getIP().split(":")[1])));
        saveToDB(false, false);
        getMap().removePlayer(this);
        this.client.setPlayer(null);
        this.client.setReceiving(false);
    }

    public void expandInventory(byte type, int amount) {
        MapleInventory inv = getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) amount);
        this.client.getSession().write(InventoryPacket.updateInventorySlotLimit(type, inv.getSlotLimit()));
    }

    public boolean allowedToTarget(MapleCharacter other) {
        return (other != null) && ((!other.isHidden()) || (getGMLevel() >= other.getGMLevel()));
    }

    public int getFollowId() {
        return this.followid;
    }

    public void setFollowId(int fi) {
        this.followid = fi;
        if (fi == 0) {
            this.followinitiator = false;
            this.followon = false;
        }
    }

    public void setFollowInitiator(boolean fi) {
        this.followinitiator = fi;
    }

    public void setFollowOn(boolean fi) {
        this.followon = fi;
    }

    public boolean isFollowOn() {
        return this.followon;
    }

    public boolean isFollowInitiator() {
        return this.followinitiator;
    }

    public int getMarriageId() {
        return this.marriageId;
    }

    public void setMarriageId(int mi) {
        this.marriageId = mi;
    }

    public int getMarriageItemId() {
        return this.marriageItemId;
    }

    public void setMarriageItemId(int mi) {
        this.marriageItemId = mi;
    }

    public boolean isStaff() {
        return this.gmLevel >= PlayerGMRank.INTERN.getLevel();
    }

    public boolean isDonator() {
        return this.gmLevel >= PlayerGMRank.DONATOR.getLevel();
    }

    public boolean startPartyQuest(int questid) {
        boolean ret = false;
        MapleQuest q = MapleQuest.getInstance(questid);
        if ((q == null) || (!q.isPartyQuest())) {
            return false;
        }
        if ((!this.quests.containsKey(q)) || (!this.questinfo.containsKey(questid))) {
            MapleQuestStatus status = getQuestNAdd(q);
            status.setStatus((byte) 1);
            updateQuest(status);
            switch (questid) {
                case 1300:
                case 1301:
                case 1302:
                    break;
                case 1303:
                    break;
                case 1204:
                    break;
                case 1206:
                    break;
                default:
            }

            ret = true;
        }
        return ret;
    }

    public String getOneInfo(int questid, String key) {
        if ((!this.questinfo.containsKey(questid)) || (key == null) || (MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return null;
        }
        String[] split = ((String) this.questinfo.get(Integer.valueOf(questid))).split(";");
        for (String x : split) {
            String[] split2 = x.split("=");
            if ((split2.length == 2) && (split2[0].equals(key))) {
                return split2[1];
            }
        }
        return null;
    }

    public void updateOneInfo(int questid, String key, String value) {
        if ((!this.questinfo.containsKey(questid)) || (key == null) || (value == null) || (MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        String[] split = ((String) this.questinfo.get(Integer.valueOf(questid))).split(";");
        boolean changed = false;
        StringBuilder newQuest = new StringBuilder();
        for (String x : split) {
            String[] split2 = x.split("=");
            if (split2.length != 2) {
                continue;
            }
            if (split2[0].equals(key)) {
                newQuest.append(key).append("=").append(value);
            } else {
                newQuest.append(x);
            }
            newQuest.append(";");
            changed = true;
        }
    }

    public void recalcPartyQuestRank(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        if (!startPartyQuest(questid)) {
            String oldRank = getOneInfo(questid, "rank");
            if ((oldRank == null) || (oldRank.equals("S"))) {
                return;
            }
            String newRank;
            if (oldRank.equals("A")) {
                newRank = "S";
            } else {
                if (oldRank.equals("B")) {
                    newRank = "A";
                } else {
                    if (oldRank.equals("C")) {
                        newRank = "B";
                    } else {
                        if (oldRank.equals("D")) {
                            newRank = "C";
                        } else {
                            if (oldRank.equals("F")) {
                                newRank = "D";
                            } else {
                                return;
                            }
                        }
                    }
                }
            }
            List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            //List<Pair> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            if (questInfo == null) {
                return;
            }
            for (Pair q : questInfo) {
                boolean found = false;
                String val = getOneInfo(questid, (String) ((Pair) q.right).left);
                if (val == null) {
                    return;
                }
                int vall;
                try {
                    vall = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    return;
                }
                switch ((String) q.left) {
                    case "less":
                        found = vall < ((Integer) ((Pair) q.right).right);
                        break;
                    case "more":
                        found = vall > ((Integer) ((Pair) q.right).right);
                        break;
                    case "equal":
                        found = vall == ((Integer) ((Pair) q.right).right);
                        break;
                }
                if (!found) {
                    return;
                }
            }
            updateOneInfo(questid, "rank", newRank);
        }
    }

    public void tryPartyQuest(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        try {
            startPartyQuest(questid);
            this.pqStartTime = System.currentTimeMillis();
            updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(getOneInfo(questid, "try")) + 1));
        } catch (NumberFormatException e) {
            FileoutputUtil.log("tryPartyQuest error");
        }
    }

    public void endPartyQuest(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        try {
            startPartyQuest(questid);
            if (this.pqStartTime > 0L) {
                long changeTime = System.currentTimeMillis() - this.pqStartTime;
                int mins = (int) (changeTime / 1000L / 60L);
                int secs = (int) (changeTime / 1000L % 60L);
                int mins2 = Integer.parseInt(getOneInfo(questid, "min"));
                if ((mins2 <= 0) || (mins < mins2)) {
                    updateOneInfo(questid, "min", String.valueOf(mins));
                    updateOneInfo(questid, "sec", String.valueOf(secs));
                    updateOneInfo(questid, "date", FileoutputUtil.CurrentReadable_Date());
                }
                int newCmp = Integer.parseInt(getOneInfo(questid, "cmp")) + 1;
                updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                updateOneInfo(questid, "CR", String.valueOf((int) Math.ceil(newCmp * 100.0D / Integer.parseInt(getOneInfo(questid, "try")))));
                recalcPartyQuestRank(questid);
                this.pqStartTime = 0L;
            }
        } catch (Exception e) {
            FileoutputUtil.log("endPartyQuest error");
        }
    }

    public void havePartyQuest(int itemId) {
        int index = -1;
        int questid;
        switch (itemId) {
            case 1002798:
                questid = 1200;
                break;
            case 1072369:
                questid = 1201;
                break;
            case 1022073:
                questid = 1202;
                break;
            case 1082232:
                questid = 1203;
                break;
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574:
                questid = 1204;
                index = itemId - 1002571;
                break;
            case 1102226:
                questid = 1303;
                break;
            case 1102227:
                questid = 1303;
                index = 0;
                break;
            case 1122010:
                questid = 1205;
                break;
            case 1032060:
            case 1032061:
                questid = 1206;
                index = itemId - 1032060;
                break;
            case 3010018:
                questid = 1300;
                break;
            case 1122007:
                questid = 1301;
                break;
            case 1122058:
                questid = 1302;
                break;
            default:
                return;
        }
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        startPartyQuest(questid);
        updateOneInfo(questid, new StringBuilder().append("have").append(index == -1 ? "" : Integer.valueOf(index)).toString(), "1");
    }

    public void resetStatsByJob(boolean beginnerJob) {
        int baseJob = beginnerJob ? this.job % 1000 : this.job % 1000 / 100 * 100;
        boolean UA = getQuestNoAdd(MapleQuest.getInstance(111111)) != null;
        if (baseJob == 100) {
            resetStats(UA ? 4 : 35, 4, 4, 4);
        } else if (baseJob == 200) {
            resetStats(4, 4, UA ? 4 : 20, 4);
        } else if ((baseJob == 300) || (baseJob == 400)) {
            resetStats(4, UA ? 4 : 25, 4, 4);
        } else if (baseJob == 500) {
            resetStats(4, UA ? 4 : 20, 4, 4);
        } else if (baseJob == 0) {
            resetStats(4, 4, 4, 4);
        }
    }

    public boolean hasSummon() {
        return this.hasSummon;
    }

    public void setHasSummon(boolean summ) {
        this.hasSummon = summ;
    }

    public void removeDoor() {
        MapleDoor door = (MapleDoor) getDoors().iterator().next();
        for (MapleCharacter chr : door.getTarget().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (MapleCharacter chr : door.getTown().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (MapleDoor destroyDoor : getDoors()) {
            door.getTarget().removeMapObject(destroyDoor);
            door.getTown().removeMapObject(destroyDoor);
        }
        clearDoors();
    }

    public void removeMechDoor() {
        for (MechDoor destroyDoor : getMechDoors()) {
            for (MapleCharacter chr : getMap().getCharactersThreadsafe()) {
                destroyDoor.sendDestroyData(chr.getClient());
            }
            getMap().removeMapObject(destroyDoor);
        }
        clearMechDoors();
    }

    public void changeRemoval() {
        changeRemoval(false);
    }

    public void changeRemoval(boolean dc) {
        removeFamiliar();
        dispelSummons();
        if ((this.playerShop != null) && (!dc)) {
            this.playerShop.removeVisitor(this);
            if (this.playerShop.isOwner(this)) {
                this.playerShop.setOpen(true);
            }
        }
        if (!getDoors().isEmpty()) {
            removeDoor();
        }
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        NPCScriptManager.getInstance().dispose(this.client);
        cancelFairySchedule(false);
    }

    public String getTeleportName() {
        return this.teleportname;
    }

    public void setTeleportName(String tname) {
        this.teleportname = tname;
    }

    public int getGachExp() {
        return this.gachexp;
    }

    public void setGachExp(int ge) {
        this.gachexp = ge;
    }

    public boolean isInBlockedMap() {
        if ((!isAlive())|| (getMap().getSquadByMap() != null) || (getEventInstance() != null) || (getMap().getEMByMap() != null)) {
            return true;
        }
        if (((getMapId() >= 680000210) && (getMapId() <= 680000502)) || ((getMapId() / 10000 == 92502) && (getMapId() >= 925020100)) || (getMapId() / 10000 == 92503) || (getMapId() == 180000001)) {
            return true;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTownMap() {
        if ((hasBlockedInventory()) || (!getMap().isTown()) || (FieldLimitType.VipRock.check(getMap().getFieldLimit())) || (getEventInstance() != null)) {
            return false;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBlockedInventory() {
        FileoutputUtil.log("getConversation："+getConversation()+"||getDirection："+getDirection());
        return (!isAlive()) || (getTrade() != null) || (getConversation() > 0) || (getDirection() > 0) || (getPlayerShop() != null)|| (this.map == null);
    }

    public void startPartySearch(List<Integer> jobs, int maxLevel, int minLevel, int membersNeeded) {
        for (MapleCharacter chr : this.map.getCharacters()) {
            if ((chr.getId() != this.id) && (chr.getParty() == null) && (chr.getLevel() >= minLevel) && (chr.getLevel() <= maxLevel) && ((jobs.isEmpty()) || (jobs.contains((int) chr.getJob()))) && ((isGM()) || (!chr.isGM()))) {
                if ((this.party == null) || (this.party.getMembers().size() >= 6) || (this.party.getMembers().size() >= membersNeeded)) {
                    break;
                }
                chr.setParty(this.party);
                WrodlPartyService.getInstance().updateParty(this.party.getId(), PartyOperation.加入队伍, new MaplePartyCharacter(chr));
                chr.receivePartyMemberHP();
                chr.updatePartyMemberHP();
            }
        }
    }

    public Battler getBattler(int pos) {
        return this.battlers[pos];
    }

    public Battler[] getBattlers() {
        return this.battlers;
    }

    public List<Battler> getBoxed() {
        return this.boxed;
    }

    public int countBattlers() {
        int ret = 0;
        for (Battler battler : this.battlers) {
            if (battler != null) {
                ret++;
            }
        }
        return ret;
    }

    public void changedBattler() {
        this.changed_pokemon = true;
    }

    public void makeBattler(int index, int monsterId) {
        MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(monsterId);
        this.battlers[index] = new Battler(mons);
        this.battlers[index].setCharacterId(this.id);
        this.changed_pokemon = true;
    }

    public boolean removeBattler(int ind) {
        if (countBattlers() <= 1) {
            return false;
        }
        if (ind == this.battlers.length) {
            this.battlers[ind] = null;
        } else {
            for (int i = ind; i < this.battlers.length; i++) {
                this.battlers[i] = (i + 1 == this.battlers.length ? null : this.battlers[(i + 1)]);
            }
        }
        this.changed_pokemon = true;
        return true;
    }

    public int getChallenge() {
        return this.challenge;
    }

    public void setChallenge(int c) {
        this.challenge = c;
    }

    public short getFatigue() {
        return this.fatigue;
    }

    public void setFatigue(int j) {
        this.fatigue = (short) Math.min(Math.max(0, j), 200);
    }

    /**
     * 复活宠物刷新下背包数据就行了
     * @param item
     */
    public void refreshItem(Item item) {
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        client.getSession().write(InventoryPacket.addItemToInventory(item));
    }

    public void fakeRelog() {
        this.client.getSession().write(MaplePacketCreator.getCharInfo(this));
        MapleMap mapp = getMap();
        mapp.setCheckStates(false);
        mapp.removePlayer(this);
        mapp.addPlayer(this);
        mapp.setCheckStates(true);
        if (isShowPacket()) {
            client.getSession().write(MaplePacketCreator.serverNotice(5, "刷新人物数据完成..."));
        }
    }

    public boolean canSummon() {
        return canSummon(5000);
    }

    public boolean canSummon(int g) {
        if (this.lastSummonTime + g < System.currentTimeMillis()) {
            this.lastSummonTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public int getIntNoRecord(int questID) {
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(questID));
        if ((stat == null) || (stat.getCustomData() == null)) {
            return 0;
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public int getIntRecord(int questID) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public void updatePetAuto() {
        this.client.getSession().write(MaplePacketCreator.petAutoHP(getIntRecord(122221)));
        this.client.getSession().write(MaplePacketCreator.petAutoMP(getIntRecord(122223)));
        this.client.getSession().write(MaplePacketCreator.petAutoBuff(getIntRecord(122224)));
    }

    public void sendEnglishQuiz(String msg) {
        this.client.getSession().write(MaplePacketCreator.englishQuizMsg(msg));
    }

    public void setChangeTime(boolean changeMap) {
        this.mapChangeTime = System.currentTimeMillis();
    }

    public long getChangeTime() {
        return this.mapChangeTime;
    }

    public short getScrolledPosition() {
        return this.scrolledPosition;
    }

    public void setScrolledPosition(short s) {
        this.scrolledPosition = s;
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(this, 9270035);
    }

    public boolean checkHearts() {
        return getInventory(MapleInventoryType.EQUIPPED).getItem((short) -35) != null;
    }

    public void setSidekick(MapleSidekick s) {
        this.sidekick = s;
    }

    public MapleSidekick getSidekick() {
        return this.sidekick;
    }

    public Map<Integer, MonsterFamiliar> getFamiliars() {
        return this.familiars;
    }

    public MonsterFamiliar getSummonedFamiliar() {
        return this.summonedFamiliar;
    }

    public void removeFamiliar() {
        if ((this.summonedFamiliar != null) && (this.map != null)) {
            removeVisibleFamiliar();
        }
        this.summonedFamiliar = null;
    }

    public void removeVisibleFamiliar() {
        getMap().removeMapObject(this.summonedFamiliar);
        removeVisibleMapObject(this.summonedFamiliar);
        getMap().broadcastMessage(MaplePacketCreator.removeFamiliar(getId()));
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        cancelEffect(ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive), false, System.currentTimeMillis());
    }

    public void spawnFamiliar(MonsterFamiliar mf) {
        this.summonedFamiliar = mf;
        mf.setStance(0);
        mf.setPosition(getPosition());
        mf.setFh(getFH());
        addVisibleMapObject(mf);
        getMap().spawnFamiliar(mf);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive);
        if ((eff != null) && (eff.getInterval() <= 0) && (eff.makeChanceResult())) {
            eff.applyTo(this);
        }
        this.lastFamiliarEffectTime = System.currentTimeMillis();
    }

    public boolean canFamiliarEffect(long now, MapleStatEffect eff) {
        return (this.lastFamiliarEffectTime > 0L) && (this.lastFamiliarEffectTime + eff.getInterval() < now);
    }

    public void doFamiliarSchedule(long now) {
        for (MonsterFamiliar mf : this.familiars.values()) {
            if ((this.summonedFamiliar != null) && (this.summonedFamiliar.getId() == mf.getId())) {
                mf.addFatigue(this, 5);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive);
                if ((eff != null) && (eff.getInterval() > 0) && (canFamiliarEffect(now, eff)) && (eff.makeChanceResult())) {
                    eff.applyTo(this);
                }
            } else if (mf.getFatigue() > 0) {
                mf.setFatigue(Math.max(0, mf.getFatigue() - 5));
            }
        }
    }

    public MapleImp[] getImps() {
        return this.imps;
    }

    public void sendImp() {
        for (int i = 0; i < this.imps.length; i++) {
            if (this.imps[i] != null) {
                this.client.getSession().write(MaplePacketCreator.updateImp(this.imps[i], ImpFlag.SUMMONED.getValue(), i, true));
            }
        }
    }

    public int getBattlePoints() {
        return 0;
    }

    public int getTotalBattleExp() {
        return 0;
    }

    public void changeTeam(int newTeam) {
        this.coconutteam = newTeam;
        this.client.getSession().write(MaplePacketCreator.showEquipEffect(newTeam));
    }

    public void disease(int type, int level) {
        if (MapleDisease.getBySkill(type) == null) {
            return;
        }
        this.chair = 0;
        this.client.getSession().write(MaplePacketCreator.cancelChair(-1, id));
        this.map.broadcastMessage(this, MaplePacketCreator.showChair(this.id, 0), false);
        giveDebuff(MapleDisease.getBySkill(type), MobSkillFactory.getInstance().getMobSkill(type, level));
    }

    public void clearAllCooldowns() {
        for (MapleCoolDownValueHolder m : getCooldowns()) {
            int skil = m.skillId;
            removeCooldown(skil);
            this.client.getSession().write(MaplePacketCreator.skillCooldown(skil, 0));
        }
    }

    public Pair<Double, Boolean> modifyDamageTaken(double damage, MapleMapObject attacke) {
        Pair ret = new Pair(Double.valueOf(damage), Boolean.valueOf(false));
        if (damage < 0.0D) {
            return ret;
        }
        if ((this.stats.ignoreDAMr > 0) && (Randomizer.nextInt(100) < this.stats.ignoreDAMr_rate)) {
            damage -= Math.floor(this.stats.ignoreDAMr * damage / 100.0D);
        }
        if ((this.stats.ignoreDAM > 0) && (Randomizer.nextInt(100) < this.stats.ignoreDAM_rate)) {
            damage -= this.stats.ignoreDAM;
        }
        Integer div = getBuffedValue(MapleBuffStat.祝福护甲);
        if (div != null) {
            if (div <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.祝福护甲);
            } else {
                setBuffedValue(MapleBuffStat.祝福护甲, div - 1);
                damage = 0.0D;
            }
        }
        List attack = ((attacke instanceof MapleMonster)) || (attacke == null) ? null : new ArrayList();
        if (damage > 0.0D) {
            if ((getJob() == 122) && (!skillisCooling(1210016))) {
                Skill divine = SkillFactory.getSkill(1210016);
                if (getTotalSkillLevel(divine) > 0) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        divineShield.applyTo(this);
                        this.client.getSession().write(MaplePacketCreator.skillCooldown(1210016, divineShield.getCooldown(this)));
                        addCooldown(1210016, System.currentTimeMillis(), divineShield.getCooldown(this) * 1000);
                    }
                }
            } else if ((getJob() == 433) || (getJob() == 434)) {
                Skill divine = SkillFactory.getSkill(4330001);
                if ((getTotalSkillLevel(divine) > 0) && (getBuffedValue(MapleBuffStat.隐身术) == null) && (!skillisCooling(divine.getId()))) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (Randomizer.nextInt(100) < divineShield.getX()) {
                        divineShield.applyTo(this);
                    }
                }
            } else if (((getJob() == 512) || (getJob() == 522) || (getJob() == 582) || (getJob() == 592) || (getJob() == 572)) && (getBuffedValue(MapleBuffStat.反制攻击) == null)) {
                Skill divine = SkillFactory.getSkill(getJob() == 522 ? 5220012 : getJob() == 512 ? 5120011 : 5720012);
                if ((getTotalSkillLevel(divine) > 0) && (!skillisCooling(divine.getId()))) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        divineShield.applyTo(this);
                        this.client.getSession().write(MaplePacketCreator.skillCooldown(divine.getId(), divineShield.getX()));
                        addCooldown(divine.getId(), System.currentTimeMillis(), divineShield.getX() * 1000);
                    }
                }
            } else if (((getJob() == 531) || (getJob() == 532)) && (attacke != null)) {
                Skill divine = SkillFactory.getSkill(5310009);
                if (getTotalSkillLevel(divine) > 0) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        if ((attacke instanceof MapleMonster)) {
                            MapleMonster attacker = (MapleMonster) attacke;
                            int theDmg = (int) (divineShield.getDamage() * getStat().getCurrentMaxBaseDamage() / 100.0D);
                            attacker.damage(this, theDmg, true);
                            getMap().broadcastMessage(MobPacket.damageMonster(attacker.getObjectId(), theDmg));
                        } else {
                            MapleCharacter attacker = (MapleCharacter) attacke;
                            attacker.addHP(-divineShield.getDamage());
                            attack.add(divineShield.getDamage());
                        }
                    }
                }
            } else if ((getJob() == 132) && (attacke != null)) {
                Skill divine = SkillFactory.getSkill(1320011);
                if ((getTotalSkillLevel(divine) > 0) && (!skillisCooling(divine.getId())) && (getBuffSource(MapleBuffStat.灵魂助力) == 1301013)) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        this.client.getSession().write(MaplePacketCreator.skillCooldown(divine.getId(), divineShield.getCooldown(this)));
                        addCooldown(divine.getId(), System.currentTimeMillis(), divineShield.getCooldown(this) * 1000);
                        if ((attacke instanceof MapleMonster)) {
                            MapleMonster attacker = (MapleMonster) attacke;
                            int theDmg = (int) (divineShield.getDamage() * getStat().getCurrentMaxBaseDamage() / 100.0D);
                            attacker.damage(this, theDmg, true);
                            getMap().broadcastMessage(MobPacket.damageMonster(attacker.getObjectId(), theDmg));
                        } else {
                            MapleCharacter attacker = (MapleCharacter) attacke;
                            attacker.addHP(-divineShield.getDamage());
                            attack.add(divineShield.getDamage());
                        }
                    }
                }
            }
            if (attacke != null) {
                int damr = (Randomizer.nextInt(100) < getStat().DAMreflect_rate ? getStat().DAMreflect : 0) + (getBuffedValue(MapleBuffStat.伤害反击) != null ? getBuffedValue(MapleBuffStat.伤害反击) : 0);
                if (damr > 0) {
                    long bouncedamage = (long) (damage * damr / 100.0D);
                    if ((attacke instanceof MapleMonster)) {
                        MapleMonster attacker = (MapleMonster) attacke;
                        bouncedamage = Math.min(bouncedamage, attacker.getMobMaxHp() / 10L);
                        attacker.damage(this, bouncedamage, true);
                        getMap().broadcastMessage(this, MobPacket.damageMonster(attacker.getObjectId(), bouncedamage), getTruePosition());
                        if (getBuffSource(MapleBuffStat.伤害反击) == 31101003) {
                            MapleStatEffect eff = getStatForBuff(MapleBuffStat.伤害反击);
                            attacker.applyStatus(this, new MonsterStatusEffect(MonsterStatus.眩晕, 1, eff.getSourceId(), null, false), false, 5000L, true, eff);
                        }
                    } else {
                        MapleCharacter attacker = (MapleCharacter) attacke;
                        bouncedamage = Math.min(bouncedamage, attacker.getStat().getCurrentMaxHp() / 10);
                        attacker.addHP(-(int) bouncedamage);

                        attack.add((int) bouncedamage);
                        if (getBuffSource(MapleBuffStat.伤害反击) == 31101003) {
                            attacker.disease(MapleDisease.昏迷.getDisease(), 1);
                        }
                    }
                    ret.right = true;
                }
                if (((getJob() == 411) || (getJob() == 412) || (getJob() == 421) || (getJob() == 422) || (getJob() == 1411) || (getJob() == 1412)) && (getBuffedValue(MapleBuffStat.召唤兽) != null)) {
                    Map<Integer,MapleSummon> ss = getSummonsReadLock();
                    try {
                        for (MapleSummon sum : ss.values()) {
                            if ((sum.getTruePosition().distanceSq(getTruePosition()) < 400000.0D) && ((sum.getSkillId() == 4111007) || (sum.getSkillId() == 4211007) || (sum.getSkillId() == 14111010) || sum.getSkillId() == 36121013 || sum.getSkillId() == 36121014 || sum.getSkillId() == 36121002)) {
                                if ((attacke instanceof MapleMonster)) {
                                    List allDamageNumbers = new ArrayList();
                                    List allDamage = new ArrayList();
                                    MapleMonster attacker = (MapleMonster) attacke;
                                    int theDmg = (int) (SkillFactory.getSkill(sum.getSkillId()).getEffect(sum.getSkillLevel()).getX() * damage / 100.0D);
                                    allDamageNumbers.add(new Pair(theDmg, false));
                                    allDamage.add(new AttackPair(attacker.getObjectId(), allDamageNumbers));
                                    getMap().broadcastMessage(SummonPacket.summonAttack(sum.getOwnerId(), sum.getObjectId(), (byte) -124, (byte) 17, allDamage, getLevel(), true));
                                    attacker.damage(this, theDmg, true);
                                    checkMonsterAggro(attacker);
                                    if (!attacker.isAlive()) {
                                        getClient().getSession().write(MobPacket.killMonster(attacker.getObjectId(), 1));
                                    }
                                } else {
                                    MapleCharacter chr = (MapleCharacter) attacke;
                                    int dmg = SkillFactory.getSkill(sum.getSkillId()).getEffect(sum.getSkillLevel()).getX();
                                    chr.addHP(-dmg);
                                    attack.add(dmg);
                                }
                            }
                        }
                    } finally {
                        unlockSummonsReadLock();
                    }
                }
            }
        } else if ((damage == 0.0D)
                && ((getJob() == 433) || (getJob() == 434)) && (attacke != null)) {
            Skill divine = SkillFactory.getSkill(4330009);
            if (getTotalSkillLevel(divine) > 0) {
                MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                int prop = getTotalSkillLevel(divine) + 10;
                if (Randomizer.nextInt(100) < prop) {
                    divineShield.applyTo(this);
                }
            }
        }

        ret.left = damage;
        return ret;
    }

    public void onAttack(long maxhp, int maxmp, int skillid, int oid, int totDamage) {
        if ((this.stats.hpRecoverProp > 0)
                && (Randomizer.nextInt(100) <= this.stats.hpRecoverProp)) {
            if (this.stats.hpRecover > 0) {
                healHP(this.stats.hpRecover);
            }
            if (this.stats.hpRecoverPercent > 0) {
                addHP((int) Math.min(maxhp, Math.min((int) (totDamage * (this.stats.hpRecoverPercent / 100.0D)), this.stats.getMaxHp() / 2)));
            }
        }

        if ((this.stats.mpRecoverProp > 0)
                && (Randomizer.nextInt(100) <= this.stats.mpRecoverProp)
                && (this.stats.mpRecover > 0)) {
            healMP(this.stats.mpRecover);
        }

        if ((getJob() == 212) || (getJob() == 222) || (getJob() == 232)) {
            int[] skillIds = {2120010, 2220010, 2320011};
            for (int i : skillIds) {
                Skill skill = SkillFactory.getSkill(i);
                if (getTotalSkillLevel(skill) > 0) {
                    MapleStatEffect venomEffect = skill.getEffect(getTotalSkillLevel(skill));
                    if (ServerProperties.ShowPacket()) {
                        FileoutputUtil.log(new StringBuilder().append("神秘瞄准术: ").append(skill.getId()).append(" - ").append(skill.getName()).append(" getAllLinkMid ").append(getAllLinkMid().size()).append(" Y ").append(venomEffect.getY()).toString());
                    }
                    if ((!venomEffect.makeChanceResult()) || (getAllLinkMid().size() > venomEffect.getY())) {
                        break;
                    }
                    if (getAllLinkMid().size() < venomEffect.getY()) {
                        setLinkMid(oid, venomEffect.getX());
                    }
                    venomEffect.applyTo(this);
                    if (!ServerProperties.ShowPacket()) {
                        break;
                    }
                    break;
                }
            }
        }
    }

    public void afterAttack(int mobCount, int attackCount, int skillid) {

        if (getBuffedValue(MapleBuffStat.斗气集中) != null) {
            handleOrbgain();
        }

        if (!isIntern()) {
            Skill skill = SkillFactory.getSkill(4330001);
            if (skill != null) {
                MapleStatEffect eff = skill.getEffect(this.getTotalSkillLevel(skill));
                if (eff == null || !eff.makeChanceResult()) {
                    cancelEffectFromBuffStat(MapleBuffStat.隐身术);
                }
            }
        }
    }

    public void applyIceGage(int x) {
    }

    public Rectangle getBounds() {
        return new Rectangle(getTruePosition().x - 25, getTruePosition().y - 75, 50, 75);
    }

    public boolean getCygnusBless() {
        int jobid = getJob();

        return ((getSkillLevel(12) > 0) && (jobid >= 0) && (jobid < 1000)) || ((getSkillLevel(10000012) > 0) && (jobid >= 1000) && (jobid < 2000)) || ((getSkillLevel(20000012) > 0) && ((jobid == 2000) || ((jobid >= 2100) && (jobid <= 2112)))) || ((getSkillLevel(20010012) > 0) && ((jobid == 2001) || ((jobid >= 2200) && (jobid <= 2218)))) || ((getSkillLevel(20020012) > 0) && ((jobid == 2002) || ((jobid >= 2300) && (jobid <= 2312)))) || ((getSkillLevel(20030012) > 0) && ((jobid == 2003) || ((jobid >= 2400) && (jobid <= 2412)))) || ((getSkillLevel(20040012) > 0) && ((jobid == 2004) || ((jobid >= 2700) && (jobid <= 2712)))) || ((getSkillLevel(30000012) > 0) && ((jobid == 3000) || ((jobid >= 3200) && (jobid <= 3512)))) || ((getSkillLevel(30010012) > 0) ) || ((getSkillLevel(30020012) > 0) && ((jobid == 3002) || ((jobid >= 3600) && (jobid <= 3612)))) || ((getSkillLevel(50000012) > 0) && ((jobid == 5000) || ((jobid >= 5100) && (jobid <= 5112)))) || ((getSkillLevel(60000012) > 0) && ((jobid == 6000) || ((jobid >= 6100) && (jobid <= 6112)))) || ((getSkillLevel(60010012) > 0) && ((jobid == 6001) || ((jobid >= 6500) && (jobid <= 6512)))) || ((getSkillLevel(100000012) > 0) && ((jobid == 10000) || ((jobid >= 10100) && (jobid <= 10112))));
    }

    public byte get精灵祝福() {
        int jobid = getJob();
        if (((getSkillLevel(20021110) > 0) && ((jobid == 2002) || ((jobid >= 2300) && (jobid <= 2312)))) || (getSkillLevel(80001040) > 0)) {
            return 10;
        }
        return 0;
    }

    public void handleForceGain(int oid, int skillid) {
        handleForceGain(oid, skillid, 0);
    }

    public void handleForceGain(int moboid, int skillid, int extraForce) {
        if (extraForce <= 0) {
            return;
        }
        int forceGain = 1;
        if ((getLevel() >= 30) && (getLevel() < 70)) {
            forceGain = 2;
        } else if ((getLevel() >= 70) && (getLevel() < 120)) {
            forceGain = 3;
        } else if (getLevel() >= 120) {
            forceGain = 4;
        }
        this.forceCounter += 1;
        addMP(extraForce > 0 ? extraForce : forceGain);
        getClient().getSession().write(SkillPacket.showForce(this, moboid, this.forceCounter, forceGain));
        if ((this.stats.mpRecoverProp > 0) && (extraForce <= 0)
                && (Randomizer.nextInt(100) <= this.stats.mpRecoverProp)) {
            this.forceCounter += 1;
            addMP(this.stats.mpRecover);
            getClient().getSession().write(SkillPacket.showForce(this, moboid, this.forceCounter, this.stats.mpRecoverProp));
        }
    }

    public void gainForce(int moboid, int forceColor, int skillid) {
        Skill effect = SkillFactory.getSkill(31110009);
        int maxFuryLevel = getSkillLevel(effect);
        this.forceCounter += 1;
        if (Randomizer.nextInt(100) >= 60) {
            addMP(forceColor);
        }
        getClient().getSession().write(SkillPacket.showForce(this, moboid, this.forceCounter, forceColor));
        if ((maxFuryLevel > 0) && ((skillid == 31000004) || (skillid == 31001006) || (skillid == 31001007) || (skillid == 31001008))) {
            this.forceCounter += 1;
            int rand = Randomizer.nextInt(100);
            if (rand >= 40) {
                addMP(forceColor);
                getClient().getSession().write(SkillPacket.showForce(this, moboid, this.forceCounter, 2));
            } else {
                getClient().getSession().write(SkillPacket.showForce(this, moboid, this.forceCounter, 3));
            }
        }
    }

    public int getCardStack() {
        return this.cardStack;
    }

    public int getCarteByJob() {
        if (getSkillLevel(20031210) > 0) {
            return 40;
        }
        if (getSkillLevel(20031209) > 0) {
            return 20;
        }
        return 0;
    }

    public void setCardStack(int amount) {
        this.cardStack = amount;
    }

    public void handleCarteGain(int oid) {
        int[] skillIds = {24120002, 24100003};
        for (int i : skillIds) {
            Skill skill = SkillFactory.getSkill(i);
            if (getSkillLevel(skill) > 0) {
                MapleStatEffect effect = skill.getEffect(getSkillLevel(skill));
                if ((!effect.makeChanceResult()) || (Randomizer.nextInt(100) > 50)) {
                    break;
                }
                this.forceCounter += 1;
                getClient().getSession().write(SkillPacket.gainCardStack(this, oid, skill.getId(), this.forceCounter, skill.getId() == 24120002 ? 2 : 1));
                if (getCardStack() >= getCarteByJob()) {
                    break;
                }
                this.cardStack += 1;
                getClient().getSession().write(SkillPacket.updateCardStack(this.cardStack));
                break;
            }
        }
    }

    /**
     * 金钱炸弹
     * @param attack
     * @param delay
     */
    public void handleMesosbomb(AttackInfo attack, int delay) { //处理金钱炸弹
        MapleMapItem mapitem;
        List<MapleMapObject> mesos = new ArrayList();
        List<MapleMapObject> items = this.getMap().getItemsInRange(this.getPosition(), 70000.0D);//地图上的物品
        for (MapleMapObject obj : items) {
            mapitem = (MapleMapItem) obj;
            if (mapitem.getMeso() > 0 && mapitem.getOwner() == this.getId()) {
                mesos.add(mapitem);
                this.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 4, this.getId()), mapitem.getPosition());
                this.getMap().removeMapObject(obj); //标记
            }
        }
        if (mesos.size() > 0) {
            this.getMap().broadcastMessage(SkillPacket.MesosBomb(this, attack));
        }
        this.client.getSession().write(MaplePacketCreator.enableActions());
    }

    public void handleAssassinStack(MapleMonster mob, int visProjectile) {
        Skill skill_2 = SkillFactory.getSkill(4100011);
        Skill skill_4 = SkillFactory.getSkill(4120018);
        MapleStatEffect effect;
        boolean isAssassin;
        if (getSkillLevel(skill_4) > 0) {
            isAssassin = false;
            effect = skill_4.getEffect(getTotalSkillLevel(skill_4));
        } else {
            if (getSkillLevel(skill_2) > 0) {
                isAssassin = true;
                effect = skill_2.getEffect(getTotalSkillLevel(skill_2));
            } else {
                return;
            }
        }
        if ((effect != null) && (effect.makeChanceResult()) && (mob != null)) {
            int mobCount = effect.getMobCount();
            Rectangle bounds = effect.calculateBoundingBox(mob.getTruePosition(), mob.isFacingLeft());
            List<MapleMapObject> affected = this.map.getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.MONSTER}));
            List moboids = new ArrayList();
            for (MapleMapObject mo : affected) {
                if ((moboids.size() < mobCount) && (mo.getObjectId() != mob.getObjectId())) {
                    moboids.add(mo.getObjectId());
                }
            }
            mob.setmark(false);
            this.forceCounter += 1;
            send_other(SkillPacket.gainAssassinStack(getId(), mob.getObjectId(), this.forceCounter, isAssassin, moboids, visProjectile, mob.getTruePosition()), true);
            this.forceCounter += moboids.size();
        }
    }

    public void setDecorate(int id) {
        if (((id >= 1012276) && (id <= 1012280)) || (id == 1012361) || (id == 1012363) || (id == 1012455) || (id == 1012456) || (id == 1012457) || (id == 1012458)) {
            this.decorate = id;
        } else {
            this.decorate = 0;
        }
    }

    public int getDecorate() {
        return this.decorate;
    }

    public void checkTailAndEar() {
        if (!this.questinfo.containsKey(59300)) {
        }
    }

    public void updateCash() {
        this.client.getSession().write(MaplePacketCreator.showCharCash(this));
    }

    public short getSpace(int type) {
        return getInventory(MapleInventoryType.getByType((byte) type)).getNumFreeSlot();
    }

    public boolean haveSpace(int type) {
        short slot = getInventory(MapleInventoryType.getByType((byte) type)).getNextFreeSlot();
        return slot != -1;
    }

    public boolean haveSpaceForId(int itemid) {
        short slot = getInventory(ItemConstants.getInventoryType(itemid)).getNextFreeSlot();
        return slot != -1;
    }

    public boolean canHold() {
        for (int i = 1; i <= 5; i++) {
            if (getInventory(MapleInventoryType.getByType((byte) i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }

    public boolean canHoldSlots(int slot) {
        for (int i = 1; i <= 5; i++) {
            if (getInventory(MapleInventoryType.getByType((byte) i)).isFull(slot)) {
                return false;
            }
        }
        return true;
    }

    public boolean canHold(int itemid) {
        return getInventory(ItemConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public long getMerchantMeso() {
        long mesos = 0;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * from hiredmerch where characterid = ?")) {
                ps.setInt(1, this.id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    mesos = rs.getLong("Mesos");
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException se) {
            log.error("获取雇佣商店金币发生错误", se);
        }
        return mesos;
    }

    public int getHyPay(int type) {
        int pay = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("select * from hypay where accname = ?")) {
                ps.setString(1, getClient().getAccountName());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (type == 1) {
                        pay = rs.getInt("pay");
                    } else if (type == 2) {
                        pay = rs.getInt("payUsed");
                    } else if (type == 3) {
                        pay = rs.getInt("pay") + rs.getInt("payUsed");
                    } else if (type == 4) {
                        pay = rs.getInt("payReward");
                    } else {
                        pay = 0;
                    }
                } else {
                    try (PreparedStatement psu = con.prepareStatement("insert into hypay (accname, pay, payUsed, payReward) VALUES (?, ?, ?, ?)")) {
                        psu.setString(1, getClient().getAccountName());
                        psu.setInt(2, 0);
                        psu.setInt(3, 0);
                        psu.setInt(4, 0);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
                ps.close();
            }
            rs.close();
        } catch (SQLException ex) {
            log.error("获取充值信息发生错误", ex);
        }
        return pay;
    }

    public int addHyPay(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE hypay SET pay = ? ,payUsed = ? ,payReward = ? where accname = ?")) {
                ps.setInt(1, pay - hypay);
                ps.setInt(2, payUsed + hypay);
                ps.setInt(3, payReward + hypay);
                ps.setString(4, getClient().getAccountName());
                ps.executeUpdate();
                ps.close();
            }
            return 1;
        } catch (SQLException ex) {
            log.error("加减充值信息发生错误", ex);
        }
        return -1;
    }

    public int delPayReward(int pay) {
        int payReward = getHyPay(4);
        if (pay <= 0) {
            return -1;
        }
        if (pay > payReward) {
            return -1;
        }
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE hypay SET payReward = ? where accname = ?")) {
                ps.setInt(1, payReward - pay);
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                ps.close();
            }
            return 1;
        } catch (SQLException ex) {
            log.error("加减消费奖励信息发生错误", ex);
        }
        return -1;
    }

    public void sendPolice(int greason, String reason, int duration) {
        this.isbanned = true;
        WorldTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                MapleCharacter.this.client.disconnect(true, false);
            }
        }, duration);
    }

    public void sendPolice(String text) {
//         this.client.getSession().write(MaplePacketCreator.sendPolice(text));
//         this.isbanned = true;
//         WorldTimer.getInstance().schedule(new Runnable() {
//            public void run() {
//                 MapleCharacter.this.client.disconnect(true, false);
//                 if (MapleCharacter.this.client.getSession().isConnected())  {
//                    MapleCharacter.this.client.getSession().close(true);
//                }
//            }
//        }, 6000L);
    }

    public Timestamp getChrCreated() {
        if (this.createDate != null) {
            return this.createDate;
        }
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT createdate FROM characters WHERE id = ?")) {
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    return null;
                }
                this.createDate = rs.getTimestamp("createdate");
                rs.close();
                ps.close();
            }
            return this.createDate;
        } catch (SQLException e) {

            throw new DatabaseException("获取角色创建日期出错", e);
        }
    }

    public int getDollars() {
        return this.dollars;
    }

    public int getShareLots() {
        return this.shareLots;
    }

    public void addDollars(int n) {
        this.dollars += n;
    }

    public void addShareLots(int n) {
        this.shareLots += n;
    }

    public int getAPS() {
        return this.apstorage;
    }

    public void gainAPS(int aps) {
        this.apstorage += aps;
    }

    public void clearSkills() {
        Map<String, Integer> chrSkill = new HashMap(getSkills());
        Map newList = new HashMap();
        for (Map.Entry skill : chrSkill.entrySet()) {
            newList.put(skill.getKey(), new SkillEntry(0, (byte) 0, -1L));
        }
        changeSkillsLevel(newList);
        newList.clear();
        chrSkill.clear();
    }

    public Map<Integer, Byte> getSkillsWithPos() {
        Map<String, Integer> chrskills = new HashMap(getSkills());
        Map skillsWithPos = new LinkedHashMap();
        for (Map.Entry skill : chrskills.entrySet()) {
            if ((((SkillEntry) skill.getValue()).position >= 0) && (((SkillEntry) skill.getValue()).position < 13)) {
                skillsWithPos.put(((Skill) skill.getKey()).getId(), ((SkillEntry) skill.getValue()).position);
            }
        }
        return skillsWithPos;
    }

    public void handleKillSpreeGain() {
        Skill skill = SkillFactory.getSkill(4221013);
        int skillLevel = getSkillLevel(skill);
        if ((skillLevel <= 0)) {
            return;
        }
        if (Randomizer.nextInt(100) <= 20) {
            skill.getEffect(skillLevel).applyTo(this, true);
        }
    }

    public String getMedalText() {
        String medal = "";
        Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -26);
        if (medalItem != null) {
            medal = new StringBuilder().append("<").append(MapleItemInformationProvider.getInstance().getName(medalItem.getItemId())).append("> ").toString();
        }
        return medal;
    }

    public int getGamePoints() {
        try {
            int gamePoints = 0;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, getClient().getAccID());
                ps.setInt(2, getWorld());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    gamePoints = rs.getInt("gamePoints");
                    Timestamp updateTime = rs.getTimestamp("updateTime");
                    Calendar sqlcal = Calendar.getInstance();
                    if (updateTime != null) {
                        sqlcal.setTimeInMillis(updateTime.getTime());
                    }
                    if ((sqlcal.get(5) + 1 <= Calendar.getInstance().get(5)) || (sqlcal.get(2) + 1 <= Calendar.getInstance().get(2)) || (sqlcal.get(1) + 1 <= Calendar.getInstance().get(1))) {
                        gamePoints = 0;
                        try (PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePoints = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                            psu.setInt(1, getClient().getAccID());
                            psu.setInt(2, getWorld());
                            psu.executeUpdate();
                            psu.close();
                        }
                    }
                } else {
                    try (PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePoints) VALUES (?, ?, ?)")) {
                        psu.setInt(1, getClient().getAccID());
                        psu.setInt(2, getWorld());
                        psu.setInt(3, 0);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
                rs.close();
                ps.close();
            }
            return gamePoints;
        } catch (SQLException Ex) {
            log.error("获取角色帐号的在线时间点出现错误 - 数据库查询失败", Ex);
        }
        return -1;
    }

    public void gainGamePoints(int amount) {
        int gamePoints = getGamePoints() + amount;
        updateGamePoints(gamePoints);
    }

    public void resetGamePoints() {
        updateGamePoints(0);
    }

    public void updateGamePoints(int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePoints = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, amount);
                ps.setInt(2, getClient().getAccID());
                ps.setInt(3, getWorld());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException Ex) {
            log.error("更新角色帐号的在线时间出现错误 - 数据库更新失败.", Ex);
        }
    }

    public int getMaxLevelForSever() {
        return ServerProperties.getMaxLevel();
    }

    public int getMaxHpForSever() {
        return ServerProperties.getMaxHp();
    }

    public int getMaxMpForSever() {
        return ServerProperties.getMaxMp();
    }

    public int getMaxDamageOver(int skillId) {
        int maxDamage = 2147483647;

        int incMaxDamage = getStat().incMaxDamage;
        if (skillId != 0) {
            Skill skill = SkillFactory.getSkill(skillId);
            if (skill != null) {
                int skillMaxDamage = skill.getMaxDamageOver();
                if ((skillId == 4221014) && (getTotalSkillLevel(4220051) > 0)) {
                    skillMaxDamage = 2147483647;
                }
                return skillMaxDamage + incMaxDamage;
            }
        }
        return  maxDamage + incMaxDamage;
    }

    public int getLove() {
        return this.love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public void addLove(int loveChange) {
        this.love += loveChange;
        MessengerRankingWorker.getInstance().updateRankFromPlayer(this);
    }

    public long getLastLoveTime() {
        return this.lastlovetime;
    }

    public Map<Integer, Long> getLoveCharacters() {
        return this.lastdayloveids;
    }

    public int canGiveLove(MapleCharacter from) {
        if ((from == null) || (this.lastdayloveids == null)) {
            return 1;
        }
        if (this.lastdayloveids.containsKey(from.getId())) {
            long lastTime = (this.lastdayloveids.get(Integer.valueOf(from.getId())));
            if (lastTime >= System.currentTimeMillis() - 86400000L) {
                return 2;
            }
            return 0;
        }

        return 0;
    }

    public void hasGiveLove(MapleCharacter to) {
        this.lastlovetime = System.currentTimeMillis();
        if (this.lastdayloveids.containsKey(to.getId())) {
            this.lastdayloveids.remove(to.getId());
        }
        this.lastdayloveids.put(to.getId(), System.currentTimeMillis());
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO lovelog (characterid, characterid_to) VALUES (?, ?)")) {
                ps.setInt(1, getId());
                ps.setInt(2, to.getId());
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("ERROR writing lovelog for char ").append(getName()).append(" to ").append(to.getName()).append(e).toString());
        }
    }

    public long getExpNeededForLevel() {
        return GameConstants.getExpNeededForLevel(this.level);
    }

    public int getPlayerStats() {
        return getHpApUsed() + this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt() + getRemainingAp();
    }

    public int getMaxStats(boolean hpap) {
        int total = 29;

        if (!GameConstants.is新手职业(this.job)) {
            int jobSp = 0;
            jobSp += (hpap ? 15 : 0);
            total += jobSp;
        }

        total += this.level * 5;

        if (hpap) {
            total += getHpApUsed();
        } else {
            total -= getHpApUsed();
        }
        return total;
    }

    public boolean checkMaxStat() {
        if ((getGMLevel() > 0) || (getLevel() < 10)) {
            return false;
        }
        return getPlayerStats() > getMaxStats(true) + 15;
    }

    public void resetStats(int str, int dex, int int_, int luk) {
        resetStats(str, dex, int_, luk, false);
    }

    public void resetStats(int str, int dex, int int_, int luk, boolean resetAll) {
        Map stat = new EnumMap(MapleStat.class);
        int total = this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt() + getRemainingAp();
        if (resetAll) {
            total = getMaxStats(false);
        }
        total -= str;
        this.stats.str = (short) str;
        total -= dex;
        this.stats.dex = (short) dex;
        total -= int_;
        this.stats.int_ = (short) int_;
        total -= luk;
        this.stats.luk = (short) luk;

        setRemainingAp((short) total);
        this.stats.recalcLocalStats(this);
        stat.put(MapleStat.力量, (long) str);
        stat.put(MapleStat.敏捷, (long) dex);
        stat.put(MapleStat.智力, (long) int_);
        stat.put(MapleStat.运气, (long) luk);
        stat.put(MapleStat.AVAILABLEAP, (long) total);

        this.client.getSession().write(MaplePacketCreator.updatePlayerStats(stat, false, this));
    }

    public void SpReset() {
        Map<String, Integer> oldList = new HashMap(getSkills());
        Map newList = new HashMap();
        for (Map.Entry toRemove : oldList.entrySet()) {
            if ( (!((Skill) toRemove.getKey()).isSpecialSkill())) {
                int skillLevel = getSkillLevel((Skill) toRemove.getKey());
                if (skillLevel > 0) {
                    if (((Skill) toRemove.getKey()).canBeLearnedBy(getJob())) {
                        newList.put(toRemove.getKey(), new SkillEntry(0, ((SkillEntry) toRemove.getValue()).masterlevel, ((SkillEntry) toRemove.getValue()).expiration));
                    } else {
                        newList.put(toRemove.getKey(), new SkillEntry(0, (byte) 0, -1L));
                    }
                }
            }
        }
        if (!newList.isEmpty()) {
            changeSkillsLevel(newList);
        }
        oldList.clear();
        newList.clear();
        //int[] spToGive[10];
        int spToGive=0;
        spToGive += ((getJob() % 100 != 0) && (getJob() % 100 != 1) ? getJob() % 10 + 3 : 0);
        if (getJob() % 10 >= 2) {
            spToGive += 3;
        }
        spToGive += (getLevel() - 10) * 3;
        setRemainingSp(spToGive);
        updateSingleStat(MapleStat.AVAILABLESP, 0L);
        this.client.getSession().write(MaplePacketCreator.enableActions());
    }

    public int getPlayerPoints() {
        return this.playerPoints;
    }

    public void setPlayerPoints(int gain) {
        this.playerPoints = gain;
    }

    public void gainPlayerPoints(int gain) {
        if (this.playerPoints + gain < 0) {
            return;
        }
        this.playerPoints += gain;
    }

    public int getPlayerEnergy() {
        return this.playerEnergy;
    }

    public void setPlayerEnergy(int gain) {
        this.playerEnergy = gain;
    }

    public void gainPlayerEnergy(int gain) {
        if (this.playerEnergy + gain < 0) {
            return;
        }
        this.playerEnergy += gain;
    }

    public void dropTopMsg(String message) {
        this.client.getSession().write(UIPacket.getTopMsg(message));
    }

    public void dropMidMsg(String message) {
        this.client.getSession().write(UIPacket.clearMidMsg());
        this.client.getSession().write(UIPacket.getMidMsg(message, true, 0));
    }

    public void clearMidMsg() {
        this.client.getSession().write(UIPacket.clearMidMsg());
    }

    public int getEventCount(String eventId) {
        return getEventCount(eventId, 0);
    }

    public int getEventCount(String eventId, int type) {
        try {
            int count = 0;
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_event WHERE accId = ? AND eventId = ?")) {
                ps.setInt(1, getClient().getAccID());
                ps.setString(2, eventId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("count");
                    Timestamp updateTime = rs.getTimestamp("updateTime");
                    if (type == 0) {
                        Calendar sqlcal = Calendar.getInstance();
                        if (updateTime != null) {
                            sqlcal.setTimeInMillis(updateTime.getTime());
                        }
                        if ((sqlcal.get(5) + 1 <= Calendar.getInstance().get(5)) || (sqlcal.get(2) + 1 <= Calendar.getInstance().get(2)) || (sqlcal.get(1) + 1 <= Calendar.getInstance().get(1))) {
                            count = 0;
                            try (PreparedStatement psu = con.prepareStatement("UPDATE accounts_event SET count = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?")) {
                                psu.setInt(1, getClient().getAccID());
                                psu.setString(2, eventId);
                                psu.executeUpdate();
                                psu.close();
                            }
                        }
                    }
                } else {
                    try (PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_event (accId, eventId, count, type) VALUES (?, ?, ?, ?)")) {
                        psu.setInt(1, getClient().getAccID());
                        psu.setString(2, eventId);
                        psu.setInt(3, 0);
                        psu.setInt(4, type);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
                rs.close();
                ps.close();
            }
            return count;
        } catch (SQLException Ex) {
            log.error("获取 EventCount 次数.", Ex);
        }
        return -1;
    }

    public void setEventCount(String eventId) {
        setEventCount(eventId, 0);
    }

    public void setEventCount(String eventId, int type) {
        setEventCount(eventId, type, 1);
    }

    public void setEventCount(String eventId, int type, int count) {
        int eventCount = getEventCount(eventId, type);
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts_event SET count = ?, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?")) {
                ps.setInt(1, eventCount + count);
                ps.setInt(2, type);
                ps.setInt(3, getClient().getAccID());
                ps.setString(4, eventId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException Ex) {
            log.error("增加 EventCount 次数失败.", Ex);
        }
    }

    public void resetEventCount(String eventId) {
        resetPQLog(eventId, 0);
    }

    public void resetEventCount(String eventId, int type) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts_event SET count = 0, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?")) {
                ps.setInt(1, type);
                ps.setInt(2, getClient().getAccID());
                ps.setString(3, eventId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException Ex) {
            log.error("重置 EventCount 次数失败.", Ex);
        }
    }

    public void gainItem(int code, int amount, String gmLog) {
        MapleInventoryManipulator.addById(client, code, (short) amount, gmLog);
    }

    public void monsterMultiKill() {
        if (killMonsterExps.size() > 2) {
            long multiKillExp = 0;
            for (long exps : killMonsterExps) {
                multiKillExp += (long) Math.ceil((double) (exps * (Math.min(monsterCombo, 60) + 1) * 0.5D / 100.0D));
            }
            gainExp((int) multiKillExp, false, false, false);
        }
        killMonsterExps.clear();
    }

    public void updateTick(int i) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public static enum FameStatus {

        OK, WRONG_CHARNAME,LOW_LEVEL,NOT_TODAY, NOT_THIS_MONTH,UNKNOW_REASON;
    }

    public int getPQLog(String pqName) {
        return getPQLog(pqName, 0);
    }

    public int getPQLog(String pqName, int type) {
        return getPQLog(pqName, type, 1);
    }

    public int getDaysPQLog(String pqName, int days) {
        return getDaysPQLog(pqName, 0, days);
    }

    public int getDaysPQLog(String pqName, int type, int days) {
        return getPQLog(pqName, type, days);
    }

    public int getPQLog(String pqName, int type, int days) {
        try {
            int count = 0;

            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM pqlog WHERE characterid = ? AND pqname = ?");
            ps.setInt(1, this.id);
            ps.setString(2, pqName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                if (type == 0) {
                    Calendar calNow = Calendar.getInstance();
                    Calendar sqlcal = Calendar.getInstance();
                    if (bossTime != null) {
                        sqlcal.setTimeInMillis(bossTime.getTime());
                        sqlcal.add(Calendar.DAY_OF_YEAR, +days);
                    }
                    int day;
                    if (calNow.get(Calendar.YEAR) - sqlcal.get(Calendar.YEAR) > 1) {
                        day = 0;
                    } else if (calNow.get(Calendar.YEAR) - sqlcal.get(Calendar.YEAR) >= 0) {
                        if (calNow.get(Calendar.YEAR) - sqlcal.get(Calendar.YEAR) > 0) {
                            sqlcal.add(Calendar.YEAR, 1);
                        }
                        day = calNow.get(Calendar.DAY_OF_YEAR) - sqlcal.get(Calendar.DAY_OF_YEAR);
                    } else {
                        day = -1;
                    }
                    if (day >= 0) {
                        count = 0;
                        ps = con.prepareStatement("UPDATE pqlog SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?");
                        ps.setInt(1, this.id);
                        ps.setString(2, pqName);
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            } else {
                try (PreparedStatement psu = con.prepareStatement("INSERT INTO pqlog (characterid, pqname, count, type) VALUES (?, ?, ?, ?)")) {
                    psu.setInt(1, this.id);
                    psu.setString(2, pqName);
                    psu.setInt(3, 0);
                    psu.setInt(4, type);
                    psu.executeUpdate();
                    ps.close();
                }
            }
            rs.close();
            ps.close();
            return count;
        } catch (SQLException Ex) {
            System.err.println("Error while get pqlog: " + Ex);
        }
        return -1;
    }

    public void setPQLog(String pqName) {
        setPQLog(pqName, 0);
    }

    public void setPQLog(String pqName, int type) {
        setPQLog(pqName, type, 1);
    }

    public void setPQLog(String pqName, int type, int count) {
        int pqCount = getPQLog(pqName, type);
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, pqCount + count);
                ps.setInt(2, type);
                ps.setInt(3, this.id);
                ps.setString(4, pqName);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException Ex) {
            System.err.println("Error while set pqlog: " + Ex);
        }
    }

    public void resetPQLog(String pqName) {
        resetPQLog(pqName, 0);
    }

    public void resetPQLog(String pqName, int type) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, 0);
                ps.setInt(2, type);
                ps.setInt(3, this.id);
                ps.setString(4, pqName);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException Ex) {
            System.err.println("Error while reset pqlog: " + Ex);
        }
    }

    /*public final int getCybDummy() {
     return this.getClient().getCybDummy();
     }*/

    /*public int tranCyb() {

     return this.getClient().tranCyb();
     }*/
    public final void gainItem(final int id, final short quantity, final boolean randomStats, final long period, String gm_log) {
        if (quantity >= 0) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(id);

            if (!MapleInventoryManipulator.checkSpace(client, id, quantity, "")) {
                return;
            }
            if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                final Item item = randomStats ? ii.randomizeStats((Equip) ii.getEquipById(id)) : ii.getEquipById(id);
                if (period > 0) {
                    item.setExpiration(System.currentTimeMillis() + period);
                }
                item.setGMLog(System.currentTimeMillis() + " - " + gm_log);
                MapleInventoryManipulator.addbyItem(client, item);
            } else {
                MapleInventoryManipulator.addById(client, id, quantity, "", null, period, System.currentTimeMillis() + " - " + getName() + "：玩家通过gainItem获得道具.");
            }
        } else {
            MapleInventoryManipulator.removeById(client, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        client.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) quantity, true));

    }

    public static MapleCharacter getOnlineCharacterById(int cid) {
        MapleCharacter chr = null;
        for (int i = 1; i <= ChannelServer.getChannelCount(); i++) {
            chr = ChannelServer.getInstance(i).getPlayerStorage().getCharacterById(cid);
            if (chr != null) {
                break;
            }
        }
        return chr;
    }

    public static MapleCharacter getCharacterById(int cid) {
        MapleCharacter chr = getOnlineCharacterById(cid);
        return chr == null ? MapleCharacter.loadCharFromDB(cid, new MapleClient(null, null, new tools.MockIOSession()), true) : chr;
    }

    public void handle被动触发技能(MapleMonster monster, int skillid) {
        /* //进阶攻击技能
         1100002 //终极剑斧
         1200002//终极剑钝器
         1300002//终极枪矛
         3100001//终极弓
         3200001//终极弩
         3120008//进阶终极攻击
         51120002
         1120013
         23120012
         21120012
         33120011
         //*/
        //魔力吸收

        Skill skill;
        MapleStatEffect eff;
        //handle进阶技能(monster, skillid);
        switch (this.getJob()) { //TODO 添加攻击触发SMIT
            case 212:
                skill = SkillFactory.getSkill(2100010);
                eff = skill.getEffect(this.getTotalSkillLevel(skill));
                if (eff != null && eff.makeChanceResult() && (skillid == 2101004 || skillid == 2111002 || skillid == 2121006 || skillid == 2121007 || skillid == 2121011 || skillid == 2121054 || skillid == 2121055)) {
                    monster.getMap().spawnMist(new MapleDefender(eff.calculateBoundingBox(monster.getTruePosition(), true), this, eff), eff.getDuration(), false);
                }
                break;
            case 222:
                skill = SkillFactory.getSkill(2220010);
                eff = skill.getEffect(this.getTotalSkillLevel(skill));
                if (skillisCooling(2220010) && eff.makeChanceResult()) {

                }
                break;
            case 522:
                skill = SkillFactory.getSkill(5221021);
                eff = skill.getEffect(this.getTotalSkillLevel(skill));
                if (eff != null && eff.makeChanceResult()) {
                    eff.applyBuffEffect(this, eff.getDuration());
                    setenattacke(true);
                } else if (getenattacke() && skillid != 0) {
                    setenattacke(false);
                }
                break;
        }
    }

    public boolean isMainWeapon(int jobs) {
        Item weapon_item = this.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.没有武器 : ItemConstants.getWeaponType(weapon_item.getItemId());
        switch (weapon) {
            case 没有武器:
                return false;
            case 单手剑:
                return jobs >= 110 && jobs <= 112;
            case 单手斧:
                return jobs >= 110 && jobs <= 112;
            case 单手钝器:
                return jobs >= 120 && jobs <= 122 || jobs >= 2100 && jobs <= 2112;
            case 矛:
                return jobs >= 130 && jobs <= 132 || jobs >= 2100 && jobs <= 2112;
            case 枪:
                return jobs >= 130 && jobs <= 132;
            case 弓:
                return jobs >= 300 && jobs <= 322;
            case 弩:
                return jobs >= 300 && jobs <= 322 || jobs >= 3300 && jobs <= 3312;
            case 短杖:
            case 长杖:
                return jobs >= 200 && jobs <= 232;
            default:
                return false;
        }
    }
}
