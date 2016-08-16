package server;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.PlayerStats;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.InventoryException;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleEquipOnlyId;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;

public class MapleInventoryManipulator {

    private static final Logger log = Logger.getLogger(MapleInventoryManipulator.class);

    public static void addRing(MapleCharacter chr, int itemId, int ringId, int sn) {
        CashItemInfo csi = CashItemFactory.getInstance().getItem(sn);
        if (csi == null) {
            return;
        }
        Item ring = chr.getCashInventory().toItem(csi, ringId);
        if ((ring == null) || (ring.getUniqueId() != ringId) || (ring.getUniqueId() <= 0) || (ring.getItemId() != itemId)) {
            return;
        }
        chr.getCashInventory().addToInventory(ring);
        chr.getClient().getSession().write(MTSCSPacket.购买商城道具(ring, sn, chr.getClient().getAccID()));
    }

    public static boolean addbyItem(MapleClient c, Item item) {
        return addbyItem(c, item, false) >= 0;
    }

    public static short addbyItem(MapleClient c, Item item, boolean fromcs) {
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        short newSlot = c.getPlayer().getInventory(type).addItem(item);
        if (newSlot == -1) {
            if (!fromcs) {
                c.getSession().write(InventoryPacket.getInventoryFull());
                c.getSession().write(InventoryPacket.getShowInventoryFull());
            }
            return newSlot;
        }
        if (item.hasSetOnlyId()) {
            item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
        }
            c.getSession().write(InventoryPacket.addItemToInventory(item));
        c.getPlayer().havePartyQuest(item.getItemId());
        if ((!fromcs) && (type.equals(MapleInventoryType.EQUIP))) {
            c.getPlayer().checkCopyItems();
        }
        return newSlot;
    }

