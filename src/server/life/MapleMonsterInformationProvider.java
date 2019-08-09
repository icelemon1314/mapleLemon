package server.life;

import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import database.dao.DropDataDao;
import database.dao.DropDataGlobalDao;
import database.entity.DropDataGlobalPo;
import database.entity.DropDataPo;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import server.StructFamiliar;
import tools.Pair;

public class MapleMonsterInformationProvider {

    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private final Map<Integer, String> mobCache = new TreeMap();
    private final Map<Integer, ArrayList<MonsterDropEntry>> drops = new HashMap();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList();
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));

    public static MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    public List<MonsterGlobalDropEntry> getGlobalDrop() {
        return this.globaldrops;
    }

    public void load() {
        DropDataDao dropDataDao = new DropDataDao();
        List<DropDataPo> dropDataList = dropDataDao.getAllData();

        ArrayList ret;

        for(DropDataPo dropData : dropDataList) {
            int mobId = dropData.getMobId();
            if (drops.containsKey(mobId)) {
                ret = drops.get(mobId);
            } else {
                ret = new ArrayList();
            }

            ret.add(new MonsterDropEntry(dropData.getItemid(), dropData.getChance(), dropData.getMinimumQuantity(),
                    dropData.getMaximumQuantity(), dropData.getQuestid()));
            this.drops.put(mobId, ret);
        }

        DropDataGlobalDao dropDataGlobalDao = new DropDataGlobalDao();
        List<DropDataGlobalPo> dropDataGlobalPoList = dropDataGlobalDao.getAllData();
        for(DropDataGlobalPo dropDataGlobal : dropDataGlobalPoList) {
            this.globaldrops.add(new MonsterGlobalDropEntry(dropDataGlobal.getItemid(), dropDataGlobal.getChance(),
                    dropDataGlobal.getContinent(), dropDataGlobal.getDropType(),dropDataGlobal.getMinimumQuantity(),
                    dropDataGlobal.getMaximumQuantity(), dropDataGlobal.getQuestid()));
        }
    }

    public ArrayList<MonsterDropEntry> retrieveDrop(int monsterId) {
        return (ArrayList) this.drops.get(monsterId);
    }

    public void addExtra() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Map.Entry e : this.drops.entrySet()) {
            for (int i = 0; i < ((ArrayList) e.getValue()).size(); i++) {
                if ((((MonsterDropEntry) ((ArrayList) e.getValue()).get(i)).itemId != 0) && (!ii.itemExists(((MonsterDropEntry) ((ArrayList) e.getValue()).get(i)).itemId))) {
                    ((ArrayList) e.getValue()).remove(i);
                }
            }
            MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(((Integer) e.getKey()).intValue());
            Integer item = ii.getItemIdByMob(((Integer) e.getKey()).intValue());
            if ((item != null) && (item > 0)) {
                if (item / 10000 == 238) {
                    continue;
                }
                ((ArrayList) e.getValue()).add(new MonsterDropEntry(item, mons.isBoss() ? 1000000 : 10000, 1, 1, 0));
            }
            StructFamiliar f = ii.getFamiliarByMob(((Integer) e.getKey()).intValue());
            if (f != null) {
                if (f.itemid / 10000 == 238) {
                    continue;
                }
                ((ArrayList) e.getValue()).add(new MonsterDropEntry(f.itemid, mons.isBoss() ? 10000 : 100, 1, 1, 0));
            }
        }
        for (StructFamiliar f : ii.getFamiliars().values()) {
            if (!this.drops.containsKey(f.mob)) {
                MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(f.mob);
                ArrayList e = new ArrayList();
                if (f.itemid / 10000 == 238) {
                    continue;
                }
                e.add(new MonsterDropEntry(f.itemid, mons.isBoss() ? 10000 : 100, 1, 1, 0));
                addMeso(mons, e);
                this.drops.put(f.mob, e);
            }

        }

    }

    public void addMeso(MapleMonsterStats mons, ArrayList<MonsterDropEntry> ret) {
        double divided = mons.getLevel() < 100 ? 10.0D : mons.getLevel() < 10 ? mons.getLevel() : mons.getLevel() / 10.0D;
        int maxMeso = mons.getLevel() * (int) Math.ceil(mons.getLevel() / divided);
        if ((mons.isBoss()) && (!mons.isPartyBonus())) {
            maxMeso *= 3;
        }
        for (int i = 0; i < mons.dropsMesoCount(); i++) {
            if ((mons.getId() >= 9600086) && (mons.getId() <= 9600098)) {
                int meso = (int) Math.floor(Math.random() * 500.0D + 1000.0D);
                ret.add(new MonsterDropEntry(0, 20000, (int) Math.floor(0.46D * meso), meso, 0));
            } else {
                ret.add(new MonsterDropEntry(0, mons.isPartyBonus() ? 40000 : (mons.isBoss()) && (!mons.isPartyBonus()) ? 800000 : 40000, (int) Math.floor(0.66D * maxMeso), maxMeso, 0));
            }
        }
    }

    public void clearDrops() {
        this.drops.clear();
        this.globaldrops.clear();
        load();
        addExtra();
    }

    public boolean contains(ArrayList<MonsterDropEntry> e, int toAdd) {
        for (MonsterDropEntry f : e) {
            if (f.itemId == toAdd) {
                return true;
            }
        }
        return false;
    }

    public int chanceLogic(int itemId) {
        switch (itemId) {
            case 2049301:
            case 2049401:
            case 4280000:
            case 4280001:
                return 5000;
            case 1002419:
            case 2049300:
            case 2049400:
                return 2000;
            case 1002938:
                return 50;
        }
        if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
            return 8000;
        }
        if ((ItemConstants.getInventoryType(itemId) == MapleInventoryType.SETUP) || (ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH)) {
            return 500;
        }
        switch (itemId / 10000) {
            case 204:
                return 1800;
            case 207:
            case 233:
                return 3000;
            case 229:
                return 400;
            case 401:
            case 402:
                return 5000;
            case 403:
                return 4000;
        }
        return 8000;
    }

    public Map<Integer, String> getAllMonsters() {
        if (mobCache.isEmpty()) {
            final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "//String.wz"));
            MapleData mobsData = stringData.getData("Mob.img");
            for (MapleData itemFolder : mobsData.getChildren()) {
                mobCache.put(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME"));
            }
        }
        return mobCache;
    }
}
