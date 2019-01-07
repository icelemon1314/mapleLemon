package server;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleDisease;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ItemConstants;
import constants.SkillConstants;
import handling.channel.ChannelServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.apache.log4j.Logger;
import provider.MapleData;
import provider.MapleDataTool;
import provider.wz.MapleDataType;
import server.life.MapleMonster;
import server.maps.MapleDefender;
import server.maps.MapleDoor;
import server.maps.MapleExtractor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.skill.冒险家.*;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;
import tools.packet.BuffPacket;
import tools.packet.SkillPacket;

public class MapleStatEffect implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private static final Logger log = Logger.getLogger(MapleStatEffect.class);
    public Map<MapleStatInfo, Integer> info;
    private boolean overTime;
    private boolean skill;
    private boolean partyBuff;
    private boolean notRemoved;
    public ArrayList<Pair<MapleBuffStat, Integer>> statups;
    private ArrayList<Pair<Integer, Integer>> availableMap;
    private EnumMap<MonsterStatus, Integer> monsterStatus;
    private Point lt;
    private Point rb;
    private byte level;
    private List<MapleDisease> cureDebuffs;
    private List<Integer> petsCanConsume;
    private List<Integer> familiars;
    private List<Integer> randomPickup;
    private List<Triple<Integer, Integer, Integer>> rewardItem;
    private byte slotCount;
    private byte slotPerLine;
    private byte expR;
    private byte familiarTarget;
    private byte recipeUseCount;
    private byte recipeValidDay;
    private byte reqSkillLevel;
    private byte effectedOnAlly;
    private byte effectedOnEnemy;
    private byte type;
    private byte preventslip;
    private byte immortal;
    private byte bs;
    private short ignoreMob;
    private short mesoR;
    private short thaw;
    private short fatigueChange;
    private short lifeId;
    private short imhp;
    private short immp;
    private short inflation;
    private short useLevel;
    private short indiePdd;
    private short indieMdd;
    private short incPVPdamage;
    private short mobSkill;
    private short mobSkillLevel;
    private double hpR;
    private double mpR;
    private int sourceid;
    private int recipe;
    private int moveTo;
    private int moneyCon;
    private int morphId;
    private int expinc;
    private int exp;
    private int consumeOnPickup;
    private int charColor;
    private int interval;
    private int rewardMeso;
    private int totalprob;
    private int cosmetic;
    private int expBuff;
    private int itemup;
    private int mesoup;
    private int cashup;
    private int berserk;
    private int illusion;
    private int booster;
    private int berserk2;
    private int cp;
    private int nuffSkill;
    public boolean isfirst = true;
    public int times = 0;
    public int bufftime = 0;

    public MapleStatEffect() {
        this.partyBuff = true;

        this.morphId = 0;
    }

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime, int level, String variables, boolean notRemoved) {
        return loadFromData(source, skillid, true, overtime, level, variables, notRemoved);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, false, false, 1, null, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, Integer>> list, MapleBuffStat buffstat, Integer val) {
        if (val != 0) {
            list.add(new Pair(buffstat, val));
        }
    }

    private static int parseEval(String path, MapleData source, int def, String variables, int level) {
        if (variables == null) {
            return MapleDataTool.getIntConvert(path, source, def);
        }
        MapleData dd = source.getChildByPath(path);
        if (dd == null) {
            return def;
        }
        if (dd.getType() != MapleDataType.STRING) {
            return MapleDataTool.getIntConvert(path, source, def);
        }
        FileoutputUtil.log("到这里就囧了！");
        return 0;
        /*
        String dddd = MapleDataTool.getString(dd).replace(variables, String.valueOf(level));
        switch (dddd.substring(0, 1)) {
            case "-":
                if ((dddd.substring(1, 2).equals("u")) || (dddd.substring(1, 2).equals("d"))) {
                    dddd = "n(" + dddd.substring(1, dddd.length()) + ")";
                } else {
                    dddd = "n" + dddd.substring(1, dddd.length());
                }
                break;
            case "=":
                dddd = dddd.substring(1, dddd.length());
                break;
        }
        if (dddd.contains("y")) {
//            FileoutputUtil.log(dddd);
            dddd = dddd.replaceAll("y", "0");
        }
        int result = (int) new CaltechEval(dddd).evaluate();
        return result;
        */
    }

    // 读入技能等级信息
    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime, int level, String variables, boolean notRemoved) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.sourceid = sourceid;
        ret.skill = skill;
        ret.level = (byte) level;
        if (source == null) {
            return ret;
        }
        ret.info = new EnumMap(MapleStatInfo.class);
        // 这里效率有点低
        for (MapleStatInfo i : MapleStatInfo.values()) {
            if (i.isSpecial()) {
                ret.info.put(i, parseEval(i.name().substring(0, i.name().length() - 1), source, i.getDefault(), variables, level));
            } else {
                ret.info.put(i, parseEval(i.name(), source, i.getDefault(), variables, level));
            }
        }
        ret.hpR = (parseEval("hpR", source, 0, variables, level) / 100.0D);
        ret.mpR = (parseEval("mpR", source, 0, variables, level) / 100.0D);
        ret.ignoreMob = (short) parseEval("ignoreMobpdpR", source, 0, variables, level);
        ret.thaw = (short) parseEval("thaw", source, 0, variables, level);
        ret.interval = parseEval("interval", source, 0, variables, level);
        ret.expinc = parseEval("expinc", source, 0, variables, level);
        ret.exp = parseEval("exp", source, 0, variables, level);
        ret.morphId = parseEval("morph", source, 0, variables, level);
        ret.cp = parseEval("cp", source, 0, variables, level);
        ret.cosmetic = parseEval("cosmetic", source, 0, variables, level);
        ret.slotCount = (byte) parseEval("slotCount", source, 0, variables, level);
        ret.slotPerLine = (byte) parseEval("slotPerLine", source, 0, variables, level);
        ret.preventslip = (byte) parseEval("preventslip", source, 0, variables, level);
        ret.useLevel = (short) parseEval("useLevel", source, 0, variables, level);
        ret.nuffSkill = parseEval("nuffSkill", source, 0, variables, level);
        ret.familiarTarget = (byte) (parseEval("familiarPassiveSkillTarget", source, 0, variables, level) + 1);
        ret.immortal = (byte) parseEval("immortal", source, 0, variables, level);
        ret.type = (byte) parseEval("type", source, 0, variables, level);
        ret.bs = (byte) parseEval("bs", source, 0, variables, level);
        ret.indiePdd = (short) parseEval("indiePdd", source, 0, variables, level);
        ret.indieMdd = (short) parseEval("indieMdd", source, 0, variables, level);
        ret.expBuff = parseEval("expBuff", source, 0, variables, level);
        ret.cashup = parseEval("cashBuff", source, 0, variables, level);
        ret.itemup = parseEval("itemupbyitem", source, 0, variables, level);
        ret.mesoup = parseEval("mesoupbyitem", source, 0, variables, level);
        ret.berserk = parseEval("berserk", source, 0, variables, level);
        ret.berserk2 = parseEval("berserk2", source, 0, variables, level);
        ret.booster = parseEval("booster", source, 0, variables, level);
        ret.lifeId = (short) parseEval("lifeId", source, 0, variables, level);
        ret.inflation = (short) parseEval("inflation", source, 0, variables, level);
        ret.imhp = (short) parseEval("imhp", source, 0, variables, level);
        ret.immp = (short) parseEval("immp", source, 0, variables, level);
        ret.illusion = parseEval("illusion", source, 0, variables, level);
        ret.consumeOnPickup = parseEval("consumeOnPickup", source, 0, variables, level);
        if ((ret.consumeOnPickup == 1)
                && (parseEval("party", source, 0, variables, level) > 0)) {
            ret.consumeOnPickup = 2;
        }

        ret.recipe = parseEval("recipe", source, 0, variables, level);
        ret.recipeUseCount = (byte) parseEval("recipeUseCount", source, 0, variables, level);
        ret.recipeValidDay = (byte) parseEval("recipeValidDay", source, 0, variables, level);
        ret.reqSkillLevel = (byte) parseEval("reqSkillLevel", source, 0, variables, level);
        ret.effectedOnAlly = (byte) parseEval("effectedOnAlly", source, 0, variables, level);
        ret.effectedOnEnemy = (byte) parseEval("effectedOnEnemy", source, 0, variables, level);
        ret.incPVPdamage = (short) parseEval("incPVPDamage", source, 0, variables, level);
        ret.moneyCon = parseEval("moneyCon", source, 0, variables, level);
        ret.moveTo = parseEval("moveTo", source, -1, variables, level);

        ret.charColor = 0;
        String cColor = MapleDataTool.getString("charColor", source, null);
        if (cColor != null) {
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
        }
        List cure = new ArrayList(5);
        if (parseEval("poison", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.中毒);
        }
        if (parseEval("seal", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.封印);
        }
        if (parseEval("darkness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.黑暗);
        }
        if (parseEval("weakness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.虚弱);
        }
        if (parseEval("curse", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.诅咒);
        }
        ret.cureDebuffs = cure;
        ret.petsCanConsume = new ArrayList();
        for (int i = 0;; i++) {
            int dd = parseEval(String.valueOf(i), source, 0, variables, level);
            if (dd <= 0) {
                break;
            }
            ret.petsCanConsume.add(dd);
        }

        MapleData mdd = source.getChildByPath("0");
        if ((mdd != null) && (mdd.getChildren().size() > 0)) {
            ret.mobSkill = (short) parseEval("mobSkill", mdd, 0, variables, level);
            ret.mobSkillLevel = (short) parseEval("level", mdd, 0, variables, level);
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
        }
        MapleData pd = source.getChildByPath("randomPickup");
        if (pd != null) {
            ret.randomPickup = new ArrayList();
            for (MapleData p : pd) {
                ret.randomPickup.add(MapleDataTool.getInt(p));
            }
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = ((Point) ltd.getData());
            ret.rb = ((Point) source.getChildByPath("rb").getData());
        }
        MapleData ltc = source.getChildByPath("con");
        if (ltc != null) {
            ret.availableMap = new ArrayList();
            for (MapleData ltb : ltc) {
                ret.availableMap.add(new Pair(MapleDataTool.getInt("sMap", ltb, 0), MapleDataTool.getInt("eMap", ltb, 999999999)));
            }
        }
        MapleData ltb = source.getChildByPath("familiar");
        if (ltb != null) {
            ret.fatigueChange = (short) (parseEval("incFatigue", ltb, 0, variables, level) - parseEval("decFatigue", ltb, 0, variables, level));
            ret.familiarTarget = (byte) parseEval("target", ltb, 0, variables, level);
            MapleData lta = ltb.getChildByPath("targetList");
            if (lta != null) {
                ret.familiars = new ArrayList();
                for (MapleData ltz : lta) {
                    ret.familiars.add(MapleDataTool.getInt(ltz, 0));
                }
            }
        } else {
            ret.fatigueChange = 0;
        }
        int totalprob = 0;
        MapleData lta = source.getChildByPath("reward");
        if (lta != null) {
            ret.rewardMeso = parseEval("meso", lta, 0, variables, level);
            MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
                ret.rewardItem = new ArrayList();
                for (MapleData lty : ltz) {
                    ret.rewardItem.add(new Triple(MapleDataTool.getInt("id", lty, 0), MapleDataTool.getInt("count", lty, 0), MapleDataTool.getInt("prop", lty, 0)));
                    totalprob += MapleDataTool.getInt("prob", lty, 0);
                }
            }
        } else {
            ret.rewardMeso = 0;
        }
        ret.totalprob = totalprob;

        if (ret.skill) {
            int priceUnit = (ret.info.get(MapleStatInfo.priceUnit));
            if (priceUnit > 0) {
                int price = (ret.info.get(MapleStatInfo.price));
                int extendPrice = (ret.info.get(MapleStatInfo.extendPrice));
                ret.info.put(MapleStatInfo.price, price * priceUnit);
                ret.info.put(MapleStatInfo.extendPrice, extendPrice * priceUnit);
            }
            switch (sourceid) {
                case 1100002:
                case 1120013:
                case 1200002:
                case 1300002:
                case 2111007:
                case 2211007:
                case 2311007:
                case 3100001:
                case 3120008:
                case 3200001:
                    ret.info.put(MapleStatInfo.mobCount, Integer.valueOf(6));
                    break;
            }

            if (GameConstants.isNoDelaySkill(sourceid)) {
                ret.info.put(MapleStatInfo.mobCount, 6);
            }
        }
        if ((!ret.skill) && ((ret.info.get(MapleStatInfo.time)) > -1)) {
            ret.overTime = true;
        } else {
            ret.info.put(MapleStatInfo.time, (ret.info.get(MapleStatInfo.time)) * 1000);
            ret.info.put(MapleStatInfo.subTime, (ret.info.get(MapleStatInfo.subTime)) * 1000);
            ret.overTime = overTime || ret.isMorph() || ret.getSummonMovementType() != null || ret.is带BUFF技能();
            ret.notRemoved = notRemoved;
        }
        ret.monsterStatus = new EnumMap(MonsterStatus.class);
        ret.statups = new ArrayList();
        if (ret.overTime && ret.getSummonMovementType() == null) {
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.物理攻击力, ret.info.get(MapleStatInfo.pad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.增加物理防御, ret.info.get(MapleStatInfo.pdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.魔法攻击力, ret.info.get(MapleStatInfo.mad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.魔法防御力, ret.info.get(MapleStatInfo.mdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.命中率, ret.info.get(MapleStatInfo.acc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.回避率, ret.info.get(MapleStatInfo.eva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.移动速度, ret.info.get(MapleStatInfo.speed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.跳跃力, ret.info.get(MapleStatInfo.jump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.神圣之火_最大体力百分比, ret.info.get(MapleStatInfo.mhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.神圣之火_最大魔力百分比, ret.info.get(MapleStatInfo.mmpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.攻击加速, ret.booster);
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.GIANT_POTION, Integer.valueOf(ret.inflation));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.魔法攻击力增加, ret.info.get(MapleStatInfo.indieMad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.最大体力, Integer.valueOf(ret.imhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.最大魔力, Integer.valueOf(ret.immp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.最大体力, ret.info.get(MapleStatInfo.indieMhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.最大魔力, ret.info.get(MapleStatInfo.indieMmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.命中值增加, ret.info.get(MapleStatInfo.indieAcc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP减少无效, ret.info.get(MapleStatInfo.thaw));
        }
            if (ret.skill) {
            switch (sourceid) {
                // 战士
                case 战士.圣甲术:
                case 魔法师.魔法铠甲:
                    ret.statups.add(new Pair(MapleBuffStat.增加物理防御, ret.info.get(MapleStatInfo.pdd)));
                    break;
                case 剑客.快速剑:
                case 剑客.快速斧:
                case 准骑士.快速剑:
                case 准骑士.快速钝器:
                case 枪战士.快速枪:
                case 枪战士.快速矛:
                case 猎人.快速箭:
                case 弩弓手.快速弩:
                case 刺客.快速暗器:
                case 侠客.快速短刀:
                    ret.statups.add(new Pair(MapleBuffStat.攻击加速, ret.info.get(MapleStatInfo.x)));
                    break;
                case 剑客.愤怒之火:
                    ret.statups.add(new Pair(MapleBuffStat.攻击力增加, ret.info.get(MapleStatInfo.pad)));
                    ret.statups.add(new Pair(MapleBuffStat.增加物理防御, ret.info.get(MapleStatInfo.pdd)));
                    break;
                case 剑客.伤害反击:
                case 准骑士.伤害反击:
                    ret.statups.add(new Pair(MapleBuffStat.伤害反击,ret.info.get(MapleStatInfo.x)));
                    break;
                case 枪战士.极限防御:
                    ret.statups.add(new Pair(MapleBuffStat.增加物理防御, ret.info.get(MapleStatInfo.pdd)));
                    ret.statups.add(new Pair(MapleBuffStat.魔法防御力, ret.info.get(MapleStatInfo.mdd)));
                    break;
                case 枪战士.神圣之火:
                    ret.statups.add(new Pair(MapleBuffStat.神圣之火_最大体力百分比, ret.info.get(MapleStatInfo.x)));
                    ret.statups.add(new Pair(MapleBuffStat.神圣之火_最大魔力百分比, ret.info.get(MapleStatInfo.y)));
                    break;
                case 勇士.斗气集中:
                    ret.statups.add(new Pair(MapleBuffStat.斗气集中, ret.info.get(MapleStatInfo.x)));
                    break;
                case 勇士.黑暗之剑:
                case 勇士.黑暗之斧:
                    ret.monsterStatus.put(MonsterStatus.恐慌, ret.info.get(MapleStatInfo.prop));
                    break;
                case 勇士.气绝剑:
                case 勇士.气绝斧:
                case 勇士.虎咆哮:
                case 骑士.属性攻击:
                    ret.monsterStatus.put(MonsterStatus.眩晕, ret.info.get(MapleStatInfo.prop));
                    break;
                case 勇士.防御崩坏:
                    ret.monsterStatus.put(MonsterStatus.物理防御无效, ret.info.get(MapleStatInfo.prop));
                    break;
                case 骑士.魔击无效:
                    ret.monsterStatus.put(MonsterStatus.魔法防御无效, ret.info.get(MapleStatInfo.prop));
                    break;
                case 龙骑士.龙之魂:
                    ret.statups.add(new Pair(MapleBuffStat.攻击力增加, ret.info.get(MapleStatInfo.pad)));
                    break;


                // 法师
                case 魔法师.魔法盾:
                    ret.statups.add(new Pair(MapleBuffStat.魔法盾, ret.info.get(MapleStatInfo.x)));
                    break;
                case 火毒魔法师.精神力:
                case 冰雷魔法师.精神力:
                    ret.statups.add(new Pair(MapleBuffStat.魔法攻击力, ret.info.get(MapleStatInfo.mad)));
                    break;
                case 火毒魔法师.缓速术:
                case 冰雷魔法师.缓速术:
                    ret.monsterStatus.put(MonsterStatus.速度, ret.info.get(MapleStatInfo.x));
                    break;
                case 火毒魔法师.毒雾术:
                case 火毒巫师.火毒合击:
                    ret.monsterStatus.put(MonsterStatus.中毒, ret.info.get(MapleStatInfo.mad));
                    break;
                case 牧师.神之保护:
                    ret.statups.add(new Pair(MapleBuffStat.神之保护, ret.info.get(MapleStatInfo.x)));
                    break;
                case 牧师.祝福:
                    ret.statups.add(new Pair(MapleBuffStat.牧师祝福, (int) ret.level));
                    break;
                case 火毒巫师.魔力激化:
                case 冰雷巫师.魔力激化:
                    ret.statups.add(new Pair(MapleBuffStat.魔法攻击力, ret.info.get(MapleStatInfo.y)));
                    break;
                case 火毒巫师.致命毒雾:
                    ret.monsterStatus.put(MonsterStatus.中毒, ret.info.get(MapleStatInfo.mad));
                    break;
                case 火毒巫师.封印术:
                case 冰雷巫师.封印术:
                    ret.monsterStatus.put(MonsterStatus.封印, ret.info.get(MapleStatInfo.prop));
                    break;
                case 火毒巫师.魔法狂暴:
                case 冰雷巫师.魔法狂暴:
                    ret.statups.add(new Pair(MapleBuffStat.攻击加速, ret.info.get(MapleStatInfo.x)));
                    break;
                case 冰雷魔法师.冰冻术:
                case 冰雷巫师.冰咆哮:
                case 冰雷巫师.冰雷合击:
                    ret.monsterStatus.put(MonsterStatus.结冰, 1);
                    break;
                case 祭司.时空门:
                    ret.statups.add(new Pair(MapleBuffStat.时空门, 1));
                    break;
                case 祭司.神圣祈祷:
                    ret.statups.add(new Pair(MapleBuffStat.神圣祈祷, ret.info.get(MapleStatInfo.x)));
                    break;
                case 祭司.巫毒术:
                    ret.monsterStatus.put(MonsterStatus.速度, -20);
                    ret.monsterStatus.put(MonsterStatus.物攻, 1);
                    ret.monsterStatus.put(MonsterStatus.魔攻, 1);
                    break;


                // 弓箭手
                case 弓箭手.集中术:
                    ret.statups.add(new Pair(MapleBuffStat.命中率,ret.info.get(MapleStatInfo.acc)));
                    ret.statups.add(new Pair(MapleBuffStat.回避率,ret.info.get(MapleStatInfo.eva)));
                    break;
                case 猎人.无形箭:
                case 弩弓手.无形箭:
                    ret.statups.add(new Pair(MapleBuffStat.无形箭弩, ret.info.get(MapleStatInfo.x)));
                    break;
                case 射手.疾风步:
                case 游侠.疾风步:
                    ret.statups.add(new Pair(MapleBuffStat.移动速度,ret.info.get(MapleStatInfo.speed)));
                    break;
                case 射手.替身术:
                case 游侠.替身术:
                    ret.statups.add(new Pair(MapleBuffStat.替身,ret.info.get(MapleStatInfo.x)));
                    break;
                case 射手.银鹰召唤:
                case 游侠.金鹰召唤:
                    ret.monsterStatus.put(MonsterStatus.眩晕, 1);
                    break;
                case 游侠.寒冰箭:
                    ret.monsterStatus.put(MonsterStatus.结冰, 1);
                    break;


                // 飞侠
                case 飞侠.诅咒术:
                case 准骑士.压制术:
                    ret.monsterStatus.put(MonsterStatus.物攻, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.物防, ret.info.get(MapleStatInfo.y));
                    break;
                case 飞侠.隐身术:
                    ret.statups.add(new Pair(MapleBuffStat.隐身术, ret.info.get(MapleStatInfo.x)));
                    ret.statups.add(new Pair(MapleBuffStat.移动速度, ret.info.get(MapleStatInfo.speed)));
                    break;
                case 刺客.轻功:
                case 侠客.轻功:
                    ret.statups.add(new Pair(MapleBuffStat.移动速度, ret.info.get(MapleStatInfo.speed)));
                    ret.statups.add(new Pair(MapleBuffStat.跳跃力, ret.info.get(MapleStatInfo.jump)));
                    break;
                case 无影人.聚财术:
                case 独行客.敛财术:
                    ret.statups.add(new Pair(MapleBuffStat.敛财术, ret.info.get(MapleStatInfo.x)));
                    break;
                case 无影人.影分身:
                case 独行客.分身术:
                    ret.statups.add(new Pair(MapleBuffStat.影分身, ret.info.get(MapleStatInfo.x)));
                    break;
                case 无影人.影网术:
                    ret.monsterStatus.put(MonsterStatus.影网, 1);
                    break;
                case 独行客.落叶斩:
                    ret.monsterStatus.put(MonsterStatus.眩晕, 1);
                    break;
                case 独行客.金钱护盾:
                    ret.statups.add(new Pair(MapleBuffStat.金钱护盾, ret.info.get(MapleStatInfo.x)));
                    break;
            }

        } else {
            // 非skill
            switch (sourceid) {
                case 2022746:
                case 2022747:
                case 2022823:
                case 2023189:
                case 2023150:
                case 2023148:
                case 2023149:
                    ret.statups.clear();
                    int value = sourceid == 2022823 ? 12 : sourceid == 2022747 ? 10 : sourceid == 2022746 ? 5 : sourceid == 2023189 ? 16 : sourceid == 2023150 ? 15 : sourceid == 2023148 ? 6 : sourceid == 20231489 ? 13 : 0;
                    if (value <= 0) {
                        break;
                    }
                    ret.statups.add(new Pair(MapleBuffStat.攻击力增加, value));
                    ret.statups.add(new Pair(MapleBuffStat.魔法攻击力增加, value));
                    if (sourceid == 2023150 || sourceid == 2023149 || sourceid == 2023148) {
                        ret.statups.add(new Pair(MapleBuffStat.最大体力, sourceid == 2023150 ? 20 : sourceid == 2023149 ? 10 : 5));
                        ret.statups.add(new Pair(MapleBuffStat.最大魔力, sourceid == 2023150 ? 20 : sourceid == 2023149 ? 10 : 5));
                    }
                    break;
            }
        }

        if (ret.isPoison()) {
            ret.monsterStatus.put(MonsterStatus.中毒, 1);
        }
        if (ret.getSummonMovementType() != null) {
            ret.statups.add(new Pair(MapleBuffStat.召唤兽, 1));
        }
        ret.statups.trimToSize();
        return ret;
    }

    public void applyPassive(MapleCharacter applyto, MapleMapObject obj) {
        if (makeChanceResult()) {
            switch (this.sourceid) {
                case 2100000:
                case 2200000:
                case 2300000:
                    if ((obj == null) || (obj.getType() != MapleMapObjectType.MONSTER)) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj;
                    if (mob.getStats().isBoss()) {
                        break;
                    }
                    int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0D)), mob.getMp());
                    if (absorbMp <= 0) {
                        break;
                    }
                    mob.setMp(mob.getMp() - absorbMp);
                    applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp);
                    applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(this.sourceid, 1, applyto.getLevel(), this.level));
                    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto, this.sourceid, 1, applyto.getLevel(), this.level), false);
            }
        }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, getDuration(chr), false);
    }

    public boolean applyTo(MapleCharacter chr, boolean passive) {
        return applyTo(chr, chr, true, null, getDuration(chr), passive);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, getDuration(chr), false);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int newDuration) {
        return applyTo(applyfrom, applyto, primary, pos, newDuration, false);
    }

    /**
     * 处理技能的效果值
     * @param applyfrom
     * @param applyto
     * @param primary
     * @param pos
     * @param newDuration
     * @param passive
     * @return
     */
    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int newDuration, boolean passive) {
        //TODO applyTo BUFF 写法不太理想 需要重新写算法
        if ((!applyfrom.isAdmin()) && (applyfrom.getMap().isMarketMap())) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        }
        if ((this.sourceid == 4341006) && (applyfrom.getBuffedValue(MapleBuffStat.影分身) == null)) {
            applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
            return false;
        }

        int hpchange = calcHPChange(applyfrom, primary); // 计算消耗的值
        int mpchange = calcMPChange(applyfrom, primary);
        PlayerStats stat = applyto.getStat();
        if (primary) {
            if (((this.info.get(MapleStatInfo.itemConNo)) != 0) ) {
                if (!applyto.haveItem((this.info.get(MapleStatInfo.itemCon)), (this.info.get(MapleStatInfo.itemConNo)), false, true)) {
                    applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                    return false;
                }
                MapleInventoryManipulator.removeById(applyto.getClient(), ItemConstants.getInventoryType((this.info.get(MapleStatInfo.itemCon))), (this.info.get(MapleStatInfo.itemCon)), (this.info.get(MapleStatInfo.itemConNo)), false, true);
            }
        } else if ((!primary) && (is复活术())) {
            hpchange = stat.getMaxHp();
            applyto.setStance(0);
        }
        if ((is净化()) && (makeChanceResult())) {
            if (applyto.dispelDebuffs()) {
                if (sourceid == 祭司.净化 && applyfrom.getParty() != null) {
                    applyfrom.getParty().givePartyBuff(祭司.净化, applyfrom.getId(), applyto.getId());
                } else {
//                    applyfrom.减少冷却时间(祭司.神圣保护, 60);
                }
            }
        }else if (this.cureDebuffs.size() > 0) {
            for (MapleDisease debuff : this.cureDebuffs) {
                applyfrom.dispelDebuff(debuff);
            }
        } else if (is龙之献祭()) {
//            applyto.dispelSkill(1301013);
            applyto.cancelEffectFromBuffStat(MapleBuffStat.灵魂助力);
            if (applyto.skillisCooling(1321013)) {
                applyto.removeCooldown(1321013);
                applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(1321013, 0));
            }
        }
        Map hpmpupdate = new EnumMap(MapleStat.class);
        if (hpchange != 0) {
            if ((hpchange < 0) && (-hpchange > stat.getHp())) {
                applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                return false;
            }
            stat.setHp(stat.getHp() + hpchange);
        }
        if (mpchange != 0 && !isNotuseMp()) {
            if ((mpchange < 0) && (-mpchange > stat.getMp())) {
                applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                return false;
            }
            stat.setMp(stat.getMp() + mpchange);
            hpmpupdate.put(MapleStat.MP, (long) stat.getMp());
        }
        // 消耗的处理完毕
        hpmpupdate.put(MapleStat.HP, (long) stat.getHp());
        applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, applyto));
        if ((this.useLevel > 0) && (!this.skill)) {
            applyto.setExtractor(new MapleExtractor(applyto, this.sourceid, this.useLevel * 50, 1440));
            applyto.getMap().spawnExtractor(applyto.getExtractor());
        } else {
            int i;
            if (is迷雾爆发()) {
                i = (this.info.get(MapleStatInfo.y));
                for (MapleDefender mist : applyto.getMap().getAllMistsThreadsafe()) {
                    if ((mist.getOwnerId() == applyto.getId()) && (mist.getSourceSkill().getId() == 2111003)) {
                        if (mist.getSchedule() != null) {
                            mist.getSchedule().cancel(false);
                            mist.setSchedule(null);
                        }
                        if (mist.getPoisonSchedule() != null) {
                            mist.getPoisonSchedule().cancel(false);
                            mist.setPoisonSchedule(null);
                        }
                        applyto.getMap().broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId(), true));
                        applyto.getMap().removeMapObject(mist);
                        i--;
                        if (i <= 0) {
                            break;
                        }
                    }
                }
            } else if (this.cosmetic > 0) {
                if (this.cosmetic >= 30000) {
                    applyto.setHair(this.cosmetic);
                    applyto.updateSingleStat(MapleStat.发型, this.cosmetic);
                } else if (this.cosmetic >= 20000) {
                    applyto.setFace(this.cosmetic);
                    applyto.updateSingleStat(MapleStat.脸型, this.cosmetic);
                } else if (this.cosmetic < 100) {
                    applyto.setSkinColor((byte) this.cosmetic);
                    applyto.updateSingleStat(MapleStat.皮肤, this.cosmetic);
                }
                applyto.equipChanged();
            } else if (this.bs > 0) {
                int xx = Integer.parseInt(applyto.getEventInstance().getProperty(String.valueOf(applyto.getId())));
                applyto.getEventInstance().setProperty(String.valueOf(applyto.getId()), String.valueOf(xx + this.bs));
                applyto.getClient().getSession().write(MaplePacketCreator.getPVPScore(xx + this.bs, false));
            } else if ((this.info.get(MapleStatInfo.iceGageCon)) > 0) {
                int x = Integer.parseInt(applyto.getEventInstance().getProperty("icegage"));
                if (x < (this.info.get(MapleStatInfo.iceGageCon))) {
                    return false;
                }
                applyto.getEventInstance().setProperty("icegage", String.valueOf(x - (this.info.get(MapleStatInfo.iceGageCon))));
                applyto.getClient().getSession().write(MaplePacketCreator.getPVPIceGage(x - (this.info.get(MapleStatInfo.iceGageCon))));
                applyto.applyIceGage(x - (this.info.get(MapleStatInfo.iceGageCon)));
            } else if (this.recipe > 0) {
                if ((applyto.getSkillLevel(this.recipe) > 0)) {
                    return false;
                }
                applyto.changeSingleSkillLevel(SkillFactory.getCraft(this.recipe), 2147483647, this.recipeUseCount, this.recipeValidDay > 0 ? System.currentTimeMillis() + this.recipeValidDay * 24L * 60L * 60L * 1000L : -1L);
            }else if (is暗器伤人()) {
                MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
                boolean itemz = false;
                int bulletConsume = (this.info.get(MapleStatInfo.bulletConsume));
                for (i = 0; i < use.getSlotLimit(); i++) {
                    Item item = use.getItem((short) (byte) i);
                    if ((item == null)
                            || (!ItemConstants.is飞镖道具(item.getItemId())) || (item.getQuantity() < bulletConsume)) {
                        continue;
                    }
                    MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) bulletConsume, false, true);
                    itemz = true;
                    break;
                }

                if (!itemz) {
                    return false;
                }
            } else if (is神圣之火()){
                int localst = applyto.getStat().getMaxHp();
                int maxst = localst + ((localst*this.info.get(MapleStatInfo.x))/100);
                applyto.getStat().setCurrentMaxHp(maxst);

                localst = applyto.getStat().getMaxMp();
                maxst = localst + ((localst*this.info.get(MapleStatInfo.y))/100);
                applyto.getStat().setCurrentMaxMp(maxst);

            } else {
                MapleCarnivalFactory.MCSkill skil;
                MapleDisease dis;
                if ((this.nuffSkill != 0) && (applyto.getParty() != null)) {
                    skil = MapleCarnivalFactory.getInstance().getSkill(this.nuffSkill);
                    if (skil != null) {
                        dis = skil.getDisease();
                        for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                            if (((applyto.getParty() == null) || (chr.getParty() == null) || (chr.getParty().getId() != applyto.getParty().getId())) && ((skil.targetsAll) || (Randomizer.nextBoolean()))) {
                                if (dis == null) {
                                    chr.dispel();
                                } else if (skil.getSkill() == null) {
                                    chr.giveDebuff(dis, 1, 30000L, dis.getDisease(), 1);
                                } else {
                                    chr.giveDebuff(dis, skil.getSkill());
                                }
                                if (!skil.targetsAll) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    int types;
                    if (((this.effectedOnEnemy > 0) || (this.effectedOnAlly > 0)) && (primary) ) {
                        types = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
                        if ((types > 0) || (this.effectedOnEnemy > 0)) {
                            for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                                if ((chr.getId() != applyto.getId()) && (this.effectedOnAlly > 0 ? chr.getTeam() != applyto.getTeam() : (chr.getTeam() != applyto.getTeam()) || (types == 0))) {
                                    applyTo(applyto, chr, false, pos, newDuration);
                                }
                            }
                        }
                    } else if ((this.randomPickup != null) && (this.randomPickup.size() > 0)) {
                        MapleItemInformationProvider.getInstance().getItemEffect((this.randomPickup.get(Randomizer.nextInt(this.randomPickup.size())))).applyTo(applyto);
                    }
                }
            }
        }
        if ((primary) && (this.availableMap != null)) {
            for (Pair e : this.availableMap) {
                if ((applyto.getMapId() < ((Integer) e.left)) || (applyto.getMapId() > ((Integer) e.right))) {
                    applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
                    return true;
                }
            }
        }

        if ((this.overTime)) {
            if ((getSummonMovementType() != null)) {
                applySummonEffect(applyfrom, primary, pos, newDuration, 0);
            } else {
                applyBuffEffect(applyfrom, applyto, primary, newDuration, passive);
            }
        }
        if (this.skill) {
            //removeMonsterBuff(applyfrom);
        }
        if (primary) {
            if (((this.overTime) || (is群体治愈()))) {
                applyBuff(applyfrom, newDuration); // 大部分走了这里
            }
            if (isMonsterBuff()) {
                applyMonsterBuff(applyfrom);
            }
        }
        if (is时空门()) { //召唤时空门 
            MapleDoor door = new MapleDoor(applyto, new Point(applyto.getTruePosition()), sourceid); // Current Map door
            if (door.getTownPortal() != null) {
                applyto.getMap().spawnDoor(door);
                applyto.addDoor(door);
                MapleDoor townDoor = new MapleDoor(door);
                applyto.addDoor(townDoor);
                door.getTown().spawnDoor(townDoor);
                door.first = false;
                if (applyto.getParty() != null) {
                    applyto.silentPartyUpdate();
                }
            } else {
                applyto.dropMessage(5, "村庄里已经没有可创造时空门的位置。");
            }
        } else if (isMist()) { // 迷雾类技能
            int addx = 0;
            Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft(), addx);
            MapleDefender mist = new MapleDefender(bounds, applyfrom, this);
            if (getCooldown(applyfrom) > 0) {
                applyfrom.getClient().getSession().write(MaplePacketCreator.skillCooldown(this.sourceid, getCooldown(applyfrom)));
                applyfrom.addCooldown(this.sourceid, System.currentTimeMillis(), getCooldown(applyfrom) * 1000);
            }
            applyfrom.getMap().spawnMist(mist, getDuration(), false);
        } else if (is伺机待发()) {
            for (MapleCoolDownValueHolder i : applyto.getCooldowns()) {
                if (i.skillId != 5121010) {
                    applyto.removeCooldown(i.skillId);
                    applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
                }
            }
        }
        if ((this.fatigueChange != 0) && (applyto.getSummonedFamiliar() != null) && ((this.familiars == null) || (this.familiars.contains(applyto.getSummonedFamiliar().getFamiliar())))) {
            applyto.getSummonedFamiliar().addFatigue(applyto, this.fatigueChange);
        }
        if (this.rewardMeso != 0) {
            applyto.gainMeso(this.rewardMeso, false);
        }
        if ((this.rewardItem != null) && (this.totalprob > 0)) {
            for (Triple reward : this.rewardItem) {
                if ((MapleInventoryManipulator.checkSpace(applyto.getClient(), ((Integer) reward.left), ((Integer) reward.mid), "")) && (((Integer) reward.right) > 0) && (Randomizer.nextInt(this.totalprob) < ((Integer) reward.right))) {
                    if (ItemConstants.getInventoryType(((Integer) reward.left)) == MapleInventoryType.EQUIP) {
                        Item item = MapleItemInformationProvider.getInstance().getEquipById(((Integer) reward.left).intValue());
                        item.setGMLog("Reward item (effect): " + this.sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.addbyItem(applyto.getClient(), item);
                    } else {
                        MapleInventoryManipulator.addById(applyto.getClient(), ((Integer) reward.left), ((Integer) reward.mid).shortValue(), "Reward item (effect): " + this.sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                }
            }
        }
        if ((this.familiarTarget == 2) && (applyfrom.getParty() != null) && (primary)) {
            for (MaplePartyCharacter mpc : applyfrom.getParty().getMembers()) {
                if ((mpc.getId() != applyfrom.getId()) && (mpc.getChannel() == applyfrom.getClient().getChannel()) && (mpc.getMapid() == applyfrom.getMapId()) && (mpc.isOnline())) {
                    MapleCharacter player = applyfrom.getMap().getCharacterById(mpc.getId());
                    if (player != null) {
                        applyTo(applyfrom, player, false, null, newDuration);
                    }
                }
            }
        } else if ((this.familiarTarget == 3) && (primary)) {
            for (MapleCharacter player : applyfrom.getMap().getCharactersThreadsafe()) {
                if (player.getId() != applyfrom.getId()) {
                    applyTo(applyfrom, player, false, null, newDuration);
                }
            }
        }
        return true;
    }

    /**
     * 处理召唤兽效果
     *
     * @param applyto 角色
     * @param primary
     * @param pos 坐标
     * @param newDuration 时间
     * @param monid 怪物的ID
     * @return 布尔值
     */
    public boolean applySummonEffect(MapleCharacter applyto, boolean primary, Point pos, int newDuration, int monid) {
        SummonMovementType summonMovementType = getSummonMovementType();
        if ((summonMovementType == null) || ((this.sourceid == 32111006) )) {
            return false;
        }
        byte[] buff = null;
        int summonSkillId = this.sourceid;
        int localDuration = newDuration;
        List localstatups = this.statups;
        if (applyto.isShowPacket()) {
            applyto.dropSpouseMessage(10, "开始召唤召唤兽 - 召唤兽技能: " + summonSkillId + " 持续时间: " + newDuration);
        }
        if (this.sourceid != 35111002) {
            applyto.cancelEffect(this, true, -1L, localstatups);
        }

        // 召唤兽处理
        MapleSummon tosummon = new MapleSummon(applyto, summonSkillId, getLevel(), new Point(pos == null ? applyto.getTruePosition() : pos), summonMovementType);
        tosummon.setLinkmonid(monid);
        applyto.getMap().spawnSummon(tosummon);
        applyto.addSummon(tosummon);
        if ((this.info.get(MapleStatInfo.hcSummonHp)) > 0) {
            tosummon.setSummonHp((this.info.get(MapleStatInfo.hcSummonHp)));
        } else if (this.sourceid == 3221014) {
            tosummon.setSummonHp((this.info.get(MapleStatInfo.x)));
        }

        if (this.sourceid == 4341006) {
            applyto.cancelEffectFromBuffStat(MapleBuffStat.影分身);
        } else if (this.sourceid == 1301013) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.灵魂助力, Integer.valueOf(level)));
            buff = BuffPacket.giveBuff(this.sourceid, localDuration, stat);
        }

        long startTime = System.currentTimeMillis();
        if (localDuration > 0) {
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, startTime, localstatups);
            ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, startTime, schedule, localstatups, false, localDuration, applyto.getId());
        }

        int cooldown = getCooldown(applyto);
        if (cooldown > 0) {
            applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(this.sourceid, cooldown));
            applyto.addCooldown(this.sourceid, startTime, cooldown * 1000);
        }
        if (buff != null) {
            applyto.getClient().getSession().write(buff);
        }
        return true;
    }

    public boolean applyReturnScroll(MapleCharacter applyto) {
        if (this.moveTo != -1) {
            if ((this.sourceid != 2031010) || (this.sourceid != 2030021)) {
                MapleMap target = null;
                boolean nearest = false;
                if (this.moveTo == 999999999) {
                    nearest = true;
                    if (applyto.getMap().getReturnMapId() != 999999999) {
                        target = applyto.getMap().getReturnMap();
                    }
                } else {
                    target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(this.moveTo);
                    if ((target.getId() == 931050500) && (target != applyto.getMap())) {
                        applyto.changeMap(target, target.getPortal(0));
                        return true;
                    }
                    int targetMapId = target.getId() / 10000000;
                    int charMapId = applyto.getMapId() / 10000000;
                    if ((targetMapId != 60) && (charMapId != 61)
                            && (targetMapId != 21) && (charMapId != 20)
                            && (targetMapId != 12) && (charMapId != 10)
                            && (targetMapId != 10) && (charMapId != 12)
                            && (targetMapId != charMapId)) {
                        FileoutputUtil.log("玩家 " + applyto.getName() + " 尝试回到一个非法的位置 (" + applyto.getMapId() + "->" + target.getId() + ")");
                        return false;
                    }

                }

                if ((target == applyto.getMap()) || ((nearest) && (applyto.getMap().isTown()))) {
                    return false;
                }
                applyto.changeMap(target, target.getPortal(0));
                return true;
            }
        }
        return false;
    }

    private void applyBuff(MapleCharacter applyfrom, int newDuration) {
        List<MapleCharacter> awarded = new ArrayList();
//        if ((isPartyBuff()) && ((applyfrom.getParty() != null) || (isGmBuff()))) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.PLAYER}));
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if ((affected.getId() != applyfrom.getId()) && ((isGmBuff()) || ((affected.getTeam() == applyfrom.getTeam()) && (Integer.parseInt(applyfrom.getEventInstance().getProperty("type")) != 0)) || ((applyfrom.getParty() != null) && (affected.getParty() != null) && (applyfrom.getParty().getId() == affected.getParty().getId())))) {
                    awarded.add(affected);
                }
            }
