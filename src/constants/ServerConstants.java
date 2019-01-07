package constants;

import client.LoginCrypto;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import server.ServerProperties;
import tools.FileoutputUtil;

public class ServerConstants implements Runnable {

    public static boolean 打怪获得点抵用卷 = false;
    public static boolean loadop = true;
    public static boolean TESPIA = false;//测试服
    public static boolean GUI = true;
    public static boolean isLinux = !"windows".equals(System.getProperty("sun.desktop"));
    public static String linuxDumpPath = "/opt/lampp/bin/";
    public static String windowsDumpPath = "..\\mysql\\bin\\";
    public static byte[] NEXON_IP = {(byte) 127, (byte) 0, (byte) 0, 1};
    public static String IP = "127.0.0.1";

    public static boolean PollEnabled = false;
    public static String Poll_Question = "Are you mudkiz?";
    public static String[] Poll_Answers = {"test1", "test2", "test3"};
    public static int 攻击上限 = 50000000;
    public static boolean 单机服务端 = false;

    public static short MAPLE_VERSION = 27;
    public static String MAPLE_PATCH = "1";
    public static MapleType MAPLE_TYPE = MapleType.中国;// 如果是測試機這裡不需要改,只要改TESPIA就可以了

    public static int SHARK_VERSION = 0x2021;
    public static boolean USE_FIXED_IV = true;
    public static boolean USE_LOCALHOST = false;
    public static final int MIN_MTS = 150;
    public static final int MTS_BASE = 0;
    public static final int MTS_TAX = 5;
    public static final int MTS_MESO = 2500;
    public static String SQL_IP = "127.0.0.1";
    public static String SQL_PORT = "3306";
    public static String SQL_USER = "root";
    public static String SQL_PASSWORD = "maplestory";
    public static int SQL_SAVETIME = 6;
    public static String SQL_DATABASE = "maplestory";
    public static long SQL_TIMEOUT = 30000;
    public static final List<String> localhostIP = new LinkedList();
    public static final List<String> vpnIp = new LinkedList();
    public static String master;
    public static ServerConstants instance;

    public static String getMaster() {
        if (master == null) {
            return "48239defb943bde63d65d02201262b8cc638b377G";
        }
        return master;
    }

    public static boolean isIPLocalhost(String sessionIP) {
        return (!USE_FIXED_IV) && (localhostIP.contains(sessionIP.replace("/", "")));
    }

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ServerConstants();
            mBeanServer.registerMBean(instance, new ObjectName("constants:type=ServerConstants"));
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            FileoutputUtil.log(e.getMessage());
            FileoutputUtil.log("Error registering Shutdown MBean");
        }
    }

    @Override
    public void run(){

    }

    static {
        localhostIP.add("127.0.0.1");
    }

    public enum MapleType {
        中国(4, "GB18030");
        final byte type;
        final String ascii;

        private MapleType(int type, String ascii) {
            this.type = (byte) type;
            this.ascii = ascii;
        }

        public String getAscii() {
            return ascii;
        }

        public byte getType() {
            return type;
        }

        public static MapleType getByType(byte type) {
            for (MapleType l : MapleType.values()) {
                if (l.getType() == type) {
                    return l;
                }
            }
            return 中国;
        }
    }

    public static void loadSetting() {
        SQL_IP = ServerProperties.getProperty("db_ip", SQL_IP);
        SQL_PORT = ServerProperties.getProperty("db_port", SQL_PORT);
        SQL_DATABASE = ServerProperties.getProperty("db_name", SQL_DATABASE);
        SQL_USER = ServerProperties.getProperty("db_user", SQL_USER);
        SQL_PASSWORD = ServerProperties.getProperty("db_password", SQL_PASSWORD);
        SQL_TIMEOUT = ServerProperties.getProperty("db_timeout", SQL_TIMEOUT);
        SQL_SAVETIME = ServerProperties.getProperty("db_savetime", SQL_SAVETIME);

        IP = ServerProperties.getProperty("IP", IP);
        USE_FIXED_IV = ServerProperties.getProperty("USE_FIXED_IV", USE_FIXED_IV);

        MAPLE_VERSION = ServerProperties.getProperty("MAPLE_VERSION", MAPLE_VERSION);
        MAPLE_PATCH = ServerProperties.getProperty("MAPLE_PATCH", MAPLE_PATCH);
        MAPLE_TYPE = MapleType.getByType(ServerProperties.getProperty("MAPLE_TYPE", MAPLE_TYPE.getType()));

        USE_LOCALHOST = ServerProperties.getProperty("USE_LOCALHOST", USE_LOCALHOST);
        SHARK_VERSION = ServerProperties.getProperty("SHARK_VERSION", SHARK_VERSION);

    }

    static {
        loadSetting();
    }
}
