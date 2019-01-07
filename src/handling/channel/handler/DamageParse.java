package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.SkillConstants;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import server.AutobanManager;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.skill.冒险家.侠客;
import server.skill.冒险家.刺客;
import server.skill.冒险家.牧师;
import server.skill.冒险家.独行客;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.BuffPacket;
import tools.packet.SkillPacket;

public class DamageParse {

    private static final Logger log = Logger.getLogger(DamageParse.class);

    public static void applyAttack(AttackInfo attack, Skill theSkill, MapleCharacter player, int attackCount, double maxDamagePerMonster, MapleStatEffect effect, AttackType attack_type, int visProjectile) {
        MapleMonster monster;
        if (!player.isAlive()) {
            player.dropMessage(5, "你升天了还想打我？");
            return;
        }
        if (attack.skillId != 0) {
            if (player.isShowPacket()) {
                player.dropMessage(5, "[技能攻击] 使用技能[" + attack.skillId + "]进行攻击");
            }
            if (effect == null) {
                player.getClient().getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (GameConstants.isMulungSkill(attack.skillId)) {
                if (player.getMapId() / 10000 != 92502) {
                    return;
                }
                if (player.getMulungEnergy() < 10000) {
                    return;
                }
                player.mulung_EnergyModify(false);
            } else if (GameConstants.isPyramidSkill(attack.skillId)) {
                if (player.getMapId() / 1000000 != 926) {
                    return;
                }
            } else if (GameConstants.isInflationSkill(attack.skillId)) {
                if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null) {
                    return;
                }
            } else if ((attack.numAttacked > effect.getMobCount(player)) && (attack.skillId != 1220010) && (attack.skillId != 32121003)) {
                if (player.isShowPacket()) {
                    player.dropMessage(0, "物理怪物数量检测 => 封包解析次数: " + attack.numAttacked + " 服务端设置次数: " + effect.getMobCount(player));
                }
                return;
            }
        }
        boolean useAttackCount = !GameConstants.is不检测次数(attack.skillId);

        int totDamage = 0;
        MapleMap map = player.getMap();
//        Point Original_Pos = player.getPosition();

//        else if (map.isPartyPvpMap()) {
//            MaplePvp.doPartyPvP(player, map, attack, effect);
//        } else if (map.isGuildPvpMap()) {
//            MaplePvp.doGuildPvP(player, map, attack, effect);
//        }
        if (attack.skillId == 独行客.金钱炸弹) {
            for (AttackPair oned : attack.allDamage) {
                if (oned.attack != null) {
                    continue;
                }
                MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);
                if (mapobject != null) {
                    MapleMapItem mapitem = (MapleMapItem) mapobject;
                    mapitem.getLock().lock();
                    try {
                        if (mapitem.getMeso() > 0) {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            map.removeMapObject(mapitem);
                            //map.broadcastMessage(InventoryPacket.explodeDrop(mapitem.getObjectId()));
                            mapitem.setPickedUp(true);
                        } else {
                            return;
                        }
                    } finally {
                        mapitem.getLock().unlock();
                    }
                } else {
                    return;
                }
            }
        }
        int totDamageToOneMonster = 0;
        long hpMob = 0L;
        PlayerStats stats = player.getStat();
        int criticalDamage = stats.passive_sharpeye_percent();
        int shdowPartnerAttackPercentage = 0;
        if ((attack_type == AttackType.RANGED_WITH_SHADOWPARTNER) || (attack_type == AttackType.NON_RANGED_WITH_MIRROR)) {
            MapleStatEffect shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.影分身);
            if (shadowPartnerEffect != null) {
                shdowPartnerAttackPercentage += shadowPartnerEffect.getShadowDamage();
            }
            attackCount /= 2;
        }
        shdowPartnerAttackPercentage *= (criticalDamage + 100) / 100;
        if ((attack.skillId == 4221014) || (attack.skillId == 4221016)) {
            shdowPartnerAttackPercentage *= 30;
        }

        if (attack.skillId == 3120017) { //三彩箭矢
            effect.getMonsterStati().clear();
            if (player.getmod() == 3) {
                effect.getMonsterStati().put(MonsterStatus.中毒, 1);
            }
        }

        int maxDamagePerHit = 0;
        int maxMaxDamageOver = 0;

        for (AttackPair oned : attack.allDamage) {
            monster = map.getMonsterByOid(oned.objectid);
            if (monster != null && monster.getLinkCID() <= 0) {
                totDamageToOneMonster = 0;
                hpMob = monster.getMobMaxHp();
                MapleMonsterStats monsterstats = monster.getStats();
                int fixeddmg = monsterstats.getFixedDamage();
                boolean Tempest = (monster.getStatusSourceID(MonsterStatus.结冰) == 21120006) || (attack.skillId == 21120006) || (attack.skillId == 1221011);
                if (!Tempest) {
                    maxDamagePerHit = CalculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect, maxDamagePerMonster, criticalDamage);
                }
                byte overallAttackCount = 0;
                for (Pair eachde : oned.attack) {
                    Integer eachd = (Integer) eachde.left;
                    overallAttackCount = (byte) (overallAttackCount + 1);
                    if ((useAttackCount) && (overallAttackCount - 1 == attackCount)) {
                        maxDamagePerHit = (int) (maxDamagePerHit / 100 * (shdowPartnerAttackPercentage * (monsterstats.isBoss() ? stats.getBossDamageRate(attack.skillId) : stats.getDamageRate()) / 100.0D));
                    }
                    if ((player.isShowPacket()) && (eachd > 0)) {
                        player.dropMessage(0, new StringBuilder().append("物理攻击打怪伤害 : ").append(eachd).append(" 服务端预计伤害 : ").append(maxDamagePerHit).append(" 是否超过 : ").append(eachd > maxDamagePerHit).toString());
                    }
                    if (fixeddmg != -1) {
                        if (monsterstats.getOnlyNoramlAttack()) {
                            eachd = attack.skillId != 0 ? 0 : fixeddmg;
                        } else {
                            eachd = fixeddmg;
                        }
                    } else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = attack.skillId != 0 ? 0 : Math.min(eachd, maxDamagePerHit);
                    } else if (!player.isGM()) {
                        if (Tempest) {
                            if (eachd > monster.getMobMaxHp()) {
                                eachd = (int) Math.min(monster.getMobMaxHp(), 2147483647L);
                            }
                        } else if (((player.getJob() >= 3200) && (player.getJob() <= 3212) ) || (attack.skillId == 23121003) || (((player.getJob() < 3200) || (player.getJob() > 3212)) )) {
                            if ((eachd > maxDamagePerHit) && (maxDamagePerHit > 2)) {
                                if ((eachd > maxDamagePerHit * 5) && (attack.skillId != 4001344)) {//TODO 开启攻击异常封号
                                    String banReason = player.getName() + " 被系统封号.[异常攻击伤害值: " + eachd + ", 预计伤害: " + maxDamagePerHit + ", 怪物ID: " + monster.getId() + "] [职业: " + player.getJob() + ", 等级: " + player.getLevel() + ", 技能: " + attack.skillId + "]";
                                    if (eachd > maxDamagePerHit * 7) {
                                        FileoutputUtil.log(FileoutputUtil.攻击异常, "玩家[" + player.getName() + " 职业: " + player.getJobName() + "] 使用技能: " + theSkill.getName() + "超过系统计算攻击:" + maxDamagePerHit * 7 + " 玩家攻击:" + eachd);
                                        player.getClient().getSession().write(MaplePacketCreator.enableActions());
                                        //AutobanManager.getInstance().autoban(player.getClient(), banReason);
                                        return;
                                    }
                                }
                            }
                        } else if (eachd > maxDamagePerHit) {
                            eachd = maxDamagePerHit;
                        }
                    }
                    totDamageToOneMonster += eachd;
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                if (player.getBuffedValue(MapleBuffStat.敛财术) != null) {
                    handlePickPocket(player, monster, oned);
                }

                if (totDamageToOneMonster > 0) {
                    if (attack.skillId == 刺客.生命吸收) {
                        int rev = Math.round((effect.getX() * totDamage)/100);
                        player.addHP(rev);
                    }
                    monster.damage(player, totDamageToOneMonster, true, attack.skillId);

                    //TODO 添加被动攻击技能处理  进阶攻击之类  召唤Mist之类的
                    player.handle被动触发技能(monster, attack.skillId);
                    //TODO 攻击前技能处理
                    player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skillId, monster.getObjectId(), totDamage);
                    //处理被动技能效果
                    switch (attack.skillId) {
                        case 4001334:
                        case 4001344:
                        case 4101008:
                        case 4101010:
                        case 4111010:
                        case 4111015:
                        case 4121013:
                        case 4201012:
                        case 4211002:
                        case 4211011:
                        case 4221007:
                        case 4221010:
                        case 4221014:
                        case 4311002:
                        case 4311003:
                        case 4321004:
                        case 4321006:
                        case 4331000:
                        case 4331006:
                        case 4341002:
                        case 4341004:
                        case 4341009:
                            int[] skills = {4110011, 4210010, 4320005, 14110004};
                            Skill skill = null;
                            MapleStatEffect venomEffect = null;
                            for (int i : skills) {
                                skill = SkillFactory.getSkill(i);
                                if (player.getTotalSkillLevel(skill) > 0) {
                                    venomEffect = skill.getEffect(player.getTotalSkillLevel(skill));
                                    if (venomEffect.makeChanceResult()) {
                                        monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.中毒, 1, i, null, false), true, venomEffect.getDuration(), true, venomEffect);
                                    }
                                    break;
                                }
                            }
                            break;
                        case 侠客.神通术:
                            monster.handleSteal(player);
                            break;
                    }
                    if (totDamageToOneMonster > 0) {
                        Item weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                        if (weapon_ != null) {
                            MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId());
                            if ((stat != null) && (Randomizer.nextInt(100) < GameConstants.getStatChance())) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat, Integer.valueOf(GameConstants.getXForStat(stat)), GameConstants.getSkillForStat(stat), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, 10000L, false, null);
                            }
                        }
                        if ((player.getJob() == 121) || (player.getJob() == 122)) {
                            Skill skill = SkillFactory.getSkill(1201012);
                            if (player.isBuffFrom(MapleBuffStat.属性攻击, skill)) {
                                MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.结冰, Integer.valueOf(1), skill.getId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 2000, true, eff);
                            }
                        }
                    }
                    if (effect != null) {
                        if (effect.getMonsterStati().size() > 0 && effect.makeChanceResult()) {
                            for (Map.Entry z : effect.getMonsterStati().entrySet()) {
                                monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                            }
                        }
                    }
                }
            }
        }
        if (totDamageToOneMonster > 0) {
            //TODO 攻击后技能处理
            player.afterAttack(attack.numAttacked, attack.numDamage, attack.skillId);
        }
        if (effect != null && ((attack.skillId != 0) && ((attack.numAttacked > 0) || (attack.skillId != 4341002)) && (!GameConstants.isNoDelaySkill(attack.skillId)))) {
            if (NotEffectforAttack(attack.skillId)) {//TODO BUFF类技能攻击不应该再给与BUFF状态
                effect.applyTo(player, attack.position);
            }
        }

    }

    public static boolean NotEffectforAttack(int skill) {//TODO 添加有BUFF状态的技能攻击不应该再给与BUFF状态
        switch (skill) {
            case 31121005:
            case 61120007:
            case 61101002:
            case 13121054:
            case 4341052:
            case 3120019:
            case 2121054:
            case 24121005:
            case 32121003:
            case 65121003:
            case 14121004:
            case 35121052:
            case 33121012:
                return false;
            default:
                return true;
        }
    }

    public static void applyAttackMagic(AttackInfo attack, Skill theSkill, MapleCharacter player, MapleStatEffect effect, double maxDamagePerMonster) {
        MapleMonster monster;
        if (!player.isAlive()) {
            return;
        }
        if ((attack.real) && (GameConstants.getAttackDelay(attack.skillId, theSkill) >= 50)) {
            //player.getCheatTracker().checkAttack(attack.skillId, attack.lastAttackTickCount);
        }
        int mobCount = effect.getMobCount(player);
        int attackCount = effect.getAttackCount(player);

        if (GameConstants.isMulungSkill(attack.skillId)) {
            if (player.getMapId() / 10000 != 92502) {
                return;
            }
            if (player.getMulungEnergy() < 10000) {
                return;
            }
            player.mulung_EnergyModify(false);
        } else if (GameConstants.isPyramidSkill(attack.skillId)) {
            if (player.getMapId() / 1000000 != 926) {
                return;
            }
        } else if ((GameConstants.isInflationSkill(attack.skillId))
                && (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null)) {
            return;
        }

        PlayerStats stats = player.getStat();
        Element element = theSkill.getElement();

        int maxDamagePerHit = 0;
        int totDamage = 0;

        int CriticalDamage = stats.passive_sharpeye_percent();
        Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
        int eaterLevel = player.getTotalSkillLevel(eaterSkill);

        MapleMap map = player.getMap();

        for (AttackPair oned : attack.allDamage) {
            monster = map.getMonsterByOid(oned.objectid);
            if ((monster != null) && (monster.getLinkCID() <= 0)) {
                boolean Tempest = (monster.getStatusSourceID(MonsterStatus.结冰) == 21120006) && (!monster.getStats().isBoss());
                int totDamageToOneMonster = 0;
                MapleMonsterStats monsterstats = monster.getStats();
                int fixeddmg = monsterstats.getFixedDamage();
                if (!Tempest) {
                    maxDamagePerHit = CalculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats, element, CriticalDamage, maxDamagePerMonster, effect);
                }
                byte overallAttackCount = 0;

                for (Pair eachde : oned.attack) {
                    Integer eachd = (Integer) eachde.left;
                    overallAttackCount = (byte) (overallAttackCount + 1);
                    if (fixeddmg != -1) {
                        eachd = monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg;
                    } else {
                        if (player.isShowPacket()) {
                            player.dropMessage(0, new StringBuilder().append("魔法攻击打怪伤害 : ").append(eachd).append(" 服务端预计伤害 : ").append(maxDamagePerHit).append(" 是否超过 : ").append(eachd > maxDamagePerHit).toString());
                        }
                        if (monsterstats.getOnlyNoramlAttack()) {
                            eachd = 0;
                        } else if (!player.isGM()) {
                            if (Tempest) {
                                if (eachd > monster.getMobMaxHp()) {
                                    eachd = (int) Math.min(monster.getMobMaxHp(), 2147483647L);
                                }
                            } else if (eachd > maxDamagePerHit) {
                                eachd = maxDamagePerHit;
                            }
                        }
                    }

                    totDamageToOneMonster += eachd;
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                if ((GameConstants.getAttackDelay(attack.skillId, theSkill) >= 50) && (!GameConstants.isNoDelaySkill(attack.skillId)) && (!GameConstants.is不检测范围(attack.skillId)) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange) * 3)
                        && (player.getMapId() != 703002000)) {
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] " + player.getName() + " ID: " + player.getId() + " (等级 " + player.getLevel() + ") 攻击范围异常。职业: " + player.getJob() + " 技能: " + attack.skillId + " [范围: " + player.getTruePosition().distanceSq(monster.getTruePosition()) + " 预期: " + GameConstants.getAttackRange(effect, player.getStat().defRange) * 3 + "]"));
                }

                if ((attack.skillId == 牧师.群体治愈) && (!monsterstats.getUndead())) {
                    return;
                }
                if (totDamageToOneMonster > 0) {
                    monster.damage(player, totDamageToOneMonster, true, attack.skillId);
//                    if (monster.isBuffed(MonsterStatus.反射魔攻)) {
//                        player.addHP(-(7000 + Randomizer.nextInt(8000)));
//                    }
//                    if (player.getBuffedValue(MapleBuffStat.缓速术) != null) {
//                        MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.缓速术);
//                        if ((eff != null) && (eff.makeChanceResult()) && (!monster.isBuffed(MonsterStatus.速度))) {
//                            monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.速度, eff.getX(), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
//                        }
//                    }
                    player.handle被动触发技能(monster, attack.skillId);
                    player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skillId, monster.getObjectId(), totDamage);
                    switch (attack.skillId) {
                        case 2211010:
                            monster.setTempEffectiveness(Element.ICE, effect.getDuration());
                            break;
                        case 2121003:
                            monster.setTempEffectiveness(Element.FIRE, effect.getDuration());
                    }

                    if ((effect.getMonsterStati().size() > 0)
                            && (effect.makeChanceResult())) {
                        for (Map.Entry z : effect.getMonsterStati().entrySet()) {
                            monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                        }

                    }

                    if (eaterLevel > 0) {
                        eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
                    }
                } else if ((attack.skillId == 27101101) && (effect.getMonsterStati().size() > 0)
                        && (effect.makeChanceResult())) {
                    for (Map.Entry z : effect.getMonsterStati().entrySet()) {
                        monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                    }
                }
            }
        }
        if (attack.skillId != 牧师.群体治愈) {
            effect.applyTo(player);
        }
    }

    private static int CalculateMaxMagicDamagePerHit(MapleCharacter chr, Skill skill, MapleMonster monster, MapleMonsterStats mobstats, PlayerStats stats, Element elem, Integer sharpEye, double maxDamagePerMonster, MapleStatEffect attackEffect) {
        int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0) * 2;
        int HitRate = Math.min((int) Math.floor(Math.sqrt(stats.getAccuracy())) - (int) Math.floor(Math.sqrt(mobstats.getEva())) + 100, 100);
        if (dLevel > HitRate) {
            HitRate = dLevel;
        }
        HitRate -= dLevel;
        if ((HitRate <= 0) && ((!GameConstants.is新手职业(skill.getId() / 10000)) || (skill.getId() % 10000 != 1000))) {
            return 0;
        }

        int CritPercent = sharpEye;
        ElementalEffectiveness ee = monster.getEffectiveness(elem);
        double elemMaxDamagePerMob;
        switch (ee) {
            case IMMUNE:
                elemMaxDamagePerMob = 1.0D;
                break;
            default:
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * ee.getValue(), stats);
        }

        int MDRate = monster.getStats().getMDRate();
        MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.魔防);
        if (pdr != null) {
            MDRate += pdr.getX();
        }
        elemMaxDamagePerMob -= elemMaxDamagePerMob * (Math.max(MDRate - stats.getIgnoreMobpdpR(skill.getId()) - attackEffect.getIgnoreMob(), 0) / 100.0D);

        elemMaxDamagePerMob += elemMaxDamagePerMob / 100.0D * CritPercent;
        elemMaxDamagePerMob *= ((monster.getStats().isBoss() ? chr.getStat().getBossDamageRate(skill.getId()) : chr.getStat().getDamageRate()) + attackEffect.getDamage()) / 100.0D;
        elemMaxDamagePerMob += elemMaxDamagePerMob * (chr.getDamageIncrease(monster.getObjectId()) / 100.0D);
        if (GameConstants.is新手职业(skill.getId() / 10000)) {
            switch (skill.getId() % 10000) {
                case 1000:
                    elemMaxDamagePerMob = 40.0D;
                    break;
                case 1020:
                    elemMaxDamagePerMob = 1.0D;
                    break;
                case 1009:
                    elemMaxDamagePerMob = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp();
            }
        }

        int maxDamagePerMob = (int) elemMaxDamagePerMob;
        switch (skill.getId()) {
            case 32001000:
            case 32101000:
            case 32111002:
            case 32121002:
                maxDamagePerMob = (int) (maxDamagePerMob * 1.5D);
                break;
            case 27121303:
                maxDamagePerMob = chr.getMaxDamageOver(skill.getId());
        }

        if ((monster.getId() >= 9400900) && (monster.getId() <= 9400911)) {
            maxDamagePerMob = 999999;
        } else if ((monster.getId() >= 9600101) && (monster.getId() <= 9600136)) {
            maxDamagePerMob = 888888;
        }
        if (maxDamagePerMob > 999999) {
            maxDamagePerMob = chr.getMaxDamageOver(skill.getId());
        } else if (elemMaxDamagePerMob <= 0.0D) {
            maxDamagePerMob = 1;
        }
        return maxDamagePerMob;
    }

    private static double ElementalStaffAttackBonus(Element elem, double elemMaxDamagePerMob, PlayerStats stats) {
        switch (elem) {
            case FIRE:
                return elemMaxDamagePerMob / 100.0D * (stats.element_fire + stats.getElementBoost(elem));
            case ICE:
                return elemMaxDamagePerMob / 100.0D * (stats.element_ice + stats.getElementBoost(elem));
            case LIGHTING:
                return elemMaxDamagePerMob / 100.0D * (stats.element_light + stats.getElementBoost(elem));
            case POISON:
                return elemMaxDamagePerMob / 100.0D * (stats.element_psn + stats.getElementBoost(elem));
        }
        return elemMaxDamagePerMob / 100.0D * (stats.def + stats.getElementBoost(elem));
    }

    /**
     * 处理敛财术
     * @param player
     * @param mob
     * @param oned
     */
    private static void handlePickPocket(MapleCharacter player, MapleMonster mob, AttackPair oned) {
        int maxmeso = player.getBuffedValue(MapleBuffStat.敛财术);
        for (Pair eachde : oned.attack) {
            Integer eachd = (Integer) eachde.left;
            if ((player.getStat().pickRate >= 100) || (Randomizer.nextInt(99) < player.getStat().pickRate)) {
                player.getMap().spawnMesoDrop(Math.min((int) Math.max(eachd / 20000.0D * maxmeso, 1.0D), maxmeso), new Point((int) (mob.getTruePosition().getX() + Randomizer.nextInt(100) - 50.0D), (int) mob.getTruePosition().getY()), mob, player, false, (byte) 0);
            }
        }
    }

    private static int CalculateMaxWeaponDamagePerHit(MapleCharacter player, MapleMonster monster, AttackInfo attack, Skill theSkill, MapleStatEffect attackEffect, double maxDamagePerMonster, Integer CriticalDamagePercent) {
        int dLevel = Math.max(monster.getStats().getLevel() - player.getLevel(), 0) * 2;
        int HitRate = Math.min((int) Math.floor(Math.sqrt(player.getStat().getAccuracy())) - (int) Math.floor(Math.sqrt(monster.getStats().getEva())) + 100, 100);
        if (dLevel > HitRate) {
            HitRate = dLevel;
        }
        HitRate -= dLevel;
        if ((HitRate <= 0) && ((!GameConstants.is新手职业(attack.skillId / 10000)) || (attack.skillId % 10000 != 1000)) && (!GameConstants.isPyramidSkill(attack.skillId)) && (!GameConstants.isMulungSkill(attack.skillId)) && (!GameConstants.isInflationSkill(attack.skillId))) {
            return 0;
        }
        List<Element> elements = new ArrayList<>();
        int CritPercent = CriticalDamagePercent;
        int PDRate = monster.getStats().getPDRate();
        MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.物防);
        if (pdr != null) {
            PDRate += pdr.getX();
        }
        double elemMaxDamagePerMonster = maxDamagePerMonster;
        if (theSkill != null) {
            if (theSkill.getId() == 1321015) {
                PDRate = monster.getStats().isBoss() ? PDRate : 0;
            }

            elements.add(theSkill.getElement());
            if (player.getBuffedValue(MapleBuffStat.属性攻击) != null) {
                int chargeSkillId = player.getBuffSource(MapleBuffStat.属性攻击);
                switch (chargeSkillId) {
                    case 1201011:
                        elements.add(Element.FIRE);
                        break;
                    case 1201012:
                    case 21101006:
                        elements.add(Element.ICE);
                        break;
                    case 1211008:
                        elements.add(Element.LIGHTING);
                        break;
                    case 1221004:
                        elements.add(Element.HOLY);
                }
            }

            double elementalEffect;
            if (elements.size() > 0) {
                switch (attack.skillId) {
                    case 3111003:
                        elementalEffect = attackEffect.getX() / 100.0D;
                        break;
                    default:
                        elementalEffect = 0.5D / elements.size();
                }

                for (Element element : elements) {
                    switch (monster.getEffectiveness(element)) {
                        case IMMUNE:
                            elemMaxDamagePerMonster = 1.0D;
                            break;
                        case WEAK:
                            elemMaxDamagePerMonster *= (1.0D + elementalEffect + player.getStat().getElementBoost(element));
                            break;
                        case STRONG:
                            elemMaxDamagePerMonster *= (1.0D - elementalEffect - player.getStat().getElementBoost(element));
                    }
                }
            }

            elemMaxDamagePerMonster -= elemMaxDamagePerMonster * (Math.max(PDRate - Math.max(player.getStat().getIgnoreMobpdpR(attack.skillId), 0) - Math.max(attackEffect == null ? 0 : attackEffect.getIgnoreMob(), 0), 0) / 100.0D);

            elemMaxDamagePerMonster += elemMaxDamagePerMonster / 100.0D * CritPercent;

            elemMaxDamagePerMonster += elemMaxDamagePerMonster * player.getDamageIncrease(monster.getObjectId()) / 100.0D;
            elemMaxDamagePerMonster *= ((monster.getStats().isBoss()) && (attackEffect != null) ? player.getStat().getBossDamageRate(attack.skillId) + attackEffect.getBossDamage() + attackEffect.getDamage() : player.getStat().getDamageRate()) / 100.0D;
        } else if (attack.skillId == 0) {
            elemMaxDamagePerMonster -= elemMaxDamagePerMonster * (Math.max(PDRate - Math.max(player.getStat().getIgnoreMobpdpR(attack.skillId), 0), 0) / 100.0D);
            elemMaxDamagePerMonster += elemMaxDamagePerMonster / 100.0D * CritPercent;
            elemMaxDamagePerMonster *= (monster.getStats().isBoss() ? player.getStat().getBossDamageRate(attack.skillId) : player.getStat().getDamageRate()) / 100.0D;
        }
        int maxDamageOver = player.getMaxDamageOver(attack.skillId);
        int maxDamagePerMob = (int) elemMaxDamagePerMonster;
        if (theSkill != null) {
            if (GameConstants.is新手职业(theSkill.getId() / 10000)) {
                switch (theSkill.getId() % 10000) {
                    case 1000:
                        maxDamagePerMob = 40;
                        break;
                    case 1020:
                        maxDamagePerMob = 1;
                        break;
                    case 1009:
                        maxDamagePerMob = (int) (monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp());
                }
            }

            switch (theSkill.getId()) {
                case 1311011:
                    maxDamagePerMob = (int) (maxDamagePerMob * ((attackEffect.getY() - attackEffect.getDamage()) / 100.0D));
                    break;
                case 1321012:
                    maxDamagePerMob = (int) (maxDamagePerMob * 1.5D);
                    break;
                case 32001000:
                case 32101000:
                case 32111002:
                case 32121002:
                    maxDamagePerMob = (int) (maxDamagePerMob * 1.5D);
                    break;
                case 61121102:
                case 61121203:
                    if (monster.getStats().isBoss()) {
                        break;
                    }
                    //maxDamagePerMob = 999999;
                    break;
                case 1221011:
                    maxDamagePerMob = (int) (monster.getStats().isBoss() ? maxDamagePerMob : monster.getHp() - 1L);
                    break;
                case 21120006:
                    maxDamagePerMob = (int) (monster.getStats().isBoss() ? maxDamagePerMob : monster.getHp() - 1L);
                    break;
                case 3221007:
                case 5221016:
                case 5721006:
                case 27121303:
                    if (monster.getStats().isBoss()) {
                        maxDamagePerMob = (int) (maxDamagePerMob * (1.0D + attackEffect.getIgnoreMob() / 100.0D));
                    } else {
                        maxDamagePerMob = maxDamageOver;
                    }
            }
        }

        if ((player.getJob() == 311) || (player.getJob() == 312) || (player.getJob() == 321) || (player.getJob() == 322)) {
            Skill mortal = SkillFactory.getSkill((player.getJob() == 311) || (player.getJob() == 312) ? 3110001 : 3210001);
            if (player.getTotalSkillLevel(mortal) > 0) {
                MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
                if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
                    //maxDamagePerMob = 999999;
                    if (mort.getZ() > 0) {
                        player.addHP(player.getStat().getMaxHp() * mort.getZ() / 100);
                    }
                }
            }
        } else if ((player.getJob() == 221) || (player.getJob() == 222)) {
            Skill mortal = SkillFactory.getSkill(2210000);
            if (player.getTotalSkillLevel(mortal) > 0) {
                MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
                if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
                    //maxDamagePerMob = 999999;
                }
            }
        }

        if ((monster.getId() >= 9400900) && (monster.getId() <= 9400911)) {
            //maxDamagePerMob = 999999;
        } else if ((monster.getId() >= 9600101) && (monster.getId() <= 9600136)) {
            //maxDamagePerMob = 888888;
        } else if (maxDamagePerMob > maxDamageOver) {
            maxDamagePerMob = maxDamageOver;
        } else if (maxDamagePerMob <= 0) {
            maxDamagePerMob = 1;
        }
        return maxDamagePerMob;
    }

    public static AttackInfo DivideAttack(AttackInfo attack, int rate) {
        attack.real = false;
        if (rate <= 1) {
            return attack;
        }
        for (AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                for (Pair<Integer, Boolean> eachd : p.attack) {
                    eachd.left /= rate; //too ex.
                }
            }
        }
        return attack;
    }

    public static AttackInfo Modify_AttackCrit(AttackInfo attack, MapleCharacter chr, int type, MapleStatEffect effect) {
        int criticalRate;
        boolean shadow;
        List damages;
        List damage;
        if (attack.skillId != 独行客.金钱炸弹) {
            criticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCritical());
            shadow = (chr.getBuffedValue(MapleBuffStat.影分身) != null) && ((type == 1) || (type == 2));
            damages = new ArrayList();
            damage = new ArrayList();

            for (AttackPair p : attack.allDamage) {
                if (p.attack != null) {
                    int hit = 0;
                    int mid_att = shadow ? p.attack.size() / 2 : p.attack.size();
                    int toCrit = (attack.skillId == 4221014) || (attack.skillId == 4221016) || (attack.skillId == 3221007) || (attack.skillId == 4321006) || (attack.skillId == 4331006) ? mid_att : 0;
                    if (toCrit == 0) {
                        for (Pair eachd : p.attack) {
                            if ((!((Boolean) eachd.right)) && (hit < mid_att)) {
                                if ((((Integer) eachd.left) > 999999) || (Randomizer.nextInt(100) < criticalRate)) {
                                    toCrit++;
                                }
                                damage.add(eachd.left);
                            }
                            hit++;
                        }
                        if (toCrit == 0) {
                            damage.clear();
                            continue;
                        }
                        Collections.sort(damage);
                        for (int i = damage.size(); i > damage.size() - toCrit; i--) {
                            damages.add(damage.get(i - 1));
                        }
                        damage.clear();
                    }
                    hit = 0;
                    for (Pair eachd : p.attack) {
                        if (!((Boolean) eachd.right)) {
                            if (attack.skillId == 4221014) {
                                eachd.right = hit == 3;
                            } else if ((attack.skillId == 3221007) || (attack.skillId == 4321006) || (attack.skillId == 4331006) || (((Integer) eachd.left) > 999999)) {
                                eachd.right = true;
                            } else if (hit >= mid_att) {
                                eachd.right = ((Pair) p.attack.get(hit - mid_att)).right;
                            } else {
                                eachd.right = damages.contains(eachd.left);
                            }
                        }
                        hit++;
                    }
                    damages.clear();
                }
            }
        }
        return attack;
    }

    /**
     * 解析魔法攻击
     * @param lea
     * @param chr
     * @return
     */
    public static AttackInfo parseMagicDamage(SeekableLittleEndianAccessor lea, MapleCharacter chr) {
        // 1C 11 6C 88 1E 00 00 1D 06 A1 86 01 00 06 80 00 00 F7 FF F6 00 F7 FF F6 00 79 02 09 00 00 00 4C FF F6 00
        AttackInfo ret = new AttackInfo();
        ret.isMagicAttack = true;
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (byte) (ret.numAttackedAndDamage >>> 4 & 0xF);
        ret.numDamage = (byte) (ret.numAttackedAndDamage & 0xF);
        ret.skillId = lea.readInt();
        lea.skip(1);
        ret.stance = lea.readByte();
        ret.direction = lea.readByte();
        ret.allDamage = new ArrayList();
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(4); // skip 4 (int)
            lea.readShort(); // skip 2 (x - short)
            lea.readShort(); // skip 2 (y - short)
            lea.skip(6); // skip 4 (int)
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (chr.isShowPacket()) {
                    chr.dropMessage(0, "魔法攻击 - 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage);
                }
                if (damage < 0) {
                    FileoutputUtil.log(FileoutputUtil.攻击出错, "魔法攻击出错封包:  打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage + " 技能ID: " + ret.skillId + lea.toString(true));
                }
                allDamageNumbers.add(new Pair(damage, false));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        return ret;
    }

    /**
     * 解析攻击参数
     * @param lea
     * @param chr
     * @return
     */
    public static AttackInfo parseCloseRangeAttack(SeekableLittleEndianAccessor lea, MapleCharacter chr) {
        // 1A 11 2C 46 0F 00 00 10 04 A2 86 01 00 06 80 00 00 94 FF F6 00 95 FF F6 00 31 01 0B 00 00 00 57 FF F6 00
        // 1A 11 2C 46 0F 00 00 14 08 A7 86 01 00 06 00 01 00 39 FF F6 00 38 FF F6 00 C1 01 2E 00 00 00 23 FF 05 01
        // 1A 01 00 00 00 00 00 06 04 FD FE 13 01
        // 1A 00 3E 41 40 00 00 35 04 D9 01 5F 00 00 63 02 金钱炸弹
        AttackInfo ret = new AttackInfo();
        ret.isCloseRangeAttack = true;
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (byte) (ret.numAttackedAndDamage >>> 4 & 0xF);//次数
        ret.numDamage = (byte) (ret.numAttackedAndDamage & 0xF);//数量
        ret.skillId = lea.readInt();

        lea.skip(1);
        ret.stance = lea.readByte();
        ret.direction = lea.readByte();

        ret.allDamage = new ArrayList();
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(4);
            lea.readShort();
            lea.readShort();
            lea.skip(4);
            lea.skip(2);
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (chr.isShowPacket()) {
                    chr.dropMessage(0, "近距离攻击 - 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage);
                }
                allDamageNumbers.add(new Pair(damage, false));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        return ret;
    }

    public static AttackInfo parseRangedAttack(SeekableLittleEndianAccessor lea, MapleCharacter chr) {
        // 1B 11 AC CA 2D 00 00 16 06 02 00 41 A1 86 01 00 06 80 00 00 4E FF F6 00 4E FF F6 00 44 02 39 00 00 00 B7 FE 13 01
        // 1B
        // 01
        // 00 00 00 00
        // 00
        // 96 06
        // 13 00 41 36 00 2F 00

        // 1B 11 8D 93 3E 00 00 9A 06 02 00 41 A2 86 01 00 06 00 00 00 57 FF F6 00 52 FF F6 00 4A 02 80 00 00 00 F3 FF F6 00 生命吸收
        AttackInfo ret = new AttackInfo();
        ret.isRangedAttack = true;
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (byte) (ret.numAttackedAndDamage >>> 4 & 0xF);//数量
        ret.numDamage = (byte) (ret.numAttackedAndDamage & 0xF);//次数
        ret.skillId = lea.readInt();
        if (chr.isShowPacket()) {
            chr.dropSpouseMessage(1, "[RangedAttack] - 技能ID: " + ret.skillId + " 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage);
        }
        lea.skip(1);
        ret.stance = lea.readByte();
        ret.direction = lea.readByte();
        switch (ret.skillId) {
            case 5310011:
            case 3121013:
            case 5220023:
            case 95001000:
            case 5221022:
                lea.skip(8);
                break;
            case 5311010:
                lea.skip(4);
        }
        ret.starSlot = lea.readShort();
        lea.skip(1);

        ret.allDamage = new ArrayList();
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(14);
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "远距离攻击 - 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage);
                }
                if ((damage > chr.getMaxDamageOver(ret.skillId)) || (damage < 0)) {
                    if (chr.isShowPacket()) {
                        chr.dropMessage(-5, "远距离攻击次数出错:  技能ID: " + ret.skillId + " 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage);
                    }
//                    if ((damage > chr.getMaxDamageOver(ret.skillId)) && (!chr.isGM())) {
//                        chr.sendPolice("系统检测到您的攻击伤害异常，系统对您进行掉线处理。");
//                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM Message] "+chr.getName()+" ID: "+chr.getId()+" (等级 "+chr.getLevel()+") 远距离攻击伤害异常。打怪伤害: "+damage+" 地图ID: "+chr.getMapId()));
//                    }
                    FileoutputUtil.log(FileoutputUtil.攻击出错, "远距离攻击出错封包: 打怪数量: " + ret.numAttacked + " 打怪次数: " + ret.numDamage + " 怪物OID " + oid + " 伤害: " + damage + " 技能ID: " + ret.skillId + lea.toString(true));
                }
                allDamageNumbers.add(new Pair(damage, false));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        if (lea.available() == 4L) {
            ret.skillposition = lea.readPos();
        }
        return ret;
    }

    public static AttackInfo parseMesoExplosion(SeekableLittleEndianAccessor lea, AttackInfo ret, MapleCharacter chr) {
        if (ret.numDamage == 0) {
            lea.skip(4);
            byte bullets = lea.readByte();
            for (int j = 0; j < bullets; j++) {
                int mesoid = lea.readInt();
                lea.skip(2);
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "金钱炸弹攻击怪物: 无怪 " + ret.numDamage + " 金币ID: " + mesoid);
                }
                ret.allDamage.add(new AttackPair(mesoid, null));
            }
            lea.skip(2);
            return ret;
        }

        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(19);
            byte bullets = lea.readByte();
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < bullets; j++) {
                int damage = lea.readInt();
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "金钱炸弹攻击怪物: " + ret.numAttacked + " 攻击次数: " + bullets + " 打怪伤害: " + damage);
                }
                allDamageNumbers.add(new Pair(damage, false));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
            lea.skip(4);
            lea.skip(4);
            lea.skip(4);
        }
        lea.skip(4);
        byte bullets = lea.readByte();
        for (int j = 0; j < bullets; j++) {
            int mesoid = lea.readInt();
            lea.skip(2);
            if (chr.isShowPacket()) {
                chr.dropMessage(-5, "金钱炸弹攻击怪物: 有怪 " + bullets + " 金币ID: " + mesoid);
            }
            ret.allDamage.add(new AttackPair(mesoid, null));
        }

        return ret;
    }
}
