package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class DefaultRecvVO extends MaplePacketRecvVO {


    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    }

}
