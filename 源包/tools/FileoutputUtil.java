package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FileoutputUtil {

    public static final String Acc_Stuck = "日志/AccountStuck.log";
    public static final String Item_Expire = "日志/道具到期.txt";
    public static final String EXCEPTION_CAUGHT = "日志/exceptionCaught.txt";
    public static final String Login_Error = "日志/登入错误.log";
    public static final String Packet_Record = "日志/数据包_编写.txt";
    public static final String Packet_Error = "日志/数据包_错误.txt";
    public static final String Packet_Ex = "日志/数据包_异常.txt";
    public static final String Packet_Unk = "日志/数据包_未知.txt";
    public static final String PacketLog = "日志/数据包_收发.log";
    public static final String SkillsLog = "日志/技能日志.log";
    public static final String SkillBuff = "日志/技能BUFF日志.log";
    public static final String AttackLog = "日志/攻击日志.log";
    public static final String ClientError = "日志/客户端错误.log";
    public static final String PlayerSkills = "日志/玩家技能.log";
    public static final String Zakum_Log = "日志/日志_扎昆.log";
    public static final String Horntail_Log = "日志/日志_暗黑龙王.log";
    public static final String Pinkbean_Log = "日志/品克缤.log";
    public static final String PacketEx_Log = "日志/封包异常.log";
    public static final String Donator_Log = "日志/捐赠.log";
    public static final String Hacker_Log = "日志/Hacker.log";
    public static final String SpecialSkill_log = "日志/SpecialSkill.log";
    public static final String SkillCancel_Error = "日志/取消BUFF错误.txt";
    public static final String 掉血错误 = "日志/掉血错误.log";
    public static final String 攻击出错 = "日志/攻击出错.log";
    public static final String 攻击异常 = "日志/攻击异常.log";
    public static final String 封包出错 = "日志/封包出错.log";
    public static final String 数据异常 = "日志/数据异常.log";
    public static final String 复制装备 = "日志/复制装备.log";
    public static final String 攻击怪物封包 = "日志/攻击怪物封包.log";
    public static final String 获取未处理被动技能 = "日志/获取未处理被动技能.log";
    public static final String 在线统计 = "日志/在线统计.txt";
    public static final String 发现异常 = "日志/发现异常.txt";
    public static final String 离开商城 = "日志/离开商城.txt";
    public static final String 玩家互动封包 = "日志/玩家互动封包.txt";
    public static final String 捐赠 = "日志/捐赠.txt";
    public static final String 地图名字错误 = "日志/地图名字错误.txt";
    public static final String 未处理的怪物技能 = "日志/未处理的怪物技能.log";
    public static final String CommandErr_Log = "日志/命令错误.log";
    public static final String CommandEx_Log = "日志/命令异常.log";
    public static final String ScriptEx_Log = "日志/脚本/脚本异常.log";
    public static final String Event_ScriptEx_Log = "日志/脚本/事件脚本异常.log";
    public static final String Item_ScriptEx_Log = "日志/脚本/物品脚本异常.log";
    public static final String Map_ScriptEx_Log = "日志/脚本/地图脚本异常.log";
    public static final String Portal_ScriptEx_Log = "日志/脚本/传送点脚本异常.log";
    public static final String Reactor_ScriptEx_Log = "日志/脚本/反应堆脚本异常.log";
    public static final String Quest_ScriptEx_Log = "日志/脚本/任务脚本异常.log";
    public static final String SQL_Ex_Log = "日志/数据库异常.log";
    public static final String GUI_Ex_Log = "日志/GUI异常记录.log";
     public static final String Shark_Dir = "日志/枫鲨档案/";
    public static final String HiredMerchDir = "日志/HiredMerch/";
    public static final String Movement_Dir = "日志/移动封包出错/";
    public static final String Movement_Sumon = Movement_Dir + "召唤兽移动出错.log";
    public static final String Movement_Char = Movement_Dir + "角色移动出错.log";
    public static final String Movement_Pet = Movement_Dir + "宠物移动出错.log";
    public static final String Movement_Mob = Movement_Dir + "怪物移动出错.log";
    public static final String Movement_Err = Movement_Dir + "移动封包出错.log";
    public static final String Movement_Unk = Movement_Dir + "未知移动封包.log";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");
    private static final String FILE_PATH = "日志/";
    private static final String ERROR = "錯誤/" + sdf_.format(Calendar.getInstance().getTime()) + "/";
    private static final String GeneralLog = "日志/运行日志.log";

    public static void printError(String name, String msg) {
        printError(name, null, msg);
    }

    public static void printError(String name, Throwable t) {
        printError(name, t, null);
    }

    public static void printError(String name, Throwable t, String info) {
        outputFileError(FILE_PATH + ERROR + name, t, info);
    }

    public static void outputFileError(String name, String msg) {
        outputFileError(name, null, msg);
    }

    public static void outputFileError(String file, Throwable t) {
        outputFileError(file, t, null);
    }

    public static void outputFileError(String file, Throwable t, String info) {
        logToFile(file, "\r\n------------------------ " + CurrentReadable_Time() + " ------------------------\r\n" + (info != null ? (info + "\r\n") : "") + (t != null ? getString(t) : ""));
    }

    public static void log(String msg) {
        logToFile(FileoutputUtil.GeneralLog,msg+"\r\n");
    }

    public static void log(String file, String msg) {
        log(file, msg, true);
    }

    public static void log(String file, String msg, boolean warp) {
        logToFile(file, (warp ? "\r\n------------------------ " + CurrentReadable_Time() + " ------------------------\r\n" : "") + msg);
    }

    public static void packetLog(String file, String msg) {
        logToFile(file, msg + "\r\n\r\n");
    }

    public static void hiredMerchLog(String name, String msg) {
        logToFile(HiredMerchDir + name + ".txt", "[" + CurrentReadable_Time() + "] " + msg + "\r\n");
    }

    public static void logToFile(final String file, final String msg) {
        FileOutputStream out = null;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            osw.write(msg);
            osw.flush();
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static String CurrentReadable_Date() {
        return sdf_.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_Time() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_TimeGMT() {
        return sdfGMT.format(new Date());
    }

    public static String getString(Throwable e) {
        String retValue = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
            }
        }
        return retValue;
    }

    static {
        sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
