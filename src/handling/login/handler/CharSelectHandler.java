package handling.login.handler;

import client.MapleClient;
import constants.WorldConstants;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.vo.recv.CharSelectRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharSelectHandler extends MaplePacketHandler<CharSelectRecvVO> {

    private static boolean loginFailCount(MapleClient c) {
        c.loginAttempt = (short) (c.loginAttempt + 1);
        return c.loginAttempt > 5;
    }

    public void handlePacket(CharSelectRecvVO recvVO, MapleClient c) {
        int charId = recvVO.getCharId();
        if (!c.isLoggedIn() || loginFailCount(c) || (!c.login_Auth(charId))) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        if ((ChannelServer.getInstance(c.getChannel()) == null) || !WorldConstants.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        String ip = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, ip.substring(ip.indexOf('/') + 1, ip.length()), c.getTempIP(), c.getChannel());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, ip);
        c.sendPacket(MaplePacketCreator.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
    }
}
