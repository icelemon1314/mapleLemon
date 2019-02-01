package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class LoginPasswordRecvVO extends MaplePacketRecvVO {

    String username;
    String passowrd;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c)
    {
        username = slea.readMapleAsciiString();
        passowrd = slea.readMapleAsciiString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassowrd() {
        return passowrd;
    }


}
