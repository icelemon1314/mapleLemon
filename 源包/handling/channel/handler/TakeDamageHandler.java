package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.awt.Point;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class TakeDamageHandler {

    /**
     * 玩家受到伤害
     * @param slea
     * @param c
     * @param chr
     */
    public static void TakeDamage(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 1E
        // FF
        // 07 00 00 00
        // 00 A2 86 01
        // 00 35 FC 01 00 00 00
        //TODO 修复反射伤害给怪物 还有其他的掉血类型
        // 1E FE 19 00 00 00 00 00 高处掉落下来扣血
        if ((chr == null) || (chr.getMap() == null) || (chr.isHidden())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte type = slea.readByte();
        int damage = slea.readInt();
        boolean isDeadlyAttack = false;
        boolean pPhysical = false;
        int oid = 0;
        int monsteridfrom = 0;
        int fake = 0;
        int mpattack = 0;
        byte direction = 0;
        int pOid = 0;
        int pDamage = 0;
        byte pType = 0;
        Point pPos = new Point(0, 0);
        MapleMonster attacker = null;
        PlayerStats stats = chr.getStat();
        if (chr.isShowPacket()) {
            chr.dropMessage(5, "受伤类型: " + type + " 受伤数值: " + damage);
        }
        if (type == -1) { // 怪物伤害
            slea.readByte();
            oid = slea.readInt();
            attacker = chr.getMap().getMonsterByOid(oid);
//        MapleStatEffect.applyDoubleDefense(chr);
            if ((attacker == null)) {
                chr.dropMessage(5, "攻击着为空！");
                return;
            }
        } else if (type == -2) { // 高处掉落扣血
            slea.readShort();
        }

//        if (stats.reduceDamageRate > 0) {
//            damage = (int) (damage - damage * (stats.reduceDamageRate / 100.0D));
//        }
//        Pair modify = chr.modifyDamageTaken(damage, attacker);
//        damage = ((Double) modify.left).intValue();
        if (chr.isShowPacket()) {
            chr.dropMessage(5, "最终受到伤害 " + damage);
        }
        if (damage > 0) {
            if (chr.getBuffedValue(MapleBuffStat.变身术) != null) {
                chr.cancelMorphs();
            }
            boolean mpAttack = false;
            if (chr.getBuffedValue(MapleBuffStat.魔法盾) != null) {
                int hploss = 0;
                int mploss = 0;
                if (isDeadlyAttack) {
                    if (stats.getHp() > 1) {
                        hploss = stats.getHp() - 1;
                    }
                    if (stats.getMp() > 1) {
                        mploss = stats.getMp() - 1;
                    }
                    chr.addMPHP(-hploss, -mploss);
                } else {
                    mploss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.魔法盾).doubleValue() / 100.0D)) + mpattack;
                    hploss = damage - mploss;
                    if (mploss > stats.getMp()) {
                        mploss = stats.getMp();
                        hploss = damage - mploss + mpattack;
                    }
                    chr.addMPHP(-hploss, -mploss);
                }
            } else if (chr.getBuffedValue(MapleBuffStat.金钱护盾) != null) {
                int mesoloss = (int) Math.ceil(damage * chr.getStat().mesoGuard / 100.0D);
                if (chr.getStat().mesoGuardMeso >= mesoloss) {
                    chr.getStat().mesoGuardMeso = chr.getStat().mesoGuardMeso - mesoloss;
                } else {
                    chr.getStat().mesoGuardMeso = 0;
                    chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.金钱护盾});
                }
                damage = damage - mesoloss;
                if (chr.isShowPacket()) {
                    chr.dropMessage(5, "金钱护盾 - 最终伤害: " + damage + " 减少金币: " + mesoloss);
                }
                if (chr.getMeso() < mesoloss) {
                    chr.gainMeso(-chr.getMeso(), false);
                    chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.金钱护盾});
                } else {
                    chr.gainMeso(-mesoloss, false);
                }
                if ((isDeadlyAttack) && (stats.getMp() > 1)) {
                    mpattack = stats.getMp() - 1;
                }
                chr.addMPHP(-damage, -mpattack);
            } else if (isDeadlyAttack) {
                chr.addMPHP(stats.getHp() > 1 ? -(stats.getHp() - 1) : 0, (stats.getMp() > 1) && (!mpAttack) ? -(stats.getMp() - 1) : 0);
            } else {
                chr.addMPHP(-damage, mpAttack ? 0 : -mpattack);
            }
        }
        byte offset = 0;
        int offset_d = 0;
        if (chr.isShowPacket()) {
            chr.dropMessage(5, "玩家掉血 - 类型: " + type + " 怪物ID: " + monsteridfrom + " 伤害: " + damage + " fake: " + fake + " direction: " + direction + " oid: " + oid + " offset: " + offset);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.damagePlayer(chr.getId(), type, damage, monsteridfrom, direction, 0, pDamage, pPhysical, pOid, pType, pPos, offset, offset_d, fake), false);
    }
}
