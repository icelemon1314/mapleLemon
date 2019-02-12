package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import handling.MaplePacketHandler;
import handling.vo.recv.UseSummonBagRecvVO;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Map;

public class UseSummonBagHandler extends MaplePacketHandler<UseSummonBagRecvVO> {


    @Override
    public void handlePacket(UseSummonBagRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if ((!chr.isAlive()) || (chr.hasBlockedInventory())) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        Short slot = recvVO.getSlot();
        int itemId = recvVO.getItemId();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && ((c.getPlayer().getMapId() < 910000000) || (c.getPlayer().getMapId() > 910000022))) {
            Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getEquipStats(itemId);
            if (toSpawn == null) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            MapleMonster ht = null;
            int type = 0;

            for (Map.Entry<String, Integer> i : toSpawn.entrySet()) {
                // for (Map.Entry i : toSpawn.entrySet()) {
                if (((i.getKey()).startsWith("mob")) && (Randomizer.nextInt(99) <= (i.getValue()))) {
                    ht = MapleLifeFactory.getMonster(Integer.parseInt(( i.getKey()).substring(3)));
                    chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
                }
            }
            if (ht == null) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
