package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import handling.MaplePacketHandler;
import handling.vo.recv.MovePlayerRecvVO;
import server.maps.MapleMap;

import server.movement.LifeMovementFragment;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.List;

public class MovePlayerHandler extends MaplePacketHandler<MovePlayerRecvVO> {


    @Override
    public void handlePacket(MovePlayerRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        List<LifeMovementFragment> res = recvVO.getRes();

        if ((res != null) && (chr.getMap() != null)) {
            MapleMap map = c.getPlayer().getMap();
            if (chr.isHidden()) {
                chr.setLastRes(res);
                chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), recvVO.getResponseMoveData()), false);
            } else {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), recvVO.getResponseMoveData()), false);
            }

            MovementParse.updatePosition(res, chr, 0);
            Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
        }
    }
}
