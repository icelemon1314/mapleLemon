package handling.channel.handler;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.MaplePacketHandler;
import handling.vo.recv.ItemMoveRecvVO;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemMoveHandler extends MaplePacketHandler<ItemMoveRecvVO> {


    @Override
    public void handlePacket(ItemMoveRecvVO recvVO, MapleClient c) {
        if ((c.getPlayer() == null) || (c.getPlayer().hasBlockedInventory())) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        MapleInventoryType type = recvVO.getInventoryType();
        short src = recvVO.getSrc();
        short dst = recvVO.getDst();
        short quantity = recvVO.getQuantity();
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
