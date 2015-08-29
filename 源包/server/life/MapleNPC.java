package server.life;

import client.MapleClient;
import server.maps.MapleMapObjectType;
import server.shop.MapleShopFactory;
import tools.packet.NPCPacket;

public class MapleNPC extends AbstractLoadedMapleLife {

    private String name = "MISSINGNO";
    private boolean custom = false;

    public MapleNPC(int id, String name) {
        super(id);
        this.name = name;
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
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
