package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleCoolDownValueHolder;
import client.MapleQuestStatus;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.ItemConstants;
import handling.Buffstat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleCarnivalChallenge;
import server.MapleItemInformationProvider;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shop.MapleShop;
import server.shop.MapleShopItem;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import tools.BitTools;
import tools.DateUtil;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PacketHelper {

    public static long MAX_TIME = 150842304000000000L;
    public static long ZERO_TIME = 94354848000000000L;
    public static long PERMANENT = 150841440000000000L;

    public static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1L) {
            return MAX_TIME;
        }
        if (realTimestamp == -2L) {
            return ZERO_TIME;
        }
        if (realTimestamp == -3L) {
            return PERMANENT;
        }
        return DateUtil.getFileTimestamp(realTimestamp);
    }

    public static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        List<MapleQuestStatus> started = chr.getStartedQuests();
        /*
        mplew.writeShort(4);
        mplew.writeInt(100);
        mplew.writeMapleAsciiString("info"); // quest值 info不行
        mplew.writeInt(1001100);
        mplew.writeMapleAsciiString("s");
        mplew.writeInt(1000100);
        mplew.writeMapleAsciiString("w");
        mplew.writeInt(1001800);
        mplew.writeMapleAsciiString("2s");
        */
        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeInt(q.getQuest().getId());
            mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
        }
    }

    /**
     * 添加角色技能信息
     * @param mplew
     * @param chr
     */
    public static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Skill, SkillEntry> skills = chr.getSkills(true);
        if (skills != null) {
            mplew.writeShort(skills.size());
            for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                mplew.writeInt((skill.getKey()).getId());
                mplew.writeInt((skill.getValue()).skillLevel);
            }
        } else {
            mplew.writeShort(0);
        }
    }

    /**
     * 添加戒指信息
     * @param mplew
     * @param chr
     */
    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Triple aRing = chr.getRings(true);
        List<MapleRing> cRing = (List) aRing.getLeft();
        mplew.writeShort(cRing.size());
        for (MapleRing ring : cRing) { // 39个字节
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 0x13);
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
        }
    }

    /**
     * 添加背包信息
     * @param mplew
     * @param chr
     */
    public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equippedList = iv.newList();
        Collections.sort(equippedList);
        List<Item> equipped = new ArrayList();
        List<Item> equippedCash = new ArrayList();
        for (Item item : equippedList) {
            if ((item.getPosition() < 0) && (item.getPosition() > -100)) {
                equipped.add(item);
            } else if ((item.getPosition() <= -100) && (item.getPosition() > -1000)) {
                equippedCash.add(item);
            }
        }
        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);

        for (Item item : equippedCash) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);

        iv = chr.getInventory(MapleInventoryType.EQUIP);
        mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        for (Item item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);

        iv = chr.getInventory(MapleInventoryType.USE);
        mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        for (Item item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.SETUP);
        mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        for (Item item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.ETC);
        mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        for (Item item : iv.list()) {
            if (item.getPosition() < 100) {
                addItemInfo(mplew, item);
            }
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.CASH);
        mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
        for (Item item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
    }

    /**
     * 添加角色状态 ok
     * @param mplew
     * @param chr
     * GW_CharacterStat::Decode
     */
    public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId());
        mplew.writeAsciiString(chr.getName(), 0x13);
        mplew.write(chr.getClient().getGender()); // 帐号控制
        mplew.write(chr.getSkinColor()); // 肤色
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());
        mplew.writeLong(0); // Pet SN
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getStat().str);
        mplew.writeShort(chr.getStat().dex);
        mplew.writeShort(chr.getStat().int_);
        mplew.writeShort(chr.getStat().luk);
        mplew.writeShort(chr.getStat().baseHp);
        mplew.writeShort(chr.getStat().baseMaxHp);
        mplew.writeShort(chr.getStat().baseMp);
        mplew.writeShort(chr.getStat().baseMaxMp);
        mplew.writeShort(chr.getRemainingAp());
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeInt((int)chr.getExp()); // @TODO 经验用int
        mplew.writeShort(chr.getFame());
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());
        mplew.writeReversedLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.writeInt(0);
    }

    public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega, boolean second) {
            if (mega) {
                mplew.write(chr.getGender());
            }
            mplew.write(chr.getSkinColor()); // skin color
            mplew.writeInt(chr.getFace()); // face
            mplew.write(0); // OdinMS: mega ? 1 : 0
            mplew.writeInt(chr.getHair()); // hair

            Map<Byte, Integer> myEquip = new LinkedHashMap();
            Map<Byte, Integer> maskedEquip = new LinkedHashMap();
            MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);

            for (Item item : equip.newList()) {
                if (item.getPosition() < -128) {
                    continue;
                }

                byte pos = (byte) (item.getPosition() * -1);
                if ((pos < 100) && (myEquip.get(pos) == null)) {
                    Equip skin = (Equip) item;
                    myEquip.put(pos, item.getItemId());
                } else if (((pos > 100) || (pos == -128)) && (pos != 111)) {
                    pos = (byte) (pos == -128 ? 28 : pos - 100);
                    if (myEquip.get(pos) != null) {
                        maskedEquip.put(pos, myEquip.get(pos));
                    }
                    myEquip.put(pos, item.getItemId());
                } else if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, item.getItemId());
                }
            }
            for (Map.Entry entry : myEquip.entrySet()) {
                mplew.write((Byte) entry.getKey());
                mplew.writeInt((Integer) entry.getValue());
            }
            mplew.write(0xFF);
            mplew.writeInt(0); // PET
