package handling.world;

import client.MapleCharacter;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tools.packet.GuildPacket;

public class WorldGuildService {

    private final Map<Integer, MapleGuild> guildList;
    private final ReentrantReadWriteLock lock;

    public static WorldGuildService getInstance() {
        return SingletonHolder.instance;
    }

    private WorldGuildService() {
        this.lock = new ReentrantReadWriteLock();
        this.guildList = new LinkedHashMap();
    }

    public void addLoadedGuild(MapleGuild guild) {
        if (guild.isProper()) {
            this.guildList.put(guild.getId(), guild);
        }
    }

    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }

    public MapleGuild getGuild(int guildId) {
        MapleGuild ret = null;
        this.lock.readLock().lock();
        try {
            ret = (MapleGuild) this.guildList.get(guildId);
        } finally {
            this.lock.readLock().unlock();
        }
        if (ret == null) {
            this.lock.writeLock().lock();
            try {
                ret = new MapleGuild(guildId);
                if ((ret.getId() <= 0) || (!ret.isProper())) {
                    return null;
                }
                this.guildList.put(guildId, ret);
            } finally {
                this.lock.writeLock().unlock();
            }
        }
        return ret;
    }

    public MapleGuild getGuildByName(String guildName) {
        this.lock.readLock().lock();
        try {
            for (MapleGuild g : this.guildList.values()) {
                if (g.getName().equalsIgnoreCase(guildName)) {
                    MapleGuild localMapleGuild1 = g;
                    return localMapleGuild1;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public MapleGuild getGuild(MapleCharacter chr) {
        return getGuild(chr.getGuildId());
    }

    public void setGuildMemberOnline(MapleGuildCharacter guildMember, boolean isOnline, int channel) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            guild.setOnline(guildMember.getId(), isOnline, channel);
        }
    }

    public void guildPacket(int guildId, byte[] message) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.broadcast(message);
        }
    }

    public int addGuildMember(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            return guild.addGuildMember(guildMember);
        }
        return 0;
    }

    public int addGuildJoinMember(MapleGuildCharacter mc) {
        MapleGuild guild = getGuild(mc.getGuildId());
        if (guild != null) {
            return guild.addGuildJoinMember(mc);
        }
        return 0;
    }

    public int removeGuildJoinMember(MapleGuildCharacter mc) {
        MapleGuild guild = getGuild(mc.getGuildId());
        if (guild != null) {
            return guild.removeGuildJoinMember(mc.getId());
        }
        return 0;
    }

    public void leaveGuild(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            guild.leaveGuild(guildMember);
        }
    }

    public void guildChat(int guildId, String name, int chrId, String msg) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.guildChat(name, chrId, msg);
        }
    }

    public void changeRank(int guildId, int chrId, int newRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeRank(chrId, newRank);
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int chrId) {
        MapleGuild guild = getGuild(initiator.getGuildId());
        if (guild != null) {
            guild.expelMember(initiator, name, chrId);
        }
    }

    public void setGuildNotice(int guildId, String notice) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setGuildNotice(notice);
        }
    }

    public void setGuildLeader(int guildId, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeGuildLeader(chrId);
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter guildMember) {
        MapleGuild guild = getGuild(guildMember.getGuildId());
        if (guild != null) {
            guild.memberLevelJobUpdate(guildMember);
        }
    }

    public void changeRankTitle(int guildId, String[] ranks) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int guildId, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int guildId) {
        MapleGuild guild = getGuild(guildId);
        lock.writeLock().lock();
        try {
            if (guild != null) {
                guild.disbandGuild();
                guildList.remove(guildId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void deleteGuildCharacter(int guildId, int charId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            MapleGuildCharacter mc = guild.getMGC(charId);
            if (mc != null) {
                if (mc.getGuildRank() > 1) {
                    guild.leaveGuild(mc);
                } else {
                    guild.disbandGuild();
                }
            }
        }
    }

    public boolean increaseGuildCapacity(int guildId, boolean b) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.increaseCapacity(b);
        }
        return false;
    }

    public void gainGP(int guildId, int amount) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.gainGP(amount);
        }
    }

    public void gainGP(int guildId, int amount, int chrId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.gainGP(amount, false, chrId);
        }
    }

    public int getGP(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getGP();
        }
        return 0;
    }

    public int getInvitedId(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getInvitedId();
        }
        return 0;
    }

    public void setInvitedId(int guildId, int inviteId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.setInvitedId(inviteId);
        }
    }

    public int getGuildLeader(int guildName) {
        MapleGuild guild = getGuild(guildName);
        if (guild != null) {
            return guild.getLeaderId();
        }
        return 0;
    }

    public int getGuildLeader(String guildName) {
        MapleGuild guild = getGuildByName(guildName);
        if (guild != null) {
            return guild.getLeaderId();
        }
        return 0;
    }

    public void save() {
        this.lock.writeLock().lock();
        try {
            for (MapleGuild guild : this.guildList.values()) {
                guild.writeToDB(false);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public List<MapleBBSThread> getBBS(int guildId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.getBBS();
        }
        return null;
    }

    public int addBBSThread(int guildId, String title, String text, int icon, boolean bNotice, int posterId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            return guild.addBBSThread(title, text, icon, bNotice, posterId);
        }
        return -1;
    }

    public void editBBSThread(int guildId, int localthreadId, String title, String text, int icon, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.editBBSThread(localthreadId, title, text, icon, posterId, guildRank);
        }
    }

    public void deleteBBSThread(int guildId, int localthreadId, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.deleteBBSThread(localthreadId, posterId, guildRank);
        }
    }

    public void addBBSReply(int guildId, int localthreadId, String text, int posterId) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.addBBSReply(localthreadId, text, posterId);
        }
    }

    public void deleteBBSReply(int guildId, int localthreadId, int replyId, int posterId, int guildRank) {
        MapleGuild guild = getGuild(guildId);
        if (guild != null) {
            guild.deleteBBSReply(localthreadId, replyId, posterId, guildRank);
        }
    }

    public void changeEmblem(int gid, int affectedPlayers, MapleGuild mgs) {
        WorldBroadcastService.getInstance().sendGuildPacket(affectedPlayers, GuildPacket.guildEmblemChange(gid, (short) mgs.getLogoBG(), (byte) mgs.getLogoBGColor(), (short) mgs.getLogo(), (byte) mgs.getLogoColor()), -1, gid);
        setGuildAndRank(affectedPlayers, -1, -1, -1, -1);
    }

    public void setGuildAndRank(int chrId, int guildId, int rank, int contribution, int alliancerank) {
        int ch = WorldFindService.getInstance().findChannel(chrId);
        if (ch == -1) {
            return;
        }
        MapleCharacter player = getStorage(ch).getCharacterById(chrId);
        if (player == null) {
            return;
        }
        boolean isDifferentGuild;
        if ((guildId == -1) && (rank == -1)) {
            isDifferentGuild = true;
        } else {
            isDifferentGuild = guildId != player.getGuildId();
            player.setGuildId(guildId);
            player.setGuildRank((byte) rank);
            player.setGuildContribution(contribution);
            player.setAllianceRank((byte) alliancerank);
            player.saveGuildStatus();
        }
        if ((isDifferentGuild) && (ch > 0)) {
            player.getMap().broadcastMessage(player, GuildPacket.loadGuildName(player), false);
            player.getMap().broadcastMessage(player, GuildPacket.loadGuildIcon(player), false);
        }
    }

    public PlayerStorage getStorage(int channel) {
        if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }

    private static class SingletonHolder {

        protected static final WorldGuildService instance = new WorldGuildService();
    }
}
