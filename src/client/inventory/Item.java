package client.inventory;

import java.io.Serializable;

import constants.ItemConstants;
import server.MapleItemInformationProvider;

public class Item implements Comparable<Item>, Serializable {

    private final int id;
    private byte position;
    private short quantity;
    private short flag;
    private long expiration = -1L;
    private long inventoryitemid = 0L;
    private MaplePet pet = null;
    private int uniqueid;
    private int sn;
    private int equipOnlyId = -1;
    private String owner = "";
    private String GameMaster_log = "";
    private String giftFrom = "";
    private short pos;

    public Item(int id, byte position, short quantity, short flag, int uniqueid) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = uniqueid;
        this.equipOnlyId = -1;
    }

    public Item(int id, byte position, short quantity, short flag) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = -1;
        this.equipOnlyId = -1;
    }

    public Item(int id, byte position, short quantity) {
        super();
        this.id = id;
        this.position =  position;
        this.quantity = quantity;
        this.uniqueid = -1;
        this.equipOnlyId = -1;
    }

    public Item copyWithQuantity(short quantitys) {
        Item ret = new Item(this.id, this.position, quantitys, this.flag, this.uniqueid);
        ret.pet = this.pet;
        ret.owner = this.owner;
        ret.sn = this.sn;
        ret.GameMaster_log = this.GameMaster_log;
        ret.expiration = this.expiration;
        ret.giftFrom = this.giftFrom;
        ret.equipOnlyId = this.equipOnlyId;
        return ret;
    }

    public Item copy() {
        Item ret = new Item(this.id, this.position, this.quantity, this.flag, this.uniqueid);
        ret.pet = this.pet;
        ret.owner = this.owner;
        ret.sn = this.sn;
        ret.GameMaster_log = this.GameMaster_log;
        ret.expiration = this.expiration;
        ret.giftFrom = this.giftFrom;
        ret.equipOnlyId = this.equipOnlyId;
        return ret;
    }

    public void setPosition(byte position) {
        this.position = position;
        if (this.pet != null) {
            this.pet.setInventoryPosition(position);
        }
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public int getItemId() {
        return this.id;
    }

    public byte getPosition() {
        return this.position;
    }

    public short getFlag() {
        return this.flag;
    }

    public short getQuantity() {
        return this.quantity;
    }

    public byte getType() {
        return ItemConstants.getInventoryType(this.id).getType();
//        return 2;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public void removeFlag(short flag) {
        this.flag = (short) (this.flag & (flag ^ 0xFFFFFFFF));
    }

    public void addFlag(short flag) {
        this.flag = (short) (this.flag | flag);
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void setExpiration(long expire) {
        this.expiration = expire;
    }

    public String getGMLog() {
        return this.GameMaster_log;
    }

    public void setGMLog(String GameMaster_log) {
        this.GameMaster_log = GameMaster_log;
    }

    public int getUniqueId() {
        return this.uniqueid;
    }

    public void setUniqueId(int ui) {
        this.uniqueid = ui;
    }

    public int getSN() {
        return this.sn;
    }

    public void setSN(int sn) {
        this.sn = sn;
    }

    public boolean hasSetOnlyId() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((this.uniqueid > 0) || (ii.isCash(this.id)) || (this.id / 1000000 != 1)) {
            return false;
        }
        return this.equipOnlyId <= 0;
    }

    public int getEquipOnlyId() {
        return this.equipOnlyId;
    }

    public void setEquipOnlyId(int OnlyId) {
        this.equipOnlyId = OnlyId;
    }

    public long getInventoryId() {
        return this.inventoryitemid;
    }

    public void setInventoryId(long ui) {
        this.inventoryitemid = ui;
    }

    public MaplePet getPet() {
        return this.pet;
    }

    public void setPet(MaplePet pet) {
        this.pet = pet;
        if (pet != null) {
            this.uniqueid = pet.getUniqueId();
        }
    }

    public void setGiftFrom(String gf) {
        this.giftFrom = gf;
    }

    public String getGiftFrom() {
        return this.giftFrom;
    }

    @Override
    public int compareTo(Item other) {
        if (Math.abs(this.position) < Math.abs(other.getPosition())) {
            return -1;
        }
        if (Math.abs(this.position) == Math.abs(other.getPosition())) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ite = (Item) obj;
        return (this.uniqueid == ite.getUniqueId()) && (this.id == ite.getItemId()) && (this.quantity == ite.getQuantity()) && (Math.abs(this.position) == Math.abs(ite.getPosition()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id;
        hash = 53 * hash + this.position;
        hash = 53 * hash + this.quantity;
        hash = 53 * hash + this.uniqueid;
        return hash;
    }

    @Override
    public String toString() {
        return "物品: " + this.id + " 数量: " + this.quantity;
    }

    public void setESPos(final short pos) {
        this.pos = pos;

    }

    public short getESPos() {
        return this.pos;
    }
}
