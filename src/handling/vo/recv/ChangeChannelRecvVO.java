package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeChannelRecvVO extends MaplePacketRecvVO {

    Integer channelId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        channelId = slea.readByte() + 1;
    }

    public Integer getChannelId() {
        return channelId;
    }
}
