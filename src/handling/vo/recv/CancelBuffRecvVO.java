package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelBuffRecvVO extends MaplePacketRecvVO {

    Integer sourceId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        sourceId = slea.readInt();
    }

    public Integer getSourceId() {
        return sourceId;
    }
}
