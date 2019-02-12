package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class GeneralChatRecvVO extends MaplePacketRecvVO {

    String text;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        text = slea.readMapleAsciiString();
    }

    public String getText() {
        return text;
    }
}
