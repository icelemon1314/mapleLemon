package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import database.DatabaseConnection;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MerchItemPackage;
import server.shops.HiredMerchant;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PlayerShopPacket;

public class HiredMerchantHandler {

    private static final Logger log = Logger.getLogger(HiredMerchantHandler.class);

    public static boolean UseHiredMerchant(MapleClient c, boolean packet) {
        MapleCharacter chr = c.getPlayer();
        if (c.getChannelServer().isShutdown()) {
            chr.dropMessage(1, "服务器即将关闭维护，暂时无法进行开店。");
            return false;
        }
        if ((chr.getMap() != null) && (chr.getMap().allowPersonalShop())) {
            HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
            if (merchant != null) {
                c.getSession().write(PlayerShopPacket.sendTitleBox(8, merchant.getMapId(), merchant.getChannel() - 1));
            } else {
                if (loadItemFrom_Database(chr) == null) {
                    if (packet) {
                        c.getSession().write(PlayerShopPacket.sendTitleBox(7));
                    }
                    return true;
                }
                c.getSession().write(PlayerShopPacket.sendTitleBox(9));
            }
        }

        return false;
    }

    private static long getMerchMesos(MapleCharacter chr) {
        Connection con = DatabaseConnection.getConnection();
        try {
            long mesos;
            try (PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ?")) {
                ps.setInt(1, chr.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        ps.close();
                        rs.close();
                        return 0;
                    }   mesos = rs.getLong("Mesos");
                }
                ps.close();
            }
            return mesos > 0 ? mesos : 0;
        } catch (SQLException se) {
        }
        return 0;
    }

    public static void MerchantItemStore(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (c.getChannelServer().isShutdown()) {
            chr.dropMessage(1, "服务器即将关闭维护，暂时无法进行道具取回。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte operation = slea.readByte();
        switch (operation) {
            case 24:
                HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
                if (merchant != null) {
                    c.getSession().write(PlayerShopPacket.merchItemStore(merchant.getMapId(), merchant.getChannel() - 1));
                    chr.setConversation(0);
                } else {
                    MerchItemPackage pack = loadItemFrom_Database(chr);

                    if (pack == null) {
                        c.getSession().write(PlayerShopPacket.merchItemStore(999999999, 0));
                        chr.setConversation(0);
                    } else {
                        c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
                    }
                }
                break;
            case 25:
                if (chr.getConversation() != 3) {
                    return;
                }
                c.getSession().write(PlayerShopPacket.merchItemStore((byte) 42));
                break;
            case 30:
                if (chr.getConversation() != 3) {
                    return;
                }
                boolean merch = World.hasMerchant(chr.getAccountID(), chr.getId());
                if (merch) {
                    chr.dropMessage(1, "请关闭现有的商店.");
                    chr.setConversation(0);
                    return;
                }
                MerchItemPackage pack = loadItemFrom_Database(chr);
                if (pack == null) {
                    chr.dropMessage(1, "发生了未知错误.");
                    return;
                }
                if (!check(chr, pack)) {
                    chr.dropMessage(5, "因为背包位置不足够无法领取道具.");
                    c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 39));
                    return;
                }
                if (deletePackage(chr.getId())) {
                    if (pack.getMesos() > 0) {
                        chr.gainMeso(pack.getMesos(), false);
                        FileoutputUtil.log("[雇佣] " + chr.getName() + " 雇佣取回获得金币: " + pack.getMesos() + " 时间: " + FileoutputUtil.CurrentReadable_Date());
                        FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣取回获得金币: " + pack.getMesos());
                    }
                    for (Item item : pack.getItems()) {
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣取回获得道具: " + item.getItemId() + " - " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " 数量: " + item.getQuantity());
                    }
                    c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 35));
                } else {
                    chr.dropMessage(1, "发生了未知错误.");
                }
                break;
            case 32:
                chr.setConversation(0);
                break;
            case 26:
            case 27:
            case 28:
            case 29:
            case 31:
            default:
                FileoutputUtil.log("弗洛兰德：未知的操作类型 " + operation);
        }
    }

    public static void RemoteStore(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
        if (merchant != null) {
            if (merchant.getChannel() == chr.getClient().getChannel()) {
                merchant.setOpen(false);
                merchant.removeAllVisitors(16, 0);
                chr.setPlayerShop(merchant);
                c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
            } else {
                c.getSession().write(PlayerShopPacket.sendTitleBox(16, 0, merchant.getChannel() - 1));
            }
        } else {
            chr.dropMessage(1, "你没有开设商店");
        }

        c.getSession().write(MaplePacketCreator.enableActions());
    }

    private static boolean check(MapleCharacter chr, MerchItemPackage pack) {
        if (chr.getMeso() + pack.getMesos() < 0) {
            FileoutputUtil.log("[雇佣] " + chr.getName() + " 雇佣取回道具金币检测错误 时间: " + FileoutputUtil.CurrentReadable_Date());
            FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣取回道具金币检测错误");
            return false;
        }
        byte eq = 0;
        byte use = 0;
        byte setup = 0;
        byte etc = 0;
        byte cash = 0;
        for (Item item : pack.getItems()) {
            MapleInventoryType invtype = ItemConstants.getInventoryType(item.getItemId());
            if (invtype == MapleInventoryType.EQUIP) {
                eq = (byte) (eq + 1);
            } else if (invtype == MapleInventoryType.USE) {
                use = (byte) (use + 1);
            } else if (invtype == MapleInventoryType.SETUP) {
                setup = (byte) (setup + 1);
            } else if (invtype == MapleInventoryType.ETC) {
                etc = (byte) (etc + 1);
            } else if (invtype == MapleInventoryType.CASH) {
                cash = (byte) (cash + 1);
            }
            if ((MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId())) && (chr.haveItem(item.getItemId(), 1))) {
                FileoutputUtil.log("[雇佣] " + chr.getName() + " 雇佣取回道具是否可以捡取错误 时间: " + FileoutputUtil.CurrentReadable_Date());
                FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣取回道具是否可以捡取错误");
                return false;
            }
        }
        if ((chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) || (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) || (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) || (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) || (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash)) {
            FileoutputUtil.log("[雇佣] " + chr.getName() + " 雇佣取回道具背包空间不够 时间: " + FileoutputUtil.CurrentReadable_Date());
            FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣取回道具背包空间不够");
            return false;
        }
        return true;
    }

    private static boolean deletePackage(int charId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where characterid = ?")) {
                ps.setInt(1, charId);
                ps.executeUpdate();
                ps.close();
            }
            ItemLoader.雇佣道具.saveItems(null, charId);
            return true;
        } catch (SQLException e) {
            FileoutputUtil.log("删除弗洛兰德道具信息出错" + e);
        }
        return false;
    }

    private static MerchItemPackage loadItemFrom_Database(MapleCharacter chr) {
        try {
            long mesos = chr.getMerchantMeso();
            Map<Long, Pair<Item, MapleInventoryType>> items = ItemLoader.雇佣道具.loadItems(false, chr.getId());
            if ((mesos == 0) && (items.isEmpty())) {
                return null;
            }
            MerchItemPackage pack = new MerchItemPackage();
            pack.setMesos(mesos);
            if (!items.isEmpty()) {
                List iters = new ArrayList();
                for (Pair z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }
            FileoutputUtil.hiredMerchLog(chr.getName(), "弗洛兰德取回最后返回 金币: " + mesos + " 道具数量: " + items.size());
            return pack;
        } catch (SQLException e) {
            FileoutputUtil.log("加载弗洛兰德道具信息出错" + e);
        }
        return null;
    }
}


