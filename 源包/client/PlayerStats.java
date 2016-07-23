package client;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import constants.ItemConstants;
import handling.world.WorldGuildService;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildSkill;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.ServerProperties;
import server.StructItemOption;
import server.StructSetItem;
import server.StructSetItemStat;
import server.life.Element;
import server.skill.冒险家.侠客;
import server.skill.冒险家.勇士;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.InventoryPacket;

/**
 * 角色状态信息 可以在在 这里添加被动技能的效果
 *
 */
public class PlayerStats implements Serializable {

    private static final long serialVersionUID = -679541993413738569L;
    private final Map<Integer, Integer> setHandling = new HashMap();
    private final Map<Integer, Integer> skillsIncrement = new HashMap();
    private final Map<Integer, Integer> damageIncrease = new HashMap();
    private final EnumMap<Element, Integer> elemBoosts = new EnumMap(Element.class);
    private final List<Equip> equipLevelHandling = new ArrayList();
    private transient float shouldHealHP; // 每次自动回血数值
    private transient float shouldHealMP; // 每次自动回蓝数值
    public short str;
    public short dex;
    public short luk;
    public short int_;
    public int baseHp; // 客户端显示的HP
    public int baseMaxHp;
    public int baseMp;
    public int baseMaxMp;
    private transient byte passive_mastery;
    private transient int localstr;
    private transient int localdex;
    private transient int localluk;
    private transient int localint_;
    private transient int localmaxhp;
    private transient int localmaxmp;
    private transient int addmaxhp;
    private transient int addmaxmp;
    private transient int IndieStrFX;
    private transient int IndieDexFX;
    private transient int IndieLukFX;
    private transient int IndieIntFX;
    private transient int magic;
    private transient int watk;
    private transient int hands;
    private transient int accuracy;
    public transient boolean equippedWelcomeBackRing;
    public transient boolean hasClone;
    public transient boolean hasPartyBonus;
    public transient boolean Berserk;
    public transient boolean canFish;
    public transient boolean canFishVIP;
    public transient double expBuff;
    public transient double dropBuff;
    public transient double mesoBuff;
    public transient double cashBuff;
    public transient double mesoGuard;
    public transient double mesoGuardMeso;
    public transient double expMod;
    public transient double pickupRange;
    public transient double incRewardProp;
    public transient int recoverHP;
    public transient int recoverMP;
    public transient int mpconReduce;
    public transient int mpconPercent;
    public transient int incMesoProp;
    public transient int reduceCooltime;
    public transient int coolTimeR;
    public transient int suddenDeathR;
    public transient int expLossReduceR;
    public transient int DAMreflect;
    public transient int DAMreflect_rate;
    public transient int ignoreDAMr;
    public transient int ignoreDAMr_rate;
    public transient int ignoreDAM;
    public transient int ignoreDAM_rate;
    public transient int mpRestore;
    public transient int hpRecover;
    public transient int hpRecoverProp;
    public transient int hpRecoverPercent;
    public transient int mpRecover;
    public transient int mpRecoverProp;
    public transient int RecoveryUP;
    public transient int BuffUP;
    public transient int RecoveryUP_Skill;
    public transient int BuffUP_Skill;
    public transient int BuffTimeR;
    public transient int incAllskill;
    public transient int combatOrders;
    public transient int defRange;
    public transient int BuffUP_Summon;
    public transient int dodgeChance;
    public transient int speed;
    public transient int speedMax;
    public transient int jump;
    public transient int harvestingTool;
    public transient int equipmentBonusExp;
    public transient int dropMod;
    public transient int cashMod;
    public transient int levelBonus;
    public transient int ASR;
    public transient int TER;
    public transient int pickRate;
    public transient int decreaseDebuff;
    public transient int equippedFairy;
    public transient int pvpDamage;
    public transient int hpRecoverTime = 0;
    public transient int mpRecoverTime = 0;
    public transient int dot;
    public transient int dotTime;
    public transient int questBonus;
    public transient int wdef;
    public transient int mdef;
    public transient int trueMastery;
    public transient int damX;
    public transient int incMaxDamage;
    public transient int incMaxDF;
    private transient short passive_sharpeye_rate;
    private transient short passive_sharpeye_max_percent;
    private transient short passive_sharpeye_min_percent;
    public transient int stanceProp;
    public transient int percent_wdef;
    public transient int percent_mdef;
    public transient int percent_hp;
    public transient int percent_mp;
    public transient int percent_str;
    public transient int percent_dex;
    public transient int percent_int;
    public transient int percent_luk;
    public transient int percent_acc;
    public transient int percent_atk;
    public transient int percent_matk;
    public transient int percent_ignore_mob_def_rate;
    public transient double percent_damage;
    public transient int percent_damage_rate;
    public transient int percent_boss_damage_rate;
    public transient int ignore_mob_damage_rate;
    public transient int reduceDamageRate;
    private transient float localmaxbasedamage;
    private transient float localmaxbasepvpdamage;
    private transient float localmaxbasepvpdamageL;
    public transient int def;
    public transient int element_ice;
    public transient int element_fire;
    public transient int element_light;
    public transient int element_psn;
    public transient int raidenCount;
    public transient int raidenPorp;
    private final Map<Integer, Integer> add_skill_duration = new HashMap();
    private final Map<Integer, Integer> add_skill_attackCount = new HashMap();
    private final Map<Integer, Integer> add_skill_targetPlus = new HashMap();
    private final Map<Integer, Integer> add_skill_bossDamageRate = new HashMap();
    private final Map<Integer, Integer> add_skill_dotTime = new HashMap();
    private final Map<Integer, Integer> add_skill_prop = new HashMap();
    private final Map<Integer, Integer> add_skill_coolTimeR = new HashMap();
    private final Map<Integer, Integer> add_skill_ignoreMobpdpR = new HashMap();

    private static final int[] allJobs = {0, 10000000, 20000000, 20010000, 20020000, 20030000, 20040000, 30000000, 30010000, 30020000, 50000000, 60000000, 60010000, 100000000};

    public void recalcLocalStats(MapleCharacter chra) {
        recalcLocalStats(false, chra);
    }

    private void resetLocalStats(int job) {
        this.accuracy = 0; //命中率
        this.wdef = 0; //物理防御
        this.mdef = 0; //魔法防御
        this.damX = 0; //攻击
        this.addmaxhp = 0; //增加MAXHP
        this.addmaxmp = 0; //增加MAXMP
        this.localdex = getDex(); //敏捷
        this.localint_ = getInt(); //智力
        this.localstr = getStr(); //力量
        this.localluk = getLuk(); //运气
        this.IndieDexFX = 0;
        this.IndieIntFX = 0;
        this.IndieStrFX = 0;
        this.IndieLukFX = 0;
        this.speed = 100; //速度
        this.jump = 100; //跳跃力
        this.pickupRange = 0.0D; //减捡取范围
        this.decreaseDebuff = 0; //
        this.ASR = 0;  //
        this.TER = 0; //
        this.dot = 0; //持续伤害
        this.questBonus = 1; //
        this.dotTime = 0; //持续伤害时间
        this.trueMastery = 0; //熟练度
        this.stanceProp = 0; //
        this.percent_wdef = 0; //增加百分比物理防御
        this.percent_mdef = 0; //增加百分比魔法防御
        this.percent_hp = 0; //增加百分比HP
        this.percent_mp = 0; //增加百分比HP
        this.percent_str = 0; //增加百分比力量
        this.percent_dex = 0; //增加百分比敏捷
        this.percent_int = 0; //增加百分比智力
        this.percent_luk = 0; //增加百分比运气
        this.percent_acc = 0; //增加百分比命中
        this.percent_atk = 0; //增加百分比攻击
        this.percent_matk = 0; //增加百分比魔法攻击
        this.percent_ignore_mob_def_rate = 0;  //增加百分比无视怪物防御
        this.passive_sharpeye_rate = 5;//暴击概率
        this.passive_sharpeye_min_percent = 20;//最小暴击伤害
        this.passive_sharpeye_max_percent = 50; //最大暴击伤害
        this.percent_damage_rate = 100; //增加百分比伤害
        this.percent_boss_damage_rate = 100;  //增加BOSS百分比伤害
        this.magic = 0; //魔法力
        this.watk = 0; //物理攻击力
        this.dodgeChance = 0; //闪避
        this.pvpDamage = 0;
        this.mesoGuard = 50.0D; //金钱盾吸收伤害比例
        this.mesoGuardMeso = 0.0D; // 金钱盾剩余金币
        this.percent_damage = 0.0D;  //百分比伤害
        this.expBuff = 100.0D; //经验BUFF
        this.cashBuff = 100.0D;
        this.dropBuff = 100.0D;
        this.mesoBuff = 100.0D;
        this.recoverHP = 0; //恢复HP量
        this.recoverMP = 0; //恢复MP量
        this.mpconReduce = 0; //恢复MP量
        this.mpconPercent = 100;  //恢复MP百分比量
        this.incMesoProp = 0;
        this.reduceCooltime = 0; //技能冷却
        this.coolTimeR = 0;  //技能冷却
        this.suddenDeathR = 0;
        this.expLossReduceR = 0;
        this.incRewardProp = 0.0D;
        this.DAMreflect = 0;
        this.DAMreflect_rate = 0;
        this.ignoreDAMr = 0; //无视伤害
        this.ignoreDAMr_rate = 0;//无视伤害百分比
        this.ignoreDAM = 0;  //无视伤害
        this.ignoreDAM_rate = 0; //无视伤害百分比
        this.hpRecover = 0; //HP恢复
        this.hpRecoverProp = 0; //HP恢复概率
        this.hpRecoverPercent = 0; //HP恢复百分比
        this.mpRecover = 0; //MP恢复
        this.mpRecoverProp = 0; //MP恢复概率
        this.mpRestore = 0;
        this.pickRate = 0;
        this.incMaxDamage = 0; //最大伤害
        this.equippedWelcomeBackRing = false;
        this.equippedFairy = 0;
        this.hasPartyBonus = false;
        this.hasClone = false;
        this.Berserk = false;
        this.canFish = false;
        this.canFishVIP = false;
        this.equipmentBonusExp = 0;
        this.RecoveryUP = 100;
        this.BuffUP = 100;
        this.RecoveryUP_Skill = 100;
        this.BuffTimeR = 100;
        this.BuffUP_Skill = 100;
        this.BuffUP_Summon = 100;
        this.dropMod = 1;
        this.expMod = 1.0D;
        this.cashMod = 1;
        this.levelBonus = 0;
        this.incMaxDF = 0;
        this.incAllskill = 0;
        this.combatOrders = 0;
        this.defRange = (isRangedJob(job) ? 200 : 0);
        this.equipLevelHandling.clear();
        this.skillsIncrement.clear();
        this.damageIncrease.clear();
        this.setHandling.clear();
        this.add_skill_duration.clear();
        this.add_skill_attackCount.clear();
        this.add_skill_targetPlus.clear();
        this.add_skill_dotTime.clear();
        this.add_skill_prop.clear();
        this.add_skill_coolTimeR.clear();
        this.add_skill_ignoreMobpdpR.clear();
        this.harvestingTool = 0;
        this.element_fire = 100;
        this.element_ice = 100;
        this.element_light = 100;
        this.element_psn = 100;
        this.def = 100;
        this.raidenCount = 0;
        this.raidenPorp = 0;
        this.ignore_mob_damage_rate = 0;
        this.reduceDamageRate = 0;
    }

