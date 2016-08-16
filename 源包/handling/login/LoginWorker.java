package handling.login;

import client.MapleClient;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.login.handler.ServerlistRequestHandler;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.LoginPacket;

public class LoginWorker {

    private static long lastUpdate = 0L;

    public static void registerClient(final MapleClient c) {
        if ((LoginServer.isAdminOnly()) && (!c.isGm()) && (!c.isLocalhost())) {
            c.getSession().write(MaplePacketCreator.serverMessageNotice("当前服务器设置只能管理员进入游戏.\r\n我们目前在修复几个问题.\r\n请稍后再试."));
            c.getSession().write(LoginPacket.getLoginFailed(16));
            return;
        }
        updateChannel(c);
        if (c.finishLogin() != 0) {
            c.getSession().write(LoginPacket.getLoginFailed(7));
            return;
        }
        FileoutputUtil.log("登录成功，准备通知客户端！！！！");
        c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
//        c.getSession().write(LoginPacket.checkUserLimit());
        //c.getSession().write(MaplePacketCreator.serverNotice(1, "恭喜您成功登陆！\r\n您的账户中有 " + c.getJinQuan() + " 张金券\r\n祝您游戏愉快"));
        ServerlistRequestHandler.handlePacket(c, false);
//        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO accounts_log (accid, accname, ip, macs) VALUES (?, ?, ?, ?)")) {
//            ps.setInt(1, c.getAccID());
//            ps.setString(2, c.getAccountName());
//            ps.setString(3, c.getSession().getRemoteAddress().toString());
//            ps.setString(4, c.getMac());
//            ps.executeUpdate();
//            ps.close();
//        } catch (SQLException e) {
//            FileoutputUtil.outputFileError(FileoutputUtil.SQL_ScriptEx_Log, e);
//        }
    }

    public static void updateChannel(final MapleClient c) {
        if (System.currentTimeMillis() - lastUpdate > 10 * 60 * 1000) {
            lastUpdate = System.currentTimeMillis();
            Map<Integer, Integer> load = ChannelServer.getChannelLoad();
            int usersOn = 0;
            if (load.size() <= 0) {
                lastUpdate = 0;
                c.getSession().write(LoginPacket.getLoginFailed(7));
                return;
            }
            double loadFactor = LoginServer.getUserLimit() / load.size(); // 每个频道人数
            for (Entry<Integer, Integer> entry : load.entrySet()) {
                load.put(entry.getKey(), Math.min(255, (int) (entry.getValue() / loadFactor * 255)));
            }
            LoginServer.setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
        }
    }

}


