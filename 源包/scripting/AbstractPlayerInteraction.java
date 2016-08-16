package scripting;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.WorldBroadcastService;
import handling.world.WorldGuildService;
import handling.world.guild.MapleGuild;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import scripting.npc.NPCScriptManager;
import server.MapleCarnivalChallenge;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructItemOption;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.DateUtil;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.packet.InventoryPacket;
import tools.packet.NPCPacket;
import tools.packet.PetPacket;
import tools.packet.UIPacket;

public abstract class AbstractPlayerInteraction {

    private static final Logger log = Logger.getLogger(AbstractPlayerInteraction.class);
    protected MapleClient c;
    protected int id;
    protected String script;
    private static final Map<Pair<Integer, MapleClient>, MapleNPC> npcRequestController = new HashMap<>();

    public AbstractPlayerInteraction(MapleClient c) {
        this.c = c;
        this.id = 0;
        this.script = null;
    }

    public AbstractPlayerInteraction(MapleClient c, int id, String script) {
        this.c = c;
        this.id = id;
        this.script = script;
    }

    public MapleClient getClient() {
        return this.c;
    }

    public MapleCharacter getPlayer() {
        return this.c.getPlayer();
    }

    public ChannelServer getChannelServer() {
        return this.c.getChannelServer();
    }

    public EventManager getEventManager(String event) {
        return this.c.getChannelServer().getEventSM().getEventManager(event);
    }

    public EventInstanceManager getEventInstance() {

        return this.c.getPlayer().getEventInstance();
    }

