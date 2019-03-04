package database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import constants.ServerConstants;
import tools.MapleLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatabaseConnection {

    //    private static final HashMap<Integer, ConWrapper> connections = new HashMap();
    private static DruidDataSource connectionPoll = null;
    public static final int CLOSE_CURRENT_RESULT = 1;
    public static final int KEEP_CURRENT_RESULT = 2;
    public static final int CLOSE_ALL_RESULTS = 3;
    public static final int SUCCESS_NO_INFO = -2;
    public static final int EXECUTE_FAILED = -3;
    public static final int RETURN_GENERATED_KEYS = 1;
    public static final int NO_GENERATED_KEYS = 2;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("[数据库信息] 找不到JDBC驱动.");
            System.exit(0);
        }
    }

    public static Connection getConnection() {
//        Thread cThread = Thread.currentThread();
//        int threadID = (int) cThread.getId();
//        ConWrapper ret = connections.get(Integer.valueOf(threadID));
        try {
            if (connectionPoll == null) {
                connectionPoll = connectToDB();
//                ret = new ConWrapper(retCon);
//                ret.id = threadID;
//                connections.put(threadID, ret);
            }
            MapleLogger.info("get db connection!!!");
            return connectionPoll.getConnection();
        } catch (SQLException e) {
            MapleLogger.error("sql get connection error.", e);
        }
        return null;
//        return ret.getConnection();
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

    private static DruidDataSource connectToDB() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(ServerConstants.SQL_USER);
        dataSource.setPassword(ServerConstants.SQL_PASSWORD);
        dataSource.setUrl("jdbc:mysql://" + ServerConstants.SQL_IP + ":" + ServerConstants.SQL_PORT + "/" + ServerConstants.SQL_DATABASE + "?autoReconnect=true&characterEncoding=GBK&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull");
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(100);
        try {
            dataSource.setFilters("stat"); // 启用监控统计功能
        }catch (Exception e) {}
        dataSource.setPoolPreparedStatements(false);
        dataSource.setRemoveAbandoned(true);    // 程序从池中拿出连接后多久没归还，系统会强制收回该连接
        dataSource.setRemoveAbandonedTimeout(180);

        return dataSource;
    }

    public static void closeAll() {
        connectionPoll.close();
    }
}