//            if (chr.getPets() != null) {
//                mplew.writeInt(chr.getPet(0).getItemId());
//            } else {
//                mplew.writeInt(0); // Pet
//            }
    }

    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(getTime(time));
    }


    /**
     * 添加道具信息
     * GW_ItemSlotBase::Decode
     * @param mplew
     * @param item
     */
    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item){
        addItemInfo(mplew,item,false);
    }

    /**
     * 添加道具信息
     * @param mplew
     * @param item
     * @param isCreateItem 是否从外部获取道具，也就是道具操作为0时要使用
     */
    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item,boolean isCreateItem) {
        if (!isCreateItem) {
            byte pos = item.getPosition();
            if (pos <= -1) { // 身上的装备
                pos = (byte) (pos * -1);
                if ((pos > 100) && (pos < 1000)) {
                    pos = (byte)(pos - 100);
                }
                mplew.write(pos);
            } else {
                mplew.write(pos);
            }
        }


        if (item.getPet() != null) {
            addPetItemInfo(mplew, item, item.getPet());
        } else {
            if (item.getType() == 1) { // 装备
                addEquipItemInfo(mplew, item);
            } else { // 非装备
                addOtherItemInfo(mplew, item); // 其它道具

            }
        }
    }

    /**
     * 非装备
     * @param mplew
     * @param item
     */
    private static void addOtherItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        PacketHelper.addBaseItemHeader(mplew,item);
        mplew.writeShort(item.getQuantity());
        mplew.writeMapleAsciiString(item.getOwner());
    }

    /**
     * 添加准备信息
     * @param mplew
     * @param item
     */
    private static void addEquipItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        PacketHelper.addBaseItemHeader(mplew,item);
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
    }

    public static void serializeMovementList(MaplePacketLittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if ((chr.getPlayerShop() != null) && (chr.getPlayerShop().isOwner(chr)) && (chr.getPlayerShop().getShopType() != 1) && (chr.getPlayerShop().isAvailable())) {
            addInteraction(mplew, chr.getPlayerShop());
        } else {
            mplew.write(0);
        }
    }

    public static void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
        mplew.write(shop.getGameType());
        mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            mplew.write(shop.getPassword().length() > 0 ? 1 : 0);
        }
        mplew.write(shop.getItemId() - 5030000);
        mplew.write(shop.getSize());
        mplew.write(shop.getMaxSize());
        if (shop.getShopType() != 1) {
            mplew.write(shop.isOpen() ? 0 : 1);
        }
    }

    /**
     * 添加角色信息到地图
     * @param mplew
     * @param chr
     */
    public static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(-1);    // 二进制 111
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity());
        mplew.writeInt(chr.getMeso());

        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        mplew.writeShort(0); // Mini Games ?
        addRingInfo(mplew, chr);
        // getVIPRockMaps
//        for (int i = 0; i < 5; i++) {
//            mplew.writeInt(910000000 + i);
//        }
        for (int map : chr.getRegRocks()) {
            mplew.writeInt(map);
        }
    }

    public static void addQuestDataInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Integer, String> questInfos = chr.getInfoQuest_Map();
        mplew.writeShort(questInfos.size());
        for (Map.Entry quest : questInfos.entrySet()) {
            mplew.writeShort(((Integer) quest.getKey()));
            mplew.writeMapleAsciiString(quest.getValue() == null ? "" : (String) quest.getValue());
        }
    }

    /**
     * 添加宠物信息
     * @param mplew
     * @param item
     * @param pet
     */
    public static void addPetItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MaplePet pet) {
        PacketHelper.addBaseItemHeader(mplew,item);
        mplew.writeAsciiString(pet.getName(), 0x13);
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        if (item == null) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5D)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
    }


    /*
     * Rank:
     * 0 - 无标题
     * 1 - 装备
     * 2 - 消耗
     * 3 - 设置
     * 4 - 其他
     * 5 - 配方
     * 6 - 卷轴
     * 7 - 特殊
     * 8 - 8周年
     * 9 - 纽扣
     * 10 - 入场券
     * 11 - 材料
     * 12 - 冒险岛
     * 13 - 运动会
     * 14 - 级核
     * 80 - 乔
     * 81 - 海美蜜
     * 82 - 小龙
     * 83 - 李卡司
     */
    public static void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        List<MapleShopItem> shopItems = shop.getItems(c);
        mplew.writeShort(shopItems.size());
        for (MapleShopItem item : shopItems) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            if (ItemConstants.is飞镖道具(item.getItemId())) {
                mplew.writeLong(1000); //显示的购买数量
            }
            mplew.writeShort(item.getBuyable());
        }
    }


    public static <E extends Buffstat> void writeSingleMask(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = GameConstants.MAX_BUFFSTAT; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

    public static <E extends Buffstat> void writeMonsterStatusMask(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = GameConstants.MAX_MONSTERSTATUS; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

    public static <E extends Buffstat> void writeMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    //TODO 修复给怪物状态
    public static <E extends Buffstat> void writeMonsterStatusMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_MONSTERSTATUS];
        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<E, Integer>> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        for (Pair statup : statups) {
            mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }

    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Map<E, Integer> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    /**
     * 通用道具头部信息
     * CBaseItem::DecodeHeader
     * @param mplew
     * @param item
     */
    public static void addBaseItemHeader(MaplePacketLittleEndianWriter mplew,Item item) {
        mplew.writeInt(item.getItemId());
        if (item.getUniqueId() > 0) {
            mplew.write(1);
            mplew.writeLong(item.getUniqueId());
        } else {
            mplew.write(0);
        }
        PacketHelper.addExpirationTime(mplew, item.getExpiration());
    }
}
