package client.messages;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.commands.AdminCommand;
import client.messages.commands.CommandExecute;
import client.messages.commands.CommandObject;
import client.messages.commands.GMCommand;
import client.messages.commands.InternCommand;
import client.messages.commands.PlayerCommand;
import client.messages.commands.SuperDonatorCommand;
import client.messages.commands.SuperGMCommand;
import constants.ServerConstants;
import database.DatabaseConnection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import tools.FileoutputUtil;

public class CommandProcessor {

    private static final HashMap<String, CommandObject> commands = new HashMap();
    private static final HashMap<Integer, ArrayList<String>> commandList = new HashMap();

    private static void sendDisplayMessage(MapleClient c, String msg, CommandType type) {
        if (c.getPlayer() == null) {
            return;
        }
        switch (type) {
            case NORMAL:
                c.getPlayer().dropMessage(6, msg);
                break;
            case TRADE:
                c.getPlayer().dropMessage(-2, new StringBuilder().append("错误 : ").append(msg).toString());
                break;
        }
    }

    public static void dropHelp(MapleClient c) {
        for (int i = 0; i <= c.getPlayer().getGMLevel(); i++) {
            if (commandList.containsKey(i)) {
                final StringBuilder sb = new StringBuilder("");
                final StringBuilder 命令前缀 = new StringBuilder("");
                char[] gmRank = PlayerGMRank.getByLevel(i).getCommandPrefix();
                for (int j = 0; j < gmRank.length; j++) {
                    命令前缀.append('"').append(gmRank[j]).append('"');
                    if (j != gmRank.length - 1 && gmRank.length != 1) {
                        命令前缀.append("或");
                    }
                }
                c.getPlayer().dropMessage(6, "-----------------------------------------------------------------------------------------");
                if (i == 0) {
                    c.getPlayer().dropMessage(6, "玩家命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 1) {
                    c.getPlayer().dropMessage(6, "捐赠者命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 2) {
                    c.getPlayer().dropMessage(6, "高级捐赠者命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 3) {
                    c.getPlayer().dropMessage(6, "实习管理员命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 4) {
                    c.getPlayer().dropMessage(6, "游戏管理员命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 5) {
                    c.getPlayer().dropMessage(6, "高级理员命令(前缀:" + 命令前缀 + ")：");
                } else if (i == 6) {
                    c.getPlayer().dropMessage(6, "服务器管理员命令(前缀:" + 命令前缀 + ")：");
                }
                for (String s : commandList.get(i)) {
                    if ((gmRank.length > 1 && s.substring(0, 1).equals(String.valueOf(gmRank[0]))) || gmRank.length == 1) {
                        sb.append(s.substring(1));
                        sb.append("，");
                    }
                }
                c.getPlayer().dropMessage(6, sb.toString());
            }
        }
    }

    public static boolean processCommand(MapleClient c, String line, CommandType type) {
        if ((line.charAt(0) == PlayerGMRank.NORMAL.getCommandPrefix()[0]) || ((c.getPlayer().getGMLevel() > PlayerGMRank.NORMAL.getLevel()) && (line.charAt(0) == PlayerGMRank.DONATOR.getCommandPrefix()[0]))) {
            String[] splitted = line.split(" ");
            splitted[0] = splitted[0].toLowerCase();

            CommandObject co = (CommandObject) commands.get(splitted[0]);
            if ((co == null) || (co.getType() != type)) {
                if (ServerConstants.单机服务端) {
                    if (splitted[0].equals(new StringBuilder().append(line.charAt(0)).append("设置管理员").toString())) {
                        c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]).setGmLevel(Byte.parseByte(splitted[2]));
                        return true;
                    }
                    c.getPlayer().dropMessage(5, "输入\"@设置管理员 <等级>\"可以设置管理员等级。");
                }
                sendDisplayMessage(c, "命令不存在，使用\"@帮助\"可以查看可用命令列表。", type);
                return true;
            }
            try {
                int ret = co.execute(c, splitted);
            } catch (Exception e) {
                int ret;
                sendDisplayMessage(c, "使用命令出现错误：", type);
                if (c.getPlayer().isGM()) {
                    sendDisplayMessage(c, new StringBuilder().append("错误: ").append(e).toString(), type);
                    FileoutputUtil.outputFileError(FileoutputUtil.CommandErr_Log, e);
                }
            }
            return true;
        }
        if ((c.getPlayer().getGMLevel() > PlayerGMRank.NORMAL.getLevel())
                && ((line.charAt(0) == PlayerGMRank.SUPERGM.getCommandPrefix()[0])
                || (line.charAt(0) == PlayerGMRank.INTERN.getCommandPrefix()[0])
                || (line.charAt(0) == PlayerGMRank.GM.getCommandPrefix()[0])
                || (line.charAt(0) == PlayerGMRank.ADMIN.getCommandPrefix()[0])
                || (line.charAt(0) == PlayerGMRank.SUPERGM.getCommandPrefix()[1])
                || (line.charAt(0) == PlayerGMRank.INTERN.getCommandPrefix()[1])
                || (line.charAt(0) == PlayerGMRank.GM.getCommandPrefix()[1])
                || (line.charAt(0) == PlayerGMRank.ADMIN.getCommandPrefix()[1]))) {
            String[] splitted = line.split(" ");
            splitted[0] = splitted[0].toLowerCase();
            CommandObject co = (CommandObject) commands.get(splitted[0]);
            if (co == null) {
                if (splitted[0].equals(new StringBuilder().append(line.charAt(0)).append("帮助").toString())) {
                    dropHelp(c);
                    return true;
                }
                sendDisplayMessage(c, "命令不存在，使用\"!帮助\"或\"！帮助\"可以查看可用命令列表。", type);
                sendDisplayMessage(c, "使用\"!搜索命令 <关键字词>\"或\"！搜索命令 <关键字词>\"可以搜索可用命令。", type);
                return true;
            }
            if (c.getPlayer().getGMLevel() >= co.getReqGMLevel()) {
                int ret = 0;
                try {
                    ret = co.execute(c, splitted);
                } catch (ArrayIndexOutOfBoundsException x) {
                    sendDisplayMessage(c, new StringBuilder().append("使用命令出错，该命令必须带参数才能使用: ").append(x).toString(), type);
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.CommandEx_Log, e);
                }
                if ((ret > 0) && (c.getPlayer() != null)) {
                    if (c.getPlayer().isGM()) {
                        logCommandToDB(c.getPlayer(), line, "gmlog");
                    }
                }
            } else {
                sendDisplayMessage(c, "你没有权限使用该命令", type);
            }
            return true;
        }

        return false;
    }

    private static void logCommandToDB(MapleCharacter player, String command, String table) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement(new StringBuilder().append("INSERT INTO ").append(table).append(" (cid, name, command, mapid) VALUES (?, ?, ?, ?)").toString());
            ps.setInt(1, player.getId());
            ps.setString(2, player.getName());
            ps.setString(3, command);
            ps.setInt(4, player.getMap().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.CommandErr_Log, e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    static {
        Class[] CommandFiles = {PlayerCommand.class, InternCommand.class, GMCommand.class, AdminCommand.class, SuperDonatorCommand.class, SuperGMCommand.class};

        for (Class clasz : CommandFiles) {
            try {
                PlayerGMRank rankNeeded = (PlayerGMRank) clasz.getMethod("getPlayerLevelRequired", new Class[0]).invoke(null, (Object[]) null);
                Class[] a = clasz.getDeclaredClasses();
                ArrayList cL = new ArrayList();
                for (Class c : a) {
                    try {
                        if ((!Modifier.isAbstract(c.getModifiers())) && (!c.isSynthetic())) {
                            Object o = c.newInstance();
                            boolean enabled;
                            try {
                                enabled = c.getDeclaredField("enabled").getBoolean(c.getDeclaredField("enabled"));
                            } catch (NoSuchFieldException ex) {
                                enabled = true;
                            }
                            if (((o instanceof CommandExecute)) && (enabled)) {
                                for (int i = 0; i < rankNeeded.getCommandPrefix().length; i++) {
                                    cL.add(new StringBuilder().append(rankNeeded.getCommandPrefix()[i]).append(c.getSimpleName().toLowerCase()).toString());
                                    commands.put(new StringBuilder().append(rankNeeded.getCommandPrefix()[i]).append(c.getSimpleName().toLowerCase()).toString(), new CommandObject((CommandExecute) o, rankNeeded.getLevel()));
                                    if ((rankNeeded.getCommandPrefix()[i] != PlayerGMRank.GM.getCommandPrefix()[i]) && (rankNeeded.getCommandPrefix()[i] != PlayerGMRank.NORMAL.getCommandPrefix()[i])) {
                                        commands.put(new StringBuilder().append(i == 0 ? "!" : "！").append(c.getSimpleName().toLowerCase()).toString(), new CommandObject((CommandExecute) o, PlayerGMRank.GM.getLevel()));
                                    }
                                }
                            }
                        }
                    } catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
                        FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
                    }
                }
                Collections.sort(cL);
                commandList.put(rankNeeded.getLevel(), cL);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            }
        }
    }

    public static HashMap<Integer, ArrayList<String>> getCommandList() {
        return commandList;
    }
}
