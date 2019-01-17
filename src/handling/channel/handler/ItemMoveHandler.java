package handling.channel.handler;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.MaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemMoveHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer() == null) || (c.getPlayer().hasBlockedInventory())) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        short src = slea.readShort();
        short dst = slea.readShort();
        short quantity = slea.readShort();
        if ((src < 0) && (dst > 0)) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }
}
