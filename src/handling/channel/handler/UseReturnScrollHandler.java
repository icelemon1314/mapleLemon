package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import handling.MaplePacketHandler;
import handling.vo.recv.UseReturnScrollRecvVO;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseReturnScrollHandler extends MaplePacketHandler<UseReturnScrollRecvVO> {


    @Override
    public void handlePacket(UseReturnScrollRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if ((!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.hasBlockedInventory()) || (chr.isInBlockedMap())) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        Short slot = recvVO.getSlot();
        int itemId = recvVO.getItemId();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (ii.getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (byte) 1, false);
            } else {
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        } else {
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}
