package handling.vo.recv;


import client.MapleClient;
import handling.channel.handler.MovementParse;
import handling.vo.MaplePacketRecvVO;
import server.movement.LifeMovementFragment;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PetMoveRecvVO extends MaplePacketRecvVO {

    Point startPos;
    List<LifeMovementFragment> res = new ArrayList<>();

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        startPos = slea.readPos();
        try {
            res = MovementParse.parseMovement(slea);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.info("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if (slea.available() != 1) {
            MapleLogger.info("slea.available != 1 (宠物移动出错) 剩余封包长度: " + slea.available());
            return;
        }
    }

    public Point getStartPos() {
        return startPos;
    }

    public List<LifeMovementFragment> getRes() {
        return res;
    }
}
