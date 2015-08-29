package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import scripting.quest.QuestScriptManager;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleQuest implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private static final Map<Integer, MapleQuest> quests = new LinkedHashMap();
    protected int id;
    protected List<MapleQuestRequirement> startReqs = new LinkedList();
    protected List<MapleQuestRequirement> completeReqs = new LinkedList();
    protected List<MapleQuestAction> startActs = new LinkedList();
    protected List<MapleQuestAction> completeActs = new LinkedList();
    protected Map<String, List<Pair<String, Pair<String, Integer>>>> partyQuestInfo = new LinkedHashMap();
    protected Map<Integer, Integer> relevantMobs = new LinkedHashMap();
    protected Map<Integer, Integer> questItems = new LinkedHashMap();
    private boolean autoStart = false;
    private boolean autoPreComplete = false;
    private boolean repeatable = false;
    private boolean customend = false;
    private boolean blocked = false;
    private boolean autoAccept = false;
    private boolean autoComplete = false;
    private boolean scriptedStart = false;
    private int viewMedalItem = 0;
    private int selectedSkillID = 0;
    protected String name = "";

    protected MapleQuest(int id) {
        this.id = id;
    }

    private static MapleQuest loadQuest(ResultSet questData, PreparedStatement questReqData, PreparedStatement questActData, PreparedStatement pss, PreparedStatement psq, PreparedStatement psi, PreparedStatement psp) throws SQLException {
        MapleQuest ret = new MapleQuest(questData.getInt("questid"));
        ret.name = questData.getString("name");
        ret.autoStart = (questData.getInt("autoStart") > 0);
        ret.autoPreComplete = (questData.getInt("autoPreComplete") > 0);
        ret.autoAccept = (questData.getInt("autoAccept") > 0);
        ret.autoComplete = (questData.getInt("autoComplete") > 0);
        ret.viewMedalItem = questData.getInt("viewMedalItem");
        ret.selectedSkillID = questData.getInt("selectedSkillID");
        ret.blocked = (questData.getInt("blocked") > 0);

        questReqData.setInt(1, ret.id);
        ResultSet rse = questReqData.executeQuery();
        while (rse.next()) {
            MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(rse.getString("name"));
            MapleQuestRequirement req = new MapleQuestRequirement(ret, type, rse);
            if (type.equals(MapleQuestRequirementType.interval)) {
                ret.repeatable = true;
            } else if (type.equals(MapleQuestRequirementType.normalAutoStart)) {
                ret.repeatable = true;
                ret.autoStart = true;
            } else if (type.equals(MapleQuestRequirementType.startscript)) {
                ret.scriptedStart = true;
            } else if (type.equals(MapleQuestRequirementType.endscript)) {
                ret.customend = true;
            } else if (type.equals(MapleQuestRequirementType.mob)) {
                for (Pair<Integer, Integer> mob : req.getDataStore()) {
                    ret.relevantMobs.put(mob.left, mob.right);
                }
            } else if (type.equals(MapleQuestRequirementType.item)) {
                for (Pair<Integer, Integer> it : req.getDataStore()) {
                    ret.questItems.put(it.left, it.right);
                }
            }
            if (rse.getInt("type") == 0) {
                ret.startReqs.add(req);
            } else {
                ret.completeReqs.add(req);
            }
        }
        rse.close();

        questActData.setInt(1, ret.id);
        rse = questActData.executeQuery();
        while (rse.next()) {
            MapleQuestActionType ty = MapleQuestActionType.getByWZName(rse.getString("name"));
            if (rse.getInt("type") == 0) {
                if ((ty == MapleQuestActionType.item) && (ret.id == 7103)) {
                    continue;
                }
                ret.startActs.add(new MapleQuestAction(ty, rse, ret, pss, psq, psi));
            } else {
                if ((ty == MapleQuestActionType.item) && (ret.id == 7102)) {
                    continue;
                }
                ret.completeActs.add(new MapleQuestAction(ty, rse, ret, pss, psq, psi));
            }
        }
        rse.close();

        psp.setInt(1, ret.id);
        rse = psp.executeQuery();
        while (rse.next()) {
            if (!ret.partyQuestInfo.containsKey(rse.getString("rank"))) {
                ret.partyQuestInfo.put(rse.getString("rank"), new ArrayList());
            }
            ((List) ret.partyQuestInfo.get(rse.getString("rank"))).add(new Pair(rse.getString("mode"), new Pair(rse.getString("property"), rse.getInt("value"))));
        }
        rse.close();
        return ret;
    }

    public List<Pair<String, Pair<String, Integer>>> getInfoByRank(String rank) {
        return (List) this.partyQuestInfo.get(rank);
    }

    public boolean isPartyQuest() {
        return this.partyQuestInfo.size() > 0;
    }

    public int getSkillID() {
        return this.selectedSkillID;
    }

    public String getName() {
        return this.name;
    }

    public List<MapleQuestAction> getCompleteActs() {
        return this.completeActs;
    }

    public static void initQuests(boolean reload) {
        if (reload) {
            quests.clear();
        }
        if (!quests.isEmpty() ) {
            return;
        }
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement questData = con.prepareStatement("SELECT * FROM wz_questdata");
                PreparedStatement psr = con.prepareStatement("SELECT * FROM wz_questreqdata WHERE questid = ?");
                PreparedStatement psa = con.prepareStatement("SELECT * FROM wz_questactdata WHERE questid = ?");
                PreparedStatement pss = con.prepareStatement("SELECT * FROM wz_questactskilldata WHERE uniqueid = ?");
                PreparedStatement psq = con.prepareStatement("SELECT * FROM wz_questactquestdata WHERE uniqueid = ?");
                PreparedStatement psi = con.prepareStatement("SELECT * FROM wz_questactitemdata WHERE uniqueid = ?");
                PreparedStatement psp = con.prepareStatement("SELECT * FROM wz_questpartydata WHERE questid = ?");
                ResultSet rs = questData.executeQuery();) {
            while (rs.next()) {
                quests.put(rs.getInt("questid"), loadQuest(rs, psr, psa, pss, psq, psi, psp));
            }
            questData.close();
            psr.close();
            psa.close();
            pss.close();
            psq.close();
            psi.close();
            psp.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("共加载 " + quests.size() + " 个任务信息.");
    }

    public static MapleQuest getInstance(int id) {
        MapleQuest ret = (MapleQuest) quests.get(Integer.valueOf(id));
        if (ret == null) {
            ret = new MapleQuest(id);
            quests.put(id, ret);
        }
        return ret;
    }

    public static Collection<MapleQuest> getAllInstances() {
        Map<Integer, MapleQuest> mapVK = new TreeMap<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer obj1, Integer obj2) {
                        Integer v1 = obj1;
                        Integer v2 = obj2;
                        int s = v1.compareTo(v2);
                        return s;
                    }
                }
        );

        Set col = quests.keySet();
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            Integer key = (Integer) iter.next();
            MapleQuest value = (MapleQuest) quests.get(key);
            mapVK.put(key, value);
        }
        return mapVK.values();
    }

    public boolean canStart(MapleCharacter chr, Integer npcid) {
        if ((chr.getQuest(this).getStatus() != 0) && ((chr.getQuest(this).getStatus() != 2) || (!this.repeatable))) {
            if (chr.isShowPacket()) {
                chr.dropMessage(6, new StringBuilder().append("开始任务 canStart: ").append(chr.getQuest(this).getStatus() != 0).append(" - ").append((chr.getQuest(this).getStatus() != 2) || (!this.repeatable)).append(" repeatable: ").append(this.repeatable).toString());
            }
            return false;
        }
        if ((this.blocked) && (!chr.isGM())) {
            if (chr.isShowPacket()) {
                chr.dropMessage(6, new StringBuilder().append("开始任务 canStart - blocked ").append(this.blocked).toString());
            }
            return false;
        }

        for (MapleQuestRequirement r : this.startReqs) {
            if ((r.getType() == MapleQuestRequirementType.dayByDay) && (npcid != null)) {
                forceComplete(chr, npcid);
                return false;
            }
            if (!r.check(chr, npcid)) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(6, new StringBuilder().append("开始任务 canStart - check ").append(!r.check(chr, npcid)).toString());
                }
                return false;
            }
        }
        return true;
    }

    public boolean canComplete(MapleCharacter chr, Integer npcid) {
        if (chr.getQuest(this).getStatus() != 1) {
            return false;
        }
        if ((this.blocked) && (!chr.isGM())) {
            return false;
        }
        if ((this.autoComplete) && (npcid != null) && (this.viewMedalItem <= 0)) {
            forceComplete(chr, npcid);
            return false;
        }
        for (MapleQuestRequirement r : this.completeReqs) {
            if (!r.check(chr, npcid)) {
                return false;
            }
        }
        return true;
    }

    public void RestoreLostItem(MapleCharacter chr, int itemid) {
        if ((this.blocked) && (!chr.isGM())) {
            return;
        }
        for (MapleQuestAction a : this.startActs) {
            if (a.RestoreLostItem(chr, itemid)) {
                break;
            }
        }
    }

    public void start(MapleCharacter chr, int npc) {
        if (chr.isShowPacket()) {
            chr.dropMessage(6, new StringBuilder().append("开始任务 start: ").append(npc).append(" autoStart：").append(this.autoStart).append(" checkNPCOnMap: ").append(checkNPCOnMap(chr, npc)).append(" canStart: ").append(canStart(chr, npc)).toString());
        }
        if (((this.autoStart) || (checkNPCOnMap(chr, npc))) && (canStart(chr, npc))) {
            for (MapleQuestAction a : this.startActs) {
                if (!a.checkEnd(chr, null)) {
                    return;
                }
            }
            for (MapleQuestAction a : this.startActs) {
                a.runStart(chr, null);
            }
            if (!this.customend) {
                forceStart(chr, npc, null);
            } else {
                QuestScriptManager.getInstance().startQuest(chr.getClient(), npc, getId());
//                QuestScriptManager.getInstance().endQuest(chr.getClient(), npc, getId(), true);//明明是start,为什么运行end
            }
        }
    }

    public void complete(MapleCharacter chr, int npc) {
        complete(chr, npc, null);
    }

    public void complete(MapleCharacter chr, int npc, Integer selection) {
        if ((chr.getMap() != null) && ((this.autoPreComplete) || (checkNPCOnMap(chr, npc))) && (canComplete(chr, npc))) {
            for (MapleQuestAction a : this.completeActs) {
                if (!a.checkEnd(chr, selection)) {
                    return;
                }
            }
            forceComplete(chr, npc);
            for (MapleQuestAction a : this.completeActs) {
                a.runEnd(chr, selection);
            }
            chr.getClient().getSession().write(MaplePacketCreator.showSpecialEffect(0x0E));
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showSpecialEffect(chr.getId(), 0x0E), false);
        }
    }

    public void forfeit(MapleCharacter chr) {
        if (chr.getQuest(this).getStatus() != 1) {
            return;
        }
        MapleQuestStatus oldStatus = chr.getQuest(this);
        MapleQuestStatus newStatus = new MapleQuestStatus(this, 0);
        newStatus.setForfeited(oldStatus.getForfeited() + 1);
        newStatus.setCompletionTime(oldStatus.getCompletionTime());
        chr.updateQuest(newStatus);
    }

    public void forceStart(MapleCharacter chr, int npc, String customData) {
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 1, npc);
        newStatus.setForfeited(chr.getQuest(this).getForfeited());
        newStatus.setCompletionTime(chr.getQuest(this).getCompletionTime());
        newStatus.setCustomData(customData);
        chr.updateQuest(newStatus);
    }

    public void forceComplete(MapleCharacter chr, int npc) {
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 2, npc);
        newStatus.setForfeited(chr.getQuest(this).getForfeited());
        chr.updateQuest(newStatus);
    }

    public int getId() {
        return this.id;
    }

    public Map<Integer, Integer> getRelevantMobs() {
        return this.relevantMobs;
    }

    private boolean checkNPCOnMap(MapleCharacter player, int npcId) {
        return ((npcId == 1013000)) ||((npcId == 0)) || (npcId == 2151009) || (npcId == 3000018) || (npcId == 9010000) || ((npcId >= 2161000) && (npcId <= 2161011)) || (npcId == 9000040) || (npcId == 9000066) || (npcId == 2010010) || (npcId == 1032204) || (npcId == 0) || ((player.getMap() != null) && (player.getMap().containsNPC(npcId)));
    }

    public int getMedalItem() {
        return this.viewMedalItem;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public int getAmountofItems(int itemId) {
        return this.questItems.get(itemId) != null ? (this.questItems.get(itemId)) : 0;
    }

    public boolean hasStartScript() {
        return this.scriptedStart;
    }

    public boolean hasEndScript() {
        return this.customend;
    }
}
