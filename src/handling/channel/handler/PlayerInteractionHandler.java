package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import handling.InteractionOpcode;
import handling.world.WorldBroadcastService;
import java.util.Arrays;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.ServerProperties;
import server.maps.FieldLimitType;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerInteractionHandler {

    public static void PlayerInteraction(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte mode = slea.readByte();
        InteractionOpcode action = InteractionOpcode.getByAction(mode);
        if (chr == null || (action == null)) {
            MapleLogger.info("玩家互动未知的操作类型: " + mode + " " + slea.toString());
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        chr.setScrolledPosition((short) 0);
        if (chr.isShowPacket()) {
            chr.dropMessage(5, "玩家互动操作类型: " + action);
        }
        switch (action) {
            case 创建:
                if ((chr.getPlayerShop() != null) || (c.getChannelServer().isShutdown()) || (chr.hasBlockedInventory())) {
                    chr.dropMessage(1, "现在还不能进行.");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                byte createType = slea.readByte();
                if (createType == 4) {
                    MapleTrade.startTrade(chr);
                } else {
                    if ((createType != 1) && (createType != 2) && (createType != 5) && (createType != 6)) {
                        break;
                    }
                    if ((!chr.getMap().getMapObjectsInRange(chr.getTruePosition(), 20000.0D, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT})).isEmpty()) || (!chr.getMap().getPortalsInRange(chr.getTruePosition(), 20000.0D).isEmpty())) {
                        chr.dropMessage(1, "无法在这个地方使用.");
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (((createType == 1) || (createType == 2)) && ((FieldLimitType.Minigames.check(chr.getMap().getFieldLimit())) || (chr.getMap().allowPersonalShop()))) {
                        chr.dropMessage(1, "无法在这个地方使用.");
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }

                    String desc = slea.readMapleAsciiString();
                    String pass = "";
                    if (slea.readByte() > 0) {
                        pass = slea.readMapleAsciiString();
                    }
                }
                break;
            case 交易邀请:
                if (chr.getMap() == null) {
                    return;
                }
                MapleCharacter chrr = chr.getMap().getCharacterById(slea.readInt());
                if ((chrr == null) || (c.getChannelServer().isShutdown()) || (chrr.hasBlockedInventory())) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                MapleTrade.inviteTrade(chr, chrr);
                break;
            case 拒绝邀请:
                MapleTrade.declineTrade(chr);
                break;
            case 访问:
                if (c.getChannelServer().isShutdown()) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if ((chr.getTrade() != null) && (chr.getTrade().getPartner() != null) && (!chr.getTrade().inTrade())) {
                    MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                } else {
                    if ((chr.getMap() == null) || (chr.getTrade() != null)) {
                        break;
                    }
                    int obid = slea.readInt();
                    MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                    if (ob == null) {
                        ob = chr.getMap().getMapObject(obid, MapleMapObjectType.SHOP);
                    }
                    if (((ob instanceof IMaplePlayerShop)) && (chr.getPlayerShop() == null)) {
                        IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                        if ((ob instanceof HiredMerchant)) {
                            HiredMerchant merchant = (HiredMerchant) ips;
                            if ((merchant.isOwner(chr)) && (merchant.isOpen()) && (merchant.isAvailable())) {
                                merchant.setOpen(false);
                                merchant.removeAllVisitors(18, 1);
                                chr.setPlayerShop(ips);
                            } else if ((!merchant.isOpen()) || (!merchant.isAvailable())) {
                                chr.dropMessage(1, "主人正在整理商店物品\r\n请稍后再度光临！");
                            } else if (ips.getFreeSlot() == -1) {
                                chr.dropMessage(1, "店铺已达到最大人数\r\n请稍后再度光临！");
                            } else if (merchant.isInBlackList(chr.getName())) {
                                chr.dropMessage(1, "你被禁止进入该店铺");
                            } else {
                                chr.setPlayerShop(ips);
                                merchant.addVisitor(chr);
                            }

                        } else if (((ips instanceof MaplePlayerShop)) && (((MaplePlayerShop) ips).isBanned(chr.getName()))) {
                            chr.dropMessage(1, "你被禁止进入该店铺");
                        } else if ((ips.getFreeSlot() < 0) || (ips.getVisitorSlot(chr) > -1) || (!ips.isOpen()) || (!ips.isAvailable())) {
                        } else {
                            if ((slea.available() > 0L) && (slea.readByte() > 0)) {
                                String pass = slea.readMapleAsciiString();
                                if (!pass.equals(ips.getPassword())) {
                                    c.getPlayer().dropMessage(1, "你输入的密码不正确.");
                                    return;
                                }
                            } else if (ips.getPassword().length() > 0) {
                                c.getPlayer().dropMessage(1, "你输入的密码不正确.");
                                return;
                            }
                            chr.setPlayerShop(ips);
                            ips.addVisitor(chr);
                        }
                    }
                }
                break;
            case 聊天:
                String message = slea.readMapleAsciiString();
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(message);
                } else {
                    if (chr.getPlayerShop() == null) {
                        break;
                    }
                    IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips.getShopType() == 1) {
                        ips.getMessages().add(new Pair(chr.getName() + " : " + message, ips.getVisitorSlot(chr)));
                    }
                    if (chr.getClient().isMonitored()) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText(chr.getName() + " said in " + ips.getOwnerName() + " shop : " + message));
                    }
                }
                break;
            case 退出:
                if (chr.getTrade() != null) {
                    MapleTrade.cancelTrade(chr.getTrade(), chr.getClient(), chr);
                } else {
                    IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips == null) {
                        return;
                    }
                    if ((ips.isOwner(chr)) && (ips.getShopType() != 1)) {
                        ips.closeShop(false, ips.isAvailable());
//                        ips.setOpen(true);
//                        ips.setAvailable(true);
                    } else {
                        ips.removeVisitor(chr);
                        if ((ips.isOwner(chr)) && (ips.isOpen() == false) && (ips.isAvailable())) {
                            ips.setOpen(true);
                        }
                    }
                    chr.setPlayerShop(null);
                }
                break;
            case 管理员修改雇佣商店名称:
                IMaplePlayerShop shop = chr.getPlayerShop();
                if ((shop == null) || (!shop.isOwner(chr)) || (shop.getShopType() >= 3) || (shop.isAvailable())) {
                    break;
                }
                if (chr.getMap().allowPersonalShop()) {
                    if (c.getChannelServer().isShutdown()) {
                        chr.dropMessage(1, "服务器即将关闭维护，暂时无法进行此操作。.");
                        c.sendPacket(MaplePacketCreator.enableActions());
                        shop.closeShop(shop.getShopType() == 1, false);
                        return;
                    }

                    if (shop.getShopType() != 2) {
                        break;
                    }
                    shop.setOpen(true);
                    shop.setAvailable(true);
                    shop.update();

                } else {
                    chr.getClient().disconnect(true, false);
                    c.getSession().close();
                }
                break;
            case 设置物品:
            case 设置物品_001:
            case 设置物品_002:
            case 设置物品_003:
                MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
                Item item = chr.getInventory(ivType).getItem((short) (byte) slea.readShort());
                short quantity = slea.readShort();
                byte targetSlot = slea.readByte();
                if (chr.getTrade() == null || item == null) {
                    break;
                }
                boolean canTrade = true;
                if (item.getItemId() == 4000463 && !canTrade) {
                    chr.dropMessage(1, "该道具无法进行交易.");
                    c.sendPacket(MaplePacketCreator.enableActions());
                } else if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.is飞镖道具(item.getItemId()) || ItemConstants.is子弹道具(item.getItemId())) {
                    chr.getTrade().setItems(c, item, targetSlot, quantity);
                }
                break;
            case 设置金币:
            case 设置金币_005:
            case 设置金币_006:
            case 设置金币_007:
                MapleTrade trade = chr.getTrade();
                if (trade == null) {
                    break;
                }
                trade.setMeso(slea.readInt());
                break;
            case 确认交易:
            case 确认交易_009:
            case 确认交易_00A:
            case 确认交易_00B:
                if (chr.getTrade() == null) {
                    break;
                }
                MapleTrade.completeTrade(chr);
                break;
            case 添加物品:
            case 添加物品_0020:
            case 添加物品_0021:
            case 添加物品_0022:
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                final byte slot = (byte) slea.readShort();
                short bundles = slea.readShort();
                short perBundle = slea.readShort();
                int price = slea.readInt();
                if ((price <= 0) || (bundles <= 0) || (perBundle <= 0)) {
                    chr.dropMessage(1, "添加物品出现错误(1)");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                shop = chr.getPlayerShop();
                if ((shop == null) || (!shop.isOwner(chr))) {
                    return;
                }
                Item ivItem = chr.getInventory(type).getItem((short) slot);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (ivItem == null) {
                    break;
                }
                long check = bundles * perBundle;
                if ((check > 32767L) || (check <= 0L)) {
                    return;
                }
                short bundles_perbundle = (short) (bundles * perBundle);
                if (ivItem.getQuantity() >= bundles_perbundle) {
                    short flag = ivItem.getFlag();
                    if (ItemFlag.封印.check(flag)) {
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (ItemFlag.不可交易.check(flag) && !ItemFlag.KARMA_USE.check(flag)) {
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if ((ii.isDropRestricted(ivItem.getItemId()) || ii.isAccountShared(ivItem.getItemId())) && !ItemFlag.KARMA_USE.check(flag)) {
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }

                    if (ivItem.getItemId() == 4000463) {
                        chr.dropMessage(1, "该道具无法进行贩卖.");
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if ((bundles_perbundle >= 50) && (ivItem.getItemId() == 2340000)) {
                        c.setMonitored(true);
                    }
                    if (ItemConstants.getLowestPrice(ivItem.getItemId()) > price) {
                        c.getPlayer().dropMessage(1, "The lowest you can sell this for is " + ItemConstants.getLowestPrice(ivItem.getItemId()));
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return;
                    }
                    if ((ItemConstants.is飞镖道具(ivItem.getItemId())) || (ItemConstants.is子弹道具(ivItem.getItemId()))) {
                        MapleInventoryManipulator.removeFromSlot(c, type, (short) slot, ivItem.getQuantity(), true);
                        Item sellItem = ivItem.copy();
                        shop.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price));
                    } else {
                        MapleInventoryManipulator.removeFromSlot(c, type, (short) slot, bundles_perbundle, true);
                        Item sellItem = ivItem.copy();
                        sellItem.setQuantity(perBundle);
                        shop.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
                    }
//                    c.sendPacket(PlayerShopPacket.shopItemUpdate(shop));
                } else {
                    chr.dropMessage(1, "添加物品的数量错误。如果是飞镖，子弹之类请充了后在进行贩卖。");
                }
                break;

            case BUY_ITEM_STORE:
            case 雇佣商店_购买道具:
            case 雇佣商店_购买道具0024:
            case 雇佣商店_购买道具0025:
            case 雇佣商店_购买道具0026:
                //case 雇佣商店_求购道具: 

                int item1 = slea.readByte();
                quantity = slea.readShort();

                shop = chr.getPlayerShop();
                if ((shop == null) || (shop.isOwner(chr)) || (item1 >= shop.getItems().size())) {
                    chr.dropMessage(1, "购买道具出现错误(1)");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                MaplePlayerShopItem tobuy = (MaplePlayerShopItem) shop.getItems().get(item1);
                if (tobuy == null) {
                    chr.dropMessage(1, "购买道具出现错误(2)");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                check = tobuy.bundles * quantity;
                long check2 = tobuy.price * quantity;
                long check3 = tobuy.item.getQuantity() * quantity;
                if ((check <= 0L) || (check2 > 9999999999L) || (check2 <= 0L) || (check3 > 32767L) || (check3 < 0L)) {
                    chr.dropMessage(1, "购买道具出现错误(3)");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if (chr.getMeso() - check2 < 0L) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if ((tobuy.bundles < quantity) || ((tobuy.bundles % quantity != 0) && (ItemConstants.isEquip(tobuy.item.getItemId()))) || (chr.getMeso() - check2 > 9999999999L) || (shop.getMeso() + check2 < 0L) || (shop.getMeso() + check2 > 9999999999L)) {
                    chr.dropMessage(1, "购买道具出现错误(4)");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if ((quantity >= 50) && (tobuy.item.getItemId() == 2340000)) {
                    c.setMonitored(true);
                }
                shop.buy(c, item1, quantity);
                break;
            case 雇佣商店_求购道具:
                chr.dropMessage(1, "当前服务器暂不支持求购道具.");
                break;
            case 雇佣商店_维护:
                slea.skip(1);
                byte type1 = slea.readByte();
                slea.skip(3);
                int obid = slea.readInt();
                if (type1 == 6) {
                    MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                    if (((ob instanceof IMaplePlayerShop)) && (chr.getPlayerShop() == null)) {
                        IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                        if ((ob instanceof HiredMerchant)) {
                            HiredMerchant merchant = (HiredMerchant) ips;
                            if ((merchant.isOwner(chr)) && (merchant.isOpen()) && (merchant.isAvailable())) {
                                merchant.setOpen(false);
                                merchant.removeAllVisitors(18, 1);
                                chr.setPlayerShop(ips);
                            } else if ((!merchant.isOpen()) || (!merchant.isAvailable())) {
                                chr.dropMessage(1, "主人正在整理商店物品\r\n请稍后再度光临！");
                            } else if (ips.getFreeSlot() == -1) {
                                chr.dropMessage(1, "店铺已达到最大人数\r\n请稍后再度光临！");
                            } else if (merchant.isInBlackList(chr.getName())) {
                                chr.dropMessage(1, "你被禁止进入该店铺");
                            }
                        }
                    }
                } else {
                    c.sendPacket(MaplePacketCreator.enableActions());
                }
                break;
            case 移除物品:
                slea.skip(1);
                int slot1 = slea.readShort();
                IMaplePlayerShop shop1 = chr.getPlayerShop();
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "移除商店道具: 道具数量 " + shop1.getItems().size() + " slot " + slot1);
                }
                if ((shop1 == null) || (!shop1.isOwner(chr))|| (shop1.getItems().size() <= 0) || (shop1.getItems().size() <= slot1) || (slot1 < 0)) {
                    return;
                }
                MaplePlayerShopItem item2 = (MaplePlayerShopItem) shop1.getItems().get(slot1);
                if ((item2 != null)
                        && (item2.bundles > 0)) {
                    Item item_get = item2.item.copy();
                    check = item2.bundles * item2.item.getQuantity();
                    if ((check < 0L) || (check > 32767L)) {
                        if (chr.isShowPacket()) {
                            chr.dropMessage(5, "移除商店道具出错: check " + check);
                        }
                        return;
                    }
                    item_get.setQuantity((short) (int) check);
                    if ((item_get.getQuantity() >= 50) && (item2.item.getItemId() == 2340000)) {
                        c.setMonitored(true);
                    }
                    if (MapleInventoryManipulator.checkSpace(c, item_get.getItemId(), item_get.getQuantity(), item_get.getOwner())) {
                        MapleInventoryManipulator.addFromDrop(c, item_get, false);
                        item2.bundles = 0;
                        shop1.removeFromSlot(slot1);
                    }
                }

                break;
            case 打开:
                // c.getPlayer().haveItem(mode, 1, false, true)
                shop = chr.getPlayerShop();
                if ((shop == null) || (!shop.isOwner(chr)) || (shop.getShopType() >= 3) || (shop.isAvailable())) {
                    break;
                }
                if (chr.getMap().allowPersonalShop()) {
                    if (c.getChannelServer().isShutdown()) {
                        chr.dropMessage(1, "服务器即将关闭维护，暂时无法进行此操作。.");
                        c.sendPacket(MaplePacketCreator.enableActions());
                        shop.closeShop(shop.getShopType() == 1, false);
                        return;
                    }
                    if (shop.getShopType() != 2) {
                        break;
                    }
                    shop.setOpen(true);
                    shop.setAvailable(true);
                    shop.update();
                } else {
                    c.getSession().close();
                }
                break;
            case 雇佣商店_关闭完成:
            case 雇佣商店_维护开启:
                shop = chr.getPlayerShop();
                if ((shop != null) && ((shop instanceof HiredMerchant)) && (shop.isOwner(chr)) && (shop.isAvailable())) {
                    shop.setOpen(true);
                    shop.saveItems();
                    shop.getMessages().clear();
                    shop.removeAllVisitors(-1, -1);
                }
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 雇佣商店_整理:
                IMaplePlayerShop imps = chr.getPlayerShop();
                if ((imps == null) || (!imps.isOwner(chr))) {
                    break;
                }
                for (int i = 0; i < imps.getItems().size(); i++) {
                    if (((MaplePlayerShopItem) imps.getItems().get(i)).bundles == 0) {
                        imps.getItems().remove(i);
                    }
                }
                if (chr.getMeso() + imps.getMeso() > 0) {
                    chr.gainMeso(imps.getMeso(), false);
                    MapleLogger.info("[雇佣] " + chr.getName() + " 雇佣整理获得金币: " + imps.getMeso() + " 时间: " + System.currentTimeMillis());
                    imps.setMeso(0);
                }
                break;
            case 雇佣商店_关闭:
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if ((merchant != null) && (merchant.getShopType() == 1) && (merchant.isOwner(chr))) {
                    merchant.removeAllVisitors(-1, -1);
                    chr.setPlayerShop(null);
                    merchant.closeShop(true, true);
                } else {
                    chr.dropMessage(1, "关闭商店出现未知错误.");
                    c.sendPacket(MaplePacketCreator.enableActions());
                }
                break;
            case 雇佣商店_错误提示:
                chr.dropMessage(1, "暂不支持管理员修改雇佣商店的名字.");
                c.sendPacket(MaplePacketCreator.enableActions());
                break;
            case 雇佣商店_查看访问名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                break;
            case 雇佣商店_查看黑名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                break;
            case 雇佣商店_添加黑名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                ((HiredMerchant) merchant).addBlackList(slea.readMapleAsciiString());
                break;
            case 雇佣商店_移除黑名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                ((HiredMerchant) merchant).removeBlackList(slea.readMapleAsciiString());
                break;
            case 雇佣商店_修改商店名称:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                String desc = slea.readMapleAsciiString();
                if (((HiredMerchant) merchant).canChangeName()) {
                    merchant.setDescription(desc);
                } else {
//                    c.sendPacket(MaplePacketCreator.craftMessage("还不能变更名称，还需要等待" + ((HiredMerchant) merchant).getChangeNameTimeLeft() + "秒。"));
                }
                break;
            default:
                if (ServerProperties.ShowPacket()) {
                    MapleLogger.info("玩家互动未知的操作类型: 0x" + StringUtil.getLeftPaddedStr(Integer.toHexString(mode).toUpperCase(), '0', 2) + " " + slea.toString());
                }
                c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}
