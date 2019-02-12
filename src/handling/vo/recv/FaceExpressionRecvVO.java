package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class FaceExpressionRecvVO extends MaplePacketRecvVO {

    Integer emote;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        emote = slea.readInt();
    }

    public Integer getEmote() {
        return emote;
    }
}
