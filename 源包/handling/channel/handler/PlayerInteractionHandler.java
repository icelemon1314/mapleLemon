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
import org.apache.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.ServerProperties;
import server.maps.FieldLimitType;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MapleMiniGame;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PlayerShopPacket;

public class PlayerInteractionHandler {

    private static final Logger log = Logger.getLogger(PlayerInteractionHandler.class);

    public static void PlayerInteraction(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte mode = slea.readByte();
        InteractionOpcode action = InteractionOpcode.getByAction(mode);
        if (chr == null || (action == null)) {
            FileoutputUtil.log("玩家互动未知的操作类型: " + mode + " " + slea.toString());
            c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(MaplePacketCreator.enableActions());
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
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (((createType == 1) || (createType == 2)) && ((FieldLimitType.Minigames.check(chr.getMap().getFieldLimit())) || (chr.getMap().allowPersonalShop()))) {
                        chr.dropMessage(1, "无法在这个地方使用.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }

                    String desc = slea.readMapleAsciiString();
                    String pass = "";
                    if (slea.readByte() > 0) {
                        pass = slea.readMapleAsciiString();
                    }
                    if ((createType == 1) || (createType == 2)) {
                        int piece = slea.readByte();
                        int itemId = createType == 1 ? 4080000 + piece : 4080100;
                        if ((!chr.haveItem(itemId)) || ((c.getPlayer().getMapId() >= 910000001) && (c.getPlayer().getMapId() <= 910000022))) {
                            return;
                        }
                        MapleMiniGame game = new MapleMiniGame(chr, itemId, desc, pass, createType);
                        game.setPieceType(piece);
                        chr.setPlayerShop(game);
                        game.setAvailable(true);
                        game.setOpen(true);
                        game.send(c);
                        chr.getMap().addMapObject(game);
                        game.update();
                    } else if (chr.getMap().allowPersonalShop()) {
                        Item shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) slea.readShort());
                        if ((shop == null) || (shop.getQuantity() <= 0) || (shop.getItemId() != slea.readInt()) || (c.getPlayer().getMapId() < 910000001) || (c.getPlayer().getMapId() > 910000022)) {
                            return;
                        }
                        if (createType == 4) {
                            MaplePlayerShop mps = new MaplePlayerShop(chr, shop.getItemId(), desc);
                            chr.setPlayerShop(mps);
                            chr.getMap().addMapObject(mps);
                            c.getSession().write(PlayerShopPacket.getPlayerStore(chr, true));
                        } else if (HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) {
                            HiredMerchant merch = new HiredMerchant(chr, shop.getItemId(), desc);
                            chr.setPlayerShop(merch);
                            chr.getMap().addMapObject(merch);
                            c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merch, true));
                        }
                    }
                }
                break;
            case 交易邀请:
                if (chr.getMap() == null) {
                    return;
                }
                MapleCharacter chrr = chr.getMap().getCharacterById(slea.readInt());
                if ((chrr == null) || (c.getChannelServer().isShutdown()) || (chrr.hasBlockedInventory())) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                MapleTrade.inviteTrade(chr, chrr);
                break;
            case 拒绝邀请:
                MapleTrade.declineTrade(chr);
                break;
            case 访问:
                if (c.getChannelServer().isShutdown()) {
                    c.getSession().write(MaplePacketCreator.enableActions());
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
                                c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                            } else if ((!merchant.isOpen()) || (!merchant.isAvailable())) {
                                chr.dropMessage(1, "主人正在整理商店物品\r\n请稍后再度光临！");
                            } else if (ips.getFreeSlot() == -1) {
                                chr.dropMessage(1, "店铺已达到最大人数\r\n请稍后再度光临！");
                            } else if (merchant.isInBlackList(chr.getName())) {
                                chr.dropMessage(1, "你被禁止进入该店铺");
                            } else {
                                chr.setPlayerShop(ips);
                                merchant.addVisitor(chr);
                                c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                            }

                        } else if (((ips instanceof MaplePlayerShop)) && (((MaplePlayerShop) ips).isBanned(chr.getName()))) {
                            chr.dropMessage(1, "你被禁止进入该店铺");
                        } else if ((ips.getFreeSlot() < 0) || (ips.getVisitorSlot(chr) > -1) || (!ips.isOpen()) || (!ips.isAvailable())) {
                            c.getSession().write(PlayerShopPacket.getMiniGameFull());
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
                            if ((ips instanceof MapleMiniGame)) {
                                ((MapleMiniGame) ips).send(c);
                            } else {
                                c.getSession().write(PlayerShopPacket.getPlayerStore(chr, false));
                            }
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
                    ips.broadcastToVisitors(PlayerShopPacket.shopChat(chr.getName() + " : " + message, ips.getVisitorSlot(chr)));
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
                        c.getSession().write(MaplePacketCreator.enableActions());
                        shop.closeShop(shop.getShopType() == 1, false);
                        return;
                    }
                    if ((shop.getShopType() == 1) && (HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false))) {
                        HiredMerchant merchant = (HiredMerchant) shop;
                        merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
                        merchant.setOpen(true);
                        merchant.setAvailable(true);
                        shop.saveItems();
                        chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
                        chr.setPlayerShop(null);
                    } else {
                        if (shop.getShopType() != 2) {
                            break;
                        }
                        shop.setOpen(true);
                        shop.setAvailable(true);
                        shop.update();
                    }
                } else {
                    chr.getClient().disconnect(true, false);
                    c.getSession().close(true);
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
                    c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                shop = chr.getPlayerShop();
                if ((shop == null) || (!shop.isOwner(chr)) || ((shop instanceof MapleMiniGame))) {
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
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (ItemFlag.不可交易.check(flag) && !ItemFlag.KARMA_USE.check(flag)) {
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    if ((ii.isDropRestricted(ivItem.getItemId()) || ii.isAccountShared(ivItem.getItemId())) && !ItemFlag.KARMA_USE.check(flag)) {
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }

                    if (ivItem.getItemId() == 4000463) {
                        chr.dropMessage(1, "该道具无法进行贩卖.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    if ((bundles_perbundle >= 50) && (ivItem.getItemId() == 2340000)) {
                        c.setMonitored(true);
                    }
                    if (ItemConstants.getLowestPrice(ivItem.getItemId()) > price) {
                        c.getPlayer().dropMessage(1, "The lowest you can sell this for is " + ItemConstants.getLowestPrice(ivItem.getItemId()));
                        c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
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
                if ((shop == null) || (shop.isOwner(chr)) || ((shop instanceof MapleMiniGame)) || (item1 >= shop.getItems().size())) {
                    chr.dropMessage(1, "购买道具出现错误(1)");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                MaplePlayerShopItem tobuy = (MaplePlayerShopItem) shop.getItems().get(item1);
                if (tobuy == null) {
                    chr.dropMessage(1, "购买道具出现错误(2)");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                check = tobuy.bundles * quantity;
                long check2 = tobuy.price * quantity;
                long check3 = tobuy.item.getQuantity() * quantity;
                if ((check <= 0L) || (check2 > 9999999999L) || (check2 <= 0L) || (check3 > 32767L) || (check3 < 0L)) {
                    chr.dropMessage(1, "购买道具出现错误(3)");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (chr.getMeso() - check2 < 0L) {
                    c.getSession().write(PlayerShopPacket.Merchant_Buy_Error((byte) 2));
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if ((tobuy.bundles < quantity) || ((tobuy.bundles % quantity != 0) && (ItemConstants.isEquip(tobuy.item.getItemId()))) || (chr.getMeso() - check2 > 9999999999L) || (shop.getMeso() + check2 < 0L) || (shop.getMeso() + check2 > 9999999999L)) {
                    chr.dropMessage(1, "购买道具出现错误(4)");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if ((quantity >= 50) && (tobuy.item.getItemId() == 2340000)) {
                    c.setMonitored(true);
                }
                shop.buy(c, item1, quantity);
                shop.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(shop));
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
                                c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
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
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
                break;
            case 移除物品:
                slea.skip(1);
                int slot1 = slea.readShort();
                IMaplePlayerShop shop1 = chr.getPlayerShop();
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "移除商店道具: 道具数量 " + shop1.getItems().size() + " slot " + slot1);
                }
                if ((shop1 == null) || (!shop1.isOwner(chr)) || ((shop1 instanceof MapleMiniGame)) || (shop1.getItems().size() <= 0) || (shop1.getItems().size() <= slot1) || (slot1 < 0)) {
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

                c.getSession().write(PlayerShopPacket.shopItemUpdate(shop1));
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
                        c.getSession().write(MaplePacketCreator.enableActions());
                        shop.closeShop(shop.getShopType() == 1, false);
                        return;
                    }
                    if ((shop.getShopType() == 1) && (HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false))) {
                        HiredMerchant merchant = (HiredMerchant) shop;
                        merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
                        merchant.setOpen(true);
                        merchant.setAvailable(true);
                        shop.saveItems();
                        chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
                        chr.setPlayerShop(null);
//                        c.getActive().finished(chr, activeEvent.activeType.开设雇佣商店);
                    } else {
                        if (shop.getShopType() != 2) {
                            break;
                        }
                        shop.setOpen(true);
                        shop.setAvailable(true);
                        shop.update();
                    }
                } else {
                    c.getSession().close(true);
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
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            case 雇佣商店_整理:
                IMaplePlayerShop imps = chr.getPlayerShop();
                if ((imps == null) || (!imps.isOwner(chr)) || ((imps instanceof MapleMiniGame))) {
                    break;
                }
                for (int i = 0; i < imps.getItems().size(); i++) {
                    if (((MaplePlayerShopItem) imps.getItems().get(i)).bundles == 0) {
                        imps.getItems().remove(i);
                    }
                }
                if (chr.getMeso() + imps.getMeso() > 0) {
                    chr.gainMeso(imps.getMeso(), false);
                    FileoutputUtil.log("[雇佣] " + chr.getName() + " 雇佣整理获得金币: " + imps.getMeso() + " 时间: " + FileoutputUtil.CurrentReadable_Date());
                    FileoutputUtil.hiredMerchLog(chr.getName(), "雇佣整理获得金币: " + imps.getMeso());
                    imps.setMeso(0);
                }
                c.getSession().write(PlayerShopPacket.shopItemUpdate(imps));
                break;
            case 雇佣商店_关闭:
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if ((merchant != null) && (merchant.getShopType() == 1) && (merchant.isOwner(chr))) {
                    c.getSession().write(PlayerShopPacket.hiredMerchantOwnerLeave());
                    merchant.removeAllVisitors(-1, -1);
                    chr.setPlayerShop(null);
                    merchant.closeShop(true, true);
                } else {
                    chr.dropMessage(1, "关闭商店出现未知错误.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
                break;
            case 雇佣商店_错误提示:
                chr.dropMessage(1, "暂不支持管理员修改雇佣商店的名字.");
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            case 雇佣商店_查看访问名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                ((HiredMerchant) merchant).sendVisitor(c);
                break;
            case 雇佣商店_查看黑名单:
                merchant = chr.getPlayerShop();
                if ((merchant == null) || (merchant.getShopType() != 1) || (!merchant.isOwner(chr))) {
                    break;
                }
                ((HiredMerchant) merchant).sendBlackList(c);
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
                    c.getSession().write(MaplePacketCreator.craftMessage("还不能变更名称，还需要等待" + ((HiredMerchant) merchant).getChangeNameTimeLeft() + "秒。"));
                }
                break;
            case GIVE_UP:
                IMaplePlayerShop ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                MapleMiniGame game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
                game.nextLoser();
                game.setOpen(true);
                game.update();
                game.checkExitAfterGame();
                break;
            case EXPEL:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))
                        || (!((MapleMiniGame) ips).isOpen())) {
                    break;
                }
                ips.removeAllVisitors(3, 1);
                break;
            case READY:
            case UN_READY:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if ((!game.isOwner(chr)) && (game.isOpen())) {
                    game.setReady(game.getVisitorSlot(chr));
                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game.isReady(game.getVisitorSlot(chr))));
                }
                break;
            case START:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if ((game.isOwner(chr)) && (game.isOpen())) {
                    for (int i = 1; i < ips.getSize(); i++) {
                        if (!game.isReady(i)) {
                            return;
                        }
                    }
                    game.setGameType();
                    game.shuffleList();
                    if (game.getGameType() == 1) {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game.getLoser()));
                    } else {
                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game, game.getLoser()));
                    }
                    game.setOpen(false);
                    game.update();
                }
                break;
            case REQUEST_TIE:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                if (game.isOwner(chr)) {
                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie(), false);
                } else {
                    game.getMCOwner().getClient().getSession().write(PlayerShopPacket.getMiniGameRequestTie());
                }
                game.setRequestedTie(game.getVisitorSlot(chr));
                break;
            case ANSWER_TIE:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                if ((game.getRequestedTie() > -1) && (game.getRequestedTie() != game.getVisitorSlot(chr))) {
                    if (slea.readByte() > 0) {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 1, game.getRequestedTie()));
                        game.nextLoser();
                        game.setOpen(true);
                        game.update();
                        game.checkExitAfterGame();
                    } else {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
                    }
                    game.setRequestedTie(-1);
                }
                break;
            case SKIP:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                if (game.getLoser() != ips.getVisitorSlot(chr)) {
                    ips.broadcastToVisitors(PlayerShopPacket.shopChat("Turn could not be skipped by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + ips.getVisitorSlot(chr), ips.getVisitorSlot(chr)));
                    return;
                }
                ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
                game.nextLoser();
                break;
            case MOVE_OMOK:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                if (game.getLoser() != game.getVisitorSlot(chr)) {
                    game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
                    return;
                }
                game.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
                break;
            case SELECT_CARD:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                if (game.getLoser() != game.getVisitorSlot(chr)) {
                    game.broadcastToVisitors(PlayerShopPacket.shopChat("Card could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
                    return;
                }
                if (slea.readByte() != game.getTurn()) {
                    game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr) + " Turn: " + game.getTurn(), game.getVisitorSlot(chr)));
                    return;
                }
                int slot2 = slea.readByte();
                int turn = game.getTurn();
                int fs = game.getFirstSlot();
                if (turn == 1) {
                    game.setFirstSlot(slot2);
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot2, fs, turn), false);
                    } else {
                        game.getMCOwner().getClient().getSession().write(PlayerShopPacket.getMatchCardSelect(turn, slot2, fs, turn));
                    }
                    game.setTurn(0);
                    return;
                }
                if ((fs > 0) && (game.getCardId(fs + 1) == game.getCardId(slot2 + 1))) {
                    game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot2, fs, game.isOwner(chr) ? 2 : 3));
                    game.setPoints(game.getVisitorSlot(chr));
                } else {
                    game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot2, fs, game.isOwner(chr) ? 0 : 1));
                    game.nextLoser();
                }
                game.setTurn(1);
                game.setFirstSlot(0);
                break;
            case EXIT_AFTER_GAME:
            case CANCEL_EXIT:
                ips = chr.getPlayerShop();
                if ((ips == null) || (!(ips instanceof MapleMiniGame))) {
                    break;
                }
                game = (MapleMiniGame) ips;
                if (game.isOpen()) {
                    break;
                }
                game.setExitAfter(chr);
                game.broadcastToVisitors(PlayerShopPacket.getMiniGameExitAfter(game.isExitAfter(chr)));
                break;
            default:
                if (ServerProperties.ShowPacket()) {
                    FileoutputUtil.log("玩家互动未知的操作类型: 0x" + StringUtil.getLeftPaddedStr(Integer.toHexString(mode).toUpperCase(), '0', 2) + " " + slea.toString());
                }
                c.getSession().write(MaplePacketCreator.enableActions());
        }
    }
}
