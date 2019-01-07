package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking;
import handling.world.WorldGuildService;
import handling.world.guild.MapleBBSReply;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildSkill;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class GuildPacket {

    private static final Logger log = Logger.getLogger(GuildPacket.class);

    public static byte[] showGuildInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x30);
        mplew.write(0);

        if ((chr == null) || (chr.getMGC() == null)) {
            mplew.write(0);
            return mplew.getPacket();
        }
        MapleGuild guild = WorldGuildService.getInstance().getGuild(chr.getGuildId());
        if (guild == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        mplew.write(1);
        getGuildInfo(mplew, guild);
        mplew.writeInt(25); //size
        mplew.writeInt(0);
        mplew.writeInt(15000);
        mplew.writeInt(60000);
        mplew.writeInt(135000);
        mplew.writeInt(240000);
        mplew.writeInt(375000);
        mplew.writeInt(540000);
        mplew.writeInt(735000);
        mplew.writeInt(960000);
        mplew.writeInt(1215000);
        mplew.writeInt(1500000);
        mplew.writeInt(1815000);
        mplew.writeInt(2160000);
        mplew.writeInt(2535000);
        mplew.writeInt(2940000);
        mplew.writeInt(3375000);
        mplew.writeInt(3840000);
        mplew.writeInt(4335000);
        mplew.writeInt(4860000);
        mplew.writeInt(5415000);
        mplew.writeInt(6000000);
        mplew.writeInt(6615000);
        mplew.writeInt(7260000);
        mplew.writeInt(7935000);
        mplew.writeInt(8640000);

        return mplew.getPacket();
    }

    private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        guild.addMemberForm(mplew);//118+
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP()); //家族经验
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
        mplew.write(guild.getLevel());
        mplew.writeShort(0);
        mplew.writeInt(0); //GP

        mplew.write(0);
        // 升级经验
        mplew.writeInt(25); //size
        mplew.writeInt(0);
        mplew.writeInt(15000);
        mplew.writeInt(60000);
        mplew.writeInt(135000);
        mplew.writeInt(240000);
        mplew.writeInt(375000);
        mplew.writeInt(540000);
        mplew.writeInt(735000);
        mplew.writeInt(960000);
        mplew.writeInt(1215000);
        mplew.writeInt(1500000);
        mplew.writeInt(1815000);
        mplew.writeInt(2160000);
        mplew.writeInt(2535000);
        mplew.writeInt(2940000);
        mplew.writeInt(3375000);
        mplew.writeInt(3840000);
        mplew.writeInt(4335000);
        mplew.writeInt(4860000);
        mplew.writeInt(5415000);
        mplew.writeInt(6000000);
        mplew.writeInt(6615000);
        mplew.writeInt(7260000);
        mplew.writeInt(7935000);
        mplew.writeInt(8640000);
    }

    public static byte[] guildSkillPurchased(int guildId, int skillId, int level, long expiration, String purchase, String activate) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());

        mplew.write(0x55);
        mplew.writeInt(guildId);
        mplew.writeInt(skillId);
        mplew.writeShort(level);
        mplew.writeLong(PacketHelper.getTime(expiration));
        mplew.writeMapleAsciiString(purchase);
        mplew.writeMapleAsciiString(activate);

        return mplew.getPacket();
    }

    public static byte[] guildLeaderChanged(int guildId, int oldLeader, int newLeader, int allianceId) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x59);
        mplew.writeInt(guildId);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        mplew.write(allianceId > 0 ? 1 : 0);
        if (allianceId > 0) {
            mplew.writeInt(allianceId);
        }

        return mplew.getPacket();
    }

    public static byte[] guildMemberOnline(int guildId, int chrId, boolean isOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x5C);
        mplew.writeInt(guildId);
        mplew.writeInt(chrId);
        mplew.write(isOnline ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] guildContribution(int guildId, int cid, int c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x61);
        mplew.writeInt(guildId);
        mplew.writeInt(cid);
        mplew.writeInt(c);
        mplew.writeInt(500); //117
        mplew.writeInt(350); //117
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis())); //117
        return mplew.getPacket();
    }

    public static byte[] createGuildNotice(String Name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeMapleAsciiString(Name);

        return mplew.getPacket();
    }

    public static byte[] guildInvite(int guildId, String charName, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x07);
        mplew.writeInt(guildId);
        mplew.writeMapleAsciiString(charName);
        mplew.writeInt(levelFrom);
        mplew.writeInt(jobFrom);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3d);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    public static byte[] genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);
        if (code == 87) {
            mplew.writeInt(0);
        }
        if ((code == 3) || (code == 0x30) || (code == 59) || (code == 60) || (code == 61) || (code == 84) || (code == 87)) {
            mplew.writeMapleAsciiString("");
        }

        return mplew.getPacket();
    }

    public static Object GuildReceipt(int guildId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x31);
        mplew.writeInt(guildId);
        MapleGuild guild = WorldGuildService.getInstance().getGuild(guildId);
        getGuildInfo(mplew, guild);
        return mplew.getPacket();
    }

    public static byte[] newGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        if ((c == null) || (c.getMGC() == null)) {
            return genericGuildMessage((byte) 37);
        }
        MapleGuild g = WorldGuildService.getInstance().getGuild(c.getGuildId());
        if (g == null) {
            return genericGuildMessage((byte) 37);
        }
        getGuildInfo(mplew, g);

        return mplew.getPacket();
    }

    public static byte[] newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3E); // 0x2D
        mplew.writeInt(mgc.getGuildId());
        guildMemberInfo(mplew, mgc);

        return mplew.getPacket();
    }

    public static byte[] newGuildJoinMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);
        mplew.writeInt(mgc.getGuildId());
        guildMemberInfo(mplew, mgc);

        return mplew.getPacket();
    }

    public static byte[] removeGuildJoin(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    private static void guildMemberInfo(MaplePacketLittleEndianWriter mplew, MapleGuildCharacter mgc) {
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(mgc.getName(), 13);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank());
        mplew.writeInt(mgc.isOnline() ? 1 : 0);
        mplew.writeInt(mgc.getAllianceRank());
        mplew.writeInt(mgc.getGuildContribution());
        mplew.writeInt(0);//可能是GP+IGP
        mplew.writeInt(0);//IGP
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
    }

    public static byte[] showSearchGuilds(List<MapleGuild> guilds) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_SEARCH.getValue());
        mplew.writeInt(guilds.size());
        for (MapleGuild guild : guilds) {
            mplew.writeInt(guild.getId());
            mplew.writeInt(guild.getLevel());
            mplew.writeMapleAsciiString(guild.getName());
            mplew.writeMapleAsciiString(guild.getMGC(guild.getLeaderId()).getName());
            mplew.writeInt(guild.getMembers().size());
            mplew.writeInt(guild.getAverageLevel());
        }

        return mplew.getPacket();
    }

    public static byte[] memberLeft(MapleGuildCharacter mgc, boolean isExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(isExpelled ? 0x4D : 0x4A); //0x35 : 0x32
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());

        return mplew.getPacket();
    }

    public static byte[] changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x46);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());

        return mplew.getPacket();
    }

    public static byte[] guildNotice(int guildId, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x4b);
        mplew.writeInt(guildId);
        mplew.writeMapleAsciiString(notice);

        return mplew.getPacket();
    }

    public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x5B);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        return mplew.getPacket();
    }

    public static byte[] rankTitleChange(int guildId, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x5B); //0x44
        mplew.writeInt(guildId);
        for (String r : ranks) {
            mplew.writeMapleAsciiString(r);
        }

        return mplew.getPacket();
    }

    public static byte[] guildDisband(int guildId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x50); // 家族解散
        mplew.writeInt(guildId);
