package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.SpawnPetRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends MaplePacketHandler<SpawnPetRecvVO> {


    @Override
    public void handlePacket(SpawnPetRecvVO recvVO, MapleClient c) {
        c.getPlayer().spawnPet(recvVO.getSlot());
    }
}
