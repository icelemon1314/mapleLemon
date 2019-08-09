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
            return this.shops.get(shopId);
        }
        return loadShopByShopId(shopId);
    }

    public MapleShop getShopForNPC(int npcId) {
        if (this.npcShops.containsKey(npcId)) {
            return this.npcShops.get(npcId);
        }
        return loadShopByNpcId(npcId);
    }

    private MapleShop loadShopByShopId(int shopId) {
        MapleShop ret = MapleShop.createFromDbByShopId(shopId);
        return loadShop(ret);
    }

    private MapleShop loadShopByNpcId(int npcId) {
        MapleShop ret = MapleShop.createFromDbByNpcId(npcId);
        return loadShop(ret);
    }

    private MapleShop loadShop(MapleShop ret) {
        this.shops.put(ret.getId(), ret);
        this.npcShops.put(ret.getNpcId(), ret);
        return ret;
    }
}
