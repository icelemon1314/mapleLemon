package tools.packet;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleDisease;
import client.SpecialBuffInfo;
import constants.GameConstants;
import handling.SendPacketOpcode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import server.MapleStatEffect;
import server.ServerProperties;
import tools.DateUtil;
import tools.HexTool;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class BuffPacket {

    private static final Logger log = Logger.getLogger(BuffPacket.class);

    public static byte[] giveDice(int buffid, int skillid, int duration, List<Pair<MapleBuffStat, Integer>> statups, MapleCharacter chr) {
        int value = 0;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeBuffMask(mplew, statups);

        int dice = buffid >= 100 ? buffid / 100 : buffid;
        mplew.writeShort(dice);

        mplew.writeInt(skillid);
        mplew.writeInt(duration);
        mplew.writeZero(5);

        mplew.writeInt(GameConstants.getDiceStat(dice, 3));
        mplew.writeInt(GameConstants.getDiceStat(dice, 3));
        mplew.writeInt(GameConstants.getDiceStat(dice, 4));
        mplew.writeZero(20);
        mplew.writeInt(GameConstants.getDiceStat(dice, 2));
        mplew.writeZero(12);
        mplew.writeInt(GameConstants.getDiceStat(dice, 5));
        mplew.writeZero(16);
        mplew.writeInt(GameConstants.getDiceStat(dice, 6));
        mplew.writeZero(16);
        mplew.writeInt(value);
        mplew.writeInt(1000);
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showMonsterRiding(int chrId, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeBuffMask(mplew, statups);
        mplew.writeZero(16);
        mplew.writeZero(7);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeZero(7);

        return mplew.getPacket();
    }

    public static byte[] givePirateBuff(List<Pair<MapleBuffStat, Integer>> statups, int duration, int skillid, MapleCharacter chr) {
        boolean infusion = (skillid == 5121009) || (skillid == 15121005) || (skillid % 10000 == 8006);
        int value = 0;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeBuffMask(mplew, statups);
        mplew.writeZero(5);
        mplew.writeInt(value);

        for (Pair stat : statups) {
            mplew.writeInt(((Integer) stat.getRight()));
            mplew.writeLong(skillid);
            mplew.writeZero(infusion ? 6 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeInt(infusion ? 600 : 0);
        mplew.write(1);
        if (!infusion) {
            mplew.write(4);
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] giveForeignDash(List<Pair<MapleBuffStat, Integer>> statups, int duration, int chrId, int skillid) {
        boolean infusion = (skillid == 5121009) || (skillid == 15121005) || (skillid % 10000 == 8006);
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeBuffMask(mplew, statups);
        if (!infusion) {
            mplew.writeZero(16);
        }
        mplew.writeZero(7);
        for (Pair stat : statups) {
            mplew.writeInt(((Integer) stat.getRight()));
            mplew.writeLong(skillid);
            mplew.writeZero(infusion ? 6 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] give无敌(int skillId, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeShort(1);
        mplew.writeInt(skillId);
        mplew.writeInt(duration);
        mplew.writeZero(18);
        return mplew.getPacket();
    }

    public static byte[] give神秘瞄准术(int x, int skillId, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeShort(x);
        mplew.writeInt(skillId);
        mplew.writeInt(duration);
        mplew.writeZero(18);

        return mplew.getPacket();
    }

    public static byte[] giveEnergyCharge(int bar, int buffId, boolean fullbar, boolean consume) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeZero(5);
        mplew.writeInt((fullbar) || ((consume) && (bar > 0)) ? buffId : 0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeZero(6);
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showEnergyCharge(int chrId, int bar, int buffId, boolean fullbar, boolean consume) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        mplew.writeZero(19);
        mplew.writeInt((fullbar) || ((consume) && (bar > 0)) ? buffId : 0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeZero(11);

        return mplew.getPacket();
    }

    public static byte[] updateLuminousGauge(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LUMINOUS_COMBO.getValue());
        mplew.writeInt(chr.getDarkTotal());
        mplew.writeInt(chr.getLightTotal());
        mplew.writeInt(chr.getDarkType());
        mplew.writeInt(chr.getLightType());
        mplew.write(new byte[]{79, 23, -106, -113});

        return mplew.getPacket();
    }



    public static byte[] startPower(boolean start, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeInt(start ? 1 : 0);
        mplew.writeInt(7200);
        mplew.writeInt(6);
        mplew.writeInt(time);
        mplew.writeZero(18);

        return mplew.getPacket();
    }

    public static byte[] showstartPower(int chrId, boolean start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(start ? 1 : 0);
        mplew.writeInt(7200);
        mplew.writeInt(6);
        mplew.writeInt(0);
        mplew.writeZero(18);

        return mplew.getPacket();
    }

    public static byte[] updatePowerCount(int skillId, int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeShort(count);
        mplew.writeInt(skillId);
        mplew.writeInt(2100000000);
        mplew.writeZero(18);

        return mplew.getPacket();
    }

    public static byte[] 取消守护模式变更() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_BUFF.getValue());

        Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
        PacketHelper.writeBuffMask(mplew, statups);
        return mplew.getPacket();
    }

    public static byte[] 灵魂助力特殊(int time, boolean isScream, boolean isControl) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.灵魂助力);
        mplew.writeShort(1);
        mplew.writeInt(1301013);
        mplew.writeInt(time);
        mplew.writeInt(0);
        mplew.write(9);//???
        mplew.writeInt(isControl ? 1311013 : 1301013);
        mplew.writeInt(isScream ? 1311014 : 0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] giveBuff(int buffid, int duration, List<Pair<MapleBuffStat, Integer>> statups) {
        //TODO giveBuff 需要整理
        // 1A
        // 02 00 00 00
        // 00 00
        // 2B 46 0F 00
        // E0 93
        // 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeBuffMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid); // 技能ID
            mplew.writeShort(duration); // 持续时间
        }
        mplew.writeShort(0);
        //int mask = 0x40020180;
        //if (( mask & 0x40020180) == 1){
            mplew.write(0);
        //}

        return mplew.getPacket();
    }

    public static byte[] giveDebuff(MapleDisease statups, int x, int skillid, int level, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, statups);
        mplew.writeShort(x);
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeInt(duration);
        mplew.writeZero(5);
        mplew.writeZero(30);

        return mplew.getPacket();
    }

    public static byte[] giveForeignDebuff(int chrId, MapleDisease statups, int skillid, int level, int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeSingleMask(mplew, statups);
        if (skillid == 125) {
            mplew.writeShort(0);
            mplew.write(0);
        }
        mplew.writeShort(x);
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeZero(3);
        mplew.writeZero(16);
        mplew.writeZero(4);
        mplew.writeShort(900);

        return mplew.getPacket();
    }

    public static byte[] cancelForeignDebuff(int chrId, MapleDisease mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeSingleMask(mplew, mask);
        //mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] cancelForeignBuff(int chrId, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeMask(mplew, statups);
        //mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] cancelForeignBuff(int chrId, MapleBuffStat buffstat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeSingleMask(mplew, buffstat);
        //mplew.write(3);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] giveForeignBuff(int chrId, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {//TODO 给其他玩家BUFF 效果
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(chrId);
        PacketHelper.writeBuffMask(mplew, statups);
        for (Pair statup : statups) {
            if (isNeedValue(statup)) {
                mplew.writeShort(((Integer) statup.getRight()).shortValue());
                mplew.writeInt(effect.isSkill() ? effect.getSourceId() : -effect.getSourceId());
            } else {
                mplew.writeShort(((Integer) statup.getRight()).shortValue());
            }
        }
        mplew.writeZero(23);
        mplew.writeShort(0);
        //mplew.write(1);

        return mplew.getPacket();
    }

    public static boolean isNeedValue(Pair<MapleBuffStat, Integer> statup) {
        return statup.getLeft() == MapleBuffStat.影分身
                || statup.getLeft() == MapleBuffStat.GIANT_POTION
                || statup.getLeft() == MapleBuffStat.属性攻击
                ;
    }

    public static byte[] cancelBuff(List<MapleBuffStat> statups, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketHelper.writeMask(mplew, statups);
        for (MapleBuffStat mask : statups) {
            if (mask == MapleBuffStat.骑兽技能) {
                continue;
            }
            List<SpecialBuffInfo> buffs = chr.getSpecialBuffInfo(mask);
            mplew.writeInt(buffs.size());
            for (SpecialBuffInfo info : buffs) {
                mplew.writeInt(info.buffid);
                mplew.writeLong(info.value);
                mplew.writeInt(0);
                mplew.writeInt(info.bufflength);
            }
        }
        if (statups.contains(MapleBuffStat.骑兽技能)) {
            mplew.write(3);
            mplew.write(1);
        } else if ((statups.contains(MapleBuffStat.移动速度)) || (statups.contains(MapleBuffStat.跳跃力))) {
            mplew.write(3);
        }else if (statups.contains(MapleBuffStat.暴击概率增加)) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] cancelBuff(MapleBuffStat buffstat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, buffstat);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] cancelDebuff(MapleDisease mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, mask);
        mplew.write(3);
        mplew.write(0);
        mplew.write(1);

        return mplew.getPacket();
    }

}
