package scripting.npc;

import client.*;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.BattleConstants;
import constants.BattleConstants.PokedexEntry;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.PlayersHandler;
import handling.login.LoginInformationProvider;
import handling.world.WorldBroadcastService;
import handling.world.WorldFindService;
import handling.world.party.ExpeditionType;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.script.Invocable;
import scripting.AbstractPlayerInteraction;
import scripting.ScriptType;
import scripting.event.EventInstanceManager;
import server.MapleCarnivalChallenge;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.RankingWorker;
import server.SpeedRunner;
import server.StructItemOption;
import server.Timer.CloneTimer;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import server.squad.MapleSquad;
import tools.*;
import tools.packet.NPCPacket;

public class NPCConversationManager extends AbstractPlayerInteraction {

    private final int npcId;
    private String getText;
    private final ScriptType type;
    private String npcMode = null;
    public boolean pendingDisposal = false;
    private final Invocable iv;

    public NPCConversationManager(MapleClient c, int npc, String npcMode, ScriptType type, Invocable iv) {
        super(c, npc, npcMode);
        this.npcId = npc;
        this.npcMode = npcMode;
        this.iv = iv;
        this.type = type;
    }

    public ScriptType getType() {
        return type;
    }

    public Invocable getIv() {
        return this.iv;
    }

    public int getNpc() {
        return this.npcId;
    }

    public String getScript() {
        return npcMode;
    }

    public void safeDispose() {
        this.pendingDisposal = true;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendSlideMenu(final int type, final String sel) {
        String[] sels = sel.split("#");
        if (sels.length < 3) {
            return;
        }
        c.sendPacket(NPCPacket.sendNPCAskMenu(id,sel));
    }

    public void sendStyle(String text,int styles[]){
        if (styles.length <= 0) {
            return;
        }
        c.sendPacket(NPCPacket.sendNPCAskAvatar(id,text,styles));
    }

    public void sendNext(String text) {
        sendNext(text, this.id);
    }

    public void sendNext(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(id, text, false,true));
    }

    public void sendPlayerToNpc(String text) {
        sendNextS(text, (byte) 3, this.id);
    }

    public void sendNextNoESC(String text) {
        sendNextS(text, (byte) 1, this.id);
    }

    public void sendNextNoESC(String text, int id) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextS(String text, byte type) {
        sendNextS(text, type, this.id);
    }

