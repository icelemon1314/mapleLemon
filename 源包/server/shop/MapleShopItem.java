package server.shop;

import client.inventory.Item;

public class MapleShopItem {

    private final short buyable;
    private final int itemId;
    private final int price;
    private final int period;
    private final int state;

    public MapleShopItem(Item rebuy, int price, short buyable) {
        this.buyable = buyable;
        this.itemId = rebuy.getItemId();
        this.price = price;
        this.period = 0;
        this.state = 0;
    }

    public MapleShopItem(short buyable, int itemId, int price, int reqItem, int reqItemQ, int period, int state, int rank) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.period = period;
        this.state = state;
    }

    public short getBuyable() {
        return this.buyable;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPrice() {
        return this.price;
    }

    public int getPeriod() {
        return this.period;
    }

    public int getState() {
        return this.state;
    }
}
