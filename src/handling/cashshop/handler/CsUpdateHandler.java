package handling.cashshop.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class CsUpdateHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.sendPacket(MTSCSPacket.刷新点券信息(c.getPlayer()));
    }
}
