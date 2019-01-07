/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packet;

import client.MapleCharacter;
import client.status.MonsterStatus;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.util.List;

import handling.channel.handler.AttackInfo;
import server.Randomizer;
import server.ServerProperties;
import server.maps.MapleArrowsTurret;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MechDoor;
import tools.AttackPair;
import tools.DateUtil;
import tools.HexTool;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * 技能相关的封包都写在这里面
 *
 * @author 7
 */
public class SkillPacket {

    public static byte[] sendEngagementRequest(String name, int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    public static byte[] sendEngagement(byte msg, int item, MapleCharacter male, MapleCharacter female) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ENGAGE_RESULT.getValue());
        mplew.write(msg);
        switch (msg) {
            case 13:
            case 14:
            case 17:
                mplew.writeInt(0);
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(msg == 14 ? 3 : 1);
                mplew.writeInt(item);
                mplew.writeInt(item);
                mplew.writeAsciiString(male.getName(), 13);
                mplew.writeAsciiString(female.getName(), 13);
            case 15:
            case 16:
        }

        return mplew.getPacket();
    }


    public static byte[] teslaTriangle(int chrId, int sum1, int sum2, int sum3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TESLA_TRIANGLE.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(sum1);
        mplew.writeInt(sum2);
        mplew.writeInt(sum3);

        return mplew.getPacket();
    }

    public static byte[] mechPortal(Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MECH_PORTAL.getValue());
        mplew.writePos(pos);

        return mplew.getPacket();
    }

    public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.writePos(md.getTruePosition());
        mplew.write(md.getId());
        mplew.writeInt(md.getPartyId());

        return mplew.getPacket();
    }

    public static byte[] removeMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.write(md.getId());

        return mplew.getPacket();
    }

    public static byte[] showForce(MapleCharacter chr, int oid, int forceCount, int forceColor) {//恶魔获得精气
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(1);
        mplew.writeInt(chr.getId());
        mplew.writeInt(oid);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(forceCount);

        mplew.writeInt(forceColor);
        mplew.writeInt(Randomizer.rand(36, 39));
        mplew.writeInt(Randomizer.rand(5, 6));
        mplew.writeInt(Randomizer.rand(33, 64));
        mplew.writeZero(12);
        mplew.writeInt(DateUtil.getTime(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateCardStack(int total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_CARTE.getValue());
        mplew.write(total);

        return mplew.getPacket();
    }

    public static byte[] gainCardStack(MapleCharacter chr, int oid, int skillId, int forceCount, int color) { //幻影卡片的效果
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(0);
        mplew.writeInt(chr.getId());
        mplew.writeInt(1);//mode 
        mplew.write(1);
        mplew.writeInt(oid);
        mplew.writeInt(skillId);
        mplew.write(1);
        mplew.writeInt(forceCount);
        mplew.writeInt(color);
        mplew.writeInt(28);
        mplew.writeInt(7);
        mplew.writeInt(9);
        mplew.writeZero(12);
        mplew.writeInt(DateUtil.getTime(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    //金钱炸弹封包
    public static byte[] MesosBomb(MapleCharacter chr,AttackInfo attack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(attack.numAttackedAndDamage);
        mplew.write(0x1E);//mode
        mplew.writeInt(attack.skillId);
        mplew.write(attack.stance);
        mplew.write(attack.direction);
        mplew.write(0);
        mplew.writeInt(0);
        for (AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                mplew.writeInt(p.objectid);
                mplew.write(0xFF);
                mplew.write(p.attack.size());
                for (Pair eachd : p.attack) {
                    mplew.writeInt((int)eachd.left);
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] gainCardStack(MapleCharacter chr, int oid, int skillId, int forceCount, int color, int times) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(0);
        mplew.writeInt(chr.getId());
        mplew.writeInt(color);
        mplew.write(1);
        mplew.writeInt(0);

        //mplew.writeInt(oid);
        mplew.writeInt(skillId);

        for (int i = 0; i < times; i++) {
            mplew.write(1);
            mplew.writeInt(forceCount + i);
            mplew.writeInt((skillId == 36001005) ? 0 : 2);
            mplew.writeInt(Randomizer.rand(15, 20));
            mplew.writeInt(Randomizer.rand(20, 30));
            mplew.writeInt(skillId == 36001005 || skillId == 24120002 ? Randomizer.rand(120, 150) : 0);
            mplew.writeInt(skillId == 24120002 ? 0 : Randomizer.rand(300, 900));
            mplew.writeZero(8);
            mplew.writeInt(DateUtil.getTime(System.currentTimeMillis()));
            mplew.writeInt(0);
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] gainAssassinStack(int chrId, int oid, int forceCount, boolean isAssassin, List<Integer> moboids, int visProjectile, Point posFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(1);
        mplew.writeInt(chrId);
        mplew.writeInt(oid);
        mplew.writeInt(11);
        mplew.write(1);
        mplew.writeInt(moboids.size());

        for (Integer moboid : moboids) {
            mplew.writeInt(moboid);
        }
        mplew.writeInt(isAssassin ? 4100012 : 4120019);
        for (int i = 0; i < moboids.size(); i++) {
            mplew.write(1);
            mplew.writeInt(forceCount + i);
            mplew.writeInt(isAssassin ? 1 : 2);
            mplew.writeInt(Randomizer.rand(32, 48));
            mplew.writeInt(Randomizer.rand(3, 4));
            mplew.writeInt(Randomizer.rand(100, 200));
            mplew.writeInt(200);
            mplew.writeZero(8);
            mplew.writeLong(8);
        }
        mplew.write(0);
        mplew.writeInt(posFrom.x - 120);
        mplew.writeInt(posFrom.y - 100);
        mplew.writeInt(posFrom.x + 120);
        mplew.writeInt(posFrom.y + 100);
        mplew.writeInt(visProjectile);

        return mplew.getPacket();
    }

    public static byte[] spawnArrowsTurret(MapleArrowsTurret summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SPAWN_ARROWS_TURRET.getValue());
        mplew.writeInt(summon.getObjectId());//Object_id  第几个
        mplew.writeInt(1);
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(0);
        mplew.writeInt(summon.getPosition().x);
        mplew.writeInt(summon.getPosition().y);
        mplew.write(summon.getSide()); //方向: 01 向右 00 向左
        return mplew.getPacket();
    }

    public static byte[] cancelArrowsTurret(MapleArrowsTurret summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CANCEL_ARROWS_TURRET.getValue());
        mplew.writeInt(1);
        mplew.writeInt(summon.getObjectId());
        return mplew.getPacket();//Object_id  第几个
    }

    public static byte[] ArrowsTurretAction(MapleArrowsTurret summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARROWS_TURRET_ACTION.getValue());
        mplew.writeInt(summon.getObjectId());//Object_id  第几个
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] ArrowsTurretAttack(int id, int mapid, int level, Point cpoint, int side, Point spoint, boolean sp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ARROWS_TURRET_ATTACK.getValue());
        mplew.writeShort(4);
        mplew.writeInt(id);//Object_id  第几个
        mplew.writeInt(mapid);
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeShort(cpoint.x + 25);
        mplew.writeShort(cpoint.y - 27);
        mplew.writeInt(900);
        mplew.writeShort(level);
        mplew.writeInt(sp ? 0 : 655370);
        mplew.writeInt(sp ? 95001000 : 3121013);
        mplew.write(side);//方向
        mplew.writeInt(side == 0 ? spoint.x : 0 - spoint.x);
        mplew.writeInt(spoint.y);
        return mplew.getPacket();
    }

    public static byte[] ShowQuiverKartrigeEffect(int chrid, int mode, int totle, boolean other) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (other) {
            mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
            mplew.writeInt(chrid);
        } else {
            mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        }
        mplew.write(57);
        mplew.writeInt(3101009);
        mplew.writeInt(mode - 1);
        mplew.writeInt(totle);
        return mplew.getPacket();
    }

    public static byte[] DrainSoul(MapleCharacter chr, int oid, List<Integer> mon, int forceCount, int count, int skillid, int dell, boolean frommon) {//灵魂吸取
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.writeBool(frommon);
        mplew.writeInt(chr.getId());
        if (frommon) {
            mplew.writeInt(oid);
        }
        mplew.writeInt(frommon ? 4 : 3);//type

        mplew.write(1);
        if (frommon) {
            mplew.writeInt(oid);
        } else {
            mplew.writeInt(mon.size());
            for (int i : mon) {
                mplew.writeInt(i);
            }
        }
        mplew.writeInt(skillid);
        mplew.write(1);
        for (int i = 0; i < (frommon ? 1 : count); i++) {
            mplew.writeInt(forceCount + i);
            mplew.writeInt(skillid == 31221014 ? 3 : 1);
            mplew.writeInt(frommon ? Randomizer.rand(0x20, 0x30) : Randomizer.rand(0x0F, 0x20));
            mplew.writeInt(frommon ? Randomizer.rand(3, 4) : Randomizer.rand(0x15, 0x30));
            mplew.writeInt(frommon ? Randomizer.rand(0x0, 0xFF) : Randomizer.rand(0x30, 0x50));
            mplew.writeInt(dell);
            mplew.writeZero(8);
            mplew.writeInt(DateUtil.getTime(System.currentTimeMillis()));
            mplew.writeInt(0);
            mplew.write(frommon ? 0 : (i == count - 1 ? 0 : 1));
        }
        return mplew.getPacket();
    }
    public static byte[] FireStep() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FIRE_STEP.getValue());
        mplew.writeInt(6850036);
        return mplew.getPacket();
    }

    public static byte[] ConveyTo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CONVEY_TO.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }
}
