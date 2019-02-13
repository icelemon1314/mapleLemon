package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SummonRemoveRecvVO extends MaplePacketRecvVO {

    Integer summonId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        summonId = slea.readInt();
    }

    public Integer getSummonId() {
        return summonId;
    }
}
