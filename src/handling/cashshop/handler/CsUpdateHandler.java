package handling.cashshop.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.CsUpdateRecvVO;
import handling.vo.recv.DefaultRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class CsUpdateHandler extends MaplePacketHandler<CsUpdateRecvVO> {


    @Override
    public void handlePacket(CsUpdateRecvVO recvMsg, MapleClient c) {
        c.sendPacket(MTSCSPacket.刷新点券信息(c.getPlayer()));
    }
}
