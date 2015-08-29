package client.inventory;

import client.MapleCharacter;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import org.apache.log4j.Logger;
import server.MapleInventoryManipulator;

public class MapleRing implements Serializable {

    private static final Logger log = Logger.getLogger(MapleRing.class);
    private static final long serialVersionUID = 9179541993413738579L;
    private final int ringId;
    private final int partnerRingId;
    private final int partnerId;
    private final int itemId;
    private String partnerName;
    private boolean equipped = false;

    private MapleRing(int ringId, int partnerRingId, int partnerId, int itemid, String partnerName) {
        this.ringId = ringId;
        this.partnerRingId = partnerRingId;
        this.partnerId = partnerId;
        this.itemId = itemid;
        this.partnerName = partnerName;
    }

    public static MapleRing loadFromDb(int ringId) {
        return loadFromDb(ringId, false);
    }

    public static MapleRing loadFromDb(int ringId, boolean equipped) {
        try {
            MapleRing ret;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM rings WHERE ringId = ?")) {
                ps.setInt(1, ringId);
                try (ResultSet rs = ps.executeQuery()) {
                    ret = null;
                    if (rs.next()) {
                        ret = new MapleRing(ringId, rs.getInt("partnerRingId"), rs.getInt("partnerChrId"), rs.getInt("itemid"), rs.getString("partnerName"));
                        ret.setEquipped(equipped);
                    }
                }
                ps.close();
            }
            return ret;
        } catch (SQLException ex) {
            log.error("加载戒指信息出错", ex);
        }
        return null;
    }

    public static void addToDB(int itemid, MapleCharacter player, String partnerName, int partnerId, int[] ringId) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO rings (ringId, itemid, partnerChrId, partnerName, partnerRingId) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, ringId[0]);
        ps.setInt(2, itemid);
        ps.setInt(3, player.getId());
        ps.setString(4, player.getName());
        ps.setInt(5, ringId[1]);
        ps.executeUpdate();
        ps.close();

        ps = con.prepareStatement("INSERT INTO rings (ringId, itemid, partnerChrId, partnerName, partnerRingId) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, ringId[1]);
        ps.setInt(2, itemid);
        ps.setInt(3, partnerId);
        ps.setString(4, partnerName);
        ps.setInt(5, ringId[0]);
        ps.executeUpdate();
        ps.close();
    }

    public static int createRing(int itemId, MapleCharacter player, String partnerName, String msg, int partnerId, int itemSn) {
        try {
            if (player == null) {
                return -2;
            }
            if (partnerId <= 0) {
                return -1;
            }
            return makeRing(itemId, player, partnerName, partnerId, msg, itemSn);
        } catch (Exception ex) {
            log.error("创建戒指信息出错", ex);
        }
        return 0;
    }

    public static int[] makeRing(int itemId, MapleCharacter player, MapleCharacter partnerPlayer)
            throws Exception {
        int[] makeRingId = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
        try {
            addToDB(itemId, player, partnerPlayer.getName(), partnerPlayer.getId(), makeRingId);
        } catch (MySQLIntegrityConstraintViolationException mslcve) {
            return makeRingId;
        }
        return makeRingId;
    }

    public static int makeRing(int itemId, MapleCharacter player, String partnerName, int partnerId, String msg, int itemSn) throws Exception {
        int[] makeRingId = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
        try {
            addToDB(itemId, player, partnerName, partnerId, makeRingId);
        } catch (MySQLIntegrityConstraintViolationException mslcve) {
            return 0;
        }
        MapleInventoryManipulator.addRing(player, itemId, makeRingId[1], itemSn);
        player.getCashInventory().gift(partnerId, player.getName(), msg, itemSn, makeRingId[0]);
        return 1;
    }

    public int getRingId() {
        return this.ringId;
    }

    public int getPartnerRingId() {
        return this.partnerRingId;
    }

    public int getPartnerChrId() {
        return this.partnerId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public boolean isEquipped() {
        return this.equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    public String getPartnerName() {
        return this.partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof MapleRing)) {
            return ((MapleRing) o).getRingId() == getRingId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.ringId;
        return hash;
    }

    public static void removeRingFromDb(int ringId, int partnerRingId) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM rings WHERE ringId = ? OR ringId = ?")) {
                ps.setInt(1, ringId);
                ps.setInt(2, partnerRingId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException ex) {
            log.error("删除戒指信息出错", ex);
        }
    }

    public static void removeRingFromDb(MapleCharacter player) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM rings WHERE partnerChrId = ?");
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                rs.close();
                return;
            }
            int otherId = rs.getInt("partnerRingId");
            int otherotherId = rs.getInt("ringId");
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rings WHERE ringId = ? OR ringId = ?");
            ps.setInt(1, otherotherId);
            ps.setInt(2, otherId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            log.error("删除戒指信息出错", ex);
        }
    }

    public static class RingComparator implements Comparator<MapleRing>, Serializable {

        @Override
        public int compare(MapleRing o1, MapleRing o2) {
            if (o1.ringId < o2.ringId) {
                return -1;
            }
            if (o1.ringId == o2.ringId) {
                return 0;
            }
            return 1;
        }
    }
}
