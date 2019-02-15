package handling.vo.recv;


import client.MapleClient;
import client.MapleJob;
import handling.channel.handler.MovementParse;
import handling.vo.MaplePacketRecvVO;
import server.movement.*;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MovePlayerRecvVO extends MaplePacketRecvVO {

    List<LifeMovementFragment> res = new ArrayList<>();
    byte[] responseMoveData;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        slea.readShort(); // position
        slea.readShort();
        try {
            res = MovementParse.parseMovement(slea);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.info("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && slea.available() != 10) {
            MapleLogger.info("slea.available != 8 (角色移动出错) 剩余封包长度: " + slea.available());
            MapleLogger.info("slea.available != 8 (角色移动出错) 封包: " + slea.toString(true));
        }
        slea.skip(2);

        responseMoveData = slea.read((int)slea.available());

    }

    public List<LifeMovementFragment> getRes() {
        return res;
    }

    public byte[] getResponseMoveData() {
        return responseMoveData;
    }
}
