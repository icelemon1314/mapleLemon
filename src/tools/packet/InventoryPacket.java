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
import tools.FileoutputUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

public class InventoryPacket {

    private static final Logger log = Logger.getLogger(InventoryPacket.class);

    /**
     * 增加道具栏格子数
     * @param invType
     * @param newSlots
     * @return
     */
    public static byte[] updateInventorySlotLimit(byte invType, byte newSlots) {
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
        mplew.write(1); // 需要更新的道具数量

        mplew.write(1); // 操作类型
        mplew.write(type.getType()); // iv type
        mplew.writeShort(item.getPosition()); // slot id
        mplew.writeShort(item.getQuantity());

        return mplew.getPacket();
    }

    /**
     * 清除道具
     * @return
     */
    public static byte[] clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);

        mplew.write(3);
        mplew.write(type.getType());
        mplew.writeShort(slot);
        if (type.getType() == 1) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }

    public static byte[] updateInventory() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    /**
     * 移动道具
     * @param type
     * @param src
     * @param dst
     * @param equipIndicator
     * @return
     */
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
     * 添加道具到背包中
     * @param item
     * @return
     */
    public static byte[] addItemToInventory(Item item){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // 17 01 01 03 01 00 06 01 00
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(1);

        mplew.write(0); // 添加道具
        mplew.write(item.getType());
        mplew.writeShort(item.getPosition());

        PacketHelper.addItemInfo(mplew, item,true);
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
            switch (mod.getMode()) { // @TODO 这个地方真恶心
                case 1: // 拾取道具
                    mplew.writeShort(mod.getPosition());
                    mplew.writeShort(mod.getItem().getQuantity());
                    PacketHelper.addItemInfo(mplew, mod.getItem());
                    break;
                case 2: // 移动道具
                    mplew.writeShort(mod.getOldPosition());
                    mplew.writeShort(mod.getPosition());
                    if (mod.getInventoryType() == 1) {
                        mplew.write(1);
                    }
                    break;
                case 3:// 丢道具
                    mplew.writeShort(mod.getOldPosition());
                    if (mod.getInventoryType() == 1) {
                        mplew.write(1);
                    }
                    break;
                default:
                    FileoutputUtil.log("未知的模式："+mod.getMode());
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
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * 更新道具数量
     * @param scroll
     * @param item
     * @param destroyed
     * @return
     */
    public static byte[] scrolledItem(Item scroll, Item item, boolean destroyed) {
        // 17 01 01 01 02 02 00 F0 DD 13 00 00 00 80 05 BB 46 E6 17 02 03 04 00 00 00 00 00 00 00 00 00 00 00 00 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0        0
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        // 修改道具
        mplew.write(1); // fromdrop always true
        mplew.write(1); // 操作次数的控制

        mplew.write(0);//v104
        mplew.write(ItemConstants.getInventoryType(item.getItemId()).getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            PacketHelper.addItemInfo(mplew, item,true);
        }


        return mplew.getPacket();
    }

    /**
     * 砸卷结果
     * @param chrId
     * @param scrollSuccess
     * @return
     */
    public static byte[] getScrollEffect(int chrId, Equip.ScrollResult scrollSuccess) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        // 027 只有成功和失败，没有消失囧
        switch (scrollSuccess) {
            case 失败:
                mplew.write(0);
                break;
            case 成功:
                mplew.write(1);
                break;
            case 消失:
                mplew.write(0);
                break;
            default:
                mplew.write(0);
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
