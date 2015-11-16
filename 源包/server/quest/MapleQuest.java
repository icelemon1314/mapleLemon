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
import java.util.*;

import scripting.quest.QuestScriptManager;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleQuest implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    // 所有任务集合
    private static final Map<Integer, MapleQuest> quests = new LinkedHashMap();
    // 按照NPC来查任务
    private static final Map<Integer,List<Integer>> npcQuest = new LinkedHashMap();
    protected int id;

    // 任务开始条件
    protected List<MapleQuestRequirement> startReqs = new LinkedList();
    // 任务完成条件
    protected Map<String,List<MapleQuestComplete>> completeReqs = new LinkedHashMap();
    // 任务状态流
    protected Map<String,String> questStatusList = new LinkedHashMap();
    // 任务奖励数据
    protected Map<String,List<MapleQuestReward>> rewards =  new LinkedHashMap();

//    protected List<MapleQuestAction> startActs = new LinkedList();
//    protected List<MapleQuestAction> completeActs = new LinkedList();
//    protected Map<Integer, Integer> relevantMobs = new LinkedHashMap();
//    protected Map<Integer, Integer> questItems = new LinkedHashMap();

    protected Map<String, List<Pair<String, Pair<String, Integer>>>> partyQuestInfo = new LinkedHashMap();

    private int npcId = 0;
    private String startStatus = "";
    private String endStatus = "";

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

    protected MapleQuest() {}

    /**
     * 任务数据解析
     * @param questData
     * @return
     * @throws SQLException
     */
    private static MapleQuest loadQuest(ResultSet questData) throws SQLException {
        MapleQuest ret = new MapleQuest(questData.getInt("questid"));
        ret.name = questData.getString("name");
        ret.npcId = questData.getInt("npcId");
        ret.startStatus = questData.getString("start_status");
        ret.endStatus = questData.getString("end_status");

        Connection con = DatabaseConnection.getConnection();
        // 开始这个任务需要的条件
        PreparedStatement psr = con.prepareStatement("SELECT * FROM wz_questreqdata WHERE questid = ?");
        psr.setInt(1, ret.id);
        ResultSet rse = psr.executeQuery();
        while (rse.next()) {
            MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(rse.getString("name"));
            MapleQuestRequirement req = new MapleQuestRequirement(ret, type, rse);
//            if (type.equals(MapleQuestRequirementType.interval)) {
//                ret.repeatable = true;
//            } else if (type.equals(MapleQuestRequirementType.normalAutoStart)) {
//                ret.repeatable = true;
//                ret.autoStart = true;
//            } else if (type.equals(MapleQuestRequirementType.startscript)) {
//                ret.scriptedStart = true;
//            } else if (type.equals(MapleQuestRequirementType.endscript)) {
//                ret.customend = true;
//            } else if (type.equals(MapleQuestRequirementType.mob)) {
//                for (Pair<Integer, Integer> mob : req.getDataStore()) {
//                    ret.relevantMobs.put(mob.left, mob.right);
//                }
//            } else if (type.equals(MapleQuestRequirementType.item)) {
//                for (Pair<Integer, Integer> it : req.getDataStore()) {
//                    ret.questItems.put(it.left, it.right);
//                }
//            }
            ret.startReqs.add(req);
        }
        rse.close();

        // 任务完成需要的数据
        PreparedStatement psComplete = con.prepareStatement("SELECT * FROM wz_questcompletedata WHERE questid = ?");
        psComplete.setInt(1,ret.id);
        rse = psComplete.executeQuery();
        while (rse.next()) {
            System.out.println(rse.getString("quest_status"));
            MapleQuestCompleteType ty = MapleQuestCompleteType.getByWZName(rse.getString("name"));
            MapleQuestComplete reward = new MapleQuestComplete(ty,rse.getInt("itemId"),rse.getInt("num"));

            String questStatus = rse.getString("quest_status");
            List<MapleQuestComplete> tmpReward = ret.completeReqs.get(questStatus);
            if (tmpReward == null) {
                tmpReward = new LinkedList();
            }
            tmpReward.add(reward);
            ret.completeReqs.put(questStatus,tmpReward);
        }

        System.out.println(ret.completeReqs.toString());


        // 任务奖励数据
        PreparedStatement psReward = con.prepareStatement("SELECT * FROM wz_questrewarddata WHERE questId = ?");
        psReward.setInt(1,ret.id);
        rse = psReward.executeQuery();
        while (rse.next()) {
            MapleQuestRewardType ty = MapleQuestRewardType.getByWZName(rse.getString("name"));
            MapleQuestReward reward = new MapleQuestReward(ty,rse);

            String questStatus = rse.getString("quest_status");
            List<MapleQuestReward> tmpReward = ret.rewards.get(questStatus);
            if (tmpReward == null) {
                tmpReward = new LinkedList();
            }
            tmpReward.add(reward);
            ret.rewards.put(questStatus,tmpReward);

        }

        // 任务流
        PreparedStatement psStatus = con.prepareStatement("SELECT * FROM wz_queststatus WHERE questid = ?");
        psStatus.setInt(1,ret.id);
        rse = psStatus.executeQuery();
        while (rse.next()) {
            ret.questStatusList.put(rse.getString("cur_status"),rse.getString("next_status"));
        }

        // 组队任务
        PreparedStatement psp = con.prepareStatement("SELECT * FROM wz_questpartydata WHERE questid = ?");
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

    /**
     * 初始化任务数据
     * @param reload
     */
    public static void initQuests(boolean reload) {
        if (reload) {
            quests.clear();
        }
        if (!quests.isEmpty() ) {
            return;
        }
        Connection con = DatabaseConnection.getConnection();
        try (
                PreparedStatement questData = con.prepareStatement("SELECT * FROM wz_questdata");
                ResultSet rs = questData.executeQuery();
        ) {
            while (rs.next()) {
                List<Integer> tmp = npcQuest.get(rs.getInt("npcId"));
                if (tmp == null) {
                    tmp = new LinkedList<>();
                }
                quests.put(rs.getInt("questid"), loadQuest(rs));
                tmp.add(rs.getInt("questid"));
                npcQuest.put(rs.getInt("npcId"), tmp);
            }
            questData.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("共加载 " + quests.size() + " 个任务信息.");
    }

    public static MapleQuest getInstance(int id) {
        MapleQuest ret = quests.get(Integer.valueOf(id));
        if (ret == null) {
            System.out.println("任务为空，new一个新的："+id);
            ret = new MapleQuest(id);
            quests.put(id, ret);
        }
        return ret;
    }

    public static MapleQuest getInstatce(){
        MapleQuest ret = new MapleQuest();
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

    public boolean canStartScriptQuest(String name){
        Connection con = DatabaseConnection.getConnection();
        try (
                PreparedStatement questData = con.prepareStatement("SELECT * FROM wz_questdata");
             ResultSet rs = questData.executeQuery();) {
//            while (rs.next()) {
//                quests.put(rs.getInt("questid"), loadQuest(rs, psr, psa, pss, psq, psi, psp));
//            }
            questData.close();
//            psr.close();
//            psa.close();
//            pss.close();
//            psq.close();
//            psi.close();
//            psp.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("共加载 " + quests.size() + " 个任务信息.");
        return false;
    }

    /**
     * 判断这个NPC下是否有任务可以做
     * @param chr
     * @param npcid
     * @return
     */
    public boolean canStart(MapleCharacter chr, Integer npcid) {
//        if ((chr.getQuest(this).getStatus() != MapleQuestStatus.QUEST_UNSTART)
//                && ((chr.getQuest(this).getStatus() != MapleQuestStatus.QUEST_COMPLETED)
//                )) {
//            if (chr.isShowPacket()) {
//                chr.dropMessage(6, new StringBuilder().append("开始任务 canStart: ").append(chr.getQuest(this).getStatus() != 0).append(" - ").append((chr.getQuest(this).getStatus() != 2) || (!this.repeatable)).append(" repeatable: ").append(this.repeatable).toString());
//            }
//            return false;
//        }
        if ((this.blocked) && (!chr.isGM())) {
            if (chr.isShowPacket()) {
                chr.dropMessage(6, new StringBuilder().append("开始任务 canStart - blocked ").append(this.blocked).toString());
            }
            return false;
        }

        Map<Integer,MapleQuestStatus> questComplete = chr.getCompletedQuests();
        boolean isCanStart = false;
        for (Integer r : getQuestIdByNpcId(npcid)) {
            if (questComplete.containsKey(r)) {
                continue;
            }
            System.out.println("存在可以完成的任务："+r);
            isCanStart = true;
            break;
        }
        return isCanStart;
    }

    /**
     * 根据NPC来获取任务列表
     * @param npcId
     * @return
     */
    public List <Integer> getQuestIdByNpcId(int npcId) {
        List <Integer> questList = this.npcQuest.get(npcId);
        if (questList == null) {
            return new ArrayList<Integer>();
        } else {
            return questList;
        }
    }

    /**
     * 判断任务是否能够完成
     * @param chr
     * @return
     */
    public boolean canComplete(MapleCharacter chr) {
        System.out.println("FUCK 1");
        if (chr.getQuest(this).getStatus() != 1) {
            return false;
        }
        System.out.println("FUCK 2");
        if ((this.blocked) && (!chr.isGM())) {
            return false;
        }
        System.out.println("FUCK 3");
//        if ((this.autoComplete) && (npcid != null) && (this.viewMedalItem <= 0)) {
//            forceComplete(chr, npcid);
//            return false;
//        }
        String queststatus = chr.getQuest(this).getCustomData();
        System.out.println("FUCK 7"+queststatus);
        List <MapleQuestComplete> com = this.completeReqs.get(queststatus);
        if (com == null) {
            System.out.println("FUCK 6");
            return false;
        }
        for (MapleQuestComplete r : com) {
            System.out.println("FUCK 4");
            if (!r.check(chr)) {
                System.out.println("FUCK 5");
                return false;
            }
        }
        System.out.println("FUCK 14");
        return true;
    }

    public void RestoreLostItem(MapleCharacter chr, int itemid) {
        if ((this.blocked) && (!chr.isGM())) {
            return;
        }
//        for (MapleQuestAction a : this.startActs) {
//            if (a.RestoreLostItem(chr, itemid)) {
//                break;
//            }
//        }
    }

    /**
     * 开始WZ中的任务
     * @param chr
     * @param npc
     */
    public void start(MapleCharacter chr, int npc) {
        if (chr.isShowPacket()) {
            chr.dropMessage(6, new StringBuilder().append("开始任务 start: ").append(npc).append(" autoStart：").append(this.autoStart).append(" checkNPCOnMap: ").append(checkNPCOnMap(chr, npc)).append(" canStart: ").append(canStart(chr, npc)).toString());
        }
        if ((checkNPCOnMap(chr, npc))) {
            // 检查任务是否开始

//            // 检查是否达到任务完成条件
//            for (Integer questId : getQuestIdByNpcId(npc)) {
//                MapleQuest chrQuest = chr.getQuestInfoById(questId);
//                if (chrQuest.canComplete(chr,npc)) {
//                    System.out.println("准备完成任务："+questId);
//                    chrQuest.complete(chr,npc);
//                }
//            }
            // 没有任务达到完成条件
            System.out.println("没有任务达到完成条件！");
            QuestScriptManager.getInstance().startQuest(chr.getClient(), npc, getId());

//            for (MapleQuestAction a : this.startActs) {
//                if (!a.checkEnd(chr, null)) {
//                    return;
//                }
//            }
//            for (MapleQuestAction a : this.startActs) {
//                a.runStart(chr, null);
//            }
//            if (!this.customend) {
//                forceStart(chr, npc, null);
//            } else {
//                QuestScriptManager.getInstance().startQuest(chr.getClient(), npc, getId());
//            }
        }
    }

    public void complete(MapleCharacter chr, int npc) {
        complete(chr, npc, null);
    }

    /**
     * 完成玩家的任务
     * @param chr
     * @param npc
     * @param selection
     */
    public void complete(MapleCharacter chr, int npc, Integer selection) {
        if ((chr.getMap() != null) && ((this.autoPreComplete) || (checkNPCOnMap(chr, npc))) && (canComplete(chr))) {

            // 扣除任务道具
            List <MapleQuestComplete> com = this.completeReqs.get(chr.getQuest(this).getCustomData());

            for (MapleQuestComplete r : com) {
                if (r.removeQuestItem(chr)) {

                    // 更新玩家任务状态
                    System.out.println("更新玩家任务状态！");
                    forceComplete(chr, npc);

                    // 发送奖励
                    System.out.println("发送奖励");
                    String curStatus = chr.getQuest(this).getCustomData();
                    List <MapleQuestReward> rewardData = this.rewards.get(curStatus);
                    // 通用奖励，金币和经验
                    if (rewardData == null) {
                        System.out.println("奖励数据为空！");
                        return ;
                    }
                    for (MapleQuestReward reward : rewardData) {
                        reward.getRewardToChr(chr);
                    }
                }
            }
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

    /**
     * 完成任务
     * @param chr
     * @param npc
     */
    public void forceComplete(MapleCharacter chr, int npc) {
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) MapleQuestStatus.QUEST_COMPLETED, npc);
        newStatus.setForfeited(chr.getQuest(this).getForfeited());
        String nextData = this.questStatusList.get(chr.getQuest(this).getCustomData());
        if (nextData == null) {
            nextData = this.endStatus;
        }
        System.out.println("新的任务状态："+nextData);
        newStatus.setCustomData(nextData);
        chr.updateQuest(newStatus);
    }

    public int getId() {
        return this.id;
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

    public boolean hasStartScript() {
        return this.scriptedStart;
    }

    public boolean hasEndScript() {
        return this.customend;
    }
}
