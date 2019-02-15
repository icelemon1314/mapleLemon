package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.SummonMoveRecvVO;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;

import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SummonPacket;

import java.awt.*;
import java.util.List;

public class SummonMoveHandler extends MaplePacketHandler<SummonMoveRecvVO> {


    @Override
    public void handlePacket(SummonMoveRecvVO recvVO, MapleClient c) {
        // 52
        // 5D 78 2F 00
        // E7 FE 1D 01
        // 04 00 E7 FE 1B 01 00 00 FE FF 00 00 0C 3C 00 00 E7 FE 34 01 00 00 3C 00 00 00 0C 68 01 01 00 00 38 FF 0C 00 00 00 E7 FE 26 01 00 00 80 FF 00 00 0C 5A 00 00
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleSummon sum = chr.getSummons().get(recvVO.getSummonId());
        if (sum == null) {
            MapleLogger.info("找不到地图物体：召唤兽！");
            return;
        }
        if ((sum.getOwnerId() != chr.getId()) || (sum.getSkillLevel() <= 0) || (sum.getMovementType() == SummonMovementType.不会移动)) {
            return;
        }
        List res = recvVO.getRes();
        Point pos = sum.getPosition();
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getId(), sum.getSkillId(), pos, res), sum.getTruePosition());
        }
    }
}
