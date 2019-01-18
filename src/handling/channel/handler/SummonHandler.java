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

import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.BuffPacket;
import tools.packet.MobPacket;
import tools.packet.SummonPacket;

public class SummonHandler {


    public static void MoveSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {

    }

    /**
     * 召唤物受到伤害
     * @param slea
     * @param chr
     */
    public static void DamageSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {

    }

    /**
     * 召唤兽攻击
     * @param slea
     * @param c
     * @param chr
     */
    public static void SummonAttack(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {

    }

    public static void RemoveSummon(SeekableLittleEndianAccessor slea, MapleClient c) {

    }

    public static void SubSummon(SeekableLittleEndianAccessor slea, MapleCharacter chr) {

    }
}
