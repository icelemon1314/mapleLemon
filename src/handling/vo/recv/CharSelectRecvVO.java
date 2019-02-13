package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharSelectRecvVO extends MaplePacketRecvVO {

    Integer charId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        charId = slea.readInt();
    }

    public Integer getCharId() {
        return charId;
    }
}
