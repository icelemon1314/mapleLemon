package client;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MapleQuickSlot implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private boolean changed = false;
    private List<Pair<Integer, Integer>> quickslot;

    public MapleQuickSlot() {
        this.quickslot = new ArrayList();
    }

    public MapleQuickSlot(List<Pair<Integer, Integer>> quickslots) {
        this.quickslot = quickslots;
    }

    public List<Pair<Integer, Integer>> Layout() {
        this.changed = true;
        return this.quickslot;
    }

    public void unchanged() {
        this.changed = false;
    }

    public void resetQuickSlot() {
        this.changed = true;
        this.quickslot.clear();
    }

    public void addQuickSlot(int index, int key) {
        this.changed = true;
        this.quickslot.add(new Pair(index, key));
    }

    public int getKeyByIndex(int index) {
        for (Pair p : this.quickslot) {
            if (((Integer) p.getLeft()) == index) {
                return ((Integer) p.getRight());
            }
        }
        return -1;
    }

    public void writeData(MaplePacketLittleEndianWriter mplew) {
        mplew.write(this.quickslot.isEmpty() ? 0 : 1);
        if (this.quickslot.isEmpty()) {
            return;
        }
        Collections.sort(this.quickslot, new QuickSlotComparator());
        for (Pair qs : this.quickslot) {
            mplew.writeInt(((Integer) qs.getRight()));
        }
    }

    public void saveQuickSlots(int charid) throws SQLException {
        if (!this.changed) {
            return;
        }
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM quickslot WHERE characterid = ?");
        ps.setInt(1, charid);
        ps.execute();
        ps.close();
        if (this.quickslot.isEmpty()) {
            return;
        }
        boolean first = true;
        StringBuilder query = new StringBuilder();
        for (Pair q : this.quickslot) {
            if (first) {
                first = false;
                query.append("INSERT INTO quickslot VALUES (");
            } else {
                query.append(",(");
            }
            query.append("DEFAULT,");
            query.append(charid).append(",");
            query.append(((Integer) q.getLeft()).intValue()).append(",");
            query.append(((Integer) q.getRight()).intValue()).append(")");
        }
        ps = con.prepareStatement(query.toString());
        ps.execute();
        ps.close();
    }

    public static class QuickSlotComparator implements Comparator<Pair<Integer, Integer>>, Serializable {

        @Override
        public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
            int val1index = (p1.getLeft());
            int val2index = (p2.getLeft());
            if (val1index > val2index) {
                return 1;
            }
            if (val1index == val2index) {
                return 0;
            }
            return -1;
        }
    }
}
