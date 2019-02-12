package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeApRecvVO extends MaplePacketRecvVO {

    Integer type;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        type = slea.readInt();
    }

    public Integer getType() {
        return type;
    }
}
