package constants;

import client.Battler;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import server.life.Element;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterStats;
import tools.FileoutputUtil;
import tools.Pair;

public class BattleConstants {

    private static List<PokedexEntry> pokedexEntries = new ArrayList<>();
    private static List<Integer> gmMobs = new ArrayList<>();
    private static EnumMap<PokemonMap, LinkedList<Pair<Integer, Integer>>> mapsToMobs = new EnumMap(PokemonMap.class);

    public static boolean isBattleMap(int mapid) {
        return getMap(mapid) != null;
    }

    public static PokemonMap getMap(int mapid) {
        for (PokemonMap map : PokemonMap.values()) {
            if (map.id == mapid) {
                return map;
            }
        }
        return null;
    }

    public static void init() {
        Map mobsToMaps = new HashMap();
        for (PokemonMap map : PokemonMap.values()) {
            LinkedList set_check = new LinkedList();
            LinkedList set = new LinkedList();
            for (PokemonMob mob : PokemonMob.values()) {
                for (int i = 0; i < mob.evolutions.size(); i++) {
                    int id = (mob.evolutions.get(i));
                    MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(id);
                    if (mons == null) {
                        FileoutputUtil.log("WARNING: monster " + id + " does not exist.");
                    } else if (((id == 6400007) || (!mons.isBoss())) && (mons.getLevel() >= map.minLevel) && (mons.getLevel() <= map.maxLevel) && (!set_check.contains(id)) && (canAdd(id))) {
                        set.add(new Pair(id, i + 1));
                        set_check.add(id);
                        List mtm = (List) mobsToMaps.get(Integer.valueOf(id));
                        if (mtm == null) {
                            mtm = new ArrayList();
                            mobsToMaps.put(id, mtm);
                        }
                        mtm.add(new Pair(map.id, i + 1));
                    }
                }
            }
            set_check.clear();
            mapsToMobs.put(map, set);
        }
        LinkedHashMap pokedex = new LinkedHashMap();

        int pokedexNum = 1;
        for (PokemonMob mob : PokemonMob.values()) {
            for (int i = 0; i < mob.evolutions.size(); i++) {
                int id = (mob.evolutions.get(i));
                if (!pokedex.containsKey(id)) {
                    PokedexEntry pe = new PokedexEntry(id, pokedexNum);
                    List<Pair<Integer, Integer>> mtm = (List) mobsToMaps.get(Integer.valueOf(id));
                    if (mtm != null) {
                        pe.maps = new ArrayList();
                        for (Pair mt : mtm) {
                            pe.maps.add(new Pair(mt.left, (int) Math.round(1.0D / ((LinkedList) mapsToMobs.get(getMap(((Integer) mt.left)))).size() / ((Integer) mt.right) * 10000.0D)));
                        }
                    } else {
                        pe.maps = null;
                    }
                    pe.dummyBattler = new Battler(MapleLifeFactory.getMonsterStats(id));
                    pe.dummyBattler.resetNature();
                    pe.dummyBattler.clearIV();
                    if ((pe.dummyBattler.getStats().isBoss()) && (mob.type == MobExp.EASY)) {
                        gmMobs.add(id);
                    } else {
                        if (i != 0) {
                            MapleMonsterStats mm = MapleLifeFactory.getMonsterStats((mob.evolutions.get(i - 1)).intValue());
                            if (mm != null) {
                                if ((mob.evoItem != null) && (i == mob.evolutions.size() - 1)) {
                                    pe.pre.put(mob.evolutions.get(i - 1), mob.evoItem.id);
                                } else {
                                    pe.pre.put(mob.evolutions.get(i - 1), pe.dummyBattler.getLevel());
                                }
                            }
                        }

                        if (i != mob.evolutions.size() - 1) {
                            MapleMonsterStats mm = MapleLifeFactory.getMonsterStats((mob.evolutions.get(i + 1)).intValue());
                            if (mm != null) {
                                if ((mob.evoItem != null) && (i == mob.evolutions.size() - 2)) {
                                    pe.evo.put(mob.evolutions.get(i + 1), mob.evoItem.id);
                                } else {
                                    pe.evo.put(mob.evolutions.get(i + 1), Integer.valueOf(mm.getLevel()));
                                }
                            }
                        }
                        pokedex.put(id, pe);
                    }
                } else {
                    PokedexEntry pe = (PokedexEntry) pokedex.get(Integer.valueOf(id));
                    if ((i != 0) && (!pe.pre.containsKey(mob.evolutions.get(i - 1)))) {
                        MapleMonsterStats mm = MapleLifeFactory.getMonsterStats((mob.evolutions.get(i - 1)).intValue());
                        if (mm != null) {
                            if ((mob.evoItem != null) && (i == mob.evolutions.size() - 1)) {
                                pe.pre.put(mob.evolutions.get(i - 1), mob.evoItem.id);
                            } else {
                                pe.pre.put(mob.evolutions.get(i - 1), pe.dummyBattler.getLevel());
                            }
                        }
                    }
                    if ((i != mob.evolutions.size() - 1) && (!pe.evo.containsKey(mob.evolutions.get(i + 1)))) {
                        MapleMonsterStats mm = MapleLifeFactory.getMonsterStats((mob.evolutions.get(i + 1)).intValue());
                        if (mm != null) {
                            if ((mob.evoItem != null) && (i == mob.evolutions.size() - 2)) {
                                pe.evo.put(mob.evolutions.get(i + 1), mob.evoItem.id);
                            } else {
                                pe.evo.put(mob.evolutions.get(i + 1), Integer.valueOf(mm.getLevel()));
                            }
                        }
                    }
                }
            }
        }
        pokedexEntries.addAll(pokedex.values());
        mobsToMaps.clear();
        pokedex.clear();
    }

    public static boolean isGMMob(int idd) {
        return gmMobs.contains(idd);
    }

    public static boolean canAdd(int id) {
        switch (id) {
            case 5100001:
            case 5130106:
                return false;
        }
        return true;
    }

    public static LinkedList<Pair<Integer, Integer>> getMobs(PokemonMap mapp) {
        return (LinkedList) mapsToMobs.get(mapp);
    }

    public static List<PokedexEntry> getAllPokedex() {
        return pokedexEntries;
    }

    public static byte getGender(PokemonMob mob) {
        switch (mob) {
            case Bunny:
            case Maverick_Y:
            case Maverick_B:
            case Maverick_V:
            case Maverick_S:
            case Guard:
            case Lord:
            case Roid:
            //case Frankenroid:
            //case Roi:
            //case Mutae:
            //case Rumo:
            case Robot:
            case Robo:
            case Block:
            case Block_Golem:
            case CD:
            case Mannequin:
            case Ninja:
            case Training_Robot:
            case Veil:
            case Veil_2:
            case Veil_3:
            case Veil_4:
            case Veil_5:
            case Unveil:
            case Cake:
            case Egg:
            case Accessory:
            case Clown:
            case Fire_Sentinel:
            case Ice_Sentinel:
            //case Warrior:
            //case Mage:
            //case Mage_1:
            //case Bowman:
            //case Rogue:
            case Rogue_1:
            //case Pirate:
            case Jar:
            case Vehicle:
            //case Dummy_Strong:
            //case Dummy:
            case Keeper:
            case Keeper_2:
                return 0;
            case Dragon:
            case Human_M:
            case Boss_M:
            case Viking:
            case Bird:
            case Black_Bird:
            case Red_Bird:
            case Blue_Bird:
            case Manon:
            case Griffey:
            case Mask:
            case Crow:
                return 1;
            case Kyrin_1:
            case Kyrin_2:
            case Human_F:
            case Witch:
            case Monkey:
            case White_Monkey:
            case Fairy:
            case Doll_V:
            case Doll_H:
            case Road_Auf:
            case Road_Dunas:
            case Cygnus_Boss:
                return 2;
        }
        return -1;
    }

