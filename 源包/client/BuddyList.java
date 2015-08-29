package client;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.packet.BuddyListPacket;

public class BuddyList implements Serializable {

    private static final long serialVersionUID = 1413738569L;
    private final Map<Integer, BuddylistEntry> buddies = new LinkedHashMap();
    private byte capacity;
    private boolean changed = false;

    public BuddyList(byte capacity) {
        this.capacity = capacity;
    }

    public boolean contains(int characterId) {
        return this.buddies.containsKey(characterId);
    }

    public boolean containsVisible(int characterId) {
        BuddylistEntry ble = (BuddylistEntry) this.buddies.get(Integer.valueOf(characterId));
        if (ble == null) {
            return false;
        }
        return ble.isVisible();
    }

    public byte getCapacity() {
        return this.capacity;
    }

    public void setCapacity(byte capacity) {
        this.capacity = capacity;
    }

    public BuddylistEntry get(int characterId) {
        return (BuddylistEntry) this.buddies.get(characterId);
    }

    public BuddylistEntry get(String characterName) {
        String lowerCaseName = characterName.toLowerCase();
        for (BuddylistEntry ble : this.buddies.values()) {
            if (ble.getName().toLowerCase().equals(lowerCaseName)) {
                return ble;
            }
        }
        return null;
    }

    public void put(BuddylistEntry entry) {
        this.buddies.put(entry.getCharacterId(), entry);
        this.changed = true;
    }

    public void remove(int characterId) {
        this.buddies.remove(characterId);
        this.changed = true;
    }

    public Collection<BuddylistEntry> getBuddies() {
        return this.buddies.values();
    }

    public boolean isFull() {
        return this.buddies.size() >= this.capacity;
    }

    public int[] getBuddyIds() {
        int[] buddyIds = new int[this.buddies.size()];
        int i = 0;
        for (BuddylistEntry ble : this.buddies.values()) {
            if (ble.isVisible()) {
                buddyIds[(i++)] = ble.getCharacterId();
            }
        }
        return buddyIds;
    }

    public void loadFromTransfer(Map<CharacterNameAndId, Boolean> data) {
        for (Map.Entry qs : data.entrySet()) {
            CharacterNameAndId buddyid = (CharacterNameAndId) qs.getKey();
            put(new BuddylistEntry(buddyid.getName(), buddyid.getId(), buddyid.getGroup(), -1, ((Boolean) qs.getValue())));
        }
    }

    public void loadFromDb(int characterId) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT b.buddyid, b.pending, c.name as buddyname, b.groupname FROM buddies as b, characters as c WHERE c.id = b.buddyid AND b.characterid = ?")) {
            ps.setInt(1, characterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    put(new BuddylistEntry(rs.getString("buddyname"), rs.getInt("buddyid"), rs.getString("groupname"), -1, rs.getInt("pending") != 1));
                }
            }
            ps.close();
        }
    }

    public void addBuddyRequest(MapleClient c, int cidFrom, String nameFrom, int channelFrom, int levelFrom, int jobFrom, int AccID) {
        put(new BuddylistEntry(nameFrom, cidFrom, "未指定群组", channelFrom, false));
        c.getSession().write(BuddyListPacket.requestBuddylistAdd(cidFrom, nameFrom, levelFrom, jobFrom, channelFrom, AccID));
    }

    public void setChanged(boolean v) {
        this.changed = v;
    }

    public boolean changed() {
        return this.changed;
    }

    public static enum BuddyAddResult {

        好友列表已满, 已经是好友关系, 添加好友成功;
    }

    public static enum BuddyOperation {

        添加好友, 删除好友;
    }
}