    /**
     * 计算各类属性状态
     *
     * @param first_login 布尔值是否第一次登录
     * @param chra 角色实例
     */
    public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        resetLocalStats(chra.getJob());
        Map sData = new HashMap();
        Iterator itera = chra.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();
        while (itera.hasNext()) {
            Equip equip = (Equip) itera.next();
            if ((equip.getPosition() == -11)
                    && (ItemConstants.isMagicWeapon(equip.getItemId()))) {
                Map eqstat = ii.getEquipStats(equip.getItemId());
                if (eqstat != null) {
                    if (eqstat.containsKey("incRMAF")) {
                        this.element_fire = ((Integer) eqstat.get("incRMAF"));
                    }
                    if (eqstat.containsKey("incRMAI")) {
                        this.element_ice = ((Integer) eqstat.get("incRMAI"));
                    }
                    if (eqstat.containsKey("incRMAL")) {
                        this.element_light = ((Integer) eqstat.get("incRMAL"));
                    }
                    if (eqstat.containsKey("incRMAS")) {
                        this.element_psn = ((Integer) eqstat.get("incRMAS"));
                    }
                    if (eqstat.containsKey("elemDefault")) {
                        this.def = ((Integer) eqstat.get("elemDefault"));
                    }
                }
            }

            if ((equip.getItemId() / 10000 == 167) && chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -34) == null) {
                continue;
            }
            this.accuracy += equip.getAcc();
            localmaxhp_ += equip.getHp();
            localmaxmp_ += equip.getMp();
            this.localdex += equip.getDex();
            this.localint_ += equip.getInt();
            this.localstr += equip.getStr();
            this.localluk += equip.getLuk();
            this.magic += equip.getMatk();
            this.watk += equip.getWatk();
            this.wdef += equip.getWdef();
            this.mdef += equip.getMdef();
            this.speed += equip.getSpeed();
            this.jump += equip.getJump();

            if (equip.getItemId() / 1000 == 1099) {
                this.incMaxDF += equip.getMp();
            }
            this.percent_hp += ii.getItemIncMHPr(equip.getItemId());
            this.percent_mp += ii.getItemIncMMPr(equip.getItemId());
            this.percent_boss_damage_rate += equip.getBossDamage();
            this.percent_ignore_mob_def_rate += equip.getIgnorePDR();
            this.percent_damage_rate += equip.getTotalDamage();

            Integer set = ii.getSetItemID(equip.getItemId());
            if ((set != null) && (set > 0)) {
                int value = 1;
                if (this.setHandling.containsKey(set)) {
                    value += (this.setHandling.get(set));
                }
                this.setHandling.put(set, value);
            }
            Iterator i$;
            Pair ix = handleEquipAdditions(ii, chra, first_login, sData, equip.getItemId());
            if (ix != null) {
                localmaxhp_ += ((Integer) ix.getLeft());
                localmaxmp_ += ((Integer) ix.getRight());
            }
            if ((GameConstants.getMaxLevel(equip.getItemId()) > 0) && (GameConstants.getStatFromWeapon(equip.getItemId()) == null ? equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId()) : equip.getEquipLevel() < GameConstants.getMaxLevel(equip.getItemId()))) {
                this.equipLevelHandling.add(equip);
            }
        }
        Iterator iter = this.setHandling.entrySet().iterator();
        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            StructSetItem setItem = ii.getSetItem(((Integer) entry.getKey()).intValue());
            if (setItem != null) {
                Map<Integer, StructSetItemStat> setItemStats = setItem.getSetItemStats();
                for (Entry<Integer, StructSetItemStat> ent : setItemStats.entrySet()) {
                    StructSetItemStat setItemStat = (StructSetItemStat) ent.getValue();
                    if ((ent.getKey()) <= ((Integer) entry.getValue())) {
                        this.localstr += setItemStat.incSTR + setItemStat.incAllStat;
                        this.localdex += setItemStat.incDEX + setItemStat.incAllStat;
                        this.localint_ += setItemStat.incINT + setItemStat.incAllStat;
                        this.localluk += setItemStat.incLUK + setItemStat.incAllStat;
                        this.watk += setItemStat.incPAD;
                        this.magic += setItemStat.incMAD;
                        this.speed += setItemStat.incSpeed;
                        this.accuracy += setItemStat.incACC;
                        localmaxhp_ += setItemStat.incMHP;
                        localmaxmp_ += setItemStat.incMMP;
                        this.percent_hp += setItemStat.incMHPr;
                        this.percent_mp += setItemStat.incMMPr;
                        this.wdef += setItemStat.incPDD;
                        this.mdef += setItemStat.incMDD;
                    }
                }
            }
        }
        int hour = Calendar.getInstance().get(11);
        for (Item item : chra.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 100000 == 52) {
                if ((this.expMod < 3.0D) && (item.getItemId() == 5211060)) {
                    this.expMod = 3.0D;
                } else if ((this.expMod < 2.0D) && ((item.getItemId() == 5210000) || (item.getItemId() == 5210001) || (item.getItemId() == 5210002) || (item.getItemId() == 5210003) || (item.getItemId() == 5210004) || (item.getItemId() == 5210005) || (item.getItemId() == 5210006) || (item.getItemId() == 5211047))) {
                    this.expMod = 2.0D;
                } else if ((this.expMod < 1.5D) && ((item.getItemId() == 5211063) || (item.getItemId() == 5211064) || (item.getItemId() == 5211065) || (item.getItemId() == 5211066) || (item.getItemId() == 5211069) || (item.getItemId() == 5211070))) {
                    this.expMod = 1.5D;
                } else if ((this.expMod < 1.2D) && ((item.getItemId() == 5211071) || (item.getItemId() == 5211072) || (item.getItemId() == 5211073) || (item.getItemId() == 5211074) || (item.getItemId() == 5211075) || (item.getItemId() == 5211076) || (item.getItemId() == 5211067))) {
                    this.expMod = 1.2D;
                }
            } else if ((this.dropMod == 1) && (item.getItemId() / 10000 == 536)) {
                if ((item.getItemId() == 5360000) || (item.getItemId() == 5360014) || (item.getItemId() == 5360015) || (item.getItemId() == 5360016)) {
                    this.dropMod = 2;
                }
            } else if (item.getItemId() == 5650000) {
                this.hasPartyBonus = true;
            } else if (item.getItemId() == 5590001) {
                this.levelBonus = 10;
            } else if ((this.levelBonus == 0) && (item.getItemId() == 5590000)) {
                this.levelBonus = 5;
            } else if (item.getItemId() == 5710000) {
                this.questBonus = 2;
            } else if (item.getItemId() == 5340000) {
                this.canFish = true;
            } else if (item.getItemId() == 5340001) {
                this.canFish = true;
                this.canFishVIP = true;
            }
        }
//        handlePassiveSkills(chra);
        handleBuffStats(chra);
        Integer buff = chra.getBuffedValue(MapleBuffStat.最大体力);
        if (buff != null) {
            localmaxhp_ += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.最大魔力);
        if (buff != null) {
            localmaxmp_ += buff;
        }
        long now;

        this.localstr = (int) (this.localstr + (Math.floor(this.localstr * this.percent_str / 100.0F) + this.IndieStrFX));
        this.localdex = (int) (this.localdex + (Math.floor(this.localdex * this.percent_dex / 100.0F) + this.IndieDexFX));
        this.localint_ = (int) (this.localint_ + (Math.floor(this.localint_ * this.percent_int / 100.0F) + this.IndieIntFX));
        this.localluk = (int) (this.localluk + (Math.floor(this.localluk * this.percent_luk / 100.0F) + this.IndieLukFX));
        if (this.localint_ > this.localdex) {
            this.accuracy = (int) (this.accuracy + Math.floor(this.localint_ * 1.6D + this.localluk * 0.8D + this.localdex * 0.4D));
        } else {
            this.accuracy = (int) (this.accuracy + Math.floor(this.localdex * 1.6D + this.localluk * 0.8D + this.localstr * 0.4D));
        }
        this.watk = (int) (this.watk + Math.floor(this.watk * this.percent_atk / 100.0F));
        this.magic = (int) (this.magic + Math.floor(this.magic * this.percent_matk / 100.0F));
        this.localint_ = (int) (this.localint_ + Math.floor(this.localint_ * this.percent_matk / 100.0F));

        this.wdef = (int) (this.wdef + Math.floor(this.localstr * 1.5D + (this.localdex + this.localluk) * 0.4D));
        this.mdef = (int) (this.mdef + Math.floor(this.localint_ * 1.5D + (this.localdex + this.localluk) * 0.4D));
        this.wdef = (int) (this.wdef + Math.min(9999.0D, Math.floor(this.wdef * this.percent_wdef / 100.0F)));
        this.mdef = (int) (this.mdef + Math.min(9999.0D, Math.floor(this.mdef * this.percent_mdef / 100.0F)));

        this.hands = (this.localdex + this.localint_ + this.localluk);
        this.accuracy = (int) (this.accuracy + Math.min(9999.0D, Math.floor(this.accuracy * this.percent_acc / 100.0F)));

        localmaxhp_ += this.addmaxhp;
        localmaxhp_ = (int) (localmaxhp_ + Math.floor(this.percent_hp * localmaxhp_ / 100.0F));
        this.localmaxhp = Math.min(chra.getMaxHpForSever(), Math.abs(Math.max(-chra.getMaxHpForSever(), localmaxhp_)));

        localmaxmp_ = (int) (localmaxmp_ + Math.floor(this.percent_mp * localmaxmp_ / 100.0F));
        localmaxmp_ += this.addmaxmp;
        this.localmaxmp = Math.min(chra.getMaxMpForSever(), Math.abs(Math.max(-chra.getMaxMpForSever(), localmaxmp_)));

        chra.changeSkillLevel_Skip(sData, false);
