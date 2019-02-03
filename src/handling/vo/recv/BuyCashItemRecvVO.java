package handling.vo.recv;


import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.*;
import constants.ItemConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.vo.MaplePacketRecvVO;
import handling.world.WorldFindService;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashShop;
import tools.MapleLogger;
import tools.StringUtil;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

import java.util.List;

public class BuyCashItemRecvVO extends MaplePacketRecvVO {

    Integer action;
    Integer itemPrice;
    Integer itemSn;
    MapleInventoryType extendType;  // 扩充类型
    Long cashId;

    @Override
    public void decodePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 73 05 00 01
        // 73 02 00 DB 96 98 00
        action = slea.readByte() & 0xFF;
        switch (action) {
            case 0x02: // 购买道具
                itemPrice = slea.readByte() + 1;
                itemSn = slea.readInt();
            case 0x03: // 赠送给别人
                // 73 03
                // DB 96 98 00
                // 07 00 31 32 33 34 35 36 37 收礼物的人
                // 08 00 71 71 71 71 71 71 71 0A 留言
//                chr.dropMessage(1, "暂不支持，直接选了点送礼吧！");
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x04:
//                chr.dropMessage(1, "暂不支持，直接选了点送礼吧！");
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x05:
                // 73 05 00 01
                // 73 05 01 02
                itemPrice = slea.readByte() + 1;
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                break;
            case 0x06: // 扩充仓库
                itemPrice = slea.readByte() + 1;
                break;

            case 0x09:
//                toCharge = slea.readByte() + 1;
//                sn = slea.readInt();
//                cItem = CashItemFactory.getInstance().getItem(sn);
//                chr.dropMessage(1, "暂时不支持。");
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x0E:
                cashId = slea.readLong();
//                Item item1 = cs.findByCashId(uniqueId);
//                if (item1 == null) {
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    if (chr.isShowPacket()) {
//                        MapleLogger.info("删除商城道具 - 道具为空 删除失败");
//                    }
//                    return;
//                }
//                cs.removeFromInventory(item1);
//                c.sendPacket(MTSCSPacket.商城删除道具(uniqueId));
                break;
            case 0x0A: // 商城到背包 06 00 00 00 00 00 00 00 02 02 00
                cashId = slea.readLong();
                byte itemType = slea.readByte();
//                if (item1 == null) {
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (chr.getInventory(MapleInventoryType.getByType(itemType)).addItem(item1) == -1) {
//                    break;
//                }
//                cs.removeFromInventory(item1);
//                c.sendPacket(MTSCSPacket.商城到背包(item1));
//                if (chr.isShowPacket()) {
//                    MapleLogger.info("商城 => 背包 - 移动成功");
//                }
                break;
            case 0xB: // 背包到商城
                int cashId = (int) slea.readLong();
                itemType = slea.readByte();
                MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(itemType));
//                item1 = mi.findByUniqueId(cashId);
//                if (chr.isShowPacket()) {
//                    MapleLogger.info(new StringBuilder().append("背包 => 商城 - 道具是否为空 ").append(item1 == null).toString());
//                }
//                if (item1 == null) {
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (cs.getItemsSize() < 100) {
//                    snCS = cashinfo.getSnFromId(item1.getItemId());
//                    cs.addToInventory(item1);
//                    mi.removeSlot(item1.getPosition());
//                    c.sendPacket(MTSCSPacket.背包到商城(item1, c.getAccID(), snCS));
//                    if (chr.isShowPacket()) {
//                        MapleLogger.info("背包 => 商城 - 移动成功");
//                    }
//                } else {
//                    chr.dropMessage(1, "移动失败。");
//                }
                break;
            case 0x20:
                slea.readMapleAsciiString();
                toCharge = 2;
                uniqueId = (int) slea.readLong();
//                item1 = cs.findByCashId(uniqueId);
//                if (item1 == null) {
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                snCS = cashinfo.getSnFromId(item1.getItemId());
//                cItem = cashinfo.getItem(snCS);
//                if ((cItem == null) || (cashinfo.isBlockRefundableItemId(item1.getItemId()))) {
//                    if (chr.isShowPacket()) {
//                        if (cItem == null) {
//                            chr.dropMessage(1, new StringBuilder().append("换购失败:\r\n道具是否为空: ").append(cItem == null).toString());
//                        } else {
//                            chr.dropMessage(1, new StringBuilder().append("换购失败:\r\n道具禁止回购: ").append(cashinfo.isBlockRefundableItemId(item1.getItemId())).toString());
//                        }
//                    } else {
//                        chr.dropMessage(1, "换购失败，当前道具不支持换购。");
//                    }
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (!ii.isCash(cItem.getId())) {
//                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法换购道具.");
//                    return;
//                }
//                int Money = cItem.getPrice() / 10 * 3;
//                cs.removeFromInventory(item1);
//                chr.modifyCSPoints(toCharge, Money, false);
//                c.sendPacket(MTSCSPacket.商城换购道具(uniqueId, Money));
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
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
//                if ((item == null) || (!ItemConstants.isEffectRing(item.getId())) || (chr.getCSPoints(toCharge) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(0));
//                    return;
//                }
//                if (!item.genderEquals(chr.getGender())) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(7));
//                    return;
//                }
//                if (chr.getCashInventory().getItemsSize() >= 100) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(24));
//                    return;
//                }
//                if (!ii.isCash(item.getId())) {
//                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买戒指道具.");
//                    return;
//                }
//                if ((cashinfo.isBlockedCashItemId(item.getId())) || (cashinfo.isBlockCashSnId(snCS))) {
//                    chr.dropMessage(1, "该道具禁止购买。");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                Triple info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
//                if ((info == null) || (((Integer) info.getLeft()) <= 0)) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(7));
//                } else if ((((Integer) info.getMid()) == c.getAccID()) || (((Integer) info.getLeft()) == chr.getId())) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(6));
//                } else {
//                    if ((((Integer) info.getRight()) == chr.getGender()) && (action == 35)) {
//                        c.sendPacket(MTSCSPacket.商城错误提示(26));
//                        return;
//                    }
//                    int err = MapleRing.createRing(item.getId(), chr, partnerName, msg, ((Integer) info.getLeft()).intValue(), item.getSN());
//                    if (err != 1) {
//                        c.sendPacket(MTSCSPacket.商城错误提示(1));
//                        return;
//                    }
//                    chr.modifyCSPoints(toCharge, -item.getPrice(), false);
//                    c.sendPacket(MTSCSPacket.商城送礼(item.getId(), item.getCount(), partnerName));
//                    chr.sendNote(partnerName, new StringBuilder().append(partnerName).append(" 您已收到").append(chr.getName()).append("送给您的礼物，请进入现金商城查看！").toString());
//                    int chz = WorldFindService.getInstance().findChannel(partnerName);
//                    if (chz > 0) {
//                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
//                        if (receiver != null) {
//                            receiver.showNote();
//                        }
//                    }
//                }
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x25: // 礼包购买
                toCharge = slea.readByte() + 1;
                int snCsId = slea.readInt();
                int count = slea.readInt();
                chr.dropMessage(1, "礼包购买未开放.");
//
                break;
            case 0x26:
                slea.readMapleAsciiString();
                snCsId = slea.readInt();
                item = cashinfo.getItem(snCsId);
                partnerName = slea.readMapleAsciiString();
                msg = slea.readMapleAsciiString();
//                if ((item == null) || (chr.getCSPoints(1) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(3));
//                    return;
//                }
//                if (cashinfo.isBlockCashSnId(snCsId)) {
//                    chr.dropMessage(1, "该礼包禁止购买.");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
//                if ((info == null) || (((Integer) info.getLeft()) <= 0)) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(7));
//                } else if ((((Integer) info.getLeft()) == chr.getId()) || (((Integer) info.getMid()) == c.getAccID())) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(6));
//                } else if (!item.genderEquals(((Integer) info.getRight()))) {
//                    c.sendPacket(MTSCSPacket.商城错误提示(8));
//                } else {
//                    if (item.getPrice() <= 0) {
//                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买礼包道具.");
//                        return;
//                    }
//                    chr.getCashInventory().gift(((Integer) info.getLeft()), chr.getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
//                    chr.modifyCSPoints(1, -item.getPrice(), false);
//
//                    c.sendPacket(MTSCSPacket.商城送礼包(item.getId(), item.getCount(), partnerName));
//                    chr.sendNote(partnerName, new StringBuilder().append(partnerName).append(" 您已收到").append(chr.getName()).append("送给您的礼物，请进入现金商城查看！").toString());
//                    int chz = WorldFindService.getInstance().findChannel(partnerName);
//                    if (chz > 0) {
//                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
//                        if (receiver != null) {
//                            receiver.showNote();
//                        }
//                    }
//                }
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x27:
                item = cashinfo.getItem(slea.readInt());
//                if ((item == null) || (!MapleItemInformationProvider.getInstance().isQuestItem(item.getId()))) {
//                    chr.dropMessage(1, "该道具不是任务物品");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if ((chr.getMeso() < item.getPrice()) || (item.getPrice() <= 0)) {
//                    chr.dropMessage(1, "金币不足");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (chr.getItemQuantity(item.getId()) > 0) {
//                    chr.dropMessage(1, "你已经有这个道具\r\n不能购买.");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (chr.getInventory(ItemConstants.getInventoryType(item.getId())).getNextFreeSlot() < 0) {
//                    chr.dropMessage(1, "背包空间不足");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if (cashinfo.isBlockedCashItemId(item.getId())) {
//                    chr.dropMessage(1, CashShopServer.getCashBlockedMsg(item.getId()));
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                if ((item.getId() == 4031063) || (item.getId() == 4031191) || (item.getId() == 4031192)) {
//                    byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null, new StringBuilder().append("商城: 任务物品 在 ").toString());
//                    if (pos < 0) {
//                        c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                        return;
//                    }
//                    chr.gainMeso(-item.getPrice(), false);
////                    c.sendPacket(MTSCSPacket.updataMeso(chr));
//                    c.sendPacket(MTSCSPacket.商城购买任务道具(item.getPrice(), (short) item.getCount(), pos, item.getId()));
//                } else {
//                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法购买任务道具.");
//                }
                break;
            case 0x32:
                slea.readByte();
                snCS = slea.readInt();
                slea.readInt();
//                if ((snCS == 50200031) && (chr.getCSPoints(1) >= 500)) {
//                    chr.modifyCSPoints(1, -500, false);
//                    chr.modifyCSPoints(2, 500, false);
//                    chr.dropMessage(1, "兑换抵用卷成功");
//                } else if ((snCS == 50200032) && (chr.getCSPoints(1) >= 1000)) {
//                    chr.modifyCSPoints(1, -1000, false);
//                    chr.modifyCSPoints(2, 1000, false);
//                    chr.dropMessage(1, "兑换抵用卷成功");
//                } else if ((snCS == 50200033) && (chr.getCSPoints(1) >= 5000)) {
//                    chr.modifyCSPoints(1, -5000, false);
//                    chr.modifyCSPoints(2, 5000, false);
//                    chr.dropMessage(1, "兑换抵用卷成功");
//                } else {
//                    chr.dropMessage(1, "没有找到这个道具的信息。");
//                }
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            case 0x34:
                c.sendPacket(MTSCSPacket.redeemResponse());
                break;
            case 0x41:
                uniqueId = (int) slea.readLong();
//                Item boxItem = cs.findByCashId((int) uniqueId);
//                if ((boxItem == null) || (!cashinfo.hasRandomItem(boxItem.getItemId()))) {
//                    chr.dropMessage(1, "打开箱子失败，服务器找不到对应的道具信息。");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                List boxItemSNs = cashinfo.getRandomItem(boxItem.getItemId());
//                if (boxItemSNs.isEmpty()) {
//                    chr.dropMessage(1, "打开箱子失败，服务器找不到对应的道具信息。");
//                    c.sendPacket(MTSCSPacket.刷新点券信息(chr));
//                    return;
//                }
//                snCS = ((Integer) boxItemSNs.get(Randomizer.nextInt(boxItemSNs.size())));
//                cItem = cashinfo.getItem(snCS);
//                if (cItem != null) {
//                    item1 = cs.toItem(cItem);
//                    if ((item1 != null) && (item1.getUniqueId() > 0) && (item1.getItemId() == cItem.getId()) && (item1.getQuantity() == cItem.getCount())) {
//                        if (chr.getInventory(ItemConstants.getInventoryType(item1.getItemId())).addItem(item1) != -1) {
//                            cs.removeFromInventory(boxItem);
//                            c.sendPacket(MTSCSPacket.商城打开箱子(item1, Long.valueOf(uniqueId)));
//                        } else {
//                            chr.dropMessage(1, "打开箱子失败，请确认背包是否有足够的空间。");
//                        }
//                    }
//                }
//                c.sendPacket(MTSCSPacket.刷新点券信息(chr));
                break;
            default:
                MapleLogger.error(new StringBuilder().append("商城操作未知的操作类型: 0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2)).append(" ").append(slea.toString()).toString());
        }
    }
}
