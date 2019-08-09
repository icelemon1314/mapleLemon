package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.MaplePacketHandler;
import handling.login.LoginServer;
import handling.vo.MaplePacketRecvVO;
import handling.vo.recv.ServerStatusRequestRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class ServerStatusRequestHandler extends MaplePacketHandler<ServerStatusRequestRecvVO> {

    public void handlePacket(ServerStatusRequestRecvVO recvMsg, MapleClient c) {
        int numPlayer = LoginServer.getUsersOn();
        int userLimit = LoginServer.getUserLimit();
        if (numPlayer >= userLimit) {
            c.sendPacket(LoginPacket.getServerStatus(2));
        } else if (numPlayer >= userLimit * 0.8D) {
            c.sendPacket(LoginPacket.getServerStatus(1)); // 区服人数过多
        } else {
            c.sendPacket(LoginPacket.getServerStatus(0));
        }
    }
}


