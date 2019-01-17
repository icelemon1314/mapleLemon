package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.FileoutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SummonPacket;

import java.awt.*;
import java.util.List;

public class SummonMoveHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 52
        // 5D 78 2F 00
        // E7 FE 1D 01
        // 04 00 E7 FE 1B 01 00 00 FE FF 00 00 0C 3C 00 00 E7 FE 34 01 00 00 3C 00 00 00 0C 68 01 01 00 00 38 FF 0C 00 00 00 E7 FE 26 01 00 00 80 FF 00 00 0C 5A 00 00
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleSummon sum = chr.getSummons().get(slea.readInt());
        if (sum == null) {
            FileoutputUtil.log("找不到地图物体：召唤兽！");
            return;
        }
        if ((sum.getOwnerId() != chr.getId()) || (sum.getSkillLevel() <= 0) || (sum.getMovementType() == SummonMovementType.不会移动)) {
            return;
        }
        Point startPos = new Point(slea.readShort(), slea.readShort());
        List res = MovementParse.parseMovement(slea, 4);
        Point pos = sum.getPosition();
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            if (slea.available() != 1L) {
                FileoutputUtil.log("slea.available() != 1 (召唤兽移动错误) 剩余封包长度: " + slea.available());
                FileoutputUtil.log(FileoutputUtil.Movement_Sumon, "slea.available() = " + slea.available() + " (召唤兽移动错误) 封包: " + slea.toString(true));
                return;
            }
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getId(), sum.getSkillId(), pos, res), sum.getTruePosition());
        }
    }
}
