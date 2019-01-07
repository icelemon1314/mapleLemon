package server;

import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ItemConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.wz.MapleDataType;
import tools.*;

public class MapleItemInformationProvider {

    private static final MapleItemInformationProvider instance = new MapleItemInformationProvider();
    protected MapleDataProvider chrData;
    protected MapleDataProvider etcData;
    protected MapleDataProvider itemData;
    protected final MapleDataProvider stringData;
    protected Map<Integer, ItemInformation> dataCache;
    protected Map<String, List<Triple<String, Point, Point>>> afterImage;
    protected Map<Integer, List<StructItemOption>> potentialCache;
    protected Map<Integer, Map<Integer, StructItemOption>> socketCache;
    protected Map<Integer, MapleStatEffect> itemEffects;
    protected Map<Integer, MapleStatEffect> itemEffectsEx;
    protected Map<Integer, Integer> mobIds;
    protected Map<Integer, Pair<Integer, Integer>> potLife;
    protected Map<Integer, StructFamiliar> familiars;
    protected Map<Integer, StructFamiliar> familiars_Item;
    protected Map<Integer, StructFamiliar> familiars_Mob;
    protected Map<Integer, StructSetItem> SetItemInfo;
    protected Map<Integer, Map<String, String>> getExpCardTimes;
    protected Map<Integer, ScriptedItem> scriptedItemCache;
    protected Map<Integer, Boolean> floatCashItem;
    protected Map<Integer, Short> petFlagInfo;
    protected Map<Integer, Integer> petSetItemID;
    protected Map<Integer, Integer> successRates;
    protected Map<Integer, Integer> forceUpgrade;
    protected Map<Integer, Integer> ScrollLimitBreak;
    protected Map<Integer, Pair<Integer, Integer>> chairRecovery;
    protected Map<Integer, Integer> exclusiveEquip;
    protected Map<Integer, StructExclusiveEquip> exclusiveEquipInfo;
    protected Map<Integer, Boolean> noCursedScroll;
    protected Map<Integer, Boolean> noNegativeScroll;
    private ItemInformation tmpInfo;
    protected Map<Integer, String> faceList;
    protected Map<Integer, String> hairList;

