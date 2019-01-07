package server.squad;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.WorldFindService;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import server.MapleCarnivalChallenge;
import server.Timer.EtcTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleSquad {

    private WeakReference<MapleCharacter> leader;
    private final String leaderName;
    private final String toSay;
    private final Map<String, String> members = new LinkedHashMap();
    private final Map<String, String> bannedMembers = new LinkedHashMap();
    private final int ch;
    private final long startTime;
    private final int expiration;
    private final int beginMapId;
    private final MapleSquadType type;
    private byte status = 0;
    private ScheduledFuture<?> removal;

    public MapleSquad(int ch, String type, MapleCharacter leader, int expiration, String toSay) {
        this.leader = new WeakReference(leader);
        this.members.put(leader.getName(), MapleCarnivalChallenge.getJobBasicNameById(leader.getJob()));
        this.leaderName = leader.getName();
        this.ch = ch;
        this.toSay = toSay;
        this.type = MapleSquadType.valueOf(type.toLowerCase());
        this.status = 1;
        this.beginMapId = leader.getMapId();
        leader.getMap().setSquad(this.type);
        if (this.type.queue.get(ch) == null) {
            this.type.queue.put(ch, new ArrayList());
            this.type.queuedPlayers.put(ch, new ArrayList());
        }
        this.startTime = System.currentTimeMillis();
        this.expiration = expiration;
    }

    public void copy() {
        while ((((ArrayList) this.type.queue.get(this.ch)).size() > 0) && (ChannelServer.getInstance(this.ch).getMapleSquad(this.type) == null)) {
            int index = 0;
            long lowest = 0L;
            for (int i = 0; i < ((ArrayList) this.type.queue.get(this.ch)).size(); i++) {
                if ((lowest == 0L) || (((Long) ((Pair) ((ArrayList) this.type.queue.get(this.ch)).get(i)).right) < lowest)) {
                    index = i;
                    lowest = ((Long) ((Pair) ((ArrayList) this.type.queue.get(this.ch)).get(i)).right);
                }
            }
            String nextPlayerId = (String) ((Pair) ((ArrayList) this.type.queue.get(Integer.valueOf(this.ch))).remove(index)).left;
            int theirCh = WorldFindService.getInstance().findChannel(nextPlayerId);
            if (theirCh > 0) {
                MapleCharacter lead = ChannelServer.getInstance(theirCh).getPlayerStorage().getCharacterByName(nextPlayerId);
                if ((lead != null) && (lead.getMapId() == this.beginMapId) && (lead.getClient().getChannel() == this.ch)) {
                    MapleSquad squad = new MapleSquad(this.ch, this.type.name(), lead, this.expiration, this.toSay);
                    if (ChannelServer.getInstance(this.ch).addMapleSquad(squad, this.type.name())) {
                        getBeginMap().broadcastMessage(MaplePacketCreator.getClock(this.expiration / 1000));
                        getBeginMap().broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append(nextPlayerId).append(this.toSay).toString()));
                        ((ArrayList) this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "Success"));
                        break;
                    }
                    squad.clear();
                    ((ArrayList) this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "Skipped"));

                    break;
                }
                if (lead != null) {
                    lead.dropMessage(6, "Your squad has been skipped due to you not being in the right channel and map.");
                }
                getBeginMap().broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append(nextPlayerId).append("'s squad has been skipped due to the player not being in the right channel and map.").toString()));
                ((ArrayList) this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "Not in map"));
            } else {
                getBeginMap().broadcastMessage(MaplePacketCreator.serverMessageNotice(new StringBuilder().append(nextPlayerId).append("'s squad has been skipped due to the player not being online.").toString()));
                ((ArrayList) this.type.queuedPlayers.get(this.ch)).add(new Pair(nextPlayerId, "Not online"));
            }
        }
    }

    public MapleMap getBeginMap() {
        return ChannelServer.getInstance(this.ch).getMapFactory().getMap(this.beginMapId);
    }

    public void clear() {
        if (this.removal != null) {
            getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
            this.removal.cancel(false);
            this.removal = null;
        }
        this.members.clear();
        this.bannedMembers.clear();
        this.leader = null;
        ChannelServer.getInstance(this.ch).removeMapleSquad(this.type);
        this.status = 0;
    }

    public MapleCharacter getChar(String name) {
        return ChannelServer.getInstance(this.ch).getPlayerStorage().getCharacterByName(name);
    }

    public long getTimeLeft() {
        return this.expiration - (System.currentTimeMillis() - this.startTime);
    }

    public void scheduleRemoval() {
        this.removal = EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if ((MapleSquad.this.status != 0) && (MapleSquad.this.leader != null) && ((MapleSquad.this.getLeader() == null) || (MapleSquad.this.status == 1))) {
                    MapleSquad.this.clear();
                    MapleSquad.this.copy();
                }
            }
        }, this.expiration);
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public List<Pair<String, Long>> getAllNextPlayer() {
        return (List) this.type.queue.get(this.ch);
    }

    public boolean containsNextPlayer(MapleCharacter player) {
        for (Pair names : type.queue.get(this.ch)) {
            if (((String) names.left).equalsIgnoreCase(player.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getNextPlayer() {
        StringBuilder sb = new StringBuilder("\n远征人数 : ");
        sb.append("#b").append(((ArrayList) this.type.queue.get(this.ch)).size()).append(" #k ").append("远征队成员信息 : \n\r ");
        int i = 0;
        for (Pair chr : type.queue.get(this.ch)) {
            i++;
            sb.append(i).append(" : ").append((String) chr.left);
            sb.append(" \n\r ");
        }
        sb.append("Would you like to #ebe next#n in the queue, or #ebe removed#n from the queue if you are in it?");
        return sb.toString();
    }

    public void setNextPlayer(String chr) {
        Pair toRemove = null;
        for (Pair s : type.queue.get(this.ch)) {
            if (((String) s.left).equals(chr)) {
                toRemove = s;
                break;
            }
        }
        if (toRemove != null) {
            ((ArrayList) this.type.queue.get(this.ch)).remove(toRemove);
            return;
        }
        for (ArrayList<Pair<String, Long>> v : this.type.queue.values()) {
            for (Pair s : v) {
                if (((String) s.left).equals(chr)) {
                    return;
                }
            }
        }
        ((ArrayList) this.type.queue.get(this.ch)).add(new Pair(chr, System.currentTimeMillis()));
    }

    public MapleCharacter getLeader() {
        if ((this.leader == null) || (this.leader.get() == null)) {
            if ((this.members.size() > 0) && (getChar(this.leaderName) != null)) {
                this.leader = new WeakReference(getChar(this.leaderName));
            } else {
                if (this.status != 0) {
                    clear();
                }
                return null;
            }
        }
        return (MapleCharacter) this.leader.get();
    }

    public boolean containsMember(MapleCharacter member) {
        for (String mmbr : this.members.keySet()) {
            if (mmbr.equalsIgnoreCase(member.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<String> getMembers() {
        return new LinkedList(this.members.keySet());
    }

    public List<String> getBannedMembers() {
        return new LinkedList(this.bannedMembers.keySet());
    }

    public int getSquadSize() {
        return this.members.size();
    }

    public boolean isBanned(MapleCharacter member) {
        return this.bannedMembers.containsKey(member.getName());
    }

    public int addMember(MapleCharacter member, boolean join) {
        if (getLeader() == null) {
            return -1;
        }
        String job = MapleCarnivalChallenge.getJobBasicNameById(member.getJob());
        if (join) {
            if ((!containsMember(member)) && (!containsNextPlayer(member))) {
                if (this.members.size() <= 30) {
                    this.members.put(member.getName(), job);
                    getLeader().dropMessage(5, new StringBuilder().append(member.getName()).append(" (").append(job).append(") 加入了远征队.").toString());
                    return 1;
                }
                return 2;
            }
            return -1;
        }
        if (containsMember(member)) {
            this.members.remove(member.getName());
            getLeader().dropMessage(5, new StringBuilder().append(member.getName()).append(" (").append(job).append(") 离开了远征队.").toString());
            return 1;
        }
        return -1;
    }

    public void acceptMember(int pos) {
        if ((pos < 0) || (pos >= this.bannedMembers.size())) {
            return;
        }
        List membersAsList = getBannedMembers();
        String toadd = (String) membersAsList.get(pos);
        if ((toadd != null) && (getChar(toadd) != null)) {
            this.members.put(toadd, this.bannedMembers.get(toadd));
            this.bannedMembers.remove(toadd);
            getChar(toadd).dropMessage(5, new StringBuilder().append(getLeaderName()).append(" 将你列为远征队队员.").toString());
        }
    }

    public void reAddMember(MapleCharacter chr) {
        removeMember(chr);
        this.members.put(chr.getName(), MapleCarnivalChallenge.getJobBasicNameById(chr.getJob()));
    }

    public void removeMember(MapleCharacter chr) {
        if (this.members.containsKey(chr.getName())) {
            this.members.remove(chr.getName());
        }
    }

    public void removeMember(String chr) {
        if (this.members.containsKey(chr)) {
            this.members.remove(chr);
        }
    }

    public void banMember(int pos) {
        if ((pos <= 0) || (pos >= this.members.size())) {
            return;
        }
        List membersAsList = getMembers();
        String toban = (String) membersAsList.get(pos);
        if ((toban != null) && (getChar(toban) != null)) {
            this.bannedMembers.put(toban, this.members.get(toban));
            this.members.remove(toban);
            getChar(toban).dropMessage(5, new StringBuilder().append(getLeaderName()).append(" 将你请出远征队，目前无法加入远征队.").toString());
        }
    }

    public void setStatus(byte status) {
        this.status = status;
        if ((status == 2) && (this.removal != null)) {
            this.removal.cancel(false);
            this.removal = null;
        }
    }

    public int getStatus() {
        return this.status;
    }

    public int getBannedMemberSize() {
        return this.bannedMembers.size();
    }

    public String getSquadMemberString(byte type) {
        switch (type) {
            case 0:
                StringBuilder sb = new StringBuilder("总共 : ");
                sb.append("#b").append(this.members.size()).append(" #k ").append("个远征队成员 : \n\r ");
                int i = 0;
                for (Map.Entry chr : this.members.entrySet()) {
                    i++;
                    sb.append(i).append(" : ").append((String) chr.getKey()).append(" (").append((String) chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(远征队队长)");
                    }
                    sb.append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            case 1:

                sb = new StringBuilder("总共 : ");
                sb.append("#b").append(this.members.size()).append(" #k ").append("个远征队成员 : \n\r ");
                i = 0;
                int selection = 0;
                for (Map.Entry chr : this.members.entrySet()) {
                    i++;
                    sb.append("#b#L").append(selection).append("#");
                    selection++;
                    sb.append(i).append(" : ").append((String) chr.getKey()).append(" (").append((String) chr.getValue()).append(") ");
                    if (i == 1) {
                        sb.append("(远征队队长)");
                    }
                    sb.append("#l").append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            case 2:
                sb = new StringBuilder("总共 : ");
                selection = 0;
                sb.append("#b").append(this.members.size()).append(" #k ").append("个远征队成员 : \n\r ");
                i = 0;
                for (Map.Entry chr : this.bannedMembers.entrySet()) {
                    i++;
                    sb.append("#b#L").append(selection).append("#");
                    selection++;
                    sb.append(i).append(" : ").append((String) chr.getKey()).append(" (").append((String) chr.getValue()).append(") ");
                    sb.append("#l").append(" \n\r ");
                }
                while (i < 30) {
                    i++;
                    sb.append(i).append(" : ").append(" \n\r ");
                }
                return sb.toString();
            case 3:
                sb = new StringBuilder("总共 : ");
                Map<String, Integer> jobs = getJobs();
                for (Map.Entry chr : jobs.entrySet()) {
                    sb.append("\r\n").append((String) chr.getKey()).append(" : ").append(chr.getValue());
                }
                return sb.toString();
        }

        return null;
    }

    public MapleSquadType getType() {
        return this.type;
    }

    public Map<String, Integer> getJobs() {
        Map jobs = new LinkedHashMap();
        for (Map.Entry chr : this.members.entrySet()) {
            if (jobs.containsKey(chr.getValue())) {
                jobs.put(chr.getValue(), ((Integer) jobs.get(chr.getValue())) + 1);
            } else {
                jobs.put(chr.getValue(), 1);
            }
        }
        return jobs;
    }
}
