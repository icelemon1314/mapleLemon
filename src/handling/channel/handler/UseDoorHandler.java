package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.UseDoorRecvVO;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseDoorHandler extends MaplePacketHandler<UseDoorRecvVO> {


    @Override
    public void handlePacket(UseDoorRecvVO recvVO, MapleClient c) {
        // 49 08 00 00 00 00
        MapleCharacter chr = c.getPlayer();
        int oid = recvVO.getOid();
        for (MapleMapObject obj : chr.getMap().getAllDoorsThreadsafe()) {
            MapleDoor door = (MapleDoor) obj;
            if (door.getOwnerId() == oid) {
                door.warp(chr, recvVO.getToTown());
                break;
            }
        }
        chr.getClient().sendPacket(MaplePacketCreator.enableActions());
    }
}
