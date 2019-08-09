package server.shop;

import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import database.dao.ShopDao;
import database.dao.ShopItemDao;
import database.entity.ShopItemPo;
import database.entity.ShopPo;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.InventoryPacket;
import tools.packet.NPCPacket;

public class MapleShop {

    private static final Set<Integer> blockedItems = new LinkedHashSet();
    private static final Set<Integer> rechargeableItems = new LinkedHashSet();
    private final int id;
    private final int npcId;
    private int shopItemId;
    private final List<MapleShopItem> items;

    private MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        this.shopItemId = 0;
        this.items = new ArrayList();
    }

    public void addItem(MapleShopItem item) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.itemExists(item.getItemId())) {
            this.items.add(item);
        }
    }

    public List<MapleShopItem> getItems(MapleClient c) {
        List itemsPlusRebuy = new ArrayList(this.items);
        return itemsPlusRebuy;
    }

    public void sendShop(MapleClient c) {
        c.getPlayer().setShop(this);
        c.sendPacket(NPCPacket.getNPCShop(getNpcId(), this, c));
    }

    public void sendShop(MapleClient c, int customNpc) {
        c.getPlayer().setShop(this);
        c.sendPacket(NPCPacket.getNPCShop(customNpc, this, c));
    }

    public void sendItemShop(MapleClient c, int itemId) {
        this.shopItemId = itemId;
        c.getPlayer().setShop(this);
        c.sendPacket(NPCPacket.getNPCShop(getNpcId(), this, c));
    }

    /**
     * 从商店购买道具
     * @param c
     * @param itemId
     * @param quantity
     * @param position
     */
    public void buy(MapleClient c, int itemId, short quantity, short position) {
        if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
            return;
        }
        if (quantity <= 0) {
            AutobanManager.getInstance().addPoints(c, 1000, 0L, "购买道具数量: " + quantity + " 道具: " + itemId);
            return;
        }
        if (itemId == 4000463) {
            AutobanManager.getInstance().addPoints(c, 1000, 0L, "商店非法购买道具: " + itemId + " 数量: " + quantity);
            return;
        }
        if ((itemId / 10000 == 190) && (!GameConstants.isMountItemAvailable(itemId, c.getPlayer().getJob()))) {
            c.getPlayer().dropMessage(1, "您无法够买这个道具。");
            c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.购买道具完成, this, c, -1));
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleShopItem shopItem = findBySlotAndId(c, itemId, position);

        MapleLogger.info("购买商店道具！");
        if ((shopItem != null) && (shopItem.getPrice() > 0)) {
            MapleLogger.info("购买商店道具！");
            int price = ItemConstants.isRechargable(itemId) ? shopItem.getPrice() : shopItem.getPrice() * quantity;
            if ((price >= 0) && (c.getPlayer().getMeso() >= price)) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    c.getPlayer().gainMeso(-price, false);
                    if (ItemConstants.isPet(itemId)) { // 宠物
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1L, "Bought from shop " + this.id + ", " + this.npcId + " on " + System.currentTimeMillis());
                    } else if (!ItemConstants.isRechargable(itemId)) { // 可冲值道具
                        int state = shopItem.getState();
                        long period = shopItem.getPeriod();
                        MapleInventoryManipulator.addById(c, itemId, quantity, period, state, "商店购买 " + this.id + ", " + this.npcId + " 时间 " + System.currentTimeMillis());
                    } else {
                        quantity = ii.getSlotMax(shopItem.getItemId());
                        MapleInventoryManipulator.addById(c, itemId, quantity, "商店购买 " + this.id + ", " + this.npcId + " 时间 " + System.currentTimeMillis());
                    }
                } else {
                    c.getPlayer().dropMessage(1, "您的背包是满的，请整理下背包。");
                }
                c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.购买道具完成, this, c, -1));
            } else {
                c.getPlayer().dropMessage(1, "钱不够了啊！");
            }
        }
    }

    /**
     * 出售道具
     * @param c
     * @param type
     * @param slot
     * @param quantity
     */
    public void sell(MapleClient c, MapleInventoryType type, Short slot, short quantity) {
        if (quantity == 65535 || quantity <= 0) {
            quantity = 1;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            MapleLogger.info("该位置上无道具："+slot);
            return;
        }
        if (ItemConstants.is飞镖道具(item.getItemId()) || ItemConstants.is子弹道具(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (item.getItemId() == 4000463) {
            c.getPlayer().dropMessage(1, "该道具无法卖出。");
            c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.卖出道具完成, this, c, -1));
            return;
        }

        short iQuant = item.getQuantity();
        if (iQuant == 65535) {
            iQuant = 1;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.cantSell(item.getItemId()) || ItemConstants.isPet(item.getItemId())) {
            c.getPlayer().dropMessage(1, "该道具无法卖出1。");
            c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.卖出道具完成, this, c, -1));
            return;
        }
        if (quantity <= iQuant && (iQuant > 0 || ItemConstants.is飞镖道具(item.getItemId()) || ItemConstants.is子弹道具(item.getItemId()))) {
            MapleInventoryManipulator.removeFromSlot(c, type, (short) slot, quantity, false);
            double price;
            if (ItemConstants.is飞镖道具(item.getItemId()) || ItemConstants.is子弹道具(item.getItemId())) {
                price = ii.getWholePrice(item.getItemId()) / ii.getSlotMax(item.getItemId());
            } else {
                price = ii.getPrice(item.getItemId());
            }
            int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0.0D);
            if ((price != -1.0D) && (recvMesos > 0)) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.卖出道具完成, this, c, -1));
        }
    }

    /**
     * 给道具冲值
     * @param c
     * @param slot
     */
    public void recharge(MapleClient c, Short slot) {
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (item == null || (!ItemConstants.is飞镖道具(item.getItemId()) && !ItemConstants.is子弹道具(item.getItemId()))) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(item.getItemId());
        int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());
        if (skill != 0) {
            slotMax = (short) (slotMax + c.getPlayer().getTotalSkillLevel(SkillFactory.getSkill(skill)) * 10);
        }
        if (item.getQuantity() < slotMax) {
            int price = (int) Math.round(ii.getPrice(item.getItemId()) * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                //c.sendPacket(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(1, item))));
                c.sendPacket(InventoryPacket.updateInventorySlot(MapleInventoryType.USE, item));
                c.getPlayer().gainMeso(-price, false, false);
                c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.充值飞镖完成, this, c, -1));
            } else {
                c.sendPacket(NPCPacket.confirmShopTransaction(MapleShopResponse.充值金币不够, this, c, -1));
            }
        }
    }

    protected MapleShopItem findBySlotAndId(MapleClient c, int itemId, int pos) {
        MapleShopItem shopItem = getItems(c).get(pos);

        if ((shopItem != null) && (shopItem.getItemId() == itemId)) {
            return shopItem;
        }
        return null;
    }

    public static MapleShop createFromDbByShopId(int shopId) {
        ShopDao shopDao = new ShopDao();
        ShopPo shopPo = shopDao.getDataByShopId(shopId);

        return createFromDB(shopPo);
    }

    public static MapleShop createFromDbByNpcId(int npcId) {
        ShopDao shopDao = new ShopDao();
        ShopPo shopPo = shopDao.getDataByNpcId(npcId);

        return createFromDB(shopPo);
    }

    public static MapleShop createFromDB(ShopPo shopPo) {
        MapleShop ret = null;
        int shopId;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        shopId = shopPo.getShopid();
        ret = new MapleShop(shopId, shopPo.getNpcid());

        ShopItemDao shopItemDao = new ShopItemDao();
        List<ShopItemPo> shopItemPoList = shopItemDao.getDataByShopId(shopId);
        List<Integer> recharges = new ArrayList(rechargeableItems);
        short buyable = 1;
        for(ShopItemPo shopItem : shopItemPoList) {
            int itemId = shopItem.getItemid();
            if ((ii.itemExists(itemId)) || !(blockedItems.contains(itemId))) {
                if ((ItemConstants.is飞镖道具(itemId)) || (ItemConstants.is子弹道具(itemId))) {
                    buyable = 1;
                    if (rechargeableItems.contains(itemId)) {
                        recharges.remove(Integer.valueOf(itemId));
                    }
                } else {
                    buyable = 1000;
                }
                MapleShopItem starItem = new MapleShopItem(buyable, itemId, shopItem.getPrice(),
                        shopItem.getReqitem(), shopItem.getReqitemq(), shopItem.getPeriod(),
                        shopItem.getState(),shopItem.getRank());
                ret.addItem(starItem);
            }
        }

        for (Integer recharge : recharges) {
            ret.addItem(new MapleShopItem((short) 1, recharge, 0, 0, 0, 0, 0, (byte) 0));
        }
        return ret;
    }

    public int getNpcId() {
        return this.npcId;
    }

    public int getId() {
        return this.id;
    }

    public int getShopItemId() {
        return this.shopItemId;
    }

    static {
        rechargeableItems.add(2070000);//海星镖
        rechargeableItems.add(2070001);//回旋镖
        rechargeableItems.add(2070002);//黑色利刃
        rechargeableItems.add(2070003);//雪花镖
        rechargeableItems.add(2070004);//黑色刺
        rechargeableItems.add(2070005);//金钱镖
        rechargeableItems.add(2070006);//齿轮镖
        rechargeableItems.add(2070007);//月牙镖
        rechargeableItems.add(2070008);//小雪球
        rechargeableItems.add(2070009);//木制陀螺
        rechargeableItems.add(2070010);//冰菱
        rechargeableItems.add(2070011);//枫叶镖
        rechargeableItems.add(2070012);//纸飞机
        rechargeableItems.add(2070013);//橘子

    }
}
