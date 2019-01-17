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
//        c.sendPacket(LoginPacket.getLoginWelcome());
        for (WorldConstants.Option servers : WorldConstants.values()) {
            if (servers.show()) {
                c.sendPacket(LoginPacket.getServerList(servers, LoginServer.getLoad()));
            }
        }
        c.sendPacket(LoginPacket.getEndOfServerList());
//        c.sendPacket(LoginPacket.enableRecommended());
    }
}
