package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.SummonSkillEntry;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.world.WorldBroadcastService;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.BuffPacket;
import tools.packet.MobPacket;
import tools.packet.SummonPacket;

public class SummonHandler {


    public static void MoveSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 52
        // 5D 78 2F 00
        // E7 FE 1D 01
        // 04 00 E7 FE 1B 01 00 00 FE FF 00 00 0C 3C 00 00 E7 FE 34 01 00 00 3C 00 00 00 0C 68 01 01 00 00 38 FF 0C 00 00 00 E7 FE 26 01 00 00 80 FF 00 00 0C 5A 00 00
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleSummon sum = chr.getSummons().get(slea.readInt());
        if (sum == null) {
            FileoutputUtil.log("找不到地图物体：召唤兽！");
            return;
        }
        if ((sum.getOwnerId() != chr.getId()) || (sum.getSkillLevel() <= 0) || (sum.getMovementType() == SummonMovementType.不会移动)) {
            return;
        }
        Point startPos = new Point(slea.readShort(), slea.readShort());
        List res = MovementParse.parseMovement(slea, 4);
        Point pos = sum.getPosition();
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            if (slea.available() != 1L) {
                FileoutputUtil.log("slea.available() != 1 (召唤兽移动错误) 剩余封包长度: " + slea.available());
                FileoutputUtil.log(FileoutputUtil.Movement_Sumon, "slea.available() = " + slea.available() + " (召唤兽移动错误) 封包: " + slea.toString(true));
                return;
            }
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getId(), sum.getSkillId(), pos, res), sum.getTruePosition());
        }
    }

    /**
     * 召唤物受到伤害
     * @param slea
     * @param chr
     */
    public static void DamageSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 54 FA FE 30 00 FF 00 00 00 00 04 87 01 00 00
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null)) {
            return;
        }
        MapleSummon summon = chr.getSummons().get(slea.readInt());
        if ((summon == null) || (summon.getOwnerId() != chr.getId())) {
            FileoutputUtil.log("找不到地图物体：召唤兽！");
            return;
        }
        int type = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.skip(1);
        boolean remove = false;
        if ((summon.is替身术()) && (damage > 0)) {
            summon.addSummonHp(-damage);
            if (summon.getSummonHp() <= 0) {
                remove = true;
            }
            chr.getMap().broadcastMessage(chr, SummonPacket.damageSummon(chr.getId(), summon.getSkillId(), damage, type, monsterIdFrom), summon.getTruePosition());
        }
        if (remove) {
            chr.dispelSkill(summon.getSkillId());
        }
    }

    /**
     * 召唤兽攻击
     * @param slea
     * @param c
     * @param chr
     */
    public static void SummonAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 53 5D 78 2F 00 03 A1 86 01 00 06 80 01 5A FF F6 00 5C FF F6 00 64 00 17 00 00 00 E6 FE 35 01
        int count;
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null)) {
            return;
        }
        MapleMap map = chr.getMap();
        MapleSummon summon = chr.getSummons().get(slea.readInt());
        if (summon == null || (summon.getOwnerId() != chr.getId()) || (summon.getSkillLevel() <= 0)) {
            chr.dropMessage(5, "出现错误.");
            return;
        }
        SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkillId());
        if ((summon.getSkillId() / 1000000 != 35) && (summon.getSkillId() != 33101008) && (sse == null)) {
            chr.dropMessage(5, "召唤兽攻击处理出错。");
            return;
        }
        byte animation = slea.readByte();
        byte numAttackedAndDamage = 0x11;
        byte numAttacked = 1;
        byte numDamage = 1;
        if (summon.getSkillId() == 1301013 && summon.getScream()) {
            //TODO 添加灵魂助力震惊的效果 
            chr.send_other(SummonPacket.summonSkill(chr.getId(), 217592, 141), false);
            chr.send_other(MaplePacketCreator.showOwnBuffEffect(1301013, 3, chr.getLevel(), 1), false);
            chr.send(BuffPacket.灵魂助力特殊(summon.SummonTime(360000), summon.setScream(false), summon.getControl()));
        }
        if (sse != null) {
            count = sse.mobCount;
            if (summon.getSkillId() == 1301013) {
                count = 10;
            }
            if (numAttacked > count) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "召唤兽攻击次数错误 (Skillid : " + summon.getSkillId() + " 怪物数量 : " + numAttacked + " 默认数量: " + count + ")");
                }
                chr.dropMessage(5, "[警告] 请不要使用非法程序。召唤兽攻击怪物数量错误.");
                return;
            }
            int numAttackCount = chr.getStat().getAttackCount(summon.getSkillId()) + sse.attackCount;
            if (numDamage > numAttackCount) {
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "召唤兽攻击次数错误 (Skillid : " + summon.getSkillId() + " 打怪次数 : " + numDamage + " 默认次数: " + sse.attackCount + " 超级技能增加次数: " + chr.getStat().getAttackCount(summon.getSkillId()) + ")");
                }
                chr.dropMessage(5, "[警告] 请不要使用非法程序。召唤兽攻击怪物次数错误.");
                return;
            }
        }

        List<AttackPair> allDamage = new ArrayList();
        for (int i = 0; i < numAttacked; i++) {
            MapleMonster mob = map.getMonsterByOid(slea.readInt());
            if (mob == null) {
                continue;
            }
            slea.skip(13);
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < numDamage; j++) {
                int damge = slea.readInt();
                if (chr.isShowPacket()) {
                    chr.dropMessage(-5, "召唤兽攻击 打怪数量: " + numAttacked + " 打怪次数: " + numDamage + " 打怪伤害: " + damge + " 怪物OID: " + mob.getObjectId());
                }
                allDamageNumbers.add(new Pair(damge, false));
            }
            slea.skip(4);
            allDamage.add(new AttackPair(mob.getObjectId(), allDamageNumbers));
        }
        Skill summonSkill = SkillFactory.getSkill(summon.getSkillId());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        if (summonEffect == null) {
            chr.dropMessage(5, "召唤兽攻击出现错误 => 攻击效果为空.");
            return;
        }

        if (allDamage.isEmpty()) {
            return;
        }
        map.broadcastMessage(chr, SummonPacket.summonAttack(summon.getOwnerId(), summon.getObjectId(), animation, numAttackedAndDamage, allDamage, chr.getLevel(), false), summon.getTruePosition());
        for (AttackPair attackEntry : allDamage) {
            MapleMonster targetMob = map.getMonsterByOid(attackEntry.objectid);
            if (targetMob == null) {
                continue;
            }
            int totDamageToOneMonster = 0;
            for (Pair eachde : attackEntry.attack) {
                int toDamage = ((Integer) eachde.left);
                totDamageToOneMonster += toDamage;
            }

            if (totDamageToOneMonster > 0) {
                if ((summonEffect.getMonsterStati().size() > 0)
                        && (summonEffect.makeChanceResult())) {
                    for (Map.Entry z : summonEffect.getMonsterStati().entrySet()) {
                        targetMob.applyStatus(chr, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), summonSkill.getId(), null, false), summonEffect.isPoison(), 4000L, true, summonEffect);
                    }
                }

                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "召唤兽打怪最终伤害 : " + totDamageToOneMonster);
                }
                targetMob.damage(chr, totDamageToOneMonster, true);
                chr.checkMonsterAggro(targetMob);
                if (!targetMob.isAlive()) {
                    chr.getClient().getSession().write(MobPacket.killMonster(targetMob.getObjectId(), 1));
                }
            }
        }

        if (!summon.isMultiAttack()) {
            chr.getMap().broadcastMessage(SummonPacket.removeSummon(summon, true));
            chr.getMap().removeMapObject(summon);
            chr.removeVisibleMapObject(summon);
            chr.removeSummon(summon.getSkillId());
            if (summon.getSkillId() != 35121011) {
                chr.dispelSkill(summon.getSkillId());
            }
        }
    }

    public static void RemoveSummon(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleSummon summon = c.getPlayer().getSummons().get(slea.readInt());
        if (summon ==null || (summon.getOwnerId() != c.getPlayer().getId()) || (summon.getSkillLevel() <= 0)) {
            c.getPlayer().dropMessage(5, "移除召唤兽出现错误.");
            return;
        }
        if (c.getPlayer().isShowPacket()) {
            c.getPlayer().dropSpouseMessage(10, "收到移除召唤兽信息 - 召唤兽技能ID: " + summon.getSkillId() + " 技能名字 " + SkillFactory.getSkillName(summon.getSkillId()));
        }
        c.getPlayer().getMap().broadcastMessage(SummonPacket.removeSummon(summon, false));
        c.getPlayer().getMap().removeMapObject(summon);
        c.getPlayer().removeVisibleMapObject(summon);
        c.getPlayer().removeSummon(summon.getSkillId());
        c.getPlayer().dispelSkill(summon.getSkillId());
    }

    public static void SubSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        MapleSummon sum = chr.getSummons().get(slea.readInt());
        if (sum == null || (sum.getOwnerId() != chr.getId()) || (sum.getSkillLevel() <= 0) || (!chr.isAlive())) {
            return;
        }
        switch (sum.getSkillId()) {
            case 1301013:
                Skill bHealing = SkillFactory.getSkill(slea.readInt());
                int bHealingLvl = chr.getTotalSkillLevel(bHealing);
                if ((bHealingLvl <= 0) || (bHealing == null)) {
                    return;
                }
                MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                if (bHealing.getId() == 1310016) {
                    healEffect.applyTo(chr);
                } else if (bHealing.getId() == 1301013) {
                    if (!chr.canSummon(healEffect.getX() * 1000)) {
                        return;
                    }
                    int healHp = Math.min(1000, healEffect.getHp() * chr.getLevel());
                    chr.addHP(healHp);
                }
                chr.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sum.getSkillId(), 3, chr.getLevel(), bHealingLvl));//2+1 119
                chr.getMap().broadcastMessage(SummonPacket.summonSkill(chr.getId(), sum.getSkillId(), bHealing.getId() == 1301013 ? 5 : Randomizer.nextInt(3) + 6));
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr, sum.getSkillId(), 3, chr.getLevel(), bHealingLvl), false);//2+1 119
        }
    }
}
