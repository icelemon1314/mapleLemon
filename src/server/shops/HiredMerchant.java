package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import constants.GameConstants;
import handling.channel.ChannelServer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.apache.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Timer.EtcTimer;
import server.maps.MapleMapObjectType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.PlayerShopPacket;

public class HiredMerchant extends AbstractPlayerStore {

    public ScheduledFuture<?> schedule;
    private final List<String> blacklist;
    private int storeid;
    private final long start;
    private long lastChangeNameTime = 0L;
    private static final Logger log = Logger.getLogger(HiredMerchant.class);

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6);
        this.start = System.currentTimeMillis();
        this.blacklist = new LinkedList();
        this.schedule = EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if ((HiredMerchant.this.getMCOwner() != null) && (HiredMerchant.this.getMCOwner().getPlayerShop() == HiredMerchant.this)) {
                    HiredMerchant.this.getMCOwner().setPlayerShop(null);
                }
                HiredMerchant.this.removeAllVisitors(-1, -1);
                HiredMerchant.this.closeShop(true, true);
            }
        }, 86400000L);
    }

    @Override
    public byte getShopType() {
        return 1;
    }

    public void setStoreid(int storeid) {
        this.storeid = storeid;
    }

    public List<MaplePlayerShopItem> searchItem(int itemSearch) {
        List itemz = new LinkedList();
        for (MaplePlayerShopItem item : this.items) {
            if ((item.item.getItemId() == itemSearch) && (item.bundles > 0)) {
                itemz.add(item);
            }
        }
        return itemz;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = (MaplePlayerShopItem) this.items.get(item);
        Item shopItem = pItem.item;
        Item newItem = shopItem.copy();
        short perbundle = newItem.getQuantity();
        int theQuantity = pItem.price * quantity;
        newItem.setQuantity((short) (quantity * perbundle));

        short flag = newItem.getFlag();

        if (ItemFlag.KARMA_USE.check(flag)) {
            newItem.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
        }

        if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner())) {
            int gainmeso = getMeso() + theQuantity - GameConstants.EntrustedStoreTax(theQuantity);
            if (gainmeso > 0) {
                setMeso(gainmeso);
                MaplePlayerShopItem tmp167_165 = pItem;
                tmp167_165.bundles = (short) (tmp167_165.bundles - quantity);
                MapleInventoryManipulator.addFromDrop(c, newItem, false);
                this.bought.add(new AbstractPlayerStore.BoughtItem(newItem.getItemId(), quantity, theQuantity, c.getPlayer().getName()));
                c.getPlayer().gainMeso(-theQuantity, false);
                saveItems();
                MapleCharacter chr = getMCOwnerWorld();
                String itemText = new StringBuilder().append(MapleItemInformationProvider.getInstance().getName(newItem.getItemId())).append(" (").append(perbundle).append(") x ").append(quantity).append(" 已经被卖出。 剩余数量: ").append(pItem.bundles).append(" 购买者: ").append(c.getPlayer().getName()).toString();
                if (chr != null) {
                    chr.dropMessage(-5, new StringBuilder().append("您雇佣商店里面的道具: ").append(itemText).toString());
                }
                FileoutputUtil.log(new StringBuilder().append("[雇佣] ").append(chr != null ? chr.getName() : getOwnerName()).append(" 雇佣商店卖出: ").append(newItem.getItemId()).append(" - ").append(itemText).append(" 价格: ").append(theQuantity).toString());
                FileoutputUtil.hiredMerchLog(chr != null ? chr.getName() : getOwnerName(), new StringBuilder().append("雇佣商店卖出: ").append(newItem.getItemId()).append(" - ").append(itemText).append(" 价格: ").append(theQuantity).toString());
            } else {
                c.getPlayer().dropMessage(1, "金币不足.");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            c.getPlayer().dropMessage(1, "背包已满.");
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
        if (this.schedule != null) {
            this.schedule.cancel(false);
        }
        if (saveItems) {
            saveItems();
            this.items.clear();
        }
        if (remove) {
            ChannelServer.getInstance(this.channel).removeMerchant(this);
            getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
        getMap().removeMapObject(this);
        this.schedule = null;
    }

    public int getTimeLeft() {
        return (int) (System.currentTimeMillis() - this.start);
    }

    public int getTimeLeft(boolean first) {
        if (first) {
            return (int) this.start;
        }
        return 86400 - (int) (System.currentTimeMillis() - this.start) / 1000;
    }

    public int getStoreId() {
        return this.storeid;
    }

    public boolean canChangeName() {
        if (this.lastChangeNameTime + 60000L > System.currentTimeMillis()) {
            return false;
        }
        this.lastChangeNameTime = System.currentTimeMillis();
        return true;
    }

    public int getChangeNameTimeLeft() {
        int time = 60 - (int) (System.currentTimeMillis() - this.lastChangeNameTime) / 1000;
        return time > 0 ? time : 1;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }

    public boolean isInBlackList(String bl) {
        return this.blacklist.contains(bl);
    }

    public void addBlackList(String bl) {
        this.blacklist.add(bl);
    }

    public void removeBlackList(String bl) {
        this.blacklist.remove(bl);
    }

    public void sendBlackList(MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantBlackListView(this.blacklist));
    }

    public void sendVisitor(MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantVisitorView(this.visitorsList));
    }
}
