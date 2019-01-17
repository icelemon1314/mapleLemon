package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseDoorHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 49 08 00 00 00 00
        MapleCharacter chr = c.getPlayer();
        int oid = slea.readInt();
        boolean mode = slea.readByte() == 0;
        for (MapleMapObject obj : chr.getMap().getAllDoorsThreadsafe()) {
            MapleDoor door = (MapleDoor) obj;
            if (door.getOwnerId() == oid) {
                door.warp(chr, mode);
                break;
            }
        }
        chr.getClient().sendPacket(MaplePacketCreator.enableActions());
    }
}
