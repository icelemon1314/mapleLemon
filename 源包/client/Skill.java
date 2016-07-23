package client;

import constants.GameConstants;
import java.util.ArrayList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import server.skill.冒险家.勇士;
import server.skill.冒险家.牧师;
import server.skill.冒险家.独行客;
import tools.FileoutputUtil;
import tools.Pair;

/**
 * 技能类 载入技能相关
 *
 */
public class Skill {

    private String name = "";
    private final List<MapleStatEffect> effects = new ArrayList();
    private List<MapleStatEffect> pvpEffects = null;
    private List<Integer> animation = null;
    private final List<Pair<String, Integer>> requiredSkill = new ArrayList<>();
    private Element element = Element.NEUTRAL;
    private final int id;
    private int animationTime = 0;
    private int masterLevel = 0;
    private int maxLevel = 0;
    private int delay = 0;
    private int trueMax = 0;
    private int eventTamingMob = 0;
    private int skillType = 0;
    private boolean invisible = false;
    private boolean chargeskill = false;
    private boolean timeLimited = false;
    private boolean combatOrders = false;
    private boolean magic = false;
    private boolean casterMove = false;
    private boolean pushTarget = false;
    private boolean pullTarget = false;
    private boolean isBuffSkill = false;
    private boolean isSummonSkill = false;
    private boolean notRemoved = false;
    private int fixLevel;
    private int hyper = 0;
    private int reqLev = 0;
    private int maxDamageOver = 2147483647;
    private boolean petPassive = false;
    private int setItemReason;
    private int setItemPartsCount;
    private int vehicleID;

