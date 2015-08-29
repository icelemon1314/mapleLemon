package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import tools.data.input.SeekableLittleEndianAccessor;

public class MapLoginHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mapleType = slea.readByte();
        short mapleVersion = slea.readShort();
        String maplePatch = String.valueOf(slea.readShort());
        if ((mapleType != ( ServerConstants.MAPLE_TYPE.getType()))) {
            c.getSession().close(true);
        }
    }
}


