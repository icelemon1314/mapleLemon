package server.cashshop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.FileoutputUtil;

public class CashItemFactory {

    private static final CashItemFactory instance = new CashItemFactory();
    private final Map<Integer, CashItemInfo> itemStats = new HashMap();
    private final Map<Integer, Integer> idLookup = new HashMap();
    private final Map<Integer, CashItemInfo> oldItemStats = new HashMap();
    private final Map<Integer, Integer> oldIdLookup = new HashMap();
    private final Map<Integer, List<Integer>> itemPackage = new HashMap();
    private final Map<Integer, List<Integer>> openBox = new HashMap();
    private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Etc.wz"));
    private final MapleData commodities = this.data.getData("Commodity.img");
    private final Map<Integer, Boolean> blockCashItemId = new HashMap();
    private final Map<Integer, Boolean> blockCashSnId = new HashMap();
    private final List<Integer> blockRefundableItemId = new LinkedList();

    public static CashItemFactory getInstance() {
        return instance;
    }

    public Map<Integer,CashItemInfo> getItemStats (){
        return itemStats;
    }

    public void initialize(boolean reload) {
        if (reload) {
            itemStats.clear();
            itemPackage.clear();
            openBox.clear();
        }
        if (!itemStats.isEmpty() || !itemPackage.isEmpty() || !openBox.isEmpty()) {
            return;
        }
        this.blockRefundableItemId.clear();
        int onSaleSize = 0;
        Map fixId = new HashMap();

        for (MapleData field : this.commodities.getChildren()) {
            int SN = MapleDataTool.getIntConvert("SN", field, 0);
            int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
            int count = MapleDataTool.getIntConvert("Count", field, 1);
            int price = MapleDataTool.getIntConvert("Price", field, 0);
            int originalPrice = MapleDataTool.getIntConvert("originalPrice", field, 0);
            int period = MapleDataTool.getIntConvert("Period", field, 0);
            int gender = MapleDataTool.getIntConvert("Gender", field, 2);
            boolean onSale = (MapleDataTool.getIntConvert("OnSale", field, 0) > 0);
            boolean bonus = MapleDataTool.getIntConvert("Bonus", field, 0) >= 0;
            boolean refundable = MapleDataTool.getIntConvert("Refundable", field, 0) == 0;
            boolean discount = MapleDataTool.getIntConvert("discount", field, 0) >= 0;
            if (onSale) {
                onSaleSize++;
            }

            CashItemInfo stats = new CashItemInfo(itemId, count, price, originalPrice, SN, period, gender, onSale, bonus, refundable, discount);
            if (SN > 0) {
                this.itemStats.put(SN, stats);
                if (this.idLookup.containsKey(itemId)) {
                    fixId.put(SN, itemId);
                    this.blockRefundableItemId.add(itemId);
                }
                this.idLookup.put(itemId, SN);
            }
        }
      FileoutputUtil.log("共加载 " + this.itemStats.size() + " 个商城道具，有 " + onSaleSize + " 个道具处于出售状态，");
      FileoutputUtil.log("其中有 " + fixId.size() + " 重复价格的道具和 " + this.blockRefundableItemId.size() + " 个禁止换购的道具；");

        MapleData packageData = this.data.getData("CashPackage.img");
        for (MapleData root : packageData.getChildren()) {
            if (root.getChildByPath("SN") == null) {
                continue;
            }
            List packageItems = new ArrayList();
            for (MapleData dat : root.getChildByPath("SN").getChildren()) {
                packageItems.add(MapleDataTool.getIntConvert(dat));
            }
            this.itemPackage.put(Integer.parseInt(root.getName()), packageItems);
        }
      FileoutputUtil.log("共加载 " + this.itemPackage.size() + " 个商城礼包；");

        onSaleSize = 0;
        MapleDataDirectoryEntry root = this.data.getRoot();
        for (MapleDataEntry topData : root.getFiles()) {
            if (topData.getName().startsWith("OldCommodity")) {
                MapleData Commodity = this.data.getData(topData.getName());
                for (MapleData field : Commodity.getChildren()) {
                    int SN = MapleDataTool.getIntConvert("SN", field, 0);
                    int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
                    int count = MapleDataTool.getIntConvert("Count", field, 1);
                    int price = MapleDataTool.getIntConvert("Price", field, 0);
                    int originalPrice = MapleDataTool.getIntConvert("originalPrice", field, 0);
                    int period = MapleDataTool.getIntConvert("Period", field, 0);
                    int gender = MapleDataTool.getIntConvert("Gender", field, 2);
                    boolean onSale = (MapleDataTool.getIntConvert("OnSale", field, 0) > 0);
                    boolean bonus = MapleDataTool.getIntConvert("Bonus", field, 0) >= 0;
                    boolean refundable = MapleDataTool.getIntConvert("Refundable", field, 0) == 0;
                    boolean discount = MapleDataTool.getIntConvert("discount", field, 0) >= 0;
                    if (onSale) {
                        onSaleSize++;
                    }
                    CashItemInfo stats = new CashItemInfo(itemId, count, price, originalPrice, SN, period, gender, onSale, bonus, refundable, discount);
                    if (SN > 0) {
                        this.oldItemStats.put(SN, stats);
                        this.oldIdLookup.put(itemId, SN);
                    }
                }
            }
        }
      FileoutputUtil.log("共加载 " + this.oldItemStats.size() + " 个老的商城道具，有 " + onSaleSize + " 个道具处于出售状态；");
    }

