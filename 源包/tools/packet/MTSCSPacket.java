package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ItemConstants;
import handling.CashShopOpcode;
import handling.SendPacketOpcode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashShop;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MTSCSPacket {

    /**
     * 进入商城发角色信息
     * @param c
     * @return
     */
    public static byte[] warpchartoCS(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_CHAR.getValue());
        PacketHelper.addCharacterInfo(mplew, c.getPlayer());

//        mplew.write(1); // 0 = beta or someshit lol
        mplew.writeMapleAsciiString(c.getAccountName());

        mplew.writeShort(2); // wishlist i think.. o.o
        mplew.writeInt(10000001);
        mplew.writeInt(10000002);
        // 4* 上面个数个字节

        // 特别推荐 貌似会自动填充到对应的类别里面
        Map<Integer,CashItemInfo> cashinfo = CashItemFactory.getInstance().getItemStats();
        int size = 0;
        for (Map.Entry csInfo : cashinfo.entrySet()) {
            if ((int)csInfo.getKey() <= 80000000) {
                size++;
            }
        }
        mplew.writeShort(size);
        FileoutputUtil.log("商城道具个数："+size);
        for (Map.Entry csInfo : cashinfo.entrySet()) {
            CashItemInfo stats = (CashItemInfo)csInfo.getValue();
            if ((int)csInfo.getKey() > 80000000) // 剩下金币包需要处理
                continue;
            mplew.writeInt((int)csInfo.getKey()); // sn
            mplew.writeInt(stats.getId()); // itemID
            mplew.writeInt(stats.getCount()); // 多少个
            mplew.writeInt(stats.getPrice());
            mplew.writeInt(stats.getPrice()); // 打折后价格
            mplew.writeInt(stats.getPeriod()); // 可用天数
            mplew.writeInt(2);
            mplew.writeInt(8);
            mplew.write(0);
            mplew.writeInt(getTime());
            mplew.write(1); // 0则不能购买
            mplew.writeInt(getTime()+86400);
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }


        // 下面是960个字节 囧 30个道具？
        //int[] itemz = new int[]{10000281, 10000282, 10000283, 10000284, 10000285};
//        CashItemFactory cashinfo = CashItemFactory.getInstance();
//        int writeByte = 8;
//        mplew.writeInt(0xE);
//        mplew.writeInt(0xE);
//        int i=0;
//        for (i=0;i<15;i++) {
//                mplew.writeInt(10000001+i);
//                writeByte += 4;
//        }
//        i=0;
//        for (i=0;i<5;i++) {
//            mplew.writeInt(20900012);
//            mplew.writeInt(1);
//            mplew.writeInt(1);
//            writeByte += 12;
//        }
//        if (writeByte < 960) {
//            mplew.writeZero(960-writeByte);
//        }
        // 1-推荐；2-装备；3-消耗；5-其它；6-宠物
        for (byte i = 1; i <= 8; i++) {
            for (byte j = 0; j <= 1; j++) {
                mplew.writeInt(30100004); // best items, these are just first id's in Commodity
                mplew.writeInt(i);
                mplew.writeInt(j);

                mplew.writeInt(30000000);
                mplew.writeInt(i);
                mplew.writeInt(j);

                mplew.writeInt(50200009);
                mplew.writeInt(i);
                mplew.writeInt(j);

                mplew.writeInt(50000017);
                mplew.writeInt(i);
                mplew.writeInt(j);

                mplew.writeInt(50000015);
                mplew.writeInt(i);
                mplew.writeInt(j);
            }
        }

//        FileoutputUtil.log("写入字节数："+writeByte);
        // 商城礼包
        mplew.writeShort(5); // Stock
        mplew.writeInt(-1); // 1 = Sold Out, 2 = Not Sold
        mplew.writeInt(20900028);

        mplew.writeInt(0); // 1 = Sold Out, 2 = Not Sold
        mplew.writeInt(20900027);

        mplew.writeInt(2); // 1 = Sold Out, 2 = Not Sold
        mplew.writeInt(20900026);

        mplew.writeInt(4); // 1 = Sold Out, 2 = Not Sold
        mplew.writeInt(20900026);

        mplew.writeInt(5); // 1 = Sold Out, 2 = Not Sold
        mplew.writeInt(20900026);

        return mplew.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CASH_SONG.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] addCharBox(MapleCharacter c, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] removeCharBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] useCharm(byte charmsleft, byte daysleft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x9);
        mplew.write(1);
        mplew.write(charmsleft);
        mplew.write(daysleft);

        return mplew.getPacket();
    }

    public static byte[] useWheel(byte charmsleft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x18);
        mplew.writeLong(charmsleft);

        return mplew.getPacket();
    }

    public static byte[] useAlienSocket(boolean start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALIEN_SOCKET_CREATOR.getValue());
        mplew.write(start ? 0 : 2);

        return mplew.getPacket();
    }

    public static byte[] sendHammerData(boolean start, int hammered) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.VICIOUS_HAMMER.getValue());

        mplew.write(start ? 59 : 67);//0x57
        mplew.writeInt(0);
        if (start) {
            mplew.writeInt(hammered);
        }
        return mplew.getPacket();
    }

    public static byte[] sendHammerData(int type, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.VICIOUS_HAMMER.getValue());

        mplew.write(type);
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] changePetFlag(int uniqueId, boolean added, int flagAdded) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PET_FLAG_CHANGE.getValue());

        mplew.writeLong(uniqueId);
        mplew.write(added ? 1 : 0);
        mplew.writeShort(flagAdded);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.PET_NAMECHANGE.getValue());

        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.writeInt(slot);

        return mplew.getPacket();
    }

    public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(notes.getInt("gift"));
            notes.next();
        }

        return mplew.getPacket();
    }

    public static byte[] useChalkboard(int charid, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.CHALKBOARD.getValue());

        mplew.writeInt(charid);
        if ((msg == null) || (msg.length() <= 0)) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    /**
     * 刷新传送石头信息
     * @param chr
     * @param vip
     * @param delete
     * @return
     */
    public static byte[] getTrockRefresh(MapleCharacter chr, byte vip, boolean delete) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(2);
        int[] map = chr.getRegRocks();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(map[i]);
        }
        return mplew.getPacket();
    }

    /**
     * 错误信息
     * 0xA-不能登录的地图
     * 0x5-未知原因，请求失败
     * 0x9-你现在位置的地图？
     * 0x6/7-现在找不到%s玩家的位置，不能做瞬间移动
     * 0x8-不能移动到您指定的位置
     * @param op
     * @return
     */
    public static byte[] getTrockMessage(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TROCK_LOCATIONS.getValue());

        mplew.write(op);

        return mplew.getPacket();
    }

    public static byte[] 测试封包(String test) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(HexTool.getByteArrayFromHexString(test));

        return mplew.getPacket();
    }

    public static byte[] enableCSUse(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_USE.getValue());
        mplew.write(type);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * 更新玩家点卷信息
     * @param chr
     * @return
     */
    public static byte[] 刷新点券信息(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(chr.getCSPoints(1));
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] updataMeso(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_UPDATE_MESO.getValue());
        mplew.writeLong(MapleStat.金币.getValue());
        mplew.writeLong(chr.getMeso());

        return mplew.getPacket();
    }

    /**
     * 商城道具栏信息
     * @param c
     * @return
     */
    public static byte[] 商城道具栏信息(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        //mplew.write(CashShopOpcode.加载道具栏.getValue());
        mplew.write(0x1D);
        CashShop mci = c.getPlayer().getCashInventory();
//        int size = 0;
        FileoutputUtil.log("商城保管箱道具个数："+mci.getItemsSize());
        mplew.writeShort(mci.getItemsSize());
        for (Item itemz : mci.getInventory()) {
            mplew.writeLong(itemz.getUniqueId() > 0 ? itemz.getUniqueId() : 0L);
            mplew.writeInt(c.getAccID());
            mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
            mplew.writeInt(itemz.getItemId());
            mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
            mplew.writeShort(itemz.getQuantity());
            mplew.writeAsciiString(itemz.getGiftFrom(), 13);
            PacketHelper.addExpirationTime(mplew, itemz.getExpiration());
            mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
            mplew.writeShort(0);


//            addCashItemInfo(mplew, itemz, c.getAccID(), 0);
//            if (ItemConstants.isPet(itemz.getItemId())) {
//                size++;
//            }
        }
        mplew.writeShort(0); // 104 * size 应该是宠物的结构体
        mplew.writeShort(0);
//        mplew.writeInt(size);
//        if (mci.getInventory().size() > 0) {
//            for (Item itemz : mci.getInventory()) {
//                if (ItemConstants.isPet(itemz.getItemId())) {
//                    PacketHelper.addItemInfo(mplew, itemz);
//                }
//            }
//        }
//        mplew.writeShort(c.getPlayer().getStorage().getSlots());
//        mplew.writeShort(c.getAccCharSlots());
//        mplew.writeShort(0);
//        //mplew.writeShort(3);
//        mplew.writeShort(c.getAccCharSlots());

        return mplew.getPacket();
    }

    /**
     * 商城礼物信息
     * @param c
     * @param gifts
     * @return
     */
    public static byte[] 商城礼物信息(MapleClient c, List<Pair<Item, String>> gifts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.加载礼物.getValue());
        mplew.writeShort(0); // 53*size
        mplew.writeShort(0); // 104*size
        mplew.writeShort(0);

        /*
        mplew.writeShort(gifts.size());
        for (Pair gift : gifts) {
            mplew.writeLong(((Item) gift.getLeft()).getUniqueId());
            mplew.writeInt(((Item) gift.getLeft()).getItemId());
            mplew.writeAsciiString(((Item) gift.getLeft()).getGiftFrom(), 13);
            mplew.writeAsciiString((String) gift.getRight(), 73);
        }*/

        return mplew.getPacket();
    }

    public static byte[] 商城购物车(MapleCharacter chr, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(update ? CashShopOpcode.更新购物车.getValue() : 0x4C);
        int[] list = chr.getWishlist();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(list[i] != -1 ? list[i] : 0);
        }
        return mplew.getPacket();
    }

    /**
     * 购买商城道具
     * @param item
     * @param sn
     * @param accid
     * @return
     */
    public static byte[] 购买商城道具(Item item, int sn, int accid) { //
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x23);
        // 53个字节
        mplew.writeLong(item.getUniqueId() > 0 ? item.getUniqueId() : 0L);
        mplew.writeInt(accid);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
        mplew.writeInt(item.getItemId());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
        mplew.writeShort(item.getQuantity()); // 26
        mplew.writeAsciiString(item.getGiftFrom(), 13); // 39
        PacketHelper.addExpirationTime(mplew, item.getExpiration()); // 47
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01")); // 51
        mplew.writeShort(0); // 53
        return mplew.getPacket();
    }

    /**
     * 商城送礼
     * @param itemid
     * @param quantity
     * @param receiver
     * @return
     */
    public static byte[] 商城送礼(int itemid, int quantity, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.商城送礼.getValue());
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemid);
        mplew.writeShort(quantity);

        return mplew.getPacket();
    }

    /**
     * 扩充道具栏
     * @param inv
     * @param slots
     * @return
     */
    public static byte[] 扩充道具栏(int inv, int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.扩充道具栏成功.getValue());
        mplew.write(inv);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    /**
     * 扩充仓库
     * @param slots
     * @return
     */
    public static byte[] 扩充仓库(int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.扩充仓库.getValue());
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] 商城到背包(Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x30);
        FileoutputUtil.log("道具位置："+item.getPosition());
        mplew.writeShort(item.getPosition());
