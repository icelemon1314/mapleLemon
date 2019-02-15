package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.MoveLifeRecvVO;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.movement.LifeMovementFragment;

import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MobPacket;

import java.awt.*;
import java.util.List;

public class MoveLifeHandler extends MaplePacketHandler<MoveLifeRecvVO> {


    @Override
    public void handlePacket(MoveLifeRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapleMonster monster = chr.getMap().getMonsterByOid(recvVO.getMonsterOid());
        if (monster == null) {
            return;
        }
        if (monster.getLinkCID() > 0) {
            return;
        }
        short moveid = recvVO.getMoveId();
        boolean useSkill = recvVO.getUseSkill();
        int skillId = recvVO.getSkillId();
        int skillLevel = 0;
        int start_x = recvVO.getStartPos(); // hmm.. startpos?
        int start_y = recvVO.getEndPos(); // hmm...

        final List<LifeMovementFragment> res = recvVO.getRes();
        if ((res != null) && (res.size() > 0)) {
            MapleMap map = chr.getMap();
            c.sendPacket(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), skillId, skillLevel));
            MovementParse.updatePosition(res, monster, -1);
            Point endPos = monster.getTruePosition();
            map.moveMonster(monster, endPos);
            map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, recvVO.getResponseMoveData(), skillId, skillLevel, monster.getObjectId()), endPos);
        }
    }
}
