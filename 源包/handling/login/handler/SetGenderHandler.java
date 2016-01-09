package handling.login.handler;

import client.MapleClient;
import handling.login.LoginWorker;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class SetGenderHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte gender = slea.readByte();
        String username = slea.readMapleAsciiString();
        if (c.getAccountName().equals(username) && c.getLoginState() == MapleClient.ENTERING_PIN) {
            c.changeGender(gender);
//            c.getSession().write(LoginPacket.genderChanged(c));
//            c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            LoginWorker.registerClient(c);
        } else {
            c.getSession().close(true);
        }
    }
}
