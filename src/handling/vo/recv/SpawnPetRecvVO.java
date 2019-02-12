package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetRecvVO extends MaplePacketRecvVO {

    Short slot;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slot = slea.readShort();
    }

    public Short getSlot() {
        return slot;
    }
}
