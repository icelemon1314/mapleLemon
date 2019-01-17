package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().spawnPet(slea.readByte(), slea.readByte() > 0);
    }
}
