package database;

import constants.ServerConstants;
import tools.FileoutputUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatabaseConnection {

    private static final HashMap<Integer, ConWrapper> connections = new HashMap();
    public static final int CLOSE_CURRENT_RESULT = 1;
    public static final int KEEP_CURRENT_RESULT = 2;
    public static final int CLOSE_ALL_RESULTS = 3;
    public static final int SUCCESS_NO_INFO = -2;
    public static final int EXECUTE_FAILED = -3;
    public static final int RETURN_GENERATED_KEYS = 1;
    public static final int NO_GENERATED_KEYS = 2;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            FileoutputUtil.log("[数据库信息] 找不到JDBC驱动.");
            System.exit(0);
        }
    }

    public static Connection getConnection() {
        Thread cThread = Thread.currentThread();
        int threadID = (int) cThread.getId();
        ConWrapper ret = (ConWrapper) connections.get(Integer.valueOf(threadID));
        if (ret == null) {
            Connection retCon = connectToDB();
            ret = new ConWrapper(retCon);
            ret.id = threadID;
            connections.put(threadID, ret);
        }
        return ret.getConnection();
    }

    private static long getWaitTimeout(Connection con) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'wait_timeout'");
            long l1;
            if (rs.next()) {
                return Math.max(1000, rs.getInt(2) * 1000 - 1000);
            }
            return -1L;
        } catch (SQLException ex) {
            long l2 = -1L;
            return l2;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex1) {
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException ex1) {
                        }
                    }
                }
            }
        }
    }

    private static Connection connectToDB() {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://" + ServerConstants.SQL_IP + ":" + ServerConstants.SQL_PORT + "/" + ServerConstants.SQL_DATABASE + "?autoReconnect=true&characterEncoding=GBK", 
                    ServerConstants.SQL_USER, ServerConstants.SQL_PASSWORD);
            long timeout = getWaitTimeout(con);
            if (timeout == -1L) {
                FileoutputUtil.log("[数据库信息] 无法读取超时时间, using " + ServerConstants.SQL_TIMEOUT + " instead.");
            } else {
                ServerConstants.SQL_TIMEOUT = timeout;
                FileoutputUtil.log("[数据库信息] 连接超时时间为: " + (ServerConstants.SQL_TIMEOUT / 1000 / 60) + " 分钟.");
            }
            return con;
        } catch (SQLException e) {
            throw new DatabaseException("[数据库信息] 连接到数据库错误,请检查MYSQL数据库是否开启,账号密码数据库名是否正确", e);
        }
    }

    public static void closeAll() throws SQLException {
        for (ConWrapper con : connections.values()) {
            con.connection.close();
        }
        connections.clear();
    }

    public static void closeConnection() throws SQLException {
        Iterator<Map.Entry<Integer, ConWrapper>> con = connections.entrySet().iterator();
        Map<Integer, ConWrapper> toclose = new HashMap();
        while (con.hasNext()) {
            Map.Entry<Integer, ConWrapper> temp = con.next();
            if (temp.getValue().expiredConnection()) {
                toclose.put(temp.getKey(), temp.getValue());
                FileoutputUtil.log("过时连接已经被清理...");
            }
        }
        for (Map.Entry<Integer, ConWrapper> t : toclose.entrySet()) {
            t.getValue().connection.close();
            connections.remove(t.getKey());
        }

    }

    public static class ConWrapper {

        private long lastAccessTime = 0L;
        private Connection connection;
        private int id;

        public ConWrapper(Connection con) {
            this.connection = con;
        }

        public Connection getConnection() {
            if (expiredConnection()) {
                try {
                    this.connection.close();
                } catch (SQLException err) {
                }
                connection = connectToDB();
            }
            this.lastAccessTime = System.currentTimeMillis();
            return this.connection;
        }

        public boolean expiredConnection() {
            if (this.lastAccessTime == 0L) {
                return false;
            }
            try {
                return (System.currentTimeMillis() - this.lastAccessTime >= ServerConstants.SQL_TIMEOUT) || (this.connection.isClosed());
            } catch (SQLException ex) {
            }
            return true;
        }
    }
}
