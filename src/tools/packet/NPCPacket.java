package tools.packet;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import handling.SendPacketOpcode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import server.ServerProperties;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.shop.MapleShop;
import server.shop.MapleShopResponse;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NPCPacket {

    private static final Logger log = Logger.getLogger(NPCPacket.class);

    /**
     * 召唤NPC
     * @param life
     * @param show
     * @return
     */
    public static byte[] spawnNPC(MapleNPC life, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        baseNPC(mplew,life);

        return mplew.getPacket();
    }

   public static void baseNPC(MaplePacketLittleEndianWriter mplew,MapleNPC life){
       mplew.writeShort(life.getPosition().x);
       mplew.writeShort(life.getCy());
       mplew.write(life.getF() == 1 ? 0 : 1);
       mplew.writeShort(life.getFh());
       mplew.writeShort(life.getRx0());
       mplew.writeShort(life.getRx1());

   }

    public static byte[] removeNPC(int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    public static byte[] removeNPCController(int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    /**
     * 请求控制NPC
     * @param life
     * @param MiniMap
     * @return
     */
    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        baseNPC(mplew,life);

        return mplew.getPacket();
    }

    public static byte[] setNPCSpecialAction(int oid, String action) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_SET_SPECIAL_ACTION.getValue());
        mplew.writeInt(oid);
        mplew.writeMapleAsciiString(action);
        mplew.writeInt(0); //unknown yet
        mplew.write(0); //unknown yet
        return mplew.getPacket();
    }

    public static byte[] NPCSpecialAction(MapleNPC life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_ACTION.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeShort(-1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.writeInt(0);

        mplew.writeShort(1);

        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(life.getFh());
        mplew.writeInt(0);
        mplew.write(5);
        mplew.write(0x38);

        mplew.write(4);
        return mplew.getPacket();
    }

    public static byte[] NPCSpecialAction(int npc, String code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_ACTION.getValue());
        mplew.writeInt(npc);
        mplew.write(HexTool.getByteArrayFromHexString(code));

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write(npc.getF() == 1 ? 0 : 1);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap();
        for (Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte) ((position.getKey()) * -1);
            if ((pos < 100) && (myEquip.get(pos) == null)) {
                myEquip.put(pos, position.getValue());
            } else if ((pos > 100) && (pos != 111)) {
                pos = (byte) (pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, position.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, position.getValue());
            }
        }
        for (Map.Entry entry : myEquip.entrySet()) {
            mplew.write(((Byte) entry.getKey()));
            mplew.writeInt(((Integer) entry.getValue()));
        }
        mplew.write(255);
        for (Map.Entry entry : maskedEquip.entrySet()) {
            mplew.write(((Byte) entry.getKey()));
            mplew.writeInt(((Integer) entry.getValue()));
        }
        mplew.write(255);
        Integer cWeapon = equip.get(Byte.valueOf((byte) -111));
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(npc.getPet(i));
        }

        return mplew.getPacket();
    }

    public static byte[] setNPCScriptable(List<Pair<Integer, String>> npcs) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
        mplew.write(npcs.size());
        for (Pair s : npcs) {
            mplew.writeInt(((Integer) s.left));
            mplew.writeMapleAsciiString((String) s.right);
            mplew.writeInt(0);
            mplew.writeInt(2147483647);
        }
        return mplew.getPacket();
    }

    public static void baseScriptMan (MaplePacketLittleEndianWriter mplew,int npc,byte msgType) {
        mplew.write(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(msgType); // 消息类型
    }

    public static byte[] sendNPCSay(int npc,String talk) {
        return sendNPCSay(npc,talk,false,false);
    }

    /**
     *
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendChoose(int npc,String talk){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)4);

        mplew.writeMapleAsciiString(talk);
        return mplew.getPacket();
    }

    /**
     * NPC说话
     * @param npc
     * @param talk
     * @param pre
     * @param next
     * @return
     */
    public static byte[] sendNPCSay(int npc,String talk,boolean pre,boolean next) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)0);

        mplew.writeMapleAsciiString(talk);
        if (pre)
            mplew.write(1);
        else
            mplew.write(0);
        if (next)
            mplew.write(1);
        else
            mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * NPC询问玩家做选择
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendNPCAskYesNo(int npc,String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)1);

        mplew.writeMapleAsciiString(talk);

        return mplew.getPacket();
    }

    /**
     * NPC显示询问文本
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendNPCAskText(int npc,String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)2);

        mplew.writeMapleAsciiString(talk);
        mplew.writeMapleAsciiString("什么？"); // def
        mplew.writeShort(0); // min
        mplew.writeShort(0); // max

        return mplew.getPacket();
    }

    /**
     * NPC显示数字
     * @param npc
     * @param talk
     * @param def
     * @param min
     * @param max
     * @return
     */
    public static byte[] sendNPCAskNumber(int npc,String talk,int def,int min,int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)3);

        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);

        return mplew.getPacket();
    }

    /**
     * NPC显示菜单
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendNPCAskMenu(int npc,String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)4);

        mplew.writeMapleAsciiString(talk);

        return mplew.getPacket();
    }

    /**
     * NPC显示宠物信息
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendNPCAskAvatar(int npc,String talk,int styles[]) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)5);

        mplew.writeMapleAsciiString(talk);
        mplew.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            mplew.writeInt(styles[i]);
        }

        return mplew.getPacket();
    }

    /**
     * NPC显示宠物信息
     * @param npc
     * @param talk
     * @return
     */
    public static byte[] sendNPCAskPet(int npc,String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        baseScriptMan(mplew,npc,(byte)6);

        mplew.writeMapleAsciiString(talk);
        mplew.write(0); // 宠物个数
//        mplew.writeLong(5000000); // sn
//        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getNPCShop(int shopId, MapleShop shop, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(shopId);
        PacketHelper.addShopInfo(mplew, shop, c);

        return mplew.getPacket();
    }

    /**
     * 确认商店购买东西
     * @param code
     * @param shop
     * @param c
     * @param indexBought
     * @return
     */
    public static byte[] confirmShopTransaction(MapleShopResponse code, MapleShop shop, MapleClient c, int indexBought) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code.getValue());
        return mplew.getPacket();
    }

    /**
     * 发送仓库内的道具
     * @param npcId
     * @param slots
     * @param items
     * @param meso
     * @return
     */
    public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, long meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STORAGE_OPEN.getValue());
        mplew.writeInt(npcId);
        mplew.write(slots);

        short flag = 0x7E; // 标记位
        mplew.writeShort(flag);
        mplew.writeInt((int)meso);

        byte size[] = new byte[]{0,0,0,0,0,0};
        for (Item item : items) {
            switch(item.getItemId() / 1000000) {
                case 1: size[1]++; break;
                case 2: size[2]++; break;
                case 3: size[3]++; break;
                case 4: size[4]++; break;
                case 5: size[5]++; break;
                default: FileoutputUtil.log("Unknown type found!"); break;
            }
        }

        for (int i=1;i<6;i++) {
            if (size[i] > 0) {
                mplew.write(size[i]);
                for (Item item : items) {
                    if (item.getItemId() / 1000000 == i) {
                        PacketHelper.addItemInfo(mplew, item, true);
                    }
                }
            } else {
                mplew.write(0);
            }
        }

        return mplew.getPacket();
    }

    /**
     * 仓库操作错误包
     * @param op
     * @return
     */
    public static byte[] getStorageError(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STORAGE_OPERATION.getValue());
        mplew.write(op);

        return mplew.getPacket();
    }

    /**
     * 存钱
     * @param slots
     * @param meso
     * @return
     */
    public static byte[] mesoStorage(byte slots, long meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STORAGE_OPERATION.getValue());

        mplew.write(0xF);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeInt((int)meso);

        return mplew.getPacket();
    }

    /**
     * 存放道具
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STORAGE_OPERATION.getValue());
        mplew.write(0xF);

        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());

        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item,true);
        }

        return mplew.getPacket();
    }

    /**
     * 从仓库取道具出来
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.STORAGE_OPERATION.getValue());
        mplew.write(0x0F);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item,true);
        }
        return mplew.getPacket();
    }

    public static byte[] spawnNPCRequestController(int npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(npc);

        return mplew.getPacket();
    }

    public static byte[] NPCSpecialAction(int oid, int value, int x, int y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_UPDATE_LIMITED_INFO.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);

        return mplew.getPacket();
    }

}
