package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerEnterGameRecvVO extends MaplePacketRecvVO {

    Integer payerId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        payerId = slea.readInt();
    }

    public Integer getPayerId() {
        return payerId;
    }
}
