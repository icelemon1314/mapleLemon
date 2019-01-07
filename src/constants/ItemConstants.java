package constants;

import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import java.util.ArrayList;
import server.MapleItemInformationProvider;

public class ItemConstants {

    public static final int[] rankC = {70000000, 70000001, 70000002, 70000003, 70000004, 70000005, 70000006, 70000007, 70000008, 70000009, 70000010, 70000011, 70000012, 70000013};
    public static final int[] rankB = {70000014, 70000015, 70000017, 70000018, 70000021, 70000022, 70000023, 70000024, 70000025, 70000026};
    public static final int[] rankA = {70000027, 70000028, 70000029, 70000030, 70000031, 70000032, 70000033, 70000034, 70000035, 70000036};
    public static final int[] rankS = {70000048, 70000049, 70000050, 70000051, 70000052, 70000053, 70000054, 70000055, 70000056, 70000057, 70000058, 70000059, 70000060, 70000061, 70000062};
    public static final int[] circulators = {2700000, 2700100, 2700200, 2700300, 2700400, 2700500, 2700600, 2700700, 2700800, 2700900, 2701000};
    public static final int[] rankBlock = {70000016, 70000037, 70000038, 70000039, 70000040, 70000041, 70000042, 70000043, 70000044, 70000045, 70000046, 70000047};

    public static ArrayList<Integer> get经验值卡() {
        return get经验值卡(0.0D);
    }

    public static ArrayList<Integer> get经验值卡(double type) {
        ArrayList<Integer> list = new ArrayList();
        int[] doubleCards = {
                4100000, //双倍经验值卡一天权
                4100001, //双倍经验值卡七天权
                4100002, //双倍经验值卡一天权(白)
                4100003, //双倍经验值卡七天权(白)
                4100004, //双倍经验值卡一天(晚)
                4100005, //双倍经验值卡七天权(晚)
        };
        if (type == 2.0D || type == 0.0D) {
            for (int i : doubleCards) {
                list.add(i);
            }
        }
        return list;
    }

    public static int get武器破攻上限(int itemId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        return ServerConstants.攻击上限 - (ii.getLimitBreak(itemId) > 0 ? ii.getLimitBreak(itemId) : 999999);
    }

    public static boolean isHarvesting(int itemId) {
        return (itemId >= 1500000) && (itemId < 1520000);
    }

