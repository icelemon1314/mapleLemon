package client;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.BattleConstants;
import constants.BattleConstants.PokemonStat;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import tools.Pair;
import tools.StringUtil;
import tools.packet.MobPacket;

public class Battler implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private int level;
    private int exp;
    private int charId;
    private int monsterId;
    private byte gender;
    private byte abilityIndex;
    private String name;
    private BattleConstants.PokemonMob family;
    private transient MapleMonsterStats stats;
    private BattleConstants.PokemonElement[] elements = {BattleConstants.PokemonElement.None, BattleConstants.PokemonElement.None};
    private BattleConstants.PokemonNature nature;
    private BattleConstants.PokemonAbility ability;
    private BattleConstants.HoldItem item;
    private EnumMap<BattleConstants.PokemonStat, Pair<Byte, Double>> mods = new EnumMap(BattleConstants.PokemonStat.class);
    private long hp;
    private transient MonsterStatusEffect status;
    private transient WeakReference<MapleMonster> mons;
    private int statusTurnsLeft;
    private int tempLevel;
    private List<Integer> damagedChars = new ArrayList();

    public Battler(int level, int exp, int charId, int monsterId, String name, BattleConstants.PokemonNature nature, int itemId, byte gender, byte hpIV, byte atkIV, byte defIV, byte spatkIV, byte spdefIV, byte speedIV, byte evaIV, byte accIV, byte ability) {
        if (level > 200) {
            level = 200;
        }
        this.level = level;
        this.nature = nature;
        this.exp = exp;
        this.charId = charId;
        this.monsterId = monsterId;
        this.name = name;
        setStats();
        this.item = BattleConstants.HoldItem.getPokemonItem(itemId);
        if (gender < 0) {
            gender = (byte) (Randomizer.nextInt(2) + 1);
        }
        if (hpIV < 0) {
            hpIV = (byte) Randomizer.nextInt(101);
        }
        if (atkIV < 0) {
            atkIV = (byte) Randomizer.nextInt(101);
        }
        if (defIV < 0) {
            defIV = (byte) Randomizer.nextInt(101);
        }
        if (spatkIV < 0) {
            spatkIV = (byte) Randomizer.nextInt(101);
        }
        if (spdefIV < 0) {
            spdefIV = (byte) Randomizer.nextInt(101);
        }
        if (speedIV < 0) {
            speedIV = (byte) Randomizer.nextInt(101);
        }
        if (evaIV < 0) {
            evaIV = (byte) Randomizer.nextInt(101);
        }
        if (accIV < 0) {
            accIV = (byte) Randomizer.nextInt(101);
        }
        if (ability < 0) {
            ability = (byte) Randomizer.nextInt(2);
        }
        this.gender = gender;
        for (BattleConstants.PokemonStat stat : BattleConstants.PokemonStat.values()) {
            byte theIV = 50;
            switch (stat) {
                case ATK:
                    theIV = atkIV;
                    break;
                case DEF:
                    theIV = defIV;
                    break;
                case SPATK:
                    theIV = spatkIV;
                    break;
                case SPDEF:
                    theIV = spdefIV;
                    break;
                case SPEED:
                    theIV = speedIV;
                    break;
                case EVA:
                    theIV = evaIV;
                    break;
                case ACC:
                    theIV = accIV;
                    break;
                case HP:
                    theIV = hpIV;
            }

            this.mods.put(stat, new Pair(theIV, 1.0D));
        }
        this.abilityIndex = ability;
        calculateFamily();
    }

    public Battler(MapleMonsterStats stats) {
        this.level = stats.getLevel();
        this.exp = 0;
        this.charId = 0;
        this.monsterId = stats.getId();
        this.name = stats.getName();
        this.stats = stats;
        for (BattleConstants.PokemonStat stat : BattleConstants.PokemonStat.values()) {
            this.mods.put(stat, new Pair((byte) Randomizer.nextInt(101), 1.0D));
        }
        this.abilityIndex = (byte) Randomizer.nextInt(2);
        this.gender = (byte) (Randomizer.nextInt(2) + 1);
        this.nature = BattleConstants.PokemonNature.randomNature();
        calculateFamily();
    }

    public Battler(MapleMonster stats) {
        this(stats.getStats());
        this.mons = new WeakReference(stats);
    }

    public final void setStats() {
        this.stats = MapleLifeFactory.getMonsterStats(this.monsterId);
    }

    public byte getGender() {
        return this.gender;
    }

    public byte getAbilityIndex() {
        return this.abilityIndex;
    }

    public BattleConstants.PokemonAbility getAbility() {
        return this.ability;
    }

    public byte getIV(BattleConstants.PokemonStat stat) {
        return ((Byte) ((Pair) this.mods.get(stat)).left);
    }

    public void resetNature() {
        this.nature = BattleConstants.PokemonNature.Bashful;
    }

    public long calcHP() {
        long ourHp = BattleConstants.getPokemonCustomHP(this.monsterId, this.stats.getHp());
        return (long) ((ourHp + Math.round(ourHp * (getLevel() - stats.getLevel()) / 50.0)) * getMod(PokemonStat.HP));
    }

    public long calcBaseHP() {
        return this.stats.getHp() + Math.round(this.stats.getHp() * (getLevel() - this.stats.getLevel()) / 50.0D);
    }

    public int getHPPercent() {
        return (int) Math.ceil(this.hp * 100.0D / calcHP());
    }

    public long getCurrentHP() {
        return this.hp;
    }

    public String getGenderString() {
        return this.gender == 1 ? "Male" : this.gender == 2 ? "Female" : "";
    }

    public String getStatusString() {
        if (this.hp <= 0L) {
            return "FAINTED";
        }
        if (this.status == null) {
            return "NONE";
        }
        return new StringBuilder().append(StringUtil.makeEnumHumanReadable(this.status.getStati().name()).toUpperCase()).append(" for ").append(this.statusTurnsLeft).append(" turns").toString();
    }

    public String getItemString() {
        if ((this.item == null) || (this.item.customName == null)) {
            return "None";
        }
        return this.item.customName;
    }

    public String getAbilityString() {
        if (this.ability == null) {
            return "None";
        }
        return new StringBuilder().append(StringUtil.makeEnumHumanReadable(this.ability.name())).append(" - ").append(this.ability.desc).toString();
    }

    public void setStatus(MonsterStatusEffect mse) {
        MonsterStatus stat = mse.getStati();
        if ((this.ability == BattleConstants.PokemonAbility.Immunity) && (stat == MonsterStatus.中毒)) {
            return;
        }
//        if ((this.ability == BattleConstants.PokemonAbility.Insomnia) && (stat == MonsterStatus.恐慌)) {
//            return;
//        }
//        if ((this.ability == BattleConstants.PokemonAbility.Limber) && (stat == MonsterStatus.影网)) {
//            return;
//        }
        if ((this.ability == BattleConstants.PokemonAbility.MagmaArmor) && (stat == MonsterStatus.结冰)) {
            return;
        }
        if ((this.ability == BattleConstants.PokemonAbility.OwnTempo) && (stat == MonsterStatus.挑衅)) {
            return;
        }
//        if ((this.ability == BattleConstants.PokemonAbility.WaterVeil) && (stat == MonsterStatus.烈焰喷射)) {
//            return;
//        }
        if (this.status != null) {
            return;
        }
        this.status = mse;
        this.statusTurnsLeft = (Randomizer.nextInt(3) + 2);
        getMonster().applyStatus(mse);
        if (this.ability == BattleConstants.PokemonAbility.Guts) {
            for (int zz = 0; zz < getStatusTurns() - 1; zz++) {
                setMod(BattleConstants.PokemonStat.ATK, increaseMod(getMod(BattleConstants.PokemonStat.ATK)));
            }
        } else if (this.ability == BattleConstants.PokemonAbility.MarvelScale) {
            for (int zz = 0; zz < getStatusTurns() - 1; zz++) {
                setMod(BattleConstants.PokemonStat.DEF, increaseMod(getMod(BattleConstants.PokemonStat.DEF)));
            }
        } else if (this.ability == BattleConstants.PokemonAbility.QuickFeet) {
            for (int zz = 0; zz < getStatusTurns() - 1; zz++) {
                setMod(BattleConstants.PokemonStat.SPEED, increaseMod(getMod(BattleConstants.PokemonStat.SPEED)));
            }
        } else if ((stat == MonsterStatus.速度) ) {
            for (int zz = 0; zz < getStatusTurns() - 1; zz++) {
                setMod(BattleConstants.PokemonStat.SPEED, decreaseMod(getMod(BattleConstants.PokemonStat.SPEED)));
            }
        }else if (stat == MonsterStatus.挑衅) {
            for (int zz = 0; zz < getStatusTurns() - 1; zz++) {
                setMod(BattleConstants.PokemonStat.ATK, increaseMod(getMod(BattleConstants.PokemonStat.ATK)));
                setMod(BattleConstants.PokemonStat.SPATK, increaseMod(getMod(BattleConstants.PokemonStat.SPATK)));
                setMod(BattleConstants.PokemonStat.DEF, decreaseMod(getMod(BattleConstants.PokemonStat.DEF)));
                setMod(BattleConstants.PokemonStat.SPDEF, decreaseMod(getMod(BattleConstants.PokemonStat.SPDEF)));
            }
            if (this.ability == BattleConstants.PokemonAbility.TangledFeet) {
                setMod(BattleConstants.PokemonStat.SPEED, increaseMod(getMod(BattleConstants.PokemonStat.SPEED)));
            }
        } else if ((stat == MonsterStatus.中毒)
                && (this.ability == BattleConstants.PokemonAbility.Unaware)) {
            setMod(BattleConstants.PokemonStat.ATK, increaseMod(getMod(BattleConstants.PokemonStat.ATK)));
        }
    }

    public MonsterStatusEffect getCurrentStatus() {
        return this.status;
    }

    public void decreaseStatusTurns() {
        if (this.status == null) {
            return;
        }
        this.statusTurnsLeft -= 1;
        if (this.statusTurnsLeft <= 0) {
            getMonster().cancelStatus(this.status.getStati());
            this.status = null;
        }
    }

    public int getStatusTurns() {
        return this.statusTurnsLeft;
    }

    public MapleMonster getMonster() {
        return (MapleMonster) this.mons.get();
    }

    public void setMonster(MapleMonster mons) {
        this.mons = new WeakReference(mons);
    }

    public void setCharacterId(int cc) {
        this.charId = cc;
    }

    public BattleConstants.HoldItem getItem() {
        return this.item;
    }

    public void setItem(int t) {
        this.item = BattleConstants.HoldItem.getPokemonItem(t);
    }

    public String getName() {
        return this.name == null ? getOriginalName() : this.name;
    }

    public int getCharacterId() {
        return this.charId;
    }

    public int getMonsterId() {
        return this.monsterId;
    }

    public int getExp() {
        return this.exp;
    }

    public String getExpString() {
        return StringUtil.makeEnumHumanReadable(this.family.type.name());
    }

    public int getNextExp() {
        if (this.level >= 200) {
            return 0;
        }
        return (int) Math.ceil(this.family.type.value + this.family.type.value * this.level / 10.0D) * this.level * this.level;
    }

    public BattleConstants.Evolution getEvolutionType() {
        for (int i = 0; i < this.family.evolutions.size(); i++) {
            if (this.monsterId == (this.family.evolutions.get(i))) {
                if (i == this.family.evolutions.size() - 1) {
                    return BattleConstants.Evolution.NONE;
                }
                if (i != this.family.evolutions.size() - 2) {
                    return BattleConstants.Evolution.LEVEL;
                }
                if (this.family.evoItem != null) {
                    MapleMonster theMob = MapleLifeFactory.getMonster((this.family.evolutions.get(this.family.evolutions.size() - 1)).intValue());
                    if (this.level > theMob.getStats().getLevel() - 5) {
                        return BattleConstants.Evolution.STONE;
                    }
                }
                return BattleConstants.Evolution.LEVEL;
            }
        }

        return BattleConstants.Evolution.NONE;
    }

    public MapleMonsterStats getStats() {
        return this.stats;
    }

    public double increaseMod(double mod) {
        return this.ability == BattleConstants.PokemonAbility.Contrary ? decreaseM(mod) : increaseM(mod);
    }

    public double increaseM(double mod) {
        if (mod == 3.5D) {
            return 4.0D;
        }
        if (mod == 3.0D) {
            return 3.5D;
        }
        if (mod == 2.5D) {
            return 3.0D;
        }
        if (mod == 2.0D) {
            return 2.5D;
        }
        if (mod == 1.5D) {
            return 2.0D;
        }
        if (mod == 1.0D) {
            return 1.5D;
        }
        if (mod == 0.66D) {
            return 1.0D;
        }
        if (mod == 0.5D) {
            return 0.66D;
        }
        if (mod == 0.4D) {
            return 0.5D;
        }
        if (mod == 0.33D) {
            return 0.4D;
        }
        if (mod == 0.285D) {
            return 0.33D;
        }
        if (mod == 0.25D) {
            return 0.285D;
        }
        return mod;
    }

    public double decreaseMod(double mod) {
        return this.ability == BattleConstants.PokemonAbility.Contrary ? increaseM(mod) : decreaseM(mod);
    }

    private double decreaseM(double mod) {
        if (mod == 4.0D) {
            return 3.5D;
        }
        if (mod == 3.5D) {
            return 3.0D;
        }
        if (mod == 3.0D) {
            return 2.5D;
        }
        if (mod == 2.5D) {
            return 2.0D;
        }
        if (mod == 2.0D) {
            return 1.5D;
        }
        if (mod == 1.5D) {
            return 1.0D;
        }
        if (mod == 1.0D) {
            return 0.66D;
        }
        if (mod == 0.66D) {
            return 0.5D;
        }
        if (mod == 0.5D) {
            return 0.4D;
        }
        if (mod == 0.4D) {
            return 0.33D;
        }
        if (mod == 0.33D) {
            return 0.285D;
        }
        if (mod == 0.285D) {
            return 0.25D;
        }
        return mod;
    }

    public BattleConstants.PokemonNature getNature() {
        return this.nature;
    }

    public void setMod(BattleConstants.PokemonStat stat, double mod) {
        ((Pair) this.mods.get(stat)).right = mod;
    }

    public int getEVA() {
        return (int) Math.round((this.stats.getEva() + getLevel()) * getMod(BattleConstants.PokemonStat.EVA) * (this.item == BattleConstants.HoldItem.Sea_Dust ? 1.2D : 1.0D));
    }

    public int getACC() {
        return (int) Math.round((this.stats.getAcc() + getLevel()) * getMod(BattleConstants.PokemonStat.ACC) * (this.ability == BattleConstants.PokemonAbility.Hustle ? 0.7D : this.ability == BattleConstants.PokemonAbility.Compoundeyes ? 1.3D : 1.0D));
    }

    public int getATK(int atk) {
        return (int) Math.round((((atk <= 0) || (this.stats.getMobAttack(atk) == null) || (this.stats.getMobAttack(atk).PADamage <= 0) ? this.stats.getPhysicalAttack() : this.stats.getMobAttack(atk).PADamage) + (getLevel() - this.stats.getLevel()) * 5) * getMod(BattleConstants.PokemonStat.ATK) * (this.ability == BattleConstants.PokemonAbility.PurePower ? 1.5D : (this.ability == BattleConstants.PokemonAbility.HugePower) || (this.ability == BattleConstants.PokemonAbility.Hustle) ? 2.0D : (this.ability == BattleConstants.PokemonAbility.Defeatist) && (getHPPercent() <= 50) ? 0.5D : 1.0D));
    }

    public int getSpATK(int atk) {
        return (int) Math.round((((atk <= 0) || (this.stats.getMobAttack(atk) == null) || (this.stats.getMobAttack(atk).MADamage <= 0) ? this.stats.getMagicAttack() : this.stats.getMobAttack(atk).MADamage) + (getLevel() - this.stats.getLevel()) * 5) * getMod(BattleConstants.PokemonStat.SPATK) * ((this.ability == BattleConstants.PokemonAbility.Defeatist) && (getHPPercent() <= 50) ? 0.5D : 1.0D));
    }

    public int getDEF() {
        return (int) Math.round(this.stats.getPDRate() * getMod(BattleConstants.PokemonStat.DEF));
    }

    public int getSpDEF() {
        return (int) Math.round(this.stats.getMDRate() * getMod(BattleConstants.PokemonStat.SPDEF));
    }

    public int getSpeed() {
        return (int) Math.round((this.stats.getSpeed() + (this.ability == BattleConstants.PokemonAbility.Stall ? 0 : 100) + (getLevel() - this.stats.getLevel()) / 2) * getMod(BattleConstants.PokemonStat.SPEED));
    }

    public double getMod(BattleConstants.PokemonStat stat) {
        return ((Double) ((Pair) this.mods.get(stat)).right) * (((Byte) ((Pair) this.mods.get(stat)).left) / 250.0D + 0.8D) * (this.nature.inc == stat ? 1.1D : 1.0D) * (this.nature.dec == stat ? 0.9D : 1.0D);
    }

    public int getAverageIV() {
        int total = 0;
        int num = 0;
        for (Map.Entry stat : this.mods.entrySet()) {
            if (stat.getKey() != BattleConstants.PokemonStat.NONE) {
                total += ((int) ((Pair) stat.getValue()).left);
                num++;
            }
        }
        return total / num;
    }

    public String getIVString() {
        StringBuilder ss = new StringBuilder();
        for (Map.Entry stat : this.mods.entrySet()) {
            if (stat.getKey() != BattleConstants.PokemonStat.NONE) {
                ss.append("#b#e").append(StringUtil.makeEnumHumanReadable(((Enum<PokemonStat>) stat.getKey()).name()).toUpperCase()).append("#n#k - ").append(getIVString(((int) ((Pair) stat.getValue()).left))).append("\r\n");
            }
        }
        return ss.append("#b#e").append("OVERALL").append("#n#k - ").append(getIVString_Average(getAverageIV())).toString();
    }

    public String getIVString(int avg) {
        if (avg >= 90) {
            return "This stat is absolutely flawless!";
        }
        if (avg >= 80) {
            return "This stat is amazing, outstanding even.";
        }
        if (avg >= 70) {
            return "This stat is pretty good.";
        }
        if (avg >= 60) {
            return "This stat is just above average.";
        }
        if (avg >= 50) {
            return "This stat is about average.";
        }
        if (avg >= 40) {
            return "This stat is just below average.";
        }
        if (avg >= 30) {
            return "This stat could be much better.";
        }
        if (avg >= 20) {
            return "This stat isn't that great.";
        }
        if (avg >= 10) {
            return "This stat will be outdone by many other monsters.";
        }
        return "This stat is just horrendous.";
    }

    public String getIVString_Average(int avg) {
        if (avg >= 90) {
            return "This monster is absolutely flawless!";
        }
        if (avg >= 80) {
            return "This monster is amazing, outstanding even.";
        }
        if (avg >= 70) {
            return "This monster does pretty well.";
        }
        if (avg >= 60) {
            return "This monster is just above average.";
        }
        if (avg >= 50) {
            return "This monster is about average.";
        }
        if (avg >= 40) {
            return "This monster is just below average.";
        }
        if (avg >= 30) {
            return "This monster could do much better.";
        }
        if (avg >= 20) {
            return "This monster isn't that great.";
        }
        if (avg >= 10) {
            return "This monster will be outperformed by many other monsters.";
        }
        return "This monster should be abandoned right away.";
    }

    public void clearIV() {
        for (Pair stat : this.mods.values()) {
            stat.left = 50;
        }
        this.gender = 0;
    }

    public BattleConstants.PokemonElement[] getElements() {
        return this.elements;
    }

    public String getElementString() {
        return new StringBuilder().append(StringUtil.makeEnumHumanReadable(this.elements[0].name()).toUpperCase()).append(this.elements[1] == BattleConstants.PokemonElement.None ? "" : new StringBuilder().append("/").append(StringUtil.makeEnumHumanReadable(this.elements[1].name()).toUpperCase()).toString()).toString();
    }

    public String getNatureString() {
        return new StringBuilder().append(StringUtil.makeEnumHumanReadable(this.nature.name())).append("(+").append(StringUtil.makeEnumHumanReadable(this.nature.inc.name())).append("/-").append(StringUtil.makeEnumHumanReadable(this.nature.dec.name())).append(")").toString();
    }

    public String getFamilyString() {
        return StringUtil.makeEnumHumanReadable(this.family.name());
    }

    public int getLevel() {
        return this.tempLevel > 0 ? this.tempLevel : this.level;
    }

    public void setTempLevel(int te) {
        this.tempLevel = te;
    }

    public BattleConstants.PokemonMob getFamily() {
        return this.family;
    }

    public String getOriginalName() {
        return this.stats.getName();
    }

    public void resetHP() {
        this.hp = calcHP();
    }

    public void resetStats() {
        wipeStatus();
        wipe();
        resetHP();
        this.tempLevel = 0;
    }

    public void wipeStatus() {
        this.status = null;
        this.statusTurnsLeft = 0;
    }

    public void wipe() {
        this.mons = new WeakReference(null);
        this.damagedChars.clear();
        for (Pair stat : this.mods.values()) {
            stat.right = 1.0D;
        }
    }

    public void pushElement(BattleConstants.PokemonElement pe) {
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == BattleConstants.PokemonElement.None) {
                this.elements[i] = pe;
                return;
            }
        }
    }

    public int getElementSize() {
        int ret = 0;
        for (BattleConstants.PokemonElement element : this.elements) {
            if (element != BattleConstants.PokemonElement.None) {
                ret++;
            }
        }
        return ret;
    }

    private void calculateFamily() {
        if (this.stats == null) {
            return;
        }
        pushElement(BattleConstants.PokemonElement.getById(this.stats.getCategory()));
        for (Map.Entry e : this.stats.getElements().entrySet()) {
            if ((e.getValue() == ElementalEffectiveness.IMMUNE) || (e.getValue() == ElementalEffectiveness.STRONG)) {
                pushElement(BattleConstants.PokemonElement.getFromElement((Element) e.getKey()));
                break;
            }
        }
        if (this.elements[0] == BattleConstants.PokemonElement.None) {
            pushElement(BattleConstants.PokemonElement.Normal);
        }
        resetStats();
        List ourFamilies = new ArrayList();
        for (BattleConstants.PokemonMob mob : BattleConstants.PokemonMob.values()) {
            if (mob.evolutions.contains(this.monsterId)) {
                ourFamilies.add(mob);
            }
        }
        if (ourFamilies.size() > 0) {
            this.family = ((BattleConstants.PokemonMob) ourFamilies.get(Randomizer.nextInt(ourFamilies.size())));
        }
        if (this.family != null) {
            byte Gender = BattleConstants.getGender(this.family);
            if (Gender >= 0) {
                this.gender = Gender;
            }
            if (this.abilityIndex == 0) {
                this.ability = this.family.ability1;
            } else {
                this.ability = this.family.ability2;
            }
        }
    }

    public void setName(String n) {
        this.name = n;
    }

    public double getCatchRate() {
        return 256.0D - this.level * 255.0D / 200.0D;
    }

    public double canCatch(double catchChance) {
        return (3.0D * calcHP() - 2.0D * getCurrentHP()) * (getCatchRate() * catchChance) / (3.0D * calcHP()) * (this.status == null ? 1.0D : 1.5D);
    }

    public void damage(int damage, MapleMap map, int uniqueidFrom, boolean leaveStanding) {
        long oldHp = this.hp;
        this.hp -= damage;
        this.hp = Math.min(this.hp, calcHP());
        this.hp = Math.max(this.hp, leaveStanding ? 1L : 0L);
        if (map != null) {
            int oid = getMonster().getObjectId();
            map.broadcastMessage(MobPacket.damageMonster(oid, damage));
        }
    }

    public void addMonsterId(int uniqueId) {
        if (!this.damagedChars.contains(uniqueId)) {
            this.damagedChars.add(uniqueId);
        }
    }

    public void removeMonsterId(int uniqueId) {
        for (int i = 0; i < this.damagedChars.size(); i++) {
            if ((this.damagedChars.size() > i) && ((this.damagedChars.get(i)) == uniqueId)) {
                this.damagedChars.remove(i);
            }
        }
    }

    public int getTrueLevel() {
        return this.level;
    }

    public int getOurExp() {
        int theExp = Math.max(1, this.stats.getExp());
        return (int) Math.min(100L, calcBaseHP() / (theExp == 1 ? this.stats.getLevel() / 5 : theExp)) * this.level / 2;
    }

    public int getExp(boolean npc, int uniqueId) {
        if (!this.damagedChars.contains(uniqueId)) {
            return 0;
        }
        int theExp = Math.max(1, this.stats.getExp());
        return (int) Math.min(100L, calcBaseHP() / (theExp == 1 ? this.stats.getLevel() / 5 : theExp)) * this.level * (npc ? 3 : 2) / 4 / this.damagedChars.size();
    }

    public void gainExp(int xp, MapleCharacter chr) {
        if (this.level >= 200) {
            this.exp = 0;
            return;
        }
        this.exp += xp;
        while (this.exp > getNextExp()) {
            this.exp -= getNextExp();
            this.level += 1;
            if (this.level >= 200) {
                this.exp = 0;
                return;
            }
            if ((getEvolutionType() != BattleConstants.Evolution.LEVEL) || (this.item == BattleConstants.HoldItem.Question_Mark)) {
                continue;
            }
            evolve(false, chr);
        }
    }

    public void evolve(boolean skipCheck, MapleCharacter chr) {
        boolean rename = this.name.equalsIgnoreCase(this.stats.getName());
        List evo = this.family.evolutions;
        int ourIndex = -1;
        for (int i = 0; i < evo.size(); i++) {
            if (((Integer) evo.get(i)) == this.monsterId) {
                ourIndex = i;
                break;
            }
        }
        if ((ourIndex >= 0) && (evo.size() > ourIndex + 1)) {
            MapleMonster next = MapleLifeFactory.getMonster(((Integer) evo.get(ourIndex + 1)).intValue());
            if ((this.level >= next.getStats().getLevel()) || (skipCheck)) {
                this.monsterId = next.getId();
                this.stats = next.getStats();
                if (rename) {
                    this.name = this.stats.getName();
                }
            }
        }
    }

    public List<Integer> getDamaged() {
        return damagedChars;
    }
}
