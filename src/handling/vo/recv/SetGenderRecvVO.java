package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SetGenderRecvVO extends MaplePacketRecvVO {

    Byte gender;
    String userName;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        gender = slea.readByte();
        userName = slea.readMapleAsciiString();
    }

    public Byte getGender() {
        return gender;
    }

    public String getUserName() {
        return userName;
    }
}
