package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;

public class ItemScrollHandler {

    /**
     * 砸卷
     * @param slea
     * @param c
     * @param chr
     * @param cash
     */
    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean cash) {
        // 2D 05 00 FF FF
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        byte slot = (byte) slea.readShort();
        byte dst = (byte) slea.readShort();
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
        byte ws = 0;
        if (slea.available() >= 3L) {
            ws = (byte) slea.readShort();
        }
        UseUpgradeScroll((short) slot, (short) dst, (short) ws, c, chr, 0, cash);
    }

    /**
     * 使用卷轴
     * @param slot
     * @param dst
     * @param ws
     * @param c
     * @param chr
     * @param vegas
     * @param cash
     * @return
     */
    public static boolean UseUpgradeScroll(short slot, short dst, short ws, MapleClient c, MapleCharacter chr, int vegas, boolean cash) {
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
        byte oldEnhance = toScroll.getEnhance();
        byte oldState = toScroll.getState();
        byte oldAddState = toScroll.getAddState();
        short oldFlag = toScroll.getFlag();
        byte oldSlots = toScroll.getUpgradeSlots();
        Item scroll;
        if (cash) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        } else {
            scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        }
        if (scroll == null) {
            if (chr.isAdmin()) {
                chr.dropMessage(0, "砸卷错误: 卷轴道具为空");
            }
            c.getSession().write(InventoryPacket.getInventoryFull());
            return false;
        }
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(10, new StringBuilder().append("砸卷信息: 卷轴ID ").append(scroll.getItemId()).append(" 卷轴名字 ").append(ii.getName(scroll.getItemId())).toString());
        }

        if ((!ItemConstants.canScroll(toScroll.getItemId()))) {
            if (chr.isAdmin()) {
                chr.dropMessage(0, new StringBuilder().append("砸卷错误: 卷轴是否能对装备进行砸卷 ").append(!ItemConstants.canScroll(toScroll.getItemId())).toString());
            }
            c.getSession().write(InventoryPacket.getInventoryFull());
            return false;
        }
        System.out.println("砸卷222");
        Item wscroll = null;
        List scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if ((scrollReqs != null) && (scrollReqs.size() > 0) && (!scrollReqs.contains(toScroll.getItemId()))) {
            if (chr.isAdmin()) {
                chr.dropMessage(0, "砸卷错误: 特定卷轴只能对指定的卷轴进行砸卷.");
            }
            c.getSession().write(InventoryPacket.getInventoryFull());
            return false;
        }
        System.out.println("砸卷3333");
        if (scroll.getQuantity() <= 0) {
            chr.dropSpouseMessage(0, new StringBuilder().append("砸卷错误，背包卷轴[").append(ii.getName(scroll.getItemId())).append("]数量为 0 .").toString());
            c.getSession().write(InventoryPacket.getInventoryFull());
            return false;
        }
        System.out.println("砸卷4444");
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll, false, chr, vegas);
        Equip.ScrollResult scrollSuccess;
            //  Equip.ScrollResult scrollSuccess;
            if ((scrolled.getUpgradeSlots() > oldSlots)) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else {
                //  Equip.ScrollResult scrollSuccess;
                if ((scrolled != toScroll)) {
                    scrolled = toScroll;
                    scrollSuccess = Equip.ScrollResult.成功;
                } else {
                    scrollSuccess = Equip.ScrollResult.失败;
                }
            }

        System.out.println("砸卷5555");
        chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
       if ((scrollSuccess == Equip.ScrollResult.失败) && (scrolled.getUpgradeSlots() < oldSlots) && (chr.getInventory(MapleInventoryType.CASH).findById(5640000) != null)) {
            chr.setScrolledPosition(scrolled.getPosition());
            if (vegas == 0) {
                c.getSession().write(MaplePacketCreator.pamSongUI());
            }
        }
        System.out.println("砸卷7777"+vegas);
        c.getSession().write(InventoryPacket.updateInventorySlot(ItemConstants.getInventoryType(scroll.getItemId()), scroll, true));
        chr.getMap().broadcastMessage(chr, InventoryPacket.getScrollEffect(chr.getId(), scrollSuccess), vegas == 0);
        if (((scrollSuccess == Equip.ScrollResult.成功)) && (vegas == 0)) {
            chr.equipChanged();
        }
        return scrollSuccess == Equip.ScrollResult.成功;
    }
}
