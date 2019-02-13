package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import handling.MaplePacketHandler;
import handling.vo.recv.PetLootRecvVO;
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

public class PetLootHandler extends MaplePacketHandler<PetLootRecvVO> {

    /**
     * 宠物拾取道具
     * @param recvVO
     * @param c
     */
    @Override
    public void handlePacket(PetLootRecvVO recvVO, MapleClient c) {
        // 4F 4C 00 F6 00 A3 86 01 00
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if ((c.getPlayer().hasBlockedInventory())) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        MaplePet pet = chr.getSpawnPet();
        Point Client_Reportedpos = recvVO.getPetPos();
        MapleMapObject ob = chr.getMap().getMapObject(recvVO.getMapOid(), MapleMapObjectType.ITEM);
        if ((ob == null) || (pet == null)) {
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.sendPacket(InventoryPacket.getInventoryFull());
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (mapitem.isPlayerDrop())) {
                return;
            }
            if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
            if ((Distance > 10000.0D) && ((mapitem.getMeso() > 0) || (mapitem.getItemId() != 4001025))) {

                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏宠吸。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            } else if (pet.getPos().distanceSq(mapitem.getPosition()) > 640000.0D) {

                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + chr.getName() + " ID: " + chr.getId() + " (等级 " + chr.getLevel() + ") 全屏宠吸。地图ID: " + chr.getMapId() + " 范围: " + Distance));
            }
            if (mapitem.getMeso() > 0) {
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
                removeItem_Pet(chr, mapitem);
            } else if ((MapleItemInformationProvider.getInstance().isPickupBlocked((int) mapitem.getItemId())) || (mapitem.getItemId() / 10000 == 291)) {
                c.sendPacket(MaplePacketCreator.enableActions());
                // @TODO useItem()
//            } else if (useItem(c, (int) mapitem.getItemId())) {
//                removeItem_Pet(chr, mapitem);
            } else if (MapleInventoryManipulator.checkSpace(c, (int) mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                if ((mapitem.getItem().getQuantity() >= 50) && (mapitem.getItemId() == 2340000)) {
                    c.setMonitored(true);
                }
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true/*, mapitem.getDropper() instanceof MapleMonster*/);
                removeItem_Pet(chr, mapitem);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeItem_Pet(MapleCharacter chr, MapleMapItem mapitem) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId()));
        chr.getMap().removeMapObject(mapitem);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }
}