    public static long getPokemonCustomHP(int mobId, long def) {
        switch (mobId) {
            case 8840000:
            case 9400112:
            case 9400113:
                return def / 200L;
            case 8300006:
            case 8300007:
            case 8850000:
            case 8850001:
            case 8850002:
            case 8850003:
            case 8850004:
            case 9400589:
            case 9400590:
            case 9400591:
            case 9400592:
            case 9400593:
            case 9420543:
            case 9420548:
                return def / 100L;
            case 9300158:
            case 9300159:
            case 9400300:
            case 9420544:
            case 9420549:
                return def / 50L;
            case 9400293:
                return def / 40L;
            case 8840003:
            case 8840004:
            case 9300215:
            case 9400121:
            case 9400405:
                return def / 20L;
            case 8210000:
            case 8210001:
            case 8210002:
            case 8210003:
            case 8210004:
            case 8210005:
            case 8210010:
            case 8210011:
            case 8210012:
            case 8600000:
            case 8600001:
            case 8600002:
            case 8600003:
            case 8600004:
            case 8600005:
            case 8600006:
            case 8610000:
            case 8610001:
            case 8610002:
            case 8610003:
            case 8610004:
            case 8610005:
            case 8610006:
            case 8610007:
            case 8610008:
            case 8610009:
            case 8610010:
            case 8610011:
            case 8610012:
            case 8610013:
            case 8610014:
            case 8840001:
            case 8840002:
            case 8840005:
            case 9001010:
            case 9400014:
                return def / 10L;
            case 6090000:
                return 1350000L;
            case 8220004:
                return 2350000L;
            case 8220005:
                return 3200000L;
            case 8220006:
                return 4100000L;
        }
        return def;
    }

    public static enum PokemonAbility {

        Adaptability("Powers moves of the same type"),
        Aftermath("Damages the foe when KOed"),
        Analytic("Powers moves when moving last"),
        AngerPoint("Raises Attack/Sp.Attack when taking a critical hit"),
        BadDreams("Hurts a foe if they are in darkness"),
        BattleArmor("Protects from critical attacks"),
        BigPecks("Protects Defense from lowering"),
        Blaze("Powers up fire type moves in a pinch"),
        ClearBody("Prevents stats from lowering"),
        Compoundeyes("Accuracy is increased"),
        Contrary("Inverts stat modifiers"),
        Defeatist("Halves Attack/Sp.Attack when below 50% HP"),
        Defiant("Raises Attack two stages when any stat is lowered"),
        DrySkin("Fire type moves are more effective, Fish type moves heal HP"),
        EarlyBird("Awakens quickly from darkness"),
        EffectSpore("Contact may paralyze, poison, or cause darkness"),
        Filter("Powers down super effective moves"),
        FlameBody("Contact may burn"),
        FlareBoost("Increases Sp.Attack to 1.5x when burned."),
        Forewarn("Tells of the opponent's type"),
        Frisk("Tells of the opponent's held item"),
        Gluttony("Uses one time items earlier"),
        Guts("Boosts Attack if there is a status problem"),
        Heatproof("Halves fire type moves effect"),
        HugePower("Doubled Attack stat"),
        Hustle("Doubled Attack stat, with lower accuracy"),
        HyperCutter("Prevents Attack from being lowered"),
        Illuminate("Raises likelihood of finding wild pokemon"),
        Immunity("Prevents poison"),
        Insomnia("Prevents darkness"),
        Intimidate("Lowers opponent's Attack"),
        IronBarbs("Damages opponent 1/8 HP on contact"),
        Klutz("Opponent can't use any held items"),
        Limber("Prevents paralysis"),
        LiquidOoze("Hurts foes when they try to absorb HP"),
        MagicGuard("Only hurt by attacks"),
        MagmaArmor("Prevents freezing"),
        MarvelScale("Boosts Defense when there is status"),
        Moody("Raises random stat two stages, lowers another"),
        MotorDrive("Raises Speed when hit by electricity"),
        Moxie("Raises Attack when KOing a monster"),
        Multiscale("When full HP, halves damage taken"),
        NaturalCure("All status healed when switching out"),
        NoGuard("Ensures hit"),
        Normalize("All moves become Normal type"),
        Overgrow("Powers up Plant in a pinch"),
        OwnTempo("Prevents Showdown status"),
        Pickpocket("Steals opponent's held item on contact"),
        PoisonHeal("Heals HP when poisoned"),
        PoisonPoint("Poisons foe on contact"),
        PoisonTouch("Poisons foe on attack, 20% chance"),
        PurePower("Raises power of Physical moves"),
        QuickFeet("Raises Speed in status problem"),
        Regenerator("Heals 1/3 HP when switching"),
        RunAway("Ensures escape from wild monsters"),
        SapSipper("Absorbs Plant moves"),
        Scrappy("All immunities do not apply"),
        SereneGrace("Boosts added effects"),
        ShadowTag("Prevents escape"),
        ShedSkin("Has a greater chance to heal status problems"),
        SheerForce("Increases power, but prevents extra effects"),
        ShieldDust("Blocks extra effects"),
        Sniper("Increased power of critical hits"),
        SpeedBoost("Increases Speed every turn"),
        Stall("Moves last"),
        Static("Paralysis on contact"),
        Stench("Lower chance of meeting wild monsters"),
        SuperLuck("Increased critical hit rate"),
        Synchronize("Opponent receives same status"),
        TangledFeet("Raises evasion when confused"),
        ThickFat("Resists Fire and Ice moves"),
        TintedLens("Powers up not very effective moves"),
        Torrent("Powers up Fish type moves in a pinch"),
        ToxicBoost("Attack 1.5x when poisoned"),
        Truant("Does nothing every second turn"),
        Unaware("Ignores stat changes by the foe"),
        Unburden("Raises Speed if any held item is used"),
        Unnerve("Prevents opposition from using one time use items"),
        VoltAbsorb("Heals HP when hit by electricity"),
        WaterAbsorb("Heals HP when hit by water"),
        WaterVeil("Prevents burning"),
        WeakArmor("Raises Speed, lowers Defense when hit"),
        WonderGuard("Only super effective moves hit");

        public String desc;

        private PokemonAbility(String desc) {
            this.desc = desc;
        }
    }

    public static enum PokemonElement {

        None(0, false, new int[0], new int[0], new int[0]),
        Fire(-1, true, new int[0], new int[]{-1, -5, -9, -10, -12}, new int[]{-2, -6, -8, -11, -14}),
        Ice(-2, true, new int[]{-12}, new int[]{-1, -2, -4, -9, -10}, new int[]{-7, -8, -13, -14}),
        Lightning(-3, true, new int[0], new int[]{-2, -5, -8, -11, -13}, new int[]{-6, -7, -9, -14}),
        Poison(-4, true, new int[]{-11, -14}, new int[]{-1, -2, -4, -6, -12}, new int[]{-3, -5, -7, -8, -9, -10}),
        Holy(-5, true, new int[]{-4}, new int[]{-5, -11}, new int[]{-6, -12, -13}),
        Dark(-6, true, new int[]{-5}, new int[]{-6, -9}, new int[]{-3, -7, -11}),
        Mammal(-7, false, new int[0], new int[]{-4, -10, -11}, new int[]{-8, -9, -12}),
        Plant(-8, false, new int[0], new int[]{-1, -4, -7, -8}, new int[]{-5, -6, -9, -14}),
        Fish(-9, false, new int[0], new int[]{-7, -9, -10}, new int[]{-1, -2, -8}),
        Reptile(-10, false, new int[0], new int[]{-4, -6, -7, -11, -13}, new int[]{-1, -3, -9}),
        Spirit(-11, true, new int[]{-15}, new int[]{-1, -2, -3, -5, -6, -7}, new int[]{-4, -11, -12}),
        Devil(-12, true, new int[0], new int[]{-8, -9, -10, -12, -14}, new int[]{-1, -2, -4, -5, -6}),
        Immortal(-13, false, new int[]{-13}, new int[]{-2, -7, -9, -10}, new int[]{-1, -4, -11, -12, -15}),
        Enchanted(-14, false, new int[0], new int[]{-2, -3, -5, -6, -8, -14}, new int[]{-4, -7, -13, -15}),
        Normal(-15, false, new int[]{-11}, new int[]{-13}, new int[0]);

