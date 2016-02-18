package client.inventory;

import constants.GameConstants;
import constants.ItemConstants;
import java.io.Serializable;

public class Equip extends Item implements Serializable {

    public static final int ARMOR_RATIO = 350000;
    public static final int WEAPON_RATIO = 700000;
    private byte upgradeSlots = 0;
    private byte level = 0;
    private byte state = 0;
    private byte enhance = 0;
    private short enhanctBuff = 0;
    private short reqLevel = 0;
    private short yggdrasilWisdom = 0;
    private short bossDamage = 0;
    private short ignorePDR = 0;
    private short totalDamage = 0;
    private short allStat = 0;
    private short karmaCount = -1;
    private boolean finalStrike = false;
    private short str = 0;
    private short dex = 0;
    private short _int = 0;
    private short luk = 0;
    private short hp = 0;
    private short mp = 0;
    private short watk = 0;
    private short matk = 0;
    private short wdef = 0;
    private short mdef = 0;
    private short acc = 0;
    private short avoid = 0;
    private short hands = 0;
    private short speed = 0;
    private short jump = 0;
    private short charmExp = 0;
    private int incSkill = -1;
    private int statemsg = 0;
    private MapleRing ring = null;

    public Equip(int id, short position, byte flag) {
        super(id, (byte)position, (short) 1, (short) flag);
    }

    public Equip(int id, short position, int uniqueid, short flag) {
        super(id, (byte)position, (short) 1, flag, uniqueid);
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        ret.str = this.str;
        ret.dex = this.dex;
        ret._int = this._int;
        ret.luk = this.luk;
        ret.hp = this.hp;
        ret.mp = this.mp;
        ret.matk = this.matk;
        ret.mdef = this.mdef;
        ret.watk = this.watk;
        ret.wdef = this.wdef;
        ret.acc = this.acc;
        ret.avoid = this.avoid;
        ret.hands = this.hands;
        ret.speed = this.speed;
        ret.jump = this.jump;
        ret.upgradeSlots = this.upgradeSlots;
        ret.level = this.level;
        ret.state = this.state;
        ret.enhance = this.enhance;
        ret.charmExp = this.charmExp;
        ret.incSkill = this.incSkill;
        ret.statemsg = this.statemsg;

        ret.enhanctBuff = this.enhanctBuff;
        ret.reqLevel = this.reqLevel;
        ret.yggdrasilWisdom = this.yggdrasilWisdom;
        ret.finalStrike = this.finalStrike;
        ret.bossDamage = this.bossDamage;
        ret.ignorePDR = this.ignorePDR;
        ret.totalDamage = this.totalDamage;
        ret.allStat = this.allStat;
        ret.karmaCount = this.karmaCount;

        ret.setGMLog(getGMLog());
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.setInventoryId(getInventoryId());
        ret.setEquipOnlyId(getEquipOnlyId());
        return ret;
    }

    public Item reset(Equip newEquip) {
        //Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        this.str = newEquip.str;
        this.dex = newEquip.dex;
        this._int = newEquip._int;
        this.luk = newEquip.luk;
        this.hp = newEquip.hp;
        this.mp = newEquip.mp;
        this.matk = newEquip.matk;
        this.mdef = newEquip.mdef;
        this.watk = newEquip.watk;
        this.wdef = newEquip.wdef;
        this.acc = newEquip.acc;
        this.avoid = newEquip.avoid;
        this.hands = newEquip.hands;
        this.speed = newEquip.speed;
        this.jump = newEquip.jump;
        this.upgradeSlots = newEquip.upgradeSlots;
        this.level = newEquip.level;
        this.enhance = newEquip.enhance;
        this.charmExp = newEquip.charmExp;
        this.incSkill = newEquip.incSkill;

        this.enhanctBuff = newEquip.enhanctBuff;
        this.reqLevel = newEquip.reqLevel;
        this.yggdrasilWisdom = newEquip.yggdrasilWisdom;
        this.finalStrike = newEquip.finalStrike;
        this.bossDamage = newEquip.bossDamage;
        this.ignorePDR = newEquip.ignorePDR;
        this.totalDamage = newEquip.totalDamage;
        this.allStat = newEquip.allStat;
        this.karmaCount = newEquip.karmaCount;
        this.setGiftFrom(getGiftFrom());
        return this;
    }

    @Override
    public byte getType() {
        return 1;
    }

    public byte getUpgradeSlots() {
        return this.upgradeSlots;
    }

    public short getStr() {
        return this.str;
    }

    public short getDex() {
        return this.dex;
    }

    public short getInt() {
        return this._int;
    }

    public short getLuk() {
        return this.luk;
    }

    public short getHp() {
        return this.hp;
    }

    public short getMp() {
        return this.mp;
    }

    public short getWatk() {
        return this.watk;
    }

    public short getMatk() {
        return this.matk;
    }

    public short getWdef() {
        return this.wdef;
    }

    public short getMdef() {
        return this.mdef;
    }

    public short getAcc() {
        return this.acc;
    }

    public short getAvoid() {
        return this.avoid;
    }

