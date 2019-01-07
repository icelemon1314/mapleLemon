package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DateUtil {

    private static final int ITEM_YEAR2000 = -1085019342;
    private static final long REAL_YEAR2000 = 946681229830L;
    private static final int QUEST_UNIXAGE = 27111908;
    private static final long FT_UT_OFFSET = 116444520000000000L;

    public static long getTempBanTimestamp(long realTimestamp) {
//        return realTimestamp * 10000L + 116444520000000000L;
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static int getItemTimestamp(long realTimestamp) {
        int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000L / 60L);
        return (int) (time * 35.762787000000003D) + ITEM_YEAR2000;
    }

    public static int getQuestTimestamp(long realTimestamp) {
        int time = (int) (realTimestamp / 1000L / 60L);
        return (int) (time * 0.1396987D) + QUEST_UNIXAGE;
    }

    public static boolean isDST() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    public static long getFileTimestamp(long timeStampinMillis) {
        return getFileTimestamp(timeStampinMillis, false);
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (isDST()) {
            timeStampinMillis -= 3600000L;
        }
        timeStampinMillis += 50400000L;
        long time;
        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        } else {
            time = timeStampinMillis * 10000L;
        }
        return time + FT_UT_OFFSET;
    }

    public static int getTime() {
        String time = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date()).replace("-", "");
        return Integer.valueOf(time);
    }

    public static int getTime(long realTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        return Integer.valueOf(sdf.format(realTimestamp));
    }

    public static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        return sdf.format(new Date());
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public static String getCurrentDate(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(new Date());
    }

    public static String getFormatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static String getFormatDate(Date date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    public static String getPreDate(String field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if ((field != null) && (!field.equals(""))) {
            switch (field) {
                case "y":
                    calendar.add(1, amount);
                    break;
                case "M":
                    calendar.add(2, amount);
                    break;
                case "d":
                    calendar.add(5, amount);
                    break;
                case "H":
                    calendar.add(10, amount);
                    break;
            }
        } else {
            return null;
        }
        return getFormatDate(calendar.getTime());
    }

    public static String getPreDate(Date d, String field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        if ((field != null) && (!field.equals(""))) {
            switch (field) {
                case "y":
                    calendar.add(1, amount);
                    break;
                case "M":
                    calendar.add(2, amount);
                    break;
                case "d":
                    calendar.add(5, amount);
                    break;
                case "H":
                    calendar.add(10, amount);
                    break;
            }
        } else {
            return null;
        }
        return getFormatDate(calendar.getTime());
    }

    public static String getPreDate(String date)
            throws ParseException {
        Date d = new SimpleDateFormat().parse(date);
        String preD = getPreDate(d, "d", 1);
        Date preDate = new SimpleDateFormat().parse(preD);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(preDate);
    }

    public static long getStringToTime(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmm");
        try {
            Date date = df.parse(dateString);
            return date.getTime();
        } catch (ParseException ex) {
            FileoutputUtil.log(ex.getMessage());
        }
        return -1L;
    }
}
