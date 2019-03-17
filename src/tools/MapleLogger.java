package tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MapleLogger {

    private static Logger log = LoggerFactory.getLogger(MapleLogger.class);

    public static void debug(String msg) {
        log.debug(msg);
    }

    public static void info(String msg) {
        log.info(msg);
    }

    public static void error(String msg) {
        log.error(msg);
    }

    public static void error(Throwable e) {
        MapleLogger.error("exception : ===>",e);
    }

    /**
     * 记录日志错误信息
     *
     * @param msg
     * @param e
     */
    public static void error(String msg, Throwable e) {
        StringBuilder s = new StringBuilder();
        s.append(("exception : -->>"));
        s.append((msg));
        log.error(s.toString(), e);
    }

    public static void toPacketFile() {

    }
}