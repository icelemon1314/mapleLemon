package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseChairHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 19 0A 00
        // 19 FF FF 取消
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int itemId = slea.readShort();

        chr.setChair(itemId);
        c.sendPacket(MaplePacketCreator.showChair(c.getPlayer().getId(),itemId));
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), itemId), false);
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