        public int trueId;
        public boolean special;
        public Set<Integer> immune = new HashSet();
        public Set<Integer> notEffective = new HashSet();
        public Set<Integer> superEffective = new HashSet();

        private PokemonElement(int trueId, boolean special, int[] immune, int[] notEffective, int[] superEffective) {
            this.special = special;
            this.trueId = trueId;
            for (int e : immune) {
                this.immune.add(e);
            }
            for (int e : notEffective) {
                this.notEffective.add(e);
            }
            for (int e : superEffective) {
                this.superEffective.add(e);
            }
        }

        public static PokemonElement getFromElement(Element c) {
            switch (c) {
                case FIRE:
                    return Fire;
                case ICE:
                    return Ice;
                case LIGHTING:
                    return Lightning;
                case POISON:
                    return Poison;
                case HOLY:
                    return Holy;
                case DARKNESS:
                    return Dark;
            }
            return None;
        }

        public static PokemonElement getById(int c) {
            switch (c) {
                case 1:
                    return Mammal;
                case 2:
                    return Plant;
                case 3:
                    return Fish;
                case 4:
                    return Reptile;
                case 5:
                    return Spirit;
                case 6:
                    return Devil;
                case 7:
                    return Immortal;
                case 8:
                    return Enchanted;
            }
            return None;
        }

        public double getEffectiveness(PokemonElement[] stats) {
            double ret = 1.0D;
            for (PokemonElement stat : stats) {
                if (this.immune.contains(stat.trueId)) {
                    return 0.0D;
                }
                if (this.notEffective.contains(stat.trueId)) {
                    ret /= 2.0D;
                } else if (this.superEffective.contains(stat.trueId)) {
                    ret *= 2.0D;
                }
            }
            return ret;
        }
    }

    public static enum PokemonNature {

        Bashful(BattleConstants.PokemonStat.NONE, BattleConstants.PokemonStat.NONE),
        Docile(BattleConstants.PokemonStat.NONE, BattleConstants.PokemonStat.NONE),
        Hardy(BattleConstants.PokemonStat.NONE, BattleConstants.PokemonStat.NONE),
        Quirky(BattleConstants.PokemonStat.NONE, BattleConstants.PokemonStat.NONE),
        Serious(BattleConstants.PokemonStat.NONE, BattleConstants.PokemonStat.NONE),
        Lonely(BattleConstants.PokemonStat.ATK, BattleConstants.PokemonStat.DEF),
        Adamant(BattleConstants.PokemonStat.ATK, BattleConstants.PokemonStat.SPATK),
        Naughty(BattleConstants.PokemonStat.ATK, BattleConstants.PokemonStat.SPDEF),
        Brave(BattleConstants.PokemonStat.ATK, BattleConstants.PokemonStat.SPEED),
        Bold(BattleConstants.PokemonStat.DEF, BattleConstants.PokemonStat.ATK),
        Impish(BattleConstants.PokemonStat.DEF, BattleConstants.PokemonStat.SPATK),
        Lax(BattleConstants.PokemonStat.DEF, BattleConstants.PokemonStat.SPDEF),
        Relaxed(BattleConstants.PokemonStat.DEF, BattleConstants.PokemonStat.SPEED),
        Modest(BattleConstants.PokemonStat.SPATK, BattleConstants.PokemonStat.ATK),
        Mild(BattleConstants.PokemonStat.SPATK, BattleConstants.PokemonStat.DEF),
        Rash(BattleConstants.PokemonStat.SPATK, BattleConstants.PokemonStat.SPDEF),
        Quiet(BattleConstants.PokemonStat.SPATK, BattleConstants.PokemonStat.SPEED),
        Calm(BattleConstants.PokemonStat.SPDEF, BattleConstants.PokemonStat.ATK),
        Gentle(BattleConstants.PokemonStat.SPDEF, BattleConstants.PokemonStat.DEF),
        Careful(BattleConstants.PokemonStat.SPDEF, BattleConstants.PokemonStat.SPATK),
        Sassy(BattleConstants.PokemonStat.SPDEF, BattleConstants.PokemonStat.SPEED),
        Timid(BattleConstants.PokemonStat.SPEED, BattleConstants.PokemonStat.ATK),
        Hasty(BattleConstants.PokemonStat.SPEED, BattleConstants.PokemonStat.DEF),
        Jolly(BattleConstants.PokemonStat.SPEED, BattleConstants.PokemonStat.SPATK),
        Naive(BattleConstants.PokemonStat.SPEED, BattleConstants.PokemonStat.SPDEF);

        public BattleConstants.PokemonStat inc;
        public BattleConstants.PokemonStat dec;

        private PokemonNature(BattleConstants.PokemonStat inc, BattleConstants.PokemonStat dec) {
            this.inc = inc;
            this.dec = dec;
        }

        public static PokemonNature randomNature() {
            return values()[server.Randomizer.nextInt(values().length)];
        }
    }

    public static enum PokemonStat {

        ATK, DEF, SPATK, SPDEF, SPEED, EVA, ACC, HP, NONE;

        public static PokemonStat getRandom() {
            return values()[server.Randomizer.nextInt(values().length - 1)];
        }
    }

    public static enum Turn {

        ATTACK, SWITCH, DISABLED, TRUANT;
    }

