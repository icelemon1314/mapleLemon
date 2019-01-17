package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.SendPacketOpcode;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NpcActionHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_ACTION.getValue());
        int length = (int) slea.available();
        if (length == 6) {
            mplew.writeInt(slea.readInt());
            mplew.writeShort(slea.readShort());
        } else{
            mplew.write(slea.read(length));
        }
        c.sendPacket(mplew.getPacket());
    }
}
