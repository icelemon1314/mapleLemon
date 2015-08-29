package handling.login.handler;

import client.MapleClient;
import constants.WorldConstants;
import handling.login.LoginServer;
import tools.packet.LoginPacket;

public class ServerlistRequestHandler {

    public static void handlePacket(MapleClient c, boolean packet) {
        if (packet) {
            return;
        }
//        c.getSession().write(LoginPacket.getLoginWelcome());
        for (WorldConstants.Option servers : WorldConstants.values()) {
            if (servers.show()) {
                c.getSession().write(LoginPacket.getServerList(servers, LoginServer.getLoad()));
            }
        }
        c.getSession().write(LoginPacket.getEndOfServerList());
//        c.getSession().write(LoginPacket.enableRecommended());
    }
}
