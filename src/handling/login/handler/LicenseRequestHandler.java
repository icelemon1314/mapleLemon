package handling.login.handler;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class LicenseRequestHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.readByte() == 1) {
//            c.sendPacket(LoginPacket.licenseResult());
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        } else {
            c.getSession().close();
        }
    }
}
