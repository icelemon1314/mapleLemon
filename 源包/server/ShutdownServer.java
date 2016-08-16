package server;

import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.WorldBroadcastService;
import handling.world.WorldGuildService;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

public class ShutdownServer implements ShutdownServerMBean {

    private static final Logger log = Logger.getLogger(ShutdownServer.class);
    public static ShutdownServer instance;
    public int mode = 0;

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ShutdownServer();
            mBeanServer.registerMBean(instance, new ObjectName("server:type=ShutdownServer"));
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            FileoutputUtil.log("Error registering Shutdown MBean");
        }
    }

    public static ShutdownServer getInstance() {
        return instance;
    }

    @Override
    public void shutdown() {
        run();
    }

    @Override
    public void run() {
        if (this.mode == 0) {
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverMessageNotice(" 游戏服务器将关闭维护，请玩家安全下线..."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
                cs.closeAllMerchants();
            }
            WorldGuildService.getInstance().save();
            FileoutputUtil.log("所有档案已保存.");
            this.mode++;
        } else if (this.mode == 1) {
            this.mode++;
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverMessageNotice(" 游戏服务器将关闭维护，请玩家安全下线..."));
            Integer[] chs = (Integer[]) ChannelServer.getAllInstance().toArray(new Integer[0]);
            for (int i = 0; i < chs.length; i++) {
                i = chs[i];
                try {
                    ChannelServer cs = ChannelServer.getInstance(i);
                    synchronized (this) {
                        cs.shutdown();
                    }
                } catch (Exception e) {
                    log.error("关闭服务端错误 - 3" + e);
                }
            }
            LoginServer.shutdown();
            CashShopServer.shutdown();
            //AuctionServer.shutdown(); //已注释启动拍卖
            FileoutputUtil.log("正在关闭时钟线程...");
            Timer.WorldTimer.getInstance().stop();
            Timer.MapTimer.getInstance().stop();
            Timer.BuffTimer.getInstance().stop();
            Timer.CloneTimer.getInstance().stop();
            Timer.CheatTimer.getInstance().stop();
            Timer.EventTimer.getInstance().stop();
            Timer.EtcTimer.getInstance().stop();
            Timer.PingTimer.getInstance().stop();
            FileoutputUtil.log("正在关闭数据库连接...");
            try {
                DatabaseConnection.closeAll();
            } catch (SQLException e) {
                log.error("关闭数据库连接错误" + e);
            }

        }
        FileoutputUtil.log("游戏服务已成功关闭");
        System.exit(0);
    }
}
