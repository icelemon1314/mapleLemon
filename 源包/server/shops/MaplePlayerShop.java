package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import java.util.ArrayList;
import java.util.List;
import server.MapleInventoryManipulator;
import tools.packet.PlayerShopPacket;

public class MaplePlayerShop extends AbstractPlayerStore {

    private int boughtnumber = 0;
    private final List<String> bannedList = new ArrayList();

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6);
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = (MaplePlayerShopItem) this.items.get(item);
        if (pItem.bundles > 0) {
            Item newItem = pItem.item.copy();
            newItem.setQuantity((short) (quantity * newItem.getQuantity()));
            short flag = newItem.getFlag();

            if (ItemFlag.KARMA_USE.check(flag)) {
                newItem.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
            }
            int gainmeso = pItem.price * quantity;
            if (c.getPlayer().getMeso() >= gainmeso) {
                if ((getMCOwner().getMeso() + gainmeso > 0) && (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner())) && (MapleInventoryManipulator.addFromDrop(c, newItem, false))) {
                    MaplePlayerShopItem tmp176_174 = pItem;
                    tmp176_174.bundles = (short) (tmp176_174.bundles - quantity);
                    this.bought.add(new AbstractPlayerStore.BoughtItem(newItem.getItemId(), quantity, gainmeso, c.getPlayer().getName()));
                    c.getPlayer().gainMeso(-gainmeso, false);
                    getMCOwner().gainMeso(gainmeso, false);
                    if (pItem.bundles <= 0) {
                        this.boughtnumber += 1;
                        if (this.boughtnumber == this.items.size()) {
                            closeShop(false, true);
                            return;
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }

            getMCOwner().getClient().getSession().write(PlayerShopPacket.shopItemUpdate(this));
        }
    }

    @Override
    public byte getShopType() {
        return 2;
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
        MapleCharacter owner = getMCOwner();
        removeAllVisitors(10, 1);
        getMap().removeMapObject(this);

        for (MaplePlayerShopItem itemss : getItems()) {
            if (itemss.bundles > 0) {
                Item newItem = itemss.item.copy();
                newItem.setQuantity((short) (itemss.bundles * newItem.getQuantity()));
                if (MapleInventoryManipulator.addFromDrop(owner.getClient(), newItem, false)) {
                    itemss.bundles = 0;
                } else {
                    saveItems();
                    break;
                }
            }
        }
        owner.setPlayerShop(null);
        update();
        getMCOwner().getClient().getSession().write(PlayerShopPacket.shopErrorMessage(10, 1));
    }

    public void banPlayer(String name) {
        if (!this.bannedList.contains(name)) {
            this.bannedList.add(name);
        }
        for (int i = 0; i < 3; i++) {
            MapleCharacter chr = getVisitor(i);
            if (chr.getName().equals(name)) {
                chr.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(5, 1));
                chr.setPlayerShop(null);
                removeVisitor(chr);
            }
        }
    }

    public boolean isBanned(String name) {
        return this.bannedList.contains(name);
    }
}
