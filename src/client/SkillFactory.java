package client;

import client.status.MonsterStatus;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.Randomizer;
import tools.StringUtil;
import tools.Triple;

public class SkillFactory {

    private static final MapleData delayData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Character.wz")).getData("00002000.img");
    private static final MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz")).getData("Skill.img");
    private static final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Skill.wz"));
    private static final Map<Integer, Skill> skills = new HashMap();
    private static final Map<String, Integer> delays = new HashMap();
    private static final Map<Integer, CraftingEntry> crafts = new HashMap();
    private static final Map<Integer, FamiliarEntry> familiars = new HashMap();
    private static final Map<Integer, List<Integer>> skillsByJob = new HashMap();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap();

    // @TODO 这里要处理
    public static void loadAllSkills(boolean reload) {
        if (reload) {
            skills.clear();
        }
        if (!skills.isEmpty()) {
            return;
        }

        MapleDataDirectoryEntry root = datasource.getRoot();
        int del = 0;
        for (MapleData delay : delayData) {
            if (!delay.getName().equals("info")) {
                delays.put(delay.getName(), del);
                del++;
            }

        }
        for (MapleDataFileEntry topDir : root.getFiles()) {
            if (topDir.getName().length() <= 9) {
                // 普通技能
                for (MapleData data : datasource.getData(topDir.getName())) {
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) {
                            if (data2 != null) {
                                int skillid = Integer.parseInt(data2.getName());
                                Skill skil = Skill.loadFromData(skillid, data2, delayData);
                                List job = (List) skillsByJob.get(Integer.valueOf(skillid / 10000));
                                if (job == null) {
                                    job = new ArrayList();
                                    skillsByJob.put(skillid / 10000, job);
                                }
                                job.add(skillid);
                                skil.setName(getName(skillid, stringData));
                                skills.put(skillid, skil);

                                MapleData summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    SummonSkillEntry sse = new SummonSkillEntry();
                                    sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                    sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                    sse.attackCount = (byte) MapleDataTool.getInt("attackCount", summon_data, 1);
                                    if (summon_data.getChildByPath("range/lt") != null) {
                                        MapleData ltd = summon_data.getChildByPath("range/lt");
                                        sse.lt = ((Point) ltd.getData());
                                        sse.rb = ((Point) summon_data.getChildByPath("range/rb").getData());
                                    } else {
                                        sse.lt = new Point(-100, -100);
                                        sse.rb = new Point(100, 100);
                                    }

                                    sse.delay = (MapleDataTool.getInt("effectAfter", summon_data, 0) + MapleDataTool.getInt("attackAfter", summon_data, 0));
                                    for (MapleData effect : summon_data) {
                                        if (effect.getChildren().size() > 0) {
                                            for (MapleData effectEntry : effect) {
                                                sse.delay += MapleDataTool.getIntConvert("delay", effectEntry, 0);
                                            }
                                        }
                                    }
                                    for (MapleData effect : data2.getChildByPath("summon/attack1")) {
                                        sse.delay += MapleDataTool.getIntConvert("delay", effect, 0);
                                    }
                                    SummonSkillInformation.put(skillid, sse);
                                }
                            }
                        }
                    }
                }
            } else if (topDir.getName().startsWith("Familiar")) {
                // 家族技能？
                for (MapleData data : datasource.getData(topDir.getName())) {
                    int skillid = Integer.parseInt(data.getName());
                    FamiliarEntry skil = new FamiliarEntry();
                    skil.prop = (byte) MapleDataTool.getInt("prop", data, 0);
                    skil.time = (byte) MapleDataTool.getInt("time", data, 0);
                    skil.attackCount = (byte) MapleDataTool.getInt("attackCount", data, 1);
                    skil.targetCount = (byte) MapleDataTool.getInt("targetCount", data, 1);
                    skil.speed = (byte) MapleDataTool.getInt("speed", data, 1);
                    skil.knockback = ((MapleDataTool.getInt("knockback", data, 0) > 0) || (MapleDataTool.getInt("attract", data, 0) > 0));
                    if (data.getChildByPath("lt") != null) {
                        skil.lt = ((Point) data.getChildByPath("lt").getData());
                        skil.rb = ((Point) data.getChildByPath("rb").getData());
                    }
                    if (MapleDataTool.getInt("stun", data, 0) > 0) {
                        skil.status.add(MonsterStatus.眩晕);
                    }

                    if (MapleDataTool.getInt("slow", data, 0) > 0) {
                        skil.status.add(MonsterStatus.速度);
                    }
                    familiars.put(skillid, skil);
                }
            } else if (topDir.getName().startsWith("Recipe")) {
                // 锻造技能？
                for (MapleData data : datasource.getData(topDir.getName())) {
                    int skillid = Integer.parseInt(data.getName());
                    CraftingEntry skil = new CraftingEntry(skillid, (byte) MapleDataTool.getInt("incFatigability", data, 0), (byte) MapleDataTool.getInt("reqSkillLevel", data, 0), (byte) MapleDataTool.getInt("incSkillProficiency", data, 0), MapleDataTool.getInt("needOpenItem", data, 0) > 0, MapleDataTool.getInt("period", data, 0));
                    for (MapleData d : data.getChildByPath("target")) {
                        skil.targetItems.add(new Triple(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0), MapleDataTool.getInt("probWeight", d, 0)));
                    }
                    for (MapleData d : data.getChildByPath("recipe")) {
                        skil.reqItems.put(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0));
                    }
                    crafts.put(skillid, skil);
                }
            }
        }
    }

    public static List<Integer> getSkillsByJob(int jobId) {
        return (List) skillsByJob.get(jobId);
    }

    public static String getSkillName(int id) {
        Skill skil = getSkill(id);
        if (skil != null) {
            return skil.getName();
        }
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }

        return null;
    }

    public static Integer getDelay(String id) {
        if (Delay.fromString(id) != null) {
            return Delay.fromString(id).i;
        }
        return delays.get(id);
    }

    private static String getName(int id, MapleData stringData) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return "";
    }

    public static SummonSkillEntry getSummonData(int skillid) {
        return (SummonSkillEntry) SummonSkillInformation.get(skillid);
    }

    public static Collection<Skill> getAllSkills() {
        Map<Integer, Skill> mapVK = new TreeMap<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer obj1, Integer obj2) {
                        Integer v1 = obj1;
                        Integer v2 = obj2;
                        int s = v1.compareTo(v2);
                        return s;
                    }
                }
        );

        Set col = skills.keySet();
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            Integer key = (Integer) iter.next();
            Skill value = (Skill) skills.get(key);
            mapVK.put(key, value);
        }
        return mapVK.values();
    }

    public static Skill getSkill(int skillid) {
        if (!skills.isEmpty()) {
            if ((skillid >= 92000000) && (skillid < 100000000) && (crafts.containsKey(skillid))) {
                return (Skill) crafts.get(skillid);
            }
            return (Skill) skills.get(skillid);
        }
        return null;
    }

    public static long getDefaultSExpiry(Skill skill) {
        if (skill == null) {
            return -1L;
        }
        return skill.isTimeLimited() ? System.currentTimeMillis() + 2592000000L : -1L;
    }

    public static CraftingEntry getCraft(int id) {
        if (!crafts.isEmpty()) {
            return (CraftingEntry) crafts.get(id);
        }
        return null;
    }

    public static FamiliarEntry getFamiliar(int id) {
        if (!familiars.isEmpty()) {
            return (FamiliarEntry) familiars.get(id);
        }
        return null;
    }

    public static enum Delay {

        walk1(0),
        walk2(1),
        stand1(2),
        stand2(3),
        alert(4),
        swingO1(5),
        swingO2(6),
        swingO3(7),
        swingOF(8),
        swingT1(9),
        swingT2(10),
        swingT3(11),
        swingTF(12),
        swingP1(13),
        swingP2(14),
        swingPF(15),
        stabO1(16),
        stabO2(17),
        stabOF(18),
        stabT1(19),
        stabT2(20),
        stabTF(21),
        swingD1(22),
        swingD2(23),
        stabD1(24),
        swingDb1(25),
        swingDb2(26),
        swingC1(27),
        swingC2(28),
        rushBoom(28),
        tripleBlow(25),
        quadBlow(26),
        deathBlow(27),
        finishBlow(28),
        finishAttack(29),
        finishAttack_link(30),
        finishAttack_link2(30),
        shoot1(31),
        shoot2(32),
        shootF(33),
        shootDb2(40),
        shotC1(41),
        dash(37),
        dash2(38),
        proneStab(41),
        prone(42),
        heal(43),
        fly(44),
        jump(45),
        sit(46),
        rope(47),
        dead(48),
        ladder(49),
        rain(50),
        alert2(52),
        alert3(53),
        alert4(54),
        alert5(55),
        alert6(56),
        alert7(57),
        ladder2(58),
        rope2(59),
        shoot6(60),
        magic1(61),
        magic2(62),
        magic3(63),
        magic5(64),
        magic6(65),
        explosion(65),
        burster1(66),
        burster2(67),
        savage(68),
        avenger(69),
        assaulter(70),
        prone2(71),
        assassination(72),
        assassinationS(73),
        tornadoDash(76),
        tornadoDashStop(76),
        tornadoRush(76),
        rush(77),
        rush2(78),
        brandish1(79),
        brandish2(80),
        braveSlash(81),
        braveslash1(81),
        braveslash2(81),
        braveslash3(81),
        braveslash4(81),
        darkImpale(97),
        sanctuary(82),
        meteor(83),
        paralyze(84),
        blizzard(85),
        genesis(86),
        blast(88),
        smokeshell(89),
        showdown(90),
        ninjastorm(91),
        chainlightning(92),
        holyshield(93),
        resurrection(94),
        somersault(95),
        straight(96),
        eburster(97),
        backspin(98),
        eorb(99),
        screw(100),
        doubleupper(101),
        dragonstrike(102),
        doublefire(103),
        triplefire(104),
        fake(105),
        airstrike(106),
        edrain(107),
        octopus(108),
        backstep(109),
        shot(110),
        rapidfire(110),
        fireburner(112),
        coolingeffect(113),
        fist(114),
        timeleap(115),
        homing(117),
        ghostwalk(118),
        ghoststand(119),
        ghostjump(120),
        ghostproneStab(121),
        ghostladder(122),
        ghostrope(123),
        ghostfly(124),
        ghostsit(125),
        cannon(126),
        torpedo(127),
        darksight(128),
        bamboo(129),
        pyramid(130),
        wave(131),
        blade(132),
        souldriver(133),
        firestrike(134),
        flamegear(135),
        stormbreak(136),
        vampire(137),
        swingT2PoleArm(139),
        swingP1PoleArm(140),
        swingP2PoleArm(141),
        doubleSwing(142),
        tripleSwing(143),
        fullSwingDouble(144),
        fullSwingTriple(145),
        overSwingDouble(146),
        overSwingTriple(147),
        rollingSpin(148),
        comboSmash(149),
        comboFenrir(150),
        comboTempest(151),
        finalCharge(152),
        finalBlow(154),
        finalToss(155),
        magicmissile(156),
        lightningBolt(157),
        dragonBreathe(158),
        breathe_prepare(159),
        dragonIceBreathe(160),
        icebreathe_prepare(161),
        blaze(162),
        fireCircle(163),
        illusion(164),
        magicFlare(165),
        elementalReset(166),
        magicRegistance(167),
        magicBooster(168),
        magicShield(169),
        recoveryAura(170),
        flameWheel(171),
        killingWing(172),
        OnixBlessing(173),
        Earthquake(174),
        soulStone(175),
        dragonThrust(176),
        ghostLettering(177),
        darkFog(178),
        slow(179),
        mapleHero(180),
        Awakening(181),
        flyingAssaulter(182),
        tripleStab(183),
        fatalBlow(184),
        slashStorm1(185),
        slashStorm2(186),
        bloodyStorm(187),
        flashBang(188),
        upperStab(189),
        bladeFury(190),
        chainPull(192),
        chainAttack(192),
        owlDead(193),
        monsterBombPrepare(195),
        monsterBombThrow(195),
        finalCut(196),
        finalCutPrepare(196),
        suddenRaid(198),
        fly2(199),
        fly2Move(200),
        fly2Skill(201),
        knockback(202),
        rbooster_pre(206),
        rbooster(206),
        rbooster_after(206),
        crossRoad(209),
        nemesis(210),
        tank(217),
        tank_laser(221),
        siege_pre(223),
        tank_siegepre(223),
        sonicBoom(226),
        darkLightning(228),
        darkChain(229),
        cyclone_pre(0),
        cyclone(0),
        glacialchain(247),
        flamethrower(233),
        flamethrower_pre(233),
        flamethrower2(234),
        flamethrower_pre2(234),
        gatlingshot(239),
        gatlingshot2(240),
        drillrush(241),
        earthslug(242),
        rpunch(243),
        clawCut(244),
        swallow(247),
        swallow_attack(247),
        swallow_loop(247),
        flashRain(249),
        OnixProtection(264),
        OnixWill(265),
        phantomBlow(266),
        comboJudgement(267),
        arrowRain(268),
        arrowEruption(269),
        iceStrike(270),
        swingT2Giant(273),
        cannonJump(295),
        swiftShot(296),
        giganticBackstep(298),
        mistEruption(299),
        cannonSmash(300),
        cannonSlam(301),
        flamesplash(302),
        noiseWave(306),
        superCannon(310),
        jShot(312),
        demonSlasher(313),
        bombExplosion(314),
        cannonSpike(315),
        speedDualShot(316),
        strikeDual(317),
        bluntSmash(319),
        crossPiercing(320),
        piercing(321),
        elfTornado(323),
        immolation(324),
        multiSniping(327),
        windEffect(328),
        elfrush(329),
        elfrush2(329),
        dealingRush(334),
        maxForce0(336),
        maxForce1(337),
        maxForce2(338),
        maxForce3(339),
        iceAttack1(274),
        iceAttack2(275),
        iceSmash(276),
        iceTempest(277),
        iceChop(278),
        icePanic(279),
        iceDoubleJump(280),
        shockwave(292),
        demolition(293),
        snatch(294),
        windspear(295),
        windshot(296);

        public int i;

        private Delay(int i) {
            this.i = i;
        }

        public static Delay fromString(String s) {
            for (Delay b : values()) {
                if (b.name().equalsIgnoreCase(s)) {
                    return b;
                }
            }
            return null;
        }
    }

    public static class FamiliarEntry {

        public byte prop;
        public byte time;
        public byte attackCount;
        public byte targetCount;
        public byte speed;
        public Point lt;
        public Point rb;
        public boolean knockback;
        public EnumSet<MonsterStatus> status = EnumSet.noneOf(MonsterStatus.class);

        public boolean makeChanceResult() {
            return (this.prop >= 100) || (Randomizer.nextInt(100) < this.prop);
        }
    }

    public static class CraftingEntry extends Skill {

        public boolean needOpenItem;
        public int period;
        public byte incFatigability;
        public byte reqSkillLevel;
        public byte incSkillProficiency;
        public List<Triple<Integer, Integer, Integer>> targetItems = new ArrayList();
        public Map<Integer, Integer> reqItems = new HashMap();

        public CraftingEntry(int id, byte incFatigability, byte reqSkillLevel, byte incSkillProficiency, boolean needOpenItem, int period) {
            super(id);
            this.incFatigability = incFatigability;
            this.reqSkillLevel = reqSkillLevel;
            this.incSkillProficiency = incSkillProficiency;
            this.needOpenItem = needOpenItem;
            this.period = period;
        }
    }
}
