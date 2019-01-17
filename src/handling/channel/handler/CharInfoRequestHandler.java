package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharInfoRequestHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int objectid = slea.readInt();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleCharacter player = chr.getMap().getCharacterById(objectid);
        c.sendPacket(MaplePacketCreator.enableActions());
        if ((player != null)) {
            c.sendPacket(MaplePacketCreator.charInfo(player, chr.getId() == objectid));
        }
    }
}
