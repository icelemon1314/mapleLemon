package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.TrockAddMapRecvVO;
import server.maps.FieldLimitType;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class TrockAddMapHandler extends MaplePacketHandler<TrockAddMapRecvVO> {


    @Override
    public void handlePacket(TrockAddMapRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        byte type = recvVO.getType();
        if (type == 0) { // 删除地图
            int mapId = recvVO.getMapId();
            chr.deleteFromRegRocks(mapId);
            c.sendPacket(MTSCSPacket.getTrockRefresh(chr, (byte)1, true));
        } else if (type == 1) {
            if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                chr.addRegRockMap();
                c.sendPacket(MTSCSPacket.getTrockRefresh(chr, (byte)1, false));
            } else {
                chr.dropMessage(1, "你可能没有保存此地图.");
            }
        }
    }
}
