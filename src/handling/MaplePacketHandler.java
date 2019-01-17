package handling;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public abstract class MaplePacketHandler {

    public abstract void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c);
}
