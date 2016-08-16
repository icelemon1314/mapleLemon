package handling.login.handler;

import client.MapleClient;
import constants.WorldConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import java.util.List;

import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CharlistRequestHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close(true);
            return;
        }
        int server = slea.readByte();
        int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel) || !WorldConstants.isExists(server)) {
            c.getSession().write(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }

        if (!WorldConstants.getById(server).isAvailable() && !(c.isGm() && server == WorldConstants.gmserver)) {
            c.getSession().write(MaplePacketCreator.serverMessageNotice("很抱歉, " + WorldConstants.getNameById(server) + "暂时未开放。\r\n请尝试连接其他服务器。"));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, but it is used to unstuck
            return;
        }
        FileoutputUtil.log("[连接信息] "+c.getSession().getRemoteAddress().toString().split(":")[0] + " 连接到世界服务器: " + server + " 频道: " + channel);
        List chars = c.loadCharacters(server);
        if ((chars != null) && (ChannelServer.getInstance(channel) != null)) {
            c.setWorld(server);
            c.setChannel(channel);
            c.getSession().write(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getAccCharSlots()));
        } else {
            c.getSession().close(true);
        }
    }
}