    public static enum PokemonItem
            implements BattleConstants.PItem {

        Basic_Ball(3992017, 1.0D),
        Great_Ball(3992018, 1.5D),
        Ultra_Ball(3992019, 2.0D),
        Yellow_Crystal(4001268, 2.0D),
        Blue_Crystal(4001269, 2.0D),
        Saint_Stone(4020012, 1.0D),
        Ice_Pick(4310007, 2.0D),
        Bright_Feather(3994193, 1.0D),
        Gold_Pick(4310001, 0.5D),
        Coin(4310002, 1.0D),
        Corrupted(4001237, 3.0D),
        More_Corrupted(4001238, 2.0D),
        Most_Corrupted(4001239, 1.0D),
        Corrupted_Item(4001240, 2.0D),
        Ancient_Relic(4001302, 2.0D),
        Heart_of_Heart(4001244, 2.0D),
        Phoenix_Egg(4001113, 1.0D),
        Freezer_Egg(4001114, 1.0D),
        Black_Hole(4001190, 0.5D),
        Maple_Marble(4031456, 0.5D),
        Rainbow_Leaf(4032733, 0.5D),
        Intelligence_Document(4001192, 2.0D),
        Dragon_Heart(4031449, 1.0D),
        Griffey_Wind(4031457, 1.0D),
        Deathly_Fear(4031448, 0.5D),
        Summoning_Frame(4031451, 1.0D),
        Magical_Array(4031453, 0.5D),
        Life_Root(4031461, 1.0D),
        Black_Tornado(4031458, 1.0D),
        Perfect_Pitch(4310000, 1.0D),
        Cold_Heart(4031460, 1.0D),
        Ventilation(4031462, 2.0D),
        Old_Glove(4031465, 1.0D),
        Pocket_Watch(4001393, 1.0D),
        Melted_Chocolate(3994199, 1.0D),
        Whirlwind(4031459, 1.0D);

        public int id;
        public double catchChance;

        private PokemonItem(int id, double chance) {
            this.id = id;
            this.catchChance = chance;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public int getItemChance() {
            return (int) (this.catchChance * 2.0D);
        }

        public static boolean isPokemonItem(int itemId) {
            return getPokemonItem(itemId) != null;
        }

        public static BattleConstants.PItem getPokemonItem(int itemId) {
            for (PokemonItem i : values()) {
                if (i.id == itemId) {
                    return i;
                }
            }
            return BattleConstants.HoldItem.getPokemonItem(itemId);
        }
    }

    public static enum HoldItem
            implements BattleConstants.PItem {

        Green_Star(3992010, 0.5D, "Scope Lens - increases critical rate"),
        Orange_Star(3992012, 0.5D, "Quick Claw - sometimes goes first"),
        King_Star(3992025, 0.5D, "King Star - increases status rate"),
        Strange_Slush(3992011, 0.5D, "Shell Bell - absorbs HP"),
        Maha_Charm(3994185, 0.5D, "EXP Share - any monsters holding this will share EXP"),
        Question_Mark(3800088, 2.0D, "Everstone - prevents your monster from evolution"),
        Mini_Dragon(3994187, 0.5D, "Life Orb - more damage, but you get hurt"),
        Pheremone(4031507, 0.5D, "Black Herb - cannot have any status"),
        Kenta_Report(4031509, 0.5D, "White Herb - cannot have stats lowered"),
        Other_World_Key(4031409, 0.5D, "Expert Key - super effective attacks do more damage"),
        Ripped_Note(4031252, 0.5D, "Ripped Note - higher chance of increasing your own stats"),
        Herb_Pouch(4031555, 0.5D, "Herb Pouch - higher chance of decreasing enemy stats"),
        Sea_Dust(4031251, 0.5D, "Brightpowder - increases evasion rate"),
        Medal(4031160, 0.5D, "Medal - increases damage of attacks of the same type"),
        Dark_Chocolate(4031110, 3.0D, "Dark Chocolate - a not very effective attack to the opponent is negated, one time use"),
        White_Chocolate(4031109, 3.0D, "White Chocolate - a super effective attack from the opponent is negated, one time use"),
        Red_Candy(4032444, 4.0D, "Red Candy - when under 50% HP, upgrades attack, one time use"),
        Blue_Candy(4032445, 4.0D, "Blue Candy - when under 50% HP, upgrades defense, one time use"),
        Green_Candy(4032446, 4.0D, "Green Candy - when under 50% HP, upgrades speed, one time use"),
        Strawberry(4140102, 4.0D, "Heal Berry - heals 10% HP when under 50% HP, one time use"),
        Pineapple(4140101, 4.0D, "Cure Berry - heals status, one time use");

        public int id;
        public String customName;
        public double catchChance;

        private HoldItem(int id, double chance, String customName) {
            this.id = id;
            this.catchChance = chance;
            this.customName = customName;
        }

        public static HoldItem getPokemonItem(int itemId) {
            for (HoldItem i : values()) {
                if (i.id == itemId) {
                    return i;
                }
            }
            return null;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public int getItemChance() {
            return (int) (this.catchChance * 2.0D);
        }
    }

    public static abstract interface PItem {

        public abstract int getItemChance();

        public abstract int getId();
    }

    public static enum PokemonMob {

        Snail(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.QuickFeet, BattleConstants.PokemonAbility.Unburden, new Integer[]{100100, 100101, 130101, 9500144, 4250000, 8600000}),
        Snail_2(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.QuickFeet, BattleConstants.PokemonAbility.Unburden, new Integer[]{100100, 100101, 130101, 2220000, 4250000, 8600000}),
        Muru(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.ShedSkin, BattleConstants.PokemonAbility.ShieldDust, new Integer[]{100130, 100131, 100132, 100133, 100134}),
        Ti(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Filter, BattleConstants.PokemonAbility.TintedLens, new Integer[]{100120, 100121, 100122, 100123, 9001011, 8600004, 8600005, 8600006}),
        Flower(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.PurePower, BattleConstants.PokemonAbility.SheerForce, new Integer[]{150000, 150001, 150002, 9300174, 9300179}),
        Spore(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.EffectSpore, BattleConstants.PokemonAbility.Immunity, new Integer[]{120100, 9300386, 3300000}),
        Sage_Cat(BattleConstants.PokemonItem.Bright_Feather, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.RunAway, BattleConstants.PokemonAbility.Normalize, new Integer[]{9400636, 6130209, 7220002}),
        Mushroom(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.EffectSpore, BattleConstants.PokemonAbility.Overgrow, new Integer[]{1210102, 1110100, 2110200, 2220100, 2230101, 9500152, 9400539, 9400550, 3300001, 5250000, 8600001}),
        Mushmom(BattleConstants.PokemonItem.Melted_Chocolate, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.EffectSpore, BattleConstants.PokemonAbility.Overgrow, new Integer[]{1210102, 1110100, 2110200, 2220100, 6130100, 8220007, 6300005, 9300191, 9300196, 9300209}),
        Boogie(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.OwnTempo, BattleConstants.PokemonAbility.Limber, new Integer[]{3230300, 9400005, 9400006, 9400007, 9400008, 6130104}),
        Chaos_Boogie(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.OwnTempo, BattleConstants.PokemonAbility.Limber, new Integer[]{3230300, 9400005, 9400006, 9400007, 9400008, 8800111}),
        Pig(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Gluttony, BattleConstants.PokemonAbility.Scrappy, new Integer[]{1210100, 1210101, 1210104, 4230103, 3300002, 9302011, 9500143, 8600003}),
        Boar(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Gluttony, BattleConstants.PokemonAbility.Scrappy, new Integer[]{1210100, 1210101, 1210104, 2230102, 3210100, 9400516, 4230400, 5250002}),
        Boss_Slime(BattleConstants.PokemonItem.Melted_Chocolate, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Illuminate, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{9400737, 210100, 1210103, 9500151, 9400538, 9300027, 9400521, 9300187}),
        Slime(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Illuminate, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{9400737, 210100, 1210103, 9500151, 9400538, 9300027, 9400521, 9400203, 9420528, 9400204, 3110300, 7120105, 8600002}),
        Fox(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.SpeedBoost, BattleConstants.PokemonAbility.Unnerve, new Integer[]{9300385, 9400002, 9400004, 5100004, 7220001}),
        Mask(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.ShadowTag, BattleConstants.PokemonAbility.Insomnia, new Integer[]{9400706, 2230110, 2230111}),
        MV(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.ShadowTag, BattleConstants.PokemonAbility.Insomnia, new Integer[]{9400706, 9400746}),
        Stump(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.SapSipper, BattleConstants.PokemonAbility.SapSipper, new Integer[]{130100, 1110101, 1130100, 2130100, 1140100, 3220000, 9300172, 9420527, 9420523, 9420514, 9420519}),
        Frog(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Immunity, BattleConstants.PokemonAbility.PoisonHeal, new Integer[]{9400634, 9420001, 9420000}),
        Cake(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.NaturalCure, BattleConstants.PokemonAbility.Synchronize, new Integer[]{9400506, 9400570, 9400507}),
        Cake_2(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.NaturalCure, BattleConstants.PokemonAbility.Synchronize, new Integer[]{9400512, 9400570, 9400513}),
        Training_Robot(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Heatproof, BattleConstants.PokemonAbility.Stall, new Integer[]{9300409, 9300410, 9300411, 9300412, 9300413}),
        Veil(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130201, 9400577, 8610000}),
        Veil_2(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130201, 9400577, 8610001}),
        Veil_3(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130201, 9400577, 8610002}),
        Veil_4(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130201, 9400577, 8610003}),
        Veil_5(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130201, 9400577, 8610004}),
        Unveil(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.FlareBoost, BattleConstants.PokemonAbility.FlameBody, new Integer[]{9300083, 5100002, 6130202}),
        Flying(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.SuperLuck, BattleConstants.PokemonAbility.Sniper, new Integer[]{2300100, 9400595, 9300084, 9300025, 4230107, 8840001}),
        Alien(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Klutz, BattleConstants.PokemonAbility.Multiscale, new Integer[]{1120100, 9001005, 3230302, 3230103, 4230120, 4230121, 4230122, 5120100}),
        Wraith(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Forewarn, BattleConstants.PokemonAbility.Frisk, new Integer[]{3230101, 4230102, 5090000, 9400556, 9400003, 5120506, 9400580}),
        Wraith_Ghost(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Forewarn, BattleConstants.PokemonAbility.Frisk, new Integer[]{3230101, 4230102, 5090000, 9400556, 9400003, 5120506, 6110301}),
        Wraith_Boss(BattleConstants.PokemonItem.Saint_Stone, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Forewarn, BattleConstants.PokemonAbility.Frisk, new Integer[]{3230101, 4230102, 5090000, 9400556, 9400003, 6090003}),
        Fairy(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.SereneGrace, BattleConstants.PokemonAbility.Illuminate, new Integer[]{3000001, 3000007, 9400526, 9400517}),
        Patrol(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.Regenerator, new Integer[]{1150000, 2150003}),
        Bird(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 3230307, 3230308, 3100102, 9400574, 8300000, 8140002, 9400599, 8210004}),
        Black_Bird(BattleConstants.PokemonItem.Saint_Stone, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 9400544, 3230308, 3100102, 9400574, 8300001, 8140001, 9400014}),
        Red_Bird(BattleConstants.PokemonItem.Phoenix_Egg, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 3230307, 3230308, 3100102, 9400574, 8300000, 8140002, 9400599, 9300089}),
        Blue_Bird(BattleConstants.PokemonItem.Freezer_Egg, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 9400544, 3230308, 3100102, 9400574, 8300001, 8140001, 9400599, 9300090}),
        Dragon(BattleConstants.PokemonItem.Dragon_Heart, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 9400544, 3230308, 3100102, 9400574, 8300006, 8300007}),
        Manon(BattleConstants.PokemonItem.Dragon_Heart, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 9400544, 3230308, 3100102, 9400574, 9300291}),
        Griffey(BattleConstants.PokemonItem.Griffey_Wind, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 3230307, 3230308, 3100102, 9400574, 9300292}),
        Crow(BattleConstants.PokemonItem.Intelligence_Document, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Defiant, BattleConstants.PokemonAbility.Compoundeyes, new Integer[]{9420005, 9600001, 9600002, 9400000, 3230307, 3230308, 3100102, 9001013}),
        Monkey(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.EarlyBird, BattleConstants.PokemonAbility.Contrary, new Integer[]{9500383, 9500384, 6130207}),
        White_Monkey(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.EarlyBird, BattleConstants.PokemonAbility.Contrary, new Integer[]{9500383, 9500385, 9500386, 6130207}),
        Sign(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Aftermath, BattleConstants.PokemonAbility.AngerPoint, new Integer[]{1150001, 9420500, 3150000, 9420503}),
        Eye(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Adaptability, BattleConstants.PokemonAbility.Analytic, new Integer[]{2230100, 3230100, 4230100, 2230113, 9400515, 6230100, 8200000}),
        Eye_Drunk(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Adaptability, BattleConstants.PokemonAbility.Analytic, new Integer[]{2230100, 3230100, 2230113, 9400515, 6230100, 8200000}),
        Dark_Snake(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.PoisonTouch, new Integer[]{1150002, 2130103, 9400633}),
        Red_Snake(BattleConstants.PokemonItem.Yellow_Crystal, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.PoisonTouch, new Integer[]{1150002, 2130103, 9420002, 2100105, 4230504, 9420516, 5220004}),
        Black_Slime(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.ClearBody, BattleConstants.PokemonAbility.Stench, new Integer[]{9420502, 9420506, 9420501, 9420529, 9420530, 9420533, 9420534, 9420508, 9420515, 9420517}),
        Golem(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.ClearBody, BattleConstants.PokemonAbility.BigPecks, new Integer[]{5130101, 5130102, 5150000, 9500149, 9500150, 9300416, 9300024, 9300287, 8840005, 8210005, 8190002}),
        Poison_Golem(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.ClearBody, BattleConstants.PokemonAbility.BigPecks, new Integer[]{5130101, 5130102, 5150000, 9300180, 9300181, 9300182}),
        Dual_Blade(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.SpeedBoost, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9001015, 9001016, 9001017, 9001018}),
        Egg(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.MarvelScale, BattleConstants.PokemonAbility.Scrappy, new Integer[]{9400511, 9400510}),
        Monster(BattleConstants.PokemonItem.Perfect_Pitch, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.NoGuard, BattleConstants.PokemonAbility.Hustle, new Integer[]{2150000, 2230114, 9300173, 5250001}),
        Crocodile(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.WaterVeil, BattleConstants.PokemonAbility.WaterAbsorb, new Integer[]{3110100, 5130103, 6220000, 6130204, 8210000, 8840002}),
        Accessory(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Contrary, BattleConstants.PokemonAbility.Contrary, new Integer[]{2150001, 2150002}),
        Wolf(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.HugePower, BattleConstants.PokemonAbility.HyperCutter, new Integer[]{9410000, 9410001, 9410002, 5130104, 5140000, 9500132, 8140000, 9300354}),
        Ninja(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.QuickFeet, BattleConstants.PokemonAbility.SpeedBoost, new Integer[]{9400400, 9400404, 9400401, 9400406, 9400402, 9400403, 9400405}),
        Lizard(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.DrySkin, BattleConstants.PokemonAbility.Moxie, new Integer[]{9420004, 9420003}),
        Sheep(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Moody, new Integer[]{9600003, 9600008}),
        Lupin_Clown(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Guts, BattleConstants.PokemonAbility.BigPecks, new Integer[]{3210800, 4230101, 9302011, 9410003, 9410004}),
        Skeleton(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.BadDreams, BattleConstants.PokemonAbility.WeakArmor, new Integer[]{5150001, 4230125, 4230126, 6230602, 7130103, 8190003, 8190004}),
        Clown(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.BadDreams, BattleConstants.PokemonAbility.WeakArmor, new Integer[]{9400558, 9400640}),
        Bunny(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.QuickFeet, BattleConstants.PokemonAbility.SpeedBoost, new Integer[]{9300414, 9400649, 3230400, 4230300, 5160000, 5160001, 2100100, 2100101, 9300392}),
        Raco(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Moody, BattleConstants.PokemonAbility.Moxie, new Integer[]{9400001, 7150000, 7150003, 8105000}),
        Goat(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Moody, new Integer[]{9600004, 9600005}),
        Witch(BattleConstants.PokemonItem.Ice_Pick, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.IronBarbs, new Integer[]{5300100, 6090001}),
        Red_Tea(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.WaterAbsorb, BattleConstants.PokemonAbility.WaterVeil, new Integer[]{3400000, 3400001, 3400002, 9410005}),
        Yellow_Tea(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.WaterAbsorb, BattleConstants.PokemonAbility.WaterVeil, new Integer[]{3400000, 3400001, 3400002, 9410006}),
        Green_Tea(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.WaterAbsorb, BattleConstants.PokemonAbility.WaterVeil, new Integer[]{3400000, 3400001, 3400002, 9410007}),
        Pepe(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.ThickFat, BattleConstants.PokemonAbility.ThickFat, new Integer[]{3300003, 3300004, 5400000, 6130103, 6230100, 3210450}),
        Muncher(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.BattleArmor, BattleConstants.PokemonAbility.MagmaArmor, new Integer[]{3150001, 3150002, 8105005}),
        Cow(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Moxie, BattleConstants.PokemonAbility.Moody, new Integer[]{9600006, 9600007}),
        Fire_Sentinel(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.MagicGuard, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{5200000, 3000000, 5200002}),
        Ice_Sentinel(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.MagicGuard, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{5200000, 3000000, 5200001}),
        Cellion(BattleConstants.PokemonItem.Most_Corrupted, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.TintedLens, BattleConstants.PokemonAbility.Filter, new Integer[]{9400509, 3210200, 5120001, 6230401, 7130000, 9500315}),
        Lioner(BattleConstants.PokemonItem.Most_Corrupted, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.TintedLens, BattleConstants.PokemonAbility.Filter, new Integer[]{3210201, 5120002, 6230401, 7130000, 9500315}),
        Grupin(BattleConstants.PokemonItem.Most_Corrupted, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.TintedLens, BattleConstants.PokemonAbility.Filter, new Integer[]{3210202, 5120003, 6230401, 7130000, 9500315}),
        Elephant(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Static, BattleConstants.PokemonAbility.Blaze, new Integer[]{9400542, 9400543, 6160001, 6160002}),
        Chronos(BattleConstants.PokemonItem.More_Corrupted, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.BadDreams, BattleConstants.PokemonAbility.Defiant, new Integer[]{9300015, 9300016, 9300017, 9300192}),
        Pixie(BattleConstants.PokemonItem.Corrupted_Item, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.MagicGuard, BattleConstants.PokemonAbility.SereneGrace, new Integer[]{3230200, 4230106, 5120000, 9300038, 9300039}),
        Walrus(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.ThickFat, BattleConstants.PokemonAbility.VoltAbsorb, new Integer[]{9500145, 9500146, 3230405, 4230124, 4230123}),
        Mannequin(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.MarvelScale, BattleConstants.PokemonAbility.Unnerve, new Integer[]{4300006, 4300007, 4300008}),
        Tortie(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.OwnTempo, new Integer[]{4130101, 9500148}),
        Leprechaun(BattleConstants.PokemonItem.Magical_Array, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Defeatist, BattleConstants.PokemonAbility.TangledFeet, new Integer[]{9400583, 9400575}),
        Horse(BattleConstants.PokemonItem.Life_Root, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Defeatist, BattleConstants.PokemonAbility.TangledFeet, new Integer[]{9400563, 3230305, 9400549}),
        Spider(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.ToxicBoost, BattleConstants.PokemonAbility.TangledFeet, new Integer[]{9400540, 7150001, 9400545}),
        Leatty(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Multiscale, BattleConstants.PokemonAbility.Heatproof, new Integer[]{5300000, 5300001}),
        Night(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.LiquidOoze, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9400011, 9400013}),
        Night_Boss(BattleConstants.PokemonItem.Bright_Feather, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.LiquidOoze, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9400011, 6090002}),
        CD(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.Defeatist, new Integer[]{4300009, 4300010, 4300011, 4300012, 4300013}),
        Goblin(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Analytic, BattleConstants.PokemonAbility.Adaptability, new Integer[]{9500387, 9500388, 9400012, 9500389}),
        Yellow_Goblin(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Analytic, BattleConstants.PokemonAbility.Adaptability, new Integer[]{9500387, 9500388, 9400012, 7130400}),
        Blue_Goblin(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Analytic, BattleConstants.PokemonAbility.Adaptability, new Integer[]{9500387, 9500388, 9400012, 7130401}),
        Green_Goblin(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Analytic, BattleConstants.PokemonAbility.Adaptability, new Integer[]{9500387, 9500388, 9400012, 7130402}),
        Ratz(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.Limber, new Integer[]{3110102, 3210205}),
        Retz(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.Limber, new Integer[]{3110102, 3210208}),
        Human_M(BattleConstants.PokemonItem.Gold_Pick, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9400100, 9400101, 9400102, 9400110, 9400111, 9400120, 9400112}),
        Boss_M(BattleConstants.PokemonItem.Gold_Pick, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9400112, 9400113, 9400300}),
        Human_F(BattleConstants.PokemonItem.Gold_Pick, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9400103, 9400121}),
        Rogue_1(BattleConstants.PokemonItem.Coin, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9400100, 9400101, 9400102, 9400110, 9400111, 9001003, 8610016}),
        Kyrin_1(BattleConstants.PokemonItem.Coin, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9001004, 9300158}),
        Kyrin_2(BattleConstants.PokemonItem.Coin, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{9001004, 9300159}),
        Yeti_Weak(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.ThickFat, BattleConstants.PokemonAbility.Unburden, new Integer[]{5100000, 5100001, 9300258, 6300001, 7130102}),
        Yeti_Strong(BattleConstants.PokemonItem.Blue_Crystal, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.ThickFat, BattleConstants.PokemonAbility.Unburden, new Integer[]{5100000, 5100001, 9300258, 6300001, 8220001}),
        Dark_Yeti_Weak(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.ThickFat, BattleConstants.PokemonAbility.Unburden, new Integer[]{5100000, 5130106, 9500128, 6400001, 8140100}),
        Camera(BattleConstants.PokemonItem.Summoning_Frame, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.Static, new Integer[]{9400546, 6150000, 7150004, 7090000}),
        Jar(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.ShieldDust, BattleConstants.PokemonAbility.ClearBody, new Integer[]{9001022, 4230506}),
        Balrog(BattleConstants.PokemonItem.Most_Corrupted, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Intimidate, new Integer[]{6400007, 8130100, 8150000, 9400514}),
        Vehicle(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.Regenerator, new Integer[]{9420507, 9420504, 9420505, 9420518}),
        Teddy(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Synchronize, BattleConstants.PokemonAbility.Insomnia, new Integer[]{3000005, 3110101, 3210203, 9001028, 6230500, 7130010, 7130300}),
        Drake(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Adaptability, BattleConstants.PokemonAbility.Aftermath, new Integer[]{4130100, 5130100, 6130100, 6230600, 6230601}),
        Doll_V(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Contrary, BattleConstants.PokemonAbility.LiquidOoze, new Integer[]{9400559, 9400561}),
        Doll_H(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Contrary, BattleConstants.PokemonAbility.NoGuard, new Integer[]{9400560, 9400562}),
        Bellflower(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Overgrow, BattleConstants.PokemonAbility.SapSipper, new Integer[]{9001023, 5120502}),
        Sea(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Torrent, BattleConstants.PokemonAbility.WaterAbsorb, new Integer[]{2230105, 2230106}),
        Plane(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.Stall, new Integer[]{3230303, 3210206, 3230304}),
        Fish(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.WaterVeil, BattleConstants.PokemonAbility.Torrent, new Integer[]{2230107, 2230109, 2230200, 3000006, 3230104, 4230200, 7130020, 8140600, 9300099, 8150101}),
        Fish_Poison(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.WaterVeil, BattleConstants.PokemonAbility.Torrent, new Integer[]{2230107, 2230109, 2230200, 3000006, 3230104, 4230201, 7130020, 8140600, 8150100, 8150101}),
        Scorpion(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.ToxicBoost, BattleConstants.PokemonAbility.PoisonTouch, new Integer[]{5160002, 5160003, 2110301}),
        Tiger(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.HyperCutter, new Integer[]{5100003, 5100005}),
        Coketump(BattleConstants.PokemonItem.Coin, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Regenerator, BattleConstants.PokemonAbility.NaturalCure, new Integer[]{9500154, 9500153, 8220009}),
        Scarlion(BattleConstants.PokemonItem.Maple_Marble, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.PurePower, new Integer[]{9420531, 9420535, 9420538, 9420548}),
        Targa(BattleConstants.PokemonItem.Maple_Marble, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.PurePower, new Integer[]{9420532, 9420536, 9420539, 9420540, 9420543}),
        Xerxes(BattleConstants.PokemonItem.Ancient_Relic, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.Immunity, new Integer[]{5160005, 5160006, 5160004, 6160000, 6160003}),
        Robo(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.MotorDrive, new Integer[]{4230111, 4230112}),
        Block_Golem(BattleConstants.PokemonItem.Corrupted, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.PurePower, BattleConstants.PokemonAbility.ShieldDust, new Integer[]{4230109, 4230110, 4130103}),
        Block(BattleConstants.PokemonItem.More_Corrupted, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.PurePower, BattleConstants.PokemonAbility.ShieldDust, new Integer[]{4230109, 4230110, 9300390}),
        Tauro(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.VoltAbsorb, BattleConstants.PokemonAbility.Static, new Integer[]{7130100, 7130101}),
        Gray(BattleConstants.PokemonItem.Ancient_Relic, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.RunAway, BattleConstants.PokemonAbility.Adaptability, new Integer[]{4230116, 4230117, 4230118, 4240000, 6220001}),
        Zakum(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.AngerPoint, BattleConstants.PokemonAbility.ClearBody, new Integer[]{6300004, 6400003, 6230101, 6300003, 6400004}),
        Chaos_Zakum(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.AngerPoint, BattleConstants.PokemonAbility.ClearBody, new Integer[]{8800116, 8800114, 8800112, 8800113, 8800115}),
        Robot(BattleConstants.PokemonItem.Ventilation, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.Regenerator, new Integer[]{6150000, 7150004, 8105001, 8105002, 9001035}),
        Tick(BattleConstants.PokemonItem.Pocket_Watch, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Insomnia, BattleConstants.PokemonAbility.EarlyBird, new Integer[]{3210207, 4230113, 5220003}),
        Crew(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.SheerForce, BattleConstants.PokemonAbility.Scrappy, new Integer[]{9001030, 6130208, 7130104}),
        Crew_Angry(BattleConstants.PokemonItem.Summoning_Frame, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.SheerForce, BattleConstants.PokemonAbility.Scrappy, new Integer[]{9001030, 9300105}),
        Cactus(BattleConstants.PokemonItem.Heart_of_Heart, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.SapSipper, BattleConstants.PokemonAbility.EffectSpore, new Integer[]{2100102, 2100103, 2100104, 3220001}),
        Keeper(BattleConstants.PokemonItem.Rainbow_Leaf, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Forewarn, new Integer[]{9400576, 9400581, 9400578, 9400579, 9400582, 9400596}),
        Keeper_2(BattleConstants.PokemonItem.Rainbow_Leaf, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Forewarn, new Integer[]{9400576, 9400581, 9400578, 9400579, 9400582, 9400597}),
        Ani(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Gluttony, new Integer[]{8210006, 8210007, 8210010}),
        Boom(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.ShadowTag, BattleConstants.PokemonAbility.ShedSkin, new Integer[]{8500003, 8510100, 8500004}),
        Plead(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Gluttony, BattleConstants.PokemonAbility.Guts, new Integer[]{2100106, 2100108, 2100107}),
        Buffoon(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Truant, BattleConstants.PokemonAbility.HugePower, new Integer[]{6300100, 6400100}),
        Papulatus_Clock(BattleConstants.PokemonItem.Pocket_Watch, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Insomnia, BattleConstants.PokemonAbility.EarlyBird, new Integer[]{5220003, 9500180}),
        Papulatus(BattleConstants.PokemonItem.Pocket_Watch, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Insomnia, BattleConstants.PokemonAbility.EarlyBird, new Integer[]{5220003, 9500181}),
        Porky(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Normalize, BattleConstants.PokemonAbility.Gluttony, new Integer[]{4230500, 4230501, 4230502}),
        Sand(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.SheerForce, BattleConstants.PokemonAbility.Intimidate, new Integer[]{2110300, 3100101, 4230600}),
        Dark_Sand(null, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.SheerForce, BattleConstants.PokemonAbility.Intimidate, new Integer[]{2110300, 3110101, 4230600}),
        Hoblin_1(BattleConstants.PokemonItem.Magical_Array, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9300276, 9300279, 9300280, 9300281}),
        Hoblin_2(BattleConstants.PokemonItem.Magical_Array, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9300277, 9300279, 9300280, 9300281}),
        Hoblin_3(BattleConstants.PokemonItem.Magical_Array, BattleConstants.MobExp.ERRATIC, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.QuickFeet, new Integer[]{9300278, 9300279, 9300280, 9300281}),
        Roid(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Regenerator, BattleConstants.PokemonAbility.Immunity, new Integer[]{5110301, 5110302, 7110300, 8105003, 8105004, 9001034}),
        Reindeer(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Gluttony, BattleConstants.PokemonAbility.Guts, new Integer[]{5120505, 8210001, 8210002}),
        Ghost(BattleConstants.PokemonItem.Old_Glove, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Frisk, BattleConstants.PokemonAbility.NoGuard, new Integer[]{9420509, 9420511, 9420510, 9420512, 9420513}),
        Buffy(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.Truant, BattleConstants.PokemonAbility.Moody, new Integer[]{6130200, 6230300}),
        Homun(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Scrappy, BattleConstants.PokemonAbility.Filter, new Integer[]{6110300, 7110301, 8110300}),
        Rash(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.BattleArmor, BattleConstants.PokemonAbility.EarlyBird, new Integer[]{7130500, 7130501}),
        Beetle(null, BattleConstants.MobExp.FAST, BattleConstants.PokemonAbility.BigPecks, BattleConstants.PokemonAbility.BattleArmor, new Integer[]{7130002, 7130003}),
        Scarlion_Boss(BattleConstants.PokemonItem.Maple_Marble, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.PurePower, new Integer[]{9420548, 9420549}),
        Targa_Boss(BattleConstants.PokemonItem.Maple_Marble, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.PurePower, new Integer[]{9420543, 9420544}),
        Cornian(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.HyperCutter, BattleConstants.PokemonAbility.Defiant, new Integer[]{9500374, 8150200, 8150201}),
        Hobi(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.OwnTempo, BattleConstants.PokemonAbility.Limber, new Integer[]{7130600, 7130601, 7130004}),
        Gatekeeper_Nex(null, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{7120100, 7120101, 7120102, 8120100, 8120101, 8140510}),
        Wyvern(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Defeatist, new Integer[]{8300002, 8150301, 8150302}),
        Klock(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.EarlyBird, BattleConstants.PokemonAbility.Insomnia, new Integer[]{8140100, 8140200}),
        Viking(null, BattleConstants.MobExp.SLOW, BattleConstants.PokemonAbility.SuperLuck, BattleConstants.PokemonAbility.Sniper, new Integer[]{7140000, 7160000, 8141000, 8141100}),
        Lord(BattleConstants.PokemonItem.Black_Tornado, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.NaturalCure, BattleConstants.PokemonAbility.Regenerator, new Integer[]{7120106, 7120107, 8120102, 8120103, 8220012}),
        Birk(null, BattleConstants.MobExp.STANDARD, BattleConstants.PokemonAbility.Synchronize, BattleConstants.PokemonAbility.TintedLens, new Integer[]{8140110, 8140111}),
        Road_Auf(BattleConstants.PokemonItem.Cold_Heart, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Immunity, BattleConstants.PokemonAbility.MarvelScale, new Integer[]{7120109, 8220011}),
        Road_Dunas(BattleConstants.PokemonItem.Whirlwind, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Immunity, BattleConstants.PokemonAbility.MarvelScale, new Integer[]{7120109, 8220010}),
        PhantomGatekeeper(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.ShedSkin, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{8142000, 8143000, 8160000}),
        PhantomThanatos(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.ShedSkin, BattleConstants.PokemonAbility.ShadowTag, new Integer[]{8142000, 8143000, 8170000}),
        Cerebes(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.RunAway, BattleConstants.PokemonAbility.FlameBody, new Integer[]{4230108, 7130001, 8140500}),
        Guard(BattleConstants.PokemonItem.Corrupted_Item, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.MagmaArmor, BattleConstants.PokemonAbility.BattleArmor, new Integer[]{8140511, 8140512}),
        Ani_Strong(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Gluttony, new Integer[]{8840003, 8840004, 8210012}),
        Ani_Weak(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Stall, new Integer[]{8840003, 8840004, 8210011}),
        Von_Strong(BattleConstants.PokemonItem.Deathly_Fear, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Intimidate, BattleConstants.PokemonAbility.Stall, new Integer[]{8840003, 8840004, 8840000}),
        Turtle(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.Analytic, new Integer[]{8140700, 8140701}),
        Ton(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.Stall, BattleConstants.PokemonAbility.Analytic, new Integer[]{8140702, 8140703}),
        Maverick_Y(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.MotorDrive, new Integer[]{8120104, 8120105, 8120106}),
        Maverick_B(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.MotorDrive, new Integer[]{8120104, 8120105, 8120106}),
        Maverick_V(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.MotorDrive, new Integer[]{8120104, 8120105, 8120106}),
        Maverick_S(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.MotorDrive, BattleConstants.PokemonAbility.MotorDrive, new Integer[]{8120104, 8120105, 8120106}),
        Monk(null, BattleConstants.MobExp.FRUSTRATING, BattleConstants.PokemonAbility.SereneGrace, BattleConstants.PokemonAbility.MagicGuard, new Integer[]{8200001, 8200002, 8200005, 8200006, 8200009, 8200010}),
        Dodo(BattleConstants.PokemonItem.Maple_Marble, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Truant, BattleConstants.PokemonAbility.Stall, new Integer[]{8200003, 8200004, 8220004}),
        Lillinof(BattleConstants.PokemonItem.Rainbow_Leaf, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Defeatist, BattleConstants.PokemonAbility.Stall, new Integer[]{8200007, 8200008, 8220005}),
        Raika(BattleConstants.PokemonItem.Black_Hole, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Defeatist, BattleConstants.PokemonAbility.Stall, new Integer[]{8200011, 8200012, 8220006}),
        Guardian(null, BattleConstants.MobExp.IMPOSSIBLE, BattleConstants.PokemonAbility.Defeatist, BattleConstants.PokemonAbility.Truant, new Integer[]{8200003, 8200004, 8200007, 8200008, 8200011, 8200012}),
        Cygnus_Boss(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.WonderGuard, BattleConstants.PokemonAbility.WonderGuard, new Integer[]{8850011}),
        Pink_Bean(null, BattleConstants.MobExp.EASY, BattleConstants.PokemonAbility.WonderGuard, BattleConstants.PokemonAbility.WonderGuard, new Integer[]{8820001});

        public BattleConstants.PokemonItem evoItem;
        public BattleConstants.MobExp type;
        public BattleConstants.PokemonAbility ability1;
        public BattleConstants.PokemonAbility ability2;
        public List<Integer> evolutions;

        private PokemonMob(BattleConstants.PokemonItem evoItem, BattleConstants.MobExp type, BattleConstants.PokemonAbility ability1, BattleConstants.PokemonAbility ability2, Integer[] evo) {
            this.type = type;
            this.ability1 = ability1;
            this.ability2 = ability2;
            this.evoItem = evoItem;
            this.evolutions = Arrays.asList(evo);
        }
    }

    public static class PokedexEntry {

        public int id;
        public int num;
        public Battler dummyBattler;
        public Map<Integer, Integer> pre = new LinkedHashMap();
        public Map<Integer, Integer> evo = new LinkedHashMap();
        public List<Pair<Integer, Integer>> maps;

        public PokedexEntry(int id, int num) {
            this.id = id;
            this.num = num;
        }

        public List<Map.Entry<Integer, Integer>> getPre() {
            return new ArrayList(this.pre.entrySet());
        }

        public List<Map.Entry<Integer, Integer>> getEvo() {
            return new ArrayList(this.evo.entrySet());
        }
    }

    public static enum MobExp {

        EASY(0.07000000000000001D),
        ERRATIC(0.1D),
        FAST(0.13D),
        STANDARD(0.16D),
        SLOW(0.19D),
        FRUSTRATING(0.22D),
        IMPOSSIBLE(0.25D);

        public double value;

        private MobExp(double value) {
            this.value = value;
        }
    }

    public static enum Evolution {

        NONE(0),
        LEVEL(1),
        STONE(2);

        public int value;

        private Evolution(int value) {
            this.value = value;
        }
    }

    public static enum PokemonMap {
        // premium·
        MAP1(190000000, 1, 4, 2, new Point(20, 35), new Point(320, 35)),
        MAP2(190000001, 1, 4, 0, new Point(-220, 215), new Point(80, 215)),
        MAP3(190000002, 1, 4, 3, new Point(-400, 215), new Point(-100, 215)),
        MAP4(191000000, 6, 4, 7, new Point(130, 278), new Point(430, 278)),
        MAP5(191000001, 6, 4, 1, new Point(-90, -15), new Point(210, -30)),
        MAP6(192000000, 11, 4, 4, new Point(1100, 2205), new Point(1400, 2205)),
        MAP7(192000001, 11, 4, 4, new Point(1100, 2205), new Point(1400, 2205)),
        MAP8(195000000, 16, 4, 3, new Point(1500, 1294), new Point(1800, 1294)),
        MAP9(195010000, 16, 4, 2, new Point(300, 1659), new Point(0, 1659), true),
        MAP10(195020000, 16, 4, 1, new Point(70, -31), new Point(370, -31)),
        MAP11(195030000, 16, 4, 1, new Point(-200, 160), new Point(100, 160)),
        MAP12(196000000, 21, 4, 5, new Point(-700, -26), new Point(-400, -26)),
        MAP13(196010000, 21, 4, 1, new Point(100, 454), new Point(400, 454)),
        MAP14(197000000, 26, 4, 0, new Point(250, 132), new Point(550, 132)),
        MAP15(197010000, 26, 4, 2, new Point(-600, -78), new Point(-300, -78)),
        ;

        public int id;
        public int minLevel;
        public int maxLevel;
        public int portalId;
        public boolean facingLeft;
        public Point pos0;
        public Point pos1;

        private PokemonMap(int id, int minLevel, int offset, int portalId, Point pos0, Point pos1) {
            this.id = id;
            this.minLevel = minLevel;
            this.portalId = portalId;
            this.maxLevel = (minLevel + offset);
            this.pos0 = pos0;
            this.pos1 = pos1;
            this.facingLeft = false;
        }

        private PokemonMap(int id, int minLevel, int offset, int portalId, Point pos0, Point pos1, boolean facingLeft) {
            this.id = id;
            this.minLevel = minLevel;
            this.portalId = portalId;
            this.maxLevel = (minLevel + offset);
            this.pos0 = pos0;
            this.pos1 = pos1;
            this.facingLeft = facingLeft;
        }
    }
}
