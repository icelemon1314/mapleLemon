package handling.login.handler;

import client.MapleClient;
import constants.GameConstants;
import handling.channel.ChannelServer;
import tools.data.input.SeekableLittleEndianAccessor;

public class WithSecondPasswordHandler {

    private static boolean loginFailCount(MapleClient c) {
        c.loginAttempt = (short) (c.loginAttempt + 1);

        return c.loginAttempt > 5;
    }

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c, boolean view) {
        String password = slea.readMapleAsciiString();
        int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        if ((!c.isLoggedIn()) || (loginFailCount(c)) || (c.getSecondPassword() == null) || (!c.login_Auth(charId)) || (ChannelServer.getInstance(c.getChannel()) == null) || (c.getWorld() != 0)) {
            c.getSession().close(true);
            return;
        }
//      if ((c.CheckSecondPassword(password)) && (password.length() >= 6) && (password.length() <= 16)) {
//        if (c.getIdleTask() != null) {
//          c.getIdleTask().cancel(true);
//        }
//        String s = c.getSessionIPAddress();
//        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
//        c.updateLoginState(1, s);
//        c.getSession().write(MaplePacketCreator.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
//      } else {
//        c.getSession().write(LoginPacket.secondPwError((byte)20));
//      }
    }
}
