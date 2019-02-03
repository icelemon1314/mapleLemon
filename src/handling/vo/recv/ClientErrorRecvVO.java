package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ClientErrorRecvVO extends MaplePacketRecvVO {

    String errorMsg;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        errorMsg = slea.readMapleAsciiString();
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
