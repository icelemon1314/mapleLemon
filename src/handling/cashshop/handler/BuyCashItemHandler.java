package handling.cashshop.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleQuestStatus;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.ItemConstants;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.world.WorldFindService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashShop;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class BuyCashItemHandler {

    private static final Logger log = Logger.getLogger(BuyCashItemHandler.class);

    public static void BuyCashItem(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 73 05 00 01
        // 73 02 00 DB 96 98 00
        int action = slea.readByte() & 0xFF;
        CashShop cs = chr.getCashInventory();
        int sn;
        CashItemFactory cashinfo = CashItemFactory.getInstance();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        switch (action) {
            case 0x02: // 购买道具
                int toCharge = slea.readByte() + 1;
                int snCS = slea.readInt();

                CashItemInfo cItem = cashinfo.getItem(snCS);
                if (chr.isShowPacket()) {
                    FileoutputUtil.log(new StringBuilder().append("商城 => 购买 - 物品 ").append(snCS).append(" 是否为空 ").append(cItem == null).toString());
                }
                if (cItem != null) {
                    if (snCS == 92000046) {
                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买道具.");
                        return;
                    }
                    if ((cItem.getId() / 1000 == 5533) && (!cashinfo.hasRandomItem(cItem.getId()))) {
                        chr.dropMessage(1, "该道具暂时无法购买，因为找不到对应的箱子信息.");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if (cItem.getId() == 5451001 || cItem.getId() == 5065000 || cItem.getId() == 5065100) {
                        chr.dropMessage(1, "该道具禁止购买。");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if (chr.getCSPoints(toCharge) < cItem.getPrice()) {
                        chr.dropMessage(1, "点券余额不足");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if (!cItem.genderEquals(chr.getGender())) {
                        chr.dropMessage(1, "请确认角色性别是否错误");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if (cs.getItemsSize() >= 100) {
                        chr.dropMessage(1, "保管箱已满");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if ((cashinfo.isBlockedCashItemId(cItem.getId())) || (cashinfo.isBlockCashSnId(snCS))) {
                        chr.dropMessage(1, "该道具禁止购买。");
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    if (cItem.getPrice() <= 0) {
                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买道具.");
                        return;
                    }
                    if (chr.isShowPacket()) {
                        FileoutputUtil.log(new StringBuilder().append("商城 => 购买 - 点券类型 ").append(toCharge).append(" 减少 ").append(cItem.getPrice()).toString());
                    }
                    Item item = cs.toItem(cItem);

                    if ((item != null) && (item.getUniqueId() > 0) && (item.getItemId() == cItem.getId()) && (item.getQuantity() == cItem.getCount())) {
                        chr.modifyCSPoints(toCharge, -cItem.getPrice(), false);
                        if (ii.isCash(item.getItemId())) {
                            // 先直接放到背包了
                            //chr.getInventory(ItemConstants.getInventoryType(item.getItemId())).addItem(item);
                            cs.addToInventory(item);
                            c.getSession().write(MTSCSPacket.购买商城道具(item, cItem.getSN(), c.getAccID()));
                        } else {
                            FileoutputUtil.log(new StringBuilder().append("[作弊] ").append(chr.getName()).append(" 商城非法购买道具.道具: ").append(item.getItemId()).append(" - ").append(ii.getName(item.getItemId())).toString());
//                            AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买道具.");
                        }
                    } else {
                        chr.dropMessage(1, "购买道具出错 代码（2）");
                    }
                } else {
                    chr.dropMessage(1, "购买道具出错：不存在的SN号码！");
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x03: // 赠送给别人
                // 73 03
                // DB 96 98 00
                // 07 00 31 32 33 34 35 36 37 收礼物的人
                // 08 00 71 71 71 71 71 71 71 0A 留言
                chr.dropMessage(1, "暂不支持，直接选了点送礼吧！");
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x04:
                chr.dropMessage(1, "暂不支持，直接选了点送礼吧！");
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
//            case 0x05: //购物栏
//                chr.clearWishlist();
//                if (slea.available() < 40L) {
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                int[] wishlist = new int[12];
//                for (int i = 0; i < 12; i++) {
//                    wishlist[i] = slea.readInt();
//                }
//                chr.setWishlist(wishlist);
//                c.getSession().write(MTSCSPacket.商城购物车(chr, true));
//                break;
            case 0x05:
                // 73 05 00 01
                // 73 05 01 02
                toCharge = slea.readByte() + 1;
//                boolean coupon = slea.readByte() > 0;
//                if (coupon) {
//                    snCS = slea.readInt();
//                    cItem = CashItemFactory.getInstance().getItem(snCS);
//                    if (cItem == null) {
//                        chr.dropMessage(1, "未知错误");
//                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                        break;
//                    }
//                    int types = (cItem.getId() - 9110000) / 1000;
//                    MapleInventoryType type = MapleInventoryType.getByType((byte) types);
//                    if (chr.isShowPacket()) {
//                        FileoutputUtil.log(new StringBuilder().append("增加道具栏  snCS ").append(snCS).append(" 扩充: ").append(types).toString());
//                    }
//                    if ((chr.getCSPoints(toCharge) >= 1100) && (chr.getInventory(type).getSlotLimit() < 96)) {
//                        chr.modifyCSPoints(toCharge, -1100, false);
//                        chr.getInventory(type).addSlot((byte) 8);
//
//                        c.getSession().write(MTSCSPacket.扩充道具栏(type.getType(), chr.getInventory(type).getSlotLimit()));
//                    } else {
//                        chr.dropMessage(1, "扩充失败，点券余额不足或者栏位已超过上限。");
//                    }
//                } else {
                    MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                    if ((chr.getCSPoints(toCharge) >= 600) && (chr.getInventory(type).getSlotLimit() < 96)) {
                        chr.modifyCSPoints(toCharge, -600, false);
                        chr.getInventory(type).addSlot((byte) 4);

                        c.getSession().write(MTSCSPacket.扩充道具栏(type.getType(), chr.getInventory(type).getSlotLimit()));
                        chr.dropMessage(1, "扩充成功！");
                    } else {
                        chr.dropMessage(1, "扩充失败，点券余额不足或者栏位已超过上限。");
                    }
//                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x06: // 扩充仓库
                toCharge = slea.readByte() + 1;
                if (chr.getCSPoints(toCharge) >= 600) {
                    if (chr.getStorage().getSlots() < 96 - 4 ) {
                        chr.modifyCSPoints(toCharge,  -600, false);
                        chr.getStorage().increaseSlots((byte) (4));
                        chr.getStorage().saveToDB();
                        chr.dropMessage(1, new StringBuilder().append("仓库扩充成功，当前栏位: ").append(chr.getStorage().getSlots()).append(" 个。").toString());
                    } else {
                        chr.dropMessage(1, "仓库扩充失败，栏位已超过上限。.");
                    }
                } else {
                    chr.dropMessage(1, "仓库扩充失败，点券余额不足.");
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;

            case 0x09:
//                toCharge = slea.readByte() + 1;
//                sn = slea.readInt();
//                cItem = CashItemFactory.getInstance().getItem(sn);
                chr.dropMessage(1, "暂时不支持。");
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x0E:
                int uniqueId = (int) slea.readLong();
                Item item1 = cs.findByCashId(uniqueId);
                if (item1 == null) {
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    if (chr.isShowPacket()) {
                        FileoutputUtil.log("删除商城道具 - 道具为空 删除失败");
                    }
                    return;
                }
                cs.removeFromInventory(item1);
                c.getSession().write(MTSCSPacket.商城删除道具(uniqueId));
                break;
            case 0x0A: // 商城到背包 06 00 00 00 00 00 00 00 02 02 00
                item1 = cs.findByCashId((int) slea.readLong());
                byte itemType = slea.readByte();
                if (item1 == null) {
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (chr.getInventory(MapleInventoryType.getByType(itemType)).addItem(item1) == -1) {
                    break;
                }
                cs.removeFromInventory(item1);
                c.getSession().write(MTSCSPacket.商城到背包(item1));
                if (chr.isShowPacket()) {
                    FileoutputUtil.log("商城 => 背包 - 移动成功");
                }
                break;
            case 0xB: // 背包到商城
                int cashId = (int) slea.readLong();
                itemType = slea.readByte();
                MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(itemType));
                item1 = mi.findByUniqueId(cashId);
                if (chr.isShowPacket()) {
                    FileoutputUtil.log(new StringBuilder().append("背包 => 商城 - 道具是否为空 ").append(item1 == null).toString());
                }
                if (item1 == null) {
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (cs.getItemsSize() < 100) {
                    snCS = cashinfo.getSnFromId(item1.getItemId());
                    cs.addToInventory(item1);
                    mi.removeSlot(item1.getPosition());
                    c.getSession().write(MTSCSPacket.背包到商城(item1, c.getAccID(), snCS));
                    if (chr.isShowPacket()) {
                        FileoutputUtil.log("背包 => 商城 - 移动成功");
                    }
                } else {
                    chr.dropMessage(1, "移动失败。");
                }
                break;
            case 0x20:
                slea.readMapleAsciiString();
                toCharge = 2;
                uniqueId = (int) slea.readLong();
                item1 = cs.findByCashId(uniqueId);
                if (item1 == null) {
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                snCS = cashinfo.getSnFromId(item1.getItemId());
                cItem = cashinfo.getItem(snCS);
                if ((cItem == null) || (cashinfo.isBlockRefundableItemId(item1.getItemId()))) {
                    if (chr.isShowPacket()) {
                        if (cItem == null) {
                            chr.dropMessage(1, new StringBuilder().append("换购失败:\r\n道具是否为空: ").append(cItem == null).toString());
                        } else {
                            chr.dropMessage(1, new StringBuilder().append("换购失败:\r\n道具禁止回购: ").append(cashinfo.isBlockRefundableItemId(item1.getItemId())).toString());
                        }
                    } else {
                        chr.dropMessage(1, "换购失败，当前道具不支持换购。");
                    }
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (!ii.isCash(cItem.getId())) {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法换购道具.");
                    return;
                }
                int Money = cItem.getPrice() / 10 * 3;
                cs.removeFromInventory(item1);
                chr.modifyCSPoints(toCharge, Money, false);
                c.getSession().write(MTSCSPacket.商城换购道具(uniqueId, Money));
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x23:
            case 0x2A:
                slea.readMapleAsciiString();
                slea.skip(1);
                toCharge = 1;
                snCS = slea.readInt();
                CashItemInfo item = cashinfo.getItem(snCS);
                slea.skip(4);
                String partnerName = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                if ((item == null) || (!ItemConstants.isEffectRing(item.getId())) || (chr.getCSPoints(toCharge) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
                    c.getSession().write(MTSCSPacket.商城错误提示(0));
                    return;
                }
                if (!item.genderEquals(chr.getGender())) {
                    c.getSession().write(MTSCSPacket.商城错误提示(7));
                    return;
                }
                if (chr.getCashInventory().getItemsSize() >= 100) {
                    c.getSession().write(MTSCSPacket.商城错误提示(24));
                    return;
                }
                if (!ii.isCash(item.getId())) {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买戒指道具.");
                    return;
                }
                if ((cashinfo.isBlockedCashItemId(item.getId())) || (cashinfo.isBlockCashSnId(snCS))) {
                    chr.dropMessage(1, "该道具禁止购买。");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                Triple info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
                if ((info == null) || (((Integer) info.getLeft()) <= 0)) {
                    c.getSession().write(MTSCSPacket.商城错误提示(7));
                } else if ((((Integer) info.getMid()) == c.getAccID()) || (((Integer) info.getLeft()) == chr.getId())) {
                    c.getSession().write(MTSCSPacket.商城错误提示(6));
                } else {
                    if ((((Integer) info.getRight()) == chr.getGender()) && (action == 35)) {
                        c.getSession().write(MTSCSPacket.商城错误提示(26));
                        return;
                    }
                    int err = MapleRing.createRing(item.getId(), chr, partnerName, msg, ((Integer) info.getLeft()).intValue(), item.getSN());
                    if (err != 1) {
                        c.getSession().write(MTSCSPacket.商城错误提示(1));
                        return;
                    }
                    chr.modifyCSPoints(toCharge, -item.getPrice(), false);
                    c.getSession().write(MTSCSPacket.商城送礼(item.getId(), item.getCount(), partnerName));
                    chr.sendNote(partnerName, new StringBuilder().append(partnerName).append(" 您已收到").append(chr.getName()).append("送给您的礼物，请进入现金商城查看！").toString());
                    int chz = WorldFindService.getInstance().findChannel(partnerName);
                    if (chz > 0) {
                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                        if (receiver != null) {
                            receiver.showNote();
                        }
                    }
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x25: // 礼包购买
                toCharge = slea.readByte() + 1;
                int snCsId = slea.readInt();
                int count = slea.readInt();
                chr.dropMessage(1, "礼包购买未开放.");
//                if ((snCsId == 10200551) || (snCsId == 10200552) || (snCsId == 10200553)) {
//                    chr.dropMessage(1, "当前服务器未开放购买商城活动栏里面的道具.");
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (cashinfo.isBlockCashSnId(snCsId)) {
//                    chr.dropMessage(1, "该礼包禁止购买.");
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                item = cashinfo.getItem(snCsId);
//                List packageIds = null;
//                if (item != null) {
//                    packageIds = cashinfo.getPackageItems(item.getId());
//                }
//                if ((item == null) || (packageIds == null)) {
//                    msg = "未知错误";
//                    if (chr.isAdmin()) {
//                        if (item == null) {
//                            msg = new StringBuilder().append(msg).append("\r\n\r\n 礼包道具信息为空").toString();
//                        }
//                        if (packageIds == null) {
//                            msg = new StringBuilder().append(msg).append("\r\n\r\n 礼包道具里面的物品道具为空").toString();
//                        }
//                    }
//                    chr.dropMessage(1, msg);
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (chr.getCSPoints(toCharge) < item.getPrice()) {
//                    c.getSession().write(MTSCSPacket.商城错误提示(3));
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (!item.genderEquals(c.getPlayer().getGender())) {
//                    chr.dropMessage(1, "性别不符合");
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (c.getPlayer().getCashInventory().getItemsSize() >= 100 - packageIds.size()) {
//                    c.getSession().write(MTSCSPacket.商城错误提示(24));
//                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (item.getPrice() <= 0) {
//                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买礼包道具.");
//                    return;
//                }
//                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
//                Map packageItems = new HashMap();
//                for (Iterator i$ = packageIds.iterator(); i$.hasNext();) {
//                    int i = ((Integer) i$.next()).intValue();
//                    CashItemInfo cii = cashinfo.getSimpleItem(i);
//                    if (cii == null) {
//                        continue;
//                    }
//                    Item itemz = chr.getCashInventory().toItem(cii);
//                    if ((itemz == null) || (itemz.getUniqueId() <= 0)
//                            || (cashinfo.isBlockedCashItemId(item.getId()))) {
//                        continue;
//                    }
//                    if (!ii.isCash(itemz.getItemId())) {
//                        FileoutputUtil.log(new StringBuilder().append("[作弊] ").append(chr.getName()).append(" 商城非法购买礼包道具.道具: ").append(itemz.getItemId()).append(" - ").append(ii.getName(itemz.getItemId())).toString());
//                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买礼包道具.");
//                        continue;
//                    }
//                    packageItems.put(Integer.valueOf(i), itemz);
//                    chr.getCashInventory().addToInventory(itemz);
//                    addCashshopLog(chr, snCsId, itemz.getItemId(), toCharge, item.getPrice(), itemz.getQuantity(), new StringBuilder().append(chr.getName()).append(" 购买礼包: ").append(ii.getName(itemz.getItemId())).append(" - ").append(i).toString());
//                }
//                c.getSession().write(MTSCSPacket.商城购买礼包(packageItems, c.getAccID()));
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x26:
                slea.readMapleAsciiString();
                snCsId = slea.readInt();
                item = cashinfo.getItem(snCsId);
                partnerName = slea.readMapleAsciiString();
                msg = slea.readMapleAsciiString();
                if ((item == null) || (chr.getCSPoints(1) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
                    c.getSession().write(MTSCSPacket.商城错误提示(3));
                    return;
                }
                if (cashinfo.isBlockCashSnId(snCsId)) {
                    chr.dropMessage(1, "该礼包禁止购买.");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
                if ((info == null) || (((Integer) info.getLeft()) <= 0)) {
                    c.getSession().write(MTSCSPacket.商城错误提示(7));
                } else if ((((Integer) info.getLeft()) == chr.getId()) || (((Integer) info.getMid()) == c.getAccID())) {
                    c.getSession().write(MTSCSPacket.商城错误提示(6));
                } else if (!item.genderEquals(((Integer) info.getRight()))) {
                    c.getSession().write(MTSCSPacket.商城错误提示(8));
                } else {
                    if (item.getPrice() <= 0) {
                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买礼包道具.");
                        return;
                    }
                    chr.getCashInventory().gift(((Integer) info.getLeft()), chr.getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
                    chr.modifyCSPoints(1, -item.getPrice(), false);

                    c.getSession().write(MTSCSPacket.商城送礼包(item.getId(), item.getCount(), partnerName));
                    chr.sendNote(partnerName, new StringBuilder().append(partnerName).append(" 您已收到").append(chr.getName()).append("送给您的礼物，请进入现金商城查看！").toString());
                    int chz = WorldFindService.getInstance().findChannel(partnerName);
                    if (chz > 0) {
                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                        if (receiver != null) {
                            receiver.showNote();
                        }
                    }
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x27:
                item = cashinfo.getItem(slea.readInt());
                if ((item == null) || (!MapleItemInformationProvider.getInstance().isQuestItem(item.getId()))) {
                    chr.dropMessage(1, "该道具不是任务物品");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if ((chr.getMeso() < item.getPrice()) || (item.getPrice() <= 0)) {
                    chr.dropMessage(1, "金币不足");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (chr.getItemQuantity(item.getId()) > 0) {
                    chr.dropMessage(1, "你已经有这个道具\r\n不能购买.");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (chr.getInventory(ItemConstants.getInventoryType(item.getId())).getNextFreeSlot() < 0) {
                    chr.dropMessage(1, "背包空间不足");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if (cashinfo.isBlockedCashItemId(item.getId())) {
                    chr.dropMessage(1, CashShopServer.getCashBlockedMsg(item.getId()));
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                if ((item.getId() == 4031063) || (item.getId() == 4031191) || (item.getId() == 4031192)) {
                    byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null, new StringBuilder().append("商城: 任务物品 在 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                    if (pos < 0) {
                        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                        return;
                    }
                    chr.gainMeso(-item.getPrice(), false);
                    c.getSession().write(MTSCSPacket.updataMeso(chr));
                    c.getSession().write(MTSCSPacket.商城购买任务道具(item.getPrice(), (short) item.getCount(), pos, item.getId()));
                } else {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买任务道具.");
                }
                break;
            case 0x32:
                slea.readByte();
                snCS = slea.readInt();
                slea.readInt();
                if ((snCS == 50200031) && (chr.getCSPoints(1) >= 500)) {
                    chr.modifyCSPoints(1, -500, false);
                    chr.modifyCSPoints(2, 500, false);
                    chr.dropMessage(1, "兑换抵用卷成功");
                } else if ((snCS == 50200032) && (chr.getCSPoints(1) >= 1000)) {
                    chr.modifyCSPoints(1, -1000, false);
                    chr.modifyCSPoints(2, 1000, false);
                    chr.dropMessage(1, "兑换抵用卷成功");
                } else if ((snCS == 50200033) && (chr.getCSPoints(1) >= 5000)) {
                    chr.modifyCSPoints(1, -5000, false);
                    chr.modifyCSPoints(2, 5000, false);
                    chr.dropMessage(1, "兑换抵用卷成功");
                } else {
                    chr.dropMessage(1, "没有找到这个道具的信息。");
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x34:
                c.getSession().write(MTSCSPacket.redeemResponse());
                break;
            case 0x41:
                uniqueId = (int) slea.readLong();
                Item boxItem = cs.findByCashId((int) uniqueId);
                if ((boxItem == null) || (!cashinfo.hasRandomItem(boxItem.getItemId()))) {
                    chr.dropMessage(1, "打开箱子失败，服务器找不到对应的道具信息。");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                List boxItemSNs = cashinfo.getRandomItem(boxItem.getItemId());
                if (boxItemSNs.isEmpty()) {
                    chr.dropMessage(1, "打开箱子失败，服务器找不到对应的道具信息。");
                    c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                    return;
                }
                snCS = ((Integer) boxItemSNs.get(Randomizer.nextInt(boxItemSNs.size())));
                cItem = cashinfo.getItem(snCS);
                if (cItem != null) {
                    item1 = cs.toItem(cItem);
                    if ((item1 != null) && (item1.getUniqueId() > 0) && (item1.getItemId() == cItem.getId()) && (item1.getQuantity() == cItem.getCount())) {
                        if (chr.getInventory(ItemConstants.getInventoryType(item1.getItemId())).addItem(item1) != -1) {
                            cs.removeFromInventory(boxItem);
                            c.getSession().write(MTSCSPacket.商城打开箱子(item1, Long.valueOf(uniqueId)));
                        } else {
                            chr.dropMessage(1, "打开箱子失败，请确认背包是否有足够的空间。");
                        }
                    }
                }
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                break;
            default:
                label1652:
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));
                FileoutputUtil.log(new StringBuilder().append("商城操作未知的操作类型: 0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2)).append(" ").append(slea.toString()).toString());
        }
    }

    public static void 商城送礼(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readMapleAsciiString();
        int snCS = slea.readInt();
        CashItemFactory cashinfo = CashItemFactory.getInstance();
        CashItemInfo item = cashinfo.getItem(snCS);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        String partnerName = slea.readMapleAsciiString();
        String msg = slea.readMapleAsciiString();
        if (snCS == 92000046) {
            AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买道具.");
            return;
        }
        if ((cashinfo.isBlockedCashItemId(item.getId())) || (cashinfo.isBlockCashSnId(snCS))) {
            chr.dropMessage(1, "该道具禁止购买。");
            c.getSession().write(MTSCSPacket.刷新点券信息(chr));
            return;
        }

        if ((item == null) || (chr.getCSPoints(1) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
            c.getSession().write(MTSCSPacket.商城错误提示(3));

            return;
        }
        Triple info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
        if ((info == null) || (((Integer) info.getLeft()) <= 0)) {
            c.getSession().write(MTSCSPacket.商城错误提示(7));
        } else if ((((Integer) info.getLeft()) == chr.getId()) || (((Integer) info.getMid()) == c.getAccID())) {
            c.getSession().write(MTSCSPacket.商城错误提示(6));
        } else if (!item.genderEquals(((Integer) info.getRight()))) {
            c.getSession().write(MTSCSPacket.商城错误提示(8));
        } else {
            if (!ii.isCash(item.getId())) {
                FileoutputUtil.log(new StringBuilder().append("[作弊] ").append(chr.getName()).append(" 商城非法购买礼物道具.道具: ").append(item.getId()).append(" - ").append(ii.getName(item.getId())).toString());
                chr.dropMessage(1, "购买商城礼物道具出现错误.");
                c.getSession().write(MTSCSPacket.刷新点券信息(chr));

                return;
            }
            if (item.getPrice() <= 0) {
                AutobanManager.getInstance().autoban(chr.getClient(), "商城非法赠送礼包道具.");
                return;
            }

            chr.getCashInventory().gift(((Integer) info.getLeft()), chr.getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
            chr.modifyCSPoints(1, -item.getPrice(), false);
            c.getSession().write(MTSCSPacket.商城送礼(item.getId(), item.getCount(), partnerName));
            c.getSession().write(MTSCSPacket.刷新点券信息(chr));
            chr.sendNote(partnerName, new StringBuilder().append(partnerName).append(" 您已收到").append(chr.getName()).append("送给您的礼物，请进入现金商城查看！").toString());
            int chz = WorldFindService.getInstance().findChannel(partnerName);
            if (chz > 0) {
                MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                if (receiver != null) {
                    receiver.showNote();
                }
            }
        }
    }

    private static MapleInventoryType getInventoryType(int id) {
        switch (id) {
            case 50200075:
                return MapleInventoryType.EQUIP;
            case 50200074:
                return MapleInventoryType.USE;
            case 50200073:
                return MapleInventoryType.ETC;
        }
        return MapleInventoryType.UNDEFINED;
    }

    public static void openAvatarRandomBox(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        CashShop cs = chr.getCashInventory();
        Item item = cs.findByCashId((int) slea.readLong());
        if ((item == null) || (item.getItemId() != 5222036)) {
            c.getSession().write(MTSCSPacket.刷新点券信息(chr));
            return;
        }
        chr.dropMessage(1, "当前游戏不支持此功能");
        c.getSession().write(MTSCSPacket.刷新点券信息(chr));
    }
}