    public MapleItemInformationProvider() {
        this.chrData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Character.wz"));
        this.etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Etc.wz"));
        this.itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Item.wz"));
        this.stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));
        this.dataCache = new HashMap();
        this.afterImage = new HashMap();
        this.potentialCache = new HashMap();
        this.socketCache = new HashMap();
        this.itemEffects = new HashMap();
        this.itemEffectsEx = new HashMap();
        this.mobIds = new HashMap();
        this.potLife = new HashMap();
        this.familiars = new HashMap();
        this.familiars_Item = new HashMap();
        this.familiars_Mob = new HashMap();
        this.SetItemInfo = new HashMap();
        this.getExpCardTimes = new HashMap();
        this.scriptedItemCache = new HashMap();
        this.floatCashItem = new HashMap();
        this.petFlagInfo = new HashMap();
        this.petSetItemID = new HashMap();
        this.successRates = new HashMap();
        this.forceUpgrade = new HashMap();
        this.ScrollLimitBreak = new HashMap();
        this.chairRecovery = new HashMap();
        this.exclusiveEquip = new HashMap();
        this.exclusiveEquipInfo = new HashMap();
        this.noCursedScroll = new HashMap();
        this.noNegativeScroll = new HashMap();
        this.faceList = new TreeMap();
        this.hairList = new TreeMap();

        this.tmpInfo = null;
    }

    public void runEtc(boolean reload) {
        if (reload) {
            potentialCache.clear();
            socketCache.clear();
            potLife.clear();
            afterImage.clear();
        }
        if (!potentialCache.isEmpty() || !socketCache.isEmpty() || !potLife.isEmpty() || !afterImage.isEmpty()) {
            return;
        }
        List thePointK = new ArrayList();
        List thePointA = new ArrayList();

        MapleDataDirectoryEntry a = (MapleDataDirectoryEntry) this.chrData.getRoot().getEntry("Afterimage");
        for (MapleDataEntry b : a.getFiles()) {
            MapleData iz = this.chrData.getData("Afterimage/" + b.getName());
            List thePoint = new ArrayList();
            Map<String, Pair> dummy = new HashMap();
            for (MapleData i : iz) {
                for (MapleData xD : i) {
                    if ((xD.getName().contains("prone")) || (xD.getName().contains("double")) || (xD.getName().contains("triple")) || (((b.getName().contains("bow")) || (b.getName().contains("Bow"))) && ((!xD.getName().contains("shoot")) || (((b.getName().contains("gun")) || (b.getName().contains("cannon"))) && (!xD.getName().contains("shot")))))) {
                        continue;
                    }
                    if (dummy.containsKey(xD.getName())) {
                        if (xD.getChildByPath("lt") != null) {
                            Point lt = (Point) xD.getChildByPath("lt").getData();
                            Point ourLt = (Point) ((Pair) dummy.get(xD.getName())).left;
                            if (lt.x < ourLt.x) {
                                ourLt.x = lt.x;
                            }
                            if (lt.y < ourLt.y) {
                                ourLt.y = lt.y;
                            }
                        }
                        if (xD.getChildByPath("rb") != null) {
                            Point rb = (Point) xD.getChildByPath("rb").getData();
                            Point ourRb = (Point) ((Pair) dummy.get(xD.getName())).right;
                            if (rb.x > ourRb.x) {
                                ourRb.x = rb.x;
                            }
                            if (rb.y > ourRb.y) {
                                ourRb.y = rb.y;
                            }
                        }
                    } else {
                        Point lt = null;
                        Point rb = null;
                        if (xD.getChildByPath("lt") != null) {
                            lt = (Point) xD.getChildByPath("lt").getData();
                        }
                        if (xD.getChildByPath("rb") != null) {
                            rb = (Point) xD.getChildByPath("rb").getData();
                        }
                        dummy.put(xD.getName(), new Pair(lt, rb));
                    }
                }
            }
            for (Entry<String, Pair> ez : dummy.entrySet()) {
                if ((((String) ez.getKey()).length() > 2) && (((String) ez.getKey()).substring(((String) ez.getKey()).length() - 2, ((String) ez.getKey()).length() - 1).equals("D"))) {
                    thePointK.add(new Triple(ez.getKey(), ((Pair) ez.getValue()).left, ((Pair) ez.getValue()).right));
                } else if (((String) ez.getKey()).contains("PoleArm")) {
                    thePointA.add(new Triple(ez.getKey(), ((Pair) ez.getValue()).left, ((Pair) ez.getValue()).right));
                } else {
                    thePoint.add(new Triple(ez.getKey(), ((Pair) ez.getValue()).left, ((Pair) ez.getValue()).right));
                }
            }
            this.afterImage.put(b.getName().substring(0, b.getName().length() - 4), thePoint);
        }
        this.afterImage.put("katara", thePointK);
        this.afterImage.put("aran", thePointA);
    }

    public void runItems(boolean reload) {
        if (reload) {
            dataCache.clear();
        }
        if (!dataCache.isEmpty()) {
            return;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wz_itemdata");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                initItemInformation(rs);
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM wz_itemequipdata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemEquipData(rs);
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM wz_itemadddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemAddData(rs);
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM wz_itemrewarddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemRewardData(rs);
            }
            rs.close();
            ps.close();

            for (Map.Entry entry : this.dataCache.entrySet()) {
                if (ItemConstants.getInventoryType(((Integer) entry.getKey())) == MapleInventoryType.EQUIP) {
                    finalizeEquipData((ItemInformation) entry.getValue());
                }
            }
        } catch (SQLException ex) {
            FileoutputUtil.log("[ItemLoader] 加载装备数据出错." + ex);
        }
    }

    public final void loadHairFace(boolean reload) {
        if (reload) {
            hairList.clear();
            faceList.clear();
        }
        if (!hairList.isEmpty() || !faceList.isEmpty()) {
            return;
        }
        String[] types = {"Hair", "Face"};
        for (String type : types) {
            MapleDataDirectoryEntry data = null;
            for (MapleDataDirectoryEntry d : chrData.getRoot().getSubdirectories()) {
                if (d.getName().equals(type)) {
                    data = d; 
                    break;
                }
            }
            if (data == null) {
                continue;
            }
            for (MapleData c : stringData.getData("Item.img").getChildByPath("Eqp/" + type)) {
                if (data.getEntry(StringUtil.getLeftPaddedStr(c.getName() + ".img", '0', 12)) != null) {
                    int dataid = Integer.parseInt(c.getName());
                    String name = MapleDataTool.getString("name", c, "无名字");
                    if (type.equals("Hair")) {
                        hairList.put(dataid, name);
                    } else {
                        faceList.put(dataid, name);
                    }
                }
            }
        }
    }

    public boolean hairExists(int hair) {
        return hairList.containsKey(hair);
    }

    public boolean faceExists(int face) {
        return faceList.containsKey(face);
    }

    public final Map<Integer, String> getHairList() {
        Map<Integer, String> list = new TreeMap();
        list.putAll(hairList);
        return list;
    }

    public final Map<Integer, String> getFaceList() {
        Map<Integer, String> list = new TreeMap();
        list.putAll(faceList);
        return list;
    }


    public Map<Integer, StructItemOption> getAllSocketInfo(int grade) {
        return (Map) this.socketCache.get(grade);
    }

    public Pair<Integer, Integer> getPot(int f) {
        return (Pair) this.potLife.get(f);
    }

    public StructFamiliar getFamiliar(int f) {
        return (StructFamiliar) this.familiars.get(f);
    }

    public Map<Integer, StructFamiliar> getFamiliars() {
        return this.familiars;
    }

    public StructFamiliar getFamiliarByItem(int f) {
        return (StructFamiliar) this.familiars_Item.get(f);
    }

    public StructFamiliar getFamiliarByMob(int f) {
        return (StructFamiliar) this.familiars_Mob.get(f);
    }

    public static MapleItemInformationProvider getInstance() {
        return instance;
    }

    public Collection<ItemInformation> getAllItems() {
        Map<Integer, ItemInformation> mapVK = new TreeMap<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer obj1, Integer obj2) {
                        Integer v1 = obj1;
                        Integer v2 = obj2;
                        int s = v1.compareTo(v2);
                        return s;
                    }
                }
        );

        Set col = dataCache.keySet();
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            Integer key = (Integer) iter.next();
            ItemInformation value = (ItemInformation) dataCache.get(key);
            mapVK.put(key, value);
        }
        return mapVK.values();
    }

    protected MapleData getItemData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = this.itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = this.itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                }
                if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    ret = this.itemData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        root = this.chrData.getRoot();
        for (final MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    ret = this.chrData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        return ret;
    }

    public Integer getItemIdByMob(int mobId) {
        return this.mobIds.get(mobId);
    }

    public Integer getSetId(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.cardSet;
    }

    public short getSlotMax(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.slotMax;
    }

    public int getWholePrice(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.wholePrice;
    }

    public double getPrice(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return -1.0D;
        }
        return i.price;
    }

    protected int rand(int min, int max) {
        return Math.abs(Randomizer.rand(min, max));
    }

    public Equip levelUpEquip(Equip equip, Map<String, Integer> sta) {
        Equip nEquip = (Equip) equip.copy();
        try {
            for (Map.Entry stat : sta.entrySet()) {
                switch ((String) stat.getKey()) {
                    case "STRMin":
                        nEquip.setStr((short) (nEquip.getStr() + rand(((Integer) stat.getValue()), (sta.get("STRMax")))));
                        break;
                    case "DEXMin":
                        nEquip.setDex((short) (nEquip.getDex() + rand(((Integer) stat.getValue()), (sta.get("DEXMax")))));
                        break;
                    case "INTMin":
                        nEquip.setInt((short) (nEquip.getInt() + rand(((Integer) stat.getValue()), (sta.get("INTMax")))));
                        break;
                    case "LUKMin":
                        nEquip.setLuk((short) (nEquip.getLuk() + rand(((Integer) stat.getValue()), (sta.get("LUKMax")))));
                        break;
                    case "PADMin":
                        nEquip.setWatk((short) (nEquip.getWatk() + rand(((Integer) stat.getValue()), (sta.get("PADMax")))));
                        break;
                    case "PDDMin":
                        nEquip.setWdef((short) (nEquip.getWdef() + rand(((Integer) stat.getValue()), (sta.get("PDDMax")))));
                        break;
                    case "MADMin":
                        nEquip.setMatk((short) (nEquip.getMatk() + rand(((Integer) stat.getValue()), (sta.get("MADMax")))));
                        break;
                    case "MDDMin":
                        nEquip.setMdef((short) (nEquip.getMdef() + rand(((Integer) stat.getValue()), (sta.get("MDDMax")))));
                        break;
                    case "ACCMin":
                        nEquip.setAcc((short) (nEquip.getAcc() + rand(((Integer) stat.getValue()), (sta.get("ACCMax")))));
                        break;
                    case "EVAMin":
                        nEquip.setAvoid((short) (nEquip.getAvoid() + rand(((Integer) stat.getValue()), (sta.get("EVAMax")))));
                        break;
                    case "SpeedMin":
                        nEquip.setSpeed((short) (nEquip.getSpeed() + rand(((Integer) stat.getValue()), (sta.get("SpeedMax")))));
                        break;
                    case "JumpMin":
                        nEquip.setJump((short) (nEquip.getJump() + rand(((Integer) stat.getValue()), (sta.get("JumpMax")))));
                        break;
                    case "MHPMin":
                        nEquip.setHp((short) (nEquip.getHp() + rand(((Integer) stat.getValue()), (sta.get("MHPMax")))));
                        break;
                    case "MMPMin":
                        nEquip.setMp((short) (nEquip.getMp() + rand(((Integer) stat.getValue()), (sta.get("MMPMax")))));
                        break;
                    case "MaxHPMin":
                        nEquip.setHp((short) (nEquip.getHp() + rand(((Integer) stat.getValue()), (sta.get("MaxHPMax")))));
                        break;
                    case "MaxMPMin":
                        nEquip.setMp((short) (nEquip.getMp() + rand(((Integer) stat.getValue()), (sta.get("MaxMPMax")))));
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return nEquip;
    }

    public List<Triple<String, String, String>> getEquipAdditions(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipAdditions;
    }

    public String getEquipAddReqs(int itemId, String key, String sub) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        for (Triple data : i.equipAdditions) {
            if ((((String) data.getLeft()).equals("key")) && (((String) data.getMid()).equals("con:" + sub))) {
                return (String) data.getRight();
            }
        }
        return null;
    }

    public Map<Integer, Map<String, Integer>> getEquipIncrements(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipIncs;
    }

    public Map<String, Integer> getEquipStats(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipStats;
    }

    public boolean canEquip(Map<String, Integer> stats, int itemid, int level, int job, int fame, int str, int dex, int luk, int int_, int supremacy) {
        if (level + supremacy >= (stats.containsKey("reqLevel") ? (stats.get("reqLevel")) : 0)) {
            if (str >= (stats.containsKey("reqSTR") ? (stats.get("reqSTR")) : 0)) {
                if (dex >= (stats.containsKey("reqDEX") ? (stats.get("reqDEX")) : 0)) {
                    if (luk >= (stats.containsKey("reqLUK") ? (stats.get("reqLUK")) : 0)) {
                        if (int_ >= (stats.containsKey("reqINT") ? (stats.get("reqINT")) : 0)) {
                            Integer fameReq = stats.get("reqPOP");

                            return (fameReq == null) || (fame >= fameReq);
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getReqLevel(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("reqLevel"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("reqLevel"));
    }

    public int getReqJob(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("reqJob"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("reqJob"));
    }

    public int getSlots(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("tuc"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("tuc"));
    }

    public Integer getSetItemID(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("setItemID"))) {
            return 0;
        }
        return getEquipStats(itemId).get("setItemID");
    }

    public StructSetItem getSetItem(int setItemId) {
        return (StructSetItem) this.SetItemInfo.get(setItemId);
    }

    public List<Integer> getScrollReqs(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.scrollReqs;
    }

    public int getScrollSuccess(int itemId) {
        if ((itemId / 10000 != 204) || (getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("success"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("success"));
    }

    /**
     * 对装备进行砸卷
     * @param equip
     * @param scroll
     * @param whiteScroll
     * @param chr
     * @param vegas
     * @return
     */
    public Item scrollEquipWithId(Item equip, Item scroll, boolean whiteScroll, MapleCharacter chr, int vegas) {
        if (equip.getType() == 1) {
            int scrollId = scroll.getItemId();
            final Equip nEquip = (Equip) equip;
            final Map<String, Integer> scrollStats = getEquipStats(scrollId);  //卷轴信息
            final Map<String, Integer> equipStats = getEquipStats(equip.getItemId()); // 装备信息

            int succ = scrollStats == null || !scrollStats.containsKey("success") ? 0 : ItemConstants.isTablet(scrollId) ? ItemConstants.getSuccessTablet(scrollId, nEquip.getLevel()) : scrollStats.get("success");

            //诅咒卷轴
            int curse = scrollStats == null || !scrollStats.containsKey("cursed") ? 0 : ItemConstants.isTablet(scrollId) ? ItemConstants.getCurseTablet(scrollId, nEquip.getLevel()) : scrollStats.get("cursed");

            int limitedLv = scrollStats == null || !scrollStats.containsKey("limitedLv") ? 0 : scrollStats.get("limitedLv");

            if (limitedLv > 0 && nEquip.getLevel() < limitedLv) {
                chr.dropMessage(1,"装备等级不够："+limitedLv );
                return nEquip;
            }
            if (nEquip.getUpgradeSlots()<=0) {
                chr.dropMessage(1,"装备已经没有升级次数了："+nEquip.getUpgradeSlots() );
                return nEquip;
            }

            int success = succ + succ * (getSuccessRates(scroll.getItemId())) / 100;
            if (chr.isShowPacket()) {
                chr.dropSpouseMessage(11, "普通卷轴 - 默认几率: " + succ + "% 最终概率: " + success + "% 失败消失几率: " + curse + "%");
            }
            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
            if (Randomizer.nextInt(100) <= success) {
                for (Map.Entry stat : scrollStats.entrySet()) {
                    String key = (String) stat.getKey();
                    switch (key) {
                        case "STR":
                            nEquip.setStr((short) (nEquip.getStr() + ((Integer) stat.getValue())));
                            break;
                        case "DEX":
                            nEquip.setDex((short) (nEquip.getDex() + ((Integer) stat.getValue())));
                            break;
                        case "INT":
                            nEquip.setInt((short) (nEquip.getInt() + ((Integer) stat.getValue())));
                            break;
                        case "LUK":
                            nEquip.setLuk((short) (nEquip.getLuk() + ((Integer) stat.getValue())));
                            break;
                        case "PAD":
                            nEquip.setWatk((short) (nEquip.getWatk() + ((Integer) stat.getValue())));
                            break;
                        case "PDD":
                            nEquip.setWdef((short) (nEquip.getWdef() + ((Integer) stat.getValue())));
                            break;
                        case "MAD":
                            nEquip.setMatk((short) (nEquip.getMatk() + ((Integer) stat.getValue())));
                            break;
                        case "MDD":
                            nEquip.setMdef((short) (nEquip.getMdef() + ((Integer) stat.getValue())));
                            break;
                        case "ACC":
                            nEquip.setAcc((short) (nEquip.getAcc() + ((Integer) stat.getValue())));
                            break;
                        case "EVA":
                            nEquip.setAvoid((short) (nEquip.getAvoid() + ((Integer) stat.getValue())));
                            break;
                        case "Speed":
                            nEquip.setSpeed((short) (nEquip.getSpeed() + ((Integer) stat.getValue())));
                            break;
                        case "Jump":
                            nEquip.setJump((short) (nEquip.getJump() + ((Integer) stat.getValue())));
                            break;
                        case "MHP":
                            nEquip.setHp((short) (nEquip.getHp() + ((Integer) stat.getValue())));
                            break;
                        case "MMP":
                            nEquip.setMp((short) (nEquip.getMp() + ((Integer) stat.getValue())));
                            break;
                    }
                }
                nEquip.setLevel((byte) (nEquip.getLevel() + 1));
            } else {
                if (Randomizer.nextInt(99) < curse) {
                    return null;
                }
            }
        }
        return equip;
    }

    public Item scrollEnhance(Item equip, Item scroll, MapleCharacter chr) {
        if (equip.getType() != 1) {
            return equip;
        }
        Equip nEquip = (Equip) equip;
        int scrollId = scroll.getItemId();
        Map scrollStats = getEquipStats(scrollId);
        boolean noCursed = isNoCursedScroll(scrollId);
        int scrollForceUpgrade = getForceUpgrade(scrollId);
        int succ = scrollStats == null || !scrollStats.containsKey("success") ? 0 : (Integer) scrollStats.get("success");
        int curse = noCursed ? 0 : scrollStats == null || !scrollStats.containsKey("cursed") ? 100 : ((Integer) scrollStats.get("cursed"));
        if ((scrollForceUpgrade == 1) && (succ == 0)) {
            succ = Math.max(((scroll.getItemId() == 2049301) || (scroll.getItemId() == 2049307) ? 80 : 100) - nEquip.getEnhance() * 10, 5);
        }
        int success = succ;
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(11, "装备强化卷轴 - 默认几率: " + succ + "% 倾向加成: " + 0 + "% 最终几率: " + success + "% 失败消失几率: " + curse + "%" + " 卷轴是否失败不消失装备: " + noCursed);
        }
        if (Randomizer.nextInt(100) > success) {
            return Randomizer.nextInt(99) < curse ? null : nEquip;
        }
        int mixStats = isSuperiorEquip(nEquip.getItemId()) ? 3 : 0;
        int maxStats = isSuperiorEquip(nEquip.getItemId()) ? 8 : 5;
        for (int i = 0; i < scrollForceUpgrade; i++) {
            if (nEquip.getStr() > 0 || Randomizer.nextInt(50) == 1) {
                nEquip.setStr((short) (nEquip.getStr() + Randomizer.rand(mixStats, maxStats)));
            }
            if (nEquip.getDex() > 0 || Randomizer.nextInt(50) == 1) {
                nEquip.setDex((short) (nEquip.getDex() + Randomizer.rand(mixStats, maxStats)));
            }
            if (nEquip.getInt() > 0 || Randomizer.nextInt(50) == 1) {
                nEquip.setInt((short) (nEquip.getInt() + Randomizer.rand(mixStats, maxStats)));
            }
            if (nEquip.getLuk() > 0 || Randomizer.nextInt(50) == 1) {
                nEquip.setLuk((short) (nEquip.getLuk() + Randomizer.rand(mixStats, maxStats)));
            }
            if (nEquip.getWatk() > 0 && (ItemConstants.isWeapon(nEquip.getItemId()))) {
                if (nEquip.getWatk() < 150) {
                    nEquip.setWatk((short) (nEquip.getWatk() + 3));
                } else if (nEquip.getWatk() < 200) {
                    nEquip.setWatk((short) (nEquip.getWatk() + 4));
                } else if (nEquip.getWatk() < 250) {
                    nEquip.setWatk((short) (nEquip.getWatk() + 5));
                } else {
                    nEquip.setWatk((short) (nEquip.getWatk() + 5 + (Randomizer.nextBoolean() ? 1 : 0)));
                }
            }
            if (nEquip.getWdef() > 0 || Randomizer.nextInt(40) == 1) {
                nEquip.setWdef((short) (nEquip.getWdef() + Randomizer.nextInt(5)));
            }
            if (nEquip.getMatk() > 0 && ItemConstants.isWeapon(nEquip.getItemId())) {
                if (nEquip.getMatk() < 50) {
                    nEquip.setMatk((short) (nEquip.getMatk() + 1));
                } else if (nEquip.getMatk() < 100) {
                    nEquip.setMatk((short) (nEquip.getMatk() + 2));
                } else if (nEquip.getMatk() < 150) {
                    nEquip.setMatk((short) (nEquip.getMatk() + 3));
                } else if (nEquip.getMatk() < 200) {
                    nEquip.setMatk((short) (nEquip.getMatk() + 4));
                } else if (nEquip.getMatk() < 250) {
                    nEquip.setMatk((short) (nEquip.getMatk() + 5));
                } else {
                    nEquip.setMatk((short) (nEquip.getMatk() + 5 + (Randomizer.nextBoolean() ? 1 : 0)));
                }
            }
            if (nEquip.getMdef() > 0 || Randomizer.nextInt(40) == 1) {
                nEquip.setMdef((short) (nEquip.getMdef() + Randomizer.nextInt(5)));
            }
            if (nEquip.getAcc() > 0 || Randomizer.nextInt(20) == 1) {
                nEquip.setAcc((short) (nEquip.getAcc() + Randomizer.nextInt(5)));
            }
            if (nEquip.getAvoid() > 0 || Randomizer.nextInt(20) == 1) {
                nEquip.setAvoid((short) (nEquip.getAvoid() + Randomizer.nextInt(5)));
            }
            if (nEquip.getSpeed() > 0 || Randomizer.nextInt(10) == 1) {
                nEquip.setSpeed((short) (nEquip.getSpeed() + Randomizer.nextInt(5)));
            }
            if (nEquip.getJump() > 0 || Randomizer.nextInt(10) == 1) {
                nEquip.setJump((short) (nEquip.getJump() + Randomizer.nextInt(5)));
            }
            if (nEquip.getHp() > 0 || Randomizer.nextInt(5) == 1) {
                nEquip.setHp((short) (nEquip.getHp() + Randomizer.rand(mixStats, maxStats)));
            }
            if (nEquip.getMp() > 0 || Randomizer.nextInt(5) == 1) {
                nEquip.setMp((short) (nEquip.getMp() + Randomizer.rand(mixStats, maxStats)));
            }
            nEquip.setEnhance((byte) (nEquip.getEnhance() + 1));
        }
        return nEquip;
    }

    public Equip resetEquipStats(Equip oldEquip) {
        Equip newEquip = (Equip) getEquipById(oldEquip.getItemId());
        oldEquip.reset(newEquip);
        return newEquip;
    }

    public Item getEquipById(int equipId) {
        return getEquipById(equipId, -1);
    }

    public Item getEquipById(int equipId, int ringId) {
        ItemInformation i = getItemInformation(equipId);
        if (i == null) {
            return new Equip(equipId, (short) 0, ringId, (short) 0);
        }
        Item eq = i.eq.copy();
        eq.setUniqueId(ringId);
        return eq;
    }

    protected short getRandStatFusion(short defaultValue, int value1, int value2) {
        if (defaultValue == 0) {
            return 0;
        }
        int range = (value1 + value2) / 2 - defaultValue;
        int rand = Randomizer.nextInt(Math.abs(range) + 1);
        return (short) (defaultValue + (range < 0 ? -rand : rand));
    }

    protected short getRandStat(short defaultValue, int maxRange) {
        if (defaultValue == 0) {
            return 0;
        }

        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1D), maxRange);
        return (short) (defaultValue - lMaxRange + Randomizer.nextInt(lMaxRange * 2 + 1));
    }

    protected short getRandStatAbove(short defaultValue, int maxRange) {
        if (defaultValue <= 0) {
            return 0;
        }
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1D), maxRange);
        return (short) (defaultValue + Randomizer.nextInt(lMaxRange + 1));
    }

    public Equip randomizeStats(Equip equip) {
        equip.setStr(getRandStat(equip.getStr(), 5));
        equip.setDex(getRandStat(equip.getDex(), 5));
        equip.setInt(getRandStat(equip.getInt(), 5));
        equip.setLuk(getRandStat(equip.getLuk(), 5));
        equip.setMatk(getRandStat(equip.getMatk(), 5));
        equip.setWatk(getRandStat(equip.getWatk(), 5));
        equip.setAcc(getRandStat(equip.getAcc(), 5));
        equip.setAvoid(getRandStat(equip.getAvoid(), 5));
        equip.setJump(getRandStat(equip.getJump(), 5));
        equip.setHands(getRandStat(equip.getHands(), 5));
        equip.setSpeed(getRandStat(equip.getSpeed(), 5));
        equip.setWdef(getRandStat(equip.getWdef(), 10));
        equip.setMdef(getRandStat(equip.getMdef(), 10));
        equip.setHp(getRandStat(equip.getHp(), 10));
        equip.setMp(getRandStat(equip.getMp(), 10));
        return equip;
    }

    public Equip randomizeStats_Above(Equip equip) {
        equip.setStr(getRandStatAbove(equip.getStr(), 5));
        equip.setDex(getRandStatAbove(equip.getDex(), 5));
        equip.setInt(getRandStatAbove(equip.getInt(), 5));
        equip.setLuk(getRandStatAbove(equip.getLuk(), 5));
        equip.setMatk(getRandStatAbove(equip.getMatk(), 5));
        equip.setWatk(getRandStatAbove(equip.getWatk(), 5));
        equip.setAcc(getRandStatAbove(equip.getAcc(), 5));
        equip.setAvoid(getRandStatAbove(equip.getAvoid(), 5));
        equip.setJump(getRandStatAbove(equip.getJump(), 5));
        equip.setHands(getRandStatAbove(equip.getHands(), 5));
        equip.setSpeed(getRandStatAbove(equip.getSpeed(), 5));
        equip.setWdef(getRandStatAbove(equip.getWdef(), 10));
        equip.setMdef(getRandStatAbove(equip.getMdef(), 10));
        equip.setHp(getRandStatAbove(equip.getHp(), 10));
        equip.setMp(getRandStatAbove(equip.getMp(), 10));
        return equip;
    }

    public Equip fuse(Equip equip1, Equip equip2) {
        if (equip1.getItemId() != equip2.getItemId()) {
            return equip1;
        }
        Equip equip = (Equip) getEquipById(equip1.getItemId());
        equip.setStr(getRandStatFusion(equip.getStr(), equip1.getStr(), equip2.getStr()));
        equip.setDex(getRandStatFusion(equip.getDex(), equip1.getDex(), equip2.getDex()));
        equip.setInt(getRandStatFusion(equip.getInt(), equip1.getInt(), equip2.getInt()));
        equip.setLuk(getRandStatFusion(equip.getLuk(), equip1.getLuk(), equip2.getLuk()));
        equip.setMatk(getRandStatFusion(equip.getMatk(), equip1.getMatk(), equip2.getMatk()));
        equip.setWatk(getRandStatFusion(equip.getWatk(), equip1.getWatk(), equip2.getWatk()));
        equip.setAcc(getRandStatFusion(equip.getAcc(), equip1.getAcc(), equip2.getAcc()));
        equip.setAvoid(getRandStatFusion(equip.getAvoid(), equip1.getAvoid(), equip2.getAvoid()));
        equip.setJump(getRandStatFusion(equip.getJump(), equip1.getJump(), equip2.getJump()));
        equip.setHands(getRandStatFusion(equip.getHands(), equip1.getHands(), equip2.getHands()));
        equip.setSpeed(getRandStatFusion(equip.getSpeed(), equip1.getSpeed(), equip2.getSpeed()));
        equip.setWdef(getRandStatFusion(equip.getWdef(), equip1.getWdef(), equip2.getWdef()));
        equip.setMdef(getRandStatFusion(equip.getMdef(), equip1.getMdef(), equip2.getMdef()));
        equip.setHp(getRandStatFusion(equip.getHp(), equip1.getHp(), equip2.getHp()));
        equip.setMp(getRandStatFusion(equip.getMp(), equip1.getMp(), equip2.getMp()));
        return equip;
    }

    public int get休彼德蔓徽章点数(int itemId) {
        switch (itemId) {
            case 1182000:
                return 3;
            case 1182001:
                return 5;
            case 1182002:
                return 7;
            case 1182003:
                return 9;
            case 1182004:
                return 13;
            case 1182005:
                return 16;
        }
        return 0;
    }

    public Equip randomize休彼德蔓徽章(Equip equip) {
        int stats = get休彼德蔓徽章点数(equip.getItemId());
        if (stats > 0) {
            int prob = equip.getItemId() - 1182000;
            if (Randomizer.nextInt(15) <= prob) {
                equip.setStr((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) {
                equip.setDex((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) {
                equip.setInt((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) {
                equip.setLuk((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(30) <= prob) {
                equip.setWatk((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) {
                equip.setWdef((short) Randomizer.nextInt(stats * 8));
            }
            if (Randomizer.nextInt(30) <= prob) {
                equip.setMatk((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) {
                equip.setMdef((short) Randomizer.nextInt(stats * 8));
            }
            if (Randomizer.nextInt(8) <= prob) {
                equip.setAcc((short) Randomizer.nextInt(stats * 5));
            }
            if (Randomizer.nextInt(8) <= prob) {
                equip.setAvoid((short) Randomizer.nextInt(stats * 5));
            }
            if (Randomizer.nextInt(10) <= prob) {
                equip.setSpeed((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) {
                equip.setJump((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(8) <= prob) {
                equip.setHp((short) Randomizer.nextInt(stats * 10));
            }
            if (Randomizer.nextInt(8) <= prob) {
                equip.setMp((short) Randomizer.nextInt(stats * 10));
            }
        }
        return equip;
    }

    public int getTotalStat(Equip equip) {
        return equip.getStr() + equip.getDex() + equip.getInt() + equip.getLuk() + equip.getMatk() + equip.getWatk() + equip.getAcc() + equip.getAvoid() + equip.getJump() + equip.getHands() + equip.getSpeed() + equip.getHp() + equip.getMp() + equip.getWdef() + equip.getMdef();
    }

    public MapleStatEffect getItemEffect(int itemId) {
        MapleStatEffect ret = this.itemEffects.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if ((item == null) || (item.getChildByPath("spec") == null)) {
                return null;
            }
            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
            this.itemEffects.put(itemId, ret);
        }
        return ret;
    }

    public MapleStatEffect getItemEffectEX(int itemId) {
        MapleStatEffect ret = (MapleStatEffect) this.itemEffectsEx.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if ((item == null) || (item.getChildByPath("specEx") == null)) {
                return null;
            }
            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("specEx"), itemId);
            this.itemEffectsEx.put(itemId, ret);
        }
        return ret;
    }

    public int getCreateId(int id) {
        ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.create;
    }

    public int getBagType(int id) {
        ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.flag & 0xF;
    }

    public int getWatkForProjectile(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if ((i == null) || (i.equipStats == null) || (i.equipStats.get("PAD") == null)) {
            return 0;
        }
        return (i.equipStats.get("PAD"));
    }

    public boolean canScroll(int scrollid, int itemid) {
        return (scrollid / 100 % 100 == itemid / 10000 % 100) || ((itemid >= 1672000) && (itemid <= 1672010));
    }

    public String getName(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.name;
    }

    public String getDesc(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.desc;
    }

    public String getMsg(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.msg;
    }

    public short getItemMakeLevel(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.itemMakeLevel;
    }

    public boolean cantSell(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x10) != 0;
    }

    public boolean isLogoutExpire(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x20) != 0;
    }

    public boolean isPickupBlocked(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x40) != 0;
    }

    public boolean isPickupRestricted(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (((i.flag & 0x80) != 0) || (ItemConstants.isPickupRestricted(itemId))) && (itemId != 4001168);
    }

    public boolean isAccountShared(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x100) != 0;
    }

    public boolean isQuestItem(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return ((i.flag & 0x200) != 0) && (itemId / 10000 != 301);
    }

    public boolean isDropRestricted(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return ((i.flag & 0x200) != 0) || ((i.flag & 0x400) != 0) || (ItemConstants.isDropRestricted(itemId));
    }

    public boolean isShareTagEnabled(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x800) != 0;
    }

    public boolean isMobHP(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x1000) != 0;
    }

    public boolean isActivatedSocketItem(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x2000) != 0;
    }

    public boolean isSuperiorEquip(int itemId) {
        Map<String, Integer> equipStats = getEquipStats(itemId);
        if (equipStats == null) {
            return false;
        }
        return equipStats.containsKey("superiorEqp") && equipStats.get("superiorEqp") == 1;
    }

    public boolean isOnlyEquip(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x8000) != 0;
    }

    public int getStateChangeItem(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.stateChange;
    }

    public int getMeso(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.meso;
    }

    public boolean isKarmaEnabled(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 1;
    }

    public boolean isPKarmaEnabled(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 2;
    }

    public Pair<Integer, List<StructRewardItem>> getRewardItem(int itemid) {
        ItemInformation i = getItemInformation(itemid);
        if (i == null) {
            return null;
        }
        return new Pair(i.totalprob, i.rewardItems);
    }

    public Pair<Integer, List<Integer>> questItemInfo(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair(i.questId, i.questItems);
    }

    public Pair<Integer, String> replaceItemInfo(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair(i.replaceItem, i.replaceMsg);
    }

    public List<Triple<String, Point, Point>> getAfterImage(String after) {
        return (List) this.afterImage.get(after);
    }

    public String getAfterImage(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.afterImage;
    }

    public boolean itemExists(int itemId) {
        if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.UNDEFINED) {
            return false;
        }
        return getItemInformation(itemId) != null;
    }

    public boolean isCash(int itemId) {
        if (getEquipStats(itemId) == null) {
            return ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH;
        }
        return (ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH) || (getEquipStats(itemId).get("cash") != null);
    }

    public ItemInformation getItemInformation(int itemId) {
        if (itemId <= 0) {
            return null;
        }
        return this.dataCache.get(itemId);
    }

    public void initItemRewardData(ResultSet sqlRewardData)
            throws SQLException {
        int itemID = sqlRewardData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(itemID)) {
                FileoutputUtil.log("[initItemRewardData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(itemID));
        }

        if (this.tmpInfo.rewardItems == null) {
            this.tmpInfo.rewardItems = new ArrayList();
        }

        StructRewardItem add = new StructRewardItem();
        add.itemid = sqlRewardData.getInt("item");
        add.period = (add.itemid == 1122017 ? Math.max(sqlRewardData.getInt("period"), 7200) : sqlRewardData.getInt("period"));

        add.prob = (add.itemid == 2511117 ? 3 : sqlRewardData.getInt("prob"));
        add.quantity = sqlRewardData.getShort("quantity");
        add.worldmsg = (sqlRewardData.getString("worldMsg").length() <= 0 ? null : sqlRewardData.getString("worldMsg"));
        add.effect = sqlRewardData.getString("effect");

        this.tmpInfo.rewardItems.add(add);
    }

    public void initItemAddData(ResultSet sqlAddData) throws SQLException {
        int itemID = sqlAddData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(itemID)) {
                FileoutputUtil.log("[initItemAddData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(itemID));
        }

        if (this.tmpInfo.equipAdditions == null) {
            this.tmpInfo.equipAdditions = new LinkedList();
        }
        this.tmpInfo.equipAdditions.add(new Triple(sqlAddData.getString("key"), sqlAddData.getString("subKey"), sqlAddData.getString("value")));
    }

    public void initItemEquipData(ResultSet sqlEquipData) throws SQLException {
        int itemID = sqlEquipData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(itemID)) {
                FileoutputUtil.log("[initItemEquipData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(itemID));
        }

        if (this.tmpInfo.equipStats == null) {
            this.tmpInfo.equipStats = new HashMap();
        }

        int itemLevel = sqlEquipData.getInt("itemLevel");
        if (itemLevel == -1) {
            this.tmpInfo.equipStats.put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
        } else {
            if (this.tmpInfo.equipIncs == null) {
                this.tmpInfo.equipIncs = new HashMap();
            }
            Map toAdd = (Map) this.tmpInfo.equipIncs.get(Integer.valueOf(itemLevel));
            if (toAdd == null) {
                toAdd = new HashMap();
                this.tmpInfo.equipIncs.put(itemLevel, toAdd);
            }
            toAdd.put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
        }
    }

    public void finalizeEquipData(ItemInformation item) {
        int itemId = item.itemId;

        if (item.equipStats == null) {
            item.equipStats = new HashMap();
        }

        item.eq = new Equip(itemId, (short) 0, -1, (short) 0);
        short stats = ItemConstants.getStat(itemId, 0);
        if (stats > 0) {
            item.eq.setStr(stats);
            item.eq.setDex(stats);
            item.eq.setInt(stats);
            item.eq.setLuk(stats);
        }
        stats = ItemConstants.getATK(itemId, 0);
        if (stats > 0) {
            item.eq.setWatk(stats);
            item.eq.setMatk(stats);
        }
        stats = ItemConstants.getHpMp(itemId, 0);
        if (stats > 0) {
            item.eq.setHp(stats);
            item.eq.setMp(stats);
        }
        stats = ItemConstants.getDEF(itemId, 0);
        if (stats > 0) {
            item.eq.setWdef(stats);
            item.eq.setMdef(stats);
        }
        if (item.equipStats.size() > 0) {
            for (Map.Entry stat : item.equipStats.entrySet()) {
                String key = (String) stat.getKey();
                switch (key) {
                    case "STR":
                        item.eq.setStr(ItemConstants.getStat(itemId, ((int) stat.getValue())));
                        break;
                    case "DEX":
                        item.eq.setDex(ItemConstants.getStat(itemId, ((int) stat.getValue())));
                        break;
                    case "INT":
                        item.eq.setInt(ItemConstants.getStat(itemId, ((int) stat.getValue())));
                        break;
                    case "LUK":
                        item.eq.setLuk(ItemConstants.getStat(itemId, ((int) stat.getValue())));
                        break;
                    case "PAD":
                        item.eq.setWatk(ItemConstants.getATK(itemId, ((int) stat.getValue())));
                        break;
                    case "PDD":
                        item.eq.setWdef(ItemConstants.getDEF(itemId, ((int) stat.getValue())));
                        break;
                    case "MAD":
                        item.eq.setMatk(ItemConstants.getATK(itemId, ((int) stat.getValue())));
                        break;
                    case "MDD":
                        item.eq.setMdef(ItemConstants.getDEF(itemId, ((int) stat.getValue())));
                        break;
                    case "ACC":
                        item.eq.setAcc((short) (int) stat.getValue());
                        break;
                    case "EVA":
                        item.eq.setAvoid((short) (int) stat.getValue());
                        break;
                    case "Speed":
                        item.eq.setSpeed((short) (int) stat.getValue());
                        break;
                    case "Jump":
                        item.eq.setJump((short) (int) stat.getValue());
                        break;
                    case "MHP":
                        item.eq.setHp(ItemConstants.getHpMp(itemId, ((int) stat.getValue())));
                        break;
                    case "MMP":
                        item.eq.setMp(ItemConstants.getHpMp(itemId, ((int) stat.getValue())));
                        break;
                    case "tuc":
                        item.eq.setUpgradeSlots(((Integer) stat.getValue()).byteValue());
                        break;
                    case "Craft":
                        item.eq.setHands(((Integer) stat.getValue()).shortValue());
                        break;
                    case "charmEXP":
                        item.eq.setCharmEXP(((Integer) stat.getValue()).shortValue());
                        break;
                    case "bdR":
                        item.eq.setBossDamage(((Integer) stat.getValue()).shortValue());
                        break;
                    case "imdR":
                        item.eq.setIgnorePDR(((Integer) stat.getValue()).shortValue());
                        break;
                }
            }
            if ((item.equipStats.get("cash") != null) && (item.eq.getCharmEXP() <= 0)) {
                short exp = 0;
                int identifier = itemId / 10000;
                if ((ItemConstants.isWeapon(itemId)) || (identifier == 106)) {
                    exp = 60;
                } else if (identifier == 100) {
                    exp = 50;
                } else if ((ItemConstants.isAccessory(itemId)) || (identifier == 102) || (identifier == 108) || (identifier == 107)) {
                    exp = 40;
                } else if ((identifier == 104) || (identifier == 105) || (identifier == 110)) {
                    exp = 30;
                }
                item.eq.setCharmEXP(exp);
            }
        }
    }

    public void initItemInformation(ResultSet sqlItemData) throws SQLException {
        ItemInformation ret = new ItemInformation();
        int itemId = sqlItemData.getInt("itemid");
        ret.itemId = itemId;
        ret.slotMax = (ItemConstants.getSlotMax(itemId) > 0 ? ItemConstants.getSlotMax(itemId) : sqlItemData.getShort("slotMax"));
        ret.price = Double.parseDouble(sqlItemData.getString("price"));
        ret.wholePrice = sqlItemData.getInt("wholePrice");
        ret.stateChange = sqlItemData.getInt("stateChange");
        ret.name = sqlItemData.getString("name");
        ret.desc = sqlItemData.getString("desc");
        ret.msg = sqlItemData.getString("msg");

        ret.flag = sqlItemData.getInt("flags");

        ret.karmaEnabled = sqlItemData.getByte("karma");
        ret.meso = sqlItemData.getInt("meso");
        ret.itemMakeLevel = sqlItemData.getShort("itemMakeLevel");
        ret.questId = sqlItemData.getInt("questId");
        ret.create = sqlItemData.getInt("create");
        ret.replaceItem = sqlItemData.getInt("replaceId");
        ret.replaceMsg = sqlItemData.getString("replaceMsg");
        ret.afterImage = sqlItemData.getString("afterImage");
        ret.cardSet = 0;

        String scrollRq = sqlItemData.getString("scrollReqs");
        if (scrollRq.length() > 0) {
            ret.scrollReqs = new ArrayList();
            String[] scroll = scrollRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.scrollReqs.add(Integer.parseInt(s));
                }
            }
        }

        String consumeItem = sqlItemData.getString("consumeItem");
        if (consumeItem.length() > 0) {
            ret.questItems = new ArrayList();
            String[] scroll = scrollRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.questItems.add(Integer.parseInt(s));
                }
            }
        }

        ret.totalprob = sqlItemData.getInt("totalprob");

        String incRq = sqlItemData.getString("incSkill");
        if (incRq.length() > 0) {
            ret.incSkill = new ArrayList();
            String[] scroll = incRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.incSkill.add(Integer.parseInt(s));
                }
            }
        }
        this.dataCache.put(itemId, ret);
    }

    public boolean isExpOrDropCardTime(int itemId) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/ShangHai"));
        String day = MapleDayInt.getDayInt(cal.get(7));
        Map times;
        if (this.getExpCardTimes.containsKey(itemId)) {
            times = (Map) this.getExpCardTimes.get(itemId);
        } else {
            List<MapleData> data = getItemData(itemId).getChildByPath("info").getChildByPath("time").getChildren();
            Map hours = new HashMap();
            for (MapleData childdata : data) {
                String[] time = MapleDataTool.getString(childdata).split(":");
                hours.put(time[0], time[1]);
            }
            times = hours;
            this.getExpCardTimes.put(itemId, hours);
            cal.get(7);
        }
        if (times.containsKey(day)) {
            String[] hourspan = ((String) times.get(day)).split("-");
            int starthour = Integer.parseInt(hourspan[0]);
            int endhour = Integer.parseInt(hourspan[1]);

            if ((cal.get(11) >= starthour) && (cal.get(11) <= endhour)) {
                return true;
            }
        }
        return false;
    }

    public ScriptedItem getScriptedItemInfo(int itemId) {
        if (scriptedItemCache.containsKey(itemId)) {
            return (ScriptedItem) scriptedItemCache.get(itemId);
        }
        if (itemId / 10000 != 243 && itemId / 10000 != 568) {
            return null;
        }
        ScriptedItem script = new ScriptedItem(MapleDataTool.getInt("spec/npc", getItemData(itemId), 0), MapleDataTool.getString("spec/script", getItemData(itemId), ""), MapleDataTool.getInt("spec/runOnPickup", getItemData(itemId), 0) == 1);
        scriptedItemCache.put(itemId, script);
        return (ScriptedItem) scriptedItemCache.get(itemId);
    }

    public boolean isFloatCashItem(int itemId) {
        if (this.floatCashItem.containsKey(itemId)) {
            return (floatCashItem.get(itemId));
        }
        if (itemId / 10000 != 512) {
            return false;
        }
        boolean floatType = MapleDataTool.getIntConvert("info/floatType", getItemData(itemId), 0) > 0;
        floatCashItem.put(itemId, floatType);
        return floatType;
    }

    public short getPetFlagInfo(int itemId) {
        if (this.petFlagInfo.containsKey(itemId)) {
            return (this.petFlagInfo.get(itemId));
        }
        short flag = 0;
        if (itemId / 10000 != 500) {
            return flag;
        }
        MapleData item = getItemData(itemId);
        if (item == null) {
            return flag;
        }
        if (MapleDataTool.getIntConvert("info/pickupItem", item, 0) > 0) {
            flag = (short) (flag | 0x1);
        }
        if (MapleDataTool.getIntConvert("info/longRange", item, 0) > 0) {
            flag = (short) (flag | 0x2);
        }
        if (MapleDataTool.getIntConvert("info/pickupAll", item, 0) > 0) {
            flag = (short) (flag | 0x4);
        }
        if (MapleDataTool.getIntConvert("info/sweepForDrop", item, 0) > 0) {
            flag = (short) (flag | 0x10);
        }
        if (MapleDataTool.getIntConvert("info/consumeHP", item, 0) > 0) {
            flag = (short) (flag | 0x20);
        }
        if (MapleDataTool.getIntConvert("info/consumeMP", item, 0) > 0) {
            flag = (short) (flag | 0x40);
        }
        if (MapleDataTool.getIntConvert("info/autoBuff", item, 0) > 0) {
            flag = (short) (flag | 0x200);
        }
        this.petFlagInfo.put(itemId, flag);
        return flag;
    }

    public int getPetSetItemID(int itemId) {
        if (this.petSetItemID.containsKey(itemId)) {
            return (this.petSetItemID.get(itemId));
        }
        int ret = -1;
        if (itemId / 10000 != 500) {
            return ret;
        }
        ret = MapleDataTool.getIntConvert("info/setItemID", getItemData(itemId), 0);
        this.petSetItemID.put(itemId, ret);
        return ret;
    }

    public int getItemIncMHPr(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("MHPr"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("MHPr"));
    }

    public int getItemIncMMPr(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("MMPr"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("MMPr"));
    }

    public int getSuccessRates(int itemId) {
        if (this.successRates.containsKey(itemId)) {
            return (this.successRates.get(itemId));
        }
        int success = 0;
        if (itemId / 10000 != 204) {
            return success;
        }
        success = MapleDataTool.getIntConvert("info/successRates/0", getItemData(itemId), 0);
        this.successRates.put(itemId, success);
        return success;
    }

    public int getForceUpgrade(int itemId) {
        if (this.forceUpgrade.containsKey(itemId)) {
            return (this.forceUpgrade.get(itemId));
        }
        int upgrade = 0;
        if (itemId / 100 != 20493) {
            return upgrade;
        }
        upgrade = MapleDataTool.getIntConvert("info/forceUpgrade", getItemData(itemId), 1);
        this.forceUpgrade.put(itemId, upgrade);
        return upgrade;
    }

    public Pair<Integer, Integer> getChairRecovery(int itemId) {
        if (itemId / 10000 != 301) {
            return null;
        }
        if (this.chairRecovery.containsKey(itemId)) {
            return (Pair) this.chairRecovery.get(itemId);
        }
        int recoveryHP = MapleDataTool.getIntConvert("info/recoveryHP", getItemData(itemId), 0);
        int recoveryMP = MapleDataTool.getIntConvert("info/recoveryMP", getItemData(itemId), 0);
        Pair ret = new Pair(Integer.valueOf(recoveryHP), Integer.valueOf(recoveryMP));
        this.chairRecovery.put(itemId, ret);
        return ret;
    }

    public int getLimitBreak(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("limitBreak"))) {
            return 999999;
        }
        return (getEquipStats(itemId).get("limitBreak"));
    }

    public int getBossDamageRate(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("bdR"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("bdR"));
    }

    public int getIgnoreMobDmageRate(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("imdR"))) {
            return 0;
        }
        return (getEquipStats(itemId).get("imdR"));
    }

    public int getScrollLimitBreak(int itemId) {
        if (this.ScrollLimitBreak.containsKey(itemId)) {
            return (this.ScrollLimitBreak.get(itemId));
        }
        int upgrade = 0;
        if (itemId / 100 != 26140) {
            return upgrade;
        }
        upgrade = MapleDataTool.getIntConvert("info/incALB", getItemData(itemId), 0);
        this.forceUpgrade.put(itemId, upgrade);
        return upgrade;
    }

    public boolean isNoCursedScroll(int itemId) {
        if (this.noCursedScroll.containsKey(itemId)) {
            return (this.noCursedScroll.get(itemId));
        }
        if (itemId / 10000 != 204) {
            return false;
        }
        boolean noCursed = MapleDataTool.getIntConvert("info/noCursed", getItemData(itemId), 0) > 0;
        this.noCursedScroll.put(itemId, noCursed);
        return noCursed;
    }

    public boolean isNegativeScroll(int itemId) {
        if (this.noNegativeScroll.containsKey(itemId)) {
            return (this.noNegativeScroll.get(itemId));
        }
        if (itemId / 10000 != 204) {
            return false;
        }
        boolean noNegative = MapleDataTool.getIntConvert("info/noNegative", getItemData(itemId), 0) > 0;
        this.noNegativeScroll.put(itemId, noNegative);
        return noNegative;
    }

    public boolean isExclusiveEquip(int itemId) {
        return this.exclusiveEquip.containsKey(itemId);
    }

    public StructExclusiveEquip getExclusiveEquipInfo(int itemId) {
        if (this.exclusiveEquip.containsKey(itemId)) {
            int exclusiveId = (this.exclusiveEquip.get(Integer.valueOf(itemId)));
            if (this.exclusiveEquipInfo.containsKey(exclusiveId)) {
                return (StructExclusiveEquip) this.exclusiveEquipInfo.get(exclusiveId);
            }
        }
        return null;
    }

    public Iterable<Pair<Integer, String>> getAllItems2() {
        //if (!itemNameCache.isEmpty()) {
        //  return itemNameCache;
        //}
        List<Pair<Integer, String>> itemPairs = new ArrayList<>();
        MapleData itemsData;
        itemsData = stringData.getData("Cash.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Consume.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
        for (MapleData eqpType : itemsData.getChildren()) {
            for (MapleData itemFolder : eqpType.getChildren()) {
                itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
            }
        }
        itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Ins.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Pet.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        return itemPairs;
    }

    public static class MapleDayInt {

        public static String getDayInt(int day) {
            if (day == 1) {
                return "SUN";
            }
            if (day == 2) {
                return "MON";
            }
            if (day == 3) {
                return "TUE";
            }
            if (day == 4) {
                return "WED";
            }
            if (day == 5) {
                return "THU";
            }
            if (day == 6) {
                return "FRI";
            }
            if (day == 7) {
                return "SAT";
            }
            return null;
        }
    }
}
