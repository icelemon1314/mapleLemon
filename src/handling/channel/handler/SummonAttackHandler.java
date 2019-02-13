package handling.channel.handler;

import client.*;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.MaplePacketHandler;
import handling.vo.recv.SummonAttackRecvVO;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleSummon;
import tools.AttackPair;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.BuffPacket;
import tools.packet.MobPacket;
import tools.packet.SummonPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SummonAttackHandler extends MaplePacketHandler<SummonAttackRecvVO> {


    @Override
    public void handlePacket(SummonAttackRecvVO recvVO, MapleClient c) {
        // 53 5D 78 2F 00 03 A1 86 01 00 06 80 01 5A FF F6 00 5C FF F6 00 64 00 17 00 00 00 E6 FE 35 01
        int count;
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null)) {
            return;
        }
        MapleMap map = chr.getMap();
        MapleSummon summon = chr.getSummons().get(recvVO.getSummonId());
        if (summon == null || (summon.getOwnerId() != chr.getId()) || (summon.getSkillLevel() <= 0)) {
            chr.dropMessage(5, "出现错误.");
            return;
        }
        SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkillId());
        if ((summon.getSkillId() / 1000000 != 35) && (summon.getSkillId() != 33101008) && (sse == null)) {
            chr.dropMessage(5, "召唤兽攻击处理出错。");
            return;
        }
        byte animation = recvVO.getAnimation();
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

        List<AttackPair> allDamage = recvVO.getAllDamage();
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
                    chr.getClient().sendPacket(MobPacket.killMonster(targetMob.getObjectId(), 1));
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
}
