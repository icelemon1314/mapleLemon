package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class OpenStorageRecvVO extends MaplePacketRecvVO {

    Byte mode;
    Byte slotType;
    Byte slot;
    Integer itemId;
    Short quantity;
    Integer meso;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        mode = slea.readByte();
        switch (mode) {
            case 4://取出
                slotType = slea.readByte();
                slot = slea.readByte();  // 从0开始计算的
                break;
            case 5://放入仓库
                slot = (byte) slea.readShort();
                itemId = slea.readInt();
                quantity = slea.readShort();
                break;
            case 6:
                meso = slea.readInt();
                break;
        }
    }

    public Byte getMode() {
        return mode;
    }

    public Byte getSlotType() {
        return slotType;
    }

    public Byte getSlot() {
        return slot;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Short getQuantity() {
        return quantity;
    }

    public Integer getMeso() {
        return meso;
    }
}
