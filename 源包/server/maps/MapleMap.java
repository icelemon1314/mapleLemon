package server.maps;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MonsterFamiliar;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.PartyOperation;
import handling.world.WorldBroadcastService;
import handling.world.party.ExpeditionType;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import scripting.event.EventManager;
import server.MapleCarnivalFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Randomizer;
import server.SpeedRunner;
import server.Timer.EtcTimer;
import server.Timer.MapTimer;
import server.events.MapleEvent;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.Spawns;
import server.squad.MapleSquad;
import server.squad.MapleSquadType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.packet.BuffPacket;
import tools.packet.InventoryPacket;
import tools.packet.MobPacket;
import tools.packet.NPCPacket;
import tools.packet.PartyPacket;
import tools.packet.PetPacket;
import tools.packet.SkillPacket;
import tools.packet.SummonPacket;
import tools.packet.UIPacket;

public final class MapleMap {

    private final Map<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapobjects;
    private final Map<MapleMapObjectType, ReentrantReadWriteLock> mapobjectlocks;
    private final List<MapleCharacter> characters = new ArrayList();
    private final ReentrantReadWriteLock charactersLock = new ReentrantReadWriteLock();
    private final AtomicInteger runningOid = new AtomicInteger(100000);
    private final Lock runningOidLock = new ReentrantLock(true);
    private final List<Spawns> monsterSpawn = new ArrayList();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final Map<Integer, MaplePortal> portals = new HashMap();
    private MapleFootholdTree footholds = null;
    private final float monsterRate;
    private float recoveryRate;
    private MapleMapEffect mapEffect;
    private final int channel;
    private short decHP = 0; // 地图自动掉血值
    private int decHPInterval = 10000; // 地图自动掉血间隔
    private short createMobInterval = 9000;
    private short top = 0;
    private short bottom = 0;
    private short left = 0;
    private short right = 0;
    private int consumeItemCoolTime = 0;
    private int protectItem = 0;
    private final int mapid;
    private int returnMapId;
    private int timeLimit;
    private int fieldLimit;
    private int maxRegularSpawn = 0;
    private int fixedMob;
    private int forcedReturnMap = 999999999;
    private int instanceid = -1;
    private int lvForceMove = 0;
    private int lvLimit = 0;
    private int permanentWeather = 0;
    private int partyBonusRate = 0;
    private boolean town;
    private boolean clock;
    private boolean boat;
    private boolean docked;
    private boolean personalShop;
    private boolean everlast = false;
    private boolean dropsDisabled = false;
    private boolean gDropsDisabled = false;
    private boolean soaring = false;
    private boolean squadTimer = false;
    private boolean isSpawns = true;
    private boolean checkStates = true;
    private String mapName;
    private String streetName;
    private String onUserEnter;
    private String onFirstUserEnter;
    private String speedRunLeader = "";
    private final List<Integer> dced = new ArrayList();
    private List<Point> spawnPoints = new ArrayList();
    private ScheduledFuture<?> squadSchedule;
    private long speedRunStart = 0;
    private long lastSpawnTime = 0;
    private long lastHurtTime = 0;
    private MapleNodes nodes;
    private MapleSquadType squad;
    private final Map<String, Integer> environment = new LinkedHashMap();
    private long runeTine = 0;
    private boolean special = false;

    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = channel;
        this.returnMapId = returnMapId;
        if (this.returnMapId == 999999999) {
            this.returnMapId = mapid;
        }
        special = mapid == 105200510;
        if (GameConstants.getPartyPlay(mapid) > 0) {
            this.monsterRate = ((monsterRate - 1.0F) * 2.5F + 1.0F);
        } else {
            this.monsterRate = monsterRate;
        }
        EnumMap objsMap = new EnumMap(MapleMapObjectType.class);
        EnumMap objlockmap = new EnumMap(MapleMapObjectType.class);
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            objsMap.put(type, new LinkedHashMap());
            objlockmap.put(type, new ReentrantReadWriteLock());
        }
        this.mapobjects = Collections.unmodifiableMap(objsMap);
        this.mapobjectlocks = Collections.unmodifiableMap(objlockmap);
    }

    public void setSpawns(boolean fm) {
        this.isSpawns = fm;
    }

    public boolean getSpawns() {
        return this.isSpawns;
    }

    public boolean getSpecial() {
        return this.special;
    }

    public void setFixedMob(int fm) {
        this.fixedMob = fm;
    }

    public void setForceMove(int fm) {
        this.lvForceMove = fm;
    }

    public int getForceMove() {
        return this.lvForceMove;
    }

    public void setLevelLimit(int fm) {
        this.lvLimit = fm;
    }

    public int getLevelLimit() {
        return this.lvLimit;
    }

    public void setReturnMapId(int rmi) {
        this.returnMapId = rmi;
    }

    public void setSoaring(boolean b) {
        this.soaring = b;
    }

    public boolean canSoar() {
        return this.soaring;
    }

    public void toggleDrops() {
        this.dropsDisabled = (!this.dropsDisabled);
    }

    public void setDrops(boolean b) {
        this.dropsDisabled = b;
    }

    public void toggleGDrops() {
        this.gDropsDisabled = (!this.gDropsDisabled);
    }

    public int getId() {
        return this.mapid;
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.returnMapId);
    }

    public int getReturnMapId() {
        return this.returnMapId;
    }

    public int getForcedReturnId() {
        return this.forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public float getRecoveryRate() {
        return this.recoveryRate;
    }

    public void setRecoveryRate(float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public int getFieldLimit() {
        return this.fieldLimit;
    }

    public void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public void setCreateMobInterval(short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapName() {
        return this.mapName;
    }

    public String getStreetName() {
        return this.streetName;
    }

    public void setFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public void setUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public String getFirstUserEnter() {
        return this.onFirstUserEnter;
    }

    public String getUserEnter() {
        return this.onUserEnter;
    }

    public boolean hasClock() {
        return this.clock;
    }

    public void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean isTown() {
        return this.town;
    }

    public void setTown(boolean town) {
        this.town = town;
    }

    public boolean allowPersonalShop() {
        return this.personalShop;
    }

    public void setPersonalShop(boolean personalShop) {
        this.personalShop = personalShop;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return this.everlast;
    }

    public int getHPDec() {
        return this.decHP;
    }

    public void setHPDec(int delta) {
        if ((delta > 0) || (this.mapid == 749040100)) {
            this.lastHurtTime = System.currentTimeMillis();
        }
        this.decHP = (short) delta;
    }

    public int getHPDecInterval() {
        return this.decHPInterval;
    }

    public void setHPDecInterval(int delta) {
        this.decHPInterval = delta;
    }

    public int getHPDecProtect() {
        return this.protectItem;
    }

    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }

    public void setSpawnPoints(List<Point> Points) {
        this.spawnPoints = Points;
    }

    public List<Point> getSpawnPoints() {
        return this.spawnPoints;
    }

    public int getCurrentPartyId() {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getParty() != null) {
                    int i = chr.getParty().getId();
                    return i;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapobject) {
        this.runningOidLock.lock();
        try {
            if (mapobject.getObjectId() != 0) {
                mapobject.setObjectId(mapobject.getObjectId());
            } else {
                mapobject.setObjectId(runningOid.getAndIncrement());
            }

        } finally {
            this.runningOidLock.unlock();
        }

        ReentrantReadWriteLock rl = this.mapobjectlocks.get(mapobject.getType());
        rl.writeLock().lock();
        try {
            ((LinkedHashMap) this.mapobjects.get(mapobject.getType())).put(mapobject.getObjectId(), mapobject);
        } finally {
            rl.writeLock().unlock();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        addMapObject(mapobject);
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if ((mapobject.getType() == MapleMapObjectType.MIST) || (chr.getTruePosition().distance(mapobject.getTruePosition()) <= GameConstants.maxViewRangeSq())) {
                    packetbakery.sendPackets(chr.getClient());
                    chr.addVisibleMapObject(mapobject);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        ReentrantReadWriteLock rl = this.mapobjectlocks.get(obj.getType());
        rl.writeLock().lock();
        try {
            ((LinkedHashMap) this.mapobjects.get(obj.getType())).remove(obj.getObjectId());
        } finally {
            rl.writeLock().unlock();
        }
    }

    public Point calcPointBelow(Point initial) {
        MapleFoothold fh = this.footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if ((!fh.isWall()) && (fh.getY1() != fh.getY2())) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    public Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 99));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(MapleCharacter chr, MapleMonster mob, boolean instanced) {//TODO 怪物死亡掉落物品
        if (mob == null) {
            chr.dropMessage(1,"怪物为空了！");
            return;
        }
        if (chr == null) {
            chr.dropMessage(1,"角色为空了！");
            return;
        }
        if ((ChannelServer.getInstance(this.channel) == null) ) {
            chr.dropMessage(1,"频道为空了！");
            return;
        }
//        if ((this.dropsDisabled)) {
//            chr.dropMessage(1,"该地图不能掉落道具");
//            return;
//        }
        if ((mob.dropsDisabled())) {
            chr.dropMessage(1,"怪物不能掉落道具："+mob.getId());
            return;
        }

        int maxSize = 200;
        if ((!instanced) && (maxSize >= 300) && (((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.ITEM)).size() >= maxSize)) {
            removeDropsDelay();
            if (chr.isShowPacket()) {
                chr.dropMessage(6, new StringBuilder().append("[系统提示] 当前地图的道具数量达到 ").append(maxSize).append(" 系统已自动清理掉所有地上的物品信息.").toString());
            }
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte droptype = (byte) (chr.getParty() != null ? 1 : mob.getStats().isFfaLoot() ? 2 : mob.getStats().isExplosiveReward() ? 3 : 0);
        int mobpos = mob.getTruePosition().x;
        int mesoServerRate = ChannelServer.getInstance(this.channel).getMesoRate(chr.getWorld());
        int dropServerRate = ChannelServer.getInstance(this.channel).getDropRate(chr.getWorld());
        int cashServerRate = ChannelServer.getInstance(this.channel).getCashRate();
        int globalServerRate = ChannelServer.getInstance(this.channel).getGlobalRate();

        byte d = 1;
        Point pos = new Point(0, mob.getTruePosition().y);

        MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        List derp = mi.retrieveDrop(mob.getId());
        if (derp == null) {
            FileoutputUtil.log("找不到怪物的掉落："+mob.getId());
            return;
        }
        List<MonsterDropEntry> dropEntry = new ArrayList(derp);
        Collections.shuffle(dropEntry);

        for (MonsterDropEntry de : dropEntry) {
            if (de.itemId == mob.getStolen()) {
                continue;
            }
            if (Randomizer.nextInt(10000) < (int) (de.chance * dropServerRate * chr.getDropMod() * (chr.getStat().getDropBuff() / 100.0D))) {
                if (((droptype != 3) && (de.itemId == 0))) {
                    continue;
                }
//                if (droptype == 3) {
//                    pos.x = (mobpos + (d % 2 == 0 ? 40 * (d + 1) / 2 : -(40 * (d / 2))));
//                } else {
                    pos.x = Math.min(Math.max(mobpos - 25 * (d / 2), footholds.getMinDropX() + 25),footholds.getMaxDropX() - d * 25);
//                    pos.x = (mobpos + (d % 2 == 0 ? 25 * (d + 1) / 2 : -(25 * (d / 2))));
//                }
                if (de.itemId > 0) {
                    Item idrop;
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        int range = Math.abs(de.Maximum - de.Minimum);
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(range <= 0 ? 1 : range) + de.Minimum : 1), (short) 0);
                    }
                    idrop.setGMLog(new StringBuilder().append("怪物掉落: ").append(mob.getId()).append(" 地图: ").append(this.mapid).append(" 时间: ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                    if (ItemConstants.isNoticeItem(de.itemId)) {
                        broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder(127).append("\"").append(chr.getName()).append("\" 在\"").append(chr.getMap().getMapName()).append("\"杀死\"").append(mob.getStats().getName()).append("\"，掉落道具\"").append(ii.getName(de.itemId)).append("\"").toString()));
                    }
                    spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
                }
                d++;
            }
        }
        d++;
        pos.x = Math.min(Math.max(mobpos - 25 * (d / 2), footholds.getMinDropX() + 25),footholds.getMaxDropX() - d * 25);
        int mesos = Randomizer.nextInt(mob.getMobLevel() * 10) + mob.getMobLevel();
        if (mesos > 0) {
            spawnMobMesoDrop((int) (mesos * (chr.getStat().mesoBuff / 100.0D) * chr.getDropMod() * mesoServerRate), calcDropPos(pos, mob.getTruePosition()), mob, chr, false, droptype);
        }

        List<MonsterGlobalDropEntry> globalEntry = new ArrayList(mi.getGlobalDrop());
        Collections.shuffle(globalEntry);
        int cashz = ((mob.getStats().isBoss()) && (mob.getStats().getHPDisplayType() == 0) ? 20 : 1) * cashServerRate;
        int cashModifier = (int) (mob.getStats().isBoss() ? mob.getStats().isPartyBonus() ? mob.getMobExp() / 1000 : 0 : mob.getMobExp() / 1000 + mob.getMobMaxHp() / 20000L);

        for (MonsterGlobalDropEntry de : globalEntry) {
            if (de.chance == 0) {
                continue;
            }
            if ((Randomizer.nextInt(GameConstants.DROP_ITEM_PER) < de.chance * globalServerRate) && ((de.continent < 0) || ((de.continent < 10) && (this.mapid / 100000000 == de.continent)) || ((de.continent < 100) && (this.mapid / 10000000 == de.continent)) || ((de.continent < 1000) && (this.mapid / 1000000 == de.continent)))) {
                if ((de.itemId == 0) && (cashServerRate != 0)) {
                    int giveCash = Randomizer.nextInt(cashz) + cashz + cashModifier;
                    if (giveCash > 0) {
                        chr.modifyCSPoints(2, giveCash, true);
                    }
                } else if (!this.gDropsDisabled) {
                    if (droptype == 3) {
                        pos.x = (mobpos + (d % 2 == 0 ? 40 * (d + 1) / 2 : -(40 * (d / 2))));
                    } else {
                        pos.x = (mobpos + (d % 2 == 0 ? 25 * (d + 1) / 2 : -(25 * (d / 2))));
                    }
                    Item idrop;
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1), (short) 0);
                    }
                    idrop.setGMLog(new StringBuilder().append("怪物掉落: ").append(mob.getId()).append(" 地图: ").append(this.mapid).append(" (Global) 时间: ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                    if (ItemConstants.isNoticeItem(de.itemId)) {
                        broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder(127).append("\"").append(chr.getName()).append("\" 在\"").append(chr.getMap().getMapName()).append("\"杀死\" ").append(mob.getStats().getName()).append("\"，掉落道具\"").append(ii.getName(de.itemId)).append("\"").toString()));
                    }
                    spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, de.onlySelf ? 0 : droptype, de.questid);
                    d++;
                }
            }
        }
    }

    public void removeMonster(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        this.spawnedMonstersOnMap.decrementAndGet();
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
        removeMapObject(monster);
        monster.killed();
    }

    public void killMonster(final MapleMonster monster) {
        if (monster == null) {
            return;
        }

        this.spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0L);
        final java.util.Timer timer = new java.util.Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (monster.getLinkCID() <= 0) {
                    monster.spawnRevives(monster.getMap());
                }
                timer.cancel();
            }
        };
        if (monster.getId() == 8820108 || monster.getId() == 8810026 || monster.getId() == 8820108 || monster.getId() == 8810130 || monster.getId() == 8820008) {
            timer.schedule(task, 4500);
        } else {
            if (monster.getLinkCID() <= 0) {
                monster.spawnRevives(monster.getMap());
            }
        }

        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), monster.getStats().getSelfD() < 0 ? 1 : monster.getStats().getSelfD()));
        removeMapObject(monster);
        monster.killed();
    }

    public void killMonster(MapleMonster monster, MapleCharacter chr, boolean withDrops, boolean second, byte animation) {
        killMonster(monster, chr, withDrops, second, animation, 0);
    }

    /**
     * 杀死怪物
     * @param monster
     * @param chr
     * @param withDrops
     * @param second
     * @param animation
     * @param lastSkill
     */
    public void killMonster(final MapleMonster monster, final MapleCharacter chr, boolean withDrops, boolean second, byte animation, int lastSkill) {
//        if (((monster.getId() == 8810122) || (monster.getId() == 8810018)) && (!second)) {
//            MapTimer.getInstance().schedule(new Runnable() {
//                @Override
//                public void run() {
//                    MapleMap.this.killMonster(monster, chr, true, true, (byte) 1);
//                    MapleMap.this.killAllMonsters(true);
//                }
//            }, 3000);
//            return;
//        }
//        if (monster.getStatusSourceID(MonsterStatus.持续掉血) == 2111010) { //TODO 处理绿水灵病毒
//            Skill skills = SkillFactory.getSkill(2111010);
//            int skilllevel = chr.getSkillLevel(2111010);
//            MapleStatEffect infoEffect = skills.getEffect(skilllevel);
//            infoEffect.applyBuffEffect(chr, monster.getBuff(MonsterStatus.持续掉血).getPoisonSchedule());
//        }
        this.spawnedMonstersOnMap.decrementAndGet();
        removeMapObject(monster);
        monster.killed();
        MapleSquad sqd = getSquadByMap();
        boolean instanced = (sqd != null) || (monster.getEventInstance() != null) || (getEMByMap() != null);
        int dropOwner = monster.killBy(chr, lastSkill);
        if (animation >= 0) {
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));
        }
        if (dropOwner != -1) { // 这里加经验
            monster.killGainExp(lastSkill);
        }

        int mobid = monster.getId();
        ExpeditionType type = null;
        if ((mobid == 8800002) && ((this.mapid == 280030000))) {
            this.charactersLock.readLock().lock();

            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Zakum;
            }
            doShrine(true);
        } else if ((mobid >= 8800003) && (mobid <= 8800010)) {
            boolean makeZakReal = true;
            Collection<MapleMonster> monsters = getAllMonstersThreadsafe();
            for (MapleMonster mons : monsters) {
                if ((mons.getId() >= 8800003) && (mons.getId() <= 8800010)) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (Object object : monsters) {
                    MapleMonster mons = (MapleMonster) object;
                    if (mons.getId() == 8800000) {
                        Point pos = mons.getTruePosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
                        break;
                    }
                }
            }
        } else if ((mobid >= 8800103) && (mobid <= 8800110)) {
            boolean makeZakReal = true;
            Collection<MapleMonster> monsters = getAllMonstersThreadsafe();
            for (MapleMonster mons : monsters) {
                if ((mons.getId() >= 8800103) && (mons.getId() <= 8800110)) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (MapleMonster mons : monsters) {
                    if (mons.getId() == 8800100) {
                        Point pos = mons.getTruePosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100), pos);
                        break;
                    }
                }
            }
        } else if (mobid >= 8800023 && mobid <= 8800030) {
            boolean makeZakReal = true;
            final Collection<MapleMonster> monsters = getAllMonstersThreadsafe();

            for (final MapleMonster mons : monsters) {
                if (mons.getId() >= 8800023 && mons.getId() <= 8800030) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (final MapleMonster mons : monsters) {
                    if (mons.getId() == 8800020) {
                        final Point pos = mons.getTruePosition();
                        this.killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800020), pos);
                        break;
                    }
                }
            }
        } else if ((mobid >= 9400903) && (mobid <= 9400910)) {
            boolean makeZakReal = true;
            Collection<MapleMonster> monsters = getAllMonstersThreadsafe();
            for (MapleMonster mons : monsters) {
                if ((mons.getId() >= 9400903) && (mons.getId() <= 9400910)) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (MapleMonster mons : monsters) {
                    if (mons.getId() == 9400900) {
                        Point pos = mons.getTruePosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9400900), pos);
                        break;
                    }
                }
            }
        } else if ((mobid / 100000 == 98) && (chr.getMapId() / 10000000 == 95) && (getAllMonstersThreadsafe().isEmpty())) {
            switch (chr.getMapId() % 1000 / 100) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    chr.getClient().getSession().write(UIPacket.MapEff("monsterPark/clear"));
                    break;
                case 5:
                    if (chr.getMapId() / 1000000 == 952) {
                        chr.getClient().getSession().write(UIPacket.MapEff("monsterPark/clearF"));
                    } else {
                        chr.getClient().getSession().write(UIPacket.MapEff("monsterPark/clear"));
                    }
                    break;
                case 6:
                    chr.getClient().getSession().write(UIPacket.MapEff("monsterPark/clearF"));
            }
        } else if ((mobid / 100000 == 93) && (chr.getMapId() / 1000000 == 955) && (getAllMonstersThreadsafe().isEmpty())) {
            switch (chr.getMapId() % 1000 / 100) {
                case 1:
                case 2:
                    chr.getClient().getSession().write(MaplePacketCreator.showEffect("aswan/clear"));
                    chr.getClient().getSession().write(MaplePacketCreator.playSound("Party1/Clear"));
                    break;
                case 3:
                    chr.getClient().getSession().write(MaplePacketCreator.showEffect("aswan/clearF"));
                    chr.getClient().getSession().write(MaplePacketCreator.playSound("Party1/Clear"));
                    chr.dropMessage(-1, "你已经通过了所有回合。请通过传送口移动到外部。");
            }
        }

        if ((type != null) && (this.speedRunStart > 0L) && (this.speedRunLeader.length() > 0)) {
            String name = "";
            switch (type.name()) {
                case "Normal_Balrog":
                    name = "蝙蝠怪";
                    break;
            }
            long endTime = System.currentTimeMillis();
            String time = StringUtil.getReadableMillis(this.speedRunStart, endTime);
            broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder(127).append(this.speedRunLeader).append("带领的远征队，耗时: ").append(time).append(" 击败了 ").append(name).append("!").toString()));
            getRankAndAdd(this.speedRunLeader, time, type, endTime - this.speedRunStart, sqd == null ? null : sqd.getMembers());
            endSpeedRun();
        }

        if ((withDrops) && (dropOwner != -1)) {
            MapleCharacter drop;
            if (dropOwner <= 0) {
                drop = chr;
            } else {
                drop = getCharacterById(dropOwner);
                if (drop == null) {
                    drop = chr;
                }
            }
            dropFromMonster(drop, monster, instanced);
        }

        if ((ServerConstants.打怪获得点抵用卷 || this.isBossMap()) && Randomizer.nextInt(100) < 30) {
            int NX = Math.max(monster.getMobLevel(), 1);
            if (chr.getParty() != null && chr.getParty().getMembers().size() >= 2) {
                for (MaplePartyCharacter pch : chr.getParty().getMembers()) {
                    if (pch.getMapid() == this.getId()) {
                        pch.getChr().modifyCSPoints(monster.getStats().isBoss() ? 1 : 2, NX / (7 - chr.getParty().getMembers().size()), true);
                    }
                }
            } else {
                chr.modifyCSPoints(monster.getStats().isBoss() ? 1 : 2, NX / 10, true);
            }
        } else if (cangetNXMap() && Randomizer.nextInt(100) < 30) {
            int NX = Math.max(monster.getMobLevel(), 1);
            if (chr.getParty() != null && chr.getParty().getMembers().size() >= 1) {
                for (MaplePartyCharacter pch : chr.getParty().getMembers()) {
                    if (pch.getMapid() == this.getId()) {
                        pch.getChr().modifyCSPoints(monster.getStats().isBoss() ? 1 : 2, Math.max(NX / 150, 1) * chr.getParty().getMembers().size() * pch.getChr().getPQLog("MonsterPark"), true);
                    }
                }
            }
        }
    }

    public boolean cangetNXMap() {
        switch (this.getId()) {
            case 952000000:
                return true;
        }
        if (this.getId() >= 952000000 && this.getId() <= 954061000 || this.getId() >= 925020100 && this.getId() <= 925033000) {
            return true;
        }

        return false;
    }

    public List<MapleReactor> getAllReactor() {
        return getAllReactorsThreadsafe();
    }

    public List<MapleReactor> getAllReactorsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ret.add((MapleReactor) mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        return ret;
    }

    public List<MapleSummon> getAllSummonsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.SUMMON)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.SUMMON).values()) {
                if ((mmo instanceof MapleSummon)) {
                    ret.add((MapleSummon) mmo);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.SUMMON)).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllDoor() {
        return getAllDoorsThreadsafe();
    }

    public List<MapleMapObject> getAllDoorsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.DOOR)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.DOOR).values()) {
                if ((mmo instanceof MapleDoor)) {
                    ret.add(mmo);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.DOOR)).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllMechDoorsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.DOOR)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.DOOR).values()) {
                if ((mmo instanceof MechDoor)) {
                    ret.add(mmo);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.DOOR)).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllMerchant() {
        return getAllHiredMerchantsThreadsafe();
    }

    public List<MapleMapObject> getAllHiredMerchantsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.HIRED_MERCHANT).values()) {
                ret.add(mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT)).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMonster> getAllMonster() {
        return getAllMonstersThreadsafe();
    }

    public List<MapleMonster> getAllMonstersThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MONSTER).values()) {
                ret.add((MapleMonster) mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
        return ret;
    }

    public List<Integer> getAllUniqueMonsters() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MONSTER).values()) {
                int theId = ((MapleMonster) mmo).getId();
                if (!ret.contains(theId)) {
                    ret.add(theId);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
        return ret;
    }

    public void killAllMonsters(){
        killAllMonsters(false);
    }

    public void killAllMonsters(boolean animate) {
        for (MapleMapObject monstermo : getAllMonstersThreadsafe()) {
            MapleMonster monster = (MapleMonster) monstermo;
            this.spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0L);
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
            removeMapObject(monster);
            monster.killed();
        }
    }

    public void killMonster(int monsId) {
        for (MapleMapObject mmo : getAllMonstersThreadsafe()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                this.spawnedMonstersOnMap.decrementAndGet();
                removeMapObject(mmo);
                broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
                ((MapleMonster) mmo).killed();
                break;
            }
        }
    }

    private String MapDebug_Log() {
        StringBuilder sb = new StringBuilder("Defeat time : ");
        sb.append(FileoutputUtil.CurrentReadable_Time());
        sb.append(" | Mapid : ").append(this.mapid);
        this.charactersLock.readLock().lock();
        try {
            sb.append(" Users [").append(this.characters.size()).append("] | ");
            for (MapleCharacter mc : this.characters) {
                sb.append(mc.getName()).append(", ");
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return sb.toString();
    }

    public void limitReactor(int rid, int num) {
        List<MapleReactor> toDestroy = new ArrayList();
        Map contained = new LinkedHashMap();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (contained.containsKey(mr.getReactorId())) {
                    if (((Integer) contained.get(mr.getReactorId())) >= num) {
                        toDestroy.add(mr);
                    } else {
                        contained.put(mr.getReactorId(), ((Integer) contained.get(mr.getReactorId())) + 1);
                    }
                } else {
                    contained.put(mr.getReactorId(), 1);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public void destroyReactors(int first, int last) {
        List<MapleReactor> toDestroy = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    toDestroy.add(mr);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        if (reactor == null) {
            return;
        }
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);

        if (reactor.getDelay() > 0) {
            MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    MapleMap.this.respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public void reloadReactors() {
        List<MapleReactor> toSpawn = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor reactor = (MapleReactor) obj;
                broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
                reactor.setAlive(false);
                reactor.setTimerActive(false);
                toSpawn.add(reactor);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor r : toSpawn) {
            removeMapObject(r);
            if (!r.isCustom()) {
                respawnReactor(r);
            }
        }
    }

    public void resetReactors() {
        setReactorState((byte) 0);
    }

    public void setReactorState() {
        setReactorState((byte) 1);
    }

    public void setReactorState(byte state) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ((MapleReactor) obj).forceHitReactor(state);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public void setReactorDelay(int state) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ((MapleReactor) obj).setDelay(state);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public void shuffleReactors() {
        shuffleReactors(0, 9999999);
    }

    public void shuffleReactors(int first, int last) {
        List points = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    points.add(mr.getPosition());
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        Collections.shuffle(points);
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    mr.setPosition((Point) points.remove(points.size() - 1));
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public void updateMonsterController(MapleMonster monster) {
        if (!monster.isAlive() || (monster.getLinkCID() > 0) || (monster.getStats().isEscort())) {
            return;
        }
        MapleCharacter Controller = monster.getController();
        if (Controller != null && Controller.getMap() != null) {
            if (Controller.getMap().getId() != this.getId() || Controller.getTruePosition().distance(monster.getTruePosition()) > monster.getRange()) {
//                FileoutputUtil.log("停止控制 " + monster.getObjectId()  +"  tr: "+Controller.getTruePosition().distance(monster.getTruePosition())+"  "+monster.getRange());
                monster.getController().stopControllingMonster(monster);
            } else {
                return;
            }
        }
        int mincontrolled = -1;
        MapleCharacter newController = null;
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (!chr.isHidden() && (chr.getControlledSize() < mincontrolled || mincontrolled == -1) && (chr.getTruePosition().distanceSq(monster.getTruePosition()) <= monster.getRange())) {
                    mincontrolled = chr.getControlledSize();
                    newController = chr;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        if (newController != null) {
//            FileoutputUtil.log("新控制 " + monster.getObjectId());
            if (monster.isFirstAttack()) {
                newController.controlMonster(monster, true);
                monster.setControllerHasAggro(true);
            } else {
                newController.controlMonster(monster, false);
            }
        }
    }

    public final MapleMapObject getMapObject(int oid, MapleMapObjectType type) {
        this.mapobjectlocks.get(type).readLock().lock();
        try {
            return this.mapobjects.get(type).get(oid);
        } finally {
            this.mapobjectlocks.get(type).readLock().unlock();
        }
    }

    public boolean containsNPC(int npcid) {
        this.mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            MapleNPC n;
            while (itr.hasNext()) {
                n = (MapleNPC) itr.next();
                if (n.getId() == npcid) {
                    return true;
                }
            }
            return false;
        } finally {
            this.mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
    }

    public MapleNPC getNPCById(int id) {
        this.mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            MapleNPC n;
            while (itr.hasNext()) {
                n = (MapleNPC) itr.next();
                if (n.getId() == id) {
                    return n;
                }
            }
            return null;
        } finally {
            this.mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
    }

    public MapleMonster getMonsterById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            MapleMonster ret = null;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();
            MapleMonster n;
            while (itr.hasNext()) {
                n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }

    public int countMonsterById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            int ret = 0;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();
            MapleMonster n;
            while (itr.hasNext()) {
                n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret++;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }

    public MapleReactor getReactorById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            MapleReactor ret = null;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();
            MapleReactor n;
            while (itr.hasNext()) {
                n = (MapleReactor) itr.next();
                if (n.getReactorId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.MONSTER);
        if (mmo == null) {
            return null;
        }
        return (MapleMonster) mmo;
    }

    public MapleSummon getSummonByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.SUMMON);
        if (mmo == null) {
            return null;
        }
        return (MapleSummon) mmo;
    }

    public MapleNPC getNPCByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.NPC);
        if (mmo == null) {
            return null;
        }
        return (MapleNPC) mmo;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.REACTOR);
        if (mmo == null) {
            return null;
        }
        return (MapleReactor) mmo;
    }

    public MonsterFamiliar getFamiliarByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.FAMILIAR);
        if (mmo == null) {
            return null;
        }
        return (MonsterFamiliar) mmo;
    }

    public MapleDefender getMistByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.MIST);
        if (mmo == null) {
            return null;
        }
        return (MapleDefender) mmo;
    }

    public MapleReactor getReactorByName(String name) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (mr.getName().equalsIgnoreCase(name)) {
                    MapleReactor localMapleReactor1 = mr;
                    return localMapleReactor1;
                }
            }
            return null;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public void spawnNpc(int id, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(NPCPacket.spawnNPC(npc, true));
    }

    public final void spawnNpcForPlayer(MapleClient c, final int id, final Point pos) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        c.getSession().write(NPCPacket.spawnNPC(npc, true));
    }

    public void removeNpc(int npcid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).writeLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                MapleNPC npc = (MapleNPC) itr.next();
                if ((npc.isCustom()) && ((npcid == -1) || (npc.getId() == npcid))) {
                    broadcastMessage(NPCPacket.removeNPCController(npc.getObjectId()));
                    broadcastMessage(NPCPacket.removeNPC(npc.getObjectId()));
                    itr.remove();
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).writeLock().unlock();
        }
    }

    public void hideNpc(int npcid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                MapleNPC npc = (MapleNPC) itr.next();
                if ((npcid == -1) || (npc.getId() == npcid)) {
                    broadcastMessage(NPCPacket.removeNPCController(npc.getObjectId()));
                    broadcastMessage(NPCPacket.removeNPC(npc.getObjectId()));
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().unlock();
        }
    }

    public void spawnReactorOnGroundBelow(MapleReactor mob, Point pos) {
        mob.setPosition(pos);
        mob.setCustom(true);
        spawnReactor(mob);
    }

    public void spawnMonster_sSack(MapleMonster mob, Point pos, int spawnType) {
        mob.setPosition(calcPointBelow(new Point(pos.x, pos.y - 1)));
        spawnMonster(mob, spawnType);
    }

    public void spawnMonster_Pokemon(MapleMonster mob, Point pos, int spawnType) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mob.setPosition(spos);
        spawnMonster(mob, spawnType, true);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        spawnMonster_sSack(mob, pos, -2);
    }

    public int spawnMonsterWithEffectBelow(MapleMonster mob, Point pos, int effect) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        return spawnMonsterWithEffect(mob, effect, spos);
    }

    public void spawnZakum(int x, int y) {
        Point pos = new Point(x, y);
        MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        spawnFakeMonster(mainb);
        int[] zakpart = {8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010};
        for (int i : zakpart) {
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);
            spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public void spawnChaosZakum(int x, int y) {
        Point pos = new Point(x, y);
        MapleMonster mainb = MapleLifeFactory.getMonster(8800100);
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        spawnFakeMonster(mainb);
        int[] zakpart = {8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};
        for (int i : zakpart) {
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);
            spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public final void spawnSimpleZakum(final int x, final int y) {
        final Point pos = new Point(x, y);
        final MapleMonster mainb = MapleLifeFactory.getMonster(8800020);
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        // Might be possible to use the map object for reference in future.
        spawnFakeMonster(mainb);

        final int[] zakpart = {8800023, 8800024, 8800025, 8800026, 8800027,
            8800028, 8800029, 8800030};

        for (final int i : zakpart) {
            final MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);

            spawnMonster(part, -2);
        }
        if (squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public void spawnPinkZakum(int x, int y) {
        Point pos = new Point(x, y);
        MapleMonster mainb = MapleLifeFactory.getMonster(9400900);
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        spawnFakeMonster(mainb);
        int[] zakpart = {9400903, 9400904, 9400905, 9400906, 9400907, 9400908, 9400909, 9400910};
        for (int i : zakpart) {
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);
            spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    private void checkRemoveAfter(MapleMonster monster) {
        int ra = monster.getStats().getRemoveAfter();
        if ((ra > 0) && (monster.getLinkCID() <= 0)) {
            monster.registerKill(ra * 1000);
        }
    }

    public void spawnRevives(final MapleMonster monster, final int oid) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        monster.setLinkOid(oid);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 ? -3 : monster.getStats().getSummonType(), oid));
            }
        });
        updateMonsterController(monster);
        this.spawnedMonstersOnMap.incrementAndGet();

    }

    public void spawnMonster(MapleMonster monster, int spawnType) {
        spawnMonster(monster, spawnType, false);
    }

    public void spawnMonster(final MapleMonster monster, final int spawnType, final boolean overwrite) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        if (monster.getId() == 9300166) {
            MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 2));
                }
            }, 3000);
        }

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 || monster.getStats().getSummonType() == 27 || overwrite ? spawnType : monster.getStats().getSummonType(), 0));
            }
        });
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public int spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        try {
            monster.setMap(this);
            monster.setPosition(pos);

            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() // spawnAndAddRangedMapObject(monster, new DelayedPacketCreation(monster, effect)
            {
                @Override
                public void sendPackets(MapleClient c) {
                    c.getSession().write(MobPacket.spawnMonster(monster, effect, 0));
                }
            });
            updateMonsterController(monster);
            this.spawnedMonstersOnMap.incrementAndGet();
            return monster.getObjectId();
        } catch (Exception e) {
        }
        return -1;
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, -4, 0));
            }
        });
        updateMonsterController(monster);
        this.spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.spawnReactor(reactor));
            }
        });
    }

    private void respawnReactor(MapleReactor reactor) {
        if ((!isSecretMap()) && (reactor.getReactorId() >= 100000) && (reactor.getReactorId() <= 200011)) {
            int newRid = (reactor.getReactorId() < 200000 ? 100000 : 200000) + Randomizer.nextInt(11);
            int prop = reactor.getReactorId() % 100;
            if ((Randomizer.nextInt(22) <= prop) && (newRid % 100 < 10)) {
                newRid++;
            }
            if ((Randomizer.nextInt(110) <= prop) && (newRid % 100 < 11)) {
                newRid++;
            }
            List toSpawnPos = new ArrayList(this.spawnPoints);
            for (MapleMapObject reactor1l : getAllReactorsThreadsafe()) {
                MapleReactor reactor2l = (MapleReactor) reactor1l;
                if ((!toSpawnPos.isEmpty()) && (toSpawnPos.contains(reactor2l.getPosition()))) {
                    toSpawnPos.remove(reactor2l.getPosition());
                }

            }

            MapleReactor newReactor = new MapleReactor(MapleReactorFactory.getReactor(newRid), newRid);
            newReactor.setPosition(toSpawnPos.isEmpty() ? reactor.getPosition() : (Point) toSpawnPos.get(Randomizer.nextInt(toSpawnPos.size())));
            newReactor.setDelay(newRid % 100 == 11 ? 60000 : 5000);
            spawnReactor(newReactor);
        } else {
            reactor.setState((byte) 0);
            reactor.setAlive(true);
            spawnReactor(reactor);
        }
    }

    public boolean isSecretMap() {
        switch (this.mapid) {
            case 910001001:
            case 910001002:
            case 910001003:
            case 910001004:
            case 910001005:
            case 910001006:
            case 910001007:
            case 910001008:
            case 910001009:
            case 910001010:
                return true;
        }
        return false;
    }

    public final void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                door.sendSpawnData(c);
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        });
    }

    public final void spawnArrowsTurret(final MapleArrowsTurret aturet) { //召唤箭矢炮盘
        MapTimer tMan = MapTimer.getInstance();
        final ScheduledFuture poisonSchedule;
        spawnAndAddRangedMapObject(aturet, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                broadcastMessage(SkillPacket.spawnArrowsTurret(aturet));
                c.getSession().write(SkillPacket.ArrowsTurretAction(aturet));
            }
        });
        poisonSchedule = tMan.register(new Runnable() {
            @Override
            public void run() {
                //for (MapleCharacter chr : aturet.getMap().getCharacters()) {
                if (MapleMap.this.getCharacterById(aturet.getOwnerId()) == null) {
                    //if (!aturet.getMap().getCharacters().equals(aturet.getOwner())) {
                    MapleMap.this.removeMapObject(aturet);
                    MapleMap.this.broadcastMessage(SkillPacket.cancelArrowsTurret(aturet));
                }
            }
        }, 500, 500);

        aturet.setSchedule(tMan.schedule(new Runnable() {
            @Override
            public void run() {
                poisonSchedule.cancel(false);
                MapleMap.this.removeMapObject(aturet);
                MapleMap.this.broadcastMessage(SkillPacket.cancelArrowsTurret(aturet));
            }
        }, ((20 + (long) Math.floor(aturet.getSkillLevel() / 3)) * 1000)));
    }

    public final void spawnSummon(final MapleSummon summon) {//第二次发送的召唤兽包
        summon.updateMap(this);
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if ((summon != null) && (c.getPlayer() != null) && ((!summon.isChangedMap()) || (summon.getOwnerId() == c.getPlayer().getId()))) {
                    c.getSession().write(SummonPacket.spawnSummon(summon, true));
                }
            }
        });
    }

    public void spawnFamiliar(final MonsterFamiliar familiar) {
        spawnAndAddRangedMapObject(familiar, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if ((familiar != null) && (c.getPlayer() != null)) {
                    c.getSession().write(MaplePacketCreator.spawnFamiliar(familiar, true));
                }
            }
        });
    }

    public void spawnExtractor(final MapleExtractor ex) {
        spawnAndAddRangedMapObject(ex, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                ex.sendSpawnData(c);
            }
        });
    }

    public void spawnLove(final MapleLove love) {
        spawnAndAddRangedMapObject(love, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                love.sendSpawnData(c);
            }
        });
        MapTimer tMan = MapTimer.getInstance();
        tMan.schedule(new Runnable() {
            @Override
            public void run() {
                MapleMap.this.broadcastMessage(MaplePacketCreator.removeLove(love.getObjectId(), love.getItemId()));
                MapleMap.this.removeMapObject(love);
            }
        }, 3600000L);
    }

    public void spawnMist(final MapleDefender mist, final int duration, boolean fake) {
        spawnAndAddRangedMapObject(mist, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                mist.sendSpawnData(c);
            }
        });
        MapTimer tMan = MapTimer.getInstance();
        final ScheduledFuture poisonSchedule;
        if ((mist.isPoisonMist()) && (!mist.isMobMist())) {
            final MapleCharacter owner = getCharacterById(mist.getOwnerId());
            poisonSchedule = tMan.register(new Runnable() {
                @Override
                public void run() {
                    for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                        if ((mist.makeChanceResult()) && (!((MapleCharacter) mo).hasDOT()) && (((MapleCharacter) mo).getId() != mist.getOwnerId())) {
                            ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getSourceSkill().getId(), mist.getSkillLevel());
                        } else if ((mist.makeChanceResult()) && (!((MapleMonster) mo).isBuffed(MonsterStatus.中毒))
                                && (owner != null)) {
                            ((MapleMonster) mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.中毒, 1, mist.getSourceSkill().getId(), null, false), true, duration, true, mist.getSource());
                        }
                    }
                }
            }, 2000, 2500);
        } else {//poisonSchedule = tMan.register(new Runnable()
            //ScheduledFuture poisonSchedule;
            if (mist.isRecoverMist()) {
                poisonSchedule = tMan.register(new Runnable() {
                    @Override
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                            MapleCharacter chr = (MapleCharacter) mo;
                            if (mist.makeChanceResult() && chr.isAlive()) {
                                chr.addMPHP((int) ((mist.getSource().getX() * (chr.getStat().getMaxHp() / 100.0D))), (int) (mist.getSource().getX() * (chr.getStat().getMaxMp() / 100.0D)));
                            }
                        }
                    }
                }, 2000, 2500);
            } else if (!mist.isMobMist() && mist.getSourceSkill().getId() == 4121015) {
                final MapleCharacter owner = getCharacterById(mist.getOwnerId());
                poisonSchedule = tMan.register(new Runnable() {
                    @Override
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList( MapleMapObjectType.MONSTER))) {
                            if ((mist.makeChanceResult()) && (!((MapleCharacter) mo).hasDOT()) && (((MapleCharacter) mo).getId() != mist.getOwnerId())) {
                                ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getSourceSkill().getId(), mist.getSkillLevel());
                            } else if ((mist.makeChanceResult()) && (!((MapleMonster) mo).isBuffed(MonsterStatus.中毒))
                                    && (owner != null)) {
                                ((MapleMonster) mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.物攻, -mist.getSource().getW(), mist.getSourceSkill().getId(), null, false), false, duration, true, mist.getSource());
                                ((MapleMonster) mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.魔攻, -mist.getSource().getW(), mist.getSourceSkill().getId(), null, false), false, duration, true, mist.getSource());
                                ((MapleMonster) mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.速度, mist.getSource().getY(), mist.getSourceSkill().getId(), null, false), false, duration, true, mist.getSource());
                            }
                        }
                    }
                }, 2000, 2500);
            } else if ((mist.isPoisonMist()) && (mist.isMobMist())) {
                poisonSchedule = tMan.register(new Runnable() {
                    @Override
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                            ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getMobSkill().getSkillId(), mist.getMobSkill().getSkillLevel());
                        }
                    }
                }, 2000, 2500);
            }else if ((mist.isGivebuff()) && (!mist.isMobMist())) {
                poisonSchedule = tMan.register(new Runnable() {
                    @Override
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                            if (((MapleCharacter) mo).getParty() != null && ((MapleCharacter) mo).getParty().getMemberById(mist.getOwnerId()) != null && ((MapleCharacter) mo).getBuffStats(mist.getSource(), -1) == null || ((MapleCharacter) mo).getId() == mist.getOwnerId() && ((MapleCharacter) mo).getBuffStats(mist.getSource(), -1) == null) {
                                mist.getSource().applyBuffEffect(((MapleCharacter) mo), mist.getSource().getDuration());
                            }
                        }
                        if (MapleMap.this.getCharacterById_InMap(mist.getOwnerId()).getParty() != null && mist.getSourceSkill().getId() != 35121010) {
                            for (MaplePartyCharacter mo : MapleMap.this.getCharacterById_InMap(mist.getOwnerId()).getParty().getMembers()) {
                                if (!getCharactersIntersect(mist.getBox()).contains(MapleMap.this.getCharacterById_InMap(mo.getId())) && MapleMap.this.getCharacterById_InMap(mo.getId()).getBuffStats(mist.getSource(), -1) != null) {
                                    MapleMap.this.getCharacterById_InMap(mo.getId()).cancelEffect(mist.getSource(), false, -1);
                                }
                            }
                        }
                    }
                }, 2000, 2500);
            } else {
                int id = mist.isMobMist() ? mist.getMobSkill().getSkillId() : mist.getSourceSkill().getId();
                String from = mist.isMobMist() ? "怪物" : "玩家：" + mist.getOwnerId();
                FileoutputUtil.log("[未处理 mist] 来自 " + from + " 技能ID：" + id + "");
                poisonSchedule = null;
            }
        }
        mist.setPoisonSchedule(poisonSchedule);
        mist.setSchedule(tMan.schedule(new Runnable() {
            @Override
            public void run() {
                MapleMap.this.removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                MapleMap.this.broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId(), false));
            }
        }, duration));
    }

    public void disappearingItemDrop(MapleMapObject dropper, MapleCharacter owner, Item item, Point pos) {
        Point droppos = calcDropPos(pos, pos);
        MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(InventoryPacket.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 3, false), drop.getTruePosition());
    }

    public void spawnMesoDrop(int meso, Point position, final MapleMapObject dropper, MapleCharacter owner, boolean playerDrop, byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(InventoryPacket.dropItemFromMapObject(mdrop, dropper.getTruePosition(), droppos, (byte) 1, false));
            }
        });
        if (!this.everlast) {
            mdrop.registerExpire(120000L);
            if ((droptype == 0) || (droptype == 1)) {
                mdrop.registerFFA(30000L);
            }
        }
    }

    public void spawnMobMesoDrop(int meso, final Point position, final MapleMapObject dropper, MapleCharacter owner, boolean playerDrop, byte droptype) {
        final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(InventoryPacket.dropItemFromMapObject(mdrop, dropper.getTruePosition(), position, (byte) 1, false));
            }
        });
        mdrop.registerExpire(120000L);
        if ((droptype == 0) || (droptype == 1)) {
            mdrop.registerFFA(30000L);
        }
    }

    public void spawnMobDrop(final Item idrop, final Point dropPos, final MapleMonster mob, MapleCharacter chr, byte droptype, final int questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if ((c != null) && (c.getPlayer() != null) && ((questid <= 0) || (c.getPlayer().getQuestStatus(questid) == 1)) && ((idrop.getItemId() / 10000 != 238) ) && (mob != null) && (dropPos != null)) {
                    c.getSession().write(InventoryPacket.dropItemFromMapObject(mdrop, mob.getTruePosition(), dropPos, (byte) 1, false));
                }
            }
        });
        mdrop.registerExpire(120000L);
        if ((droptype == 0) || (droptype == 1)) {
            mdrop.registerFFA(30000L);
        }
        activateItemReactors(mdrop, chr.getClient());
    }

    public void spawnRandDrop() {
        if ((this.mapid != 910000000) || (this.channel != 1)) {
            return;
        }

        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for (MapleMapObject o : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (((MapleMapItem) o).isRandDrop()) {
                    return;
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().unlock();
        }
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
                int theItem = Randomizer.nextInt(1000);
                int itemid;
                if (theItem < 950) {
                    itemid = GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
                } else {
                    if (theItem < 990) {
                        itemid = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
                    } else {
                        itemid = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
                    }
                }
                MapleMap.this.spawnAutoDrop(itemid, pos);
            }
        }, 20000L);
    }

    public void spawnAutoDrop(int itemid, final Point pos) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop;
        if (ItemConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip) ii.getEquipById(itemid));
        } else {
            idrop = new Item(itemid, (byte) 0, (short) 1, (short) 0);
        }
        idrop.setGMLog(new StringBuilder().append("自动掉落 ").append(itemid).append(" 地图 ").append(this.mapid).toString());
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(InventoryPacket.dropItemFromMapObject(mdrop, pos, pos, (byte) 1, false));
            }
        });
        broadcastMessage(InventoryPacket.dropItemFromMapObject(mdrop, pos, pos, (byte) 0, false));
        if (itemid / 10000 != 291) {
            mdrop.registerExpire(120000L);
        }
    }

    public void spawnItemDrop(final MapleMapObject dropper, MapleCharacter owner, Item item, Point pos, boolean ffaDrop, boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 2, playerDrop);

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(InventoryPacket.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 1, false));
            }
        });
        broadcastMessage(InventoryPacket.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 0, false));

        if (!this.everlast) {
            drop.registerExpire(120000L);
            activateItemReactors(drop, owner.getClient());
        }
    }

    public void spawnItemDrop(final MapleMapObject dropper, MapleCharacter owner,int itemId,short quantity) {
        final Point droppos = calcDropPos(owner.getPosition(), owner.getPosition());
        Item toDrop;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
            toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
        } else {
            toDrop = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
        }

        final MapleMapItem drop = new MapleMapItem(toDrop, droppos, dropper, owner, (byte) 2, false);

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(InventoryPacket.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 1, false));
            }
        });
        broadcastMessage(InventoryPacket.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 0, false));

        if (!this.everlast) {
            drop.registerExpire(120000L);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(MapleMapItem drop, MapleClient c) {
        Item item = drop.getItem();

        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject o : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor react = (MapleReactor) o;

                if ((react.getReactorType() == 100)
                        && (item.getItemId() == GameConstants.getCustomReactItem(react.getReactorId(), (react.getReactItem().getLeft()))) && ((react.getReactItem().getRight()) == item.getQuantity())
                        && (react.getArea().contains(drop.getTruePosition()))
                        && (!react.isTimerActive())) {
                    MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000L);
                    react.setTimerActive(true);
                    break;
                }
            }

        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public int getItemsSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.ITEM)).size();
    }

    public int getExtractorSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.EXTRACTOR)).size();
    }

    public int getMobsSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).size();
    }

    public int getRunesSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MAPLE_RUNE)).size();
    }

    public List<MapleMapItem> getAllItems() {
        return getAllItemsThreadsafe();
    }

    public List<MapleMapItem> getAllItemsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                ret.add((MapleMapItem) mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().unlock();
        }
        return ret;
    }

    public Point getPointOfItem(int itemid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                MapleMapItem mm = (MapleMapItem) mmo;
                if ((mm.getItem() != null) && (mm.getItem().getItemId() == itemid)) {
                    Point localPoint = mm.getPosition();
                    return localPoint;
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().unlock();
        }
        return null;
    }

    public List<MapleDefender> getAllMistsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MIST)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MIST).values()) {
                ret.add((MapleDefender) mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MIST)).readLock().unlock();
        }
        return ret;
    }

    public void returnEverLastItem(MapleCharacter chr) {
        for (MapleMapObject o : getAllItemsThreadsafe()) {
            MapleMapItem item = (MapleMapItem) o;
            if (item.getOwner() == chr.getId()) {
                item.setPickedUp(true);
                broadcastMessage(InventoryPacket.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getTruePosition());
                if (item.getMeso() > 0) {
                    chr.gainMeso(item.getMeso(), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
                }
                removeMapObject(item);
            }
        }
        spawnRandDrop();
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, false);
    }

    public void startMapEffect(String msg, int itemId, boolean jukebox) {
        if (this.mapEffect != null) {
            return;
        }
        this.mapEffect = new MapleMapEffect(msg, itemId);
        this.mapEffect.setJukebox(jukebox);
        broadcastMessage(this.mapEffect.makeStartData());
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.mapEffect != null) {
                    MapleMap.this.broadcastMessage(MapleMap.this.mapEffect.makeDestroyData());
                    MapleMap.this.mapEffect = null;
                }
            }
        }, jukebox ? 300000L : 30000L);
    }

    public void startPredictCardMapEffect(String msg, int itemId, int effectType) {
        startMapEffect(msg, itemId, 30, effectType);
    }

    public void startMapEffect(String msg, int itemId, int time) {
        startMapEffect(msg, itemId, time, -1);
    }

    public void startMapEffect(String msg, int itemId, int time, int effectType) {
        if (this.mapEffect != null) {
            return;
        }
        if (time <= 0) {
            time = 5;
        }
        this.mapEffect = new MapleMapEffect(msg, itemId, effectType);
        this.mapEffect.setJukebox(false);
//        broadcastMessage(this.mapEffect.makeStartData());
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.mapEffect != null) {
                    MapleMap.this.broadcastMessage(MapleMap.this.mapEffect.makeDestroyData());
                    MapleMap.this.mapEffect = null;
                }
            }
        }, time * 1000);
    }

    public void startExtendedMapEffect(final String msg, final int itemId) {
        broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, true));
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                MapleMap.this.broadcastMessage(MaplePacketCreator.removeMapEffect());
                MapleMap.this.broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, false));
            }
        }, 60000L);
    }

    public void startSimpleMapEffect(String msg, int itemId) {
        broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, true));
    }

    public void startJukebox(String msg, int itemId) {
        startMapEffect(msg, itemId, true);
    }

    /**
     * 添加玩家到地图
     *
     * @param chr
     */
    public final void addPlayer(final MapleCharacter chr) {
        this.mapobjectlocks.get(MapleMapObjectType.PLAYER).writeLock().lock();
        try {
            this.mapobjects.get(MapleMapObjectType.PLAYER).put(chr.getObjectId(), chr);
        } finally {
            this.mapobjectlocks.get(MapleMapObjectType.PLAYER).writeLock().unlock();
        }
        this.charactersLock.writeLock().lock();
        try {
            this.characters.add(chr);
        } finally {
            this.charactersLock.writeLock().unlock();
        }
        chr.setChangeTime(true);
        if ((GameConstants.isTeamMap(this.mapid))) {
            chr.setTeam(getAndSwitchTeam() ? 0 : 1);
        }
        byte[] packet = MaplePacketCreator.spawnPlayerMapobject(chr); //封包在这
        if (!chr.isHidden()) {
            broadcastMessage(chr, packet, false);
            if ((chr.isIntern()) && (this.speedRunStart > 0L)) {
                endSpeedRun();
                broadcastMessage(MaplePacketCreator.serverMessageNotice("The speed run has ended."));
            }
        } else {
            broadcastGMMessage(chr, packet, false);
        }
        if (!onFirstUserEnter.equals("")) {
            if (getCharactersSize() == 1) {
                MapScriptMethods.startScript_FirstUser(chr.getClient(), onFirstUserEnter);
            }
        }
        if (!onUserEnter.equals("")) {
            MapScriptMethods.startScript_User(chr.getClient(), onUserEnter);
        }
        sendObjectPlacement(chr);
//        GameConstants.achievementRatio(chr.getClient());
        if ((GameConstants.isTeamMap(this.mapid)) ) {
            //chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect(chr.getTeam()));
        }

        MaplePet pets = chr.getSpawnPets(); //宠物发的包
        if ((pets != null) && (pets.getSummoned())) {
            pets.setPos(chr.getTruePosition());
//            chr.getClient().getSession().write(PetPacket.updatePet(pets, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pets.getInventoryPosition()), false));
            chr.getClient().getSession().write(PetPacket.showPet(chr, pets, false, false, true));
//            chr.getClient().getSession().write(PetPacket.loadExceptionList(chr, pets));
        }
        if (chr.getSummonedFamiliar() != null) {
//            chr.spawnFamiliar(chr.getSummonedFamiliar());
        }
        if (chr.getParty() != null) {
//            chr.silentPartyUpdate();
//            chr.getClient().getSession().write(PartyPacket.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.更新队伍, null));
//            chr.updatePartyMemberHP();
//            chr.receivePartyMemberHP();
        }
        boolean quickMove = false;
        if ((!chr.isInBlockedMap()) && (chr.getLevel() >= 10)) {
            for (MapleQuickMove qm : MapleQuickMove.values()) {
                if (qm.getMap() == chr.getMapId()) {
                    List<MapleQuickMove.QuickMoveNPC> qmn = new LinkedList();
                    int npcs = qm.getNPCFlag();
                    for (MapleQuickMove.QuickMoveNPC npc : MapleQuickMove.QuickMoveNPC.values()) {
                        if ((npcs & npc.getValue()) != 0 && npc.show()) {
                            qmn.add(npc);
                        }
                    }
                    quickMove = true;
                    chr.getClient().getSession().write(MaplePacketCreator.getQuickMoveInfo(true, qmn));
                    break;
                }
            }
        }
        if (!quickMove) {
//            chr.getClient().getSession().write(MaplePacketCreator.getQuickMoveInfo(false, new LinkedList()));
        }

        Map<Integer,MapleSummon> ss = chr.getSummonsReadLock();//召唤兽
        try {
            for (MapleSummon summon : ss.values()) {
                summon.setPosition(chr.getTruePosition());
                chr.addVisibleMapObject(summon);
                spawnSummon(summon);
            }
        } finally {
            chr.unlockSummonsReadLock();
        }
//        if (this.mapEffect != null) {
//            this.mapEffect.sendStartData(chr.getClient());
//        }
        if ((this.timeLimit > 0) && (getForcedReturnMap() != null)) {
            chr.startMapTimeLimitTask(this.timeLimit, getForcedReturnMap());
        }
//        if ((chr.getBuffedValue(MapleBuffStat.骑兽技能) != null) && (!GameConstants.is反抗者(chr.getJob()))
//                && (FieldLimitType.Mount.check(this.fieldLimit))) {
//            chr.cancelEffectFromBuffStat(MapleBuffStat.骑兽技能);
//        }

        if (chr.getSidekick() != null) {
            MapleCharacter side = getCharacterById(chr.getSidekick().getCharacter(chr.getSidekick().getCharacter(0).getId() == chr.getId() ? 1 : 0).getId());
            if (side != null) {
                chr.getSidekick().applyBuff(side);
                chr.getSidekick().applyBuff(chr);
            }
        }
        if ((chr.getEventInstance() != null) && (chr.getEventInstance().isTimerStarted())) {
                chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000L)));
        }
        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            chr.getClient().getSession().write(MaplePacketCreator.getClockTime(cal.get(11), cal.get(12), cal.get(13)));
        }
        if(hasBoat() == 2) { // 船还在
            chr.getClient().getSession().write(MaplePacketCreator.boatPacket(true));
        }

        if ((chr.getCarnivalParty() != null) && (chr.getEventInstance() != null)) {
            chr.getEventInstance().onMapLoad(chr);
        }
        MapleEvent.mapLoad(chr, this.channel);
        if ((getSquadBegin() != null) && (getSquadBegin().getTimeLeft() > 0L) && (getSquadBegin().getStatus() == 1)) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (getSquadBegin().getTimeLeft() / 1000L)));
        }
        if ((this.mapid / 1000 != 105100) && (this.mapid / 100 != 8020003) && (this.mapid / 100 != 8020008) && (this.mapid != 271040100)) {
            MapleSquad sqd = getSquadByMap();
            EventManager em = getEMByMap();
            if ((!this.squadTimer) && (sqd != null) && (chr.getName().equals(sqd.getLeaderName())) && (em != null) && (em.getProperty("leader") != null) && (em.getProperty("leader").equals("true")) && (this.checkStates)) {
                doShrine(false);
                this.squadTimer = true;
            }
        }
        if ((getNumMonsters() > 0) && ((this.mapid == 280030001) || (this.mapid == 240060201) || (this.mapid == 280030000) || (this.mapid == 280030100) || (this.mapid == 240060200) || (this.mapid == 220080001) || (this.mapid == 541020800) || (this.mapid == 541010100))) {
            String music = "Bgm09/TimeAttack";
            switch (this.mapid) {
                case 240060200:
                case 240060201:
                    music = "Bgm14/HonTale";
                    break;
                case 280030000:
                case 280030001:
                case 280030100:
                    music = "Bgm06/FinalFight";
            }

            chr.getClient().getSession().write(MaplePacketCreator.musicChange(music));
        }
        if (this.permanentWeather > 0) {
            chr.getClient().getSession().write(MaplePacketCreator.startMapEffect("", this.permanentWeather, false));
        }
        if (getPlatforms().size() > 0) {
            chr.getClient().getSession().write(MaplePacketCreator.getMovingPlatforms(this));
        }
        if (this.environment.size() > 0) {
            chr.getClient().getSession().write(MaplePacketCreator.getUpdateEnvironment(this));
        }
    }

    public int getNumItems() {
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            return mapobjects.get(MapleMapObjectType.ITEM).size();
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
    }

    public int getNumMonsters() {
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            return mapobjects.get(MapleMapObjectType.MONSTER).size();
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
    }

    public void doShrine(final boolean spawned) {
        if (this.squadSchedule != null) {
            cancelSquadSchedule(true);
        }
        MapleSquad sqd = getSquadByMap();
        if (sqd == null) {
            return;
        }
        final int mode = (this.mapid == 240060200) || (this.mapid == 240060201) ? 3 : this.mapid == 280030001 ? 2 : (this.mapid == 280030000) || (this.mapid == 280030100) ? 1 : 0;

        EventManager em = getEMByMap();
        if ((em != null) && (getCharactersSize() > 0)) {
            final String leaderName = sqd.getLeaderName();
            final String state = em.getProperty("state");

            MapleMap returnMapa = getForcedReturnMap();
            if ((returnMapa == null) || (returnMapa.getId() == this.mapid)) {
                returnMapa = getReturnMap();
            }
            if (mode == 1 || mode == 2) { //chaoszakum
                broadcastMessage(MaplePacketCreator.showChaosZakumShrine(spawned, 5));
            } else if (mode == 3) { //ht/chaosht
                broadcastMessage(MaplePacketCreator.showChaosHorntailShrine(spawned, 5));
            } else {
                broadcastMessage(MaplePacketCreator.showHorntailShrine(spawned, 5));
            }
            if (spawned) { //both of these together dont go well
                broadcastMessage(MaplePacketCreator.getClock(300));
            }
            final MapleMap returnMapz = returnMapa;
            Runnable run;
            if (!spawned) {
                final List<MapleMonster> monsterz = getAllMonstersThreadsafe();
                final List<Integer> monsteridz = new ArrayList<>();
                for (MapleMapObject m : monsterz) {
                    monsteridz.add(m.getObjectId());
                }
                run = new Runnable() {
                    @Override
                    public void run() {
                        MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        if ((MapleMap.this.getCharactersSize() > 0) && (MapleMap.this.getNumMonsters() == monsterz.size()) && (sqnow != null) && (sqnow.getStatus() == 2) && (sqnow.getLeaderName().equals(leaderName)) && (MapleMap.this.getEMByMap().getProperty("state").equals(state))) {
                            boolean passed = monsterz.isEmpty();
                            for (MapleMapObject m : MapleMap.this.getAllMonstersThreadsafe()) {
                                for (int i : monsteridz) {
                                    if (m.getObjectId() == i) {
                                        passed = true;
                                        break;
                                    }
                                }
                                if (passed) {
                                    break;
                                }
                            }
                            if (passed) {
                                byte[] packet;

                                if ((mode == 1) || (mode == 2)) {
                                    packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                                } else {
                                    packet = MaplePacketCreator.showHorntailShrine(spawned, 0);
                                }
                                for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                    chr.getClient().getSession().write(packet);
                                    chr.changeMap(returnMapz, returnMapz.getPortal(0));
                                }
                                MapleMap.this.checkStates("");
                                MapleMap.this.resetFully();
                            }
                        }
                    }
                };
            } else {
                run = new Runnable() {
                    @Override
                    public void run() {
                        MapleSquad sqnow = MapleMap.this.getSquadByMap();

                        if ((MapleMap.this.getCharactersSize() > 0) && (sqnow != null) && (sqnow.getStatus() == 2) && (sqnow.getLeaderName().equals(leaderName)) && (MapleMap.this.getEMByMap().getProperty("state").equals(state))) {
                            byte[] packet;

                            if ((mode == 1) || (mode == 2)) {
                                packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                            } else {
                                packet = MaplePacketCreator.showHorntailShrine(spawned, 0);
                            }
                            for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                chr.getClient().getSession().write(packet);
                                chr.changeMap(returnMapz, returnMapz.getPortal(0));
                            }
                            MapleMap.this.checkStates("");
                            MapleMap.this.resetFully();
                        }
                    }
                };
            }
            this.squadSchedule = MapTimer.getInstance().schedule(run, 300000L);
        }
    }

    public MapleSquad getSquadByMap() {
        MapleSquadType zz;
        switch (this.mapid) {
            case 105100300:
            case 105100400:
                zz = MapleSquadType.bossbalrog;
                break;
            case 280030000:
            case 280030100:
                zz = MapleSquadType.zak;
                break;
            case 280030001:
                zz = MapleSquadType.chaoszak;
                break;
            case 240060200:
                zz = MapleSquadType.horntail;
                break;
            case 240060201:
                zz = MapleSquadType.chaosht;
                break;
            case 270050100:
                zz = MapleSquadType.pinkbean;
                break;
            case 270051100:
                zz = MapleSquadType.chaospb;
                break;
            case 802000111:
                zz = MapleSquadType.nmm_squad;
                break;
            case 802000211:
                zz = MapleSquadType.vergamot;
                break;
            case 802000311:
                zz = MapleSquadType.tokyo_2095;
                break;
            case 802000411:
                zz = MapleSquadType.dunas;
                break;
            case 802000611:
                zz = MapleSquadType.nibergen_squad;
                break;
            case 802000711:
                zz = MapleSquadType.dunas2;
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                zz = MapleSquadType.core_blaze;
                break;
            case 802000821:
            case 802000823:
                zz = MapleSquadType.aufheben;
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                zz = MapleSquadType.vonleon;
                break;
            case 551030200:
                zz = MapleSquadType.scartar;
                break;
            case 271040100:
                zz = MapleSquadType.cygnus;
                break;
            case 689013000:
                zz = MapleSquadType.pinkzak;
                break;
            case 262030300:
            case 262031300:
            case 262031310:
                zz = MapleSquadType.hillah;
                break;
            case 272030400:
            case 272030420:
                zz = MapleSquadType.arkarium;
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(this.channel).getMapleSquad(zz);
    }

    public MapleSquad getSquadBegin() {
        if (this.squad != null) {
            return ChannelServer.getInstance(this.channel).getMapleSquad(this.squad);
        }
        return null;
    }

    public EventManager getEMByMap() {
        String em;
        switch (this.mapid) {
            case 105100400:
                em = "BossBalrog_EASY";
                break;
            case 105100300:
                em = "BossBalrog_NORMAL";
                break;
            case 280030000:
            case 280030100:
                em = "ZakumBattle";
                break;
            case 240060200:
                em = "HorntailBattle";
                break;
            case 280030001:
                em = "ChaosZakum";
                break;
            case 240060201:
                em = "ChaosHorntail";
                break;
            case 270050100:
                em = "PinkBeanBattle";
                break;
            case 270051100:
                em = "ChaosPinkBean";
                break;
            case 802000111:
                em = "NamelessMagicMonster";
                break;
            case 802000211:
                em = "Vergamot";
                break;
            case 802000311:
                em = "2095_tokyo";
                break;
            case 802000411:
                em = "Dunas";
                break;
            case 802000611:
                em = "Nibergen";
                break;
            case 802000711:
                em = "Dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                em = "CoreBlaze";
                break;
            case 802000821:
            case 802000823:
                em = "Aufhaven";
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                em = "VonLeonBattle";
                break;
            case 551030200:
                em = "ScarTarBattle";
                break;
            case 271040100:
                em = "CygnusBattle";
                break;
            case 689013000:
                em = "PinkZakum";
                break;
            case 262031300:
            case 262031310:
                em = "Hillah_170";
                break;
            case 272030400:
            case 272030420:
                em = "ArkariumBattle";
                break;
            case 955000100:
            case 955000200:
            case 955000300:
                em = "AswanOffSeason";
                break;
            default:
                if (mapid >= 262020000 && mapid < 262023000) {
                    em = "Azwan";
                    break;
                }
                return null;
        }
        return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
    }

    /**
     * 移除玩家离开地图
     * @param chr
     */
    public void removePlayer(MapleCharacter chr) {
        if (this.everlast) {
            returnEverLastItem(chr);
        }
        this.charactersLock.writeLock().lock();
        try {
            this.characters.remove(chr);
        } finally {
            this.charactersLock.writeLock().unlock();
        }
        removeMapObject(chr);
        chr.removeExtractor();
        if (chr.getSidekick() != null) {
            MapleCharacter side = getCharacterById(chr.getSidekick().getCharacter(chr.getSidekick().getCharacter(0).getId() == chr.getId() ? 1 : 0).getId());
        }
        broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));

        if (chr.getSummonedFamiliar() != null) {
            chr.removeVisibleFamiliar();
        }

        List<MapleSummon> toCancel = new ArrayList();
        Map<Integer,MapleSummon> listSummons = chr.getSummonsReadLock();
        try {
            for (MapleSummon summon : listSummons.values()) {
                broadcastMessage(SummonPacket.removeSummon(summon, true));
                removeMapObject(summon);
                // 不能移动的召唤物，换地图后就没有了
                if ((summon.getMovementType() == SummonMovementType.不会移动) || (summon.getMovementType() == SummonMovementType.CIRCLE_STATIONARY) || (summon.getMovementType() == SummonMovementType.自由移动)) {
                    ((List) toCancel).add(summon);
                } else {
                    summon.setChangedMap(true);
                }
            }
        } finally {
            chr.unlockSummonsReadLock();
        }
        // @TODO 这里应该可以合并到上面去的
        for (MapleSummon summon : toCancel) {
            chr.removeSummon(summon.getSkillId());
            chr.dispelSkill(summon.getSkillId());
        }
        checkStates(chr.getName());
        if (this.mapid == 109020001) {
            chr.canTalk(true);
        }
        chr.leaveMap(this);
    }

    public void broadcastMessage(byte[] packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getTruePosition());
    }

    public void broadcastMessage(byte[] packet, Point rangedFrom) {
        broadcastMessage(null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public void broadcastMessage(MapleCharacter source, byte[] packet, Point rangedFrom) {
        broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public void broadcastMessage(MapleCharacter source, byte[] packet, double rangeSq, Point rangedFrom) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distance(chr.getTruePosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    private void sendObjectPlacement(MapleCharacter chr) {  //标记载入地图所有存在的实例  召唤兽第一次载入的包
        if (chr == null) {
            return;
        }
        for (MapleMapObject o : getMapObjectsInRange(chr.getTruePosition(), chr.getRange(), GameConstants.rangedMapobjectTypes)) {
            if ((o.getType() == MapleMapObjectType.REACTOR) && (!((MapleReactor) o).isAlive())) {
                continue;
            }
            o.sendSpawnData(chr.getClient());
            chr.addVisibleMapObject(o);
        }
    }

    public List<MaplePortal> getPortalsInRange(Point from, double rangeSq) {
        List ret = new ArrayList();
        for (MaplePortal type : this.portals.values()) {
            if ((from.distanceSq(type.getPosition()) <= rangeSq) && (type.getTargetMapId() != this.mapid) && (type.getTargetMapId() != 999999999)) {
                ret.add(type);
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator<MapleMapObject> itr = mapobjects.get(type).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        return getMapObjectsInRange(from, rangeSq, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.ITEM}));
    }

    public List<MapleMapObject> getMonstersInRange(Point from, double rangeSq) {
        return getMapObjectsInRange(from, rangeSq, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.MONSTER}));
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> MapObject_types) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapObject_types) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    /**
     * 获取矩形中的怪物信息
     * @param box
     * @param MapObject_types
     * @return
     */
    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> MapObject_types) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapObject_types) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (box.contains(mmo.getTruePosition())) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleCharacter> getCharactersIntersect(Rectangle box) {
        List ret = new ArrayList();
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getBounds().intersects(box)) {
                    ret.add(chr);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }

    public List<MapleCharacter> getPlayersInRectAndInList(Rectangle box, List<MapleCharacter> chrList) {
        List character = new LinkedList();

        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter a : this.characters) {
                if ((chrList.contains(a)) && (box.contains(a.getTruePosition()))) {
                    character.add(a);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return character;
    }

    public void addPortal(MaplePortal myPortal) {
        this.portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : this.portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return (MaplePortal) this.portals.get(portalid);
    }

    public void resetPortals() {
        for (MaplePortal port : this.portals.values()) {
            port.setPortalState(true);
        }
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return this.footholds;
    }

    public int getNumSpawnPoints() {
        return this.monsterSpawn.size();
    }

    public void loadMonsterRate(boolean first) {
        int spawnSize = this.monsterSpawn.size();
        if ((spawnSize >= 20) || (this.partyBonusRate > 0)) {
            this.maxRegularSpawn = Math.round(spawnSize / this.monsterRate);
        } else {
            this.maxRegularSpawn = (int) Math.ceil(spawnSize * this.monsterRate);
        }
        if (this.fixedMob > 0) {
            this.maxRegularSpawn = this.fixedMob;
        } else if (this.maxRegularSpawn <= 2) {
            this.maxRegularSpawn = 2;
        } else if (this.maxRegularSpawn > spawnSize) {
            this.maxRegularSpawn = Math.max(10, spawnSize);
        }

        Collection newSpawn = new LinkedList();
        Collection newBossSpawn = new LinkedList();
        for (Spawns s : this.monsterSpawn) {
            if (s.getCarnivalTeam() >= 2) {
                continue;
            }
            if (s.getMonster().isBoss()) {
                newBossSpawn.add(s);
            } else {
                newSpawn.add(s);
            }
        }
        this.monsterSpawn.clear();
        this.monsterSpawn.addAll(newBossSpawn);
        this.monsterSpawn.addAll(newSpawn);

        if ((first) && (spawnSize > 0)) {
            this.lastSpawnTime = System.currentTimeMillis();
            if (GameConstants.isForceRespawn(this.mapid)) {
                this.createMobInterval = 15000;
            }
            respawn(false); // this should do the trick, we don't need to wait upon entering map
        }
    }

    public SpawnPoint addMonsterSpawn(MapleMonster monster, int mobTime, byte carnivalTeam, String msg) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, carnivalTeam, msg);
        if (carnivalTeam > -1) {
            this.monsterSpawn.add(0, sp);
        } else {
            this.monsterSpawn.add(sp);
        }
        return sp;
    }

    public void addAreaMonsterSpawn(MapleMonster monster, Point pos1, Point pos2, Point pos3, int mobTime, String msg, boolean shouldSpawn, boolean sendWorldMsg) {
        pos1 = calcPointBelow(pos1);
        pos2 = calcPointBelow(pos2);
        pos3 = calcPointBelow(pos3);
        if (pos1 != null) {
            pos1.y -= 1;
        }
        if (pos2 != null) {
            pos2.y -= 1;
        }
        if (pos3 != null) {
            pos3.y -= 1;
        }
        if ((pos1 == null) && (pos2 == null) && (pos3 == null)) {
            FileoutputUtil.log(new StringBuilder().append("WARNING: mapid ").append(this.mapid).append(", monster ").append(monster.getId()).append(" could not be spawned.").toString());
            return;
        }
        if (pos1 != null) {
            if (pos2 == null) {
                pos2 = new Point(pos1);
            }
            if (pos3 == null) {
                pos3 = new Point(pos1);
            }
        } else if (pos2 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos2);
            }
            if (pos3 == null) {
                pos3 = new Point(pos2);
            }
        } else if (pos3 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos3);
            }
            if (pos2 == null) {
                pos2 = new Point(pos3);
            }
        }
        this.monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg, shouldSpawn, sendWorldMsg));
    }

    public List<MapleCharacter> getCharacters() {
        return getCharactersThreadsafe();
    }

    public List<MapleCharacter> getCharactersThreadsafe() {
        List chars = new ArrayList();
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                chars.add(mc);
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return chars;
    }

    public MapleCharacter getCharacterByName(String id) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                if (mc.getName().equalsIgnoreCase(id)) {
                    MapleCharacter localMapleCharacter1 = mc;
                    return localMapleCharacter1;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }

    public MapleCharacter getCharacterById_InMap(int id) {
        return getCharacterById(id);
    }

    public MapleCharacter getCharacterById(int id) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                if (mc.getId() == id) {
                    MapleCharacter localMapleCharacter1 = mc;
                    return localMapleCharacter1;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }

    public void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (chr == null) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) {
            if ((mo.getType() == MapleMapObjectType.MIST) || (mo.getType() == MapleMapObjectType.EXTRACTOR) || (mo.getType() == MapleMapObjectType.SUMMON) || (mo.getType() == MapleMapObjectType.FAMILIAR) || ((mo instanceof MechDoor)) || (mo.getTruePosition().distance(chr.getTruePosition()) <= mo.getRange())) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else if (!(mo instanceof MechDoor) && (mo.getType() != MapleMapObjectType.MIST) && (mo.getType() != MapleMapObjectType.EXTRACTOR) && (mo.getType() != MapleMapObjectType.SUMMON) && (mo.getType() != MapleMapObjectType.FAMILIAR) && (mo.getTruePosition().distance(chr.getTruePosition()) > mo.getRange())) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
        } else if ((mo.getType() == MapleMapObjectType.MONSTER) && (chr.getTruePosition().distance(mo.getTruePosition()) <= mo.getRange())) {
            updateMonsterController((MapleMonster) mo);
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);

        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                updateMapObjectVisibility(mc, monster);
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        Collection<MapleMapObject> visibleObjects;
        try {
            visibleObjects = player.getAndWriteLockVisibleMapObjects();
            ArrayList copy = new ArrayList(visibleObjects);
            Iterator itr = copy.iterator();
            while (itr.hasNext()) {
                MapleMapObject mo = (MapleMapObject) itr.next();
                if ((mo != null) && (getMapObject(mo.getObjectId(), mo.getType()) == mo)) {
                    updateMapObjectVisibility(player, mo);
                } else if (mo != null) {
                    player.removeVisibleMapObject(mo);
                }
            }
            for (MapleMapObject mo : getMapObjectsInRange(player.getTruePosition(), player.getRange())) {
                if ((mo != null) && (!visibleObjects.contains(mo))) {
                    mo.sendSpawnData(player.getClient());
                    player.addVisibleMapObject(mo);
                }
            }
        } finally {
            // Collection visibleObjects;
            player.unlockWriteVisibleMapObjects();
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = getPortal(0);
        double shortestDistance = (1.0D / 0.0D);
        for (MaplePortal portal : this.portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if ((portal.getType() >= 0) && (portal.getType() <= 2) && (distance < shortestDistance) && (portal.getTargetMapId() == 999999999)) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = getPortal(0);
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : this.portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public String spawnDebug() {
        StringBuilder sb = new StringBuilder("Mobs in map : ");
        sb.append(getMobsSize());
        sb.append(" spawnedMonstersOnMap: ");
        sb.append(this.spawnedMonstersOnMap);
        sb.append(" spawnpoints: ");
        sb.append(this.monsterSpawn.size());
        sb.append(" maxRegularSpawn: ");
        sb.append(this.maxRegularSpawn);
        sb.append(" actual monsters: ");
        sb.append(getNumMonsters());
        sb.append(" monster rate: ");
        sb.append(this.monsterRate);
        sb.append(" fixed: ");
        sb.append(this.fixedMob);

        return sb.toString();
    }

    public int getMapObjectSize() {
        return this.mapobjects.size();
    }

    public int getCharactersSize() {
        this.charactersLock.readLock().lock();
        try {
            int i = this.characters.size();
            return i;
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(this.portals.values());
    }

    public int getSpawnedMonstersOnMap() {
        return this.spawnedMonstersOnMap.get();
    }

    public void respawn(boolean force) {
        respawn(force, System.currentTimeMillis());
    }

    public void respawn(boolean force, long now) {
        lastSpawnTime = now;
        int numShouldSpawn;
        int spawned;
        if (force) {
            numShouldSpawn = this.monsterSpawn.size() - this.spawnedMonstersOnMap.get();
            if (numShouldSpawn > 0) {
                spawned = 0;
                for (Spawns spawnPoint : this.monsterSpawn) {
                    spawnPoint.spawnMonster(this);
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } else {
            numShouldSpawn = (GameConstants.isForceRespawn(this.mapid) ? this.monsterSpawn.size() : this.maxRegularSpawn) - this.spawnedMonstersOnMap.get();
            if (numShouldSpawn > 0) {
                spawned = 0;
                List<Spawns> randomSpawn = new ArrayList(this.monsterSpawn);

                Collections.shuffle(randomSpawn);
                for (Spawns spawnPoint : randomSpawn) {
                    if (!isSpawns && (spawnPoint.getMobTime() > 0)) {
                        continue;
                    }
                    if ((spawnPoint.shouldSpawn(lastSpawnTime)) || (GameConstants.isForceRespawn(mapid)) || (monsterSpawn.size() < 10 && this.maxRegularSpawn > monsterSpawn.size() && this.partyBonusRate > 0)) {
                        spawnPoint.spawnMonster(this);
                        spawned++;
                    }
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        }
    }

    public String getSnowballPortal() {
        int[] teamss = new int[2];
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getTruePosition().y > -80) {
                    teamss[0] += 1;
                } else {
                    teamss[1] += 1;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        if (teamss[0] > teamss[1]) {
            return "st01";
        }
        return "st00";
    }

    public boolean isDisconnected(int id) {
        return this.dced.contains(id);
    }

    public void addDisconnected(int id) {
        this.dced.add(id);
    }

    public void resetDisconnected() {
        this.dced.clear();
    }

    public void startSpeedRun() {
        MapleSquad squads = getSquadByMap();
        if (squads != null) {
            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter chr : this.characters) {
                    if ((chr.getName().equals(squads.getLeaderName())) && (!chr.isIntern())) {
                        startSpeedRun(chr.getName());
                        return;
                    }
                }
            } finally {
                this.charactersLock.readLock().unlock();
            }
        }
    }

    public void startSpeedRun(String leader) {
        this.speedRunStart = System.currentTimeMillis();
        this.speedRunLeader = leader;
    }

    public void endSpeedRun() {
        this.speedRunStart = 0L;
        this.speedRunLeader = "";
    }

    public void getRankAndAdd(String leader, String time, ExpeditionType type, long timz, Collection<String> squad) {
        try {
            long lastTime = SpeedRunner.getSpeedRunData(type) == null ? 0L : (SpeedRunner.getSpeedRunData(type).right);
            StringBuilder rett = new StringBuilder();
            if (squad != null) {
                for (String chr : squad) {
                    rett.append(chr);
                    rett.append(",");
                }
            }
            String z = rett.toString();
            if (squad != null) {
                z = z.substring(0, z.length() - 1);
            }
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO speedruns(`type`, `leader`, `timestring`, `time`, `members`) VALUES (?,?,?,?,?)")) {
                ps.setString(1, type.name());
                ps.setString(2, leader);
                ps.setString(3, time);
                ps.setLong(4, timz);
                ps.setString(5, z);
                ps.executeUpdate();
                ps.close();
            }

            if (lastTime == 0L) {
                SpeedRunner.addSpeedRunData(type, SpeedRunner.addSpeedRunData(new StringBuilder(SpeedRunner.getPreamble(type)), new HashMap(), z, leader, 1, time), timz);
            } else {
                SpeedRunner.removeSpeedRunData(type);
                SpeedRunner.loadSpeedRunData(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getSpeedRunStart() {
        return this.speedRunStart;
    }

    public void disconnectAll() {
        for (MapleCharacter chr : getCharactersThreadsafe()) {
            if (!chr.isGM()) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close(true);
            }
        }
    }

    public List<MapleNPC> getAllNPCs() {
        return getAllNPCsThreadsafe();
    }

    public List<MapleNPC> getAllNPCsThreadsafe() {
        ArrayList ret = new ArrayList();
        mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.NPC).values()) {
                ret.add((MapleNPC) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
        return ret;
    }

    public List<MapleArrowsTurret> getAllArrowsTurrets() {
        return getArrowsTurretsThreadsafe();
    }

    public List<MapleArrowsTurret> getArrowsTurretsThreadsafe() {
        ArrayList ret = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ARROWS_TURRET)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ARROWS_TURRET).values()) {
                ret.add((MapleArrowsTurret) mmo);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ARROWS_TURRET)).readLock().unlock();
        }
        return ret;
    }

    public void resetNPCs() {
        removeNpc(-1);
    }

    public void resetPQ(int level) {
        resetFully();
        for (MapleMonster mons : getAllMonstersThreadsafe()) {
            mons.getStats().setChange(true);
            mons.changeLevel(level, true);
        }
        resetSpawnLevel(level);
    }

    public void resetSpawnLevel(int level) {
        for (Spawns spawn : this.monsterSpawn) {
            if ((spawn instanceof SpawnPoint)) {
                ((SpawnPoint) spawn).setLevel(level);
            }
        }
    }

    public void resetFully() {
        resetFully(true);
    }

    public void resetFully(boolean respawn) {
        killAllMonsters(false);
        reloadReactors();
        removeDrops();
        resetNPCs();
        resetSpawns();
        resetDisconnected();
        endSpeedRun();
        cancelSquadSchedule(true);
        resetPortals();
        this.environment.clear();
        if (respawn) {
            respawn(true);
        }
    }

    public void cancelSquadSchedule(boolean interrupt) {
        this.squadTimer = false;
        this.checkStates = true;
        if (this.squadSchedule != null) {
            this.squadSchedule.cancel(interrupt);
            this.squadSchedule = null;
        }
    }

    public void removeDrops() {
        List<MapleMapItem> mapItems = getAllItemsThreadsafe();
        for (MapleMapItem mapItem : mapItems) {
            mapItem.expire(this);
        }
    }

    public void removeDropsDelay() {
        List<MapleMapItem> mapItems = getAllItemsThreadsafe();
        int delay = 0;
        int i = 0;
        for (MapleMapItem mapItem : mapItems) {
            i++;
            if (i < 50) {
                mapItem.expire(this);
            } else {
                delay++;
                if (mapItem.hasFFA()) {
                    mapItem.registerFFA(delay * 20);
                } else {
                    mapItem.registerExpire(delay * 30);
                }
            }
        }
    }

    public void resetAllSpawnPoint(int mobid, int mobTime) {
        Collection<Spawns> AllSpawnPoints = new LinkedList(this.monsterSpawn);
        resetFully();
        this.monsterSpawn.clear();
        for (Spawns spawnPoint : AllSpawnPoints) {
            MapleMonster newMons = MapleLifeFactory.getMonster(mobid);
            newMons.setF(spawnPoint.getF());
            newMons.setFh(spawnPoint.getFh());
            newMons.setPosition(spawnPoint.getPosition());
            addMonsterSpawn(newMons, mobTime, (byte) -1, null);
        }
        loadMonsterRate(true);
    }

    public void resetSpawns() {
        boolean changed = false;
        Iterator AllSpawnPoints = this.monsterSpawn.iterator();
        while (AllSpawnPoints.hasNext()) {
            if (((Spawns) AllSpawnPoints.next()).getCarnivalId() > -1) {
                AllSpawnPoints.remove();
                changed = true;
            }
        }
        setSpawns(true);
        if (changed) {
            loadMonsterRate(true);
        }
    }

    public boolean makeCarnivalSpawn(int team, MapleMonster newMons, int num) {
        MapleNodes.MonsterPoint ret = null;
        for (MapleNodes.MonsterPoint mp : this.nodes.getMonsterPoints()) {
            if ((mp.team == team) || (mp.team == -1)) {
                Point newpos = calcPointBelow(new Point(mp.x, mp.y));
                newpos.y -= 1;
                boolean found = false;
                for (Spawns s : this.monsterSpawn) {
                    if ((s.getCarnivalId() > -1) && ((mp.team == -1) || (s.getCarnivalTeam() == mp.team)) && (s.getPosition().x == newpos.x) && (s.getPosition().y == newpos.y)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ret = mp;
                    break;
                }
            }
        }
        if (ret != null) {
            newMons.setCy(ret.cy);
            newMons.setF(0);
            newMons.setFh(ret.fh);
            newMons.setRx0(ret.x + 50);
            newMons.setRx1(ret.x - 50);
            newMons.setPosition(new Point(ret.x, ret.y));
            newMons.setHide(false);
            SpawnPoint sp = addMonsterSpawn(newMons, 1, (byte) team, null);
            sp.setCarnival(num);
        }
        return ret != null;
    }

    public boolean makeCarnivalReactor(int team, int num) {
        MapleReactor old = getReactorByName(new StringBuilder().append(team).append("").append(num).toString());
        if ((old != null) && (old.getState() < 5)) {
            return false;
        }
        Point guardz = null;
        List<MapleReactor> react = getAllReactorsThreadsafe();
        for (Pair guard : this.nodes.getGuardians()) {
            if ((((Integer) guard.right) == team) || (((Integer) guard.right) == -1)) {
                boolean found = false;
                for (MapleReactor r : react) {
                    if ((r.getTruePosition().x == ((Point) guard.left).x) && (r.getTruePosition().y == ((Point) guard.left).y) && (r.getState() < 5)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    guardz = (Point) guard.left;
                    break;
                }
            }
        }
        MapleCarnivalFactory.MCSkill skil;
        if (guardz != null) {
            MapleReactor my = new MapleReactor(MapleReactorFactory.getReactor(9980000 + team), 9980000 + team);
            my.setState((byte) 1);
            my.setName(new StringBuilder().append(team).append("").append(num).toString());

            spawnReactorOnGroundBelow(my, guardz);
            skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            for (MapleMonster mons : getAllMonstersThreadsafe()) {
                if (mons.getCarnivalTeam() == team) {
                    skil.getSkill().applyEffect(null, mons, false);
                }
            }
        }
        return guardz != null;
    }

    public void blockAllPortal() {
        for (MaplePortal p : this.portals.values()) {
            p.setPortalState(false);
        }
    }

    public boolean getAndSwitchTeam() {
        return getCharactersSize() % 2 != 0;
    }

    public void setSquad(MapleSquadType s) {
        this.squad = s;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getConsumeItemCoolTime() {
        return this.consumeItemCoolTime;
    }

    public void setConsumeItemCoolTime(int ciit) {
        this.consumeItemCoolTime = ciit;
    }

    public void setPermanentWeather(int pw) {
        this.permanentWeather = pw;
    }

    public int getPermanentWeather() {
        return this.permanentWeather;
    }

    public void checkStates(String chr) {
        if (!this.checkStates) {
            return;
        }
        MapleSquad sqd = getSquadByMap();
        EventManager em = getEMByMap();
        int size = getCharactersSize();
        if ((sqd != null) && (sqd.getStatus() == 2)) {
            sqd.removeMember(chr);
            if (em != null) {
                if (sqd.getLeaderName().equalsIgnoreCase(chr)) {
                    em.setProperty("leader", "false");
                }
                if ((chr.equals("")) || (size == 0)) {
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
                    cancelSquadSchedule(!chr.equals(""));
                    sqd.clear();
                    sqd.copy();
                }
            }
        }
        if ((em != null) && (em.getProperty("state") != null) && ((sqd == null) || (sqd.getStatus() == 2)) && (size == 0)) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
                em.setProperty("leader", "true");
            }
        }
        if ((this.speedRunStart > 0L) && (size == 0)) {
            endSpeedRun();
        }
    }

    public void setCheckStates(boolean b) {
        this.checkStates = b;
    }

    public void setNodes(MapleNodes mn) {
        this.nodes = mn;
    }

    public List<MapleNodes.MaplePlatform> getPlatforms() {
        return this.nodes.getPlatforms();
    }

    public Collection<MapleNodes.MapleNodeInfo> getNodes() {
        return this.nodes.getNodes();
    }

    public MapleNodes.MapleNodeInfo getNode(int index) {
        return this.nodes.getNode(index);
    }

    public boolean isLastNode(int index) {
        return this.nodes.isLastNode(index);
    }

    public List<Rectangle> getAreas() {
        return this.nodes.getAreas();
    }

    public Rectangle getArea(int index) {
        return this.nodes.getArea(index);
    }

    public void changeEnvironment(String ms, int type) {
        broadcastMessage(MaplePacketCreator.environmentChange(ms, type));
    }

    public void toggleEnvironment(String ms) {
        if (this.environment.containsKey(ms)) {
            moveEnvironment(ms, (this.environment.get(ms)) == 1 ? 2 : 1);
        } else {
            moveEnvironment(ms, 1);
        }
    }

    public void moveEnvironment(String ms, int type) {
        broadcastMessage(MaplePacketCreator.environmentMove(ms, type));
        this.environment.put(ms, type);
    }

    public Map<String, Integer> getEnvironment() {
        return this.environment;
    }

    public int getNumPlayersInArea(int index) {
        return getNumPlayersInRect(getArea(index));
    }

    public int getNumPlayersInRect(Rectangle rect) {
        int ret = 0;
        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                if (rect.contains(((MapleCharacter) ltr.next()).getTruePosition())) {
                    ret++;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }

    public int getNumPlayersItemsInArea(int index) {
        return getNumPlayersItemsInRect(getArea(index));
    }

    public int getNumPlayersItemsInRect(Rectangle rect) {
        int ret = getNumPlayersInRect(rect);
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (rect.contains(mmo.getTruePosition())) {
                    ret++;
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().unlock();
        }
        return ret;
    }

    public void broadcastGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet);
    }

    private void broadcastGMMessage(MapleCharacter source, byte[] packet) {
        this.charactersLock.readLock().lock();
        try {
            if (source == null) {
                for (MapleCharacter chr : this.characters) {
                    if (chr.isStaff()) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            } else {
                for (MapleCharacter chr : this.characters) {
                    if ((chr != source) && (chr.getGMLevel() >= source.getGMLevel())) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public void broadcastNONGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastNONGMMessage(repeatToSource ? null : source, packet);
    }

    private void broadcastNONGMMessage(MapleCharacter source, byte[] packet) {
        charactersLock.readLock().lock();
        try {
            if (source == null) {
                for (MapleCharacter chr : characters) {
                    if (!chr.isStaff()) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            } else {
                for (MapleCharacter chr : characters) {
                    if (chr != source && (chr.getGMLevel() < source.getGMLevel())) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    public List<Pair<Integer, Integer>> getMobsToSpawn() {
        return this.nodes.getMobsToSpawn();
    }

    public List<Integer> getSkillIds() {
        return this.nodes.getSkillIds();
    }

    public boolean canSpawn(long now) {
        return (this.lastSpawnTime > 0L) && (this.lastSpawnTime + this.createMobInterval < now);
    }

    public boolean canHurt(long now) {
        if ((this.lastHurtTime > 0L) && (this.lastHurtTime + this.decHPInterval < now)) {
            this.lastHurtTime = now;
            return true;
        }
        return false;
    }

    public void resetShammos(final MapleClient c) {
        killAllMonsters(true);
        broadcastMessage(MaplePacketCreator.serverMessageNotice("A player has moved too far from Shammos. Shammos is going back to the start."));
        EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (c.getPlayer() != null) {
                    c.getPlayer().changeMap(MapleMap.this, MapleMap.this.getPortal(0));
                    if (MapleMap.this.getCharactersThreadsafe().size() > 1) {
                        MapScriptMethods.startScript_FirstUser(c, "shammos_Fenter");
                    }
                }
            }
        }, 500L);
    }

    public int getInstanceId() {
        return this.instanceid;
    }

    public void setInstanceId(int ii) {
        this.instanceid = ii;
    }

    public int getPartyBonusRate() {
        return this.partyBonusRate;
    }

    public void setPartyBonusRate(int ii) {
        this.partyBonusRate = ii;
    }

    public short getTop() {
        return this.top;
    }

    public short getBottom() {
        return this.bottom;
    }

    public short getLeft() {
        return this.left;
    }

    public short getRight() {
        return this.right;
    }

    public void setTop(int ii) {
        this.top = (short) ii;
    }

    public void setBottom(int ii) {
        this.bottom = (short) ii;
    }

    public void setLeft(int ii) {
        this.left = (short) ii;
    }

    public void setRight(int ii) {
        this.right = (short) ii;
    }

    public List<Pair<Point, Integer>> getGuardians() {
        return this.nodes.getGuardians();
    }

    public MapleNodes.DirectionInfo getDirectionInfo(int i) {
        return this.nodes.getDirection(i);
    }

    public void AutoNx(int jsNx, boolean isAutoPoints) {
        if (this.mapid != 910000000) {
            return;
        }
        for (MapleCharacter chr : this.characters) {
            if (chr != null) {
                if (isAutoPoints) {
                    chr.gainPlayerPoints(10);
                    chr.dropMessage(5, "[系统奖励] 在线时间奖励获得 [10] 点积分.");
                } else {
                    int givNx = chr.getLevel() / 10 + jsNx;
                    chr.modifyCSPoints(2, givNx);
                    chr.dropMessage(5, new StringBuilder().append("[系统奖励] 在线时间奖励获得 [").append(givNx).append("] 点抵用券.").toString());
                }
            }
        }
    }

    public void AutoGain(int rate, int expRate) {
        for (MapleCharacter chr : this.characters) {
            if (chr == null) {
                return;
            }
            int giveP;
            if (Randomizer.isSuccess(40)) {
                giveP = Randomizer.rand(4, 5) * 3;
            } else {
                giveP = Randomizer.rand(2, 3) * 3;
            }
            giveP *= rate;
            chr.modifyCSPoints(2, giveP);
            String msg = "在线奖励 [" + giveP + "] 抵用券";
            if (chr.getLevel() < 255) {
                int givExp = chr.getLevel() <= 10 ? chr.getLevel() ^ 5 : chr.getLevel() * expRate * Randomizer.rand(8, 12);
                givExp *= rate;
                chr.gainExp(givExp, true, false, true);
                msg += "和 [" + givExp + "] 经验";
            }
            if (rate > 1) {
                msg += "    （" + rate + "倍）";
            }
            chr.dropMessage(-5, msg);
        }
    }

    public boolean isMarketMap() {
        return (this.mapid >= 910000000) && (this.mapid <= 910000017);
    }

    public boolean isBossMap() {
        switch (this.mapid) {
            case 105100300:
            case 105100400:
            case 211070100:
            case 211070101:
            case 211070110:
            case 220080001:
            case 240040700:
            case 240060200:
            case 240060201:
            case 262031300:
            case 262031310:
            case 270050100:
            case 271040100:
            case 271040200:
            case 272030400:
            case 272030420:
            case 280030000:
            case 280030001:
            case 280030100:
            case 300030310:
            case 551030200:
            case 802000111:
            case 802000211:
            case 802000311:
            case 802000411:
            case 802000611:
            case 802000711:
            case 802000801:
            case 802000802:
            case 802000803:
            case 802000821:
            case 802000823:
                return true;
        }
        return false;
    }

    private int hasBoat() {
        return docked ? 2 : (boat ? 1 : 0);
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    /**
     * 设置是否可以停靠船只
     * @param isDocked
     */
    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }

    public void checkMoveMonster(Point from, boolean fly, MapleCharacter chr) {
        if ((this.maxRegularSpawn <= 2) || (this.monsterSpawn.isEmpty()) || (this.monsterRate <= 1.0D) || (chr == null)) {
            return;
        }
        //检测吸怪
        /*int check = (int) ((fly ? 70 : 60) / 100.0D * this.maxRegularSpawn);

         if (getMonstersInRange(from, 5000.0D).size() >= check) {
         for (MapleMapObject obj : getMonstersInRange(from, (1.0D / 0.0D))) {
         MapleMonster mob = (MapleMonster) obj;
         killMonster(mob, chr, false, false, (byte) 1);
         }
         }*/
    }

    private static abstract interface DelayedPacketCreation {

        public abstract void sendPackets(MapleClient paramMapleClient);
    }

    private class ActivateItemReactor implements Runnable {

        private final MapleMapItem mapitem;
        private final MapleReactor reactor;
        private final MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if ((this.mapitem != null) && (this.mapitem == MapleMap.this.getMapObject(this.mapitem.getObjectId(), this.mapitem.getType())) && (!this.mapitem.isPickedUp())) {
                this.mapitem.expire(MapleMap.this);
                this.reactor.hitReactor(this.c);
                this.reactor.setTimerActive(false);

                if (this.reactor.getDelay() > 0) {
                    MapTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            MapleMap.ActivateItemReactor.this.reactor.forceHitReactor((byte) 0);
                        }
                    }, this.reactor.getDelay());
                }

            } else {
                this.reactor.setTimerActive(false);
            }
        }
    }
}
