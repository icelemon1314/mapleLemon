package handling.login.handler;

import client.MapleClient;
import constants.WorldConstants;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import handling.vo.recv.CharlistRequestRecvVO;
import handling.world.World;
import java.util.List;


import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CharlistRequestHandler extends MaplePacketHandler<CharlistRequestRecvVO> {

    public void handlePacket(CharlistRequestRecvVO recvMsg, MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        int server = recvMsg.getServer();
        int channel = recvMsg.getChannel();
        if (!World.isChannelAvailable(channel) || !WorldConstants.isExists(server)) {
            c.sendPacket(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }

        if (!WorldConstants.getById(server).isAvailable() && !(c.isGm() && server == WorldConstants.gmserver)) {
            c.sendPacket(MaplePacketCreator.serverMessageNotice("很抱歉, " + WorldConstants.getNameById(server) + "暂时未开放。\r\n请尝试连接其他服务器。"));
            c.sendPacket(LoginPacket.getLoginFailed(1)); //Shows no message, but it is used to unstuck
            return;
        }
        MapleLogger.info("[连接信息] "+c.getSession().remoteAddress().toString().split(":")[0] + " 连接到世界服务器: " + server + " 频道: " + channel);
        List chars = c.loadCharacters(server);
        if ((chars != null) && (ChannelServer.getInstance(channel) != null)) {
            c.setWorld(server);
            c.setChannel(channel);
            c.sendPacket(LoginPacket.getCharList(c.getSecondPassword(), chars));
        } else {
            c.getSession().close();
        }
    }
}


