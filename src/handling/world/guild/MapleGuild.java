package handling.world.guild;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.SkillFactory;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.world.WorldBroadcastService;
import handling.world.WorldGuildService;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import server.MapleStatEffect;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.GuildPacket;
import tools.packet.PacketHelper;
import tools.packet.UIPacket;

public class MapleGuild
        implements Serializable {

    public static final long serialVersionUID = 6322150443228168192L;
    private final List<MapleGuildCharacter> members = new CopyOnWriteArrayList();
    private final String[] rankTitles = new String[5];
    private String name;
    private String notice;
    private int id;
    private int gp;
    private int logo;
    private int logoColor;
    private int leader;
    private int capacity;
    private int logoBG;
    private int logoBGColor;
    private int signature;
    private int level;
    private boolean bDirty = true;
    private boolean proper = true;
    private int allianceid = 0;
    private int invitedid = 0;
    private final Map<Integer, MapleBBSThread> bbs = new HashMap();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean init = false;
    private boolean changed = false;
    private static final Logger log = Logger.getLogger(MapleGuild.class);
    private List<Integer> joinList = new ArrayList<>();

    public MapleGuild(int guildid) {
        this(guildid, null);
    }

    public MapleGuild(int guildid, Map<Integer, Map<Integer, MapleBBSReply>> replies) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM questinfo WHERE customData = ?");
            ps.setString(1, "GuildID=" + guildid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                addJoinList(rs.getInt("characterid"));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                this.id = -1;
                return;
            }
            this.id = guildid;
            this.name = rs.getString("name");
            this.gp = rs.getInt("GP");
            this.logo = rs.getInt("logo");
            this.logoColor = rs.getInt("logoColor");
            this.logoBG = rs.getInt("logoBG");
            this.logoBGColor = rs.getInt("logoBGColor");
            this.capacity = rs.getInt("capacity");
            this.rankTitles[0] = rs.getString("rank1title");
            this.rankTitles[1] = rs.getString("rank2title");
            this.rankTitles[2] = rs.getString("rank3title");
            this.rankTitles[3] = rs.getString("rank4title");
            this.rankTitles[4] = rs.getString("rank5title");
            this.leader = rs.getInt("leader");
            this.notice = rs.getString("notice");
            this.signature = rs.getInt("signature");
            this.allianceid = rs.getInt("alliance");
            rs.close();
            ps.close();

            this.allianceid = 0;

            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, guildContribution, alliancerank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC", 1008);
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                System.err.println(new StringBuilder().append("家族ID: ").append(this.id).append(" 没用成员，系统自动解散该家族。").toString());
                rs.close();
                ps.close();
                writeToDB(true);
                this.proper = false;
                return;
            }
            boolean leaderCheck = false;
            byte gFix = 0;
            byte aFix = 0;
            do {
                int chrId = rs.getInt("id");
                byte gRank = rs.getByte("guildrank");
                byte aRank = rs.getByte("alliancerank");

                if (chrId == this.leader) {
                    leaderCheck = true;
                    if (gRank != 1) {
                        gRank = 1;
                        gFix = 1;
                    }
                } else {
                    if (gRank == 1) {
                        gRank = 2;
                        gFix = 2;
                    }
                    if (aRank < 3) {
                        aRank = 3;
                        aFix = 3;
                    }
                }
                this.members.add(new MapleGuildCharacter(chrId, rs.getShort("level"), rs.getString("name"), (byte) -1, rs.getInt("job"), gRank, rs.getInt("guildContribution"), aRank, guildid, false));
            } while (rs.next());
            rs.close();
            ps.close();

            if (!leaderCheck) {
                System.err.println(new StringBuilder().append("族长[ ").append(this.leader).append(" ]没有在家族ID为 ").append(this.id).append(" 的家族中，系统自动解散这个家族。").toString());
                writeToDB(true);
                this.proper = false;
                return;
            }

            if (gFix > 0) {
                ps = con.prepareStatement("UPDATE characters SET guildrank = ? WHERE id = ?");
                ps.setByte(1, gFix);
                ps.setInt(2, this.leader);
                ps.executeUpdate();
                ps.close();
            }

            if (aFix > 0) {
                ps = con.prepareStatement("UPDATE characters SET alliancerank = ? WHERE id = ?");
                ps.setByte(1, aFix);
                ps.setInt(2, this.leader);
                ps.executeUpdate();
                ps.close();
            }

            ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("localthreadid");
                MapleBBSThread thread = new MapleBBSThread(tID, rs.getString("name"), rs.getString("startpost"), rs.getLong("timestamp"), guildid, rs.getInt("postercid"), rs.getInt("icon"));
                if ((replies != null) && (replies.containsKey(rs.getInt("threadid")))) {
                    thread.replies.putAll((Map) replies.get(rs.getInt("threadid")));
                }
                this.bbs.put(tID, thread);
            }
            rs.close();
            ps.close();

            this.level = calculateLevel();
        } catch (SQLException se) {
            log.error(new StringBuilder().append("[MapleGuild] 从数据库中加载家族信息出错.").append(se).toString());
        }
    }

    public boolean isProper() {
        return this.proper;
    }

    public static void loadAll() {
        Map replies = new LinkedHashMap();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_replies");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("threadid");
                Map reply = (Map) replies.get(Integer.valueOf(tID));
                if (reply == null) {
                    reply = new HashMap();
                    replies.put(tID, reply);
                }
                reply.put(reply.size(), new MapleBBSReply(reply.size(), rs.getInt("postercid"), rs.getString("content"), rs.getLong("timestamp")));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT guildid FROM guilds");
            rs = ps.executeQuery();
            while (rs.next()) {
                WorldGuildService.getInstance().addLoadedGuild(new MapleGuild(rs.getInt("guildid"), replies));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            log.error(new StringBuilder().append("[MapleGuild] 从数据库中加载家族信息出错.").append(se).toString());
        }
    }

    public static void loadAll(Object toNotify) {
        Map replies = new LinkedHashMap();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_replies");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("threadid");
                Map reply = (Map) replies.get(Integer.valueOf(tID));
                if (reply == null) {
                    reply = new HashMap();
                    replies.put(tID, reply);
                }
                reply.put(reply.size(), new MapleBBSReply(reply.size(), rs.getInt("postercid"), rs.getString("content"), rs.getLong("timestamp")));
            }
            rs.close();
            ps.close();
            boolean cont = false;
            ps = con.prepareStatement("SELECT guildid FROM guilds");
            rs = ps.executeQuery();
            while (rs.next()) {
                GuildLoad.QueueGuildForLoad(rs.getInt("guildid"), replies);
                cont = true;
            }
            rs.close();
            ps.close();
            if (!cont) {
                return;
            }
        } catch (SQLException se) {
            log.error(new StringBuilder().append("[MapleGuild] 从数据库中加载家族信息出错.").append(se).toString());
        }
        AtomicInteger FinishedThreads = new AtomicInteger(0);
        GuildLoad.Execute(toNotify);
        synchronized (toNotify) {
            try {
                toNotify.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        while (FinishedThreads.incrementAndGet() != 6) {
            synchronized (toNotify) {
                try {
                    toNotify.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public final void writeToDB(boolean bDisband) {
        int ourId;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (!bDisband) {
                StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
                for (int i = 1; i < 6; i++) {
                    buf.append("rank").append(i).append("title = ?, ");
                }
                buf.append("capacity = ?, notice = ?, alliance = ?, leader = ? WHERE guildid = ?");

                PreparedStatement ps = con.prepareStatement(buf.toString());
                ps.setInt(1, this.gp);
                ps.setInt(2, this.logo);
                ps.setInt(3, this.logoColor);
                ps.setInt(4, this.logoBG);
                ps.setInt(5, this.logoBGColor);
                ps.setString(6, this.rankTitles[0]);
                ps.setString(7, this.rankTitles[1]);
                ps.setString(8, this.rankTitles[2]);
                ps.setString(9, this.rankTitles[3]);
                ps.setString(10, this.rankTitles[4]);
                ps.setInt(11, this.capacity);
                ps.setString(12, this.notice);
                ps.setInt(13, this.allianceid);
                ps.setInt(14, this.leader);
                ps.setInt(15, this.id);
                ps.executeUpdate();
                ps.close();

                if (this.changed) {
                    ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();

                    ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                    ps.setInt(1, this.id);
                    ps.execute();
                    ps.close();

                    try (PreparedStatement pse = con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`, `content`, `guildid`) VALUES (?, ?, ?, ?, ?)")) {
                        ps = con.prepareStatement("INSERT INTO bbs_threads(`postercid`, `name`, `timestamp`, `icon`, `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?, ?, ?, ?)", 1);
                        ps.setInt(6, this.id);
                        for (MapleBBSThread bb : this.bbs.values()) {
                            ps.setInt(1, bb.ownerID);
                            ps.setString(2, bb.name);
                            ps.setLong(3, bb.timestamp);
                            ps.setInt(4, bb.icon);
                            ps.setString(5, bb.text);
                            ps.setInt(7, bb.localthreadID);
                            ps.execute();
                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                if (!rs.next()) {
                                    rs.close();
                                    continue;
                                }
                                ourId = rs.getInt(1);
                            }
                            pse.setInt(5, this.id);
                            for (MapleBBSReply r : bb.replies.values()) {
                                pse.setInt(1, ourId);
                                pse.setInt(2, r.ownerID);
                                pse.setLong(3, r.timestamp);
                                pse.setString(4, r.content);
                                pse.addBatch();
                            }
                        }
                        pse.executeBatch();
                        pse.close();
                    }
                    ps.close();
                }
                this.changed = false;
            } else {
                PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.executeUpdate();
                ps.close();

                broadcast(GuildPacket.guildDisband(this.id));
            }
        } catch (SQLException se) {
            log.error(new StringBuilder().append("[MapleGuild] 保存家族信息出错.").append(se).toString());
        }
    }

    public int getId() {
        return this.id;
    }

    public int getLeaderId() {
        return this.leader;
    }

    public MapleCharacter getLeader(MapleClient c) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(this.leader);
    }

    public int getGP() {
        return this.gp;
    }

    public int getLogo() {
        return this.logo;
    }

    public void setLogo(int l) {
        this.logo = l;
    }

    public int getLogoColor() {
        return this.logoColor;
    }

    public void setLogoColor(int c) {
        this.logoColor = c;
    }

    public int getLogoBG() {
        return this.logoBG;
    }

    public void setLogoBG(int bg) {
        this.logoBG = bg;
    }

    public int getLogoBGColor() {
        return this.logoBGColor;
    }

    public void setLogoBGColor(int c) {
        this.logoBGColor = c;
    }

    public String getNotice() {
        if (this.notice == null) {
            return "";
        }
        return this.notice;
    }

    public String getName() {
        return this.name;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getSignature() {
        return this.signature;
    }

    public void broadcast(byte[] packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public void broadcast(byte[] packet, int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    // multi-purpose function that reaches every member of guild (except the character with exceptionId) in all channels with as little access to rmi as possible
    public void broadcast(byte[] packet, int exceptionId, BCOp bcop) {
        lock.writeLock().lock();
        try {
            buildNotifications();
        } finally {
            lock.writeLock().unlock();
        }
        lock.readLock().lock();
        try {
            for (MapleGuildCharacter mgc : this.members) {
                if (bcop == BCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), 0, 5, 0, 5);
                    } else {
                        setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, mgc.getId());
                    }
                } else if (mgc.isOnline() && (mgc.getId() != exceptionId)) {
                    if (bcop == BCOp.EMBELMCHANGE) {
                        WorldGuildService.getInstance().changeEmblem(this.id, mgc.getId(), this);
                    } else {
                        WorldBroadcastService.getInstance().sendGuildPacket(mgc.getId(), packet, exceptionId, this.id);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void buildNotifications() {
        if (!this.bDirty) {
            return;
        }
        List mem = new LinkedList();
        for (MapleGuildCharacter mgc : this.members) {
            if (!mgc.isOnline()) {
                continue;
            }
            if ((mem.contains(mgc.getId())) || (mgc.getGuildId() != this.id)) {
                this.members.remove(mgc);
                continue;
            }
            mem.add(mgc.getId());
        }

        this.bDirty = false;
    }

    public void setOnline(int cid, boolean online, int channel) {
        boolean bBroadcast = true;
        for (MapleGuildCharacter mgc : this.members) {
            if ((mgc.getGuildId() == this.id) && (mgc.getId() == cid)) {
                if (mgc.isOnline() == online) {
                    bBroadcast = false;
                }
                mgc.setOnline(online);
                mgc.setChannel((byte) channel);
                break;
            }
        }
        if (bBroadcast) {
            broadcast(GuildPacket.guildMemberOnline(this.id, cid, online), cid);
        }
        this.bDirty = true;
        this.init = true;
    }

    public void guildChat(String name, int cid, String msg) {
        broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }

    public void allianceChat(String name, int cid, String msg) {
        broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
    }

    public String getRankTitle(int rank) {
        return this.rankTitles[(rank - 1)];
    }

    public int getAllianceId() {
        return this.allianceid;
    }

    public int getInvitedId() {
        return this.invitedid;
    }

    public void setInvitedId(int iid) {
        this.invitedid = iid;
    }

    public void setAllianceId(int a) {
        this.allianceid = a;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?")) {
                ps.setInt(1, a);
                ps.setInt(2, this.id);
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            log.error(new StringBuilder().append("[MapleGuild] 保存家族联盟信息出错.").append(e).toString());
        }
    }

    public static int createGuild(int leaderId, String name) {
        if (name.length() > 12) {
            return 0;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.first()) {
                rs.close();
                ps.close();
                return 0;
            }
            ps.close();
            rs.close();

            ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES (?, ?, ?, 0)", 1);
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) (System.currentTimeMillis() / 1000L));
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            int ret = 0;
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException se) {
            log.error(new StringBuilder().append("[MapleGuild] 创建家族信息出错.").append(se).toString());
        }
        return 0;
    }

    public int addGuildMember(MapleGuildCharacter mgc) {
        this.lock.writeLock().lock();
        try {
            if (this.members.size() >= this.capacity) {
                int i = 0;
                return i;
            }
            for (int i = this.members.size() - 1; i >= 0; i--) {
                if ((((MapleGuildCharacter) this.members.get(i)).getGuildRank() < 5) || (((MapleGuildCharacter) this.members.get(i)).getName().compareTo(mgc.getName()) < 0)) {
                    this.members.add(i + 1, mgc);
                    this.bDirty = true;
                    break;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        gainGP(500, true, mgc.getId());
        setGuildQuest(false, MapleCharacter.getCharacterById(mgc.getId()));
        broadcast(GuildPacket.newGuildMember(mgc));
        return 1;
    }

    public final int addGuildJoinMember(final MapleGuildCharacter mgc) {
        lock.writeLock().lock();
        try {
            if (members.size() >= capacity) {
                return 0;
            }
        } finally {
            lock.writeLock().unlock();
        }
        setGuildQuest(true, MapleCharacter.getCharacterById(mgc.getId()));
        broadcast(GuildPacket.newGuildJoinMember(mgc));
        return 1;
    }

    public final int removeGuildJoinMember(final int cid) {
        setGuildQuest(false, MapleCharacter.getCharacterById(cid));
        broadcast(GuildPacket.removeGuildJoin(cid));
        return 1;
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        this.lock.writeLock().lock();
        try {
            for (MapleGuildCharacter mgcc : this.members) {
                if (mgcc.getId() == mgc.getId()) {
                    broadcast(GuildPacket.memberLeft(mgcc, false));
                    this.bDirty = true;
                    gainGP(mgcc.getGuildContribution() > 0 ? -mgcc.getGuildContribution() : -50);
                    this.members.remove(mgcc);
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(mgcc.getId(), 0, 5, 0, 5);
                        break;
                    }
                    setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, mgcc.getId());

                    break;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int chrId) {
        this.lock.writeLock().lock();
        try {
            for (MapleGuildCharacter mgc : this.members) {
                if ((mgc.getId() == chrId) && (initiator.getGuildRank() < mgc.getGuildRank())) {
                    broadcast(GuildPacket.memberLeft(mgc, true));
                    this.bDirty = true;
                    gainGP(mgc.getGuildContribution() > 0 ? -mgc.getGuildContribution() : -50);
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(chrId, 0, 5, 0, 5);
                    } else {
                        MapleCharacterUtil.sendNote(mgc.getName(), initiator.getName(), "被家族除名了。", 0);
                        setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, chrId);
                    }
                    this.members.remove(mgc);
                    break;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void changeARank() {
        changeARank(false);
    }

    public void changeARank(boolean leader) {
        if (this.allianceid <= 0) {
            return;
        }
        for (MapleGuildCharacter mgc : this.members) {
            byte newRank = 3;
            if (this.leader == mgc.getId()) {
                newRank = (byte) (leader ? 1 : 2);
            }
            if (mgc.isOnline()) {
                WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
                setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank, mgc.getId());
            }
            mgc.setAllianceRank(newRank);
        }
    }

    public void changeARank(int newRank) {
        if (this.allianceid <= 0) {
            return;
        }
        for (MapleGuildCharacter mgc : this.members) {
            if (mgc.isOnline()) {
                WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
                setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte) newRank, mgc.getId());
            }
            mgc.setAllianceRank((byte) newRank);
        }
    }

    public boolean changeARank(int chrId, int newRank) {
        if (this.allianceid <= 0) {
            return false;
        }
        for (MapleGuildCharacter mgc : this.members) {
            if (chrId == mgc.getId()) {
                if (mgc.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
                } else {
                    setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte) newRank, chrId);
                }
                mgc.setAllianceRank((byte) newRank);
                return true;
            }
        }
        return false;
    }

    public void changeGuildLeader(int chrId) {
        if ((changeRank(chrId, 1)) && (changeRank(this.leader, 2))) {
            if (this.allianceid > 0) {
                int aRank = getMGC(this.leader).getAllianceRank();
                if (aRank == 1) {
                } else {
                    changeARank(chrId, aRank);
                }
                changeARank(this.leader, 3);
            }
            broadcast(GuildPacket.guildLeaderChanged(this.id, this.leader, chrId, this.allianceid));
            this.leader = chrId;
            try {
                try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET leader = ? WHERE guildid = ?")) {
                    ps.setInt(1, chrId);
                    ps.setInt(2, this.id);
                    ps.execute();
                    ps.close();
                }
            } catch (SQLException e) {
                log.error(new StringBuilder().append("[MapleGuild] Saving leaderid ERROR.").append(e).toString());
            }
        }
    }

    public boolean changeRank(int chrId, int newRank) {
        for (MapleGuildCharacter mgc : this.members) {
            if (chrId == mgc.getId()) {
                if (mgc.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, newRank, mgc.getGuildContribution(), mgc.getAllianceRank());
                } else {
                    setOfflineGuildStatus(this.id, (byte) newRank, mgc.getGuildContribution(), mgc.getAllianceRank(), chrId);
                }
                mgc.setGuildRank((byte) newRank);
                broadcast(GuildPacket.changeRank(mgc));
                return true;
            }
        }

        return false;
    }

    public void setGuildNotice(String notice) {
        this.notice = notice;
        broadcast(GuildPacket.guildNotice(this.id, notice));
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        for (MapleGuildCharacter member : this.members) {
            if (member.getId() == mgc.getId()) {
                int old_level = member.getLevel();
                int old_job = member.getJobId();
                member.setJobId(mgc.getJobId());
                member.setLevel((short) mgc.getLevel());
                if (mgc.getLevel() > old_level) {
                    gainGP((mgc.getLevel() - old_level) * mgc.getLevel(), false, mgc.getId());
                }

                if (old_level != mgc.getLevel()) {
                    broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
                }
                if (old_job != mgc.getJobId()) {
                    broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
                }
                broadcast(GuildPacket.guildMemberLevelJobUpdate(mgc));
                if (this.allianceid <= 0) {
                    break;
                }
                break;
            }
        }
    }

    public void changeRankTitle(String[] ranks) {
        for (int i = 0; i < 5; i++) {
            this.rankTitles[i] = ranks[i];
        }
        broadcast(GuildPacket.rankTitleChange(this.id, ranks));
    }

    public void disbandGuild() {
        writeToDB(true);
        broadcast(null, -1, BCOp.DISBAND);
    }

    public void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        broadcast(null, -1, BCOp.EMBELMCHANGE);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?")) {
            ps.setInt(1, logo);
            ps.setInt(2, this.logoColor);
            ps.setInt(3, this.logoBG);
            ps.setInt(4, this.logoBGColor);
            ps.setInt(5, this.id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error(new StringBuilder().append("[MapleGuild] Saving guild logo / BG colo ERROR.").append(e).toString());
        }
    }

    public MapleGuildCharacter getMGC(int cid) {
        for (MapleGuildCharacter mgc : this.members) {
            if (mgc.getId() == cid) {
                return mgc;
            }
        }
        return null;
    }

    public boolean increaseCapacity(boolean trueMax) {
        if (this.capacity < (trueMax ? 200 : 100)) {
            if (this.capacity + 5 <= (trueMax ? 200 : 100));
        } else {
            return false;
        }

        if ((trueMax) && (this.gp < 25000)) {
            return false;
        }
        if ((trueMax) && (this.gp - 25000 < GameConstants.getGuildExpNeededForLevel(getLevel() - 1))) {
            return false;
        }
        this.capacity += 5;
        broadcast(GuildPacket.guildCapacityChange(this.id, this.capacity));
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?")) {
                ps.setInt(1, this.capacity);
                ps.setInt(2, this.id);
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            log.error(new StringBuilder().append("[MapleGuild] Saving guild capacity ERROR.").append(e).toString());
        }
        return true;
    }

    public void gainGP(int amount) {
        gainGP(amount, true, -1);
    }

    public void gainGP(int amount, boolean broadcast) {
        gainGP(amount, broadcast, -1);
    }

    public void gainGP(int amount, boolean broadcast, int chrId) {
        if (amount == 0) {
            return;
        }
        if (amount + this.gp < 0) {
            amount = -this.gp;
        }
        if ((chrId > 0) && (amount > 0)) {
            MapleGuildCharacter mg = getMGC(chrId);
            if (mg != null) {
                mg.setGuildContribution(mg.getGuildContribution() + amount);
                if (mg.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, mg.getGuildRank(), mg.getGuildContribution(), mg.getAllianceRank());
                } else {
                    setOfflineGuildStatus(this.id, mg.getGuildRank(), mg.getGuildContribution(), mg.getAllianceRank(), chrId);
                }
                broadcast(GuildPacket.guildContribution(this.id, chrId, mg.getGuildContribution()));
            }
        }
        this.gp += amount;
        this.level = calculateLevel();
        broadcast(GuildPacket.updateGP(this.id, this.gp, this.level));
    }

    public int getLevel() {
        return this.level;
    }

    public final int calculateLevel() {
        for (int i = 1; i < 10; i++) {
            if (this.gp < GameConstants.getGuildExpNeededForLevel(i)) {
                return i;
            }
        }
        return 10;
    }

    public void addMemberData(MaplePacketLittleEndianWriter mplew) {
        List<MapleGuildCharacter> players = new ArrayList();
        for (MapleGuildCharacter mgc : this.members) {
            if (mgc.getId() == this.leader) {
                players.add(mgc);
            }
        }
        for (MapleGuildCharacter mgc : this.members) {
            if (mgc.getId() != this.leader) {
                players.add(mgc);
            }
        }
        if (players.size() != this.members.size()) {
            FileoutputUtil.log(new StringBuilder().append("家族成员信息加载错误 - 实际加载: ").append(players.size()).append(" 应当加载: ").append(this.members.size()).toString());
        }
        mplew.writeShort(players.size());
        for (MapleGuildCharacter mgc : players) {
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : players) {
            mplew.writeAsciiString(mgc.getName(), 13);
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(mgc.getAllianceRank());
            mplew.writeInt(mgc.getGuildContribution());
            mplew.writeInt(10); //v117 可能是GP+IGP
            mplew.writeInt(7); //v117 IGP
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis())); //v117
        }
    }

    public static MapleGuildResponse sendInvite(MapleClient c, String targetName) {
        MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        }
        if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        }
        mc.getClient().getSession().write(GuildPacket.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob()));
        return null;
    }

    public Collection<MapleGuildCharacter> getMembers() {
        return Collections.unmodifiableCollection(this.members);
    }

    public boolean isInit() {
        return this.init;
    }

    public List<MapleBBSThread> getBBS() {
        List ret = new ArrayList(this.bbs.values());
        Collections.sort(ret, new MapleBBSThread.ThreadComparator());
        return ret;
    }

    public int addBBSThread(String title, String text, int icon, boolean bNotice, int posterID) {
        int add = this.bbs.get(0) == null ? 1 : 0;
        this.changed = true;
        int ret = bNotice ? 0 : Math.max(1, this.bbs.size() + add);
        this.bbs.put(ret, new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }

    public void editBBSThread(int localthreadid, String title, String text, int icon, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread) this.bbs.get(Integer.valueOf(localthreadid));
        if ((thread != null) && ((thread.ownerID == posterID) || (guildRank <= 2))) {
            this.changed = true;
            this.bbs.put(localthreadid, new MapleBBSThread(localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }
    }

    public void deleteBBSThread(int localthreadid, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread) this.bbs.get(Integer.valueOf(localthreadid));
        if ((thread != null) && ((thread.ownerID == posterID) || (guildRank <= 2))) {
            this.changed = true;
            this.bbs.remove(localthreadid);
        }
    }

    public void addBBSReply(int localthreadid, String text, int posterID) {
        MapleBBSThread thread = (MapleBBSThread) this.bbs.get(Integer.valueOf(localthreadid));
        if (thread != null) {
            this.changed = true;
            thread.replies.put(thread.replies.size(), new MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }
    }

    public void deleteBBSReply(int localthreadid, int replyid, int posterID, int guildRank) {
        MapleBBSThread thread = (MapleBBSThread) this.bbs.get(Integer.valueOf(localthreadid));
        if (thread != null) {
            MapleBBSReply reply = (MapleBBSReply) thread.replies.get(Integer.valueOf(replyid));
            if ((reply != null) && ((reply.ownerID == posterID) || (guildRank <= 2))) {
                this.changed = true;
                thread.replies.remove(replyid);
            }
        }
    }

    public static void setOfflineGuildStatus(int guildid, byte guildrank, int contribution, byte alliancerank, int cid) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, guildContribution = ?, alliancerank = ? WHERE id = ?")) {
                ps.setInt(1, guildid);
                ps.setInt(2, guildrank);
                ps.setInt(3, contribution);
                ps.setInt(4, alliancerank);
                ps.setInt(5, cid);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException se) {
            FileoutputUtil.log(new StringBuilder().append("SQLException: ").append(se.getLocalizedMessage()).toString());
        }
    }

    public final void addMemberForm(final MaplePacketLittleEndianWriter mplew) {
        //mplew.write(members.size());
        mplew.writeShort(joinList.size());

        for (int cid : getJoinList()) {
            mplew.writeInt(cid);
        }
        for (int cid : getJoinList()) {
            MapleCharacter chr = MapleCharacter.getCharacterById(cid);
            if (chr != null) {
                chr.setGuildId(id);
                memberInfo(mplew, chr.getMGC());
                chr.setGuildId(0);
            }
        }
    }

    private void memberInfo(final MaplePacketLittleEndianWriter mplew, MapleGuildCharacter mgc) {
        mplew.writeAsciiString(mgc.getName(), 13);
        mplew.writeInt(mgc.getJobId()); //-1 = ??
        mplew.writeInt(mgc.getLevel()); //-1 = ??
        mplew.writeInt(mgc.getGuildRank());
        mplew.writeInt(mgc.isOnline() ? 1 : 0);
        mplew.writeInt(mgc.getAllianceRank());
        mplew.writeInt(mgc.getGuildContribution());
        mplew.writeInt(0); //v117 可能是GP+IGP
        mplew.writeInt(0); //v117 IGP
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis())); //v117
    }

    public static List<MapleGuild> searchGuild(String keyWord) {
        List<MapleGuild> guilds = new ArrayList<>();
        for (MapleGuild guild : getAllGuilds()) {
            if (guild.getName().contains(keyWord) || guild.getMGC(guild.getLeaderId()).getName().contains(keyWord)) {
                guilds.add(guild);
            }
        }
        return guilds;
    }

    public static List<MapleGuild> searchGuild(int[] keyWords) {
        List<MapleGuild> guilds = new ArrayList<>();
        for (MapleGuild guild : getAllGuilds()) {
            int a = guild.getMembers().size();
            if (keyWords[0] <= guild.getLevel() && guild.getLevel() <= keyWords[1]
                    && keyWords[2] <= guild.getMembers().size() && guild.getMembers().size() <= keyWords[3]
                    && keyWords[4] <= guild.getAverageLevel() && guild.getMembers().size() <= keyWords[5]) {
                guilds.add(guild);
            }
        }
        return guilds;
    }

    public static List<MapleGuild> getAllGuilds() {
        List<MapleGuild> guilds = new ArrayList<>();
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `guilds`")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    guilds.add(WorldGuildService.getInstance().getGuild(rs.getInt("guildid")));
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException se) {
            FileoutputUtil.log("SQLException: " + se.getLocalizedMessage());
        }
        return guilds;
    }

    public final int getAverageLevel() {
        int totalLevel = 0;
        for (MapleGuildCharacter mgc : members) {
            totalLevel += mgc.getLevel();
        }
        return totalLevel / members.size();
    }

    public final void setGuildQuest(boolean add, MapleCharacter chr) {
        if (add) {
            addJoinList(chr.getId());
        } else {
            removeJoinList(chr.getId());
        }
//        chr.updateInfoQuest(GameConstants.申请公会名, add ? "name=" + name : "");
//        chr.updateInfoQuest(GameConstants.申请公ID, add ? "GuildID=" + id : "");
    }

    public final void addJoinList(int cid) {
        joinList.add(cid);
    }

    public final void removeJoinList(int cid) {
        Iterator<Integer> itr = joinList.iterator();
        while (itr.hasNext()) {
            if (cid == itr.next()) {
                itr.remove();
            }
        }
    }

    public static String getJoinGuildName(int cid) {
        MapleCharacter chr = MapleCharacter.getCharacterById(cid);
        String questInfo = chr.getInfoQuest(GameConstants.申请公会名);
        if (questInfo.split("=").length > 1) {
            return questInfo.split("=")[1];
        }
        return null;
    }

    public static int getJoinGuildId(int cid) {
        MapleCharacter chr = MapleCharacter.getCharacterById(cid);
        String questInfo = chr.getInfoQuest(GameConstants.申请公ID);
        if (questInfo.split("=").length > 1) {
            return Integer.valueOf(questInfo.split("=")[1]);
        }
        return -1;
    }

    public final List<Integer> getJoinList() {
        return joinList;
    }

    private static enum BCOp {

        NONE, DISBAND, EMBELMCHANGE;
    }
}
