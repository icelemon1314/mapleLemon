package handling.vo.recv;


import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemMoveRecvVO extends MaplePacketRecvVO {

    MapleInventoryType inventoryType;
    Short src;
    Short dst;
    Short quantity;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        inventoryType = MapleInventoryType.getByType(slea.readByte());
        src = slea.readShort();
        dst = slea.readShort();
        quantity = slea.readShort();
    }

    public MapleInventoryType getInventoryType() {
        return inventoryType;
    }

    public Short getSrc() {
        return src;
    }

    public Short getDst() {
        return dst;
    }

    public Short getQuantity() {
        return quantity;
    }
}
