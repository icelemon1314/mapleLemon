package server.shops;

import client.inventory.Item;

public class MaplePlayerShopItem {

    public Item item;
    public short bundles;
    public int price;

    public MaplePlayerShopItem(Item item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
    }

    public Item getItem() {
        return this.item;
    }

    public short getBundles() {
        return this.bundles;
    }

    public int getPrice() {
        return this.price;
    }
}
