package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class OpenNpcShopRecvVO extends MaplePacketRecvVO {

    Byte type;
    Short slot;
    Integer itemId;
    Short quantity;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        // 25 00 00 00 60 4A 0F 00 01 00
        // 25 00 01 00 80 84 1E 00 01 00
        // 25 03
        type = slea.readByte();
        switch (type) {
            case 0://购买
                slot = slea.readShort();
                itemId = slea.readInt();
                quantity = slea.readShort();
                break;
            case 1://出售
                slot = slea.readShort();
                itemId = slea.readInt();
                quantity = slea.readShort();
                break;
            case 2://充值
                slot = slea.readShort();
                break;
            default:
                break;
        }
    }

    public Byte getType() {
        return type;
    }

    public Short getSlot() {
        return slot;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Short getQuantity() {
        return quantity;
    }
}
