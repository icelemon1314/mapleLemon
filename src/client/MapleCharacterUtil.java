package client;

import database.DatabaseConnection;
import database.dao.CharacterDao;
import database.entity.CharacterPO;
import handling.channel.ChannelServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import tools.MapleLogger;
import tools.Triple;

public class MapleCharacterUtil {

    private static final Pattern namePattern = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_一-龥]+$");
    private static final Pattern petPattern = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_一-龥]+$");

    public static boolean canCreateChar(String name, boolean gm) {
        return (getIdByName(name) == -1) && (isEligibleCharName(name, gm));
    }

    public static boolean canChangePetName(String name) {
        if ((name.getBytes().length < 4) || (name.getBytes().length > 12)) {
            return false;
        }
        return petPattern.matcher(name).matches();
    }

    public static boolean isEligibleCharName(String name, boolean gm) {
        if (name.getBytes().length > 12) {
            return false;
        }
        if (gm) {
            return true;
        }
        if (name.getBytes().length < 4) {
            return false;
        }
        return namePattern.matcher(name).matches();
    }

    public static String makeMapleReadable(String in) {
        String wui = in.replace('I', 'i');
        wui = wui.replace('l', 'L');
        wui = wui.replace("rn", "Rn");
        wui = wui.replace("vv", "Vv");
        wui = wui.replace("VV", "Vv");
        return wui;
    }

    public static int getIdByName(String name) {
        CharacterDao charDao = new CharacterDao();
        CharacterPO chrPo = charDao.getCharacterByName(name);
        if (chrPo.getId() != 0) {
            return chrPo.getId();
        } else {
            return -1;
        }
    }

    public static boolean PromptPoll(int accountid) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean prompt = false;
        Connection con = DatabaseConnection.getConnection();
        try {
            ps = con.prepareStatement("SELECT * from game_poll_reply where AccountId = ?");
            ps.setInt(1, accountid);
            rs = ps.executeQuery();
            prompt = !rs.next();
            ps.close();
        } catch (SQLException e) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.close();
            } catch (SQLException e) {
            }
        }
        return prompt;
    }

    public static boolean SetPoll(int accountid, int selection) {
        if (!PromptPoll(accountid)) {
            return false;
        }
        PreparedStatement ps = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            ps = con.prepareStatement("INSERT INTO game_poll_reply (AccountId, SelectAns) VALUES (?, ?)");
            ps.setInt(1, accountid);
            ps.setInt(2, selection);

            ps.execute();
            ps.close();
        } catch (SQLException e) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                con.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

    public static void setNXCodeUsed(String name, String code) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `user` = ?, `valid` = 0, time = CURRENT_TIMESTAMP() WHERE code = ?")) {
            ps.setString(1, name);
            ps.setString(2, code);
            ps.execute();
            ps.close();
        }
    }

    /**
     * 玩家发给玩家的消息
     * @param to
     * @param name
     * @param msg
     * @param fame
     */
    public static void sendNote(String to, String name, String msg, int fame) {
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `gift`) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, to);
            ps.setString(2, name);
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setInt(5, fame);
            ps.executeUpdate();
            ps.close();
            MapleCharacter receiver = ChannelServer.getCharacterByName(to);
            if (receiver != null) {
                receiver.showNote();
            }
        } catch (SQLException e) {
            MapleLogger.error("Unable to send note" + e);
        }
    }

    public static Triple<Boolean, Integer, Integer> getNXCodeInfo(String code) throws SQLException {
        Triple ret = null;
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT `valid`, `type`, `item` FROM nxcode WHERE code = ?")) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = new Triple(rs.getInt("valid") > 0,rs.getInt("type"), rs.getInt("item"));
            }
            rs.close();
            ps.close();
        }
        return ret;
    }
}