    public void warp_rand(int mapId) {
        MapleMap mapz = getWarpMap(mapId);
        try {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
        } catch (Exception e) {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public void warp(int mapId) {
        warp(mapId,0);
    }

    /**
     * 传送到地图
     * @param mapId
     * @param portal
     */
    public void warp(int mapId, int portal) {
        MapleMap mapz = getWarpMap(mapId);
        FileoutputUtil.log("准备传送到地图："+mapId);
        if ((portal != 0) && (mapId == this.c.getPlayer().getMapId())) {
            FileoutputUtil.log("准备地图内传送："+mapId);
            Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(getPlayer().getTruePosition()) < 90000.0D) {
                this.c.getSession().write(MaplePacketCreator.instantMapWarp((byte) portal));
                this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), portalPos);
            } else {
                this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            FileoutputUtil.log("准备跨地图传送："+mapId);
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public void warp(int mapId, String portal) {
        MapleMap mapz = getWarpMap(mapId);
        if ((mapId == 109060000) || (mapId == 109060002) || (mapId == 109060004)) {
            portal = mapz.getSnowballPortal();
        }
        if (mapId == this.c.getPlayer().getMapId()) {
            Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(getPlayer().getTruePosition()) < 90000.0D) {
                this.c.getSession().write(MaplePacketCreator.instantMapWarp((byte) this.c.getPlayer().getMap().getPortal(portal).getId()));
                this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition()));
            } else {
                this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public void warpMap(int mapId, int portal) {
        MapleMap map = getMap(mapId);
        for (MapleCharacter chr : this.c.getPlayer().getMap().getCharactersThreadsafe()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public void playPortalSE() {
        this.c.getSession().write(MaplePacketCreator.showOwnBuffEffect(0, 8, 1, 1));//7+1 119
    }

    private MapleMap getWarpMap(int mapId) {
        return ChannelServer.getInstance(this.c.getChannel()).getMapFactory().getMap(mapId);
    }

    public MapleMap getMap() {
        return this.c.getPlayer().getMap();
    }

    public MapleMap getMap(int mapId) {
        return getWarpMap(mapId);
    }

    public MapleMap getMap_Instanced(int mapId) {
        return this.c.getPlayer().getEventInstance() == null ? getMap(mapId) : this.c.getPlayer().getEventInstance().getMapInstance(mapId);
    }

    public void spawnMobLevel(int mobId, int level) {
        spawnMobLevel(mobId, 1, level, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobLevel(int mobId, int quantity, int level) {
        spawnMobLevel(mobId, quantity, level, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobLevel(int mobId, int quantity, int level, int x, int y) {
        spawnMobLevel(mobId, quantity, level, new Point(x, y));
    }

    public void spawnMobLevel(int mobId, int quantity, int level, Point pos) {
        for (int i = 0; i < quantity; i++) {
            MapleMonster mob = MapleLifeFactory.getMonster(mobId);
            if ((mob == null) || (!mob.getStats().isChangeable())) {
                if (this.c.getPlayer().isShowPacket()) {
                    this.c.getPlayer().dropMessage(-11, new StringBuilder().append("[系统提示] spawnMobLevel召唤怪物出错，ID为: ").append(mobId).append(" 怪物不存在或者该怪物无法使用这个函数来改变怪物的属性！").toString());
                }
            } else {
                mob.changeLevel(level, false);
                this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, pos);
            }
        }
    }

    public void spawnMobStats(int mobId, long newhp, int newExp) {
        spawnMobStats(mobId, 1, newhp, newExp, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobStats(int mobId, int quantity, long newhp, int newExp) {
        spawnMobStats(mobId, quantity, newhp, newExp, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobStats(int mobId, int quantity, long newhp, int newExp, int x, int y) {
        spawnMobStats(mobId, quantity, newhp, newExp, new Point(x, y));
    }

    public void spawnMobStats(int mobId, int quantity, long newhp, int newExp, Point pos) {
        for (int i = 0; i < quantity; i++) {
            MapleMonster mob = MapleLifeFactory.getMonster(mobId);
            if (mob == null) {
                if (this.c.getPlayer().isShowPacket()) {
                    this.c.getPlayer().dropMessage(-11, new StringBuilder().append("[系统提示] spawnMobStats召唤怪物出错，ID为: ").append(mobId).append(" 怪物不存在！").toString());
                }
            } else {
                OverrideMonsterStats overrideStats = new OverrideMonsterStats(newhp, mob.getMobMaxMp(), newExp <= 0 ? mob.getMobExp() : newExp, false);
                mob.setOverrideStats(overrideStats);
                this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, pos);
            }
        }
    }

    public void spawnMobMultipler(int mobId, int multipler) {
        spawnMobMultipler(mobId, 1, multipler, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobMultipler(int mobId, int quantity, int multipler) {
        spawnMobMultipler(mobId, quantity, multipler, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobMultipler(int mobId, int quantity, int multipler, int x, int y) {
        spawnMobMultipler(mobId, quantity, multipler, new Point(x, y));
    }

    public void spawnMobMultipler(int mobId, int quantity, int multipler, Point pos) {
        for (int i = 0; i < quantity; i++) {
            MapleMonster mob = MapleLifeFactory.getMonster(mobId);
            if (mob == null) {
                if (this.c.getPlayer().isShowPacket()) {
                    this.c.getPlayer().dropMessage(-11, new StringBuilder().append("[系统提示] spawnMobMultipler召唤怪物出错，ID为: ").append(mobId).append(" 怪物不存在！").toString());
                }
            } else {
                OverrideMonsterStats overrideStats = new OverrideMonsterStats(mob.getMobMaxHp() * multipler, mob.getMobMaxMp() * multipler, mob.getMobExp() + multipler * 100, false);
                mob.setOverrideStats(overrideStats);
                this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, pos);
            }
        }
    }

    public void spawnMonster(int mobId, int quantity) {
        spawnMob(mobId, quantity, this.c.getPlayer().getTruePosition());
    }

    public void spawnMobOnMap(int mobId, int quantity, int x, int y, int map) {
        for (int i = 0; i < quantity; i++) {
            getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new Point(x, y));
        }
    }

    public void spawnMob(int mobId, int quantity, int x, int y) {
        spawnMob(mobId, quantity, new Point(x, y));
    }

    public void spawnMob(int mobId, int x, int y) {
        spawnMob(mobId, 1, new Point(x, y));
    }

    private void spawnMob(int mobId, int quantity, Point pos) {
        for (int i = 0; i < quantity; i++) {
            this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), pos);
        }
    }

    public void killMob(int mobId) {
        this.c.getPlayer().getMap().killMonster(mobId);
    }

    public void killAllMob() {
        this.c.getPlayer().getMap().killAllMonsters(true);
    }

    public void addHP(int delta) {
        this.c.getPlayer().addHP(delta);
    }

    /**
     * 获取角色的属性
     * @param type
     * @return
     */
    public int getPlayerStat(String type) {
        if (type.equals("LVL")) {
            return this.c.getPlayer().getLevel();
        }
        if (type.equals("STR")) {
            return this.c.getPlayer().getStat().getStr();
        }
        if (type.equals("DEX")) {
            return this.c.getPlayer().getStat().getDex();
        }
        if (type.equals("INT")) {
            return this.c.getPlayer().getStat().getInt();
        }
        if (type.equals("LUK")) {
            return this.c.getPlayer().getStat().getLuk();
        }
        if (type.equals("HP")) {
            return this.c.getPlayer().getStat().getHp();
        }
        if (type.equals("MP")) {
            return this.c.getPlayer().getStat().getMp();
        }
        if (type.equals("MAXHP")) {
            return this.c.getPlayer().getStat().getMaxHp();
        }
        if (type.equals("MAXMP")) {
            return this.c.getPlayer().getStat().getMaxMp();
        }
        if (type.equals("RAP")) {
            return this.c.getPlayer().getRemainingAp();
        }
        if (type.equals("RSP")) {
            return this.c.getPlayer().getRemainingSp();
        }
        if (type.equals("GID")) {
            return this.c.getPlayer().getGuildId();
        }
        if (type.equals("GRANK")) {
            return this.c.getPlayer().getGuildRank();
        }
        if (type.equals("ARANK")) {
            return this.c.getPlayer().getAllianceRank();
        }
        if (type.equals("GM")) {
            return this.c.getPlayer().isGM() ? 1 : 0;
        }
        if (type.equals("ADMIN")) {
            return this.c.getPlayer().isAdmin() ? 1 : 0;
        }
        if (type.equals("GENDER")) {
            return this.c.getPlayer().getGender();
        }
        if (type.equals("FACE")) {
            return this.c.getPlayer().getFace();
        }
        if (type.equals("HAIR")) {
            return this.c.getPlayer().getHair();
        }
        return -1;
    }

    public String getName() {
        return this.c.getPlayer().getName();
    }

    public String getServerName() {
        return this.c.getPlayer().getClient().getChannelServer().getServerName();
    }

    public String getTrueServerName() {
        return this.c.getPlayer().getClient().getChannelServer().getTrueServerName();
    }

    public boolean haveItem(int itemId) {
        return haveItem(itemId, 1);
    }

    public boolean haveItem(int itemId, int quantity) {
        return haveItem(itemId, quantity, false, true);
    }

    public boolean haveItem(int itemId, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        return c.getPlayer().haveItem(itemId, quantity, checkEquipped, greaterOrEquals);
    }

    public int getItemQuantity(int itemId) {
        return c.getPlayer().getItemQuantity(itemId);
    }

    public boolean canHold() {
        return this.c.getPlayer().canHold();
    }

    public boolean canHoldSlots(int slot) {
        return this.c.getPlayer().canHoldSlots(slot);
    }

    public boolean canHold(int itemId) {
        return this.c.getPlayer().canHold(itemId);
    }

    public boolean canHold(int itemId, int quantity) {
        return MapleInventoryManipulator.checkSpace(this.c, itemId, quantity, "");
    }

    public MapleQuestStatus getQuestRecord(int questId) {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questId));
    }

    public MapleQuestStatus getQuestNoRecord(int questId) {
        return this.c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(questId));
    }

    public byte getQuestStatus(int questId) {
        byte status = this.c.getPlayer().getQuestStatus(questId);
        FileoutputUtil.log("任务状态："+questId+"-"+status);
        return status;
    }

    public boolean isQuestCompleted(int questId){
        byte status = this.c.getPlayer().getQuestStatus(questId);
        FileoutputUtil.log("任务状态1："+questId+"-"+status);
        return status == MapleQuestStatus.QUEST_COMPLETED;
    }

    public boolean isQuestStarted(int questId){
        byte status = this.c.getPlayer().getQuestStatus(questId);
        FileoutputUtil.log("任务状态2："+questId+"-"+status);
        return status == MapleQuestStatus.QUEST_STARTED;
    }

    public boolean isQuestActive(int questId) {
        return getQuestStatus(questId) == 1;
    }

    public boolean isQuestFinished(int questId) {
        return getQuestStatus(questId) == 2;
    }

    public void showQuestMsg(String msg) {
        this.c.getSession().write(MaplePacketCreator.showQuestMsg(msg));
    }

    public void forceStartQuest(int questId, String data) {
        MapleQuest.getInstance(questId).forceStart(this.c.getPlayer(), 0, data);
    }

    public void forceStartQuest(int questId, int data, boolean filler) {
        MapleQuest.getInstance(questId).forceStart(this.c.getPlayer(), 0, filler ? String.valueOf(data) : null);
    }

    public void forceStartQuest(int questId) {
        MapleQuest.getInstance(questId).forceStart(this.c.getPlayer(), 0, null);
    }

    public void forceCompleteQuest(int questId) {
        MapleQuest.getInstance(questId).forceComplete(getPlayer(), 0);
    }

    public void spawnNpc(int npcId) {
        this.c.getPlayer().getMap().spawnNpc(npcId, this.c.getPlayer().getPosition());
    }

    public void spawnNpc(int npcId, int x, int y) {
        this.c.getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
    }

    public void spawnNpc(int npcId, Point pos) {
        this.c.getPlayer().getMap().spawnNpc(npcId, pos);
    }

    public void removeNpc(int mapid, int npcId) {
        this.c.getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
    }

    public void removeNpc(int npcId) {
        this.c.getPlayer().getMap().removeNpc(npcId);
    }

    public void forceStartReactor(int mapId, int reactorId) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapId);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == reactorId) {
                react.forceStartReactor(this.c);
                break;
            }
        }
    }

    public void destroyReactor(int mapId, int reactorId) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapId);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == reactorId) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public void hitReactor(int mapId, int reactorId) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapId);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == reactorId) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public int getJobId() {
        return this.c.getPlayer().getJob();
    }

    public String getJobName(int jobId) {
        return MapleCarnivalChallenge.getJobNameById(jobId);
    }

    public boolean isBeginnerJob() {
        return (GameConstants.is新手职业(getJobId())) && (getLevel() < 11);
    }

    public int getLevel() {
        return this.c.getPlayer().getLevel();
    }

    public int getFame() {
        return this.c.getPlayer().getFame();
    }

    public void gainFame(int famechange) {
        gainFame(famechange, false);
    }

    public void gainFame(int famechange, boolean show) {
    }

    public int getNX(int type) {
        return this.c.getPlayer().getCSPoints(type);
    }

    public boolean gainNX(int amount) {
        return this.c.getPlayer().modifyCSPoints(1, amount, true);
    }

    public boolean gainNX(int type, int amount) {
        if ((type <= 0) || (type > 2)) {
            type = 2;
        }
        return this.c.getPlayer().modifyCSPoints(type, amount, true);
    }

    public void gainItemPeriod(int itemId, short quantity, long period) {
        gainItem(itemId, quantity, false, period, -1, "", 0);
    }

    public void gainItemPeriod(int itemId, short quantity, long period, String owner) {
        gainItem(itemId, quantity, false, period, -1, owner, 0);
    }

    public void gainItem(int itemId, short quantity) {
        gainItem(itemId, quantity, false, 0L, -1, "", 0);
    }

    public void gainItemByState(int itemId, short quantity, int state) {
        gainItem(itemId, quantity, false, 0L, -1, "", state);
    }

    public void gainItem(int itemId, short quantity, boolean randomStats) {
        gainItem(itemId, quantity, randomStats, 0L, -1, "", 0);
    }

    public void gainItem(int itemId, short quantity, boolean randomStats, int slots) {
        gainItem(itemId, quantity, randomStats, 0L, slots, "", 0);
    }

    public void gainItem(int itemId, short quantity, long period) {
        gainItem(itemId, quantity, false, period, -1, "", 0);
    }

    public void gainItem(int itemId, short quantity, long period, int state) {
        gainItem(itemId, quantity, false, period, -1, "", state);
    }

    public void gainItem(int itemId, short quantity, boolean randomStats, long period, int slots) {
        gainItem(itemId, quantity, randomStats, period, slots, "", 0);
    }

    public void gainItem(int itemId, short quantity, boolean randomStats, long period, int slots, String owner) {
        gainItem(itemId, quantity, randomStats, period, slots, owner, 0);
    }

    public void gainItem(int itemId, short quantity, boolean randomStats, long period, int slots, String owner, int state) {
        gainItem(itemId, quantity, randomStats, period, slots, owner, state, this.c);
    }

    public void gainPetItem(int itemId, short quantity) {
        //gainItem(itemId, quantity, false, 0L, -1, "", 0);
        MapleInventoryManipulator.addById(c, itemId, (short) 1, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), 45, c.getPlayer().getName() + " 使用 [宠物箱] 黑暗灵魂 获得");
    }

    public void gainItem(int itemId, short quantity, boolean randomStats, long period, int slots, String owner, int state, MapleClient cg) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType type = ItemConstants.getInventoryType(itemId);
            if (!MapleInventoryManipulator.checkSpace(cg, itemId, quantity, "")) {
                return;
            }
            if (itemId / 10000 == 500) { //宠物
                Item item = new Item(itemId, (byte) 0, (short) 1);
                if (period > 0) {
                    item.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                }
                final MaplePet pet = MaplePet.createPet(itemId, -1);
                if (pet != null) {
                    item.setPet(pet);
                    c.getPlayer().getInventory(type).addItem(item);
                    c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, item))));
                }
                return;
            }
            if ((type.equals(MapleInventoryType.EQUIP)) && (!ItemConstants.is飞镖道具(itemId)) && (!ItemConstants.is子弹道具(itemId))) {
                Equip item = (Equip) (randomStats ? ii.randomizeStats((Equip) ii.getEquipById(itemId)) : ii.getEquipById(itemId));
                if (period > 0L) {
                    if (period < 1000L) {
                        item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    } else {
                        item.setExpiration(System.currentTimeMillis() + period);
                    }
                }
                if (slots > 0) {
                    item.setUpgradeSlots((byte) (item.getUpgradeSlots() + slots));
                }
                if (state > 0) {
                    int newstate = 16 + state;
                    if ((newstate > 20) || (newstate < 17)) {
                        newstate = 17;
                    }
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                item.setGMLog("脚本获得 " + id + " (" + script + ") 地图: " + cg.getPlayer().getMapId() + " 时间: " + FileoutputUtil.CurrentReadable_Time());
                MapleInventoryManipulator.addbyItem(cg, item.copy());
            } else {
                MapleInventoryManipulator.addById(cg, itemId, quantity, owner == null ? "" : owner, null, period, new StringBuilder().append("脚本获得 ").append(id).append(" (").append(script).append(") 地图: ").append(cg.getPlayer().getMapId()).append(" 时间: ").append(FileoutputUtil.CurrentReadable_Time()).toString());
            }
        } else {
            MapleInventoryManipulator.removeById(cg, ItemConstants.getInventoryType(itemId), itemId, -quantity, true, false);
        }
        cg.getSession().write(MaplePacketCreator.getShowItemGain(itemId, quantity, true));
    }

    public boolean removeItem(int itemId) {
        if (MapleInventoryManipulator.removeById_Lock(this.c, ItemConstants.getInventoryType(itemId), itemId)) {
            this.c.getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) -1, true));
            return true;
        }
        return false;
    }

    public void gainItemAndEquip(int itemId, short slot) {
        MapleInventoryManipulator.addItemAndEquip(this.c, itemId, slot);
    }

    public void changeMusic(String songName) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void worldMessage(String message) {
        worldMessage(6, message);
    }

    public void worldMessage(int type, String message) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void playerMessage(String message) {
        playerMessage(5, message);
    }

    public void mapMessage(String message) {
        mapMessage(5, message);
    }

    public void guildMessage(String message) {
        guildMessage(5, message);
    }

    public void playerMessage(int type, String message) {
        this.c.getPlayer().dropMessage(type, message);
    }

    public void mapMessage(int type, String message) {
        this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void guildMessage(int type, String message) {
        if (getPlayer().getGuildId() > 0) {
            WorldGuildService.getInstance().guildPacket(getPlayer().getGuildId(), MaplePacketCreator.serverNotice(type, message));
        }
    }

    public void topMessage(String message) {
        this.c.getSession().write(UIPacket.getTopMsg(message));
    }

    public void topMsg(String message) {
        topMessage(message);
    }

    public MapleGuild getGuild() {
        return getGuild(getPlayer().getGuildId());
    }

    public MapleGuild getGuild(int guildid) {
        return WorldGuildService.getInstance().getGuild(guildid);
    }

    public MapleParty getParty() {
        return this.c.getPlayer().getParty();
    }

    public int getCurrentPartyId(int mapId) {
        return getMap(mapId).getCurrentPartyId();
    }

    public boolean isLeader() {
        if (getPlayer().getParty() == null) {
            return false;
        }
        return getParty().getLeader().getId() == this.c.getPlayer().getId();
    }

    public void partyMessage(int type, String msg) {
        for (MaplePartyCharacter mem : this.c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(mem.getId());
            chr.dropMessage(type, msg);
        }
    }

    public boolean isAllPartyMembersAllowedJob(int jobId) {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (MaplePartyCharacter mem : this.c.getPlayer().getParty().getMembers()) {
            if (mem.getJobId() / 100 != jobId) {
                return false;
            }
        }
        return true;
    }

    public final boolean isAllPartyMembersAllowedLevel(final int min, final int max) {
        if (c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            if (mem.getLevel() < min || mem.getLevel() > max) {
                return false;
            }
        }
        return true;
    }

    public final boolean isAllPartyMembersAllowedPQ(final String pqName, int times) {
        if (c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(mem.getId());
            if (chr == null || chr.getPQLog(pqName) >= times) {
                return false;
            }
        }
        return true;
    }

    public final MaplePartyCharacter getNotAllowedPQMember(final String pqName, int times) {
        if (c.getPlayer().getParty() == null) {
            return null;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(mem.getId());
            if (chr == null || chr.getPQLog(pqName) >= times) {
                return mem;
            }
        }
        return null;
    }

    public final void gainMembersPQ(final String pqName, int num) {
        if (c.getPlayer().getParty() == null) {
            return;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(mem.getId());
            if (chr != null) {
                chr.setPQLog(pqName, 0, num);
            }
        }
    }

    public boolean allMembersHere() {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (MaplePartyCharacter mem : this.c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.c.getPlayer().getMap().getCharacterById(mem.getId());
            if (chr == null) {
                return false;
            }
        }
        return true;
    }

    public void warpParty(int mapId) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            warp(mapId, 0);
            return;
        }
        MapleMap target = getMap(mapId);
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void warpParty(int mapId, int portal) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            if (portal < 0) {
                warp(mapId,0);
            } else {
                warp(mapId, portal);
            }
            return;
        }
        boolean rand = portal < 0;
        MapleMap target = getMap(mapId);
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                if (rand) {
                    try {
                        curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
                    } catch (Exception e) {
                        curChar.changeMap(target, target.getPortal(0));
                    }
                } else {
                    curChar.changeMap(target, target.getPortal(portal));
                }
            }
        }
    }

    public void warpParty_Instanced(int mapId) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            warp(mapId,0);
            return;
        }
        MapleMap target = getMap_Instanced(mapId);

        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void gainMeso(long gain) {
        this.c.getPlayer().gainMeso(gain, true, true);
    }

    public void gainExp(int gain) {
        this.c.getPlayer().gainExp(gain, true, true, true);
    }

    public void gainExpR(int gain) {
        this.c.getPlayer().gainExp(gain * this.c.getChannelServer().getExpRate(getPlayer().getWorld()), true, true, true);
    }

    public void givePartyItems(int itemId, short quantity, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            if (quantity >= 0) {
                MapleInventoryManipulator.addById(chr.getClient(), itemId, quantity, new StringBuilder().append("Received from party interaction ").append(itemId).append(" (").append(this.id).append(")").toString());
            } else {
                MapleInventoryManipulator.removeById(chr.getClient(), ItemConstants.getInventoryType(itemId), itemId, -quantity, true, false);
            }
            chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, quantity, true));
        }
    }

    public void addPartyTrait(String t, int e, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
        }
    }

    public void addPartyTrait(String t, int e) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            addTrait(t, e);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
            }
        }
    }

    public void addTrait(String t, int e) {
    }

    public void givePartyItems(int itemId, short quantity) {
        givePartyItems(itemId, quantity, false);
    }

    public void givePartyItems(int itemId, short quantity, boolean removeAll) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            gainItem(itemId, (short) (removeAll ? -getPlayer().itemQuantity(itemId) : quantity));
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                gainItem(itemId, (short) (removeAll ? -curChar.itemQuantity(itemId) : quantity), false, 0L, 0, "", 0, curChar.getClient());
            }
        }
    }

    public void givePartyExp_PQ(int maxLevel, double mod, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            int amount = (int) Math.round(GameConstants.getExpNeededForLevel(chr.getLevel() > maxLevel ? maxLevel + (maxLevel - chr.getLevel()) / 10 : chr.getLevel()) / (Math.min(chr.getLevel(), maxLevel) / 5.0D) / (mod * 2.0D));
            chr.gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()), true, true, true);
        }
    }

    public void gainExp_PQ(int maxLevel, double mod) {
        int amount = (int) Math.round(GameConstants.getExpNeededForLevel(getPlayer().getLevel() > maxLevel ? maxLevel + getPlayer().getLevel() / 10 : getPlayer().getLevel()) / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0D) / mod);
        gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()));
    }

    public void givePartyExp_PQ(int maxLevel, double mod) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            int amount = (int) Math.round(GameConstants.getExpNeededForLevel(getPlayer().getLevel() > maxLevel ? maxLevel + getPlayer().getLevel() / 10 : getPlayer().getLevel()) / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0D) / mod);
            gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()));
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                int amount = (int) Math.round(GameConstants.getExpNeededForLevel(curChar.getLevel() > maxLevel ? maxLevel + curChar.getLevel() / 10 : curChar.getLevel()) / (Math.min(curChar.getLevel(), maxLevel) / 10.0D) / mod);
                curChar.gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()), true, true, true);
            }
        }
    }

    public void givePartyExp(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()), true, true, true);
        }
    }

    public void givePartyExp(int amount) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()));
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.gainExp(amount * this.c.getChannelServer().getExpRate(getPlayer().getWorld()), true, true, true);
            }
        }
    }

    public void givePartyNX(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.modifyCSPoints(1, amount, true);
        }
    }

    public void givePartyNX(int amount) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            gainNX(amount);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.modifyCSPoints(1, amount, true);
            }
        }
    }

    public void endPartyQuest(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.endPartyQuest(amount);
        }
    }

    public void endPartyQuest(int amount) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            getPlayer().endPartyQuest(amount);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.endPartyQuest(amount);
            }
        }
    }

    public void removeFromParty(int itemId, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            int possesed = chr.getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(this.c, ItemConstants.getInventoryType(itemId), itemId, possesed, true, false);
                chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) (-possesed), true));
            }
        }
    }

    public void removeFromParty(int itemId) {
        givePartyItems(itemId, (short) 0, true);
    }

    public void useSkill(int skillId, int skillLevel) {
        if (skillLevel <= 0) {
            return;
        }
        SkillFactory.getSkill(skillId).getEffect(skillLevel).applyTo(this.c.getPlayer());
    }

