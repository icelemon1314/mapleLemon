package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharlistRequestRecvVO extends MaplePacketRecvVO {

    Byte server;
    Byte channel;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        server = slea.readByte();
        channel = (byte)(slea.readByte() + 1);
    }

    public Byte getServer() {
        return server;
    }

    public Byte getChannel() {
        return channel;
    }
}
