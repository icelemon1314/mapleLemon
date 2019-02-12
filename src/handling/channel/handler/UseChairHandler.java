package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.UseChairRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseChairHandler extends MaplePacketHandler<UseChairRecvVO> {


    @Override
    public void handlePacket(UseChairRecvVO recvVO, MapleClient c) {
        // 19 0A 00
        // 19 FF FF 取消
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int itemId = recvVO.getItemId();

        chr.setChair(itemId);
        c.sendPacket(MaplePacketCreator.showChair(c.getPlayer().getId(),itemId));
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), itemId), false);
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
