package server.life;

import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import tools.Pair;

public class MapleMonsterStats {

    private byte cp;
    private byte selfDestruction_action;
    private byte tagColor;
    private byte tagBgColor;
    private byte rareItemDropLevel;
    private byte HPDisplayType;
    private byte summonType;
    private byte PDRate;
    private byte MDRate;
    private byte category;
    private short level;
    private short charismaEXP;
    private long hp;
    private final int id;
    private int exp;
    private int mp;
    private int removeAfter;
    private int buffToGive;
    private int fixedDamage;
    private int selfDestruction_hp;
    private int dropItemPeriod;
    private int point;
    private int eva;
    private int acc;
    private int PhysicalAttack;
    private int MagicAttack;
    private int speed;
    private int partyBonusR;
    private int pushed;
    private boolean boss;
    private boolean undead;
    private boolean ffaLoot;
    private boolean firstAttack;
    private boolean isExplosiveReward;
    private boolean mobile;
    private boolean fly;
    private boolean onlyNormalAttack;
    private boolean friendly;
    private boolean noDoom;
    private boolean invincible;
    private boolean partyBonusMob;
    private boolean changeable;
    private boolean escort;
    private String name;
    private String mobType;
    private final EnumMap<Element, ElementalEffectiveness> resistance = new EnumMap(Element.class);
    private List<Integer> revives = new ArrayList();
    private final List<Pair<Integer, Integer>> skills = new ArrayList();
    private final List<MobAttackInfo> mai = new ArrayList();
    private BanishInfo banish;

