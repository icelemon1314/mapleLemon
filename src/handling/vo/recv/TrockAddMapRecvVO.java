package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class TrockAddMapRecvVO extends MaplePacketRecvVO {

    Byte type;
    Integer mapId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        type = slea.readByte();
        if (type == 0) { // 删除地图
            mapId = slea.readInt();
        }
    }

    public Byte getType() {
        return type;
    }

    public Integer getMapId() {
        return mapId;
    }
}
