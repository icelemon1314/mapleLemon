package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcActionRecvVO extends MaplePacketRecvVO {

    byte[] available;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        available = slea.read((int)slea.available());
    }

    public byte[] getAvailable() {
        return available;
    }
}
