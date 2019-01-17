package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginWorker;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class SetGenderHandler extends MaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte gender = slea.readByte();
        String username = slea.readMapleAsciiString();
        if (c.getAccountName().equals(username) && c.getLoginState() == MapleClient.ENTERING_PIN) {
            c.changeGender(gender);
//            c.sendPacket(LoginPacket.genderChanged(c));
//            c.sendPacket(LoginPacket.getAuthSuccessRequest(c));
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            LoginWorker.registerClient(c);
        } else {
            c.getSession().close();
        }
    }
}
