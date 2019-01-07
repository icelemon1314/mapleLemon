package client.inventory;

import constants.ItemConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import tools.Pair;

public enum ItemLoader {

    装备道具(0, false), // 在背包的道具
    仓库道具(1, true),
    现金道具(2, true), // 在现金保管箱里面的道具
    雇佣道具(5, false),
    送货道具(6, false),
    拍卖道具(8, false),
    MTS_TRANSFER(9, false);

    private final int value;
    private final boolean account;

    private ItemLoader(int value, boolean account) {
        this.value = value;
        this.account = account;
    }

    public int getValue() {
        return this.value;
    }

    /**
     * 加载道具信息
     * @param login
     * @param id
     * @return
     * @throws SQLException
     */
    public Map<Long, Pair<Item, MapleInventoryType>> loadItems(boolean login, int id) throws SQLException {
        Map items = new LinkedHashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
            query.append(this.account ? "accountid" : "characterid");
            query.append("` = ?");

            if (login) {
                query.append(" AND `inventorytype` = ");
                query.append(MapleInventoryType.EQUIPPED.getType());
            }

            ps = DatabaseConnection.getConnection().prepareStatement(query.toString());
            ps.setInt(1, this.value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            while (rs.next()) {
                if (!ii.itemExists(rs.getInt("itemid"))) {
                    continue;
                }
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                if ((mit.equals(MapleInventoryType.EQUIP)) || (mit.equals(MapleInventoryType.EQUIPPED))) {
                    Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                        equip.setQuantity((byte) 1);
                        equip.setInventoryId(rs.getLong("inventoryitemid"));
                        equip.setOwner(rs.getString("owner"));
                        equip.setExpiration(rs.getLong("expiredate"));
                        equip.setEquipOnlyId(rs.getInt("equipOnlyId"));
                        equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                        equip.setLevel(rs.getByte("level"));
                        equip.setStr(rs.getShort("str"));
                        equip.setDex(rs.getShort("dex"));
                        equip.setInt(rs.getShort("int"));
                        equip.setLuk(rs.getShort("luk"));
                        equip.setHp(rs.getShort("hp"));
                        equip.setMp(rs.getShort("mp"));
                        equip.setWatk(rs.getShort("watk"));
                        equip.setMatk(rs.getShort("matk"));
                        equip.setWdef(rs.getShort("wdef"));
                        equip.setMdef(rs.getShort("mdef"));
                        equip.setAcc(rs.getShort("acc"));
                        equip.setAvoid(rs.getShort("avoid"));
                        equip.setHands(rs.getShort("hands"));
                        equip.setSpeed(rs.getShort("speed"));
                        equip.setJump(rs.getShort("jump"));
                        equip.setGMLog(rs.getString("GM_Log"));
                        equip.setState(rs.getByte("state"));
                        equip.setEnhance(rs.getByte("enhance"));
                        equip.setGiftFrom(rs.getString("sender"));
                        equip.setIncSkill(rs.getInt("incSkill"));
                        equip.setCharmEXP(rs.getShort("charmEXP"));
                        equip.setStateMsg(rs.getInt("statemsg"));
                        equip.setEnhanctBuff(rs.getShort("enhanctBuff"));
                        equip.setReqLevel(rs.getShort("reqLevel"));
                        equip.setYggdrasilWisdom(rs.getShort("yggdrasilWisdom"));
                        equip.setFinalStrike(rs.getShort("finalStrike") > 0);
                        equip.setBossDamage(rs.getShort("bossDamage"));
                        equip.setIgnorePDR(rs.getShort("ignorePDR"));

                        equip.setTotalDamage(rs.getShort("totalDamage"));
                        equip.setAllStat(rs.getShort("allStat"));
                        equip.setKarmaCount(rs.getShort("karmaCount"));

                        if (equip.getCharmEXP() < 0) {
                            equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                        }

                        if ((equip.getBossDamage() <= 0) && (ii.getBossDamageRate(equip.getItemId()) > 0)) {
                            equip.setBossDamage((short) ii.getBossDamageRate(equip.getItemId()));
                        }
                        if ((equip.getIgnorePDR() <= 0) && (ii.getIgnoreMobDmageRate(equip.getItemId()) > 0)) {
                            equip.setIgnorePDR((short) ii.getIgnoreMobDmageRate(equip.getItemId()));
                        }

                        if (equip.getUniqueId() > -1) {
                            if (ItemConstants.isEffectRing(rs.getInt("itemid"))) {
                                MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                if (ring != null) {
                                    equip.setRing(ring);
                                }
                        }
                        if (equip.hasSetOnlyId()) {
                            equip.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
                        }
                    }
                    items.put(rs.getLong("inventoryitemid"), new Pair(equip.copy(), mit));
                } else {
                    Item item = new Item(rs.getInt("itemid"), (byte)rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                    item.setOwner(rs.getString("owner"));
                    item.setInventoryId(rs.getLong("inventoryitemid"));
                    item.setExpiration(rs.getLong("expiredate"));
                    item.setGMLog(rs.getString("GM_Log"));
                    item.setGiftFrom(rs.getString("sender"));
                    if (ItemConstants.isPet(item.getItemId())) {
                        if (item.getUniqueId() > -1) {
                            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                            if (pet != null) {
                                item.setPet(pet);
                            }
                        } else {
                            item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                        }
                    }
                    items.put(rs.getLong("inventoryitemid"), new Pair(item.copy(), mit));
                }
            }
            rs.close();
            ps.close();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
        return items;
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id) throws SQLException {
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        try {
            StringBuilder query = new StringBuilder();
            query.append("DELETE FROM `inventoryitems` WHERE `type` = ? AND `");
            query.append(this.account ? "accountid" : "characterid");
            query.append("` = ?");

            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement(query.toString());
            ps.setInt(1, this.value);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            if (items == null) {
                return;
            }
            ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            pse = con.prepareStatement("INSERT INTO `inventoryequipment` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            for (Pair pair : items) {
                Item item = (Item) pair.getLeft();
                MapleInventoryType mit = (MapleInventoryType) pair.getRight();
                ps.setInt(1, this.value);
                ps.setString(2, this.account ? null : String.valueOf(id));
                ps.setString(3, this.account ? String.valueOf(id) : null);
                ps.setInt(4, item.getItemId());
                ps.setInt(5, mit.getType());
                ps.setInt(6, item.getPosition());
                ps.setInt(7, item.getQuantity());
                ps.setString(8, item.getOwner());
                ps.setString(9, item.getGMLog());
                if (item.getPet() != null) {
                    ps.setInt(10, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                } else {
                    ps.setInt(10, item.getUniqueId());
                }
                ps.setShort(11, item.getFlag());
                ps.setLong(12, item.getExpiration());
                ps.setString(13, item.getGiftFrom());
                ps.setInt(14, item.getEquipOnlyId());
                ps.executeUpdate();

                if ((mit.equals(MapleInventoryType.EQUIP)) || (mit.equals(MapleInventoryType.EQUIPPED))) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("[saveItems] 保存道具失败.");
                        }
                        pse.setLong(1, rs.getLong(1));
                    }

                    Equip equip = (Equip) item;
                    pse.setInt(2, equip.getUpgradeSlots());
                    pse.setInt(3, equip.getLevel());
                    pse.setInt(4, equip.getStr());
                    pse.setInt(5, equip.getDex());
                    pse.setInt(6, equip.getInt());
                    pse.setInt(7, equip.getLuk());
                    pse.setInt(8, equip.getHp());
                    pse.setInt(9, equip.getMp());
                    pse.setInt(10, equip.getWatk());
                    pse.setInt(11, equip.getMatk());
                    pse.setInt(12, equip.getWdef());
                    pse.setInt(13, equip.getMdef());
                    pse.setInt(14, equip.getAcc());
                    pse.setInt(15, equip.getAvoid());
                    pse.setInt(16, equip.getHands());
                    pse.setInt(17, equip.getSpeed());
                    pse.setInt(18, equip.getJump());
                    pse.setInt(19, 0);
                    pse.setInt(20, 0);
                    pse.setInt(21, 0);
                    pse.setByte(22, equip.getState());
                    pse.setByte(23, equip.getEnhance());
                    pse.setInt(24, 0);
                    pse.setInt(25, 0);
                    pse.setInt(26, 0);
                    pse.setInt(27, 0);
                    pse.setInt(28, 0);
                    pse.setInt(29, 0);
                    pse.setInt(30, equip.getIncSkill());
                    pse.setShort(31, equip.getCharmEXP());
                    pse.setShort(32, (short)0);
                    pse.setInt(33, equip.getStateMsg());
                    pse.setInt(34, 0);
                    pse.setInt(35, 0);
                    pse.setInt(36, 0);
                    pse.setInt(37, 0);
                    pse.setInt(38, 0);

                    pse.setInt(39, equip.getEnhanctBuff());
                    pse.setInt(40, equip.getReqLevel());
                    pse.setInt(41, equip.getYggdrasilWisdom());
                    pse.setInt(42, equip.getFinalStrike() ? 1 : 0);
                    pse.setInt(43, equip.getBossDamage());
                    pse.setInt(44, equip.getIgnorePDR());
                    pse.setInt(45, equip.getTotalDamage());
                    pse.setInt(46, equip.getAllStat());
                    pse.setInt(47, equip.getKarmaCount());
                    pse.executeUpdate();
                }
            }
            pse.close();
            ps.close();
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (pse != null) {
                pse.close();
            }
        }
    }
}