    public short getHands() {
        return this.hands;
    }

    public short getSpeed() {
        return this.speed;
    }

    public short getJump() {
        return this.jump;
    }

    public void setStr(short str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    public void setDex(short dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    public void setInt(short _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    public void setLuk(short luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    public void setHp(short hp) {
        if (hp < 0) {
            hp = 0;
        }
        this.hp = hp;
    }

    public void setMp(short mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public void setWatk(short watk) {
        if (watk < 0) {
            watk = 0;
        }
        this.watk = watk;
    }

    public void setMatk(short matk) {
        if (matk < 0) {
            matk = 0;
        }
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        if (wdef < 0) {
            wdef = 0;
        }
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        if (mdef < 0) {
            mdef = 0;
        }
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    public void setJump(short jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return this.level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getEquipLevel() {
        if (GameConstants.getMaxLevel(getItemId()) <= 0) {
            return 0;
        }

        int levelz = getBaseLevel();
        for (int i = levelz; (GameConstants.getStatFromWeapon(getItemId()) == null ? i <= GameConstants.getMaxLevel(getItemId()) : i < GameConstants.getMaxLevel(getItemId()))
                ; i++) {
            levelz++;
        }

        return levelz;
    }

    public int getBaseLevel() {
        return GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0;
    }

    @Override
    public void setQuantity(short quantity) {
        if ((quantity < 0) || (quantity > 1)) {
            throw new RuntimeException("设置装备的数量错误 欲设置的数量： " + quantity + " (道具ID: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public byte getEnhance() {
        return this.enhance;
    }

    public void setEnhance(byte en) {
        this.enhance = en;
    }
    public byte getState() {
        return getState(false);
    }

    public byte getState(boolean useAddPot) {
        byte ret = 0;
        return ret;
    }

    public void setState(byte en) {
        this.state = en;
    }





    public byte getAddState() {
        byte ret = 0;

        return ret;
    }

    public int getIncSkill() {
        return this.incSkill;
    }

    public void setIncSkill(int inc) {
        this.incSkill = inc;
    }

    public short getCharmEXP() {
        return this.charmExp;
    }

    public void setCharmEXP(short s) {
        this.charmExp = s;
    }

    public MapleRing getRing() {
        if ((!ItemConstants.isEffectRing(getItemId())) || (getUniqueId() <= 0)) {
            return null;
        }
        if (this.ring == null) {
            this.ring = MapleRing.loadFromDb(getUniqueId(), getPosition() < 0);
        }
        return this.ring;
    }

    public void setRing(MapleRing ring) {
        this.ring = ring;
    }

    public int getStateMsg() {
        return this.statemsg;
    }

    public void setStateMsg(int en) {
        if (en >= 3) {
            this.statemsg = 3;
        } else if (en < 0) {
            this.statemsg = 0;
        } else {
            this.statemsg = en;
        }
    }

    public short getEnhanctBuff() {
        return this.enhanctBuff;
    }

    public void setEnhanctBuff(short enhanctBuff) {
        if (enhanctBuff < 0) {
            enhanctBuff = 0;
        }
        this.enhanctBuff = enhanctBuff;
    }

    public short getReqLevel() {
        return this.reqLevel;
    }

    public void setReqLevel(short reqLevel) {
        if (reqLevel < 0) {
            reqLevel = 0;
        }
        this.reqLevel = reqLevel;
    }

    public short getYggdrasilWisdom() {
        return this.yggdrasilWisdom;
    }

    public void setYggdrasilWisdom(short yggdrasilWisdom) {
        if (yggdrasilWisdom < 0) {
            yggdrasilWisdom = 0;
        }
        this.yggdrasilWisdom = yggdrasilWisdom;
    }

    public boolean getFinalStrike() {
        return this.finalStrike;
    }

    public void setFinalStrike(boolean finalStrike) {
        this.finalStrike = finalStrike;
    }

    public short getBossDamage() {
        return this.bossDamage;
    }

    public void setBossDamage(short bossDamage) {
        if (bossDamage < 0) {
            bossDamage = 0;
        }
        this.bossDamage = bossDamage;
    }

    public short getIgnorePDR() {
        return this.ignorePDR;
    }

    public void setIgnorePDR(short ignorePDR) {
        if (ignorePDR < 0) {
            ignorePDR = 0;
        }
        this.ignorePDR = ignorePDR;
    }

    public short getTotalDamage() {
        return this.totalDamage;
    }

    public void setTotalDamage(short totalDamage) {
        if (totalDamage < 0) {
            totalDamage = 0;
        }
        this.totalDamage = totalDamage;
    }

    public short getAllStat() {
        return this.allStat;
    }

    public void setAllStat(short allStat) {
        if (allStat < 0) {
            allStat = 0;
        }
        this.allStat = allStat;
    }

    public short getKarmaCount() {
        return this.karmaCount;
    }

    public void setKarmaCount(short karmaCount) {
        this.karmaCount = karmaCount;
    }

    public static enum ScrollResult {

        失败, 成功,消失;
    }
}
