package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class MesoDropRecvVO extends MaplePacketRecvVO {

    Integer meso;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        meso = slea.readInt();
    }

    public Integer getMeso() {
        return meso;
    }
}
