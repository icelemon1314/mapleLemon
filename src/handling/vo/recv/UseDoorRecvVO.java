package handling.vo.recv;


import client.MapleClient;
import handling.vo.MaplePacketRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseDoorRecvVO extends MaplePacketRecvVO {

    Integer oid;
    Boolean isToTown;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        oid = slea.readInt();
        isToTown = slea.readByte() == 0;
    }

    public Integer getOid() {
        return oid;
    }

    public Boolean getToTown() {
        return isToTown;
    }
}
