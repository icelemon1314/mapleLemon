package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import constants.GameConstants;
import handling.MaplePacketHandler;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.EnumMap;
import java.util.Map;

public class DistributeApHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 2E 40 00 00 00
        Map statupdate = new EnumMap(MapleStat.class);
        MapleCharacter chr = c.getPlayer();
        c.sendPacket(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));

        PlayerStats stat = chr.getStat();
        int job = chr.getJob();
        if (chr.getRemainingAp() > 0) {
            switch (slea.readInt()) {
                case 0x40:
                    stat.setStr((short) (stat.getStr() + 1), chr);
                    statupdate.put(MapleStat.力量, (long) stat.getStr());
                    break;
                case 0x80:
                    stat.setDex((short) (stat.getDex() + 1), chr);
                    statupdate.put(MapleStat.敏捷, (long) stat.getDex());
                    break;
                case 0x100:
                    stat.setInt((short) (stat.getInt() + 1), chr);
                    statupdate.put(MapleStat.智力, (long) stat.getInt());
                    break;
                case 0x200:
                    stat.setLuk((short) (stat.getLuk() + 1), chr);
                    statupdate.put(MapleStat.运气, (long) stat.getLuk());
                    break;
                case 0x800:
                    int maxhp = stat.getMaxHp();
                    if (GameConstants.is新手职业(job)) {
                        maxhp += Randomizer.rand(8, 12);
                    } else if (((job >= 100) && (job <= 132)) ) {
                        maxhp += Randomizer.rand(36, 42);
                    } else if (((job >= 200) && (job <= 232))) {
                        maxhp += Randomizer.rand(10, 20);
                    } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434))) {
                        maxhp += Randomizer.rand(16, 20);
                    } else if (((job >= 510) && (job <= 512)) ) {
                        maxhp += Randomizer.rand(28, 32);
                    } else if (((job >= 500) && (job <= 532)) ) {
                        maxhp += Randomizer.rand(18, 22);
                    } else {
                        maxhp += Randomizer.rand(18, 26);
                    }
                    maxhp = Math.min(chr.getMaxHpForSever(), Math.abs(maxhp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxHp(maxhp, chr);
                    statupdate.put(MapleStat.MAXHP, (long) maxhp);
                    break;
                case 0x2000:
                    int maxmp = stat.getMaxMp();
                    if ((chr.getHpApUsed() >= 10000) || (stat.getMaxMp() >= chr.getMaxMpForSever())) {
                        return;
                    }
                    if (GameConstants.is新手职业(job)) {
                        maxmp += Randomizer.rand(6, 8);
                    } else {
                        if (((job >= 200) && (job <= 232))) {
                            maxmp += Randomizer.rand(38, 40);
                        } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)) || ((job >= 500) && (job <= 532))) {
                            maxmp += Randomizer.rand(10, 12);
                        } else if (((job >= 100) && (job <= 132))) {
                            maxmp += Randomizer.rand(6, 9);
                        } else {
                            maxmp += Randomizer.rand(6, 12);
                        }
                    }
                    maxmp = Math.min(chr.getMaxMpForSever(), Math.abs(maxmp));
                    chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                    stat.setMaxMp(maxmp, chr);
                    statupdate.put(MapleStat.MAXMP, (long) maxmp);
                    break;
                default:
                    c.sendPacket(MaplePacketCreator.updatePlayerStats(new EnumMap(MapleStat.class), true, chr));
                    return;
            }
            chr.setRemainingAp((short) (chr.getRemainingAp() - 1));
            statupdate.put(MapleStat.AVAILABLEAP, (long) chr.getRemainingAp());
            c.sendPacket(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));
        }
    }
}
