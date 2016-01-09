package server.life;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import tools.packet.NPCPacket;

import java.util.List;

public class MapleNPC extends AbstractLoadedMapleLife {

    private String name = "MISSION";
    private boolean custom = false;
    private String questScript = "";

    public MapleNPC(int id, String name) {
        super(id);
        this.name = name;
    }

    public void setScriptName(String name) {
        this.questScript = name;
    }

    public String getScriptName() {
        return this.questScript;
    }

    public boolean hasScriptQuest() {
        if (this.questScript == "")
            return false;

        // 检测脚本任务是否完成
//        MapleQuest.getInstatce().canStart()

        return this.questScript != null;
    }

    /**
     * 检查是否有任务可以开始
     * @param chr
     * @return
     */
    public boolean hasQuest(MapleCharacter chr) {
        // 判断任务是否可以开始
        int questId = this.getQuestId();
        if (questId == 0) {
            return false;
        }
        return MapleQuest.getInstance(questId).canStart(chr,getId());
    }

    public boolean hasCompleteQuest(MapleCharacter chr){
        MapleQuest quest = chr.getQuestInfoById(getQuestId());
        if (quest == null) {
            quest = new MapleQuest(getQuestId());
        }
        return quest.start(chr,getId());
    }

    public int getQuestId() {
        List<Integer> questIdList = MapleQuest.getInstatce().getQuestIdByNpcId(getId());
        if (questIdList.size() > 0) {
            return questIdList.get(0);
        } else {
            return 0;
        }
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    public boolean isStorage(){
        int npcid = getId();
        return npcid==1012009 || npcid==1022005 || npcid==1032006 || npcid==1052017 || npcid==1061008 || npcid==2020004;
    }



    @Override
    public void sendSpawnData(MapleClient client) {
        if ((getId() >= 9901000) || (getId() == 9000069) || (getId() == 9000133)) {
            return;
        }
        client.getSession().write(NPCPacket.spawnNPC(this, true));
        client.getSession().write(NPCPacket.spawnNPCRequestController(this, true));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(NPCPacket.removeNPCController(getObjectId()));
        client.getSession().write(NPCPacket.removeNPC(getObjectId()));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public boolean isCustom() {
        return this.custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