//        CalcPassive_SharpEye(chra);
        CalcPassive_Mastery(chra);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }
        calculateMaxBaseDamage(Math.max(this.magic, this.watk), this.damX, this.pvpDamage, chra);
        this.trueMastery = Math.min(100, this.trueMastery);
        this.passive_sharpeye_min_percent = (short) Math.min(this.passive_sharpeye_min_percent, this.passive_sharpeye_max_percent);
        if ((getMaxHp() != this.localmaxhp)) {
            chra.updatePartyMemberHP();
        }
    }

    public double getDropBuff() {
        if (this.incRewardProp > 100.0D) {
            this.incRewardProp = 100.0D;
        }
        return this.dropBuff + this.incRewardProp;
    }

    /**
     * 处理被动技能 可以在在 这里添加被动技能的效果
     *
     * @author 7
     */
    private void handlePassiveSkills(MapleCharacter chra) {
        //  MapleStatEffect eff = null;
        Item shield = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        switch (chra.getJob()) {
            case 100:
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132:
                Skill bx = SkillFactory.getSkill(1001003);
                int bof = chra.getTotalSkillLevel(bx);
                MapleStatEffect eff;
                if (bof > 0) {
                    addBuffDuration(1001003, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(1000009);
                bof = chra.getTotalSkillLevel(bx);

                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    this.addmaxhp += eff.getLevelToMaxHp() * chra.getLevel();
                    this.jump += eff.getPassiveJump();
                    this.speed += eff.getSpeedMax();
                }
                break;

            case 200:
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232:
                bx = SkillFactory.getSkill(2000006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    this.percent_mp += eff.getPercentMP();
                    this.addmaxmp += eff.getLevelToMaxMp() * chra.getLevel();
                }
                break;
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
                bx = SkillFactory.getSkill(3000002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    this.defRange += eff.getRange();
                    this.accuracy += eff.getAcc();
                }

                break;
            case 400:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:
            case 431:
            case 432:
            case 433:
            case 434:
                bx = SkillFactory.getSkill(4000010);
                bof = chra.getTotalSkillLevel(bx);

                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    this.percent_hp += eff.getPercentHP();
                    this.ASR += eff.getASRRate();
                }
                bx = SkillFactory.getSkill(4001005);
                bof = chra.getTotalSkillLevel(bx);
                eff = bx.getEffect(bof);
                if (bof > 0) {
                    this.speed += eff.getSpeedMax();
                }
                bx = SkillFactory.getSkill(4000012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dodgeChance += bx.getEffect(bof).getER();
                }

                break;
            case 500:
            case 510:
            case 511:
            case 512:
            case 520:
            case 521:
            case 522:
                bx = SkillFactory.getSkill(5000000);
                bof = chra.getTotalSkillLevel(bx);
                eff = bx.getEffect(bof);

                if (bof > 0) {
                    this.accuracy += eff.getAccX();
                    this.jump += eff.getPassiveJump();
                    this.speed += eff.getSpeedMax();
                }
        }

        Skill bx = SkillFactory.getSkill(80000000);
        int bof = chra.getTotalSkillLevel(bx);
        MapleStatEffect eff = bx.getEffect(bof);
        if (bof > 0) {
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
            this.localint_ += eff.getIntX();
            this.localluk += eff.getLukX();
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
        }

        if (GameConstants.is冒险家(chra.getJob())) {
            bx = SkillFactory.getSkill(74);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.levelBonus += bx.getEffect(bof).getX();
            }
            bx = SkillFactory.getSkill(80);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.levelBonus += bx.getEffect(bof).getX();
            }
            bx = SkillFactory.getSkill(10074);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.levelBonus += bx.getEffect(bof).getX();
            }
            bx = SkillFactory.getSkill(10080);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.levelBonus += bx.getEffect(bof).getX();
            }
            bx = SkillFactory.getSkill(110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.localstr += bx.getEffect(bof).getStrX();
                this.localdex += bx.getEffect(bof).getDexX();
                this.localint_ += bx.getEffect(bof).getIntX();
                this.localluk += bx.getEffect(bof).getLukX();
                this.percent_hp = (int) (this.percent_hp + bx.getEffect(bof).getHpR());
                this.percent_mp = (int) (this.percent_mp + bx.getEffect(bof).getMpR());
            }
            bx = SkillFactory.getSkill(10110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                this.localstr += bx.getEffect(bof).getStrX();
                this.localdex += bx.getEffect(bof).getDexX();
                this.localint_ += bx.getEffect(bof).getIntX();
                this.localluk += bx.getEffect(bof).getLukX();
                this.percent_hp = (int) (this.percent_hp + bx.getEffect(bof).getHpR());
                this.percent_mp = (int) (this.percent_mp + bx.getEffect(bof).getMpR());
            }

        }

        switch (chra.getJob()) {
            case 110:
            case 111:
            case 112:
                bx = SkillFactory.getSkill(1100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(1110011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(1110009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage_rate += bx.getEffect(bof).getDamage();
                    this.percent_boss_damage_rate += bx.getEffect(bof).getDamage();
                }
                bx = SkillFactory.getSkill(1120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(1120013);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.watk += bx.getEffect(bof).getAttackX();
                }

                bx = SkillFactory.getSkill(1120043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    MapleStatEffect 斗气效果 = chra.getStatForBuff(MapleBuffStat.斗气集中);
                    Integer 斗气状态 = chra.getBuffedValue(MapleBuffStat.斗气集中);
                    if ((斗气效果 != null) && (斗气状态 != null)) {
                        this.percent_damage_rate += bx.getEffect(bof).getDAMRate() * (斗气状态 - 1);
                        this.percent_boss_damage_rate += bx.getEffect(bof).getDAMRate() * (斗气状态 - 1);
                    }
                }
                bx = SkillFactory.getSkill(1120044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addSkillProp(勇士.斗气集中, bx.getEffect(bof).getProb());
                    addSkillProp(1120003, bx.getEffect(bof).getProb());
                }
                bx = SkillFactory.getSkill(1120045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    MapleStatEffect 斗气效果 = chra.getStatForBuff(MapleBuffStat.斗气集中);
                    Integer 斗气状态 = chra.getBuffedValue(MapleBuffStat.斗气集中);
                    if ((斗气效果 != null) && (斗气状态 != null)) {
                        this.percent_boss_damage_rate += bx.getEffect(bof).getW() * (斗气状态 - 1);
                    }
                }
                bx = SkillFactory.getSkill(1120046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_acc += bx.getEffect(bof).getArRate();
                }
                bx = SkillFactory.getSkill(1120047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(1120049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(1121008, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(1120017, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(1120050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(1121008, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(1120017, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(1120051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(1121008, bx.getEffect(bof).getAttackCount());
                    addAttackCount(1120017, bx.getEffect(bof).getAttackCount());
                }
                break;
            case 120:
            case 121:
            case 122:
                bx = SkillFactory.getSkill(1200009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(1210001);
                bof = chra.getTotalSkillLevel(bx);
                if ((bof > 0) && (shield != null)) {
                    this.percent_wdef += bx.getEffect(bof).getX();
                    this.percent_mdef += bx.getEffect(bof).getX();
                    this.dodgeChance += bx.getEffect(bof).getER();
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(1210015);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.reduceDamageRate += bx.getEffect(bof).getT();
                }
                bx = SkillFactory.getSkill(1221016);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(1220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(1201011, bx.getEffect(bof).getAttackCount());
                    addAttackCount(1201012, bx.getEffect(bof).getAttackCount());
                    addAttackCount(1211008, bx.getEffect(bof).getAttackCount());
                    addAttackCount(1221004, bx.getEffect(bof).getAttackCount());
                    addAttackCount(1221009, bx.getEffect(bof).getAttackCount());

                    addTargetPlus(1201011, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(1201012, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(1211008, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(1221004, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(1221009, bx.getEffect(bof).getTargetPlus());
                }

                bx = SkillFactory.getSkill(1220043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBuffDuration(1211013, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(1220044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addSkillProp(1211013, bx.getEffect(bof).getProb());
                }
                bx = SkillFactory.getSkill(1220046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(1221009, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(1220048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(1221009, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(1220049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(1221011, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(1220050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(1221011, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(1220051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(1221011, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 130:
            case 131:
            case 132:
                bx = SkillFactory.getSkill(1300009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(1310010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(1310009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + bx.getEffect(bof).getCritical());
                    this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + bx.getEffect(bof).getCriticalMin());
                    this.hpRecoverProp += bx.getEffect(bof).getProb();
                    this.hpRecoverPercent += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(1321015);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }

                bx = SkillFactory.getSkill(1320043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBuffDuration(1301007, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(1320046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage_rate += bx.getEffect(bof).getDamage();
                    this.percent_boss_damage_rate += bx.getEffect(bof).getDamage();
                }
                bx = SkillFactory.getSkill(1320047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + bx.getEffect(bof).getCriticalMin());
                }
                bx = SkillFactory.getSkill(1320048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + bx.getEffect(bof).getCritical());
                }
                bx = SkillFactory.getSkill(1320049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(1321012, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(1320050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addIgnoreMobpdpRate(1321012, bx.getEffect(bof).getIgnoreMob());
                }
                bx = SkillFactory.getSkill(1320051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(1321012, bx.getEffect(bof).getAttackCount());
                }
                break;
            case 210:
            case 211:
            case 212:
                bx = SkillFactory.getSkill(2100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dotTime += bx.getEffect(bof).getX();
                    this.dot += bx.getEffect(bof).getZ();
                }
                bx = SkillFactory.getSkill(2110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.mpconPercent += bx.getEffect(bof).getCostMpRate();
                    this.percent_damage += bx.getEffect(bof).getDAMRate();
                }
                bx = SkillFactory.getSkill(2121003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(2111003, bx.getEffect(bof).getX());
                }
                bx = SkillFactory.getSkill(2120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.magic += bx.getEffect(bof).getMagicX();
                    this.BuffUP_Skill += bx.getEffect(bof).getBuffTimeRate();
                }
                bx = SkillFactory.getSkill(2120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_boss_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }

                bx = SkillFactory.getSkill(2120046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(2121006, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(2120049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(2121003, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(2120050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addIgnoreMobpdpRate(2121003, bx.getEffect(bof).getIgnoreMob());
                }
                bx = SkillFactory.getSkill(2120051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(2121003, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 220:
            case 221:
            case 222:
                bx = SkillFactory.getSkill(2200007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dot += bx.getEffect(bof).getZ();
                }
                bx = SkillFactory.getSkill(2210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.mpconPercent += bx.getEffect(bof).getCostMpRate();
                    this.percent_damage += bx.getEffect(bof).getDAMRate();
                }
                bx = SkillFactory.getSkill(2220013);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.magic += bx.getEffect(bof).getMagicX();
                    this.BuffUP_Skill += bx.getEffect(bof).getBuffTimeRate();
                }
                bx = SkillFactory.getSkill(2220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_boss_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }

                bx = SkillFactory.getSkill(2220043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(2211007, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(2220044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(2211007, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(2220046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(2221006, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(2220047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(2221006, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(2220048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(2221006, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(2220049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(2211010, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(2220050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(2211010, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(2220051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(2211010, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 230:
            case 231:
            case 232:
                bx = SkillFactory.getSkill(2300007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2320012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.magic += bx.getEffect(bof).getMagicX();
                    this.BuffUP_Skill += bx.getEffect(bof).getBuffTimeRate();
                }
                bx = SkillFactory.getSkill(2320011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_boss_damage_rate += bx.getEffect(bof).getX() * bx.getEffect(bof).getY();
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }

                bx = SkillFactory.getSkill(2320044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBuffDuration(2311009, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(2320045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(2311009, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 310:
            case 311:
            case 312:
                bx = SkillFactory.getSkill(3100006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(3110012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(3110014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                    this.percent_acc += bx.getEffect(bof).getArRate();
                    this.percent_damage += bx.getEffect(bof).getDAMRate();
                }
                bx = SkillFactory.getSkill(3111005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_wdef += bx.getEffect(bof).getWDEFRate();
                    this.percent_mdef += bx.getEffect(bof).getMDEFRate();
                }
                bx = SkillFactory.getSkill(3111010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(3110007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(3120008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.watk += bx.getEffect(bof).getAttackX();
                    addDamageIncrease(3100001, bx.getEffect(bof).getDamage());
                }

                bx = SkillFactory.getSkill(3120043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBuffDuration(3121002, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(3120044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                }
                bx = SkillFactory.getSkill(3120045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {

                }
                bx = SkillFactory.getSkill(3120046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(3121015, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(3120047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(3121015, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(3120048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(3121015, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(3120049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(3121013, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(3120050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_boss_damage_rate += bx.getEffect(bof).getBossDamage();
                }
                bx = SkillFactory.getSkill(3120051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(3121013, bx.getEffect(bof).getTargetPlus());
                }
                break;
            case 320:
            case 321:
            case 322:
                bx = SkillFactory.getSkill(3200006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(3210007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(3210015);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                    this.percent_acc += bx.getEffect(bof).getArRate();
                    this.percent_damage += bx.getEffect(bof).getDAMRate();
                }
                bx = SkillFactory.getSkill(3211005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_wdef += bx.getEffect(bof).getWDEFRate();
                    this.percent_mdef += bx.getEffect(bof).getMDEFRate();
                }
                bx = SkillFactory.getSkill(3211010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(3211011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }

                bx = SkillFactory.getSkill(3220043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBuffDuration(3221002, bx.getEffect(bof).getDuration());
                }
                bx = SkillFactory.getSkill(3220044);
                bof = chra.getTotalSkillLevel(bx);
                bx = SkillFactory.getSkill(3220045);
                bof = chra.getTotalSkillLevel(bx);
                bx = SkillFactory.getSkill(3220046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(3221017, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(3220047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(3221017, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(3220048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(3221017, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(3220049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(3221007, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(3220050);
                bof = chra.getTotalSkillLevel(bx);
                bx = SkillFactory.getSkill(3220051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(3221007, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 410:
            case 411:
            case 412:
                bx = SkillFactory.getSkill(4100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localluk += bx.getEffect(bof).getLukX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(4110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(4110012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_damage += bx.getEffect(bof).getPercentDamageRate();
                }
                bx = SkillFactory.getSkill(4110014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.RecoveryUP += bx.getEffect(bof).getX() - 100;
                    this.BuffUP += bx.getEffect(bof).getY() - 100;
                }
                bx = SkillFactory.getSkill(4121014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }

                bx = SkillFactory.getSkill(4120043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4111015, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4120044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(4111015, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(4120045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4111015, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(4120048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(4121015, bx.getEffect(bof).getCooltimeReduceR());
                }
                bx = SkillFactory.getSkill(4120049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4121013, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4120050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_boss_damage_rate += bx.getEffect(bof).getBossDamage();
                }
                bx = SkillFactory.getSkill(4120051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4121013, bx.getEffect(bof).getBulletCount());
                }
                break;
            case 420:
            case 421:
            case 422:
                bx = SkillFactory.getSkill(4200007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localluk += bx.getEffect(bof).getLukX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(4210013);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                    this.ASR += bx.getEffect(bof).getASRRate();
                    this.TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(4200010);
                bof = chra.getTotalSkillLevel(bx);
                //    Item shield = chra.getInventory(MapleInventoryType.EQUIPPED).getItem(-10);
                if ((bof > 0) && (shield != null)) {
                    this.percent_wdef += bx.getEffect(bof).getX();
                    this.percent_mdef += bx.getEffect(bof).getX();
                    this.dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(4221013);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(4221007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4201012, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(侠客.神通术, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4211002, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4211011, bx.getEffect(bof).getDAMRate());
                }

                bx = SkillFactory.getSkill(4220043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4211006, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4220044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(4211006, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(4220046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4221007, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4220047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(4221007, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(4220048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4221007, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(4220049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4221014, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4220050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4221014, bx.getEffect(bof).getAttackCount());
                }
                break;
            case 431:
            case 432:
            case 433:
            case 434:
                bx = SkillFactory.getSkill(4310006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localluk += bx.getEffect(bof).getLukX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(4330007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.hpRecoverProp += bx.getEffect(bof).getProb();
                    this.hpRecoverPercent += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(4330008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                    this.ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(4330009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(4341006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_wdef += bx.getEffect(bof).getWDEFRate();
                    this.percent_mdef += bx.getEffect(bof).getMDEFRate();
                    this.dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(4341002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4311002, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4311003, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4301004, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4331000, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4321004, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(4321006, bx.getEffect(bof).getDAMRate());
                }

                bx = SkillFactory.getSkill(4340043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4331000, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4340044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(4331000, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(4340045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4331000, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(4340046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4341009, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4340047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addIgnoreMobpdpRate(4341009, bx.getEffect(bof).getIgnoreMob());
                }
                bx = SkillFactory.getSkill(4340048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(4341009, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(4340049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(4341011, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(4340051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addCoolTimeReduce(4341011, bx.getEffect(bof).getCooltimeReduceR());
                }
                break;
            case 510:
            case 511:
            case 512:
                bx = SkillFactory.getSkill(5100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(5100010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(5121015);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(5120014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_ignore_mob_def_rate += bx.getEffect(bof).getX();
                }
                double energyrate = 1.0D;
                if (chra.getTotalSkillLevel(5120018) > 0) {
                    bx = SkillFactory.getSkill(5120018);
                    bof = chra.getTotalSkillLevel(bx);
                    this.watk = (int) (this.watk + bx.getEffect(bof).getWatk() * energyrate);
                    this.wdef = (int) (this.wdef + bx.getEffect(bof).getEnhancedWdef() * energyrate);
                    this.mdef = (int) (this.mdef + bx.getEffect(bof).getEnhancedMdef() * energyrate);
                    this.speed = (int) (this.speed + bx.getEffect(bof).getSpeed() * energyrate);
                    this.accuracy = (int) (this.accuracy + bx.getEffect(bof).getAcc() * energyrate);
                } else if (chra.getTotalSkillLevel(5110014) > 0) {
                    bx = SkillFactory.getSkill(5110014);
                    bof = chra.getTotalSkillLevel(bx);
                    this.watk = (int) (this.watk + bx.getEffect(bof).getWatk() * energyrate);
                    this.wdef = (int) (this.wdef + bx.getEffect(bof).getEnhancedWdef() * energyrate);
                    this.mdef = (int) (this.mdef + bx.getEffect(bof).getEnhancedMdef() * energyrate);
                    this.speed = (int) (this.speed + bx.getEffect(bof).getSpeed() * energyrate);
                    this.accuracy = (int) (this.accuracy + bx.getEffect(bof).getAcc() * energyrate);
                } else if (chra.getTotalSkillLevel(5100015) > 0) {
                    bx = SkillFactory.getSkill(5100015);
                    bof = chra.getTotalSkillLevel(bx);
                    this.wdef = (int) (this.wdef + bx.getEffect(bof).getEnhancedWdef() * energyrate);
                    this.mdef = (int) (this.mdef + bx.getEffect(bof).getEnhancedMdef() * energyrate);
                    this.speed = (int) (this.speed + bx.getEffect(bof).getSpeed() * energyrate);
                    this.accuracy = (int) (this.accuracy + bx.getEffect(bof).getAcc() * energyrate);
                }

                bx = SkillFactory.getSkill(5121054);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_boss_damage_rate += bx.getEffect(bof).getBossDamage();
                }

                bx = SkillFactory.getSkill(5120046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(5121007, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(5121020, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5120047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addBossDamageRate(5121007, bx.getEffect(bof).getBossDamage());
                    addBossDamageRate(5121020, bx.getEffect(bof).getBossDamage());
                }
                bx = SkillFactory.getSkill(5120048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(5121007, bx.getEffect(bof).getAttackCount());
                    addAttackCount(5121020, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(5120049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(5121016, bx.getEffect(bof).getDAMRate());
                    addDamageIncrease(5121017, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5120050);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(5121016, bx.getEffect(bof).getTargetPlus());
                    addTargetPlus(5121017, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(5120051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(5121016, bx.getEffect(bof).getAttackCount());
                    addAttackCount(5121017, bx.getEffect(bof).getAttackCount());
                }
                break;
            case 520:
            case 521:
            case 522:
                bx = SkillFactory.getSkill(5200009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                    this.localdex += bx.getEffect(bof).getDexX();
                }

                bx = SkillFactory.getSkill(5220043);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(5211008, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5220044);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(5211008, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(5220045);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(5211008, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(5220046);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(5221017, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5220047);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addTargetPlus(5221017, bx.getEffect(bof).getTargetPlus());
                }
                bx = SkillFactory.getSkill(5220048);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addAttackCount(5221017, bx.getEffect(bof).getAttackCount());
                }
                bx = SkillFactory.getSkill(5220049);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    addDamageIncrease(5221004, bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5220051);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_boss_damage_rate += bx.getEffect(bof).getBossDamage();
                }
                break;
        }

        switch (chra.getJob()) {
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
            case 434:
                bx = SkillFactory.getSkill(getHyperSkillByJob(30, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localstr += bx.getEffect(bof).getStrX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(31, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localdex += bx.getEffect(bof).getDexX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(32, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(33, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.localluk += bx.getEffect(bof).getLukX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(34, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + bx.getEffect(bof).getCritical());
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(35, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                bx = SkillFactory.getSkill(getHyperSkillByJob(36, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(37, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_mp += bx.getEffect(bof).getPercentMP();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(38, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.incMaxDF = bx.getEffect(bof).getIndieMaxDF();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(39, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_wdef += bx.getEffect(bof).getWdefX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(40, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.percent_mdef += bx.getEffect(bof).getMdefX();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(41, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.speed += bx.getEffect(bof).getPassiveSpeed();
                }
                bx = SkillFactory.getSkill(getHyperSkillByJob(42, chra.getJob()));
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    this.jump += bx.getEffect(bof).getPassiveJump();
                }

        }
    }

    private void handleBuffStats(MapleCharacter chra) {//TODO 添加处理BUFF效果

        Integer buff = chra.getBuffedValue(MapleBuffStat.物理攻击力);
        if (buff != null) {
            this.watk += buff;
        }
        MapleStatEffect effect = chra.getStatForBuff(MapleBuffStat.伤害增加);
        if ((effect != null) && (effect.getSourceId() == 31121054)) {
            this.mpconReduce += 20;
        }

        buff = chra.getBuffedValue(MapleBuffStat.增加物理防御);
        if (buff != null) {
            this.wdef += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.魔法防御力);
        if (buff != null) {
            this.mdef += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.魔法攻击力);
        if (buff != null) {
            this.magic += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.命中率);
        if (buff != null) {
            this.accuracy += buff;
        }

        buff = chra.getBuffedSkill_Y(MapleBuffStat.隐身术);
        if (buff != null) {
            this.percent_damage_rate += buff;
            this.percent_boss_damage_rate += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.移动速度);
        if (buff != null) {
            this.speed += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.跳跃力);
        if (buff != null) {
            this.jump += buff;
        }

        effect = chra.getStatForBuff(MapleBuffStat.神圣之火_最大体力百分比);
        if (effect != null) {
            this.percent_hp += effect.getX();
        }
        effect = chra.getStatForBuff(MapleBuffStat.神圣之火_最大魔力百分比);
        if (effect != null) {
            this.percent_mp += effect.getY();
        }

        buff = chra.getBuffedValue(MapleBuffStat.斗气集中);
        if (buff != null) {
            Skill combos = SkillFactory.getSkill(1110013);
            int comboslevel = chra.getTotalSkillLevel(combos);
            if (comboslevel > 0) {
                effect = combos.getEffect(comboslevel);
                this.percent_damage_rate += buff * effect.getX();
                this.percent_boss_damage_rate += buff * effect.getX();
            }
        }
        effect = chra.getStatForBuff(MapleBuffStat.召唤兽);
        if ((effect != null)) {
            this.percent_damage_rate += effect.getX();
            this.percent_boss_damage_rate += effect.getX();
        }

        buff = chra.getBuffedValue(MapleBuffStat.聚财术);
        if (buff != null) {
            this.mesoBuff *= buff.doubleValue() / 100.0D;
        }
        effect = chra.getStatForBuff(MapleBuffStat.敛财术);
        if (effect != null) {
            this.pickRate = effect.getProb();
        }
        effect = chra.getStatForBuff(MapleBuffStat.金钱护盾);
        if (effect != null) {
            this.mesoGuard = (this.mesoGuard * effect.getX())/100.0D;
            this.mesoGuardMeso = effect.getMoneyCon();
        }

        buff = chra.getBuffedValue(MapleBuffStat.神圣祈祷);
        if (buff != null) {
            this.expBuff *= 1.0D + (buff.doubleValue() / 100.0D);
        }

        effect = chra.getStatForBuff(MapleBuffStat.祝福护甲);
        if (effect != null) {
            this.watk += effect.getEnhancedWatk();
        }
        effect = chra.getStatForBuff(MapleBuffStat.反制攻击);
        if (effect != null) {
            switch (effect.getSourceId()) {
                case 5120011:
                case 5220012:
                case 5720012:
                    this.percent_damage_rate += effect.getIndieDamR();
                    this.percent_boss_damage_rate += effect.getIndieDamR();
                    break;
                case 5121015:
                    this.percent_damage_rate += effect.getX();
                    this.percent_boss_damage_rate += effect.getX();
                    break;
                default:
                    this.percent_damage_rate += effect.getDAMRate();
                    this.percent_boss_damage_rate += effect.getDAMRate();
            }
        }

        effect = chra.getStatForBuff(MapleBuffStat.牧师祝福);
        if (effect != null) {
            this.watk += effect.getX();
            this.magic += effect.getY();
            this.accuracy += effect.getV();
        }

        buff = chra.getBuffedValue(MapleBuffStat.攻击力增加);
        if (buff != null) {
            this.watk += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.魔法攻击力增加);
        if (buff != null) {
            this.magic += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.命中值增加);
        if (buff != null) {
            this.accuracy += buff;
        }

        buff = chra.getBuffedValue(MapleBuffStat.增加物理防御);
        if (buff != null) {
            this.wdef += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.魔法防御增加);
        if (buff != null) {
            this.mdef += buff;
        }

        buff = chra.getBuffedValue(MapleBuffStat.最大体力百分比);
        if (buff != null) {
            this.percent_hp += buff;
        }

        buff = chra.getBuffedValue(MapleBuffStat.最大魔力百分比);
        if (buff != null) {
            this.percent_mp += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.百分比无视防御);
        if (buff != null) {
            this.percent_ignore_mob_def_rate += buff;
        }


        buff = chra.getBuffedValue(MapleBuffStat.伤害增加);
        if (buff != null) {
            this.percent_damage_rate += buff;
            this.percent_boss_damage_rate += buff;
        }


//        buff = chra.getBuffedValue(MapleBuffStat.骑兽技能);
//        if (buff != null) {
//            this.jump = 120;
//            switch (buff) {
//                case 1:
//                    this.speed = 150;
//                    break;
//                case 2:
//                    this.speed = 170;
//                    break;
//                case 3:
//                    this.speed = 180;
//                    break;
//                default:
//                    this.speed = 200;
//            }
//        }
    }

    public boolean checkEquipLevels(MapleCharacter chr, int gain) {
        boolean changed = false;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Equip> all = new ArrayList(this.equipLevelHandling);
        for (Equip eq : all) {
            int lvlz = eq.getEquipLevel();
            if (eq.getEquipLevel() > lvlz) {
                Iterator i$;
                for (int i = eq.getEquipLevel() - lvlz; i > 0; i--) {
                    Map inc = ii.getEquipIncrements(eq.getItemId());
                    if ((inc != null) && (inc.containsKey(lvlz + i))) {
                        eq = ii.levelUpEquip(eq, (Map) inc.get(lvlz + i));
                    }

                }
                changed = true;
            }
            chr.forceUpdateItem(eq.copy());
        }
        if (changed) {
            chr.equipChanged();
            chr.getClient().getSession().write(MaplePacketCreator.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }

    private void CalcPassive_Mastery(MapleCharacter player) {
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11) == null) {
            this.passive_mastery = 0;
            return;
        }

        MapleWeaponType weaponType = ItemConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11).getItemId());
        boolean acc = true;
        int skil;
        switch (weaponType) {
            case 弓:
                skil =  3100000;
                break;
            case 手杖:
                skil = player.getTotalSkillLevel(24120006) > 0 ? 24120006 : 24100004;
                break;
            case 短刀:
                skil = (player.getJob() >= 430) && (player.getJob() <= 434) ? 4300000 : 4200000;
                break;
            case 弩:
                skil =  3200000;
                break;
            case 单手剑:
            case 单手钝器:
                skil = (player.getJob() >= 110) && (player.getJob() <= 112) ? 1100000 : 1200000;
                break;
            case 双手剑:
            case 单手斧:
            case 双手斧:
            case 双手钝器:
                skil = (player.getJob() >= 110) && (player.getJob() <= 112) ? 1100000 :  1200000;
                break;
            case 枪:
                skil = 1300000;
                break;
            case 矛:
                skil = 1300000;
                break;
            case 指节:
                skil =  5100001;
                break;
            case 长杖:
            case 短杖:
                acc = false;
                skil = player.getJob() <= 2000 ? 12100007 : player.getJob() <= 232 ? 2300006 : player.getJob() <= 222 ? 2200006 : player.getJob() <= 212 ? 2100006 : 22120002;
                break;
            case 大剑:
                skil = 101000103;
                break;
            case 太刀:
                skil = 101000203;
                break;
            default:
                this.passive_mastery = 0;
                return;
        }
        if (player.getSkillLevel(skil) <= 0) {
            this.passive_mastery = 0;
            return;
        }

        MapleStatEffect eff = SkillFactory.getSkill(skil).getEffect(player.getTotalSkillLevel(skil));
        if (acc) {
            this.accuracy += eff.getX();
        } else {
            this.magic += eff.getX();
        }
        this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + eff.getCritical());
        this.passive_mastery = (byte) eff.getMastery();
        this.trueMastery += eff.getMastery() + weaponType.getBaseMastery();
        if (player.getJob() == 132) {
            Skill bx = SkillFactory.getSkill(1320018);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.watk += eff2.getAttackX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + eff.getCriticalMin());
            }
        } else if ((player.getJob() == 231) || (player.getJob() == 232)) {
            Skill bx = SkillFactory.getSkill(2310008);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + eff2.getCritical());
                this.percent_acc += eff2.getArRate();
            }
        } else if (player.getJob() == 312) {
            Skill bx = SkillFactory.getSkill(3120005);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + eff.getCriticalMin());
            }
        } else if (player.getJob() == 322) {
            Skill bx = SkillFactory.getSkill(3220004);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + eff.getCriticalMin());
            }
        } else if (player.getJob() == 412) {
            Skill bx = SkillFactory.getSkill(4120012);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.accuracy += eff2.getPercentAcc();
                this.dodgeChance += eff2.getPercentAvoid();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        } else if (player.getJob() == 422) {
            Skill bx = SkillFactory.getSkill(4220012);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.accuracy += eff2.getPercentAcc();
                this.dodgeChance += eff2.getPercentAvoid();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        } else if (player.getJob() == 434) {
            Skill bx = SkillFactory.getSkill(4340013);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.accuracy += eff2.getPercentAcc();
                this.dodgeChance += eff2.getPercentAvoid();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        } else if (player.getJob() == 512) {
            Skill bx = SkillFactory.getSkill(5121015);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        } else if (player.getJob() == 522) {
            Skill bx = SkillFactory.getSkill(5220020);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = (byte) eff2.getMastery();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        }
    }

    private void CalcPassive_SharpEye(MapleCharacter player) {
        Skill critSkill;
        int critlevel;
        switch (player.getJob()) {
            case 410:
            case 411:
            case 412:
                critSkill = SkillFactory.getSkill(4100001);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 434:
                critSkill = SkillFactory.getSkill(4340010);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 211:
            case 212:
                critSkill = SkillFactory.getSkill(2110009);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 221:
            case 222:
                critSkill = SkillFactory.getSkill(2210009);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 231:
            case 232:
                critSkill = SkillFactory.getSkill(2310010);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 530:
            case 531:
            case 532:
                critSkill = SkillFactory.getSkill(5300004);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
                critSkill = SkillFactory.getSkill(3000001);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb());
                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                break;
            case 500:
            case 510:
            case 511:
            case 512:
            case 520:
            case 521:
            case 522:
                critSkill = SkillFactory.getSkill(5000007);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                    this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                }
                if ((player.getJob() == 511) || (player.getJob() == 512)) {
                    critSkill = SkillFactory.getSkill(5110011);
                    critlevel = player.getTotalSkillLevel(critSkill);
                    if (critlevel > 0) {
                        this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                        this.passive_sharpeye_max_percent = (short) (this.passive_sharpeye_max_percent + critSkill.getEffect(critlevel).getCriticalMax());
                        this.percent_boss_damage_rate += critSkill.getEffect(critlevel).getProb();
                    }
                    critSkill = SkillFactory.getSkill(5110000);
                    critlevel = player.getTotalSkillLevel(critSkill);
                    if (critlevel > 0) {
                        this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb());
                        this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin());
                    }
                }
                if ((player.getJob() != 521) && (player.getJob() != 522)) {
                    break;
                }
                critSkill = SkillFactory.getSkill(5210013);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel <= 0) {
                    break;
                }
                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCritical());
                this.percent_ignore_mob_def_rate += critSkill.getEffect(critlevel).getIgnoreMob();
        }
    }

    public short passive_sharpeye_rate() {
        return this.passive_sharpeye_rate;
    }

    public short passive_sharpeye_min_percent() {
        return this.passive_sharpeye_min_percent;
    }

    public short passive_sharpeye_percent() {
        return this.passive_sharpeye_max_percent;
    }

    public byte passive_mastery() {
        return this.passive_mastery;
    }

    public double calculateMaxProjDamage(int projectileWatk, MapleCharacter chra) {
        if (projectileWatk < 0) {
            return 0.0D;
        }
        Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.没有武器 : ItemConstants.getWeaponType(weapon_item.getItemId());
        int mainstat;
        int secondarystat;
        switch (weapon) {
            case 单手钝器:
            case 枪:
                mainstat = this.localdex;
                secondarystat = this.localstr;
                break;
            default:
                mainstat = 0;
                secondarystat = 0;
        }

        float maxProjDamage = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (projectileWatk / 100.0F);
        maxProjDamage = (float) (maxProjDamage + maxProjDamage * (this.percent_damage / 100.0D));
        return maxProjDamage;
    }

    public void calculateMaxBaseDamage(int watk, int lv2damX, int pvpDamage, MapleCharacter chra) {
        if (watk <= 0) {
            this.localmaxbasedamage = 1.0F;
            this.localmaxbasepvpdamage = 1.0F;
        } else {
            Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            Item weapon_item2 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
            int job = chra.getJob();
            MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.没有武器 : ItemConstants.getWeaponType(weapon_item.getItemId());
            MapleWeaponType weapon2 = weapon_item2 == null ? MapleWeaponType.没有武器 : ItemConstants.getWeaponType(weapon_item2.getItemId());
            int thirdstat = 0;
            int thirdstatpvp = 0;
            boolean mage = ((job >= 200) && (job <= 232)) || ((job >= 1200) && (job <= 1212)) || ((job >= 2200) && (job <= 2218)) || ((job >= 2700) && (job <= 2712)) || ((job >= 3200) && (job <= 3212) || ((job >= 11000) && (job <= 11212)));
            int mainstat;
            int secondarystat;
            int mainstatpvp;
            int secondarystatpvp;
            switch (weapon) {
                case 弓:
                case 弩:
                case 指节:
                    mainstat = this.localdex;
                    secondarystat = this.localstr;
                    mainstatpvp = this.dex;
                    secondarystatpvp = this.str;
                    break;
                case 短刀:
                case 手杖:
                case 拳套:
                    mainstat = this.localluk;
                    secondarystat = this.localdex + this.localstr;
                    mainstatpvp = this.luk;
                    secondarystatpvp = this.dex + this.str;
                    break;
                default:
                    if (mage) {
                        mainstat = this.localint_;
                        secondarystat = this.localluk;
                        mainstatpvp = this.int_;
                        secondarystatpvp = this.luk;
                    } else {
                        mainstat = this.localstr;
                        secondarystat = this.localdex;
                        mainstatpvp = this.str;
                        secondarystatpvp = this.dex;
                    }
            }

            if (GameConstants.is新手职业(job)) {
                mainstat = this.localstr;
                secondarystat = this.localdex;
                mainstatpvp = this.str;
                secondarystatpvp = this.dex;
            }
            float weaponDamageMultiplier = weapon.getMaxDamageMultiplier();
            this.localmaxbasepvpdamage = (weaponDamageMultiplier * (4 * mainstatpvp + secondarystatpvp) * (100.0F + pvpDamage / 100.0F) + lv2damX);
            this.localmaxbasepvpdamageL = (weaponDamageMultiplier * (4 * mainstat + secondarystat) * (100.0F + pvpDamage / 100.0F) + lv2damX);
            if ((weapon2 != MapleWeaponType.没有武器) && (weapon_item != null) && (weapon_item2 != null) ) {
                Equip we1 = (Equip) weapon_item;
                Equip we2 = (Equip) weapon_item2;
                int watk2 = mage ? we2.getMatk() : we2.getWatk();
                this.localmaxbasedamage = (weaponDamageMultiplier * (4 * mainstat + secondarystat) * ((watk - watk2) / 100.0F) + lv2damX);
            } else {
                if ((job == 110) || (job == 111) || (job == 112)) {
                    weaponDamageMultiplier = (float) (weaponDamageMultiplier + 0.1D);
                }
                this.localmaxbasedamage = (weaponDamageMultiplier * (4 * mainstat + secondarystat) * (watk / 100.0F) + lv2damX);
            }

            if (ServerProperties.ShowPacket()) {
                System.err.println("当前攻击: " + this.localmaxbasedamage + " 攻击加成: " + this.localmaxbasedamage * (this.percent_damage / 100.0D));
            }
            this.localmaxbasedamage = (float) (this.localmaxbasedamage + this.localmaxbasedamage * ((this.percent_damage + this.percent_damage_rate + percent_boss_damage_rate) / 100.0D));
            if (ServerProperties.ShowPacket()) {
                System.err.println("武器类型: " + weapon + " 攻击力: " + watk + " indieDamR: " + this.percent_damage / 100.0D);
                System.err.println("武器加成: " + weaponDamageMultiplier + " 主要属性: " + mainstat + " 次要属性: " + secondarystat + " 第三属性: " + thirdstat + " 攻击力加成: " + watk / 100.0F);
                System.err.println("最终攻击: " + this.localmaxbasedamage);
            }
        }
    }

    public float getHealHP() {
        return this.shouldHealHP;
    }

    public float getHealMP() {
        return this.shouldHealMP;
    }

    public void relocHeal(MapleCharacter chra) {
        int playerjob = chra.getJob();

        this.shouldHealHP = (10 + this.recoverHP);
        this.shouldHealMP = (3 + this.mpRestore + this.recoverMP + this.localint_ / 10);
        this.mpRecoverTime = 0;
        this.hpRecoverTime = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((playerjob == 111) || (playerjob == 112)) {
            Skill effect = SkillFactory.getSkill(1110000);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                MapleStatEffect eff = effect.getEffect(lvl);
                this.shouldHealHP += eff.getHp();
                this.hpRecoverTime = 4000;
                this.shouldHealMP += eff.getMp();
                this.mpRecoverTime = 4000;
            }
        }
        if (chra.getChair() != 0) { // Is sitting on a chair.
            shouldHealHP += 99; // Until the values of Chair heal has been fixed,
            shouldHealMP += 99; // MP is different here, if chair data MP = 0, heal + 1.5
        } else if (chra.getMap() != null) { // Because Heal isn't multipled when there's a chair :)
            final float recvRate = chra.getMap().getRecoveryRate();
            if (recvRate > 0) {
                shouldHealHP *= recvRate;
                shouldHealMP *= recvRate;
            }
        }
//        if (chra.getChair() != 0) {
//            Pair ret = ii.getChairRecovery(chra.getChair());
//            this.shouldHealHP += ((Integer) ret.getLeft());
//            if (this.hpRecoverTime == 0) {
//            this.hpRecoverTime = 4000;
//            }
//            this.shouldHealMP += ((Integer) ret.getRight());
//            if ((this.mpRecoverTime == 0)) {
//            this.hpRecoverTime = 4000;
//            }
//            } else if (chra.getMap() != null) {
//            float recvRate = chra.getMap().getRecoveryRate();
//            if (recvRate > 0.0F) {
//            this.shouldHealHP *= recvRate;
//            this.shouldHealMP *= recvRate;
//            }
//         }
    }

    public static int getSkillByJob(int skillId, int job) {
        return skillId;
    }

    public static int getHyperSkillByJob(int skillId, int job) {
        switch (job) {
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
            case 434:
            case 512:
            case 522:
            case 532:
                return job * 10000 + skillId;
        }
        return skillId;
    }

    public int getSkillIncrement(int skillID) {
        if (this.skillsIncrement.containsKey(skillID)) {
            return (this.skillsIncrement.get(skillID));
        }
        return 0;
    }

    public int getElementBoost(Element key) {
        if (this.elemBoosts.containsKey(key)) {
            return (this.elemBoosts.get(key));
        }
        return 0;
    }

    public int getDamageIncrease(int key) {
        if (this.damageIncrease.containsKey(key)) {
            return (this.damageIncrease.get(key));
        }
        return 0;
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public void heal_noUpdate(MapleCharacter chra) {
        setHp(getCurrentMaxHp());
        setMp(getCurrentMaxMp());
    }

    public void heal(MapleCharacter chra) {
        heal_noUpdate(chra);
        chra.updateSingleStat(MapleStat.HP, getCurrentMaxHp());
        chra.updateSingleStat(MapleStat.MP, getCurrentMaxMp());
    }

    public Pair<Integer, Integer> handleEquipAdditions(MapleItemInformationProvider ii, MapleCharacter chra, boolean first_login, Map<Skill, SkillEntry> sData, int itemId) {
        List<Triple<String, String, String>> additions = ii.getEquipAdditions(itemId);
        if (additions == null) {
            return null;
        }
        int localmaxhp_x = 0;
        int localmaxmp_x = 0;
        int skillid = 0;
        int skilllevel = 0;

        for (Triple add : additions) {
            if (((String) add.getMid()).contains("con")) {
                continue;
            }
            int right = ((String) add.getMid()).equals("elemVol") ? 0 : Integer.parseInt((String) add.getRight());
            switch ((String) add.getLeft()) {
                case "elemboost": {
                    String craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                    if ((((String) add.getMid()).equals("elemVol")) && ((craft == null) )) {
                        int value = Integer.parseInt(((String) add.getRight()).substring(1, ((String) add.getRight()).length()));
                        Element key = Element.getFromChar(((String) add.getRight()).charAt(0));
                        if (this.elemBoosts.get(key) != null) {
                            value += (this.elemBoosts.get(key));
                        }
                        this.elemBoosts.put(key, value);
                    }
                    break;
                }
                case "mobcategory":
                    if (((String) add.getMid()).equals("damage")) {
                        this.percent_damage_rate += right;
                        this.percent_boss_damage_rate += right;
                    }
                    break;
                case "critical": {
                    boolean canJob = false;
                    boolean canLevel = false;
                    String job = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "job");
                    if (job != null) {
                        if (job.contains(",")) {
                            String[] jobs = job.split(",");
                            for (String x : jobs) {
                                if (chra.getJob() == Integer.parseInt(x)) {
                                    canJob = true;
                                }
                            }
                        } else if (chra.getJob() == Integer.parseInt(job)) {
                            canJob = true;
                        }
                    }
                    String level = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "level");
                    if ((level != null)
                            && (chra.getLevel() >= Integer.parseInt(level))) {
                        canLevel = true;
                    }
                    if (((job != null) && (canJob)) || ((job == null) && (((level != null) && (canLevel)) || (level == null)))) {
                        switch ((String) add.getMid()) {
                            case "prob":
                                this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + right);
                                break;
                            case "damage":
                                this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + right);
                                this.passive_sharpeye_max_percent = (short) (this.passive_sharpeye_max_percent + right);
                                break;
                        }
                    }
                    break;
                }
                case "boss": {
                    String craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                    if ((((String) add.getMid()).equals("damage")) && ((craft == null))) {
                        this.percent_boss_damage_rate += right;
                    }
                    break;
                }
                case "mobdie": {
                    String craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                    if ((craft == null)) {
                        switch ((String) add.getMid()) {
                            case "hpIncOnMobDie":
                                this.hpRecover += right;
                                this.hpRecoverProp += 5;
                                break;
                            case "mpIncOnMobDie":
                                this.mpRecover += right;
                                this.mpRecoverProp += 5;
                                break;
                        }
                    }
                    break;
                }
                case "skill":
                    if (first_login) {
                        String craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                        if ((craft == null)) {
                            switch ((String) add.getMid()) {
                                case "id":
                                    skillid = right;
                                    break;
                                case "level":
                                    skilllevel = right;
                                    break;
                            }
                        }
                    }
                    break;
                case "hpmpchange":
                    switch ((String) add.getMid()) {
                        case "hpChangerPerTime":
                            this.recoverHP += right;
                            break;
                        case "mpChangerPerTime":
                            this.recoverMP += right;
                            break;
                    }
                    break;
                case "statinc": {
                    boolean canJobx = false;
                    boolean canLevelx = false;
                    String job = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "job");
                    if (job != null) {
                        if (job.contains(",")) {
                            String[] jobs = job.split(",");
                            for (String x : jobs) {
                                if (chra.getJob() == Integer.parseInt(x)) {
                                    canJobx = true;
                                }
                            }
                        } else if (chra.getJob() == Integer.parseInt(job)) {
                            canJobx = true;
                        }
                    }
                    String level = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "level");
                    if ((level != null) && (chra.getLevel() >= Integer.parseInt(level))) {
                        canLevelx = true;
                    }
                    if (((!canJobx) && (job != null)) || ((!canLevelx) && (level != null))) {
                        continue;
                    }
                    if (itemId == 1142367) {
                        int day = Calendar.getInstance().get(7);
                        if ((day != 1) && (day != 7)) {
                            continue;
                        }
                    }
                    if (((String) add.getMid()).equals("incPAD")) {
                        this.watk += right;
                    } else if (((String) add.getMid()).equals("incMAD")) {
                        this.magic += right;
                    } else if (((String) add.getMid()).equals("incSTR")) {
                        this.localstr += right;
                    } else if (((String) add.getMid()).equals("incDEX")) {
                        this.localdex += right;
                    } else if (((String) add.getMid()).equals("incINT")) {
                        this.localint_ += right;
                    } else if (((String) add.getMid()).equals("incLUK")) {
                        this.localluk += right;
                    } else if (((String) add.getMid()).equals("incJump")) {
                        this.jump += right;
                    } else if (((String) add.getMid()).equals("incMHP")) {
                        localmaxhp_x += right;
                    } else if (((String) add.getMid()).equals("incMMP")) {
                        localmaxmp_x += right;
                    } else if (((String) add.getMid()).equals("incPDD")) {
                        this.wdef += right;
                    } else if (((String) add.getMid()).equals("incMDD")) {
                        this.mdef += right;
                    } else if (((String) add.getMid()).equals("incACC")) {
                        this.accuracy += right;
                    } else if (!((String) add.getMid()).equals("incEVA")) {
                        switch ((String) add.getMid()) {
                            case "incSpeed":
                                this.speed += right;
                                break;
                            case "incMMPr":
                                this.percent_mp += right;
                                break;
                        }
                    }
                    break;
                }
            }
        }
        if ((skillid != 0) && (skilllevel != 0)) {
            sData.put(SkillFactory.getSkill(skillid), new SkillEntry((byte) skilllevel, (byte) 0, -1L));
        }
        return new Pair(localmaxhp_x, localmaxmp_x);
    }

    public void handleItemOption(StructItemOption soc, MapleCharacter chra, boolean first_login, Map<Skill, SkillEntry> sData) {
        this.localstr += soc.get("incSTR");
        this.localdex += soc.get("incDEX");
        this.localint_ += soc.get("incINT");
        this.localluk += soc.get("incLUK");
        if (soc.get("incSTRlv") > 0) {
            this.localstr += chra.getLevel() / 10 * soc.get("incSTRlv");
        }
        if (soc.get("incDEXlv") > 0) {
            this.localdex += chra.getLevel() / 10 * soc.get("incDEXlv");
        }
        if (soc.get("incINTlv") > 0) {
            this.localint_ += chra.getLevel() / 10 * soc.get("incINTlv");
        }
        if (soc.get("incLUKlv") > 0) {
            this.localluk += chra.getLevel() / 10 * soc.get("incLUKlv");
        }
        this.accuracy += soc.get("incACC");

        this.speed += soc.get("incSpeed");
        this.jump += soc.get("incJump");
        this.watk += soc.get("incPAD");
        if (soc.get("incPADlv") > 0) {
            this.watk += chra.getLevel() / 10 * soc.get("incPADlv");
        }
        this.magic += soc.get("incMAD");
        if (soc.get("incMADlv") > 0) {
            this.magic += chra.getLevel() / 10 * soc.get("incMADlv");
        }
        this.wdef += soc.get("incPDD");
        this.mdef += soc.get("incMDD");
        this.percent_str += soc.get("incSTRr");
        this.percent_dex += soc.get("incDEXr");
        this.percent_int += soc.get("incINTr");
        this.percent_luk += soc.get("incLUKr");
        this.percent_hp += soc.get("incMHPr");
        this.percent_mp += soc.get("incMMPr");
        this.percent_acc += soc.get("incACCr");
        this.dodgeChance += soc.get("incEVAr");
        this.percent_atk += soc.get("incPADr");
        this.percent_matk += soc.get("incMADr");
        this.percent_wdef += soc.get("incPDDr");
        this.percent_mdef += soc.get("incMDDr");
        this.passive_sharpeye_rate = (short) (this.passive_sharpeye_rate + soc.get("incCr"));
        this.percent_boss_damage_rate += soc.get("incDAMr");
        if (soc.get("boss") <= 0) {
            this.percent_damage_rate += soc.get("incDAMr");
        }
        this.recoverHP += soc.get("RecoveryHP");
        this.recoverMP += soc.get("RecoveryMP");
        if (soc.get("HP") > 0) {
            this.hpRecover += soc.get("HP");
            this.hpRecoverProp += soc.get("prop");
        }
        if ((soc.get("MP") > 0) ) {
            this.mpRecover += soc.get("MP");
            this.mpRecoverProp += soc.get("prop");
        }
        this.percent_ignore_mob_def_rate += soc.get("ignoreTargetDEF");
        if (soc.get("ignoreDAM") > 0) {
            this.ignoreDAM += soc.get("ignoreDAM");
            this.ignoreDAM_rate += soc.get("prop");
        }
        this.incAllskill += soc.get("incAllskill");
        if (soc.get("ignoreDAMr") > 0) {
            this.ignoreDAMr += soc.get("ignoreDAMr");
            this.ignoreDAMr_rate += soc.get("prop");
        }
        if (soc.get("incMaxDamage") > 0) {
            this.incMaxDamage += soc.get("incMaxDamage");
        }
        this.RecoveryUP += soc.get("RecoveryUP");
        this.passive_sharpeye_min_percent = (short) (this.passive_sharpeye_min_percent + soc.get("incCriticaldamageMin"));
        this.passive_sharpeye_max_percent = (short) (this.passive_sharpeye_max_percent + soc.get("incCriticaldamageMax"));
        this.TER += soc.get("incTerR");
        this.ASR += soc.get("incAsrR");
        if (soc.get("DAMreflect") > 0) {
            this.DAMreflect += soc.get("DAMreflect");
            this.DAMreflect_rate += soc.get("prop");
        }
        this.mpconReduce += soc.get("mpconReduce");
        this.reduceCooltime += soc.get("reduceCooltime");
        this.incMesoProp += soc.get("incMesoProp");
        this.incRewardProp += soc.get("incRewardProp");
        if ((first_login) && (soc.get("skillID") > 0)) {
            sData.put(SkillFactory.getSkill(getSkillByJob(soc.get("skillID"), chra.getJob())), new SkillEntry(1, (byte) 0, -1L));
        }
    }

    public int getHPPercent() {
        return (int) Math.ceil(this.baseHp * 100.0D / this.localmaxhp);
    }

    public int getMPPercent() {
        return (int) Math.ceil(this.baseMp * 100.0D / this.localmaxmp);
    }

    public void init(MapleCharacter chra) {
        recalcLocalStats(chra);
    }

    public short getStr() {
        return this.str;
    }

    public short getDex() {
        return this.dex;
    }

    public short getLuk() {
        return this.luk;
    }

    public short getInt() {
        return this.int_;
    }

    public void setStr(short str, MapleCharacter chra) {
        this.str = str;
        recalcLocalStats(chra);
    }

    public void setDex(short dex, MapleCharacter chra) {
        this.dex = dex;
        recalcLocalStats(chra);
    }

    public void setLuk(short luk, MapleCharacter chra) {
        this.luk = luk;
        recalcLocalStats(chra);
    }

    public void setInt(short int_, MapleCharacter chra) {
        this.int_ = int_;
        recalcLocalStats(chra);
    }

    public int getHealHp() {
        return Math.max(this.localmaxhp - this.baseHp, 0);
    }

    public int getHealMp(int job) {
        return Math.max(this.localmaxmp - this.baseMp, 0);
    }

    public boolean setHp(int newhp) {
        if (newhp < 0 ) {
            newhp = 0;
        }
        if (newhp > getCurrentMaxHp()) {
            newhp = getCurrentMaxHp();
        }
        this.baseHp=newhp;
        return true;
    }

    public boolean setMp(int newmp) {
        if (newmp < 0 ) {
            newmp = 0;
        }
        if (newmp > getCurrentMaxMp()) {
            newmp = getCurrentMaxMp();
        }
        this.baseMp = newmp;
        return true;
    }

    public void setInfo(int maxhp, int maxmp, int hp, int mp) {
        this.baseMaxHp = maxhp;
        this.baseMaxMp= maxmp;
        this.baseHp = hp;
        this.baseMp = mp;
    }

    public void setMaxHp(int hp, MapleCharacter chra) {
        this.baseMaxHp = hp;
//        recalcLocalStats(chra);
    }

    public void setMaxMp(int mp, MapleCharacter chra) {
        this.baseMaxMp = mp;
//        recalcLocalStats(chra);
    }

    public int getHp() {
        return this.baseHp;
    }

    public int getMaxHp() {
        return this.baseMaxHp;
    }

    public int getMp() {
        return this.baseMp;
    }

    public int getMaxMp() {
        return this.baseMaxMp;
    }

    public int getTotalDex() {
        return this.localdex;
    }

    public int getTotalInt() {
        return this.localint_;
    }

    public int getTotalStr() {
        return this.localstr;
    }

    public int getTotalLuk() {
        return this.localluk;
    }

    public int getTotalMagic() {
        return this.magic;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getJump() {
        return this.jump;
    }

    public int getTotalWatk() {
        return this.watk;
    }

    public int getCurrentMaxHp() {
        return this.localmaxhp;
    }

    public int getCurrentMaxMp() {
        return this.localmaxmp;
    }

    public void setCurrentMaxHp(int localMaxHp){ this.localmaxhp = localMaxHp;}

    public void setCurrentMaxMp(int localMaxMp){ this.localmaxmp = localMaxMp;}

    public int getAsrR() {
        return this.ASR;
    }

    public int getHands() {
        return this.hands;
    }

    public float getCurrentMaxBaseDamage() {
        return this.localmaxbasedamage;
    }

    public float getCurrentMaxBasePVPDamage() {
        return this.localmaxbasepvpdamage;
    }

    public float getCurrentMaxBasePVPDamageL() {
        return this.localmaxbasepvpdamageL;
    }

    public boolean isRangedJob(int job) {
        return (job == 400) || (job / 10 == 52) || (job / 10 == 59) || (job / 100 == 3) || (job / 100 == 13) || (job / 100 == 14) || (job / 100 == 33) || (job / 100 == 35) || (job / 10 == 41);
    }

    public int getCoolTimeR() {
        if (this.coolTimeR > 5) {
            return 5;
        }
        return this.coolTimeR;
    }

    public int getReduceCooltime() {
        if (this.reduceCooltime > 5) {
            return 5;
        }
        return this.reduceCooltime;
    }

    public int getAttackCount(int skillId) {
        if (this.add_skill_attackCount.containsKey(skillId)) {
            return (this.add_skill_attackCount.get(skillId));
        }
        return 0;
    }

    public int getMobCount(int skillId) {
        if (this.add_skill_targetPlus.containsKey(skillId)) {
            return (this.add_skill_targetPlus.get(skillId));
        }
        return 0;
    }

    public int getReduceCooltimeRate(int skillId) {
        if (this.add_skill_coolTimeR.containsKey(skillId)) {
            return (this.add_skill_coolTimeR.get(skillId));
        }
        return 0;
    }

    public int getIgnoreMobpdpR(int skillId) {
        if (this.add_skill_ignoreMobpdpR.containsKey(skillId)) {
            return (this.add_skill_ignoreMobpdpR.get(skillId)) + this.percent_ignore_mob_def_rate;
        }
        return this.percent_ignore_mob_def_rate;
    }

    public int getDamageRate() {
        return this.percent_damage_rate;
    }

    public int getBossDamageRate() {
        return this.percent_boss_damage_rate;
    }

    public int getBossDamageRate(int skillId) {
        if (this.add_skill_bossDamageRate.containsKey(skillId)) {
            return (this.add_skill_bossDamageRate.get(skillId)) + this.percent_boss_damage_rate;
        }
        return this.percent_boss_damage_rate;
    }

    public int getDuration(int skillId) {
        if (this.add_skill_duration.containsKey(skillId)) {
            return (this.add_skill_duration.get(skillId));
        }
        return 0;
    }

    public void addDamageIncrease(int skillId, int val) { //增加伤害 
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.damageIncrease.containsKey(skillId)) {
            int oldval = (this.damageIncrease.get(Integer.valueOf(skillId)));
            this.damageIncrease.put(skillId, oldval + val);
        } else {
            this.damageIncrease.put(skillId, val);
        }
    }

    public void addTargetPlus(int skillId, int val) { //增加攻击目标数
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_targetPlus.containsKey(skillId)) {
            int oldval = (this.add_skill_targetPlus.get(Integer.valueOf(skillId)));
            this.add_skill_targetPlus.put(skillId, oldval + val);
        } else {
            this.add_skill_targetPlus.put(skillId, val);
        }
    }

    public void addAttackCount(int skillId, int val) { //增加攻击次数
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_attackCount.containsKey(skillId)) {
            int oldval = (this.add_skill_attackCount.get(Integer.valueOf(skillId)));
            this.add_skill_attackCount.put(skillId, oldval + val);
        } else {
            this.add_skill_attackCount.put(skillId, val);
        }
    }

    public void addBossDamageRate(int skillId, int val) { //增加BOSS伤害
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_bossDamageRate.containsKey(skillId)) {
            int oldval = (this.add_skill_bossDamageRate.get(Integer.valueOf(skillId)));
            this.add_skill_bossDamageRate.put(skillId, oldval + val);
        } else {
            this.add_skill_bossDamageRate.put(skillId, val);
        }
    }

    public void addIgnoreMobpdpRate(int skillId, int val) {//增加无视怪物防御
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_ignoreMobpdpR.containsKey(skillId)) {
            int oldval = (this.add_skill_ignoreMobpdpR.get(Integer.valueOf(skillId)));
            this.add_skill_ignoreMobpdpR.put(skillId, oldval + val);
        } else {
            this.add_skill_ignoreMobpdpR.put(skillId, val);
        }
    }

    public void addBuffDuration(int skillId, int val) { //增加BUFF时间
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_duration.containsKey(skillId)) {
            int oldval = (this.add_skill_duration.get(Integer.valueOf(skillId)));
            this.add_skill_duration.put(skillId, oldval + val);
        } else {
            this.add_skill_duration.put(skillId, val);
        }
    }

    public void addDotTime(int skillId, int val) { //增加持续掉血时间
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_dotTime.containsKey(skillId)) {
            int oldval = (this.add_skill_dotTime.get(Integer.valueOf(skillId)));
            this.add_skill_dotTime.put(skillId, oldval + val);
        } else {
            this.add_skill_dotTime.put(skillId, val);
        }
    }

    public void addCoolTimeReduce(int skillId, int val) { //增加减少冷却时间
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_coolTimeR.containsKey(skillId)) {
            int oldval = (this.add_skill_coolTimeR.get(Integer.valueOf(skillId)));
            this.add_skill_coolTimeR.put(skillId, oldval + val);
        } else {
            this.add_skill_coolTimeR.put(skillId, val);
        }
    }

    public void addSkillProp(int skillId, int val) { //增加技能概率
        if ((skillId < 0) || (val <= 0)) {
            return;
        }
        if (this.add_skill_prop.containsKey(skillId)) {
            int oldval = (this.add_skill_prop.get(Integer.valueOf(skillId)));
            this.add_skill_prop.put(skillId, oldval + val);
        } else {
            this.add_skill_prop.put(skillId, val);
        }
    }

    public int getMesoGuardMeso(){
        return (int)this.mesoGuardMeso;
    }

    public void setMesoGuardMeso(int meso){
        this.mesoGuardMeso = (double) meso;
    }

}