    public static boolean is飞镖道具(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean is子弹道具(int itemId) {
        return itemId / 10000 == 233;
    }

    public static boolean isRechargable(int itemId) {
        return (is飞镖道具(itemId)) || (is子弹道具(itemId));
    }

    public static boolean isOverall(int itemId) {
        return itemId / 10000 == 105;
    }

    public static boolean isPet(int itemId) {
        return itemId / 10000 == 500;
    }

    public static boolean is弩矢道具(int itemId) {
        return (itemId >= 2061000) && (itemId < 2062000);
    }

    public static boolean is弓矢道具(int itemId) {
        return (itemId >= 2060000) && (itemId < 2061000);
    }

    public static boolean isMagicWeapon(int itemId) {
        int type = itemId / 10000;
        return (type == 137) || (type == 138) || (type == 121);
    }

    public static boolean isWeapon(int itemId) {
        if (itemId == 1342069) {
            return false;
        }
        if (isSpecialShield(itemId)) {
            return false;
        }
        return (itemId >= 1300000 && itemId < 1540000) || itemId / 1000 == 1212 || itemId / 1000 == 1222 || itemId / 1000 == 1232 || itemId / 1000 == 1242 || itemId / 1000 == 1252;
    }

    public static MapleInventoryType getInventoryType(int itemId) {
        byte type = (byte) (itemId / 1000000);
        if ((type < 1) || (type > 5)) {
            return MapleInventoryType.UNDEFINED;
        }

        return MapleInventoryType.getByType(type);
    }

    public static MapleWeaponType getWeaponType(int itemId) {
        int cat = itemId / 10000;
        cat %= 100;
        switch (cat) {
            case 30:
                return MapleWeaponType.单手剑;
            case 31:
                return MapleWeaponType.单手斧;
            case 32:
                return MapleWeaponType.单手钝器;
            case 33:
                return MapleWeaponType.短刀;
            case 36:
                return MapleWeaponType.手杖;
            case 37:
                return MapleWeaponType.短杖;
            case 38:
                return MapleWeaponType.长杖;
            case 40:
                return MapleWeaponType.双手剑;
            case 41:
                return MapleWeaponType.双手斧;
            case 42:
                return MapleWeaponType.双手钝器;
            case 43:
                return MapleWeaponType.枪;
            case 44:
                return MapleWeaponType.矛;
            case 45:
                return MapleWeaponType.弓;
            case 46:
                return MapleWeaponType.弩;
            case 47:
                return MapleWeaponType.拳套;
            case 48:
                return MapleWeaponType.指节;
            case 56:
                return MapleWeaponType.大剑;
            case 57:
                return MapleWeaponType.太刀;
        }
        return MapleWeaponType.没有武器;
    }

    public static boolean isShield(int itemId) {
        int cat = itemId / 10000;
        cat %= 100;
        return cat == 9;
    }

    public static boolean isEquip(int itemId) {
        return itemId / 1000000 == 1;
    }

    public static boolean is回城卷轴(int id) {
        return id / 10000 == 203;
    }

    public static boolean is升级卷轴(int id) {
        return id / 10000 == 204;
    }

    public static boolean is短枪道具(int id) {
        return id / 10000 == 149 && id % 1000 >= 2000;
    }

    public static boolean isUse(int id) {
        return id / 1000000 == 2;
    }

    public static boolean is怪物召唤包(int id) {
        return id / 10000 == 210;
    }

    public static boolean is怪物卡片(int id) {
        return id / 10000 == 238;
    }

    public static boolean isBoss怪物卡(int id) {
        return id / 1000 >= 2388;
    }

    public static int getCardShortId(int id) {
        return id % 10000;
    }

    public static boolean is强化宝石(int id) {
        return (id >= 4250000) && (id <= 4251402);
    }


    public static boolean isNoticeItem(int itemId) {
        switch (itemId) {
            case 2028061:
            case 2028062:
            case 2290285:
            case 2430112:
            case 4020013:
            case 4021011:
            case 4021012:
            case 4021019:
            case 4021020:
            case 4021021:
            case 4021022:
            case 4310015:
                return true;
        }
        return false;
    }


    public static boolean canScroll(int itemId) {
        return ((itemId / 100000 != 19) && (itemId / 100000 != 16)) || ((itemId / 1000 == 1672) && (itemId != 1672030) && (itemId != 1672031) && (itemId != 1672032));
    }

    public static int getLowestPrice(int itemId) {
        switch (itemId) {
            case 2340000:
            case 2530000:
            case 2531000:
                return 50000000;
        }
        return -1;
    }

    public static int getModifier(int itemId, int up) {
        if (up <= 0) {
            return 0;
        }
        switch (itemId) {
            case 2022459:
            case 2860179:
            case 2860193:
            case 2860207:
                return 130;
            case 2022460:
            case 2022462:
            case 2022730:
                return 150;
            case 2860181:
            case 2860195:
            case 2860209:
                return 200;
        }
        if (itemId / 10000 == 286) {
            return 150;
        }
        return 200;
    }

    public static short getSlotMax(int itemId) {
        switch (itemId) {
            case 4030003:
            case 4030004:
            case 4030005:
                return 1;
            case 3993000:
            case 3993002:
            case 3993003:
            case 4001168:
            case 4031306:
            case 4031307:
                return 100;
            case 5220010:
            case 5220013:
                return 1000;
            case 5220020:
                return 2000;
        }
        return 0;
    }

    public static boolean isDropRestricted(int itemId) {
        return (itemId == 3012000) || (itemId == 4030004) || (itemId == 1052098) || (itemId == 1052202);
    }

    public static boolean isPickupRestricted(int itemId) {
        return (itemId == 4030003) || (itemId == 4030004);
    }

    public static short getStat(int itemId, int def) {
        switch (itemId) {
            case 1002419:
                return 5;
            case 1002959:
                return 25;
            case 1142002:
                return 10;
            case 1122121:
                return 7;
        }
        return (short) def;
    }

    public static short getHpMp(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 500;
            case 1002959:
            case 1142002:
                return 1000;
        }
        return (short) def;
    }

