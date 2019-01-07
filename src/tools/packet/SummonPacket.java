package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.util.List;
import org.apache.log4j.Logger;
import server.ServerProperties;
import server.maps.MapleSummon;
import server.movement.LifeMovementFragment;
import tools.AttackPair;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class SummonPacket {

    private static final Logger log = Logger.getLogger(SummonPacket.class);

    /**
     * 召唤召唤兽
     * @param summon
     * @param animated
     * @return
     */
    public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getSkillId());
        mplew.write(summon.getSkillLevel());
        mplew.writePos(summon.getPosition());
        mplew.write(0); // ?
        mplew.writeShort(0);
        mplew.write(summon.getMovementType().getValue());
        mplew.write(animated ? 1 : 0);

        return mplew.getPacket();
    }

    /**
     * 移除召唤兽
     * @param summon
     * @param animated
     * @return
     */
    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getSkillId());
        mplew.write(animated ? 4 : summon.getRemoveStatus());

        return mplew.getPacket();
    }

    /**
     * 移除召唤兽
     * @param chrId
     * @param skillID
     * @param startPos
     * @param moves
     * @return
     */
    public static byte[] moveSummon(int chrId, int skillID, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(skillID);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 召唤兽攻击
     * @param chrId
     * @param summonSkillId
     * @param animation
     * @param numAttackedAndDamage
     * @param allDamage
     * @param level
     * @param darkFlare
     * @return
     */
    public static byte[] summonAttack(int chrId, int summonSkillId, byte animation, byte numAttackedAndDamage, List<AttackPair> allDamage, int level, boolean darkFlare) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SUMMON_ATTACK.getValue());

        mplew.writeInt(chrId);
        mplew.writeInt(summonSkillId);
        mplew.write(animation);
        mplew.write(numAttackedAndDamage);

        for (AttackPair attackEntry : allDamage) {
            if (attackEntry.attack != null) {
                mplew.writeInt(attackEntry.objectid);
                mplew.write(6);
                for (Pair eachd : attackEntry.attack) {
                    if (((Boolean) eachd.right)) {
                        mplew.writeInt(((Integer) eachd.left) + -2147483648);
                    } else {
                        mplew.writeInt(((Integer) eachd.left));
                    }
                }
            }
        }
        mplew.writeLong(0);

        return mplew.getPacket();
    }

    public static byte[] summonSkill(int chrId, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);

        return mplew.getPacket();
    }

    /**
     * 召唤兽受到伤害
     * @param chrId
     * @param summonSkillId
     * @param damage
     * @param unkByte
     * @param monsterIdFrom
     * @return
     */
    public static byte[] damageSummon(int chrId, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(monsterIdFrom);
        mplew.writeInt(damage);
        mplew.write(0);

        return mplew.getPacket();
    }
}
