package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.world.WorldBroadcastService;
import handling.world.party.MaplePartyCharacter;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class ItemPickupHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 63 CC FE 13 01 A4 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 C7 FF F6 00 A8 86 01 00 拾取道具
        MapleCharacter chr = c.getPlayer();
        if (chr.hasBlockedInventory()) {
            chr.dropMessage(5, "现在还不能进行操作.");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        Point Client_Reportedpos = slea.readPos();
        if (chr.getMap() == null) {
            return;
        }
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
        if (ob == null) {
            chr.dropMessage(5, "找不到地图上的道具");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                chr.dropMessage(5, "地图上的道具已经被拾取！");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if ((mapitem.getQuest() > 0) && (chr.getQuestStatus(mapitem.getQuest()) != 1)) {
                chr.dropMessage(5, "地图上的道具为任务道具");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                chr.dropMessage(5, "这个道具不属于你，等会再捡！");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                chr.dropMessage(5, "这个道具不属于你，等会再捡啊！");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            // 飞镖类需要处理下数量问题
            if (ItemConstants.is飞镖道具(mapitem.getItemId())) {
                mapitem.getItem().setQuantity((short)0);
            }
            double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
            if ((Distance > 5000.0D) && ((mapitem.getMeso() > 0) || (mapitem.getItemId() != 4001025))) {
                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏捡物。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            } else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 640000.0D) {
                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏捡物。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            }
            if (mapitem.getMeso() > 0) { // 捡RMB
                if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                    List<MapleCharacter> toGive = new LinkedList();
                    long splitMeso = mapitem.getMeso() * 40 / 100;
                    for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                        if ((m != null) && (m.getId() != chr.getId())) {
                            toGive.add(m);
                        }
                    }
                    for (MapleCharacter m : toGive) {
                        m.gainMeso(splitMeso / toGive.size() + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0D) : 0), true);
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                } else {
                    chr.gainMeso(mapitem.getMeso(), true);
                }
                removeItem(chr, mapitem, ob);
            } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                chr.dropMessage(5, "这个道具无法捡取。");
                c.sendPacket(MaplePacketCreator.enableActions());
                // @TODO useItem()
//            } else if (useItem(c, (int) mapitem.getItemId())) {
//                chr.dropMessage(5, "捡到立即使用的道具！");
//                removeItem(c.getPlayer(), mapitem, ob);
            } else if ((mapitem.getItemId() / 10000 != 291) && (MapleInventoryManipulator.checkSpace(c, (int) mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner()))) {
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true);
                removeItem(chr, mapitem, ob);
            } else {
                c.sendPacket(InventoryPacket.getInventoryFull());
                c.sendPacket(InventoryPacket.getShowInventoryFull());
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        } finally {
            lock.unlock();
        }
    }

    private void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getMap().removeMapObject(ob);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }
}
