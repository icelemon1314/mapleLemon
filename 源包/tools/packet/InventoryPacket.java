package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import server.Randomizer;
import server.ServerProperties;
import server.maps.MapleMapItem;
import tools.data.output.MaplePacketLittleEndianWriter;

public class InventoryPacket {

    private static final Logger log = Logger.getLogger(InventoryPacket.class);

    public static byte[] updateInventorySlotLimit(byte invType, byte newSlots) {
        if (ServerProperties.ShowPacket()) {
            System.out.println("调用: " + new java.lang.Throwable().getStackTrace()[0]);
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_INVENTORY_SLOT.getValue());
        mplew.write(invType);
        mplew.write(newSlots);

        return mplew.getPacket();
    }

    public static byte[] modifyInventory(boolean updateTick, List<ModifyInventory> mods) {
        return modifyInventory(updateTick, mods, null);
    }

    //更新背包
    public static byte[] updateInventorySlot(MapleInventoryType type, Item item) {
        return updateInventorySlot(type, item, false);
    }

    //药的数量减少1
    public static byte[] updateInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
        //21 00 00 01 00 01 02 0F 00 02 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(1); // update
        mplew.write(1);
        mplew.write(type.getType()); // iv type
        mplew.writeShort(item.getPosition()); // slot id
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static byte[] clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);
        mplew.write(3);
        mplew.write(type.getType());
        mplew.writeShort(slot);
        return mplew.getPacket();
    }

    public static byte[] updateInventory() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] addInventorySlot(MapleInventoryType type, Item item) {
        return addInventorySlot(type,item,false);
    }

    public static byte[] addInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0); // 0
        }
        mplew.write(1);
        mplew.write(0);
        mplew.write(type.getType()); // iv type
        PacketHelper.addItemInfo(mplew, item,true);
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.write(2);
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }
        return mplew.getPacket();
    }
    /**
     * 道具栏信息变更
     * @param updateTick
     * @param mods
     * @param chr
     * @return
     */
    public static byte[] modifyInventory(boolean updateTick, List<ModifyInventory> mods, MapleCharacter chr) {//标记
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // 17 01 01 03 01 00 06 01 00
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        for (ModifyInventory mod : mods) {
            mplew.write(1);
            mplew.write(1);
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            System.out.println("道具模式："+mod.getMode());
            switch (mod.getMode()) { // @TODO 这个地方真恶心
                case 0: // 拾取道具
                    PacketHelper.addItemInfo(mplew, mod.getItem(),true);
                    break;
                case 2: // 穿脱装备
                    mplew.writeShort(mod.getOldPosition());
                    mplew.writeShort(mod.getPosition());
                    break;
                case 3:// 丢道具
                    mplew.writeShort(mod.getOldPosition());
                    if (mod.getInventoryType() == 1) {
                        mplew.write(1);
                    }
                    break;
                case 5:
                    mplew.writeShort(!mod.switchSrcDst() ? mod.getPosition() : mod.getOldPosition());
                    if (mod.getIndicator() == -1) {
                        break;
                    }
                    mplew.writeShort(mod.getIndicator());
                    break;
                case 8:
                    mplew.writeShort(mod.getPosition());
                    break;
                case 9:
                    PacketHelper.addItemInfo(mplew, mod.getItem());
                default:
                    mplew.write(0); // ?
                    mplew.write(mod.getPosition());
                    mplew.writeShort(mod.getQuantity());
            }
            mod.clear();
        }

        return mplew.getPacket();
    }

    /**
     * 背包满了
     * @return
     */
    public static byte[] getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getInventoryStatus() {
        return modifyInventory(false, Collections.EMPTY_LIST);
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(255);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(254);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        if (ServerProperties.ShowPacket()) {
            System.out.println("调用: " + new java.lang.Throwable().getStackTrace()[0]);
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showScrollTip(boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SCROLL_TIP.getValue());
        mplew.writeInt(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getScrollEffect(int chrId, int scroll, int toScroll) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chrId);
        mplew.writeShort(1);
        mplew.writeInt(scroll);
        mplew.writeInt(toScroll);
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * 砸卷结果
     * @param chrId
     * @param scrollSuccess
     * @param legendarySpirit
     * @param whiteScroll
     * @param scroll
     * @param toScroll
     * @return
     */
    public static byte[] getScrollEffect(int chrId, Equip.ScrollResult scrollSuccess) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.writeInt(4);
        // 027 只有成功和失败，没有消失囧
        switch (scrollSuccess) {
            case 失败:
                mplew.write(0);
                break;
            case 成功:
                mplew.write(2);
                break;
            case 消失:
                mplew.write(1);
                break;
            default:
                throw new IllegalArgumentException("effect in illegal range");
        }
        return mplew.getPacket();
    }

    /**
     * 掉落道具到地图上
     * @param drop
     * @param dropfrom
     * @param dropto
     * @param mod
     * @param move
     * @return
     */
    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod, boolean move) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(drop.getObjectId());
        mplew.write(drop.getMeso() > 0 ? 1 : 0);
        mplew.writeInt(drop.getItemId());
        mplew.writeInt(drop.getOwner());
        mplew.write(drop.getDropType());
        mplew.writePos(dropto);
        mplew.writeInt(drop.getObjectId());
        if (mod != 2) {
            mplew.writePos(dropfrom);
            mplew.writeShort(0);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        mplew.write(drop.isPlayerDrop() ? 0 : 1);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] explodeDrop(int oid) {
        if (ServerProperties.ShowPacket()) {
            System.out.println("调用: " + new java.lang.Throwable().getStackTrace()[0]);
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4);
        mplew.writeInt(oid);
        mplew.writeShort(655);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int chrId) {
        return removeItemFromMap(oid, animation, chrId, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int chrId, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation);
        mplew.writeInt(oid);
        if (animation == 2 || animation == 3 || animation == 5) {
            mplew.writeInt(chrId);
        } else if (animation == 4) {
            mplew.writeShort(1); // delay ?
        }

        return mplew.getPacket();
    }
}
