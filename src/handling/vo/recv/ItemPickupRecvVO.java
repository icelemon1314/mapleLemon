package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class ItemPickupRecvVO extends MaplePacketRecvVO {

    Point chrPos;
    Integer mapOid;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 63 CC FE 13 01 A4 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 8E FF 8D 00 A1 86 01 00
        // 63 C7 FF F6 00 A8 86 01 00 拾取道具
        chrPos = slea.readPos();
        mapOid = slea.readInt();
    }

    public Point getChrPos() {
        return chrPos;
    }

    public Integer getMapOid() {
        return mapOid;
    }
}