    public static short getATK(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 3;
            case 1002959:
                return 4;
            case 1142002:
                return 9;
        }
        return (short) def;
    }

    public static short getDEF(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 250;
            case 1002959:
                return 500;
        }
        return (short) def;
    }

    public static int getRewardPot(int itemid, int closeness) {
        switch (itemid) {
            case 2440000:
                switch (closeness / 10) {
                    case 0:
                    case 1:
                    case 2:
                        return 2028041 + closeness / 10;
                    case 3:
                    case 4:
                    case 5:
                        return 2028046 + closeness / 10;
                    case 6:
                    case 7:
                    case 8:
                        return 2028049 + closeness / 10;
                }
                return 2028057;
            case 2440001:
                switch (closeness / 10) {
                    case 0:
                    case 1:
                    case 2:
                        return 2028044 + closeness / 10;
                    case 3:
                    case 4:
                    case 5:
                        return 2028049 + closeness / 10;
                    case 6:
                    case 7:
                    case 8:
                        return 2028052 + closeness / 10;
                }
                return 2028060;
            case 2440002:
                return 2028069;
            case 2440003:
                return 2430278;
            case 2440004:
                return 2430381;
            case 2440005:
                return 2430393;
        }
        return 0;
    }

    public static boolean isTablet(int itemId) {
//        return itemId / 1000 == 2047;
        return false;
    }

    public static boolean isGeneralScroll(int itemId) {
        return itemId / 1000 == 2046 || itemId / 1000 == 2047 || ((itemId / 10000 == 261 || itemId / 10000 == 264));
    }

    public static int getSuccessTablet(int scrollId, int level) {
        if (scrollId % 1000 / 100 == 2) {
            switch (level) {
                case 0:
                    return 70;
                case 1:
                    return 55;
                case 2:
                    return 43;
                case 3:
                    return 33;
                case 4:
                    return 26;
                case 5:
                    return 20;
                case 6:
                    return 16;
                case 7:
                    return 12;
                case 8:
                    return 10;
            }
            return 7;
        }
        if (scrollId % 1000 / 100 == 3) {
            switch (level) {
                case 0:
                    return 70;
                case 1:
                    return 35;
                case 2:
                    return 18;
                case 3:
                    return 12;
            }
            return 7;
        }

        switch (level) {
            case 0:
                return 70;
            case 1:
                return 50;
            case 2:
                return 36;
            case 3:
                return 26;
            case 4:
                return 19;
            case 5:
                return 14;
            case 6:
                return 10;
        }
        return 7;
    }

    public static int getCurseTablet(int scrollId, int level) {
        if (scrollId % 1000 / 100 == 2) {
            switch (level) {
                case 0:
                    return 10;
                case 1:
                    return 12;
                case 2:
                    return 16;
                case 3:
                    return 20;
                case 4:
                    return 26;
                case 5:
                    return 33;
                case 6:
                    return 43;
                case 7:
                    return 55;
                case 8:
                    return 70;
            }
            return 100;
        }
        if (scrollId % 1000 / 100 == 3) {
            switch (level) {
                case 0:
                    return 12;
                case 1:
                    return 18;
                case 2:
                    return 35;
                case 3:
                    return 70;
            }
            return 100;
        }

        switch (level) {
            case 0:
                return 10;
            case 1:
                return 14;
            case 2:
                return 19;
            case 3:
                return 26;
            case 4:
                return 36;
            case 5:
                return 50;
            case 6:
                return 70;
        }
        return 100;
    }

    public static boolean isAccessory(int itemId) {
        return (itemId >= 1010000 && itemId < 1040000) || (itemId >= 1122000 && itemId < 1153000) || (itemId >= 1112000 && itemId < 1113000) || (itemId >= 1670000 && itemId < 1680000);
    }

    public static boolean isRing(int itemId) {
        return (itemId >= 1112000) && (itemId < 1113000);
    }

    public static boolean isEffectRing(int itemid) {
        return (is好友戒指(itemid)) || (is恋人戒指(itemid)) || (is结婚戒指(itemid));
    }

    public static boolean is结婚戒指(int itemId) {
        switch (itemId) {
            case 1112300:
            case 1112301:
            case 1112302:
            case 1112303:
            case 1112304:
            case 1112305:
            case 1112306:
            case 1112307:
            case 1112308:
            case 1112309:
            case 1112310:
            case 1112311:
            case 1112312:
            case 1112315:
            case 1112316:
            case 1112317:
            case 1112318:
            case 1112319:
            case 1112320:
            case 1112804:
                return true;
        }
        return false;
    }

    public static boolean is好友戒指(int itemId) {
        switch (itemId) {
            case 1049000:
            case 1112800:
            case 1112801:
            case 1112802:
            case 1112810:
            case 1112811:
            case 1112812:
            case 1112817:
                return true;
        }
        return false;
    }

    public static boolean is恋人戒指(int itemId) {
        switch (itemId) {
            case 1048000:
            case 1048001:
            case 1048002:
            case 1112001:
            case 1112002:
            case 1112003:
            case 1112005:
            case 1112006:
            case 1112007:
            case 1112012:
            case 1112013:
            case 1112014:
            case 1112015:
            case 1112816:
            case 1112820:
                return true;
        }
        return false;
    }

    public static boolean isSubWeapon(int itemId) {
        switch (itemId / 10000) {
            case 135:
        }
        return true;
    }

    public static boolean isTwoHanded(int itemId) {
        return isTwoHanded(itemId, 0);
    }

    public static boolean isTwoHanded(int itemId, int job) {//砸卷不能砸一般是在这里没有判断
        switch (getWeaponType(itemId)) {
            case 双手剑:
                return (job < 6100) || (job > 6112);
            case 手杖:
                return false;
            case 双手斧:
            case 双手钝器:
            case 拳套:
            case 弓:
            case 弩:
            case 指节:
            case 枪:
            case 矛:
            case 太刀:
            case 大剑:
                return true;
            default:
                return false;
        }

    }

    public static boolean isSpecialShield(final int itemid) {
        return itemid / 1000 == 1098 || itemid / 1000 == 1099 || itemid / 10000 == 135;
    }

    public static boolean isPetEquip(final int itemid) {
        return itemid / 10000 == 180;
    }

    public static boolean is符号(final int itemid) {
        return itemid / 10000 == 119;
    }

    public static boolean isInBag(final int slot, final byte type) {
        return ((slot >= 101 && slot <= 512) && type == MapleInventoryType.ETC.getType());
    }
}
