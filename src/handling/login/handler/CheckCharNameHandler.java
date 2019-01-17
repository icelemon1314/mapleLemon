package handling.login.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CheckCharNameHandler  extends MaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        c.sendPacket(LoginPacket.charNameResponse(name, (!MapleCharacterUtil.canCreateChar(name, c.isGm())) || ((LoginInformationProvider.getInstance().isForbiddenName(name)) && (!c.isGm()))));
    }
}