//        PacketHelper.addItemInfo(mplew, item);
        // 这里比较奇怪了，理论上应该和普通的背包道具是一样的哈
        mplew.write(item.getType());
        mplew.writeInt(item.getItemId());

        boolean hasUniqueId = (item.getUniqueId() > 0) && (!ItemConstants.is结婚戒指(item.getItemId())) && (item.getItemId() / 10000 != 166);
        mplew.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            mplew.writeLong(item.getUniqueId());
        }
        if (item.getPet() != null) {
            PacketHelper.addPetItemInfo(mplew, item, item.getPet());
        } else {
            PacketHelper.addExpirationTime(mplew, item.getExpiration());
            if (item.getType() == 1) { // 装备
                Equip equip = (Equip) item;
                mplew.write(equip.getUpgradeSlots());
                mplew.write(equip.getLevel());
                mplew.writeShort(equip.getStr());
                mplew.writeShort(equip.getDex());
                mplew.writeShort(equip.getInt());
                mplew.writeShort(equip.getLuk());
                mplew.writeShort(equip.getHp());
                mplew.writeShort(equip.getMp());
                mplew.writeShort(equip.getWatk());
                mplew.writeShort(equip.getMatk());
                mplew.writeShort(equip.getWdef());
                mplew.writeShort(equip.getMdef());
                mplew.writeShort(equip.getAcc());
                mplew.writeShort(equip.getAvoid());
                mplew.writeShort(equip.getHands());
                mplew.writeShort(equip.getSpeed());
                mplew.writeShort(equip.getJump());
                mplew.writeMapleAsciiString(equip.getOwner());
            } else { // 非装备
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
            }
        }



        return mplew.getPacket();
    }

    /**
     * CCashShop::OnCashItemResMoveStoLDone
     * @param item
     * @param accId
     * @param sn
     * @return
     */
    public static byte[] 背包到商城(Item item, int accId, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x32);
