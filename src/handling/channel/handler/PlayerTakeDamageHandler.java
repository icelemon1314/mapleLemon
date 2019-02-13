package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.DefaultRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerTakeDamageHandler extends MaplePacketHandler<DefaultRecvVO> {


    @Override
    public void handlePacket(DefaultRecvVO slea, MapleClient c) {

    }
}
