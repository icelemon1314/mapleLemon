package server.shop;

import java.util.HashMap;
import java.util.Map;

public class MapleShopFactory {

    private final Map<Integer, MapleShop> shops = new HashMap();
    private final Map<Integer, MapleShop> npcShops = new HashMap();
    private static final MapleShopFactory instance = new MapleShopFactory();

    public static MapleShopFactory getInstance() {
        return instance;
    }

    public void clear() {
        this.shops.clear();
        this.npcShops.clear();
    }

    public MapleShop getShop(int shopId) {
        if (this.shops.containsKey(shopId)) {
            return (MapleShop) this.shops.get(shopId);
        }
        return loadShop(shopId, true);
    }

    public MapleShop getShopForNPC(int npcId) {
        if (this.npcShops.containsKey(npcId)) {
            return (MapleShop) this.npcShops.get(npcId);
        }
        return loadShop(npcId, false);
    }

    private MapleShop loadShop(int id, boolean isShopId) {
        MapleShop ret = MapleShop.createFromDB(id, isShopId);
        if (ret != null) {
            this.shops.put(ret.getId(), ret);
            this.npcShops.put(ret.getNpcId(), ret);
        } else if (isShopId) {
            this.shops.put(id, null);
        } else {
            this.npcShops.put(id, null);
        }
        return ret;
    }
}
