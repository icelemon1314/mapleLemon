package server.life;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import tools.packet.NPCPacket;

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

    public boolean hasQuest(MapleCharacter chr) {
        return MapleQuest.getInstatce().canStart(chr,getId());
    }

    public int getQuestId() {
        return MapleQuest.getInstatce().getQuestIdByNpcId(getId()).get(0);
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
