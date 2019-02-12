package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseChairRecvVO extends MaplePacketRecvVO {

    Integer itemId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // @TODO maybe have problem
        itemId = (int) slea.readShort();
    }

    public Integer getItemId() {
        return itemId;
    }
}