    public void sendNextS(String text, byte type, int idd) {
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text, false,true));
    }

    public void sendPrev(String text) {
        sendPrev(text, this.id);
    }

    public void sendPrev(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(id,text, true,false));
    }

    public void sendPrevS(String text, byte type) {
        sendPrevS(text, type, this.id);
    }

    public void sendPrevS(String text, byte type, int idd) {
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id,text, true,false));
    }

    public void sendNextPrev(String text) {
        sendNextPrev(text, this.id);
    }

    public void sendNextPrev(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(id, text, true,true));
    }

    public void PlayerToNpc(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text, byte type) {
        sendNextPrevS(text, type, this.id);
    }

    public void sendNextPrevS(String text, byte type, int idd) {
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text, true,true));
    }

    public void sendNextPrevS(String text, byte type, int idd, int npcid) {
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.sendPacket(NPCPacket.sendNPCSay(npcid, text, true,true));
    }

    public void sendOk(String text) {
        sendOk(text, this.id);
    }

    public void sendOk(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(id,text));
    }

    public void sendOkS(String text, byte type) {
        sendOkS(text, type, this.id);
    }

    public void sendOkS(String text, byte type, int idd) {
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text));
    }

    public void sendYesNo(String text) {
        sendYesNo(text, this.id);
    }

    public void sendYesNo(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCAskYesNo(id, text));
    }

    public void sendYesNoS(String text, byte type) {
        sendYesNoS(text, type, this.id);
    }

    public void sendYesNoS(String text, byte type, int idd) {
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCAskYesNo(this.id,text));
    }

    public void sendSimple(String text) {
        sendSimple(text, this.id);
    }

    public void sendSimple(String text, int id) {
        if (!text.contains("#L")) {
            sendNext(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendChoose(id,text));
    }

    public void sendSimpleS(String text, byte type) {
        sendSimpleS(text, type, this.id);
    }

    public void sendSimpleS(String text, byte type, int idd) {
        if (!text.contains("#L")) {
            sendNextS(text, type);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCAskNumber(this.id,text, def, min, max));
    }

    public void sendGetText(String text) {
        sendGetText(text, this.id);
    }

    public void sendGetText(String text, int id) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCAskText(id, text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public void sendPlayerOk(String text) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id,text));
    }

    public void sendPlayerNext(String text) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id,text, false,true));
    }

    public void sendPlayerNextPrev(String text) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text, true,true));
    }

    public void sendRevivePet(String text) {
        if (text.contains("#L")) {
            sendSimple(text);
//            return;
        }
    }

    public void sendPlayerStart(String text) {
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }

        this.c.sendPacket(NPCPacket.sendNPCSay(this.id, text));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.发型, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.脸型, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor((byte) color);
        getPlayer().updateSingleStat(MapleStat.皮肤, color);
        getPlayer().equipChanged();
    }

    public int setRandomAvatar(int ticket, int[] args_all) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);
        int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte) args);
            this.c.getPlayer().updateSingleStat(MapleStat.皮肤, args);
        } else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.脸型, args);
        } else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.发型, args);
        }
        this.c.getPlayer().equipChanged();
        return 1;
    }

    public int setAvatar(int ticket, int args) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);
        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte) args);
            this.c.getPlayer().updateSingleStat(MapleStat.皮肤, args);
        } else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.脸型, args);
        } else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.发型, args);
        }
        this.c.getPlayer().equipChanged();
        return 1;
    }

    public void sendStorage() {
        this.c.getPlayer().setConversation(4);
        this.c.getPlayer().getStorage().sendStorage(this.c, this.id);
    }

    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c);
    }

    public void openShopNPC(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c, this.id);
    }

    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, new StringBuilder().append(this.c.getPlayer().getMap().getStreetName()).append(" - ").append(this.c.getPlayer().getMap().getMapName()).toString());
    }

    public int gainGachaponItem(int id, int quantity, String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try {
            if (!ii.itemExists(id)) {
                return -1;
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short) quantity, new StringBuilder().append("从 ").append(msg).append(" 中获得时间: ").toString());
            if (item == null) {
                return -1;
            }
            byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            return item.getItemId();
        } catch (Exception e) {
            MapleLogger.error("gainGachaponItem 错误", e);
        }
        return -1;
    }

    public int gainGachaponItem(int id, int quantity, String msg, int rareness) {
        return gainGachaponItem(id, quantity, msg, rareness, false, 0L);
    }

    public int gainGachaponItem(int id, int quantity, String msg, int rareness, long period) {
        return gainGachaponItem(id, quantity, msg, rareness, false, period);
    }

    public int gainGachaponItem(int id, int quantity, String msg, int rareness, boolean buy) {
        return gainGachaponItem(id, quantity, msg, rareness, buy, 0L);
    }

    public int gainGachaponItem(int id, int quantity, String msg, int rareness, boolean buy, long period) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try {
            if (!ii.itemExists(id)) {
                return -1;
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short) quantity, new StringBuilder().append("从 ").append(msg).append(" 中").append(buy ? "购买" : "获得").append("时间: ").toString(), period);
            if (item == null) {
                return -1;
            }
            return item.getItemId();
        } catch (Exception e) {
            MapleLogger.error("gainGachaponItem 错误", e);
        }
        return -1;
    }

    public void changeJob(int jobId) {
        this.c.getPlayer().changeJob(jobId, true);
    }

    public void changeJobById(int jobId) {
        this.c.getPlayer().changeJob(jobId, true);
    }

    public boolean isValidJob(int jobId) {
        return MapleCarnivalChallenge.getJobNameByIdNull(jobId) != null;
    }

    public String getJobNameById(int jobId) {
        return MapleCarnivalChallenge.getJobNameByIdNull(jobId);
    }

    public void startQuest(int questId) {
        MapleQuestStatus tmp = getPlayer().getQuest(MapleQuest.getInstance(questId));
        if (tmp.getCustomData().isEmpty()) {
            MapleLogger.info("开始纯记录任务！");
            tmp.setStatus((byte)MapleQuestStatus.QUEST_STARTED);
        } else {
            MapleLogger.info("开始WZ任务");
            MapleQuest.getInstance(questId).start(getPlayer(), getNpc());
        }
    }

    public void completeQuest(int questId) {
        MapleLogger.info("任务附加数据："+getPlayer().getQuest(MapleQuest.getInstance(questId)).getCustomData());
        MapleQuestStatus tmp = getPlayer().getQuest(MapleQuest.getInstance(questId));
        if (tmp.getCustomData().isEmpty()) {
            MapleLogger.info("完成纯记录任务！");
            getPlayer().getQuest(MapleQuest.getInstance(questId)).setStatus((byte)MapleQuestStatus.QUEST_COMPLETED);
        } else {
            MapleLogger.info("完成WZ任务");
            MapleQuest.getInstance(questId).complete(getPlayer(), getNpc());
        }
    }

    public void forfeitQuest(int questId) {
        MapleQuest.getInstance(questId).forfeit(getPlayer());
    }

    @Override
    public void forceStartQuest(int questId) {
        MapleQuest.getInstance(questId).forceStart(getPlayer(), getNpc(), null);
    }

    @Override
    public void forceCompleteQuest(int questId) {
        MapleQuest.getInstance(questId).forceComplete(getPlayer(), getNpc());
    }

    public static boolean hairExists(int hair) {
        return MapleItemInformationProvider.getInstance().hairExists(hair);
    }

    public int[] getCanHair(int[] hairs) {
        List<Integer> canHair = new ArrayList();
        List<Integer> cantHair = new ArrayList();
        for (int hair : hairs) {
            if (hairExists(hair)) {
                canHair.add(hair);
            } else {
                cantHair.add(hair);
            }
        }
        if (cantHair.size() > 0 && c.getPlayer().isAdmin()) {
            StringBuilder sb = new StringBuilder("正在读取的发型里有");
            sb.append(cantHair.size()).append("个发型客户端不支持显示，已经被清除：");
            for (int i = 0; i < cantHair.size(); i++) {
                sb.append(cantHair.get(i));
                if (i < cantHair.size() - 1) {
                    sb.append(",");
                }
            }
            playerMessage(sb.toString());
        }
        int[] getHair = new int[canHair.size()];
        for (int i = 0; i < canHair.size(); i++) {
            getHair[i] = canHair.get(i);
        }
        return getHair;
    }

    public static boolean faceExists(int face) {
        return MapleItemInformationProvider.getInstance().faceExists(face);
    }

    public int[] getCanFace(int[] faces) {
        List<Integer> canFace = new ArrayList();
        List<Integer> cantFace = new ArrayList();
        for (int face : faces) {
            if (faceExists(face)) {
                canFace.add(face);
            } else {
                cantFace.add(face);
            }
        }
        if (cantFace.size() > 0 && c.getPlayer().isAdmin()) {
            StringBuilder sb = new StringBuilder("正在读取的脸型里有");
            sb.append(cantFace.size()).append("个脸型客户端不支持显示，已经被清除：");
            for (int i = 0; i < cantFace.size(); i++) {
                sb.append(cantFace.get(i));
                if (i < cantFace.size() - 1) {
                    sb.append(",");
                }
            }
            playerMessage(sb.toString());
        }
        int[] getFace = new int[canFace.size()];
        for (int i = 0; i < canFace.size(); i++) {
            getFace[i] = canFace.get(i);
        }
        return getFace;
    }

    public long getMeso() {
        return getPlayer().getMeso();
    }

    public void gainAp(int amount) {
        this.c.getPlayer().gainAp((short) amount);
    }

    public void expandInventory(byte type, int amt) {
        this.c.getPlayer().expandInventory(type, amt);
    }

    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List itemIds = new LinkedList();
        for (Item item : equipped.newList()) {
            itemIds.add(item.getPosition());
        }
        for (Iterator i$ = itemIds.iterator(); i$.hasNext();) {
            short ids = ((Short) i$.next());
            MapleInventoryManipulator.unequip(getClient(), ids, equip.getNextFreeSlot());
        }
    }

