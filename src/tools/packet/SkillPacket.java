/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.util.List;

import handling.channel.handler.AttackInfo;
import server.Randomizer;
import server.maps.MechDoor;
import tools.AttackPair;
import tools.DateUtil;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * 技能相关的封包都写在这里面
 *
 * @author 7
 */
public class SkillPacket {

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
}
