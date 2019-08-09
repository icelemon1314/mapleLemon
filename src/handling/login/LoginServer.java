package handling.login;

import handling.MapleServerHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import handling.netty.ServerConnection;
import server.ServerProperties;

import tools.MapleLogger;
import tools.Triple;

public class LoginServer {

    public static short port;
    public static final short DEFAULT_PORT = 8484;
    private static InetSocketAddress InetSocketadd;
    private static ServerConnection acceptor;
    private static Map<Integer, Integer> load = new HashMap();
    private static String serverName;
    private static byte flag;
    private static int maxCharacters;
    private static int userLimit;
    private static int usersOn = 0;
    private static boolean finishedShutdown = true;
    private static boolean adminOnly = false;
    public static boolean autoReg = false;
    private static boolean checkMacs = false;
    private static final HashMap<Integer, Triple<String, String, Integer>> loginAuth = new HashMap();
    private static final HashSet<String> loginIPAuth = new HashSet();

    public static void putLoginAuth(int chrid, String ip, String tempIp, int channel) {
        loginAuth.put(chrid, new Triple(ip, tempIp, channel));
        loginIPAuth.add(ip);
    }

    public static Triple<String, String, Integer> getLoginAuth(int chrid) {
        return (Triple) loginAuth.remove(chrid);
    }

    public static boolean containsIPAuth(String ip) {
        return loginIPAuth.contains(ip);
    }

    public static void removeIPAuth(String ip) {
        loginIPAuth.remove(ip);
    }

    public static void addIPAuth(String ip) {
        loginIPAuth.add(ip);
    }

    public static void addChannel(int channel) {
        load.put(channel, 0);
    }

    public static void removeChannel(int channel) {
        load.remove(channel);
    }

    public static void run_startup_configurations() {
        userLimit = ServerProperties.getProperty("userlimit", 10);
        serverName = ServerProperties.getProperty("serverName", "MapleStory");
        flag = ServerProperties.getProperty("flag", (byte) 3);
        adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("admin", "false"));
        maxCharacters = ServerProperties.getProperty("maxCharacters", 30);
        autoReg = Boolean.parseBoolean(ServerProperties.getProperty("autoReg", "false"));
        checkMacs = Boolean.parseBoolean(ServerProperties.getProperty("checkMacs", "false"));
        port = Short.parseShort(ServerProperties.getProperty("world.port", String.valueOf(DEFAULT_PORT)));

//        IoBuffer.setUseDirectBuffer(false);
//        IoBuffer.setAllocator(new SimpleBufferAllocator());
//
//        acceptor = new NioSocketAcceptor();
//        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
//        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);

        try {
//            acceptor.setHandler(new MapleServerHandler(MapleServerHandler.LOGIN_SERVER));
//            acceptor.bind(new InetSocketAddress(port));
//            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            acceptor = new ServerConnection(port, 1, MapleServerHandler.LOGIN_SERVER);
            acceptor.run();

            MapleLogger.info("\"登入\"伺服器正在监听" + port + "端口\r\n");
        } catch (Exception e) {
            System.err.println("无法绑定" + port + "端口: " + e);
        }
    }

    public static void shutdown() {
        if (finishedShutdown) {
            return;
        }
        MapleLogger.info("正在关闭登录服务器...");
        acceptor.close();
        finishedShutdown = true;
    }

    public static String getServerName() {
        return serverName;
    }

    public static byte getFlag() {
        return flag;
    }

    public static int getMaxCharacters() {
        return maxCharacters;
    }

    public static Map<Integer, Integer> getLoad() {
        return load;
    }

    public static void setLoad(Map<Integer, Integer> load_, int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public static void setFlag(byte newflag) {
        flag = newflag;
    }

    public static int getUserLimit() {
        return userLimit;
    }

    public static int getUsersOn() {
        return usersOn;
    }

    public static void setUserLimit(int newLimit) {
        userLimit = newLimit;
    }

    public static boolean isAdminOnly() {
        return adminOnly;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }

    public static void setOn() {
        finishedShutdown = false;
    }

    public static boolean isAutoReg() {
        return autoReg;
    }

    public static boolean isCheckMacs() {
        return checkMacs;
    }
}
