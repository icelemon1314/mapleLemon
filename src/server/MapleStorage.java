package server;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ItemConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import tools.Pair;
import tools.packet.NPCPacket;

public class MapleStorage
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final int storageId;
    private final int accountId;
    private final List<Item> items;
    private long meso;
    private byte slots;
    private int storageNpcId;
    private boolean changed = false;
    private final Map<MapleInventoryType, List<Item>> typeItems = new EnumMap(MapleInventoryType.class);

    private MapleStorage(int storageId, byte slots, long meso, int accountId) {
        this.storageId = storageId;
        this.slots = slots;
        this.meso = meso;
        this.accountId = accountId;
        this.items = new LinkedList();
        if (this.slots > 96) {
            this.slots = 96;
            this.changed = true;
        }
    }

    public static int create(int accountId)
            throws SQLException {
        ResultSet rs;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, ?, ?)", 1)) {
            ps.setInt(1, accountId);
            ps.setInt(2, 4);
            ps.setInt(3, 0);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int storageid = rs.getInt(1);
                ps.close();
                rs.close();
                return storageid;
            }
            ps.close();
        }
        rs.close();
        throw new DatabaseException("Inserting char failed.");
    }

    public static MapleStorage loadOrCreateFromDB(int accountId) {
        MapleStorage ret = null;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM storages WHERE accountid = ?");
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, rs.getByte("slots"), rs.getLong("meso"), accountId);
                rs.close();
                ps.close();
                for (Pair<Item, MapleInventoryType> mit : ItemLoader.仓库道具.loadItems(false, accountId).values()) {
                    ret.items.add(mit.getLeft());
                }
            } else {
                int storeId = create(accountId);
                ret = new MapleStorage(storeId, (byte) 4, 0, accountId);
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) {
            System.err.println("Error loading storage" + ex);
        }
        return ret;
    }

    public void saveToDB() {
        if (!this.changed) {
            return;
        }
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
                ps.setInt(1, this.slots);
                ps.setLong(2, this.meso);
                ps.setInt(3, this.storageId);
                ps.executeUpdate();
                ps.close();
            }

            List itemsWithType = new ArrayList();
            for (Item item : this.items) {
                itemsWithType.add(new Pair(item, ItemConstants.getInventoryType(item.getItemId())));
            }
            ItemLoader.仓库道具.saveItems(itemsWithType, this.accountId);
            this.changed = false;
        } catch (SQLException ex) {
            System.err.println("Error saving storage" + ex);
        }
    }

    public Item getItem(byte slot) {
        if ((slot >= this.items.size()) || (slot < 0)) {
            return null;
        }
        return (Item) this.items.get(slot);
    }

    public Item takeOut(byte slot) {
        this.changed = true;
        Item ret = (Item) this.items.remove(slot);
        MapleInventoryType type = ItemConstants.getInventoryType(ret.getItemId());
        this.typeItems.put(type, new ArrayList(filterItems(type)));
        return ret;
    }

    public void store(Item item) {
        this.changed = true;
        this.items.add(item);
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        this.typeItems.put(type, new ArrayList(filterItems(type)));
    }

    public void arrange() {
        Collections.sort(this.items, new Comparator() {
            public int compare(Item o1, Item o2) {
                if (o1.getItemId() < o2.getItemId()) {
                    return -1;
                }
                if (o1.getItemId() == o2.getItemId()) {
                    return 0;
                }
                return 1;
            }

            @Override
            public int compare(Object o1, Object o2) {
                throw new UnsupportedOperationException("");
            }
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            this.typeItems.put(type, new ArrayList(this.items));
        }
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List ret = new LinkedList();
        for (Item item : this.items) {
            if (ItemConstants.getInventoryType(item.getItemId()) == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        byte ret = 0;
        List it = (List) this.typeItems.get(type);
        if ((it == null) || (slot >= it.size()) || (slot < 0)) {
            return -1;
        }
        for (Item item : this.items) {
            if (item == it.get(slot)) {
                return ret;
            }
            ret = (byte) (ret + 1);
        }
        return -1;
    }

    public void sendStorage(MapleClient c, int npcId) {
        try {
            this.storageNpcId = npcId;
//            Collections.sort(this.items, new Comparator() {
//                public int compare(Item o1, Item o2) {
//                    if (ItemConstants.getInventoryType(o1.getItemId()).getType() < ItemConstants.getInventoryType(o2.getItemId()).getType()) {
//                        return -1;
//                    }
//                    if (ItemConstants.getInventoryType(o1.getItemId()) == ItemConstants.getInventoryType(o2.getItemId())) {
//                        return 0;
//                    }
//                    return 1;
//                }
//
//                @Override
//                public int compare(Object o1, Object o2) {
//                    throw new UnsupportedOperationException("");
//                }
//            });
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item o1, Item o2) {
                    if (GameConstants.getInventoryType(o1.getItemId()).getType() < GameConstants.getInventoryType(o2.getItemId()).getType()) {
                        return -1;
                    } else if (GameConstants.getInventoryType(o1.getItemId()) == GameConstants.getInventoryType(o2.getItemId())) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            for (MapleInventoryType type : MapleInventoryType.values()) {
                this.typeItems.put(type, new ArrayList(this.items));
            }
            c.getSession().write(NPCPacket.getStorage(npcId, this.slots, this.items, this.meso));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        c.getSession().write(NPCPacket.storeStorage(this.slots, type, (Collection) this.typeItems.get(type)));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        c.getSession().write(NPCPacket.takeOutStorage(this.slots, type, (Collection) this.typeItems.get(type)));
    }

    public long getMeso() {
        return this.meso;
    }

    public Item findById(int itemId) {
        for (Item item : this.items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public void setMeso(long meso) {
        if (meso < 0) {
            return;
        }
        this.changed = true;
        this.meso = meso;
    }

    public void sendMeso(MapleClient c) {
        c.getSession().write(NPCPacket.mesoStorage(this.slots, this.meso));
    }

    public boolean isFull() {
        return this.items.size() >= this.slots;
    }

    public int getSlots() {
        return this.slots;
    }

    public void increaseSlots(byte gain) {
        this.changed = true;
        this.slots = (byte) (this.slots + gain);
    }

    public void setSlots(byte set) {
        this.changed = true;
        this.slots = set;
    }

    public int getNpcId() {
        return this.storageNpcId;
    }

    public void close() {
        this.typeItems.clear();
    }
}