//        }
        for (MapleCharacter chr : awarded) {
            if (isPartyBuff() && chr.getParty() != null && !is群体治愈()) {
                chr.getParty().givePartyBuff(this.sourceid, applyfrom.getId(), chr.getId());
            }
            if (((is复活术()) && (!chr.isAlive())) || ((!is复活术()) && (chr.isAlive()))) {
                applyTo(applyfrom, chr, false, null, newDuration);
                chr.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(this.sourceid, 3/*, applyfrom.getLevel()*/, this.level));//2+1 119
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr, this.sourceid, 3, applyfrom.getLevel(), this.level), false);//2+1 119
            }
            if (is伺机待发()) {
                for (MapleCoolDownValueHolder i : chr.getCooldowns()) {
                    if (i.skillId != 5121010) {
                        chr.removeCooldown(i.skillId);
                        chr.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
                    }
                }
            }
        }
        if (is净化()) {
            if (applyfrom.getParty() == null) {
                return;
            }
            int time = applyfrom.getParty().getPartyBuffs(applyfrom.getId()) * 60;
            if (time > 0) {
//                applyfrom.减少冷却时间(祭司.神圣保护, time);
            }
            for (MaplePartyCharacter mc : applyfrom.getParty().getMembers()) {
                applyfrom.getParty().cancelPartyBuff(sourceid, mc.getId());
            }
        } else if (isPartyBuff() && !is群体治愈() && !is复活术()) {
            if (applyfrom.getParty() != null) {
                applyfrom.getParty().givePartyBuff(sourceid, applyfrom.getId(), applyfrom.getId());
            }
//            MapleStatEffect.apply祈祷众生(applyfrom);
        }
    }

    public static void applyDoubleDefense(final MapleCharacter applyfrom) {//双重防御
        int skilllevel = applyfrom.getSkillLevel(36111003);
        Skill skills = SkillFactory.getSkill(36111003);
        MapleStatEffect infoEffect = skills.getEffect(skilllevel);
        int i = 0;
        infoEffect.times = infoEffect.getX() - (infoEffect.getProb() - infoEffect.getStatups().get(0).right) / infoEffect.getY();
        if (infoEffect.times <= 0) {
            infoEffect.isfirst = true;
            applyfrom.cancelEffect(skills.getEffect(1), false, -1L);
            //applyfrom.cancelEffect(infoEffect, false, -1L);
        } else {
            infoEffect.isfirst = false;
            infoEffect.applyBuffEffect(applyfrom, 2100000000);
        }
    }

    private void removeMonsterBuff(MapleCharacter applyfrom) {
        List<MonsterStatus> cancel = new ArrayList();
        switch (this.sourceid) {
            case 1121016://魔击无效
            case 1221014:
            case 1321014:
            case 1211009:
            case 1111007:
            case 1311007:
//                cancel.add(MonsterStatus.物防提升);
//                cancel.add(MonsterStatus.魔防提升);
//                cancel.add(MonsterStatus.物攻提升);
//                cancel.add(MonsterStatus.魔攻提升);
                break;
            default:
                return;
        }
        Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.MONSTER}));
        int i = 0;
        for (MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (MonsterStatus stat : cancel) {
                    ((MapleMonster) mo).cancelStatus(stat);
                }
            }
            i++;
            if (i >= (this.info.get(MapleStatInfo.mobCount))) {
                break;
            }
        }
    }

    /**
     * 给怪物加BUFF
     * @param applyfrom
     */
    public void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        MapleMapObjectType types = MapleMapObjectType.MONSTER;
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{types}));
        int i = 0;
        for (MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (Map.Entry stat : getMonsterStati().entrySet()) {
                    MapleMonster mons = (MapleMonster) mo;
                    mons.applyStatus(applyfrom, new MonsterStatusEffect((MonsterStatus) stat.getKey(), (Integer) stat.getValue(), this.sourceid, null, false), isPoison(), getDuration(), true, this);
                }
            }
            i++;
            if ((i >= (this.info.get(MapleStatInfo.mobCount))) && (this.sourceid != 35111005)) {
                break;
            }
        }
    }

    public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, (this.info.get(MapleStatInfo.range)));
    }

    public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int addedRange) {
        return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, (this.info.get(MapleStatInfo.range)) + addedRange);
    }

    public static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, Point lt, Point rb, int range) {
        if ((lt == null) || (rb == null)) {
            return new Rectangle((facingLeft ? -200 - range : 0) + posFrom.x, -100 - range + posFrom.y, 200 + range, 100 + range);
        }
        Point myrb;
        Point mylt;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public double getMaxDistanceSq() {
        int maxX = Math.max(Math.abs(this.lt == null ? 0 : this.lt.x), Math.abs(this.rb == null ? 0 : this.rb.x));
        int maxY = Math.max(Math.abs(this.lt == null ? 0 : this.lt.y), Math.abs(this.rb == null ? 0 : this.rb.y));
        return maxX * maxX + maxY * maxY;
    }

    public void setDuration(int d) {
        this.info.put(MapleStatInfo.time, d);
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime, int localDuration, List<Pair<MapleBuffStat, Integer>> statup, int chrId) {
        int maskedDuration = 0;
        int newDuration = (int) (starttime + localDuration - System.currentTimeMillis());
        if (is终极无限()) {
            maskedDuration = alchemistModifyVal(chr, 4000, false);
        }
        ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, statup), newDuration);
        chr.registerEffect(this, starttime, schedule, statup, true, localDuration, chrId);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            MapleSummon summon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType);
            if (!summon.is替身术()) {
                chr.getMap().spawnSummon(summon);
                chr.addSummon(summon);
                summon.addSummonHp((this.info.get(MapleStatInfo.x)).shortValue());
                if (is灵魂助力()) {
                    summon.addSummonHp(1);
                }
            }
        }
    }


    public void applyBuffEffect(MapleCharacter chr, int newDuration) {
        applyBuffEffect(chr, chr, false, newDuration, false);
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int newDuration) {
        applyBuffEffect(applyfrom, applyto, primary, newDuration, false);
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int newDuration, boolean passive) {
        //TODO 写法太不理想 造成严重的冗余
        if ((!applyto.isAdmin()) && (applyto.getMap().isMarketMap())) {
            applyto.getClient().getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int localDuration = newDuration;
        if (localDuration == 1000) { // 如果持续时间为1s,那么表示需要服务端自动取消buff
            newDuration = localDuration = 65535000;
        }

        this.bufftime = newDuration;
        if (primary) {
            localDuration = Math.max(newDuration, alchemistModifyVal(applyfrom, localDuration, false));
        }
        if (applyfrom.isShowPacket()) {
            applyfrom.dropSpouseMessage(10, "开始 => applyBuffEffect ID: " + this.sourceid + " 持续时间: " + localDuration);
        }
        List localstatups = statups;
        List maskedStatups = null;
        boolean normal = true;
        boolean showEffect = primary;
        int maskedDuration = 0;
        int rate = 1;
        byte[] buff = null;
        byte[] foreignbuff = null;
        if (is神圣之火()) {
            localstatups = new ArrayList();
            localstatups.add(new Pair(MapleBuffStat.神圣之火_最大体力百分比, (this.info.get(MapleStatInfo.x))));
            localstatups.add(new Pair(MapleBuffStat.神圣之火_最大魔力百分比, (this.info.get(MapleStatInfo.x))));
        } else if (is隐身术()) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.隐身术, 0));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if (this.sourceid == 1210016) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.祝福护甲, 0));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if (is斗气集中()) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.斗气集中, 0));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if ((this.sourceid == 3101004) || (this.sourceid == 3201004)) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.无形箭弩, 0));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
            /*} else if (this.sourceid == 2321005) {
             applyto.cancelEffectFromBuffStat(MapleBuffStat.牧师祝福);*/
        } else if (is影分身()) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.影分身, this.info.get(MapleStatInfo.x)));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if (this.sourceid == 1121010) {
            applyto.handleOrbconsume(1);
        }else if (isMorph()) {
            if (is冰骑士()) {
                List stat = Collections.singletonList(new Pair(MapleBuffStat.冰骑士, Integer.valueOf(2)));
                buff = BuffPacket.giveBuff(0, localDuration, stat);
            }
            List stat = Collections.singletonList(new Pair(MapleBuffStat.变身术, Integer.valueOf(getMorph(applyto))));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if (isInflation()) {
            List stat = Collections.singletonList(new Pair(MapleBuffStat.GIANT_POTION, Integer.valueOf(this.inflation)));
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), stat, this);
        } else if (is骑兽技能()) {
            localDuration = 2100000000;
            localstatups = new ArrayList(this.statups);
            int mountid = parseMountInfo(applyto, this.sourceid);
            int mountid2 = parseMountInfo_Pure(applyto, this.sourceid);
            if ((mountid != 0) && (mountid2 != 0)) {
//                localstatups.add(new Pair(MapleBuffStat.骑兽技能, mountid2));
//                List stat = Collections.singletonList(new Pair(MapleBuffStat.骑兽技能, 0));
//                foreignbuff = BuffPacket.showMonsterRiding(applyto.getId(), stat, mountid, this.sourceid);
            } else {
                if (applyto.isShowPacket()) {
                    applyto.dropSpouseMessage(10, "骑宠BUFF " + this.sourceid + " 错误，未找到这个骑宠的外形ID。");
                }
                return;
            }
        } else if (sourceid == 3110012) { //精神集中
            showEffect = true;
//            int healRate = applyto.getBuffedIntValue(MapleBuffStat.精神集中) + 1;
//            this.getStatups().set(0, new Pair(MapleBuffStat.精神集中, healRate));
            //List stat = Collections.singletonList(new Pair(MapleBuffStat.精神集中, applyto.getBuffedIntValue(MapleBuffStat.精神集中)));
            buff = BuffPacket.giveBuff(this.sourceid, localDuration, localstatups);
            foreignbuff = BuffPacket.giveForeignBuff(applyto.getId(), localstatups, this);
        }

        if (!is骑兽技能()) {
            //applyto.cancelEffect(this, true, -1L, localstatups);
        }

        if ((showEffect) && (!applyto.isHidden())) { // TODO 发送 [SHOW_FOREIGN_EFFECT]
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto, this.sourceid, 1, applyto.getLevel(), this.level), false);
        }
        if (buff != null) { // TODO 发送 [GIVE_BUFF]
            applyfrom.dropMessage(5,"发送技能：GIVE_BUFF");
            applyto.getClient().getSession().write(buff);
        } else if (normal && localstatups.size() > 0) { //自动使用？
            FileoutputUtil.log("发送技能：GIVE_BUFF|null"+sourceid);
            applyto.getClient().getSession().write(BuffPacket.giveBuff(skill ? sourceid : -sourceid, localDuration, maskedStatups == null ? localstatups : maskedStatups));
        }

        if ((foreignbuff != null) && (!applyto.isHidden())) { // TODO 发送 [GIVE_FOREIGN_BUFF]
            applyto.getMap().broadcastMessage(foreignbuff);
        }

        long startTime = System.currentTimeMillis();
        if (localDuration > 0) {
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, startTime, localstatups);
            ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, maskedDuration > 0 ? maskedDuration : localDuration);
            applyto.registerEffect(this, startTime, schedule, localstatups, true, maskedDuration > 0 ? maskedDuration : localDuration, applyfrom.getId());
        }

        int cooldown = getCooldown(applyto);
        if (cooldown > 0) {
            if (SkillConstants.is触发性冷却技能(this.sourceid)) {
                return;
            }
            if (!applyto.skillisCooling(this.sourceid)) {
                applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(this.sourceid, cooldown));
                applyto.addCooldown(this.sourceid, startTime, cooldown * 1000);
            }
        }
    }

    public static int parseMountInfo(MapleCharacter player, int skillid) {
        if ((skillid == 80001000) || (GameConstants.is骑兽技能(skillid))) {
            if ((player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -123) != null) && (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -124) != null)) {
                return player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -123).getItemId();
            }
            return parseMountInfo_Pure(player, skillid);
        }
        return GameConstants.getMountItem(skillid, player);
    }

    public static int parseMountInfo_Pure(MapleCharacter player, int skillid) {
        if ((skillid == 80001000) || (GameConstants.is骑兽技能(skillid))) {
            if ((player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) && (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -19) != null)) {
                return player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId();
            }
            return 0;
        }
        return GameConstants.getMountItem(skillid, player);
    }

    private int getHpMpChange(MapleCharacter applyfrom, boolean hpchange) {
        int change = 0;
        if ((this.hpR != 0.0D) || (this.mpR != 0.0D)) {
            double healHpRate = this.hpR;
            if (applyfrom.isShowPacket()) {
                applyfrom.dropMessage(-5, "HpMpChange => 默认: " + this.hpR + " - " + healHpRate);
            }
            int maxChange = this.mpR < 1.0D ? Math.min((int) Math.floor(GameConstants.MAX_HP/2), (int) Math.floor(GameConstants.MAX_HP * (hpchange ? healHpRate : this.mpR))) : GameConstants.MAX_HP;
            int current = applyfrom.getStat().getCurrentMaxHp();
            change = (int) (current * (hpchange ? healHpRate : this.mpR)) > maxChange ? maxChange : (int) (current * (hpchange ? healHpRate : this.mpR));
        }
        return change;
    }

    /**
     * 计算血量变化
     * @param applyfrom
     * @param primary
     * @return
     */
    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if ((this.info.get(MapleStatInfo.hp)) != 0) {
            if (!this.skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, (this.info.get(MapleStatInfo.hp)), true);
                } else {
                    hpchange += (this.info.get(MapleStatInfo.hp));
                }
            } else {
                hpchange += makeHealHP((this.info.get(MapleStatInfo.hp)) / 100.0D, applyfrom.getStat().getTotalMagic(), 3.0D, 5.0D);
            }
        }
        if (this.hpR != 0.0D) {
            hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * hpR);
        }

        if ((primary) && ((this.info.get(MapleStatInfo.hpCon)) != 0)) {
            hpchange -= (this.info.get(MapleStatInfo.hpCon));
        }

        if ((applyfrom.getTotalSkillLevel(21120043) > 0)) {
            hpchange = 0;
        }
        if (this.sourceid == 独行客.转化术) {
            hpchange += this.info.get(MapleStatInfo.y) * this.info.get(MapleStatInfo.mpCon);
        }
        return hpchange;
    }

    private static int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) (Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if ((this.info.get(MapleStatInfo.mp)) != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, (this.info.get(MapleStatInfo.mp)), true);
            } else {
                mpchange += (this.info.get(MapleStatInfo.mp));
            }
        }
        if (this.mpR != 0.0D) {
            mpchange += (int) (applyfrom.getStat().getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (((this.info.get(MapleStatInfo.mpCon)) != 0)) {
                boolean free = false;
                if ((applyfrom.getJob() == 411) || (applyfrom.getJob() == 412)) {
                    Skill expert = SkillFactory.getSkill(4110012);
                    if (applyfrom.getTotalSkillLevel(expert) > 0) {
                        MapleStatEffect eff = expert.getEffect(applyfrom.getTotalSkillLevel(expert));
                        if (eff.makeChanceResult()) {
                            free = true;
                        }
                    }
                }
               if (!free) {
                    mpchange = (int) (mpchange - ((this.info.get(MapleStatInfo.mpCon)) - (this.info.get(MapleStatInfo.mpCon)) * applyfrom.getStat().mpconReduce / 100) * (applyfrom.getStat().mpconPercent / 100.0D));
                }
            } else if (((this.info.get(MapleStatInfo.forceCon)) != 0)) {
                mpchange -= (this.info.get(MapleStatInfo.forceCon));
            }
        }
        if ((applyfrom.getTotalSkillLevel(21120043) > 0)) {
            mpchange = 0;
        }
        return mpchange;
    }

    public int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!this.skill) {
            return val * (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP) / 100;
        }
        return val * (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP_Skill + (getSummonMovementType() == null ? 0 : chr.getStat().BuffUP_Summon)) / 100 * chr.getStat().BuffTimeR / 100;
    }

    private int calcPowerChange(MapleCharacter applyfrom) {
        return 0;
    }

    public void setSourceId(int newid) {
        this.sourceid = newid;
    }

    public boolean isGmBuff() {
        switch (this.sourceid) {
            case 9001000:
            case 9001001:
            case 9001002:
            case 9001003:
            case 9001005:
            case 9001008:
            case 9101000:
            case 9101001:
            case 9101002:
            case 9101003:
            case 9101005:
            case 9101008:
            case 10001075:
                return true;
        }
        return (GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1005);
    }

    public boolean isInflation() {
        return this.inflation > 0;
    }

    public int getInflation() {
        return this.inflation;
    }

    /**
     * 是否对怪物的BUFF
     * @return
     */
    private boolean isMonsterBuff() {
        switch (this.sourceid) {
            case 准骑士.压制术:
            case 火毒魔法师.缓速术:
            case 冰雷魔法师.缓速术:
            case 冰雷巫师.封印术:
            case 火毒巫师.封印术:
            case 祭司.巫毒术:
            case 无影人.影网术:
                return this.skill;
        }
        return false;
    }

    public void setPartyBuff(boolean pb) {
        this.partyBuff = pb;
    }

    public boolean isPartyBuff() {
        switch (this.sourceid) { //TODO 设置非组队BUFF
            case 1201011:
            case 1201012:
            case 1211008:
            case 1221004:
            case 4341002:
            case 4341052:
            case 1221054:
            case 4111002:
                return false;
        }

        return !GameConstants.isNoDelaySkill(this.sourceid);
    }

    public boolean is神圣之火() {
        return (this.skill) && (this.sourceid == 1301007);
    }

    public boolean is不倦神酒() {
        return (this.skill) && (this.sourceid == 1301007);
    }

    public boolean is神秘瞄准术() {
        return (this.skill) && ((this.sourceid == 2320011) || (this.sourceid == 2220010) || (this.sourceid == 2120010));
    }

    public boolean is群体治愈() {
        return (this.skill) && ((this.sourceid == 牧师.群体治愈) || (this.sourceid == 9101000) || (this.sourceid == 9001000));
    }

    public boolean is复活术() {
        return (this.skill) && ((this.sourceid == 9001005) || (this.sourceid == 9101005) || (this.sourceid == 2321006));
    }

    public boolean is伺机待发() {
        return (this.skill) && (this.sourceid == 5121010);
    }

    public int getHp() {
        return (this.info.get(MapleStatInfo.hp));
    }

    public int getMp() {
        return (this.info.get(MapleStatInfo.mp));
    }

    public int getDOTStack() {
        return (this.info.get(MapleStatInfo.dotSuperpos));
    }

    public double getHpR() {
        return this.hpR;
    }

    public double getMpR() {
        return this.mpR;
    }

    public int getMastery() {
        return (this.info.get(MapleStatInfo.mastery));
    }

    public int getWatk() {
        return (this.info.get(MapleStatInfo.pad));
    }

    public int getMatk() {
        return (this.info.get(MapleStatInfo.mad));
    }

    public int getWdef() {
        return (this.info.get(MapleStatInfo.pdd));
    }

    public int getMdef() {
        return (this.info.get(MapleStatInfo.mdd));
    }

    public int getAcc() {
        return (this.info.get(MapleStatInfo.acc));
    }

    public int getAvoid() {
        return (this.info.get(MapleStatInfo.eva));
    }

    public int getSpeed() {
        return (this.info.get(MapleStatInfo.speed));
    }

    public int getJump() {
        return (this.info.get(MapleStatInfo.jump));
    }

    public int getSpeedMax() {
        return (this.info.get(MapleStatInfo.speedMax));
    }

    public int getPassiveSpeed() {
        return (this.info.get(MapleStatInfo.psdSpeed));
    }

    public int getPassiveJump() {
        return (this.info.get(MapleStatInfo.psdJump));
    }

    public int getDuration() {
        return (this.info.get(MapleStatInfo.time));
    }

    public int getDuration(MapleCharacter applyfrom) {
        int time = this.skill ? applyfrom.getStat().getDuration(this.sourceid) : 0;
        return (this.info.get(MapleStatInfo.time)) + time;
    }

    public int getSubTime() {
        return (this.info.get(MapleStatInfo.subTime));
    }

    public boolean isOverTime() {
        return this.overTime;
    }

    public boolean isNotRemoved() {
        return this.notRemoved;
    }

    public List<Pair<MapleBuffStat, Integer>> getStatups() {
        return this.statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return (effect != null) && (this.sourceid == effect.sourceid) && (this.skill == effect.skill);
    }

    public int getT() {
        return (this.info.get(MapleStatInfo.t));
    }

    public int getU() {
        return (this.info.get(MapleStatInfo.u));
    }

    public int getV() {
        return (this.info.get(MapleStatInfo.v));
    }

    public int getW() {
        return (this.info.get(MapleStatInfo.w));
    }

    public int getX() {
        return (this.info.get(MapleStatInfo.x));
    }

    public int getY() {
        return (this.info.get(MapleStatInfo.y));
    }

    public int getZ() {
        return (this.info.get(MapleStatInfo.z));
    }

    public int getDamage() {
        return (this.info.get(MapleStatInfo.damage));
    }

    public int getPVPDamage() {
        return (this.info.get(MapleStatInfo.PVPdamage));
    }

    public int getAttackCount() {
        return (this.info.get(MapleStatInfo.attackCount));
    }

    public int getAttackCount(MapleCharacter applyfrom) {
        int addcount = (applyfrom.getSkillLevel(3220015) > 0) && (getAttackCount() >= 2) ? 1 : 0;
        return (this.info.get(MapleStatInfo.attackCount)) + applyfrom.getStat().getAttackCount(this.sourceid) + addcount;
    }

    public int getBulletCount() {
        return (this.info.get(MapleStatInfo.bulletCount));
    }

    public int getBulletCount(MapleCharacter applyfrom) {
        int addcount = (applyfrom.getSkillLevel(3220015) > 0) && (getBulletCount() >= 2) ? 1 : 0;
        return (this.info.get(MapleStatInfo.bulletCount)) + applyfrom.getStat().getAttackCount(this.sourceid) + addcount;
    }

    public int getBulletConsume() {
        return (this.info.get(MapleStatInfo.bulletConsume));
    }

    public int getMobCount() {
        return (this.info.get(MapleStatInfo.mobCount));
    }

    public int getMobCount(MapleCharacter applyfrom) {
        return (this.info.get(MapleStatInfo.mobCount)) + applyfrom.getStat().getMobCount(this.sourceid);
    }

    public int getMoneyCon() {
        return this.moneyCon;
    }

    public int getCooltimeReduceR() {
        return (this.info.get(MapleStatInfo.coolTimeR));
    }

    public int getMesoAcquisition() {
        return (this.info.get(MapleStatInfo.mesoR));
    }

    public boolean isNoTCoolDown(MapleCharacter applyfrom) {//TODO 设置条件技能不冷却时间为
        if (is神枪降临() && applyfrom.getBuffSource(MapleBuffStat.百分比无视防御) == 1321015
                || ((this.sourceid == 1321013 || this.sourceid == 27121303))) {
            return true;
        }
        return false;
    }

    public int getCooldown(MapleCharacter applyfrom) {
        if (isNoTCoolDown(applyfrom)) {
            return 0;
        }
        if ((this.info.get(MapleStatInfo.cooltime)) >= 5) {
            int cooldownX = (int) ((this.info.get(MapleStatInfo.cooltime)) * (applyfrom.getStat().getCoolTimeR() / 100.0D));
            int coolTimeR = (int) ((this.info.get(MapleStatInfo.cooltime)) * (applyfrom.getStat().getReduceCooltimeRate(this.sourceid) / 100.0D));
            if (applyfrom.isShowPacket()) {
                applyfrom.dropMessage(-5, "技能冷却时间 => 默认: " + this.info.get(MapleStatInfo.cooltime) + " [减少百分比: " + applyfrom.getStat().getCoolTimeR() + "% - " + cooldownX + "] [减少时间: " + applyfrom.getStat().getReduceCooltime() + "] [超级技能减少百分比: " + applyfrom.getStat().getReduceCooltimeRate(this.sourceid) + "% 减少时间: " + coolTimeR + "]");
            }
            return Math.max(0, (this.info.get(MapleStatInfo.cooltime)) - applyfrom.getStat().getReduceCooltime() - (cooldownX > 5 ? 5 : cooldownX) - coolTimeR);
        }
        return (this.info.get(MapleStatInfo.cooltime));
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return this.monsterStatus;
    }

    public int getBerserk() {
        return this.berserk;
    }

    public boolean is神枪降临() {
        return (this.skill) && (this.sourceid == 1321013);
    }

    public boolean is隐藏术() {
        return (this.skill) && ((this.sourceid == 9001004) || (this.sourceid == 9101004));
    }

    public boolean is隐身术() {
        return (this.skill) && ((this.sourceid == 9001004) || (this.sourceid == 9101004) || (this.sourceid == 4001003) || (this.sourceid == 14001003) || (this.sourceid == 4330001));
    }

    public boolean is龙之力() {
        return (this.skill) && (this.sourceid == 龙骑士.龙之魂);
    }

    public boolean is龙之献祭() {
        return (this.skill) && (this.sourceid == 1311005);
    }

    public boolean is团队治疗() {
        return (this.skill) && ((this.sourceid == 1001) || (this.sourceid == 10001001) || (this.sourceid == 20001001) || (this.sourceid == 20011001) || (this.sourceid == 35121005));
    }

    public boolean is重生契约() {
        return (this.skill) && (this.sourceid == 1320016 || this.sourceid == 1320019);
    }

    public boolean is灵魂助力() {
        return (this.skill) && (this.sourceid == 1301013);
    }

    public boolean is灵魂助力统治() {
        return (this.skill) && (this.sourceid == 1311013);
    }

    public boolean is能量激发() {
        return (this.skill) && (this.sourceid == 5121054);
    }

    public boolean is极限射箭() {
        return (this.skill) && ((this.sourceid == 3111011) || (this.sourceid == 3211012));
    }

    public boolean is终极无限() {
        return (this.skill) && ((this.sourceid == 2121004) || (this.sourceid == 2221004) || (this.sourceid == 2321004));
    }

    public boolean is骑兽技能_() {
        return (this.skill) && ((GameConstants.is骑兽技能(this.sourceid)) || (this.sourceid == 80001000) || this.getvehicleID() > 0);
    }

    public boolean is骑兽技能() {
        return (this.skill) && ((is骑兽技能_()) || (GameConstants.getMountItem(this.sourceid, null) != 0));
    }

    public boolean is时空门() {
        return (this.skill) && ((this.sourceid == 2311002) || (this.sourceid % 10000 == 8001));
    }

    public boolean is金钱护盾() {
        return (this.skill) && (this.sourceid == 4201011);
    }

    public boolean is影子闪避() {
        return (this.skill) && (this.sourceid == 4330009);
    }

    public boolean isCharge() {
        switch (this.sourceid) {
            case 1211008:
            case 12101005:
            case 21101006:
                return this.skill;
        }
        return false;
    }

    public boolean isPoison() {
        return ((this.info.get(MapleStatInfo.dot)) > 0) && ((this.info.get(MapleStatInfo.dotTime)) > 0);
    }

    /**
     *
     * @return
     */
    private boolean isMist() {
        switch (this.sourceid) {
            case 火毒巫师.致命毒雾:
            case 2311011:
            case 4121015:
            case 4221006:
                return true;
        }
        return false;
    }

    private boolean is暗器伤人() {
        return (this.skill) && ((this.sourceid == 4111009));
    }

    private boolean is无限子弹() {
        return (this.skill) && (this.sourceid == 5201008);
    }

    private boolean is净化() {
        return (this.skill) && ((this.sourceid == 祭司.净化) || (this.sourceid == 9001000) || (this.sourceid == 9101000));
    }
    public boolean is侠盗本能() {
        return (this.skill) && (this.sourceid == 4221013);
    }

    public boolean is斗气集中() {
        switch (this.sourceid) {
            case 1111002:
                return this.skill;
        }
        return false;
    }

    public boolean isMorph() {
        return this.morphId > 0;
    }

    public int getMorph() {
        return this.morphId;
    }

    public boolean is金刚霸体() {
        return (this.skill) && (GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1010);
    }

    public boolean is祝福护甲() {
        switch (this.sourceid) {
            case 1210016:
                return this.skill;
        }
        return false;
    }

    public boolean is狂暴战魂() {
        return (this.skill) && (GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1011);
    }

    public int getMorph(MapleCharacter chr) {
        int morph = getMorph();
        switch (morph) {
            case 1000:
            case 1001:
            case 1003:
                return morph + (chr.getGender() == 1 ? 100 : 0);
            case 1002:
        }
        return morph;
    }

    public byte getLevel() {
        return this.level;
    }

    public SummonMovementType getSummonMovementType() {
        if (!this.skill) {
            return null;
        }
        switch (this.sourceid) {
            case 游侠.替身术:
            case 射手.替身术:
                return SummonMovementType.不会移动;
            case 射手.银鹰召唤:
            case 游侠.金鹰召唤:
            case 祭司.圣龙召唤:
                return SummonMovementType.飞行跟随;
            case 5210015:
            case 5210016:
            case 5210017:
            case 5210018:
                return SummonMovementType.移动跟随;
        }
        return null;
    }

    public boolean is带BUFF技能() {
        switch (this.sourceid) {
            case 2321052:
                return this.skill;
        }
        return false;
    }

    public boolean isSkill() {
        return this.skill;
    }

    public int getSourceId() {
        return this.sourceid;
    }

    public boolean is冰骑士() {
        return (this.skill) && (GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1105);
    }

    public boolean isSoaring() {
        return (isSoaring_Normal()) || (isSoaring_Mount());
    }

    public boolean isSoaring_Normal() {
        return (this.skill) && (GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1026);
    }

    public boolean isSoaring_Mount() {
        return (this.skill) && (((GameConstants.is新手职业(this.sourceid / 10000)) && (this.sourceid % 10000 == 1142)) || (this.sourceid == 80001089));
    }

    public boolean is迷雾爆发() {
        return (this.skill) && (this.sourceid == 2121003);
    }

    public boolean is影分身() {
        switch (this.sourceid) {
            case 4111002:
            case 4211008:
            case 4331002:
            case 14111000:
            case 36111006:
                return this.skill;
        }
        return false;
    }

    public int getShadowDamage() {
        switch (this.sourceid) {
            case 4111002:
            case 4211008:
            case 4331002:
            case 14111000:
                return (this.info.get(MapleStatInfo.x));
            case 36111006:
                return (this.info.get(MapleStatInfo.y));
        }
        return (this.info.get(MapleStatInfo.x));
    }

    public boolean isMechPassive() {
        switch (this.sourceid) {
            case 35001001:
            case 35101009:
            case 35111004:
            case 35121005:
            case 35121013:
            case 35121054:
                return true;
        }
        return false;
    }

    public boolean makeChanceResult() {
        return ((this.info.get(MapleStatInfo.prop)) >= 100) || (Randomizer.nextInt(100) < (this.info.get(MapleStatInfo.prop)));
    }

    public boolean makeAngelReborn() {
        return ((this.info.get(MapleStatInfo.onActive)) >= 100) || (Randomizer.nextInt(100) < (this.info.get(MapleStatInfo.onActive)));
    }

    public boolean makeChanceResult(MapleCharacter applyfrom) {
        int prop = (this.info.get(MapleStatInfo.prop));
        return (prop >= 100) || (Randomizer.nextInt(100) < prop);
    }

    public int getProb() {
        return (this.info.get(MapleStatInfo.prop));
    }

    public short getIgnoreMob() {
        return this.ignoreMob;
    }

    public int getEnhancedHP() {
        return (this.info.get(MapleStatInfo.emhp));
    }

    public int getEnhancedMP() {
        return (this.info.get(MapleStatInfo.emmp));
    }

    public int getEnhancedWatk() {
        return (this.info.get(MapleStatInfo.epad));
    }

    public int getEnhancedMatk() {
        return (this.info.get(MapleStatInfo.emad));
    }

    public int getEnhancedWdef() {
        return (this.info.get(MapleStatInfo.pdd));
    }

    public int getEnhancedMdef() {
        return (this.info.get(MapleStatInfo.emdd));
    }

    public int getDOT() {
        return (this.info.get(MapleStatInfo.dot));
    }

    public int getDOTTime() {
        return (this.info.get(MapleStatInfo.dotTime));
    }

    public int getCritical() {
        return (this.info.get(MapleStatInfo.cr));
    }

    public int getCriticalMax() {
        return (this.info.get(MapleStatInfo.criticaldamageMax));
    }

    public int getCriticalMin() {
        return (this.info.get(MapleStatInfo.criticaldamageMin));
    }

    public int getArRate() {
        return (this.info.get(MapleStatInfo.ar));
    }

    public int getASRRate() {
        return (this.info.get(MapleStatInfo.asrR));
    }

    public int getTERRate() {
        return (this.info.get(MapleStatInfo.terR));
    }

    public int getDAMRate() {
        return (this.info.get(MapleStatInfo.damR));
    }

    public int getMdRate() {
        return (this.info.get(MapleStatInfo.mdR));
    }

    public int getPercentDamageRate() {
        return (this.info.get(MapleStatInfo.pdR));
    }

    public short getMesoRate() {
        return this.mesoR;
    }

    public int getEXP() {
        return this.exp;
    }

    public int getWdefToMdef() {
        return (this.info.get(MapleStatInfo.pdd2mdd));
    }

    public int getMdefToWdef() {
        return (this.info.get(MapleStatInfo.mdd2pdd));
    }

    public int getAvoidToHp() {
        return (this.info.get(MapleStatInfo.eva2hp));
    }

    public int getAccToMp() {
        return (this.info.get(MapleStatInfo.acc2mp));
    }

    public int getStrToDex() {
        return (this.info.get(MapleStatInfo.str2dex));
    }

    public int getDexToStr() {
        return (this.info.get(MapleStatInfo.dex2str));
    }

    public int getIntToLuk() {
        return (this.info.get(MapleStatInfo.int2luk));
    }

    public int getLukToDex() {
        return (this.info.get(MapleStatInfo.luk2dex));
    }

    public int getHpToDamageX() {
        return (this.info.get(MapleStatInfo.mhp2damX));
    }

    public int getMpToDamageX() {
        return (this.info.get(MapleStatInfo.mmp2damX));
    }

    public int getLevelToMaxHp() {
        return (this.info.get(MapleStatInfo.lv2mhp));
    }

    public int getLevelToMaxMp() {
        return (this.info.get(MapleStatInfo.lv2mmp));
    }

    public int getLevelToDamageX() {
        return (this.info.get(MapleStatInfo.lv2damX));
    }

    public int getLevelToWatk() {
        return (this.info.get(MapleStatInfo.lv2pad));
    }

    public int getLevelToMatk() {
        return (this.info.get(MapleStatInfo.lv2mad));
    }

    public int getLevelToWatkX() {
        return (this.info.get(MapleStatInfo.lv2pdX));
    }

    public int getLevelToMatkX() {
        return (this.info.get(MapleStatInfo.lv2mdX));
    }

    public int getEXPLossRate() {
        return (this.info.get(MapleStatInfo.expLossReduceR));
    }

    public int getBuffTimeRate() {
        return (this.info.get(MapleStatInfo.bufftimeR));
    }

    public int getSuddenDeathR() {
        return (this.info.get(MapleStatInfo.suddenDeathR));
    }

    public int getSummonTimeInc() {
        return (this.info.get(MapleStatInfo.summonTimeR));
    }

    public int getMPConsumeEff() {
        return (this.info.get(MapleStatInfo.mpConEff));
    }

    public int getAttackX() {
        return (this.info.get(MapleStatInfo.padX));
    }

    public int getMagicX() {
        return (this.info.get(MapleStatInfo.madX));
    }

    public int getPercentHP() {
        return (this.info.get(MapleStatInfo.mhpR));
    }

    public int getPercentMP() {
        return (this.info.get(MapleStatInfo.mmpR));
    }

    public int getIgnoreMobDamR() {
        return (this.info.get(MapleStatInfo.ignoreMobDamR));
    }

    public int getConsume() {
        return this.consumeOnPickup;
    }

    public int getSelfDestruction() {
        return (this.info.get(MapleStatInfo.selfDestruction));
    }

    public int getCharColor() {
        return this.charColor;
    }

    public List<Integer> getPetsCanConsume() {
        return this.petsCanConsume;
    }

    public boolean isReturnScroll() {
        return (this.skill) && ((this.sourceid == 20031203) || (this.sourceid == 80001040) || (this.sourceid == 20021110));
    }

    public boolean isMechChange() {
        switch (this.sourceid) {
            case 35001001:
            case 35101009:
            case 35111004:
            case 35121005:
            case 35121013:
            case 35121054:
                return this.skill;
        }
        return false;
    }

    public int getRange() {
        return (this.info.get(MapleStatInfo.range));
    }

    public int getER() {
        return (this.info.get(MapleStatInfo.er));
    }

    public int getPrice() {
        return (this.info.get(MapleStatInfo.price));
    }

    public int getExtendPrice() {
        return (this.info.get(MapleStatInfo.extendPrice));
    }

    public int getPeriod() {
        return (this.info.get(MapleStatInfo.period));
    }

    public int getReqGuildLevel() {
        return (this.info.get(MapleStatInfo.reqGuildLevel));
    }

    public byte getEXPRate() {
        return this.expR;
    }

    public short getLifeID() {
        return this.lifeId;
    }

    public short getUseLevel() {
        return this.useLevel;
    }

    public byte getSlotCount() {
        return this.slotCount;
    }

    public byte getSlotPerLine() {
        return this.slotPerLine;
    }

    public int getStr() {
        return (this.info.get(MapleStatInfo.str));
    }

    public int getStrX() {
        return (this.info.get(MapleStatInfo.strX));
    }

    public int getStrFX() {
        return (this.info.get(MapleStatInfo.strFX));
    }

    public int getStrRate() {
        return (this.info.get(MapleStatInfo.strR));
    }

    public int getDex() {
        return (this.info.get(MapleStatInfo.dex));
    }

    public int getDexX() {
        return (this.info.get(MapleStatInfo.dexX));
    }

    public int getDexFX() {
        return (this.info.get(MapleStatInfo.dexFX));
    }

    public int getDexRate() {
        return (this.info.get(MapleStatInfo.dexR));
    }

    public int getInt() {
        return (this.info.get(MapleStatInfo.int_));
    }

    public int getIntX() {
        return (this.info.get(MapleStatInfo.intX));
    }

    public int getIntFX() {
        return (this.info.get(MapleStatInfo.intFX));
    }

    public int getIntRate() {
        return (this.info.get(MapleStatInfo.intR));
    }

    public int getLuk() {
        return (this.info.get(MapleStatInfo.luk));
    }

    public int getLukX() {
        return (this.info.get(MapleStatInfo.lukX));
    }

    public int getLukFX() {
        return (this.info.get(MapleStatInfo.lukFX));
    }

    public int getLukRate() {
        return (this.info.get(MapleStatInfo.lukR));
    }

    public int getMaxHpX() {
        return (this.info.get(MapleStatInfo.mhpX));
    }

    public int getMaxMpX() {
        return (this.info.get(MapleStatInfo.mmpX));
    }

    public int getAccX() {
        return (this.info.get(MapleStatInfo.accX));
    }

    public int getPercentAcc() {
        return (this.info.get(MapleStatInfo.accR));
    }

    public int getAvoidX() {
        return (this.info.get(MapleStatInfo.evaX));
    }

    public int getPercentAvoid() {
        return (this.info.get(MapleStatInfo.evaR));
    }

    public int getWdefX() {
        return (this.info.get(MapleStatInfo.pddX));
    }

    public int getMdefX() {
        return (this.info.get(MapleStatInfo.mddX));
    }

    public int getIndieMHp() {
        return (this.info.get(MapleStatInfo.indieMhp));
    }

    public int getIndieMMp() {
        return (this.info.get(MapleStatInfo.indieMmp));
    }

    public int getIndieMhpR() {
        return (this.info.get(MapleStatInfo.indieMhpR));
    }

    public int getIndieMmpR() {
        return (this.info.get(MapleStatInfo.indieMmpR));
    }

    public int getIndieAllStat() {
        return (this.info.get(MapleStatInfo.indieAllStat));
    }

    public int getIndieCr() {
        return (this.info.get(MapleStatInfo.indieCr));
    }

    public short getIndiePdd() {
        return this.indiePdd;
    }

    public short getIndieMdd() {
        return this.indieMdd;
    }

    public int getIndieDamR() {
        return (this.info.get(MapleStatInfo.indieDamR));
    }

    public byte getType() {
        return this.type;
    }

    public int getBossDamage() {
        return (this.info.get(MapleStatInfo.bdR));
    }

    public int getMobCountDamage() {
        return (this.info.get(MapleStatInfo.mobCountDamR));
    }

    public int getInterval() {
        return this.interval;
    }

    public ArrayList<Pair<Integer, Integer>> getAvailableMaps() {
        return this.availableMap;
    }

    public int getWDEFRate() {
        return (this.info.get(MapleStatInfo.pddR));
    }

    public int getMDEFRate() {
        return (this.info.get(MapleStatInfo.mddR));
    }

    public int getKillSpree() {
        return (this.info.get(MapleStatInfo.kp));
    }

    public int getMaxDamageOver() {
        return (this.info.get(MapleStatInfo.MDamageOver));
    }

    public int getIndieMaxDamageOver() {
        return (this.info.get(MapleStatInfo.indieMaxDamageOver));
    }

    public int getCostMpRate() {
        return (this.info.get(MapleStatInfo.costmpR));
    }

    public int getMPConReduce() {
        return (this.info.get(MapleStatInfo.mpConReduce));
    }

    public int getIndieMaxDF() {
        return (this.info.get(MapleStatInfo.MDF));
    }

    public int getTargetPlus() {
        return (this.info.get(MapleStatInfo.targetPlus));
    }

    public int getForceCon() {
        return (this.info.get(MapleStatInfo.forceCon));
    }

    public int gethcHp() {
        return (this.info.get(MapleStatInfo.hcHp));
    }

    public int getsubProp() {
        return (this.info.get(MapleStatInfo.subProp));
    }

    public int getvehicleID() {
        Skill skil = SkillFactory.getSkill(sourceid);
        return skil.getvehicleID();
    }

    public boolean isNotuseMp() { //TODO 添加 不消耗MP技能
        switch (this.sourceid) {
            case 95001000:
            case 13100022:
            case 13120003:
            case 13110022:
            case 13121054:
            case 36101001:
            case 2121054:
            case 4221052:
            case 2221012:
                return true;
        }
        return false;
    }

    public int setbufftime(int time) {
        return (this.bufftime = time);
    }

    public int settimes(int time) {
        return (this.times = time);
    }

    public static class CancelEffectAction implements Runnable {

        private final MapleStatEffect effect;
        private final WeakReference<MapleCharacter> target;
        private final long startTime;
        private final List<Pair<MapleBuffStat, Integer>> statup;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime, List<Pair<MapleBuffStat, Integer>> statup) {
            this.effect = effect;
            this.target = new WeakReference(target);
            this.startTime = startTime;
            this.statup = statup;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = (MapleCharacter) this.target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(this.effect, false, this.startTime, this.statup);
            }
        }
    }
}