//    public void showEffect(boolean broadcast, String effect) {
//        if (broadcast) {
//            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
//        } else {
//            this.c.sendPacket(MaplePacketCreator.showEffect(effect));
//        }
//    }
//
//    public void playSound(boolean broadcast, String sound) {
//        if (broadcast) {
//            this.c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
//        } else {
//            this.c.sendPacket(MaplePacketCreator.playSound(sound));
//        }
//    }

    public void updateBuddyCapacity(int capacity) {
        this.c.getPlayer().setBuddyCapacity((byte) capacity);
    }

    public int getBuddyCapacity() {
        return this.c.getPlayer().getBuddyCapacity();
    }

    public int partyMembersInMap() {
        int inMap = 0;
        if (getPlayer().getParty() == null) {
            return inMap;
        }
        for (MapleCharacter chr : getPlayer().getMap().getCharactersThreadsafe()) {
            if ((chr.getParty() != null) && (chr.getParty().getId() == getPlayer().getParty().getId())) {
                inMap++;
            }
        }
        return inMap;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<>();// List chars = new LinkedList();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                if (ch != null) {
                    chars.add(ch);
                }
            }
        }
        MaplePartyCharacter chr;
        return chars;
    }

    public void warpPartyWithExp(int mapId, int exp) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter partychr : getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.c.getChannelServer().getPlayerStorage().getCharacterByName(partychr.getName());
            if (((chr.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (chr.getEventInstance() == getPlayer().getEventInstance())) {
                chr.changeMap(target, target.getPortal(0));
                chr.gainExp(exp, true, false, true);
            }
        }
    }

    public void warpPartyWithExpMeso(int mapId, int exp, long meso) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            gainMeso(meso);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter partychr : getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.c.getChannelServer().getPlayerStorage().getCharacterByName(partychr.getName());
            if (((chr.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (chr.getEventInstance() == getPlayer().getEventInstance())) {
                chr.changeMap(target, target.getPortal(0));
                chr.gainExp(exp, true, false, true);
                chr.gainMeso(meso, true);
            }
        }
    }

    public MapleSquad getSquad(String type) {
        return this.c.getChannelServer().getMapleSquad(type);
    }

    public int getSquadAvailability(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }

    public boolean registerSquad(String type, int minutes, String startText) {
        if (this.c.getChannelServer().getMapleSquad(type) == null) {
            MapleSquad squad = new MapleSquad(this.c.getChannel(), type, this.c.getPlayer(), minutes * 60 * 1000, startText);

            boolean ret = this.c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                MapleMap map = this.c.getPlayer().getMap();
                map.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
                map.broadcastMessage(MaplePacketCreator.serverMessageRedText(new StringBuilder().append(this.c.getPlayer().getName()).append(startText).toString()));
            } else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }

    public byte isSquadLeader(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if ((squad.getLeader() != null) && (squad.getLeader().getId() == this.c.getPlayer().getId())) {
                return 1;
            }
            return 0;
        }

        return -1;
    }

    public boolean reAdd(String eim, String squad) {
        EventInstanceManager eimz = getDisconnected(eim);
        MapleSquad squadz = getSquad(squad);
        if ((eimz != null) && (squadz != null)) {
            squadz.reAddMember(getPlayer());
            eimz.registerPlayer(getPlayer());
            return true;
        }
        return false;
    }

    public void banMember(String type, int pos) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }

    public void acceptMember(String type, int pos) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }

    public int addMember(String type, boolean join) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.addMember(this.c.getPlayer(), join);
        }
        return -1;
    }

    public byte isSquadMember(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if (squad.containsMember(this.c.getPlayer())) {
                return 1;
            }
            if (squad.isBanned(this.c.getPlayer())) {
                return 2;
            }
            return 0;
        }

        return -1;
    }

    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    public boolean removePlayerFromInstance() {
        if (this.c.getPlayer().getEventInstance() != null) {
            this.c.getPlayer().getEventInstance().removePlayer(this.c.getPlayer());
            return true;
        }
        return false;
    }

    public boolean isPlayerInstance() {
        return this.c.getPlayer().getEventInstance() != null;
    }

    public void changeStat(byte slot, int type, int amount) {
        Equip sel = (Equip) this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) slot);
        switch (type) {
            case 0:
                sel.setStr((short) amount);
                break;
            case 1:
                sel.setDex((short) amount);
                break;
            case 2:
                sel.setInt((short) amount);
                break;
            case 3:
                sel.setLuk((short) amount);
                break;
            case 4:
                sel.setHp((short) amount);
                break;
            case 5:
                sel.setMp((short) amount);
                break;
            case 6:
                sel.setWatk((short) amount);
                break;
            case 7:
                sel.setMatk((short) amount);
                break;
            case 8:
                sel.setWdef((short) amount);
                break;
            case 9:
                sel.setMdef((short) amount);
                break;
            case 10:
                sel.setAcc((short) amount);
                break;
            case 11:
                sel.setAvoid((short) amount);
                break;
            case 12:
                sel.setHands((short) amount);
                break;
            case 13:
                sel.setSpeed((short) amount);
                break;
            case 14:
                sel.setJump((short) amount);
                break;
            case 15:
                sel.setUpgradeSlots((byte) amount);
                break;
            case 17:
                sel.setLevel((byte) amount);
                break;
            case 18:
                sel.setState((byte) amount);
                break;
            case 19:
                sel.setEnhance((byte) amount);
                break;
            case 23:
                sel.setOwner(getText());
                break;
        }

        this.c.getPlayer().equipChanged();
        fakeRelog();
    }

    public void openMerchantItemStore() {
        this.c.getPlayer().setConversation(3);
    }

    public short getKegs() {
        return this.c.getChannelServer().getFireWorks().getKegsPercentage();
    }

    public void giveKegs(int kegs) {
        this.c.getChannelServer().getFireWorks().giveKegs(this.c.getPlayer(), kegs);
    }

    public short getSunshines() {
        return this.c.getChannelServer().getFireWorks().getSunsPercentage();
    }

    public void addSunshines(int kegs) {
        this.c.getChannelServer().getFireWorks().giveSuns(this.c.getPlayer(), kegs);
    }

    public short getDecorations() {
        return this.c.getChannelServer().getFireWorks().getDecsPercentage();
    }

    public void addDecorations(int kegs) {
        try {
            this.c.getChannelServer().getFireWorks().giveDecs(this.c.getPlayer(), kegs);
        } catch (Exception e) {
            MapleLogger.error("addDecorations 错误", e);
        }
    }

    public MapleCarnivalParty getCarnivalParty() {
        return this.c.getPlayer().getCarnivalParty();
    }

    public MapleCarnivalChallenge getNextCarnivalRequest() {
        return this.c.getPlayer().getNextCarnivalRequest();
    }

    public MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }

    public void maxStats() {
        Map statup = new EnumMap(MapleStat.class);
        this.c.getPlayer().getStat().str = 32767;
        this.c.getPlayer().getStat().dex = 32767;
        this.c.getPlayer().getStat().int_ = 32767;
        this.c.getPlayer().getStat().luk = 32767;

        this.c.getPlayer().getStat().setMaxHp(this.c.getPlayer().getMaxHpForSever(),this.c.getPlayer());
        this.c.getPlayer().getStat().setMaxMp(this.c.getPlayer().getMaxMpForSever(),this.c.getPlayer());
        this.c.getPlayer().getStat().setHp(this.c.getPlayer().getMaxHpForSever());
        this.c.getPlayer().getStat().setMp(this.c.getPlayer().getMaxMpForSever());

        statup.put(MapleStat.力量, 32767L);
        statup.put(MapleStat.敏捷, 32767L);
        statup.put(MapleStat.运气, 32767L);
        statup.put(MapleStat.智力, 32767L);
        statup.put(MapleStat.HP, (long) this.c.getPlayer().getMaxHpForSever());
        statup.put(MapleStat.MAXHP, (long) this.c.getPlayer().getMaxHpForSever());
        statup.put(MapleStat.MP, (long) this.c.getPlayer().getMaxMpForSever());
        statup.put(MapleStat.MAXMP, (long) this.c.getPlayer().getMaxMpForSever());
        this.c.getPlayer().getStat().recalcLocalStats(this.c.getPlayer());
        this.c.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this.c.getPlayer()));
    }

    public Triple<String, Map<Integer, String>, Long> getSpeedRun(String typ) {
        ExpeditionType types = ExpeditionType.valueOf(typ);
        if (SpeedRunner.getSpeedRunData(types) != null) {
            return SpeedRunner.getSpeedRunData(types);
        }
        return new Triple("", new HashMap(), 0L);
    }

    public boolean getSR(Triple<String, Map<Integer, String>, Long> ma, int sel) {
        if ((((Map) ma.mid).get(sel) == null) || (((String) ((Map) ma.mid).get(sel)).length() <= 0)) {
            dispose();
            return false;
        }
        sendOk((String) ((Map) ma.mid).get(sel));
        return true;
    }

    public Equip getEquip(int itemid) {
        return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }

    public void setExpiration(Object statsSel, long expire) {
        if ((statsSel instanceof Equip)) {
            ((Equip) statsSel).setExpiration(System.currentTimeMillis() + expire * 24L * 60L * 60L * 1000L);
        }
    }

    public void setLock(Object statsSel) {
        if ((statsSel instanceof Equip)) {
            Equip eq = (Equip) statsSel;
            if (eq.getExpiration() == -1L) {
                eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.封印.getValue()));
            } else {
                eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.不可交易.getValue()));
            }
        }
    }

    public boolean addFromDrop(Object statsSel) {
        if ((statsSel instanceof Item)) {
            Item it = (Item) statsSel;
            return (MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner())) && (MapleInventoryManipulator.addFromDrop(getClient(), it, false));
        }
        return false;
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
        return replaceItem(slot, invType, statsSel, offset, type, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        Item item = getPlayer().getInventory(inv).getItem((short) (byte) slot);
        if ((item == null) || ((statsSel instanceof Item))) {
            item = (Item) statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            Equip eq = (Equip) item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                }
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() - 1));

                if (eq.getExpiration() == -1L) {
                    eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.封印.getValue()));
                } else {
                    eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.不可交易.getValue()));
                }
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte) (eq.getLevel() + offset));
            }else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short) (eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short) (eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short) (eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short) (eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short) (eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short) (eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short) (eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short) (eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short) (eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short) (eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("ACC")) {
                eq.setAcc((short) (eq.getAcc() + offset));
            } else if (type.equalsIgnoreCase("Avoid")) {
                eq.setAvoid((short) (eq.getAvoid() + offset));
            } else if (type.equalsIgnoreCase("Hands")) {
                eq.setHands((short) (eq.getHands() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short) (eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("Jump")) {
                eq.setJump((short) (eq.getJump() + offset));
            } else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration(eq.getExpiration() + offset);
            } else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((short) (byte) (eq.getFlag() + offset));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
        return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }

    public boolean isCash(int itemId) {
        return MapleItemInformationProvider.getInstance().isCash(itemId);
    }

    public int getTotalStat(int itemId) {
        return MapleItemInformationProvider.getInstance().getTotalStat((Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId));
    }

    public int getReqLevel(int itemId) {
        return MapleItemInformationProvider.getInstance().getReqLevel(itemId);
    }

    public MapleStatEffect getEffect(int buff) {
        return MapleItemInformationProvider.getInstance().getItemEffect(buff);
    }

    public void buffGuild(int buff, int duration, String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleStatEffect mse;
        if ((ii.getItemEffect(buff) != null) && (getPlayer().getGuildId() > 0)) {
            mse = ii.getItemEffect(buff);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr.getGuildId() == getPlayer().getGuildId()) {
                        mse.applyTo(chr, chr, true, null, duration);
                        chr.dropMessage(5, new StringBuilder().append("Your guild has gotten a ").append(msg).append(" buff.").toString());
                    }
                }
            }
        }
    }

    public boolean hasSkill(int skillid) {
        Skill theSkill = SkillFactory.getSkill(skillid);
        if (theSkill != null) {
            return this.c.getPlayer().getSkillLevel(theSkill) > 0;
        }
        return false;
    }

    public void maxAllSkills() {
        HashMap sDate = new HashMap();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (skil.getId() < 90000000) {
                sDate.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        getPlayer().changeSkillsLevel(sDate);
        sDate.clear();
    }

    public void maxSkillsByJob() {
        List<Integer> skillIds = new ArrayList();
        HashMap<Skill, SkillEntry> sDate = new HashMap();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if ((skil.canBeLearnedBy(getPlayer().getJob())) && (!GameConstants.is新手职业(skil.getId() / 10000)) && (!skil.isSpecialSkill())) {
                sDate.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                skillIds.add(skil.getId());
            }
        }
        getPlayer().changeSkillsLevel(sDate);
        Collections.sort(skillIds);
        String job;
        Iterator i$;
        if (getPlayer().isShowPacket()) {
            job = new StringBuilder().append("Skill\\").append(MapleCarnivalChallenge.getJobNameById(getPlayer().getJob())).append(".txt").toString();
            for (i$ = skillIds.iterator(); i$.hasNext();) {
                Integer skillId = (Integer) i$.next();
                for (Entry<Skill, SkillEntry> data : sDate.entrySet()) {
                    if (((Skill) data.getKey()).getId() == skillId) {
                        String txt = new StringBuilder().append("public static final int ").append(((Skill) data.getKey()).getName()).append(" = ").append(((Skill) data.getKey()).getId()).append("; //技能最大等级").append(((Skill) data.getKey()).getMaxLevel()).toString();
                        MapleLogger.info(txt);
                    }
                }
            }
        }
        Integer skillId;
        sDate.clear();
        skillIds.clear();
    }

    public void clearSkills() {
        this.c.getPlayer().clearSkills();
    }

    public void maxHyperSkillsByJob() {
        List skillIds = new ArrayList();
        HashMap<Skill, SkillEntry> sDate = new HashMap();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if ((skil.canBeLearnedBy(getPlayer().getJob()))) {
                sDate.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                skillIds.add(skil.getId());
            }
        }
        getPlayer().changeSkillsLevel(sDate);
        Collections.sort(skillIds);
        String job;
        Iterator i$;
        if (getPlayer().isShowPacket()) {
            job = new StringBuilder().append("Skill\\").append(MapleCarnivalChallenge.getJobNameById(getPlayer().getJob())).append(".txt").toString();
            for (i$ = skillIds.iterator(); i$.hasNext();) {
                Integer skillId = (Integer) i$.next();
                for (Map.Entry data : sDate.entrySet()) {
                    if (((Skill) data.getKey()).getId() == skillId) {
                        String txt = new StringBuilder().append("public static final int ").append(((Skill) data.getKey()).getName()).append(" = ").append(((Skill) data.getKey()).getId()).append("; //技能最大等级").append(((Skill) data.getKey()).getMaxLevel()).toString();
                        MapleLogger.info(txt);
                    }
                }
            }
        }
        Integer skillId;
        sDate.clear();
        skillIds.clear();
    }

    public void resetStats(int str, int dex, int z, int luk) {
        this.c.getPlayer().resetStats(str, dex, z, luk);
    }

    public boolean dropItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.drop(this.c, inv, (short) slot, (short) quantity, true);
    }

    public boolean removeItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.removeFromSlot(this.c, inv, (short) slot, (short) quantity, true);
    }

    public void setQuestRecord(Object ch, int questid, String data) {
        ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }

    public void doRing(String name, int itemid) {
        PlayersHandler.DoRing(getClient(), name, itemid);
    }

    public int getNaturalStats(int itemid, String it) {
        Map eqStats = MapleItemInformationProvider.getInstance().getEquipStats(itemid);
        if ((eqStats != null) && (eqStats.containsKey(it))) {
            return ((Integer) eqStats.get(it));
        }
        return 0;
    }

    public boolean isEligibleName(String t) {
        return (MapleCharacterUtil.canCreateChar(t, getPlayer().isGM())) && ((!LoginInformationProvider.getInstance().isForbiddenName(t)) || (getPlayer().isGM()));
    }

    public String checkDrop(int mobId) {
        List ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        if ((ranks != null) && (ranks.size() > 0)) {
            int num = 0;

            StringBuilder name = new StringBuilder();
            for (Object rank : ranks) {
                MonsterDropEntry de = (MonsterDropEntry) rank;
                if ((de.chance > 0) && ((de.questid <= 0) || ((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0)))) {
                    int itemId = de.itemId;
                    if (num == 0) {
                        name.append("当前怪物 #o").append(mobId).append("# 的爆率为:\r\n");
                        name.append("--------------------------------------\r\n");
                    }

                    String namez = new StringBuilder().append("#z").append(itemId).append("#").toString();
                    if (itemId == 0) {
                        itemId = 4031041;
                        namez = new StringBuilder().append(de.Minimum * getClient().getChannelServer().getMesoRate(getPlayer().getWorld())).append(" - ").append(de.Maximum * getClient().getChannelServer().getMesoRate(getPlayer().getWorld())).append(" 的金币").toString();
                    }
                    int chance = de.chance * getClient().getChannelServer().getDropRate(getPlayer().getWorld());
                    if (getPlayer().isShowPacket()) {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ").append(Integer.valueOf(chance >= 999999 ? 1000000 : chance).doubleValue() / 10000.0D).append("%的爆率. ").append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    } else {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    }
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "没有找到这个怪物的爆率数据。";
    }

    public String checkMapDrop() {
        List ranks = new ArrayList(MapleMonsterInformationProvider.getInstance().getGlobalDrop());
        int mapid = this.c.getPlayer().getMap().getId();
        int cashServerRate = getClient().getChannelServer().getCashRate();
        int globalServerRate = getClient().getChannelServer().getGlobalRate();
        if (ranks.size() > 0) {
            int num = 0;

            StringBuilder name = new StringBuilder();
            for (Object rank : ranks) {
                MonsterGlobalDropEntry de = (MonsterGlobalDropEntry) rank;
                if ((de.continent < 0) || ((de.continent < 10) && (mapid / 100000000 == de.continent)) || ((de.continent < 100) && (mapid / 10000000 == de.continent)) || ((de.continent < 1000) && (mapid / 1000000 == de.continent))) {
                    int itemId = de.itemId;
                    if (num == 0) {
                        name.append("当前地图 #r").append(mapid).append("#k - #m").append(mapid).append("# 的全局爆率为:");
                        name.append("\r\n--------------------------------------\r\n");
                    }
                    String names = new StringBuilder().append("#z").append(itemId).append("#").toString();
                    if ((itemId == 0) && (cashServerRate != 0)) {
                        itemId = 4031041;
                        names = new StringBuilder().append(de.Minimum * cashServerRate).append(" - ").append(de.Maximum * cashServerRate).append(" 的抵用卷").toString();
                    }
                    int chance = de.chance * globalServerRate;
                    if (getPlayer().isShowPacket()) {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(names).append(" - ").append(Integer.valueOf(chance >= 999999 ? 1000000 : chance).doubleValue() / 10000.0D).append("%的爆率. ").append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    } else {
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(names).append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("需要接受任务: ").append(MapleQuest.getInstance(de.questid).getName()).toString() : "").append("\r\n");
                    }
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }
        return "当前地图没有设置全局爆率。";
    }

    public void outputWithLogging(int mobId, String buff) {
        String file = new StringBuilder().append("drop_data\\").append(mobId).append(".sql").toString();
        MapleLogger.info(buff);
    }

    public List<BattleConstants.PokedexEntry> getAllPokedex() {
        return BattleConstants.getAllPokedex();
    }

    public String getLeftPadded(String in, char padchar, int length) {
        return StringUtil.getLeftPaddedStr(in, padchar, length);
    }

    public List<Integer> makeTeam(int lowRange, int highRange, int neededLevel, int restrictedLevel) {
        List ret = new ArrayList();
        int averageLevel = 0;
        int numBattlers = 0;
        for (Battler b : getPlayer().getBattlers()) {
            if (b != null) {
                if (b.getLevel() > averageLevel) {
                    averageLevel = b.getLevel();
                }
                numBattlers++;
            }
        }
        boolean hell = lowRange == highRange;
        if ((numBattlers < 3) || (averageLevel < neededLevel)) {
            return null;
        }
        if (averageLevel > restrictedLevel) {
            averageLevel = restrictedLevel;
        }
        List<PokedexEntry> pokeEntries = new ArrayList(getAllPokedex());
        Collections.shuffle(pokeEntries);
        while (ret.size() < numBattlers) {
            for (BattleConstants.PokedexEntry d : pokeEntries) {
                if (((d.dummyBattler.getStats().isBoss()) && (hell)) || ((!d.dummyBattler.getStats().isBoss()) && (!hell))) {
                    if (!hell) {
                        if ((d.dummyBattler.getLevel() <= averageLevel + highRange) && (d.dummyBattler.getLevel() >= averageLevel + lowRange) && (Randomizer.nextInt(numBattlers) == 0)) {
                            ret.add(d.id);
                            if (ret.size() >= numBattlers) {
                                break;
                            }
                        }
                    } else if ((d.dummyBattler.getFamily().type != BattleConstants.MobExp.EASY) && (d.dummyBattler.getLevel() >= neededLevel) && (d.dummyBattler.getLevel() <= averageLevel) && (Randomizer.nextInt(numBattlers) == 0)) {
                        ret.add(d.id);
                        if (ret.size() >= numBattlers) {
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    public BattleConstants.HoldItem[] getAllHoldItems() {
        return BattleConstants.HoldItem.values();
    }

    public String getReadableMillis(long startMillis, long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }

    public boolean canCreateUltimate() {
        if (getPlayer().getLevel() < 120) {
            return false;
        }
        int jobId = getPlayer().getJob();
        return (jobId == 1111) || (jobId == 1112) || (jobId == 1211) || (jobId == 1212) || (jobId == 1311) || (jobId == 1312) || (jobId == 1411) || (jobId == 1412) || (jobId == 1511) || (jobId == 1512);
    }

    public String getRankingInformation(int job) {
        StringBuilder sb = new StringBuilder();
        for (RankingWorker.RankingInformation pi : RankingWorker.getRankingInfo(job)) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public String getPokemonRanking() {
        StringBuilder sb = new StringBuilder();
        for (RankingWorker.PokemonInformation pi : RankingWorker.getPokemonInfo()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public String getPokemonRanking_Caught() {
        StringBuilder sb = new StringBuilder();
        for (RankingWorker.PokedexInformation pi : RankingWorker.getPokemonCaught()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public String getPokemonRanking_Ratio() {
        StringBuilder sb = new StringBuilder();
        for (RankingWorker.PokebattleInformation pi : RankingWorker.getPokemonRatio()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public Triple<Integer, Integer, Integer> getCompensation() {
        Triple ret = null;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM compensationlog_confirmed WHERE chrname LIKE ?")) {
                ps.setString(1, getPlayer().getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret = new Triple(rs.getInt("value"), rs.getInt("taken"), rs.getInt("donor"));
                    }
                }
                ps.close();
            }
            return ret;
        } catch (SQLException e) {
            MapleLogger.error("getCompensation error:", e);
        }
        return ret;
    }

    public boolean deleteCompensation(int taken) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE compensationlog_confirmed SET taken = ? WHERE chrname LIKE ?")) {
                ps.setInt(1, taken);
                ps.setString(2, getPlayer().getName());
                ps.executeUpdate();
                ps.close();
            }
            return true;
        } catch (SQLException e) {
            MapleLogger.error("deleteCompensation error:", e);
        }
        return false;
    }

    public void testPacket(String testmsg) {
        this.c.sendPacket(MaplePacketCreator.testPacket(testmsg));
    }

    public void testPacket(String op, String msg) {
        this.c.sendPacket(MaplePacketCreator.testPacket(op, msg));
    }

    public short getSpace(byte type) {
        return getPlayer().getSpace(type);
    }

    public boolean haveSpace(int type) {
        return getPlayer().haveSpace(type);
    }

    public boolean haveSpaceForId(int itemid) {
        return getPlayer().haveSpaceForId(itemid);
    }

    public int getMoney() {
        int money = 0;
        try {
            int cid = getPlayer().getId();
            Connection con = DatabaseConnection.getConnection();
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("select * from bank where charid=?")) {
                ps.setInt(1, cid);
                rs = ps.executeQuery();
                if (rs.next()) {
                    money = rs.getInt("money");
                } else {
                    try (PreparedStatement psu = con.prepareStatement("insert into bank (charid, money) VALUES (?, ?)")) {
                        psu.setInt(1, cid);
                        psu.setInt(2, 0);
                        psu.executeUpdate();
                        ps.close();
                    }
                }
            }
            rs.close();
        } catch (SQLException ex) {
            MapleLogger.error("银行存款获取信息发生错误", ex);
        }
        return money;
    }

    public int addMoney(int money, int type) {
        try {
            int cid = getPlayer().getId();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from bank where charid=?");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ((type == 1)
                        && (money > rs.getInt("money"))) {
                    return -1;
                }

                ps = con.prepareStatement(new StringBuilder().append("UPDATE bank SET money =money+ ").append(money).append(" WHERE charid = ").append(cid).append("").toString());
                return ps.executeUpdate();
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            MapleLogger.error("银行存款添加数量发生错误", ex);
        }
        return 0;
    }

    public int getHyPay(int type) {
        return getPlayer().getHyPay(type);
    }

    public int addHyPay(int hypay) {
        return getPlayer().addHyPay(hypay);
    }

    public int delPayReward(int pay) {
        return getPlayer().delPayReward(pay);
    }

    public void fakeRelog() {
        if ((!this.c.getPlayer().isAlive()) || (this.c.getPlayer().getEventInstance() != null) || (FieldLimitType.ChannelSwitch.check(this.c.getPlayer().getMap().getFieldLimit()))) {
            this.c.getPlayer().dropMessage(1, "刷新人物数据失败.");
            return;
        }
        this.c.getPlayer().dropMessage(5, "正在刷新人数据.请等待...");
        this.c.getPlayer().fakeRelog();
    }

    public MapleCharacter getCharByName(String name) {
        try {
            return this.c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        } catch (Exception e) {
        }
        return null;
    }

    public String EquipList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<String> stra = new LinkedList();
        for (Item item : equip.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String UseList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory use = c.getPlayer().getInventory(MapleInventoryType.USE);
        List<String> stra = new LinkedList();
        for (Item item : use.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String CashList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory cash = c.getPlayer().getInventory(MapleInventoryType.CASH);
        List<String> stra = new LinkedList();
        for (Item item : cash.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String EtcList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory etc = c.getPlayer().getInventory(MapleInventoryType.ETC);
        List<String> stra = new LinkedList();
        for (Item item : etc.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String SetupList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory setup = c.getPlayer().getInventory(MapleInventoryType.SETUP);
        List<String> stra = new LinkedList();
        for (Item item : setup.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public void deleteAll(int itemId) {
        MapleInventoryManipulator.removeAllById(getClient(), itemId, true);
    }

    public int getCurrentSharesPrice() {
        return ChannelServer.getInstance(1).getSharePrice();
    }

    public int getDollars() {
        return getPlayer().getDollars();
    }

    public int getShareLots() {
        return getPlayer().getShareLots();
    }

    public void addDollars(int n) {
        getPlayer().addDollars(n);
    }

    public void addShareLots(int n) {
        getPlayer().addShareLots(n);
    }

    public void giveMountSkill(int itemId, int mountSkillId, long period) {
        giveMountSkill(itemId, mountSkillId, period, false);
    }

    public void giveMountSkill(int itemId, int mountSkillId, long period, boolean test) {
        if ((mountSkillId > 0) && (haveItem(itemId))) {
            if (test) {
                System.err.println(new StringBuilder().append("骑宠技能 - 1 ").append(mountSkillId).append(" LinkedMountItem: ").append(mountSkillId % 1000).toString());
            }
            mountSkillId = mountSkillId > 80001000 ? mountSkillId : PlayerStats.getSkillByJob(mountSkillId, this.c.getPlayer().getJob());
            int fk = GameConstants.getMountItem(mountSkillId, this.c.getPlayer());
            if (test) {
                System.err.println(new StringBuilder().append("骑宠技能 - 2 ").append(mountSkillId).append(" 骑宠ID: ").append(fk).toString());
            }
            if ((fk > 0) && (mountSkillId < 80001000)) {
                for (int i = 80001001; i < 80001999; i++) {
                    Skill skill = SkillFactory.getSkill(i);
                    if ((skill != null) && (GameConstants.getMountItem(skill.getId(), this.c.getPlayer()) == fk)) {
                        mountSkillId = i;
                        break;
                    }
                }
            }
            if (test) {
                System.err.println(new StringBuilder().append("骑宠技能 - 3 ").append(mountSkillId).append(" 技能是否为空: ").append(SkillFactory.getSkill(mountSkillId) == null).append(" 骑宠: ").append(GameConstants.getMountItem(mountSkillId, this.c.getPlayer()) == 0).toString());
            }
            if (this.c.getPlayer().getSkillLevel(mountSkillId) > 0) {
                this.c.getPlayer().dropMessage(1, new StringBuilder().append("您已经拥有了[").append(SkillFactory.getSkill(mountSkillId).getName()).append("]这个骑宠的技能，无法使用该道具。").toString());
            } else if ((SkillFactory.getSkill(mountSkillId) == null) || (GameConstants.getMountItem(mountSkillId, this.c.getPlayer()) == 0)) {
                this.c.getPlayer().dropMessage(1, "暂时无法使用这个骑宠的技能.");
            } else if (period > 0L) {
                gainItem(itemId, (short) -1);
                this.c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountSkillId), (byte) 1, (byte) 1, System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                this.c.getPlayer().dropMessage(1, new StringBuilder().append("恭喜您获得[").append(SkillFactory.getSkill(mountSkillId).getName()).append("]骑宠技能 ").append(period).append(" 权。").toString());
            } else if (period == -1L) {
                gainItem(itemId, (short) -1);
                this.c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountSkillId), (byte) 1, (byte) 1, -1L);
                this.c.getPlayer().dropMessage(1, new StringBuilder().append("恭喜您获得[").append(SkillFactory.getSkill(mountSkillId).getName()).append("]骑宠技能永久权。").toString());
            }
        } else {
            this.c.getPlayer().dropMessage(1, new StringBuilder().append("暂时无法使用这个骑宠的技能\r\n我的道具ID为: ").append(itemId).toString());
        }
        this.c.sendPacket(MaplePacketCreator.enableActions());
    }

    public boolean checkLevelAndItem(int minLevel, int maxLevel, int itemId) {
        return checkLevelAndItem(minLevel, maxLevel, itemId, 2);
    }

    public boolean checkLevelAndItem(int minLevel, int maxLevel, int itemId, int minSize) {
        MapleParty party = this.c.getPlayer().getParty();
        if ((party == null) || (party.getLeader().getId() != this.c.getPlayer().getId())) {
            this.c.getPlayer().dropMessage(5, "您没有队伍 或者 不是队长..");
            return false;
        }
        int partySize = party.getMembers().size();
        if (partySize < minSize) {
            this.c.getPlayer().dropMessage(5, new StringBuilder().append("队伍的人数成员不够 必须 ").append(minSize).append(" 人才可以开始组队任务，当前队伍人数: ").append(partySize).toString());
            return false;
        }
        int chrSize = 0;
        for (MaplePartyCharacter partyPlayer : party.getMembers()) {
            MapleCharacter player = getPlayer().getMap().getCharacterById(partyPlayer.getId());
            if (player == null) {
                this.c.getPlayer().dropMessage(5, new StringBuilder().append("队伍中的成员 ").append(partyPlayer.getName()).append(" 不在线 或者 不在同一地图.").toString());
            } else if ((player.getLevel() < minLevel) || (player.getLevel() > maxLevel)) {
                this.c.getPlayer().dropMessage(5, new StringBuilder().append("队伍中的成员 ").append(partyPlayer.getName()).append(" 等级不符合要求.等级限制: Lv.").append(minLevel).append("～").append(maxLevel).toString());
            } else if (!player.haveItem(itemId)) {
                this.c.getPlayer().dropMessage(5, new StringBuilder().append("队伍中的成员 ").append(partyPlayer.getName()).append(" 没有开始组队任务需要的道具.").toString());
            } else {
                chrSize++;
            }
        }
        return partySize == chrSize;
    }

    public int getMin() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public int getSec() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    public void sendchangeMap(int mapid) {
        final MapleMap mapto = c.getChannelServer().getMapFactory().getMap(mapid);
        c.getPlayer().changeMap(mapto, mapto.getPortal(0));
    }

    /**
     * 是否为新手
     * @return
     */
    public boolean isBeginner(){
        return c.getPlayer().getJob() == MapleJob.新手.getId();
    }

    public boolean isMagician(){ return c.getPlayer().getJob() == MapleJob.魔法师.getId();}
}
