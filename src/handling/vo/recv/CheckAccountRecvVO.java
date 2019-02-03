package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CheckAccountRecvVO extends MaplePacketRecvVO {

    String accountName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        accountName = slea.readMapleAsciiString();
    }

    public String getAccountName() {
        return accountName;
    }
}