//    public void useItem(int itemId) {
//        MapleItemInformationProvider.getInstance().getItemEffect(itemId).applyTo(this.c.getPlayer());
//        this.c.getSession().write(UIPacket.getStatusMsg(itemId));
//    }

    public void cancelItem(int itemId) {
        this.c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1L);
    }

    public int getMorphState() {
        return this.c.getPlayer().getMorphState();
    }

    public void removeAll(int itemId) {
        this.c.getPlayer().removeAll(itemId);
    }

    public void gainCloseness(int closeness, int index) {
        MaplePet pet = getPlayer().getSpawnPet();
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + closeness * getChannelServer().getTraitRate());
            getClient().getSession().write(PetPacket.updatePet(pet, getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
        }
    }

    public void gainClosenessAll(int closeness) {
        MaplePet pets = getPlayer().getSpawnPets();
        if ((pets != null) && (pets.getSummoned())) {
            pets.setCloseness(pets.getCloseness() + closeness);
            getClient().getSession().write(PetPacket.updatePet(pets, getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pets.getInventoryPosition()), false));
        }
    }

    public void resetMap(int mapId) {
        getMap(mapId).resetFully();
    }

    public void openNpc(int npcId) {
        getClient().removeClickedNPC();
        NPCScriptManager.getInstance().start(getClient(), npcId);
    }

    public void openNpc(MapleClient cg, int npcId) {
        cg.removeClickedNPC();
        NPCScriptManager.getInstance().start(cg, npcId);
    }

    public void openNpc(int npcId, String npcMode) {
        getClient().removeClickedNPC();
        NPCScriptManager.getInstance().start(getClient(), npcId, npcMode);
    }

    public int getMapId() {
        return this.c.getPlayer().getMap().getId();
    }

    public boolean haveMonster(int mobId) {
        for (MapleMapObject obj : this.c.getPlayer().getMap().getAllMonstersThreadsafe()) {
            MapleMonster mob = (MapleMonster) obj;
            if (mob.getId() == mobId) {
                return true;
            }
        }
        return false;
    }

    public int getChannelNumber() {
        return this.c.getChannel();
    }

    public int getMonsterCount(int mapId) {
        return this.c.getChannelServer().getMapFactory().getMap(mapId).getNumMonsters();
    }

    public void teachSkill(int skillId, int skilllevel, byte masterlevel) {
        getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillId), skilllevel, masterlevel);
    }

    public void teachSkill(int skillId, int skilllevel) {
        Skill skil = SkillFactory.getSkill(skillId);
        if (getPlayer().getSkillLevel(skil) > skilllevel) {
            skilllevel = getPlayer().getSkillLevel(skil);
        }
        getPlayer().changeSingleSkillLevel(skil, skilllevel, (byte) skil.getMaxLevel());
    }

    public int getPlayerCount(int mapId) {
        return this.c.getChannelServer().getMapFactory().getMap(mapId).getCharactersSize();
    }

    public MapleEvent getEvent(String loc) {
        return this.c.getChannelServer().getEvent(MapleEventType.valueOf(loc));
    }

    public int getSavedLocation(String loc) {
        Integer ret = this.c.getPlayer().getSavedLocation(SavedLocationType.fromString(loc));
        if (ret == -1) {
            return 950000100;
        }
        return ret;
    }

    public void saveLocation(String loc) {
        this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc));
    }

    public void saveReturnLocation(String loc) {
        this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc), this.c.getPlayer().getMap().getReturnMap().getId());
    }

    public void clearSavedLocation(String loc) {
        this.c.getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
    }

    public void summonMsg(String msg) {
        if (!this.c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        this.c.getSession().write(UIPacket.summonMessage(msg));
    }

    public void summonMsg(int type) {
        if (!this.c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        this.c.getSession().write(UIPacket.summonMessage(type));
    }

    public void showInstruction(String msg, int width, int height) {
        this.c.getSession().write(MaplePacketCreator.sendHint(msg, width, height));
    }

    public void playerSummonHint(boolean summon) {
        this.c.getPlayer().setHasSummon(summon);
        this.c.getSession().write(UIPacket.summonHelper(summon));
    }

    public String getInfoQuest(int questId) {
        return this.c.getPlayer().getInfoQuest(questId);
    }

    public void updateInfoQuest(int questId, String data) {
    }

    public boolean getEvanIntroState(String data) {
        return getInfoQuest(22013).contains(data);
    }

    public void updateEvanIntroState(String data) {
        updateInfoQuest(22013, data);
    }

    public final void ShowWZEffect(final String data) {
        c.getSession().write(UIPacket.TutInstructionalBalloon(data));
    }

    public final void showWZEffect(String data) {
        this.c.getSession().write(UIPacket.ShowWZEffect(data));
    }

    public final void showWZEffect(final int type, final String data) {
        c.getSession().write(UIPacket.ShowWZEffect(type, data));
    }

    public final void EarnTitleMsg(String data) {
        this.c.getSession().write(UIPacket.EarnTitleMsg(data));
    }

    public void showEffect(String effect) {
        this.c.getSession().write(MaplePacketCreator.showEffect(effect));
    }

    public void playSound(String sound) {
        this.c.getSession().write(MaplePacketCreator.playSound(sound));
    }

    public void startMapEffect(String msg, int itemId) {
        this.c.getPlayer().getMap().startMapEffect(msg, itemId);
    }

    public void showMapEffect(String path) {
        getClient().getSession().write(UIPacket.MapEff(path));
    }

    public void EnableUI(short i) {
        this.c.getSession().write(UIPacket.IntroEnableUI(i));
    }

    public void DisableUI(boolean enabled) {
        this.c.getSession().write(UIPacket.IntroDisableUI(enabled));
    }

    public void introDisableUI(boolean enabled) {
        this.c.getSession().write(UIPacket.IntroDisableUI(enabled));
    }

    public void MovieClipIntroUI(boolean enabled) {
        this.c.getSession().write(UIPacket.IntroDisableUI(enabled));
        this.c.getSession().write(UIPacket.IntroLock(enabled));
    }

    public void lockUI() {
        this.c.getSession().write(UIPacket.IntroDisableUI(true));
        this.c.getSession().write(UIPacket.IntroLock(true));
    }

    public void unlockUI() {
        this.c.getSession().write(UIPacket.IntroDisableUI(false));
        this.c.getSession().write(UIPacket.IntroLock(false));
    }

    public MapleInventoryType getInvType(int i) {
        return MapleInventoryType.getByType((byte) i);
    }

    public String getItemName(int itemId) {
        return MapleItemInformationProvider.getInstance().getName(itemId);
    }

    public void gainPet(int itemId, String name, int level, int closeness, int fullness, long period, short flags) {
        if ((itemId > 5000200) || (itemId < 5000000)) {
            itemId = 5000000;
        }
        if (level > 30) {
            level = 30;
        }
        if (closeness > 30000) {
            closeness = 30000;
        }
        if (fullness > 100) {
            fullness = 100;
        }
        try {
            MapleInventoryManipulator.addById(this.c, itemId, (short) 1, "", MaplePet.createPet(itemId, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), itemId == 5000054 ? (int) period : 0, (flags == -1 ? MapleItemInformationProvider.getInstance().getPetFlagInfo(itemId) : flags), 0), 45L, new StringBuilder().append("Pet from interaction ").append(itemId).append(" (").append(this.id).append(")").append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        } catch (NullPointerException ex) {
        }
    }

    public void removeSlot(int invType, byte slot, short quantity) {
        MapleInventoryManipulator.removeFromSlot(this.c, getInvType(invType), (short) slot, quantity, true);
    }

    public void gainGP(int gp) {
        if (getPlayer().getGuildId() <= 0) {
            return;
        }
        WorldGuildService.getInstance().gainGP(getPlayer().getGuildId(), gp);
    }

    public int getGP() {
        if (getPlayer().getGuildId() <= 0) {
            return 0;
        }
        return WorldGuildService.getInstance().getGP(getPlayer().getGuildId());
    }

    public int itemQuantity(int itemId) {
        return getPlayer().itemQuantity(itemId);
    }

    public EventInstanceManager getDisconnected(String event) {
        EventManager em = getEventManager(event);
        if (em == null) {
            return null;
        }
        for (EventInstanceManager eim : em.getInstances()) {
            if ((eim.isDisconnected(this.c.getPlayer())) && (eim.getPlayerCount() > 0)) {
                return eim;
            }
        }
        return null;
    }

    public boolean isAllReactorState(int reactorId, int state) {
        boolean ret = false;
        for (MapleReactor r : getMap().getAllReactorsThreadsafe()) {
            if (r.getReactorId() == reactorId) {
                ret = r.getState() == state;
            }
        }
        return ret;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void spawnMonster(int mobId) {
        spawnMonster(mobId, 1, getPlayer().getTruePosition());
    }

    public void spawnMonster(int mobId, int x, int y) {
        spawnMonster(mobId, 1, new Point(x, y));
    }

    public void spawnMonster(int mobId, int quantity, int x, int y) {
        spawnMonster(mobId, quantity, new Point(x, y));
    }

    public void spawnMonster(int mobId, int quantity, Point pos) {
        for (int i = 0; i < quantity; i++) {
            getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), pos);
        }
    }

    public void sendNPCText(String text, int npcId) {
        getMap().broadcastMessage(NPCPacket.sendNPCSay(npcId, text));
    }

    public boolean getTempFlag(int flag) {
        return (this.c.getChannelServer().getTempFlag() & flag) == flag;
    }

    public void logPQ(String text) {
    }

    public void outputFileError(Throwable t) {
        FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, t);
    }

    public void trembleEffect(int type, int delay) {
        this.c.getSession().write(MaplePacketCreator.trembleEffect(type, delay));
    }

    public int nextInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public MapleQuest getQuest(int arg0) {
        return MapleQuest.getInstance(arg0);
    }

    public void achievement(int a) {
        this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.achievementRatio(a));
    }

    public MapleInventory getInventory(int type) {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
    }

    public boolean isGMS() {
        return GameConstants.GMS;
    }

    public int randInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public void sendDirectionStatus(int key, int value) {
        this.c.getSession().write(UIPacket.getDirectionInfo(key, value));
        this.c.getSession().write(UIPacket.getDirectionStatus(true));
    }

    public void sendDirectionInfo(String data) {
        this.c.getSession().write(UIPacket.getDirectionInfo(data, 2000, 0, -100, 0));
        this.c.getSession().write(UIPacket.getDirectionInfo(1, 2000));
    }

    public int getPQLog(String pqName) {
        return this.c.getPlayer().getPQLog(pqName);
    }

    public int getPQLog(String pqName, int type) {
        return this.c.getPlayer().getPQLog(pqName, type);
    }

    public void setPQLog(String pqName) {
        this.c.getPlayer().setPQLog(pqName);
    }

    public void setPQLog(String pqName, int type) {
        this.c.getPlayer().setPQLog(pqName, type);
    }

    public void setPQLog(String pqName, int type, int count) {
        this.c.getPlayer().setPQLog(pqName, type, count);
    }

    public void resetPQLog(String pqName) {
        this.c.getPlayer().resetPQLog(pqName);
    }

    public void resetPQLog(String pqName, int type) {
        this.c.getPlayer().resetPQLog(pqName, type);
    }

    public void setPartyPQLog(String pqName) {
        setPartyPQLog(pqName, 0);
    }

    public void setPartyPQLog(String pqName, int type) {
        setPartyPQLog(pqName, type, 1);
    }

    public void setPartyPQLog(String pqName, int type, int count) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            this.c.getPlayer().setPQLog(pqName, type, count);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getPlayer().getMap().getCharacterById(chr.getId());
            if ((curChar != null) && (curChar.getMapId() == cMap)) {
                curChar.setPQLog(pqName, type, count);
            }
        }
    }

    public int getGamePoints() {
        return this.c.getPlayer().getGamePoints();
    }

    public void gainGamePoints(int amount) {
        this.c.getPlayer().gainGamePoints(amount);
    }

    public void resetGamePoints() {
        this.c.getPlayer().resetGamePoints();
    }

    public void getClock(int time) {
        this.c.getSession().write(MaplePacketCreator.getClock(time));
    }

    public void openWeb(String web) {
        this.c.getSession().write(MaplePacketCreator.openWeb(web));
    }

    public boolean isCanPvp() {
        return this.c.getChannelServer().isCanPvp();
    }

    public void showDoJangRank() {
        this.c.getSession().write(MaplePacketCreator.showDoJangRank());
    }

    public int MarrageChecking() {
        if (getPlayer().getParty() == null) {
            return -1;
        }
        if (getPlayer().getMarriageId() > 0) {
            return 0;
        }
        if (getPlayer().getParty().getMembers().size() != 2) {
            return 1;
        }
        if ((getPlayer().getGender() == 0) && (!getPlayer().haveItem(1050121)) && (!getPlayer().haveItem(1050122)) && (!getPlayer().haveItem(1050113))) {
            return 5;
        }
        if ((getPlayer().getGender() == 1) && (!getPlayer().haveItem(1051129)) && (!getPlayer().haveItem(1051130)) && (!getPlayer().haveItem(1051114))) {
            return 5;
        }
        if (!getPlayer().haveItem(1112001)) {
            return 6;
        }
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            if (chr.getId() == getPlayer().getId()) {
                continue;
            }
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar == null) {
                return 2;
            }
            if (curChar.getMarriageId() > 0) {
                return 3;
            }
            if (curChar.getGender() == getPlayer().getGender()) {
                return 4;
            }
            if ((curChar.getGender() == 0) && (!curChar.haveItem(1050121)) && (!curChar.haveItem(1050122)) && (!curChar.haveItem(1050113))) {
                return 5;
            }
            if ((curChar.getGender() == 1) && (!curChar.haveItem(1051129)) && (!curChar.haveItem(1051130)) && (!curChar.haveItem(1051114))) {
                return 5;
            }
            if (!curChar.haveItem(1112001)) {
                return 6;
            }
        }
        return 9;
    }

    public int getPartyFormID() {
        int curCharID = -1;
        if (getPlayer().getParty() == null) {
            curCharID = -1;
        } else if (getPlayer().getMarriageId() > 0) {
            curCharID = -2;
        } else if (getPlayer().getParty().getMembers().size() != 2) {
            curCharID = -3;
        }
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            if (chr.getId() == getPlayer().getId()) {
                continue;
            }
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar == null) {
                curCharID = -4;
            } else {
                curCharID = chr.getId();
            }
        }
        return curCharID;
    }

    public int getGMLevel() {
        return this.c.getPlayer().getGMLevel();
    }

    public void getLevelup() {
        this.c.getPlayer().levelUp();
    }

    public void spouseMessage(int op, String msg) {
        this.c.getSession().write(MaplePacketCreator.spouseMessage(op, msg));
    }

    public void sendPolice(String text, boolean dc) {
        if (dc) {
            this.c.getPlayer().sendPolice(text);
        } else {
            this.c.getSession().write(MaplePacketCreator.sendPolice(text));
        }
    }

    public String getTime() {
        return DateUtil.getNowTime();
    }

    public boolean checkPartyEvent(int minLevel, int maxLevel, int minPartySize, int maxPartySize, int itemId) {
        MapleParty party = this.c.getPlayer().getParty();
        if ((party == null) || (party.getMembers().size() < minPartySize) || (party.getLeader().getId() != this.c.getPlayer().getId())) {
            return false;
        }
        int inMap = 0;
        boolean next = true;
        int checkMapId = getPlayer().getMapId();
        for (MaplePartyCharacter cPlayer : party.getMembers()) {
            MapleCharacter ccPlayer = getPlayer().getMap().getCharacterById(cPlayer.getId());
            if ((ccPlayer != null) && (ccPlayer.getLevel() >= minLevel) && (ccPlayer.getLevel() <= maxLevel) && (ccPlayer.getMapId() == checkMapId) && (ccPlayer.haveItem(itemId))) {
                inMap++;
            } else {
                return false;
            }
        }
        if ((party.getMembers().size() > maxPartySize) || (inMap < minPartySize)) {
            next = false;
        }
        return next;
    }

    public int getPlayerPoints() {
        return this.c.getPlayer().getPlayerPoints();
    }

    public void setPlayerPoints(int gain) {
        this.c.getPlayer().setPlayerPoints(gain);
    }

    public void gainPlayerPoints(int gain) {
        this.c.getPlayer().gainPlayerPoints(gain);
    }

    public int getPlayerEnergy() {
        return this.c.getPlayer().getPlayerEnergy();
    }

    public void setPlayerEnergy(int gain) {
        this.c.getPlayer().setPlayerEnergy(gain);
    }

    public void gainPlayerEnergy(int gain) {
        this.c.getPlayer().gainPlayerEnergy(gain);
    }

    public int getEventCount(String eventId) {
        return this.c.getPlayer().getEventCount(eventId);
    }

    public int getEventCount(String eventId, int type) {
        return this.c.getPlayer().getEventCount(eventId, type);
    }

    public void setEventCount(String eventId) {
        this.c.getPlayer().setEventCount(eventId);
    }

    public void setEventCount(String eventId, int type) {
        this.c.getPlayer().setEventCount(eventId, type);
    }

    public void setEventCount(String eventId, int type, int count) {
        this.c.getPlayer().setEventCount(eventId, type, count);
    }

    public void resetEventCount(String eventId) {
        this.c.getPlayer().resetEventCount(eventId);
    }

    public void resetEventCount(String eventId, int type) {
        this.c.getPlayer().resetEventCount(eventId, type);
    }

    public void setPartyEventCount(String eventId) {
        setPartyEventCount(eventId, 0);
    }

    public void setPartyEventCount(String eventId, int type) {
        setPartyEventCount(eventId, type, 1);
    }

    public void setPartyEventCount(String eventId, int type, int count) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            this.c.getPlayer().setEventCount(eventId, type, count);
            return;
        }
        int checkMap = getPlayer().getMapId();
        for (MaplePartyCharacter partyPlayer : getPlayer().getParty().getMembers()) {
            MapleCharacter chr = getPlayer().getMap().getCharacterById(partyPlayer.getId());
            if ((chr != null) && (chr.getMapId() == checkMap)) {
                chr.setEventCount(eventId, type, count);
            }
        }
    }

    public boolean checkPartyEventCount(String eventId) {
        return checkPartyEventCount(eventId, 1);
    }

    public boolean checkPartyEventCount(String eventId, int checkcount) {
        MapleParty party = this.c.getPlayer().getParty();

        if ((party == null) || (party.getMembers().size() == 1)) {
            int count = getEventCount(eventId);
            return (count >= 0) && (count < checkcount);
        }
        int check = 0;
        int partySize = party.getMembers().size();
        for (MaplePartyCharacter partyPlayer : party.getMembers()) {
            MapleCharacter chr = getPlayer().getMap().getCharacterById(partyPlayer.getId());
            if (chr != null) {
                int count = chr.getEventCount(eventId);
                if ((count >= 0) && (count < checkcount)) {
                    check++;
                }
            }
        }
        return partySize == check;
    }

    public MapleItemInformationProvider getItemInfo() {
        return MapleItemInformationProvider.getInstance();
    }

    public Equip getEquipBySlot(short slot) {
        return (Equip) this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
    }

    public void sendRemoveNPC(int oid) {
        c.getSession().write(NPCPacket.removeNPC(oid));
    }

    public void sendDirectionFacialExpression(int expression, int duration) {
        c.getSession().write(UIPacket.getDirectionFacialExpression(expression, duration));
    }

    public void introEnableUI(int wtf) {
        c.getSession().write(UIPacket.IntroEnableUI(wtf));
    }

    public void getDirectionStatus(boolean enable) {
        c.getSession().write(UIPacket.getDirectionStatus(enable));
    }

    public void playMovie(String data, boolean show) {
        c.getSession().write(UIPacket.playMovie(data, show));
    }

    public void sendDirectionStatus(int key, int value, boolean direction) {
        c.getSession().write(UIPacket.getDirectionInfo(key, value));
        c.getSession().write(UIPacket.getDirectionStatus(direction));
    }

    public void getDirectionInfo(String data, int value, int x, int y, int a, int b) {
        c.getSession().write(UIPacket.getDirectionInfo(data, value, x, y, a, b));
    }

    public void getDirectionInfo(byte type, int value) {
        c.getSession().write(UIPacket.getDirectionInfo(type, value));
    }

    public void getDirectionInfoNew(byte type, int x, int y, int z) {
        c.getSession().write(UIPacket.getDirectionInfoNew(type, x, y, z));
    }

    public void getDirectionInfoNew(byte type, int value) {
        c.getSession().write(UIPacket.getDirectionInfoNew(type, value));
    }

    public void getDIRECTION_INFO(int value, int s, String data) {
        c.getSession().write(UIPacket.getDIRECTION_INFO(data, value, s));
    }

    public void getDirectionEffect(String data, int value, int x, int y) {
        c.getSession().write(UIPacket.getDirectionEffect(data, value, x, y));
    }

    public void sendPyramidEnergy(String object, String amount) {
        c.getSession().write(MaplePacketCreator.sendPyramidEnergy(object, amount));
    }

    public void spawnPortal() {
        c.getSession().write(MaplePacketCreator.spawnPortal(999999999, 999999999, 0, null));
    }

    public void spawnNPCRequestController(int npcid, int x, int y, int cy) {
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npcRequestController.remove(new Pair(npcid, c));
        }
        MapleNPC npc = MapleLifeFactory.getNPC(npcid);
        if (npc == null) {
            return;
        }
        npc.setPosition(new Point(x, y));
        npc.setCy(cy);
        npc.setRx0(x - 50);
        npc.setRx1(x + 50);
        npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(new Point(x, y)).getId());
        npc.setCustom(true);
        npc.setObjectId(npcid);
        npcRequestController.put(new Pair(npcid, c), npc);
        c.getSession().write(NPCPacket.spawnNPCRequestController(npc, true));//isMiniMap
    }

    public void setNPCSpecialAction(int npcid, String action) {
        final MapleNPC npc;
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npc = npcRequestController.get(new Pair(npcid, c));
        } else {
            return;
        }
        c.getSession().write(NPCPacket.setNPCSpecialAction(npc.getObjectId(), action));
    }

    public void updateNPCSpecialAction(int npcid, int value, int x, int y) {
        final MapleNPC npc;
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npc = npcRequestController.get(new Pair(npcid, c));
        } else {
            return;
        }
        c.getSession().write(NPCPacket.NPCSpecialAction(npc.getObjectId(), value, x, y));
    }

    public void getNPCDirectionEffect(int npcid, String data, int value, int x, int y) {
        final MapleNPC npc;
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npc = npcRequestController.get(new Pair(npcid, c));
        } else {
            return;
        }
        c.getSession().write(UIPacket.getDirectionEffect(data, value, x, y, npc.getObjectId()));
    }

    public void removeNPCRequestController(int npcid) {
        final MapleNPC npc;
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npc = npcRequestController.get(new Pair(npcid, c));
        } else {
            return;
        }
        c.getSession().write(NPCPacket.spawnNPCRequestController(npc.getObjectId()));
        c.getSession().write(NPCPacket.removeNPC(npc.getObjectId()));
        npcRequestController.remove(new Pair(npcid, c));
    }

    public void enableActions() {
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public void spawnReactorOnGroundBelow(int id, int x, int y) {
        c.getPlayer().getMap().spawnReactorOnGroundBelow(new MapleReactor(MapleReactorFactory.getReactor(id), id), new Point(x, y));
    }

    public void removeNPCController(int npcid) {
        final MapleNPC npc;
        if (npcRequestController.containsKey(new Pair(npcid, c))) {
            npc = npcRequestController.get(new Pair(npcid, c));
        } else {
            return;
        }
        c.getSession().write(NPCPacket.removeNPCController(npc.getObjectId()));
    }

    public void setDirection(int z) {
        c.getPlayer().setDirection(z);
    }

    public List<MapleCharacter> getchrlist() {
        return this.c.loadCharacters((int) c.getPlayer().getWorld());
    }

    public int deleteCharacter(int charId) {
        return c.deleteCharacter(charId);
    }

    public String getCustomData(int questId) {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questId)).getCustomData();
    }

    public void ShowDamageHp() {
        MapleQuest.getInstance(7291).forceStart(this.c.getPlayer(), 0, String.valueOf(Integer.valueOf(this.script)));
    }

    public void DeleteItem() {
        gainItem(Integer.valueOf(this.script), (byte) -1, false, 0L, -1, "", 0);
    }

    public void sendUIWindow(final int type, final int npc) {
        c.getSession().write(UIPacket.openUIOption(type, npc));
    }

    public void deleteChrSkills() {
        Connection con = DatabaseConnection.getConnection();
        try {
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", c.getPlayer().getId());
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(AbstractPlayerInteraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void instantMapWarp(byte portal) {
        this.c.getSession().write(MaplePacketCreator.instantMapWarp((byte) portal));
    }

    public void EventGainNX() {
        //for (MaplePartyCharacter pchr : this.c.getPlayer().getParty().getMembers()) {
        c.getPlayer().modifyCSPoints(1, Math.min(c.getPlayer().getParty().getAverageLevel(c.getPlayer()) / 250 * 500, 2000), true);
        //}
    }

    public List<Item> getCashItemlist() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventory Equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Item> ret = new ArrayList();
        for (Item tep : Equip) {
            Map stats = ii.getEquipStats(tep.getItemId());
            if (stats.containsKey("cash")) {
                ret.add(tep);
            }
        }
        return ret;
    }

    public long getOnlineTime() {
        return System.currentTimeMillis() - c.getPlayer().getlogintime();
    }

    public void setOnlineTime() {
        c.getPlayer().setlogintime(System.currentTimeMillis());
    }

    public boolean isAdmin() {
        return c.getPlayer().isAdmin();
    }

    public void FreeTransfer() {
        this.c.getSession().write(UIPacket.openUI(0xA7));
    }
}
