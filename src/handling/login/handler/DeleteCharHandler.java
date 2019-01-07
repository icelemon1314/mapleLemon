package handling.login.handler;

import client.MapleClient;
import gui.ServerUI;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class DeleteCharHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*if (!c.isGm()) {
         return;
         }*/
        slea.skip(2);
        //String Secondpw_Client = slea.readMapleAsciiString();
        int charId = slea.readInt();
        if ((!c.login_Auth(charId)) || (!c.isLoggedIn())) {
            c.getSession().close(true);
            return;
        }
        byte state = 0;
        /*if (c.getSecondPassword() != null) {
         if (Secondpw_Client == null) {
         c.getSession().close(true);
         return;
         }
         //        if (!c.CheckSecondPassword(Secondpw_Client)) {
         //          state = 12;
         //        }
         }*/

        if (state == 0) {
            state = (byte) c.deleteCharacter(charId);
        }
        ServerUI.getInstance().removeCharTable(charId);
        c.getSession().write(LoginPacket.deleteCharResponse(charId, state));
    }
}