//        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] guildEmblemChange(int guildId, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x60); //0x49
        mplew.writeInt(guildId);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);

        return mplew.getPacket();
    }

    public static byte[] guildCapacityChange(int guildId, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(guildId);
        mplew.write(capacity);

        return mplew.getPacket();
    }

    public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(2);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);

        return mplew.getPacket();
    }

    public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(25);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);

        return mplew.getPacket();
    }

    public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(3);
        mplew.writeInt(inviter.getGuildId());
        mplew.writeMapleAsciiString(inviter.getName());

        mplew.writeMapleAsciiString(allianceName);

        return mplew.getPacket();
    }

    public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(5);
        mplew.writeInt(allianceid);
        mplew.writeInt(player.getId());
        mplew.writeInt(player.getAllianceRank());

        return mplew.getPacket();
    }

    public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(14);
        mplew.writeInt(alliance);
        mplew.writeInt(gid);
        mplew.writeInt(id);
        mplew.write(online ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(24);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());

        return mplew.getPacket();
    }

    public static byte[] updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(27);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getAllianceRank());

        return mplew.getPacket();
    }

    public static byte[] disbandAlliance(int alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(29);
        mplew.writeInt(alliance);

        return mplew.getPacket();
    }

    public static byte[] BBSThreadList(List<MapleBBSThread> bbs, int start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(6);

        if (bbs == null) {
            mplew.write(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (MapleBBSThread b : bbs) {
            if (b.isNotice()) {
                notice = b;
                break;
            }
        }
        mplew.write(notice == null ? 0 : 1);
        if (notice != null) {
            addThread(mplew, notice);
        }
        if (threadCount < start) {
            start = 0;
        }

        mplew.writeInt(threadCount);
        int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);

        for (int i = 0; i < pages; i++) {
            addThread(mplew, (MapleBBSThread) bbs.get(start + i));
        }
        return mplew.getPacket();
    }

    private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }

    public static byte[] showThread(MapleBBSThread thread) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(7);

        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        return mplew.getPacket();
    }

    public static byte[] showGuildRanks(int npcid, List<MapleGuildRanking.GuildRankingInfo> all, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x69);
        mplew.writeInt(npcid);

        mplew.writeInt(show ? all.size() : 0);
        if (show) {
            for (MapleGuildRanking.GuildRankingInfo info : all) {
                mplew.writeShort(0);//118+
                mplew.writeMapleAsciiString(info.getName());
                mplew.writeInt(info.getGP());
                mplew.writeInt(info.getLogo());
                mplew.writeInt(info.getLogoColor());
                mplew.writeInt(info.getLogoBg());
                mplew.writeInt(info.getLogoBgColor());
            }
        }

        return mplew.getPacket();
    }

    public static byte[] updateGP(int guildId, int GP, int guildlevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x68);
        mplew.writeInt(guildId);
        mplew.writeInt(GP);
        mplew.writeInt(guildlevel);
        mplew.writeInt(GP);

        return mplew.getPacket();
    }

    public static byte[] loadGuildName(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
        mplew.writeInt(chr.getId());
        if (chr.getGuildId() <= 0) {
            mplew.writeShort(0);
        } else {
            MapleGuild guild = WorldGuildService.getInstance().getGuild(chr.getGuildId());
            if (guild != null) {
                mplew.writeMapleAsciiString(guild.getName());
            } else {
                mplew.writeShort(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] loadGuildIcon(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
        mplew.writeInt(chr.getId());
        if (chr.getGuildId() <= 0) {
            mplew.writeZero(6);
        } else {
            MapleGuild guild = WorldGuildService.getInstance().getGuild(chr.getGuildId());
            if (guild != null) {
                mplew.writeShort(guild.getLogoBG());
                mplew.write(guild.getLogoBGColor());
                mplew.writeShort(guild.getLogo());
                mplew.write(guild.getLogoColor());
            } else {
                mplew.writeZero(6);
            }
        }
        return mplew.getPacket();
    }
}
