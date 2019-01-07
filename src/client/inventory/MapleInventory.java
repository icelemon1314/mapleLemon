package client.inventory;

import constants.ItemConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapleInventory implements Iterable<Item>, Serializable {

    private final Map<Short, Item> inventory;
    private byte slotLimit = 0;
    private final MapleInventoryType type;
    private byte maxSlot = 96;

    public MapleInventory(MapleInventoryType type) {
        this.inventory = new LinkedHashMap();
        this.type = type;
    }

    public void addSlot(byte slot) {
        this.slotLimit = (byte) (this.slotLimit + slot);
        if (this.slotLimit > this.maxSlot) {
            this.slotLimit = this.maxSlot;
        }
    }

    public byte getSlotLimit() {
        return this.slotLimit;
    }

    public void setSlotLimit(byte slot) {
        if (slot > this.maxSlot) {
            slot = this.maxSlot;
        }
        this.slotLimit = slot;
    }

    public Item findById(int itemId) {
        for (Item item : this.inventory.values()) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public Item findByUniqueId(int itemId) {
        for (Item item : this.inventory.values()) {
            if (item.getUniqueId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public Item findByInventoryId(long onlyId, int itemId) {
        for (Item item : this.inventory.values()) {
            if ((item.getInventoryId() == onlyId) && (item.getItemId() == itemId)) {
                return item;
            }
        }
        return findById(itemId);
    }

    public Item findByEquipOnlyId(long onlyId, int itemId) {
        for (Item item : this.inventory.values()) {
            if ((item.getEquipOnlyId() == onlyId) && (item.getItemId() == itemId)) {
                return item;
            }
        }
        return null;
    }

    public int countById(int itemId) {
        int possesed = 0;
        for (Item item : this.inventory.values()) {
            if (item.getItemId() == itemId) {
                possesed += item.getQuantity();
            }
        }
        return possesed;
    }

    public List<Item> listById(int itemId) {
        List ret = new ArrayList();
        for (Item item : this.inventory.values()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public List<Item> listByEquipOnlyId(int equipOnlyId) {
        List ret = new ArrayList();
        for (Item item : this.inventory.values()) {
            if ((item.getEquipOnlyId() > 0) && (item.getEquipOnlyId() == equipOnlyId)) {
                ret.add(item);
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public Collection<Item> list() {
        return this.inventory.values();
    }

    public List<Item> newList() {
        if (this.inventory.size() <= 0) {
            return Collections.emptyList();
        }
        return new LinkedList(this.inventory.values());
    }

    public List<Integer> listIds() {
        List ret = new ArrayList();
        for (Item item : this.inventory.values()) {
            if (!ret.contains(item.getItemId())) {
                ret.add(item.getItemId());
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public short addItem(Item item) {
        short slotId = getNextFreeSlot();
        if (slotId < 0) {
            return -1;
        }
        this.inventory.put(slotId, item);
        item.setPosition((byte)slotId);
        return slotId;
    }

    public void addFromDB(Item item) {
        if ((item.getPosition() < 0) && (!this.type.equals(MapleInventoryType.EQUIPPED))) {
            return;
        }
        if ((item.getPosition() > 0) && (this.type.equals(MapleInventoryType.EQUIPPED))) {
            return;
        }
        this.inventory.put((short)item.getPosition(), item);
    }

    public void move(short sSlot, short dSlot, short slotMax) {
        Item source = (Item) this.inventory.get(Short.valueOf(sSlot));
        Item target = (Item) this.inventory.get(Short.valueOf(dSlot));
        if (source == null) {
            throw new InventoryException("Trying to move empty slot");
        }
        if (target == null) {
            if ((dSlot < 0) && (!this.type.equals(MapleInventoryType.EQUIPPED))) {
                return;
            }
            if ((dSlot > 0) && (this.type.equals(MapleInventoryType.EQUIPPED))) {
                return;
            }
            source.setPosition((byte)dSlot);
            this.inventory.put(dSlot, source);
            this.inventory.remove(sSlot);
        } else if ((target.getItemId() == source.getItemId()) && (!ItemConstants.isRechargable(source.getItemId())) && (target.getOwner().equals(source.getOwner())) && (target.getExpiration() == source.getExpiration())) {
            if ((this.type.getType() == MapleInventoryType.EQUIP.getType()) || (this.type.getType() == MapleInventoryType.CASH.getType())) {
                swap(target, source);
            } else if (source.getQuantity() + target.getQuantity() > slotMax) {
                source.setQuantity((short) (source.getQuantity() + target.getQuantity() - slotMax));
                target.setQuantity(slotMax);
            } else {
                target.setQuantity((short) (source.getQuantity() + target.getQuantity()));
                this.inventory.remove(sSlot);
            }
        } else {
            swap(target, source);
        }
    }

    private void swap(Item source, Item target) {
        this.inventory.remove(source.getPosition());
        this.inventory.remove(target.getPosition());
        short swapPos = source.getPosition();
        source.setPosition(target.getPosition());
        target.setPosition((byte)swapPos);
        this.inventory.put((short)source.getPosition(), source);
        this.inventory.put((short)target.getPosition(), target);
    }

    public Item getItem(short slot) {
        return this.inventory.get(slot);
    }

    public void removeItem(short slot) {
        removeItem(slot, (byte) 1, false);
    }

    public void removeItem(short slot, short quantity, boolean allowZero) {
        Item item = (Item) this.inventory.get(Short.valueOf(slot));
        if (item == null) {
            return;
        }
        item.setQuantity((short) (item.getQuantity() - quantity));
        if (item.getQuantity() < 0) {
            item.setQuantity((short) 0);
        }
        if ((item.getQuantity() == 0) && (!allowZero)) {
            removeSlot(slot);
        }
    }

    public void removeSlot(short slot) {
        this.inventory.remove(slot);
    }

    public boolean isFull() {
        return this.inventory.size() >= this.slotLimit;
    }

    public boolean isFull(int margin) {
        return this.inventory.size() + margin >= this.slotLimit;
    }

    public short getNextFreeSlot() {
        if (isFull()) {
            return -1;
        }
        for (short i = 1; i <= this.slotLimit; i = (short) (i + 1)) {
            if (!this.inventory.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    public short getNumFreeSlot() {
        if (isFull()) {
            return 0;
        }
        byte free = 0;
        for (short i = 1; i <= this.slotLimit; i = (short) (i + 1)) {
            if (!this.inventory.containsKey(i)) {
                free = (byte) (free + 1);
            }
        }
        return (short) free;
    }

    public MapleInventoryType getType() {
        return this.type;
    }

    @Override
    public Iterator<Item> iterator() {
        return Collections.unmodifiableCollection(this.inventory.values()).iterator();
    }

    public Item findESById(short position) {
        for (Item item : this.inventory.values()) {
            //FileoutputUtil.log("返回itemID："+item.getPosition()+"realpos："+position);
            if (item.getPosition() == position) {
                return item;
            }
        }
        return null;
    }

    public Item findESByEquip(short position) {
        for (Item item : this.inventory.values()) {
            //FileoutputUtil.log("返回itemID："+item.getPosition()+"realpos："+position);
            if (item.getESPos() == position) {
                return item;
            }
        }
        return null;
    }

    public Item findESEquip() {
        for (Item item : this.inventory.values()) {
            if (item.getESPos() > 0) {
                return item;
            }
        }
        return null;
    }

    public int findESCount() {
        int i = 0;
        for (Item item : this.inventory.values()) {
            if (item.getESPos() > 0) {
                i++;
            }
        }
        return i;
    }
}