    public MapleMonsterStats(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public long getHp() {
        return this.hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public short getLevel() {
        return this.level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public short getCharismaEXP() {
        return this.charismaEXP;
    }

    public void setCharismaEXP(short leve) {
        this.charismaEXP = leve;
    }

    public void setSelfD(byte selfDestruction_action) {
        this.selfDestruction_action = selfDestruction_action;
    }

    public byte getSelfD() {
        return this.selfDestruction_action;
    }

    public void setSelfDHP(int selfDestruction_hp) {
        this.selfDestruction_hp = selfDestruction_hp;
    }

    public int getSelfDHp() {
        return this.selfDestruction_hp;
    }

    public void setFixedDamage(int damage) {
        this.fixedDamage = damage;
    }

    public int getFixedDamage() {
        return this.fixedDamage;
    }

    public void setPushed(int damage) {
        this.pushed = damage;
    }

    public int getPushed() {
        return this.pushed;
    }

    public void setPhysicalAttack(int PhysicalAttack) {
        this.PhysicalAttack = PhysicalAttack;
    }

    public int getPhysicalAttack() {
        return this.PhysicalAttack;
    }

    public void setMagicAttack(int MagicAttack) {
        this.MagicAttack = MagicAttack;
    }

    public int getMagicAttack() {
        return this.MagicAttack;
    }

    public void setEva(int eva) {
        this.eva = eva;
    }

    public int getEva() {
        return this.eva;
    }

    public void setAcc(int acc) {
        this.acc = acc;
    }

    public int getAcc() {
        return this.acc;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setPartyBonusRate(int speed) {
        this.partyBonusR = speed;
    }

    public int getPartyBonusRate() {
        return this.partyBonusR;
    }

    public void setOnlyNormalAttack(boolean onlyNormalAttack) {
        this.onlyNormalAttack = onlyNormalAttack;
    }

    public boolean getOnlyNoramlAttack() {
        return this.onlyNormalAttack;
    }

    public BanishInfo getBanishInfo() {
        return this.banish;
    }

    public void setBanishInfo(BanishInfo banish) {
        this.banish = banish;
    }

    public int getRemoveAfter() {
        return this.removeAfter;
    }

    public void setRemoveAfter(int removeAfter) {
        this.removeAfter = removeAfter;
    }

    public byte getrareItemDropLevel() {
        return this.rareItemDropLevel;
    }

    public void setrareItemDropLevel(byte rareItemDropLevel) {
        this.rareItemDropLevel = rareItemDropLevel;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public boolean isBoss() {
        return this.boss;
    }

    public void setFfaLoot(boolean ffaLoot) {
        this.ffaLoot = ffaLoot;
    }

    public boolean isFfaLoot() {
        return this.ffaLoot;
    }

    public void setEscort(boolean ffaL) {
        this.escort = ffaL;
    }

    public boolean isEscort() {
        return this.escort;
    }

    public void setExplosiveReward(boolean isExplosiveReward) {
        this.isExplosiveReward = isExplosiveReward;
    }

    public boolean isExplosiveReward() {
        return this.isExplosiveReward;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isMobile() {
        return this.mobile;
    }

    public void setFly(boolean fly) {
        this.fly = fly;
    }

    public boolean isFly() {
        return this.fly;
    }

    public List<Integer> getRevives() {
        return this.revives;
    }

    public void setRevives(List<Integer> revives) {
        this.revives = revives;
    }

    public void setUndead(boolean undead) {
        this.undead = undead;
    }

    public boolean getUndead() {
        return this.undead;
    }

    public void setSummonType(byte selfDestruction) {
        this.summonType = selfDestruction;
    }

    public byte getSummonType() {
        return this.summonType;
    }

    public void setCategory(byte selfDestruction) {
        this.category = selfDestruction;
    }

    public byte getCategory() {
        return this.category;
    }

    public void setPDRate(byte selfDestruction) {
        this.PDRate = selfDestruction;
    }

    public byte getPDRate() {
        return this.PDRate;
    }

    public void setMDRate(byte selfDestruction) {
        this.MDRate = selfDestruction;
    }

    public byte getMDRate() {
        return this.MDRate;
    }

    public EnumMap<Element, ElementalEffectiveness> getElements() {
        return this.resistance;
    }

    public void setEffectiveness(Element e, ElementalEffectiveness ee) {
        this.resistance.put(e, ee);
    }

    public void removeEffectiveness(Element e) {
        this.resistance.remove(e);
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
        ElementalEffectiveness elementalEffectiveness = this.resistance.get(e);
        if (elementalEffectiveness == null) {
            return ElementalEffectiveness.NORMAL;
        }
        return elementalEffectiveness;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.mobType;
    }

    public void setType(String mobt) {
        this.mobType = mobt;
    }

    public byte getTagColor() {
        return this.tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = (byte) tagColor;
    }

    public byte getTagBgColor() {
        return this.tagBgColor;
    }

    public void setTagBgColor(int tagBgColor) {
        this.tagBgColor = (byte) tagBgColor;
    }

    public void setSkills(List<Pair<Integer, Integer>> skill_) {
        for (Pair skill : skill_) {
            this.skills.add(skill);
        }
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return Collections.unmodifiableList(this.skills);
    }

    public byte getNoSkills() {
        return (byte) this.skills.size();
    }

    public boolean hasSkill(int skillId, int level) {
        for (Pair skill : this.skills) {
            if ((((Integer) skill.getLeft()) == skillId) && (((Integer) skill.getRight()) == level)) {
                return true;
            }
        }
        return false;
    }

    public void setFirstAttack(boolean firstAttack) {
        this.firstAttack = firstAttack;
    }

    public boolean isFirstAttack() {
        return this.firstAttack;
    }

    public void setCP(byte cp) {
        this.cp = cp;
    }

    public byte getCP() {
        return this.cp;
    }

    public void setPoint(int cp) {
        this.point = cp;
    }

    public int getPoint() {
        return this.point;
    }

    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    public boolean isFriendly() {
        return this.friendly;
    }

    public void setInvincible(boolean invin) {
        this.invincible = invin;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public void setChange(boolean invin) {
        this.changeable = invin;
    }

    public boolean isChangeable() {
        return this.changeable;
    }

    public void setPartyBonus(boolean invin) {
        this.partyBonusMob = invin;
    }

    public boolean isPartyBonus() {
        return this.partyBonusMob;
    }

    public void setNoDoom(boolean doom) {
        this.noDoom = doom;
    }

    public boolean isNoDoom() {
        return this.noDoom;
    }

    public void setBuffToGive(int buff) {
        this.buffToGive = buff;
    }

    public int getBuffToGive() {
        return this.buffToGive;
    }

    public byte getHPDisplayType() {
        return this.HPDisplayType;
    }

    public void setHPDisplayType(byte HPDisplayType) {
        this.HPDisplayType = HPDisplayType;
    }

    public int getDropItemPeriod() {
        return this.dropItemPeriod;
    }

    public void setDropItemPeriod(int d) {
        this.dropItemPeriod = d;
    }

    public void addMobAttack(MobAttackInfo ma) {
        this.mai.add(ma);
    }

    public MobAttackInfo getMobAttack(int attack) {
        if ((attack >= this.mai.size()) || (attack < 0)) {
            return null;
        }
        return (MobAttackInfo) this.mai.get(attack);
    }

    public List<MobAttackInfo> getMobAttacks() {
        return this.mai;
    }

    public int dropsMesoCount() {
        if ((getRemoveAfter() != 0) || (isInvincible()) || (getOnlyNoramlAttack()) || (getDropItemPeriod() > 0) || (getCP() > 0) || (getPoint() > 0) || (getFixedDamage() > 0) || (getSelfD() != -1) || (getPDRate() <= 0) || (getMDRate() <= 0)) {
            return 0;
        }
        int mobId = getId() / 100000;
        if ((GameConstants.getPartyPlayHP(getId()) > 0) || (mobId == 97) || (mobId == 95) || (mobId == 93) || (mobId == 91) || (mobId == 90)) {
            return 0;
        }
        if (isExplosiveReward()) {
            return 7;
        }
        if (isBoss()) {
            return 2;
        }
        return 1;
    }
}
