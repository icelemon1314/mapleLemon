package handling.vo.recv;


import client.MapleClient;
import handling.channel.handler.MovementParse;
import handling.vo.MaplePacketRecvVO;
import server.movement.LifeMovementFragment;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.List;

public class MoveLifeRecvVO extends MaplePacketRecvVO {

    Integer monsterOid;
    Short moveId;
    Boolean isUseSkill;
    Byte skillId;
    Byte skillLevel;
    Short startPos;
    Short endPos;
    List<LifeMovementFragment> res = new ArrayList<>();
    byte[] responseMoveData;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        monsterOid = slea.readInt();

        moveId = slea.readShort();
        isUseSkill = (slea.readByte() & 0xFF) > 0;
        skillId = slea.readByte();
        int skillLevel = 0;
        startPos = slea.readShort(); // hmm.. startpos?
        endPos = slea.readShort(); // hmm...
        slea.readShort();
        slea.readShort();

        try {
            res = MovementParse.parseMovement(slea);
        } catch (ArrayIndexOutOfBoundsException e) {
            MapleLogger.info("AIOBE Type1:\r\n" + slea.toString(true));
            return;
        }
        if ((res != null) && slea.available() != 1) {
            MapleLogger.info("slea.available != 1 (怪物移动错误) 剩余封包长度: " + slea.available());
            MapleLogger.info("slea.available != 1 (怪物移动错误) 封包: " + slea.toString(true));
        }

        responseMoveData = slea.read((int)slea.available());
    }

    public byte[] getResponseMoveData() {
        return responseMoveData;
    }

    public Integer getMonsterOid() {
        return monsterOid;
    }

    public Short getMoveId() {
        return moveId;
    }

    public Boolean getUseSkill() {
        return isUseSkill;
    }

    public Byte getSkillId() {
        return skillId;
    }

    public Byte getSkillLevel() {
        return skillLevel;
    }

    public Short getStartPos() {
        return startPos;
    }

    public Short getEndPos() {
        return endPos;
    }

    public List<LifeMovementFragment> getRes() {
        return res;
    }
}