    public void loadBlockedCash() {
        blockCashItemId.clear();
        /*
         MapleData root = this.data.getData("BlockCash.img");
         for (MapleData dat : root.getChildByPath("ItemId").getChildren()) {
         int itemId = Integer.parseInt(dat.getName());
         boolean block = MapleDataTool.getIntConvert("Block", dat, 0) >= 0;
         if (this.blockCashItemId.containsKey(itemId)) {
         FileoutputUtil.log("发现重复禁止道具信息: " + itemId);
         continue;
         }
         this.blockCashItemId.put(itemId, block);
         }
         FileoutputUtil.log("共加载 " + this.blockCashItemId.size() + " 个商城禁止购买的道具ID信息...");
         this.blockCashSnId.clear();
         for (MapleData dat : root.getChildByPath("SNId").getChildren()) {
         int packageId = Integer.parseInt(dat.getName());
         boolean block = MapleDataTool.getIntConvert("Block", dat, 0) >= 0;
         if (this.blockCashSnId.containsKey(packageId)) {
         FileoutputUtil.log("发现重复禁止SN信息: " + packageId);
         continue;
         }
         this.blockCashSnId.put(packageId, block);
         }
         FileoutputUtil.log("共加载 " + this.blockCashSnId.size() + " 个商城禁止购买的道具SN信息...");
         */
    }

    public Map<Integer, Boolean> getBlockedCashItem() {
        return this.blockCashItemId;
    }

    public boolean isBlockedCashItemId(int itemId) {
        return this.blockCashItemId.containsKey(itemId);
    }

    public Map<Integer, Boolean> getBlockCashSn() {
        return this.blockCashSnId;
    }

    public boolean isBlockCashSnId(int itemId) {
        return this.blockCashSnId.containsKey(itemId);
    }

    public CashItemInfo getSimpleItem(int sn) {
        return (CashItemInfo) this.itemStats.get(sn);
    }

    public boolean isBlockRefundableItemId(int itemId) {
        return this.blockRefundableItemId.contains(itemId);
    }

    public CashItemInfo getItem(int sn) {
        return getItem(sn, false);
    }

    public CashItemInfo getItem(int sn, boolean checkSale) {
        CashItemInfo stats = (CashItemInfo) this.itemStats.get(Integer.valueOf(sn));

        if (stats == null) {
            return null;
        }
        return (checkSale) && (!stats.onSale()) ? null : stats;
    }

    public List<Integer> getPackageItems(int itemId) {
        return (List) this.itemPackage.get(itemId);
    }

    public Map<Integer, List<Integer>> getRandomItemInfo() {
        return this.openBox;
    }

    public boolean hasRandomItem(int itemId) {
        return this.openBox.containsKey(itemId);
    }

    public List<Integer> getRandomItem(int itemId) {
        return (List) this.openBox.get(itemId);
    }

    public int getLinkItemId(int itemId) {
        switch (itemId) {
            case 5000029:
            case 5000030:
            case 5000032:
            case 5000033:
            case 5000035:
                return 5000028;
            case 5000048:
            case 5000049:
            case 5000050:
            case 5000051:
            case 5000052:
                return 5000047;
            case 5000031:
            case 5000034:
            case 5000036:
            case 5000037:
            case 5000038:
            case 5000039:
            case 5000040:
            case 5000041:
            case 5000042:
            case 5000043:
            case 5000044:
            case 5000045:
            case 5000046:
            case 5000047:
        }
        return itemId;
    }

    public int getSnFromId(int itemId) {
        if (this.idLookup.containsKey(itemId)) {
            return (this.idLookup.get(itemId));
        }
        return 0;
    }
}
