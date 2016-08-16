package handling.cashshop;

import constants.ServerConstants;
import handling.MapleServerHandler;
import handling.channel.PlayerStorage;
import handling.mina.MapleCodecFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.ServerProperties;
import tools.FileoutputUtil;

/**
 * 现金商城服务
 * @author 7
 */
public class CashShopServer {

    private static String ip;
    private static IoAcceptor acceptor;
    private static PlayerStorage players;
    private static boolean finishedShutdown = false;
    public static short port;
    private static final short DEFAULT_PORT = 8600;
    private static final Logger log = Logger.getLogger(CashShopServer.class);
    private static int autoPaoDian;

    public static void run_startup_configurations() {
        autoPaoDian = Integer.parseInt(ServerProperties.getProperty("autoPaoDian", "1"));
        port = Short.parseShort(ServerProperties.getProperty("cashshop.port", String.valueOf(DEFAULT_PORT)));
        ip = ServerProperties.getProperty("world.host", ServerConstants.IP) + ":" + port;

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());

        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        players = new PlayerStorage(MapleServerHandler.CASH_SHOP_SERVER);
        try {
            acceptor.setHandler(new MapleServerHandler(MapleServerHandler.CASH_SHOP_SERVER));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);

            FileoutputUtil.log("完成!");
            FileoutputUtil.log("商城伺服器正在监听" + port + "端口\r\n");
        } catch (IOException e) {
            FileoutputUtil.log("失败!");
            System.err.println("无法绑定" + port + "端口");
            throw new RuntimeException("绑定端口失败.", e);
        }
    }

    public static String getIP() {
        return ip;
    }

    public static PlayerStorage getPlayerStorage() {
        return players;
    }

    public static int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public static void shutdown() {
        if (finishedShutdown) {
            return;
        }
        FileoutputUtil.log("正在关闭商城服务器...");
        players.disconnectAll();
        FileoutputUtil.log("商城服务器解除端口绑定...");
        acceptor.unbind();
        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }

    public static String getCashBlockedMsg(int itemId) {
        switch (itemId) {
            case 5050000:
            case 5060003:
            case 5072000:
            case 5073000:
            case 5074000:
            case 5076000:
            case 5077000:
            case 5079001:
            case 5079002:
            case 5360000:
            case 5360014:
            case 5360015:
            case 5360016:
            case 5390000:
            case 5390001:
            case 5390002:
            case 5390003:
            case 5390004:
            case 5390005:
            case 5390006:
            case 5390007:
            case 5390008:
            case 5390010:
                return "该道具只能通过NPC购买。";
        }
        return "该道具禁止购买。";
    }
}
