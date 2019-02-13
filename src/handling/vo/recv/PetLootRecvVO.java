package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class PetLootRecvVO extends MaplePacketRecvVO {

    Point petPos;
    Integer mapOid;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        petPos = slea.readPos();
        mapOid = slea.readInt();
    }

    public Point getPetPos() {
        return petPos;
    }

    public Integer getMapOid() {
        return mapOid;
    }
}
