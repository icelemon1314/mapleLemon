package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseUpgradeScrollRecvVO extends MaplePacketRecvVO {

    Short slot;
    Short dst;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 2D 05 00 FF FF
        slot = slea.readShort();
        dst = slea.readShort();
    }

    public Short getSlot() {
        return slot;
    }

    public Short getDst() {
        return dst;
    }
}
