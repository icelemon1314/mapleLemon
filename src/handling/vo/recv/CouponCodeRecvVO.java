package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CouponCodeRecvVO extends MaplePacketRecvVO {

    String toPlayer;
    String code;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        toPlayer = slea.readMapleAsciiString();
        code = slea.readMapleAsciiString();
    }

    public String getToPlayer() {
        return toPlayer;
    }

    public String getCode() {
        return code;
    }
}
