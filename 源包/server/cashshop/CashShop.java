package server.cashshop;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.ItemConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.MTSCSPacket;

public class CashShop implements Serializable {

    private static final long serialVersionUID = 231541893513373579L;
    private final int accountId;
    private final int characterId;
    private final ItemLoader factory = ItemLoader.现金道具;
    private final List<Item> inventory = new ArrayList();
    private final List<Integer> uniqueids = new ArrayList();

    public CashShop(int accountId, int characterId, int jobType) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;
        for (Pair<Item, MapleInventoryType> item : this.factory.loadItems(false, accountId).values()) {
            this.inventory.add(item.getLeft());
        }
    }

    public int getItemsSize() {
        return this.inventory.size();
    }

    public List<Item> getInventory() {
        return this.inventory;
    }

    public Item findByCashId(int cashId) {
        for (Item item : this.inventory) {
            if (item.getUniqueId() == cashId) {
                return item;
            }
        }
        return null;
    }

    public void checkExpire(MapleClient c) {
        List<Item> toberemove = new ArrayList();
        for (Item item : this.inventory) {
            if ((item != null) && (!ItemConstants.isPet(item.getItemId())) && (item.getExpiration() > 0L) && (item.getExpiration() < System.currentTimeMillis())) {
                toberemove.add(item);
            }
        }
        if (toberemove.size() > 0) {
            for (Item item : toberemove) {
                removeFromInventory(item);
                c.getSession().write(MTSCSPacket.cashItemExpired(item.getUniqueId()));
            }
            toberemove.clear();
        }
    }

    public Item toItem(CashItemInfo cItem) {
        return toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), null), "");
    }

    public Item toItem(CashItemInfo cItem, String gift) {
        return toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), null), gift);
    }

    public Item toItem(CashItemInfo cItem, int uniqueid) {
        return toItem(cItem, uniqueid, "");
    }

    public Item toItem(CashItemInfo cItem, int uniqueid, String gift) {
        if (uniqueid <= 0) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        long period = cItem.getPeriod();
        Item ret;
        if (ItemConstants.getInventoryType(cItem.getId()) == MapleInventoryType.EQUIP) {
            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(cItem.getId(), uniqueid);
            if (period > 0L) {
                eq.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }
            eq.setGMLog("商城购买 " + cItem.getSN() + " 时间 " + FileoutputUtil.CurrentReadable_Date());
            eq.setGiftFrom(gift);
            if ((ItemConstants.isEffectRing(cItem.getId())) && (uniqueid > 0)) {
                MapleRing ring = MapleRing.loadFromDb(uniqueid);
                if (ring != null) {
                    eq.setRing(ring);
                }
            }
            ret = eq.copy();
        } else {
            Item item = new Item((cItem.getId()), (byte) 0, (short) cItem.getCount(), (byte) 0, uniqueid);
            if (ItemConstants.isPet(cItem.getId())) {
                period = 90L;
                item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            } else if ((cItem.getId() == 5211047) || (cItem.getId() == 5360014)) {
                item.setExpiration(System.currentTimeMillis() + 10800000L);
            } else if (cItem.getId() == 5211060) {
                item.setExpiration(System.currentTimeMillis() + 7200000L);
            } else if (period > 0L) {
                item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            } else {
                item.setExpiration(-1L);
            }
            item.setGMLog("商城购买 " + cItem.getSN() + " 时间 " + FileoutputUtil.CurrentReadable_Date());
            item.setGiftFrom(gift);
            if (ItemConstants.isPet(cItem.getId())) {
                MaplePet pet = MaplePet.createPet(cItem.getId(), uniqueid);
                if (pet != null) {
                    item.setPet(pet);
                }
            }
            ret = item.copy();
        }
        return ret;
    }

    public void addToInventory(Item item) {
        this.inventory.add(item);
    }

    public void removeFromInventory(Item item) {
        this.inventory.remove(item);
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, 0);
    }

    public void gift(int recipient, String from, String message, int sn, int uniqueid) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, recipient);
                ps.setString(2, from);
                ps.setString(3, message);
                ps.setInt(4, sn);
                ps.setInt(5, uniqueid);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public List<Pair<Item, String>> loadGifts() {
        List gifts = new ArrayList();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `recipient` = ?");
            ps.setInt(1, this.characterId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(rs.getInt("sn"));
                if (cItem == null) {
                    continue;
                }
                Item item = toItem(cItem, rs.getInt("uniqueid"), rs.getString("from"));
                gifts.add(new Pair(item, rs.getString("message")));
                this.uniqueids.add(item.getUniqueId());
                List packages = CashItemFactory.getInstance().getPackageItems(cItem.getId());
                Iterator i$;
                if ((packages != null) && (packages.size() > 0)) {
                    for (i$ = packages.iterator(); i$.hasNext();) {
                        int packageItem = ((Integer) i$.next());
                        CashItemInfo pack = CashItemFactory.getInstance().getSimpleItem(packageItem);
                        if (pack != null) {
                            addToInventory(toItem(pack, rs.getString("from")));
                        }
                    }
                } else {
                    addToInventory(item);
                }
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM `gifts` WHERE `recipient` = ?");
            ps.setInt(1, this.characterId);
            ps.executeUpdate();
            ps.close();
            save();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return gifts;
    }

    public boolean canSendNote(int uniqueid) {
        return this.uniqueids.contains(uniqueid);
    }

    public void sendedNote(int uniqueid) {
        for (int i = 0; i < this.uniqueids.size(); i++) {
            if ((this.uniqueids.get(i)) == uniqueid) {
                this.uniqueids.remove(i);
            }
        }
    }

    public void save() throws SQLException {
        List itemsWithType = new ArrayList();
        for (Item item : this.inventory) {
            itemsWithType.add(new Pair(item, ItemConstants.getInventoryType(item.getItemId())));
        }
        this.factory.saveItems(itemsWithType, this.accountId);
    }
}
