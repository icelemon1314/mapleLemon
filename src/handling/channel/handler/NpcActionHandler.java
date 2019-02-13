package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.SendPacketOpcode;
import handling.vo.recv.NpcActionRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NpcActionHandler extends MaplePacketHandler<NpcActionRecvVO> {


    @Override
    public void handlePacket(NpcActionRecvVO recvVO, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_ACTION.getValue());
        mplew.write(recvVO.getAvailable());
        c.sendPacket(mplew.getPacket());
    }
}
