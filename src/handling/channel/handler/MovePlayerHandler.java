package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import handling.MaplePacketHandler;
import server.maps.MapleMap;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.List;

public class MovePlayerHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        slea.readByte();
        Point Original_Pos = chr.getPosition();
        slea.readShort(); // position
        slea.readShort();
        List res;
        try {
            res = MovementParse.parseMovement(slea, 1, chr);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.info("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && (chr.getMap() != null)) {
            if (slea.available() != 10) {
                MapleLogger.info("玩家" + chr.getName() + "(" + MapleJob.getName(MapleJob.getById(chr.getJob())) + ") slea.available != 8 (角色移动出错) 剩余封包长度: " + slea.available());
                MapleLogger.info("slea.available != 8 (角色移动出错) 封包: " + slea.toString(true));
                return;
            }
            MapleMap map = c.getPlayer().getMap();
            slea.skip(2);
            if (chr.isHidden()) {
                chr.setLastRes(res);
                chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), slea), false);
            } else {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), slea), false);
            }

            MovementParse.updatePosition(res, chr, 0);
            Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
        }
    }
}
