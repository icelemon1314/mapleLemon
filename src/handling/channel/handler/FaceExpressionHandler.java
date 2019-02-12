package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.vo.recv.FaceExpressionRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FaceExpressionHandler extends MaplePacketHandler<FaceExpressionRecvVO> {

    @Override
    public void handlePacket(FaceExpressionRecvVO recvVo, MapleClient c) {
        int emote = recvVo.getEmote();
        MapleCharacter chr = c.getPlayer();
        if (emote > 7) {
            int emoteid = 5159992 + emote;
            MapleInventoryType type = ItemConstants.getInventoryType(emoteid);
            if (chr.getInventory(type).findById(emoteid) == null) {
                return;
            }
        }
        if ((emote > 0) && (chr != null) && (chr.getMap() != null) && (!chr.isHidden())) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.facialExpression(chr, emote), false);
        }
    }
}
