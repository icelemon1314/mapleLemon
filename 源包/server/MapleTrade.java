package server;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessor;
import client.messages.CommandType;
import constants.GameConstants;
import constants.ItemConstants;
import handling.world.WorldBroadcastService;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.PlayerShopPacket;
import tools.packet.TradePacket;

public class MapleTrade {

    private MapleTrade partner = null;
    private final List<Item> items = new LinkedList();
    private List<Item> exchangeItems;
    private int meso = 0;
    private int exchangeMeso = 0;
    private boolean locked = false;
    private boolean inTrade = false;
    private final WeakReference<MapleCharacter> chr;
    private final byte tradingslot;
    private static final Logger log = Logger.getLogger(MapleTrade.class);

    public MapleTrade(byte tradingslot, MapleCharacter chr) {
        this.tradingslot = tradingslot;
        this.chr = new WeakReference(chr);
    }

    public void CompleteTrade() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (this.exchangeItems != null) {
            List<Item> itemz = new LinkedList(this.exchangeItems);
            for (Item item : itemz) {
                short flag = item.getFlag();
                if (ItemFlag.KARMA_USE.check(flag)) {
                    item.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
                }
                MapleInventoryManipulator.addFromDrop(((MapleCharacter) this.chr.get()).getClient(), item, false);
                FileoutputUtil.log("[交易] " + ((MapleCharacter) this.chr.get()).getName() + " 交易获得道具: " + item.getItemId() + " x " + item.getQuantity() + " - " + ii.getName(item.getItemId()));
            }
            this.exchangeItems.clear();
        }
        if (this.exchangeMeso > 0) {
            ((MapleCharacter) this.chr.get()).gainMeso(this.exchangeMeso - GameConstants.getTaxAmount(this.exchangeMeso), false, false);
            FileoutputUtil.log("[交易] " + ((MapleCharacter) this.chr.get()).getName() + " 交易获得金币: " + this.exchangeMeso);
        }
        this.exchangeMeso = 0;
        ((MapleCharacter) this.chr.get()).getClient().getSession().write(TradePacket.TradeMessage(this.tradingslot, (byte) 8));
    }

    public void cancel(MapleClient c, MapleCharacter chr) {
        cancel(c, chr, 0);
    }

    public void cancel(MapleClient c, MapleCharacter chr, int message) {
        if (this.items != null) {
            List<Item> itemz = new LinkedList(this.items);
            for (Item item : itemz) {
                MapleInventoryManipulator.addFromDrop(c, item, false);
            }
            this.items.clear();
        }
        if (this.meso > 0) {
            chr.gainMeso(this.meso, false, false);
        }
        this.meso = 0;
        c.getSession().write(TradePacket.getTradeCancel(this.tradingslot, message));
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setMeso(int meso) {
        if ((this.locked) || (this.partner == null) || (meso <= 0) || (this.meso + meso <= 0)) {
            return;
        }
        if (((MapleCharacter) this.chr.get()).getMeso() >= meso) {
            ((MapleCharacter) this.chr.get()).gainMeso(-meso, false, false);
            this.meso += meso;
            ((MapleCharacter) this.chr.get()).getClient().getSession().write(TradePacket.getTradeMesoSet((byte) 0, this.meso));
            if (this.partner != null) {
                this.partner.getChr().getClient().getSession().write(TradePacket.getTradeMesoSet((byte) 1, this.meso));
            }
        }
    }

    public void addItem(Item item) {
        if ((this.locked) || (this.partner == null)) {
            return;
        }
        this.items.add(item);
        ((MapleCharacter) this.chr.get()).getClient().getSession().write(TradePacket.getTradeItemAdd((byte) 0, item));
        if (this.partner != null) {
            this.partner.getChr().getClient().getSession().write(TradePacket.getTradeItemAdd((byte) 1, item));
        }
    }

    public void chat(String message) {
        if (!CommandProcessor.processCommand(((MapleCharacter) this.chr.get()).getClient(), message, CommandType.TRADE)) {
            ((MapleCharacter) this.chr.get()).dropMessage(-2, ((MapleCharacter) this.chr.get()).getName() + " : " + message);
            if (this.partner != null) {
                this.partner.getChr().getClient().getSession().write(PlayerShopPacket.shopChat(((MapleCharacter) this.chr.get()).getName() + " : " + message, 1));
            }
        }
        if (((MapleCharacter) this.chr.get()).getClient().isMonitored()) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(((MapleCharacter) this.chr.get()).getName() + " 在交易中对 " + this.partner.getChr().getName() + " 说: " + message));
        } else if ((this.partner != null) && (this.partner.getChr() != null) && (this.partner.getChr().getClient().isMonitored())) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(((MapleCharacter) this.chr.get()).getName() + " 在交易中对 " + this.partner.getChr().getName() + " 说: " + message));
        }
    }

    public void chatAuto(String message) {
        ((MapleCharacter) this.chr.get()).dropMessage(-2, message);
        if (this.partner != null) {
            this.partner.getChr().getClient().getSession().write(PlayerShopPacket.shopChat(message, 1));
        }
        if (((MapleCharacter) this.chr.get()).getClient().isMonitored()) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText( ((MapleCharacter) this.chr.get()).getName() + " said in trade [Automated] with " + this.partner.getChr().getName() + " 说: " + message));
        } else if ((this.partner != null) && (this.partner.getChr() != null) && (this.partner.getChr().getClient().isMonitored())) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(((MapleCharacter) this.chr.get()).getName() + " said in trade [Automated] with " + this.partner.getChr().getName() + " 说: " + message));
        }
    }

    public MapleTrade getPartner() {
        return this.partner;
    }

    public void setPartner(MapleTrade partner) {
        if (this.locked) {
            return;
        }
        this.partner = partner;
    }

    public MapleCharacter getChr() {
        return (MapleCharacter) this.chr.get();
    }

    public int getNextTargetSlot() {
        if (this.items.size() >= 9) {
            return -1;
        }
        int ret = 1;
        for (Item item : this.items) {
            if (item.getPosition() == ret) {
                ret++;
            }
        }
        return ret;
    }

    public boolean inTrade() {
        return this.inTrade;
    }

    public boolean setItems(MapleClient c, Item item, byte targetSlot, int quantity) {
        int target = getNextTargetSlot();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((this.partner == null) || (target == -1) || (ItemConstants.isPet(item.getItemId())) || (isLocked()) || ((ItemConstants.getInventoryType(item.getItemId()) == MapleInventoryType.EQUIP) && (quantity != 1))) {
            return false;
        }
        short flag = item.getFlag();
        if (ItemFlag.封印.check(flag)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        if (ItemFlag.不可交易.check(flag) && !ItemFlag.KARMA_USE.check(flag)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        if ((ii.isDropRestricted(item.getItemId()) || ii.isAccountShared(item.getItemId())) && !ItemFlag.KARMA_USE.check(flag)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return false;
        }

        Item tradeItem = item.copy();
        if ((ItemConstants.is飞镖道具(item.getItemId())) || (ItemConstants.is子弹道具(item.getItemId()))) {
            tradeItem.setQuantity(item.getQuantity());
            MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(item.getItemId()), item.getPosition(), item.getQuantity(), true);
        } else {
            tradeItem.setQuantity((short) quantity);
            MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(item.getItemId()), item.getPosition(), (short) quantity, true);
        }
        if (targetSlot < 0) {
            targetSlot = (byte) target;
        } else {
            for (Item itemz : this.items) {
                if (itemz.getPosition() == targetSlot) {
                    targetSlot = (byte) target;
                    break;
                }
            }
        }
        tradeItem.setPosition((byte) targetSlot);
        addItem(tradeItem);
        return true;
    }

    private int check() {
        if (((MapleCharacter) this.chr.get()).getMeso() + this.exchangeMeso < 0) {
            return 1;
        }
        if (this.exchangeItems != null) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            byte eq = 0;
            byte use = 0;
            byte setup = 0;
            byte etc = 0;
            byte cash = 0;
            for (Item item : this.exchangeItems) {
                switch (ItemConstants.getInventoryType(item.getItemId())) {
                    case EQUIP:
                        eq++;
                        break;
                    case USE:
                        use++;
                        break;
                    case SETUP:
                        setup++;
                        break;
                    case ETC:
                        etc++;
                        break;
                    case CASH:
                        cash++;
                }

                if ((ii.isPickupRestricted(item.getItemId())) && (((MapleCharacter) this.chr.get()).haveItem(item.getItemId(), 1, true, true))) {
                    return 2;
                }
            }
            if ((((MapleCharacter) this.chr.get()).getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) || (((MapleCharacter) this.chr.get()).getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) || (((MapleCharacter) this.chr.get()).getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) || (((MapleCharacter) this.chr.get()).getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) || (((MapleCharacter) this.chr.get()).getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash)) {
                return 1;
            }
        }
        return 0;
    }

    public static void completeTrade(MapleCharacter player) {
        MapleTrade local = player.getTrade();
        MapleTrade partner = local.getPartner();
        if ((partner == null) || (local.locked)) {
            return;
        }
        local.locked = true;
        partner.getChr().getClient().getSession().write(TradePacket.getTradeConfirmation());
        partner.exchangeItems = new LinkedList(local.items);
        partner.exchangeMeso = local.meso;
        if (partner.isLocked()) {
            int lz = local.check();
            int lz2 = partner.check();
            if ((lz == 0) && (lz2 == 0)) {
                local.CompleteTrade();
                partner.CompleteTrade();
                FileoutputUtil.log("[交易] " + local.getChr().getName() + " 和 " + partner.getChr().getName() + " 交易完成。");
            } else {
                partner.cancel(partner.getChr().getClient(), partner.getChr(), lz == 0 ? lz2 : lz);
                local.cancel(player.getClient(), player, lz == 0 ? lz2 : lz);
            }
            partner.getChr().setTrade(null);
            player.setTrade(null);
        }
    }

    public static void cancelTrade(MapleTrade Localtrade, MapleClient c, MapleCharacter player) {
        Localtrade.cancel(c, player);
        MapleTrade partner = Localtrade.getPartner();
        if ((partner != null) && (partner.getChr() != null)) {
            partner.cancel(partner.getChr().getClient(), partner.getChr());
            partner.getChr().setTrade(null);
        }
        player.setTrade(null);
    }

    public static void startTrade(MapleCharacter player) {
        if (player.getTrade() == null) {
            player.setTrade(new MapleTrade((byte) 0, player));
            player.getClient().getSession().write(TradePacket.getTradeStart(player.getClient(), player.getTrade(), (byte) 0));
        } else {
            player.getClient().getSession().write(MaplePacketCreator.serverMessageRedText("不能同时做多件事情。"));
        }
    }

    public static void inviteTrade(MapleCharacter player, MapleCharacter target) {
        if ((player == null) || (player.getTrade() == null)) {
            return;
        }
        if ((target != null) && (target.getTrade() == null)) {
            target.setTrade(new MapleTrade((byte) 1, target));
            target.getTrade().setPartner(player.getTrade());
            player.getTrade().setPartner(target.getTrade());
            target.getClient().getSession().write(TradePacket.getTradeInvite(player));
        } else {
            player.getClient().getSession().write(MaplePacketCreator.serverMessageRedText("对方正在和其他玩家进行交易中。"));
            cancelTrade(player.getTrade(), player.getClient(), player);
        }
    }

    public static void visitTrade(MapleCharacter player, MapleCharacter target) {
        if ((target != null) && (player.getTrade() != null) && (player.getTrade().getPartner() == target.getTrade()) && (target.getTrade() != null) && (target.getTrade().getPartner() == player.getTrade())) {
            player.getTrade().inTrade = true;
            target.getClient().getSession().write(PlayerShopPacket.shopVisitorAdd(player, 1));
            player.getClient().getSession().write(TradePacket.getTradeStart(player.getClient(), player.getTrade(), (byte) 1));
            player.dropMessage(-2, "系统提示 : 进行金币交换请注意手续费");
            target.dropMessage(-2, "系统提示 : 进行金币交换请注意手续费");
            player.getClient().getSession().write(MaplePacketCreator.enableActions());
            target.getClient().getSession().write(MaplePacketCreator.enableActions());
        } else {
            player.getClient().getSession().write(MaplePacketCreator.serverMessageRedText("对方已经取消了交易。"));
        }

    }

    public static void declineTrade(MapleCharacter player) {
        MapleTrade trade = player.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                MapleCharacter other = trade.getPartner().getChr();
                if ((other != null) && (other.getTrade() != null)) {
                    other.getTrade().cancel(other.getClient(), other);
                    other.setTrade(null);
                    other.dropMessage(5, player.getName() + " 拒绝了你的交易邀请。");
                }
            }
            trade.cancel(player.getClient(), player);
            player.setTrade(null);
        }
    }
}
