package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.movement.LifeMovementFragment;

import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MobPacket;

import java.awt.*;
import java.util.List;

public class MoveLifeHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapleMonster monster = chr.getMap().getMonsterByOid(slea.readInt());
        if (monster == null) {
            return;
        }
        if (monster.getLinkCID() > 0) {
            return;
        }
        short moveid = slea.readShort();
        boolean useSkill = (slea.readByte() & 0xFF) > 0;
        int skillId = slea.readByte();
        int skillLevel = 0;
        int start_x = slea.readShort(); // hmm.. startpos?
        int start_y = slea.readShort(); // hmm...
        slea.readShort();
        slea.readShort();

        final List<LifeMovementFragment> res;
        try {
            res = MovementParse.parseMovement(slea, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.error("怪物ID " + monster.getId() + ", AIOBE Type2:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && (res.size() > 0)) {
            MapleMap map = chr.getMap();
            c.sendPacket(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), skillId, skillLevel));
            if (slea.available() != 1) {
                MapleLogger.error("slea.available != 1 (怪物移动错误) 剩余封包长度: " + slea.available());
                return;
            }
            MovementParse.updatePosition(res, monster, -1);
            Point endPos = monster.getTruePosition();
            map.moveMonster(monster, endPos);
            map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, slea, skillId, skillLevel, monster.getObjectId()), endPos);
        }
    }
}
