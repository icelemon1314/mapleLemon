package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CheckCharNameRecvVO extends MaplePacketRecvVO {

    String charName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        charName = slea.readMapleAsciiString();
    }

    public String getCharName() {
        return charName;
    }
}
