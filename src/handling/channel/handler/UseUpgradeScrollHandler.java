package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.vo.recv.UseUpgradeScrollRecvVO;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;

import java.util.List;

public class UseUpgradeScrollHandler extends MaplePacketHandler<UseUpgradeScrollRecvVO> {


    @Override
    public void handlePacket(UseUpgradeScrollRecvVO recvVO, MapleClient c) {
        // 2D 05 00 FF FF
        boolean cash = false;
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        Short slot = recvVO.getSlot();
        Short dst = recvVO.getDst();
        if (cash) {
            Item scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
            if (scroll == null) {
                scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
                if (scroll == null) {
                    return;
                }
                cash = false;
            }
        }
        UseUpgradeScroll(slot, dst, c, chr, 0, cash);
    }

    /**
     * 使用卷轴
     * @param slot
     * @param dst
     * @param c
     * @param chr
     * @param vegas
     * @param cash
     * @return
     */
    public static boolean UseUpgradeScroll(short slot, short dst, MapleClient c, MapleCharacter chr, int vegas, boolean cash) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        chr.setScrolledPosition((short) 0);

        Equip toScroll;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        if ((toScroll == null) || (c.getPlayer().hasBlockedInventory())) {
            return false;
        }
        byte oldLevel = toScroll.getLevel();
        Item scroll;
        if (cash) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        } else {
            scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        }
        if (scroll == null) {
            if (chr.isShowPacket()) {
                chr.dropMessage(0, "砸卷错误: 卷轴道具为空");
            }
            c.sendPacket(InventoryPacket.getInventoryFull());
            return false;
        }
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(10, new StringBuilder().append("砸卷信息: 卷轴ID ").append(scroll.getItemId()).append(" 卷轴名字 ").append(ii.getName(scroll.getItemId())).toString());
        }

        if ((!ItemConstants.canScroll(toScroll.getItemId()))) {
            if (chr.isShowPacket()) {
                chr.dropMessage(0, new StringBuilder().append("砸卷错误: 卷轴是否能对装备进行砸卷 ").append(!ItemConstants.canScroll(toScroll.getItemId())).toString());
            }
            c.sendPacket(InventoryPacket.getInventoryFull());
            return false;
        }
        List scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if ((scrollReqs != null) && (scrollReqs.size() > 0) && (!scrollReqs.contains(toScroll.getItemId()))) {
            if (chr.isShowPacket()) {
                chr.dropMessage(0, "砸卷错误: 特定卷轴只能对指定的卷轴进行砸卷.");
            }
            c.sendPacket(InventoryPacket.getInventoryFull());
            return false;
        }
        int scrollCount = scroll.getQuantity();
        if (scrollCount <= 0) {
            chr.dropSpouseMessage(0, new StringBuilder().append("砸卷错误，背包卷轴[").append(ii.getName(scroll.getItemId())).append("]数量为 0 .").toString());
            c.sendPacket(InventoryPacket.getInventoryFull());
            return false;
        }
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll, false, chr, vegas);
        Equip.ScrollResult scrollSuccess;
        if (scrolled != null) {
            if ((scrolled.getLevel() > oldLevel)) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else {
                scrollSuccess = Equip.ScrollResult.失败;
            }
        } else {
            scrollSuccess = Equip.ScrollResult.消失;
        }
        // 消耗卷轴
        chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
        if (scrollCount == 1) {
            // 如果只有一个道具了，那么就需要移除道具，否则是更新道具数量
            c.sendPacket(InventoryPacket.clearInventoryItem(ItemConstants.getInventoryType(scroll.getItemId()), scroll.getPosition(), false));
        } else {
            c.sendPacket(InventoryPacket.updateInventorySlot(ItemConstants.getInventoryType(scroll.getItemId()), scroll, false));
        }
        c.sendPacket(InventoryPacket.scrolledItem(scroll, toScroll, false));
        chr.getMap().broadcastMessage(chr, InventoryPacket.getScrollEffect(chr.getId(), scrollSuccess), vegas == 0);
        if (scrollSuccess == Equip.ScrollResult.消失) {
            chr.dropMessage(1,"由于诅咒卷轴的力量，道具消失了！消失了！消失了！" );
            c.sendPacket(InventoryPacket.clearInventoryItem(ItemConstants.getInventoryType(toScroll.getItemId()),toScroll.getPosition(), false));
            if (dst < 0) {
                chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
            }
            chr.equipChanged();
        }
        return scrollSuccess == Equip.ScrollResult.成功;
    }
}