    public Skill(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static Skill loadFromData(int id, MapleData data, MapleData delayData) {
        boolean showSkill = false;
        if (showSkill) {
            FileoutputUtil.log(FileoutputUtil.SkillsLog, "正在解析技能id: " + id + " 名字: " + SkillFactory.getSkillName(id), true);
        }
        Skill ret = new Skill(id);

        int skillType = MapleDataTool.getInt("skillType", data, -1);
        String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        }
        ret.skillType = skillType;
        ret.invisible = (MapleDataTool.getInt("invisible", data, 0) > 0);
        ret.notRemoved = (MapleDataTool.getInt("notRemoved", data, 0) > 0);
        ret.timeLimited = (MapleDataTool.getInt("timeLimited", data, 0) > 0);
        ret.combatOrders = (MapleDataTool.getInt("combatOrders", data, 0) > 0);
        ret.fixLevel = MapleDataTool.getInt("fixLevel", data, 0);
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
        ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
        ret.vehicleID = MapleDataTool.getInt("vehicleID", data, 0);
        ret.hyper = MapleDataTool.getInt("hyper", data, 0);
        ret.reqLev = MapleDataTool.getInt("reqLev", data, 0);

        ret.petPassive = (MapleDataTool.getInt("petPassive", data, 0) > 0);
        ret.setItemReason = MapleDataTool.getInt("setItemReason", data, 0);
        ret.setItemPartsCount = MapleDataTool.getInt("setItemPartsCount", data, 0);
        MapleData inf = data.getChildByPath("info");
        if (inf != null) {
            ret.magic = (MapleDataTool.getInt("magicDamage", inf, 0) > 0);
            ret.casterMove = (MapleDataTool.getInt("casterMove", inf, 0) > 0);
            ret.pushTarget = (MapleDataTool.getInt("pushTarget", inf, 0) > 0);
            ret.pullTarget = (MapleDataTool.getInt("pullTarget", inf, 0) > 0);
        }
        MapleData effect = data.getChildByPath("effect");
        boolean isBuff;
        if (skillType == 2) {
            isBuff = true;
        } else {
            MapleData action_ = data.getChildByPath("action");
            MapleData hit = data.getChildByPath("hit");
            MapleData ball = data.getChildByPath("ball");

            boolean action = false;
            if ((action_ == null)
                    && (data.getChildByPath("prepare/action") != null)) {
                action_ = data.getChildByPath("prepare/action");
                action = true;
            }

            isBuff = (effect != null) && (hit == null) && (ball == null);
            String d;
            if (action_ != null) {
                if (action) {
                    d = MapleDataTool.getString(action_, null);
                } else {
                    d = MapleDataTool.getString("0", action_, null);
                }
                if (d != null) {
                    isBuff |= d.equals("alert2");
                    MapleData dd = delayData.getChildByPath(d);
                    if (dd != null) {
                        for (MapleData del : dd) {
                            ret.delay += Math.abs(MapleDataTool.getInt("delay", del, 0));
                        }
                        if (ret.delay > 30) {
                            ret.delay = (int) Math.round(ret.delay * 11.0D / 16.0D);
                            ret.delay -= ret.delay % 30;
                        }
                    }
                    if (SkillFactory.getDelay(d) != null) {
                        ret.animation = new ArrayList();
                        ret.animation.add(SkillFactory.getDelay(d));
                        if (!action) {
                            for (MapleData ddc : action_) {
                                if ((!MapleDataTool.getString(ddc, d).equals(d)) && (!ddc.getName().contentEquals("delay"))) {
                                    String c = MapleDataTool.getString(ddc);
                                    if (SkillFactory.getDelay(c) != null) {
                                        ret.animation.add(SkillFactory.getDelay(c));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            switch (id) {
                case 1076:
                case 2111002:
                case 2111003:
                case 牧师.群体治愈:
                case 2321001:
                case 4301004:
                    isBuff = false;
                    break;
                case 93:
                case 1004:
                case 1026:
                case 勇士.斗气集中:
                case 1121016:
                case 1210016:
                case 1221014:
                case 1310016:
                case 1321014:
                case 2120010:
                case 2120012:
                case 2220010:
                case 2220013:
                case 2320011:
                case 2320012:
                case 3101004:
                case 3111011:
                case 3201004:
                case 3211012:
                case 独行客.敛财术:
                case 4221013:
                case 4330009:
                case 4341002:
                case 4341052:
                case 5001005:
                case 5100015:
                case 5111007:
                case 5120011:
                case 5120012:
                case 5121009:
                case 5211007:
                case 5220012:
                case 5220014:
                case 5220019:
                case 5221015:
                case 5311005:
                case 5320007:
                case 5321003:
                case 5321004:
                case 5711001:
                case 5711011:
                case 5720005:
                case 5720012:
                case 5721003:
                case 1121053://传说冒险家
                case 1221053:
                case 1321053:
                case 2121053:
                case 2221053:
                case 2321053:
                case 3121053:
                case 3221053:
                case 3321053:
                case 4121053:
                case 4221053:
                case 5121053:
                case 5221053://传说冒险家
                case 9001004:
                case 9101004:
                case 1221009:
                case 5121052:
                    isBuff = true;
            }
        }
        ret.chargeskill = (data.getChildByPath("keydown") != null);
        // 技能等级数据
        for (MapleData leve : data.getChildByPath("level")) {
            ret.effects.add(MapleStatEffect.loadSkillEffectFromData(leve, id, isBuff, Byte.parseByte(leve.getName()), null, ret.notRemoved));
        }
        ret.maxLevel = ret.effects.size();
        ret.trueMax = ret.effects.size();
        MapleData reqDataRoot = data.getChildByPath("req");
        if (reqDataRoot != null) {
            for (MapleData reqData : reqDataRoot.getChildren()) {
                ret.requiredSkill.add(new Pair<>(reqData.getName(), MapleDataTool.getInt(reqData, 1)));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }
        ret.isBuffSkill = isBuff;
        ret.isSummonSkill = (data.getChildByPath("summon") != null);

        return ret;
    }

    public MapleStatEffect getEffect(int level) {
        if (this.effects.size() < level) {
            if (this.effects.size() > 0) {
                return this.effects.get(this.effects.size() - 1);
            }
            return null;
        }
        if (level <= 0) {
            return this.effects.get(0);
        }
        return this.effects.get(level - 1);
    }

    public MapleStatEffect getPVPEffect(int level) {
        if (this.pvpEffects == null) {
            return getEffect(level);
        }
        if (this.pvpEffects.size() < level) {
            if (this.pvpEffects.size() > 0) {
                return (MapleStatEffect) this.pvpEffects.get(this.pvpEffects.size() - 1);
            }
            return null;
        }
        if (level <= 0) {
            return (MapleStatEffect) this.pvpEffects.get(0);
        }
        return (MapleStatEffect) this.pvpEffects.get(level - 1);
    }

    public int getSkillType() {
        return this.skillType;
    }

    public List<Integer> getAllAnimation() {
        return this.animation;
    }

    public int getAnimation() {
        if (this.animation == null) {
            return -1;
        }
        return (this.animation.get(Randomizer.nextInt(this.animation.size())));
    }

    public boolean isChargeSkill() {
        return this.chargeskill;
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    public boolean isNotRemoved() {
        return this.notRemoved;
    }

    public int getFixLevel() {
        return this.fixLevel;
    }

    public boolean is特性技能() {
        return this.fixLevel > 0;
    }

    public boolean hasRequiredSkill() {
        return this.requiredSkill.size() > 0;
    }

    public List<Pair<String, Integer>> getRequiredSkills() {
        return this.requiredSkill;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getTrueMax() {
        return this.trueMax;
    }

    public boolean combatOrders() {
        return this.combatOrders;
    }

    public boolean canBeLearnedBy(int job) {
        int jid = job;
        int skillForJob = this.id / 10000;
        if (skillForJob == 0) {
            return GameConstants.is冒险家(job);
        }

        if (jid / 100 != skillForJob / 100) {
            return false;
        }
        if (jid / 1000 != skillForJob / 1000) {
            return false;
        }
        if ((GameConstants.is冒险家(skillForJob)) && (!GameConstants.is冒险家(job))) {
            return false;
        }
        if ((jid / 10 % 10 == 0) && (skillForJob / 10 % 10 > jid / 10 % 10)) {
            return false;
        }
        if ((skillForJob / 10 % 10 != 0) && (skillForJob / 10 % 10 != jid / 10 % 10)) {
            return false;
        }
        return skillForJob % 10 <= jid % 10;
    }

    public boolean isTimeLimited() {
        return this.timeLimited;
    }

    public Element getElement() {
        return this.element;
    }

    public int getvehicleID() {
        return this.vehicleID;
    }

    public int getAnimationTime() {
        return this.animationTime;
    }

    public int getMasterLevel() {
        return this.masterLevel;
    }

    public int getDelay() {
        return this.delay;
    }

    public int getTamingMob() {
        return this.eventTamingMob;
    }

    public int getHyper() {
        return this.hyper;
    }

    public int getReqLevel() {
        return this.reqLev;
    }

    public int getMaxDamageOver() {
        return this.maxDamageOver;
    }

    public boolean isMagic() {
        return this.magic;
    }

    public boolean isMovement() {
        return this.casterMove;
    }

    public boolean isPush() {
        return this.pushTarget;
    }

    public boolean isPull() {
        return this.pullTarget;
    }

    public boolean isBuffSkill() {
        return this.isBuffSkill;
    }

    public boolean isSummonSkill() {
        return this.isSummonSkill;
    }

    public boolean isAdminSkill() {
        int jobId = this.id / 10000;
        return (jobId == 800) || (jobId == 900);
    }

    public boolean isSpecialSkill() {
        int jobId = this.id / 10000;
        return (jobId == 7000) || (jobId == 7100) || (jobId == 8000) || (jobId == 9000) || (jobId == 9100) || (jobId == 9200) || (jobId == 9201) || (jobId == 9202) || (jobId == 9203) || (jobId == 9204);
    }

    public int getSkillByJobBook() {
        return getSkillByJobBook(this.id);
    }

    public int getSkillByJobBook(int skillid) {
        switch (skillid / 10000) {
            case 112:
            case 122:
            case 132:
            case 212:
            case 222:
            case 232:
            case 312:
            case 322:
            case 412:
            case 422:
            case 512:
            case 522:
                return 4;
            case 111:
            case 121:
            case 131:
            case 211:
            case 221:
            case 231:
            case 311:
            case 321:
            case 411:
            case 421:
            case 511:
            case 521:
                return 3;
            case 110:
            case 120:
            case 130:
            case 210:
            case 220:
            case 230:
            case 310:
            case 320:
            case 410:
            case 420:
            case 510:
            case 520:
                return 2;
            case 100:
            case 200:
            case 300:
            case 400:
            case 500:
                return 1;
        }
        return -1;
    }

    public boolean isPetPassive() {
        return this.petPassive;
    }

    public int getSetItemReason() {
        return this.setItemReason;
    }

    public int geSetItemPartsCount() {
        return this.setItemPartsCount;
    }

}