    public static int getUniqueId(int itemId, MaplePet pet) {
        int uniqueid = -1;
        if (ItemConstants.isPet(itemId)) {
            if (pet != null) {
                uniqueid = pet.getUniqueId();
            } else {
                uniqueid = MapleInventoryIdentifier.getInstance();
            }
        } else if ((ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH) || (MapleItemInformationProvider.getInstance().isCash(itemId))) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        return uniqueid;
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String gmLog) {
        return addById(c, itemId, quantity, null, null, 0L, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, int state, String gmLog) {
        return addById(c, itemId, quantity, null, null, 0L, state, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, long period, String gmLog) {
        return addById(c, itemId, quantity, null, null, period, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, long period, int state, String gmLog) {
        return addById(c, itemId, quantity, null, null, period, state, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addById(c, itemId, quantity, owner, null, 0L, 0, gmLog);
    }

    public static byte addId(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addId(c, itemId, quantity, owner, null, 0L, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, 0L, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, period, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, int state, String gmLog) {
        return addId(c, itemId, quantity, owner, pet, period, state, gmLog) >= 0;
    }

    /**
     * 通过ID来添加道具
     * @param c
     * @param itemId
     * @param quantity
     * @param owner
     * @param pet
     * @param period
     * @param state
     * @param gmLog
     * @return
     */
    public static byte addId(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, int state, String gmLog) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (((ii.isPickupRestricted(itemId)) && (c.getPlayer().haveItem(itemId, 1, true, false))) || (!ii.itemExists(itemId))) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return -1;
        }
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        int uniqueid = getUniqueId(itemId, pet);
        short newSlot = -1;
        if (!type.equals(MapleInventoryType.EQUIP)) { // 非装备
            short slotMax = ii.getSlotMax(itemId);
            List existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ItemConstants.isRechargable(itemId)) { // 非冲值道具
                if (existing.size() > 0) {
                    Iterator i = existing.iterator();
                    while ((quantity > 0) && (i.hasNext())) {
                        Item eItem = (Item) i.next();
                        short oldQ = eItem.getQuantity();
                        if ((oldQ < slotMax) && ((eItem.getOwner().equals(owner)) || (owner == null)) && (eItem.getExpiration() == -1L)) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity = (short) (quantity - (newQ - oldQ));
                            eItem.setQuantity(newQ);
                            c.getSession().write(InventoryPacket.updateInventorySlot(type, eItem));
                            newSlot = eItem.getPosition();
                        }
                    }
                }
                Item nItem;
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity = (short) (quantity - newQ);
                        nItem = new Item(itemId, (byte) 0, newQ, (byte) 0, uniqueid);
                        newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1) {
                            c.getSession().write(InventoryPacket.getInventoryFull());
                            c.getSession().write(InventoryPacket.getShowInventoryFull());
                            return -1;
                        }
                        if (gmLog != null) {
                            nItem.setGMLog(gmLog);
                        }
                        if (owner != null) {
                            nItem.setOwner(owner);
                        }
                        if (period > 0L) {
                            if (period < 1000L) {
                                nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                            } else {
                                nItem.setExpiration(System.currentTimeMillis() + period);
                            }
                        }
                        if (pet != null) {
                            nItem.setPet(pet);
                            pet.setInventoryPosition(newSlot);
                        }
                        //c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
                        FileoutputUtil.log("添加新道具到背包："+nItem.getItemId());
                        c.getSession().write(InventoryPacket.addItemToInventory(nItem));

                        if ((ItemConstants.isRechargable(itemId)) && (quantity == 0)) {
                            break;
                        }
                    } else {
                        c.getPlayer().havePartyQuest(itemId);
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return (byte) newSlot;
                    }
                }
            } else {
                Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0, uniqueid);
                newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return -1;
                }
                if (period > 0L) {
                    if (period < 1000L) {
                        nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    } else {
                        nItem.setExpiration(System.currentTimeMillis() + period);
                    }
                }
                if (gmLog != null) {
                    nItem.setGMLog(gmLog);
                }
                FileoutputUtil.log("添加新道具到背包2："+nItem.getItemId());
                c.getSession().write(InventoryPacket.addItemToInventory(nItem));
                c.getSession().write(MaplePacketCreator.enableActions());
            }

        } else if (quantity == 1) { // 装备
            Item nEquip = ii.getEquipById(itemId, uniqueid);
            if (owner != null) {
                nEquip.setOwner(owner);
            }
            if (gmLog != null) {
                nEquip.setGMLog(gmLog);
            }
            if (period > 0L) {
                if (period < 1000L) {
                    nEquip.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                } else {
                    nEquip.setExpiration(System.currentTimeMillis() + period);
                }
            }
            if (nEquip.hasSetOnlyId()) {
                nEquip.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
            if (newSlot == -1) {
                c.getSession().write(InventoryPacket.getInventoryFull());
                c.getSession().write(InventoryPacket.getShowInventoryFull());
                return -1;
            }
            FileoutputUtil.log("添加新装备到背包1："+nEquip.getItemId());
            c.getSession().write(InventoryPacket.addItemToInventory(nEquip));
            c.getPlayer().checkCopyItems();
        } else {
            throw new InventoryException("Trying to create equip with non-one quantity");
        }

        c.getPlayer().havePartyQuest(itemId);
        return (byte) newSlot;
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, short quantity) {
        return addbyId_Gachapon(c, itemId, quantity, null, 0L);
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, short quantity, String gmLog) {
        return addbyId_Gachapon(c, itemId, quantity, null, 0L);
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, short quantity, String gmLog, long period) {
        if ((c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1) || (c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1) || (c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1) || (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1)) {
            return null;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || !ii.itemExists(itemId)) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return null;
        }
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(itemId);
            List existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ItemConstants.isRechargable(itemId)) {
                Item nItem = null;
                boolean recieved = false;
                if (existing.size() > 0) {
                    Iterator i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        nItem = (Item) i.next();
                        short oldQ = nItem.getQuantity();
                        if (oldQ < slotMax) {
                            recieved = true;
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity = (short) (quantity - (newQ - oldQ));
                            nItem.setQuantity(newQ);
                            c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, nItem))));
                        }
                    }
                }
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ == 0) {
                        break;
                    }
                    quantity = (short) (quantity - newQ);
                    nItem = new Item(itemId, (byte) 0, newQ, (byte) 0);
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1 && recieved) {
                        return nItem;
                    }
                    if (newSlot == -1) {
                        return null;
                    }
                    recieved = true;
                    if (gmLog != null) {
                        nItem.setGMLog(gmLog);
                    }
                    if (period > 0L) {
                        if (period < 1000L) {
                            nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                        } else {
                            nItem.setExpiration(System.currentTimeMillis() + period);
                        }
                    }
                    c.getSession().write(InventoryPacket.addItemToInventory(nItem));
                    if ((ItemConstants.isRechargable(itemId)) && (quantity == 0)) {
                        break;
                    }
                }
                if (recieved) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
            } else {
                Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0);
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    return null;
                }
                if (gmLog != null) {
                    nItem.setGMLog(gmLog);
                }
                if (period > 0L) {
                    if (period < 1000L) {
                        nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    } else {
                        nItem.setExpiration(System.currentTimeMillis() + period);
                    }
                }
                c.getSession().write(InventoryPacket.addItemToInventory(nItem));
                c.getPlayer().havePartyQuest(nItem.getItemId());
                return nItem;
            }
        } else {
            if (quantity == 1) {
                Item nEquip = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                short newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                if (newSlot == -1) {
                    return null;
                }
                if (gmLog != null) {
                    nEquip.setGMLog(gmLog);
                }
                if (period > 0L) {
                    if (period < 1000L) {
                        nEquip.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    } else {
                        nEquip.setExpiration(System.currentTimeMillis() + period);
                    }
                }
                if (nEquip.hasSetOnlyId()) {
                    nEquip.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
                }
                c.getSession().write(InventoryPacket.addItemToInventory(nEquip));
                c.getPlayer().havePartyQuest(nEquip.getItemId());
                return nEquip;
            }
            throw new InventoryException("Trying to create equip with non-one quantity");
        }

        return null;
    }

    /**
     * 拾取道具
     * @param c
     * @param item
     * @param show
     * @return
     */
    public static boolean addFromDrop(MapleClient c, Item item, boolean show) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((c.getPlayer() == null) || ((ii.isPickupRestricted(item.getItemId())) && (c.getPlayer().haveItem(item.getItemId(), 1, true, false))) || (!ii.itemExists(item.getItemId()))) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            c.getSession().write(InventoryPacket.showItemUnavailable());
            return false;
        }
        int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        if (!type.equals(MapleInventoryType.EQUIP)) { // 不是装备
            short slotMax = ii.getSlotMax(item.getItemId());
            List existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!ItemConstants.isRechargable(item.getItemId())) { // 不是飞镖类
                if (quantity <= 0) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.showItemUnavailable());
                    return false;
                }
                if (existing.size() > 0) {
                    Iterator i = existing.iterator();
                    while ((quantity > 0) && (i.hasNext())) {
                        Item eItem = (Item) i.next();
                        short oldQ = eItem.getQuantity();
                        if ((oldQ < slotMax) && (item.getOwner().equals(eItem.getOwner())) && (item.getExpiration() == eItem.getExpiration())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity = (short) (quantity - (newQ - oldQ));
                            eItem.setQuantity(newQ);
                            c.getSession().write(InventoryPacket.addItemToInventory(eItem));
                        }
                    }
                }
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    quantity = (short) (quantity - newQ);
                    Item nItem = new Item(item.getItemId(), (byte) 0, newQ, item.getFlag());
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setPet(item.getPet());
                    nItem.setGMLog(item.getGMLog());
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    c.getSession().write(InventoryPacket.addItemToInventory(nItem));
                }
            } else {
                // 飞镖
                Item nItem = new Item(item.getItemId(), (byte) 0, quantity, item.getFlag());
                nItem.setExpiration(item.getExpiration());
                nItem.setOwner(item.getOwner());
                nItem.setPet(item.getPet());
                nItem.setGMLog(item.getGMLog());
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    c.getSession().write(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(InventoryPacket.addItemToInventory(nItem));
                c.getSession().write(MaplePacketCreator.enableActions());
            }

        } else if (quantity == 1) { // 装备
            if (item.hasSetOnlyId()) {
                item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            short newSlot = c.getPlayer().getInventory(type).addItem(item);
            if (newSlot == -1) {
                c.getSession().write(InventoryPacket.getInventoryFull());
                c.getSession().write(InventoryPacket.getShowInventoryFull());
                return false;
            }
            c.getSession().write(InventoryPacket.addItemToInventory(item));
            c.getPlayer().checkCopyItems();
        } else {
            throw new RuntimeException(new StringBuilder().append("玩家[").append(c.getPlayer().getName()).append("] 获得装备但装备的数量不为1 装备ID: ").append(item.getItemId()).toString());
        }

        c.getPlayer().havePartyQuest(item.getItemId());
        if (show) {
            c.getSession().write(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
        }
        return true;
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot) {
        return addItemAndEquip(c, itemId, slot, 0);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, boolean removeItem) {
        return addItemAndEquip(c, itemId, slot, 0, removeItem);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state) {
        return addItemAndEquip(c, itemId, slot, state, true);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state, boolean removeItem) {
        return addItemAndEquip(c, itemId, slot, null, 0L, state, new StringBuilder().append("系统赠送 时间: ").append(FileoutputUtil.CurrentReadable_Date()).toString(), removeItem);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state, String gmLog) {
        return addItemAndEquip(c, itemId, slot, null, 0L, state, gmLog, true);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, String owner, long period, int state, String gmLog, boolean removeItem) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if ((!ii.itemExists(itemId)) || (slot > 0) || (!type.equals(MapleInventoryType.EQUIP))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        Item toRemove = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        if (toRemove != null) {
            if (removeItem) {
                removeFromSlot(c, MapleInventoryType.EQUIPPED, toRemove.getPosition(), toRemove.getQuantity(), false);
            } else {
                short nextSlot = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                if (nextSlot > -1) {
                    unequip(c, toRemove.getPosition(), nextSlot);
                }
            }
        }
        Item nEquip = ii.getEquipById(itemId);
        if (owner != null) {
            nEquip.setOwner(owner);
        }
        if (gmLog != null) {
            nEquip.setGMLog(gmLog);
        }
        if (period > 0L) {
            if (period < 1000L) {
                nEquip.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            } else {
                nEquip.setExpiration(System.currentTimeMillis() + period);
            }
        }
        if (nEquip.hasSetOnlyId()) {
            nEquip.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
        }
        nEquip.setPosition((byte)slot);
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(nEquip);
        c.getSession().write(InventoryPacket.addItemToInventory(nEquip));
        return true;
    }

    public static boolean checkSpace(MapleClient c, int itemid, int quantity, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((c.getPlayer() == null) || ((ii.isPickupRestricted(itemid)) && (c.getPlayer().haveItem(itemid, 1, true, false))) || (!ii.itemExists(itemid))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        if ((quantity <= 0) && (!ItemConstants.isRechargable(itemid))) {
            return false;
        }
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        if ((c.getPlayer() == null) || (c.getPlayer().getInventory(type) == null)) {
            return false;
        }
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(itemid);
            List<Item> existing = c.getPlayer().getInventory(type).listById(itemid);
            if ((!ItemConstants.isRechargable(itemid))
                    && (existing.size() > 0)) {
                for (Item eItem : existing) {
                    short oldQ = eItem.getQuantity();
                    if ((oldQ < slotMax) && (owner != null) && (owner.equals(eItem.getOwner()))) {
                        short newQ = (short) Math.min(oldQ + quantity, slotMax);
                        quantity -= newQ - oldQ;
                    }
                    if (quantity <= 0) {
                        break;
                    }
                }
            }
            int numSlotsNeeded;
            if ((slotMax > 0) && (!ItemConstants.isRechargable(itemid))) {
                numSlotsNeeded = (int) Math.ceil((double) quantity / slotMax);
            } else {
                numSlotsNeeded = 1;
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        }
        return !c.getPlayer().getInventory(type).isFull();
    }

    public static boolean removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop) {
        return removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static boolean removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop, boolean consume) {
        if ((c.getPlayer() == null) || (c.getPlayer().getInventory(type) == null)) {
            return false;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            if (((item.getItemId() == 5370000) || (item.getItemId() == 5370001)) && (c.getPlayer().getChalkboard() != null)) {
                c.getPlayer().setChalkboard(null);
            }
            boolean allowZero = (consume) && (ItemConstants.isRechargable(item.getItemId()));
            c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
            if ((item.getQuantity() == 0) && (!allowZero)) {
                c.getSession().write(InventoryPacket.clearInventoryItem(type,item.getPosition(),fromDrop));
            } else {
                c.getSession().write(InventoryPacket.updateInventorySlot(type,item,fromDrop));
            }
            return true;
        }
        return false;
    }

    public static boolean removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consume) {
        int remremove = quantity;
        if ((c.getPlayer() == null) || (c.getPlayer().getInventory(type) == null)) {
            return false;
        }
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            int theQ = item.getQuantity();
            if ((remremove <= theQ) && (removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume))) {
                remremove = 0;
                break;
            }
            if ((remremove > theQ) && (removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume))) {
                remremove -= theQ;
            }
        }
        return remremove <= 0;
    }

    public static boolean removeFromSlot_Lock(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop, boolean consume) {
        if ((c.getPlayer() == null) || (c.getPlayer().getInventory(type) == null)) {
            return false;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            if ((ItemFlag.封印.check(item.getFlag())) || (ItemFlag.不可交易.check(item.getFlag()))) {
                return false;
            }
            return removeFromSlot(c, type, slot, quantity, fromDrop, consume);
        }
        return false;
    }

    public static boolean removeById_Lock(MapleClient c, MapleInventoryType type, int itemId) {
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (removeFromSlot_Lock(c, type, item.getPosition(), (short) 1, false, false)) {
                return true;
            }
        }
        return false;
    }

    public static void removeAllById(MapleClient c, int itemId, boolean checkEquipped) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (item != null) {
                removeFromSlot(c, type, item.getPosition(), item.getQuantity(), true, false);
            }
        }
        if (checkEquipped) {
            Item ii = c.getPlayer().getInventory(type).findById(itemId);
            if (ii != null) {
                c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(ii.getPosition());
                c.getPlayer().equipChanged();
            }
        }
    }

    public static void removeAllByEquipOnlyId(MapleClient c, int equipOnlyId) {
        if (c.getPlayer() == null) {
            return;
        }
        boolean locked = false;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        List<Item> copyEquipItems = c.getPlayer().getInventory(MapleInventoryType.EQUIP).listByEquipOnlyId(equipOnlyId);
        for (Item item : copyEquipItems) {
            if (item != null) {
                if (!locked) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.封印.getValue());
                    flag = (short) (flag | ItemFlag.不可交易.getValue());
                    item.setFlag(flag);
                    item.setOwner("复制装备");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, new StringBuilder().append("在背包中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其锁定。").toString());
                    String msgtext = new StringBuilder().append("玩家 ").append(c.getPlayer().getName()).append(" ID: ").append(c.getPlayer().getId()).append(" (等级 ").append(c.getPlayer().getLevel()).append(") 地图: ").append(c.getPlayer().getMapId()).append(" 在玩家背包中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其锁定。").toString();
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append("[GM 信息] ").append(msgtext).toString()));
                    FileoutputUtil.log(FileoutputUtil.复制装备, new StringBuilder().append(msgtext).append(" 道具唯一ID: ").append(item.getEquipOnlyId()).toString());
                    locked = true;
                } else {
                    removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, new StringBuilder().append("在背包中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其删除。").toString());
                }
            }
        }

        List<Item> copyEquipedItems = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).listByEquipOnlyId(equipOnlyId);
        for (Item item : copyEquipedItems) {
            if (item != null) {
                if (!locked) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.封印.getValue());
                    flag = (short) (flag | ItemFlag.不可交易.getValue());
                    item.setFlag(flag);
                    item.setOwner("复制装备");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, new StringBuilder().append("在穿戴中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其锁定。").toString());
                    String msgtext = new StringBuilder().append("玩家 ").append(c.getPlayer().getName()).append(" ID: ").append(c.getPlayer().getId()).append(" (等级 ").append(c.getPlayer().getLevel()).append(") 地图: ").append(c.getPlayer().getMapId()).append(" 在玩家穿戴中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其锁定。").toString();
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append("[GM 信息] ").append(msgtext).toString()));
                    FileoutputUtil.log(FileoutputUtil.复制装备, new StringBuilder().append(msgtext).append(" 道具唯一ID: ").append(item.getEquipOnlyId()).toString());
                    locked = true;
                } else {
                    removeFromSlot(c, MapleInventoryType.EQUIPPED, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, new StringBuilder().append("在穿戴中发现复制装备[").append(ii.getName(item.getItemId())).append("]已经将其删除。").toString());
                    c.getPlayer().equipChanged();
                }
            }
        }
    }

    public static void move(MapleClient c, MapleInventoryType type, short src, short dst) {
        if ((src < 0) || (dst < 0) || (src == dst) || (type == MapleInventoryType.EQUIPPED)) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item source = c.getPlayer().getInventory(type).getItem(src);
        Item initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            c.getPlayer().dropMessage(1, "移动道具失败，找不到移动道具的信息。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        boolean bag = false;
        boolean switchSrcDst = false;
        boolean bothBag = false;
        short eqIndicator = -1;
        List mods = new ArrayList();
        if ((src > c.getPlayer().getInventory(type).getSlotLimit()) && (type == MapleInventoryType.ETC) && (src > 100) && (src % 100 != 0)) {
            if (!bag) {
                switchSrcDst = true;
                eqIndicator = 0;
                bag = true;
            } else {
                bothBag = true;
            }
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        short oldsrcQ = source.getQuantity();
        short slotMax = ii.getSlotMax(source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        if ((!type.equals(MapleInventoryType.EQUIP)) && (initialTarget != null) && (initialTarget.getItemId() == source.getItemId()) && (initialTarget.getOwner().equals(source.getOwner())) && (initialTarget.getExpiration() == source.getExpiration()) && (!ItemConstants.isRechargable(source.getItemId())) && (!type.equals(MapleInventoryType.CASH))) {
            if (olddstQ + oldsrcQ > slotMax) {
                mods.add(new ModifyInventory((bag) && ((switchSrcDst) || (bothBag)) ? 6 : 1, source));
                mods.add(new ModifyInventory((bag) && ((switchSrcDst) || (bothBag)) ? 6 : 1, initialTarget));
            } else {
                mods.add(new ModifyInventory((bag) && ((switchSrcDst) || (bothBag)) ? 7 : 3, source));
                mods.add(new ModifyInventory((bag) && ((!switchSrcDst) || (bothBag)) ? 6 : 1, initialTarget));
            }
        } else {
            mods.add(new ModifyInventory(bag ? 5 : bothBag ? 8 : 2, source, src, eqIndicator, switchSrcDst));
        }

        c.getSession().write(InventoryPacket.modifyInventory(true, mods));
    }

    /**
     * 穿装备
     * @param c
     * @param src
     * @param dst
     */
    public static void equip(MapleClient c, short src, short dst) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        PlayerStats statst = chr.getStat();
        Equip source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src);
        if ((source == null) || (ItemConstants.isHarvesting(source.getItemId()))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (chr.isShowPacket()) {
            chr.dropMessage(5, new StringBuilder().append("穿戴装备  ").append(source.getItemId()).append(" src: ").append(src).append(" dst: ").append(dst).toString());
        }

        if (((source.getItemId() == 1003142) || (source.getItemId() == 1002140) || (source.getItemId() == 1042003) || (source.getItemId() == 1062007) || (source.getItemId() == 1322013) || (source.getItemId() == 1003824))
                && (!chr.isIntern())) {
            chr.dropMessage(1, "无法佩带此物品");
            FileoutputUtil.log(new StringBuilder().append("[作弊] 非管理员玩家: ").append(chr.getName()).append(" 非法穿戴GM装备 ").append(source.getItemId()).toString());
            removeById(c, MapleInventoryType.EQUIP, source.getItemId(), 1, true, false);
            AutobanManager.getInstance().autoban(chr.getClient(), "非法穿戴GM装备。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }

        if (GameConstants.isOverPoweredEquip(c, source.getItemId(), src) && !c.getPlayer().isStaff()) {
            c.getPlayer().dropMessage(1, "这件装备的能量看起来太过于强大，如果你觉得是系统错误请报告给管理员。");
            //c.getPlayer().removeAll(source.getItemId(), false); //清除作弊装备,可能判断系统有误
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }

        Map stats = ii.getEquipStats(source.getItemId());
        if (stats == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (((dst < -5003) || ((dst >= -999) && (dst < -99))) && (!stats.containsKey("cash"))) {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, new StringBuilder().append("穿戴装备 - 2 ").append(source.getItemId()).append(" dst: ").append(dst).append(" 检测1: ").append(dst <= -1200).append(" 检测2: ").append((dst >= -999) && (dst < -99)).append(" 检测3: ").append(!stats.containsKey("cash")).toString());
            }
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((dst <= -1300) && (dst > -1306) ) {
            if (chr.isShowPacket()) {
                chr.dropMessage(5, new StringBuilder().append("穿戴装备 - 4 ").append(source.getItemId()).append(" dst: ").append(dst).append(" 检测1: ").append((dst <= -1300) && (dst > -1306)).append(" 检测2: ").toString());
            }
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!ii.canEquip(stats, source.getItemId(), chr.getLevel(), chr.getJob(), chr.getFame(), statst.getTotalStr(), statst.getTotalDex(), statst.getTotalLuk(), statst.getTotalInt(), chr.getStat().levelBonus)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((ItemConstants.isWeapon(source.getItemId())) && (dst != -10) && (dst != -11)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((dst == -23) && (!GameConstants.isMountItemAvailable(source.getItemId(), chr.getJob()))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((dst == -118) && (source.getItemId() / 10000 != 190)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if ((dst == -119) && (source.getItemId() / 10000 != 191)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        StructExclusiveEquip exclusive;
        List theList;
        if (ii.isExclusiveEquip(source.getItemId())) {
            exclusive = ii.getExclusiveEquipInfo(source.getItemId());
            if (exclusive != null) {
                theList = chr.getInventory(MapleInventoryType.EQUIPPED).listIds();
                for (Integer i : exclusive.itemIDs) {
                    if (theList.contains(i)) {
                        chr.dropMessage(1, exclusive.msg);
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                }
            }
        }

        short takeOff = 0;
        switch (dst) {
            case -6: { // unequip the overall
                Item top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -5);
                if ((top == null) || (!ItemConstants.isOverall(top.getItemId()))) {
                    break;
                }
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull(-1)) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    return;
                }
                takeOff = dst;
                dst = -5;
                break;
            }
            case -5: { // unequip the bottom and top
                final Item top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
                final Item bottom = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
                if (bottom != null && ItemConstants.isOverall(source.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull(-2)) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        return;
                    }
                    if (top == null) {
                        takeOff = dst;
                        dst = -6;
                    } else {
                        unequip(c, (byte) -6, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                }
                break;
            }
            case -10: { // check if weapon is two-handed
                Item shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
                Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
               if (weapon != null && ItemConstants.isTwoHanded(weapon.getItemId(), chr.getJob()) && !ItemConstants.isSpecialShield(source.getItemId())) {
                    if (chr.getInventory(MapleInventoryType.EQUIP).isFull(-1)) {
                        c.getSession().write(InventoryPacket.getInventoryFull());
                        c.getSession().write(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                    if (shield == null) {
                        takeOff = dst;
                        dst = -11;
                    } else {
                        unequip(c, (byte) -11, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                }
                break;
            }
            case -11: {
                Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
                Item shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
                if ((shield == null) || (!ItemConstants.isTwoHanded(source.getItemId(), chr.getJob())) || ItemConstants.isSpecialShield(shield.getItemId())) {
                    break;
                }
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(InventoryPacket.getInventoryFull());
                    return;
                }
                if (weapon == null) {
                    takeOff = dst;
                    dst = -10;
                } else {
                    unequip(c, (byte) -10, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                }
                break;
            }
            case -9:
            case -8:
            case -7:
        }
        source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src);
        Equip target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        if (source == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        short flag = source.getFlag();
        boolean itemChanged = false;
        if ((stats.get("equipTradeBlock") != null || source.getItemId() / 10000 == 167) && !ItemFlag.不可交易.check(flag)) {
            flag = (short) (flag | ItemFlag.不可交易.getValue());
            source.setFlag(flag);
            itemChanged = true;
        }

        chr.getInventory(MapleInventoryType.EQUIP).removeSlot(src);
        if (target != null) {
            chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
        }

        List mods = new ArrayList();
        if (itemChanged) {
            mods.add(new ModifyInventory(3, source));
            mods.add(new ModifyInventory(0, source));
        }
        source.setPosition(takeOff == 0 ? (byte)dst : (byte)takeOff);
        chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
        if (target != null) {
            target.setPosition((byte)src);
            chr.getInventory(MapleInventoryType.EQUIP).addFromDB(target);
        }
        if (ItemConstants.isWeapon(source.getItemId())) {
//            chr.cancelEffectFromBuffStat(MapleBuffStat.攻击加速);
//            chr.cancelEffectFromBuffStat(MapleBuffStat.暗器伤人);
//            chr.cancelEffectFromBuffStat(MapleBuffStat.无形箭弩);
//            chr.cancelEffectFromBuffStat(MapleBuffStat.属性攻击);
        }
        if (source.getItemId() == 1122017) {
            chr.startFairySchedule(true, true);
        }
        mods.add(new ModifyInventory(2, source, src));
        if (takeOff != 0) {
            mods.add(new ModifyInventory(2, target, dst));
        }
        //c.getSession().write(InventoryPacket.modifyInventory(true, mods));
        c.getSession().write(InventoryPacket.moveInventoryItem(ItemConstants.getInventoryType(source.getItemId()),(byte)src,(byte)dst,(byte)3));
        chr.equipChanged();
    }

    /**
     * 脱下装备
     * @param c
     * @param src
     * @param dst
     */
    public static void unequip(MapleClient c, short src, short dst) {
        if (c.getPlayer() == null) {
            return;
        }
        Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
        Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
        if ((dst < 0) || (source == null)) {
            return;
        }
        if ((target != null) && (src <= 0)) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            return;
        }
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
        if (target != null) {
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
        }
        source.setPosition((byte)dst);
        c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
        if (target != null) {
            target.setPosition((byte)src);
            c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
        }
        if (ItemConstants.isWeapon(source.getItemId())) {
//            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.攻击加速);
//            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.暗器伤人);
//            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.无形箭弩);
//            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.属性攻击);
        }else if (source.getItemId() == 1122017) {
//            c.getPlayer().cancelFairySchedule(true);
        }
        //c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(2, source, src))));
        c.getSession().write(InventoryPacket.moveInventoryItem(ItemConstants.getInventoryType(source.getItemId()),(byte)src,(byte)dst,(byte)4));
        c.getPlayer().equipChanged();
    }

    public static boolean drop(MapleClient c, MapleInventoryType type, short src, short quantity) {
        return drop(c, type, src, quantity, false);
    }

    /**
     * 丢弃东西
     * @param c
     * @param type
     * @param src
     * @param quantity
     * @param npcInduced
     * @return
     */
    public static boolean drop(MapleClient c, MapleInventoryType type, short src, short quantity, boolean npcInduced) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
            return false;
        }
        Item source = c.getPlayer().getInventory(type).getItem(src);
        if ((quantity < 0) || (source == null) || ((!npcInduced) && (ItemConstants.isPet(source.getItemId()))) || ((quantity == 0) && (!ItemConstants.isRechargable(source.getItemId())))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }

        if ((!npcInduced) && (source.getItemId() == 4000463)) {
            c.getPlayer().dropMessage(1, "该道具无法丢弃。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }

        short flag = source.getFlag();
        if (quantity > source.getQuantity() && !ItemConstants.isRechargable(source.getItemId())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        if ((ItemFlag.封印.check(flag)) || ((quantity != 1) && (type == MapleInventoryType.EQUIP))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        Point dropPos = new Point(c.getPlayer().getPosition());
        if (quantity < source.getQuantity() && !ItemConstants.isRechargable(source.getItemId())) {
            Item target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, source,src))));

            if (ItemConstants.isPet(source.getItemId()) || ItemFlag.不可交易.check(flag) || ii.isDropRestricted(target.getItemId()) || ii.isAccountShared(target.getItemId())) {
                if (ItemFlag.KARMA_USE.check(flag)) {
                    target.setFlag((short) (byte) (flag - ItemFlag.KARMA_USE.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                }
            } else {
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
            }
        } else {
            c.getPlayer().getInventory(type).removeSlot(src);
            c.getSession().write(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, source,src))));
            if (src < 0) {
                c.getPlayer().equipChanged();
            }

            if (ItemConstants.isPet(source.getItemId()) || ItemFlag.不可交易.check(flag) || ii.isDropRestricted(source.getItemId()) || ii.isAccountShared(source.getItemId())) {
                if (ItemFlag.KARMA_USE.check(flag)) {
                    source.setFlag((short) (byte) (flag - ItemFlag.KARMA_USE.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                } else {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
                }
            } else {
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
            }
        }

        return true;
    }
}