//        addCashItemInfo(mplew, item, accId, sn);
        // @TODO 还是继续用addCashItemInfo
        mplew.writeLong(item.getUniqueId() > 0 ? item.getUniqueId() : 0L);
        mplew.writeInt(accId);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(item.getQuantity());
        mplew.writeAsciiString(item.getGiftFrom(), 13);
        PacketHelper.addExpirationTime(mplew, item.getExpiration());
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] 商城删除道具(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.删除道具.getValue());
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    public static byte[] cashItemExpired(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.道具到期.getValue());
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    public static byte[] 商城换购道具(int uniqueId, int Money) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.换购道具.getValue());
        mplew.writeLong(uniqueId);
        mplew.writeLong(Money);

        return mplew.getPacket();
    }

    public static byte[] 商城购买礼包(Map<Integer, Item> packageItems, int accId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.购买礼包.getValue());
        mplew.write(packageItems.size());
        int size = 0;
        for (Map.Entry sn : packageItems.entrySet()) {
            addCashItemInfo(mplew, (Item) sn.getValue(), accId, ((Integer) sn.getKey()));
            if ((ItemConstants.isPet(((Item) sn.getValue()).getItemId())) || (ItemConstants.getInventoryType(((Item) sn.getValue()).getItemId()) == MapleInventoryType.EQUIP)) {
                size++;
            }
        }
        mplew.writeInt(size);
        if (packageItems.size() > 0) {
            for (Item itemz : packageItems.values()) {
                if ((ItemConstants.isPet(itemz.getItemId())) || (ItemConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP)) {
                    PacketHelper.addItemInfo(mplew, itemz);
                }
            }
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] 商城送礼包(int itemId, int quantity, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.商城送礼包.getValue());
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemId);
        mplew.writeInt(quantity);

        return mplew.getPacket();
    }

    public static byte[] 商城购买任务道具(int price, short quantity, byte position, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.购买任务道具.getValue());
        mplew.writeInt(price);
        mplew.writeShort(quantity);
        mplew.writeShort((short) position);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    /**
     * 商城道具信息
     * @param mplew
     * @param item
     * @param accId
     * @param sn
     */
    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn) {
        PacketHelper.addItemInfo(mplew,item);
        /*
        // 53个字节
        mplew.writeLong(item.getUniqueId() > 0 ? item.getUniqueId() : 0L);
        mplew.writeInt(accId);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
        mplew.writeInt(item.getItemId());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
        mplew.writeShort(item.getQuantity());
        mplew.writeAsciiString(item.getGiftFrom(), 13);
        PacketHelper.addExpirationTime(mplew, item.getExpiration());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01"));
        mplew.writeShort(0);
        */
    }

    public static byte[] 商城错误提示(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.错误提示.getValue());
        mplew.write(err);

        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(CashShopOpcode.领奖卡提示.getValue());
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(26);
        mplew.writeInt(itemid);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(Map<Integer, Item> items, int mesos, int maplePoints, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.领奖卡提示.getValue());
        mplew.write(items.size());
        for (Map.Entry item : items.entrySet()) {
            addCashItemInfo(mplew, (Item) item.getValue(), c.getAccID(), ((Integer) item.getKey()));
        }
        mplew.writeInt(maplePoints);
        mplew.writeInt(0);
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static byte[] redeemResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());

        mplew.write(CashShopOpcode.注册商城.getValue());
        mplew.writeInt(0);
        mplew.writeInt(1);

        return mplew.getPacket();
    }

    public static byte[] 商城打开箱子(Item item, Long uniqueId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(CashShopOpcode.打开箱子.getValue());
        mplew.writeLong(uniqueId);
        mplew.writeInt(0);
        PacketHelper.addItemInfo(mplew, item);
        mplew.writeInt(item.getPosition());
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] 商城提示(int 消费, int 达到, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_MSG.getValue());

        mplew.write(CashShopOpcode.商城提示.getValue());
        mplew.writeInt(消费);
        mplew.writeInt(达到);
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] 热点推荐(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_HOT.getValue());
        int[] hotSn = {20400253, 20500121, 20600140, 20000348};

        mplew.writeInt(hotSn.length);
        for (int i = 0; i < hotSn.length; i++) {
            mplew.writeInt(hotSn[i]);
        }

        return mplew.getPacket();
    }

    public static byte[] 每日特卖() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CS_DAILY.getValue());
        mplew.writeInt(getTime());
        mplew.writeLong(0L);

        return mplew.getPacket();
    }

    public static int getTime() {
        String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        return Integer.valueOf(time);
    }

    public static byte[] showXmasSurprise(int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.XMAS_SURPRISE.getValue());
        mplew.write(230);
        mplew.writeLong(idFirst);
        mplew.writeInt(0);
        addCashItemInfo(mplew, item, accid, 0);
        mplew.writeInt(item.getItemId());
        mplew.write(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] getBoosterFamiliar(int cid, int familiar, int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOOSTER_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(familiar);
        mplew.writeLong(id);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPack(int f1, int f2, int f3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(215);
        mplew.writeInt(f1);
        mplew.writeInt(f2);
        mplew.writeInt(f3);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackClick() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(213);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackReveal() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(214);

        return mplew.getPacket();
    }

    /**
     * 使用金币包失败
     * @return
     */
    public static byte[] sendMesobagFailed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MESOBAG_FAILURE.getValue());
        return mplew.getPacket();
    }

    /**
     * 使用金币包成功
     * @param mesos
     * @return
     */
    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.MESOBAG_SUCCESS.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

}
