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

public class SummonMoveRecvVO extends MaplePacketRecvVO {

    Integer summonId;
    List<LifeMovementFragment> res = new ArrayList<>();
    byte[] responseMoveData;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        summonId = slea.readInt();
        Point startPos = new Point(slea.readShort(), slea.readShort());
        try {
            res = MovementParse.parseMovement(slea);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.info("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && slea.available() != 1) {
            MapleLogger.info("slea.available != 1 (召唤兽移动错误) 剩余封包长度: " + slea.available());
            MapleLogger.info("slea.available != 1 (召唤兽移动错误) 封包: " + slea.toString(true));
        }
        slea.skip(2);

        responseMoveData = slea.read((int)slea.available());

    }

    public Integer getSummonId() {
        return summonId;
    }

    public List<LifeMovementFragment> getRes() {
        return res;
    }

    public byte[] getResponseMoveData() {
        return responseMoveData;
    }
}
