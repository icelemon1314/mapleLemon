package server;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class MapleCarnivalParty {

    private final List<Integer> members = new LinkedList();
    private final WeakReference<MapleCharacter> leader;
    private final byte team;
    private final int channel;
    private short availableCP = 0;
    private short totalCP = 0;
    private boolean winner = false;

    public MapleCarnivalParty(MapleCharacter owner, List<MapleCharacter> members1, byte team1) {
        this.leader = new WeakReference(owner);
        for (MapleCharacter mem : members1) {
            this.members.add(mem.getId());
            mem.setCarnivalParty(this);
        }
        this.team = team1;
        this.channel = owner.getClient().getChannel();
    }

    public MapleCharacter getLeader() {
        return (MapleCharacter) this.leader.get();
    }

    public void addCP(MapleCharacter player, int ammount) {
        this.totalCP = (short) (this.totalCP + ammount);
        this.availableCP = (short) (this.availableCP + ammount);
        player.addCP(ammount);
    }

    public int getTotalCP() {
        return this.totalCP;
    }

    public int getAvailableCP() {
        return this.availableCP;
    }

    public void useCP(MapleCharacter player, int ammount) {
        this.availableCP = (short) (this.availableCP - ammount);
        player.useCP(ammount);
    }

    public List<Integer> getMembers() {
        return this.members;
    }

    public int getTeam() {
        return this.team;
    }

    public void warp(MapleMap map, String portalname) {
        for (Iterator i$ = this.members.iterator(); i$.hasNext();) {
            int chr = ((Integer) i$.next());
            MapleCharacter c = ChannelServer.getInstance(this.channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.changeMap(map, map.getPortal(portalname));
            }
        }
    }

    public void warp(MapleMap map, int portalid) {
        for (Iterator i$ = this.members.iterator(); i$.hasNext();) {
            int chr = ((Integer) i$.next());
            MapleCharacter c = ChannelServer.getInstance(this.channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.changeMap(map, map.getPortal(portalid));
            }
        }
    }

    public boolean allInMap(MapleMap map) {
        for (Iterator i$ = this.members.iterator(); i$.hasNext();) {
            int chr = ((Integer) i$.next());
            if (map.getCharacterById(chr) == null) {
                return false;
            }
        }
        return true;
    }

    public void removeMember(MapleCharacter chr) {
        for (int i = 0; i < this.members.size(); i++) {
            if ((this.members.get(i)) == chr.getId()) {
                this.members.remove(i);
                chr.setCarnivalParty(null);
            }
        }
    }

    public boolean isWinner() {
        return this.winner;
    }

    public void setWinner(boolean status) {
        this.winner = status;
    }

    public void displayMatchResult() {
        String effect = this.winner ? "quest/carnival/win" : "quest/carnival/lose";
        String sound = this.winner ? "MobCarnival/Win" : "MobCarnival/Lose";
        boolean done = false;
        for (Iterator i$ = this.members.iterator(); i$.hasNext();) {
            int chr = ((Integer) i$.next());
            MapleCharacter c = ChannelServer.getInstance(this.channel).getPlayerStorage().getCharacterById(chr);
            if (c != null) {
                c.getClient().getSession().write(MaplePacketCreator.showEffect(effect));
                c.getClient().getSession().write(MaplePacketCreator.playSound(sound));
                if (!done) {
                    done = true;
                    c.getMap().killAllMonsters(true);
                    c.getMap().setSpawns(false);
                }
            }
        }
    }
}
