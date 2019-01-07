package handling.world.party;

import client.MapleCharacter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.MapleStatEffect;

public class MapleParty implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private MaplePartyCharacter leader;
    private List<MaplePartyCharacter> members = new LinkedList();
    private int id;
    private int expeditionLink = -1;
    private boolean disbanded = false;
    private boolean 非公开组队;
    private String 组队名称;
    private Map<Integer, Map<Integer, List<Integer>>> partyBuffs = new HashMap();

    public MapleParty(int id, MaplePartyCharacter chrfor, boolean 非公开组队, String 组队名称) {
        this.leader = chrfor;
        this.members.add(this.leader);
        this.id = id;
        this.非公开组队 = 非公开组队;
        this.组队名称 = 组队名称;
    }

    public MapleParty(int id, MaplePartyCharacter chrfor, int expeditionLink) {
        this.leader = chrfor;
        this.members.add(this.leader);
        this.id = id;
        this.expeditionLink = expeditionLink;
    }

    public boolean containsMembers(MaplePartyCharacter member) {
        return this.members.contains(member);
    }

    public void addMember(MaplePartyCharacter member) {
        this.members.add(member);
    }

    public void removeMember(MaplePartyCharacter member) {
        this.members.remove(member);
        cancelAllPartyBuffsByChr(member.getId());
    }

    public void updateMember(MaplePartyCharacter member) {
        for (int i = 0; i < this.members.size(); i++) {
            MaplePartyCharacter chr = (MaplePartyCharacter) this.members.get(i);
            if (chr.equals(member)) {
                this.members.set(i, member);
            }
        }
    }

    public MaplePartyCharacter getMemberById(int id) {
        for (MaplePartyCharacter chr : this.members) {
            if (chr.getId() == id) {
                return chr;
            }
        }
        return null;
    }

    public int getAverageLevel(MapleCharacter chr) {
        int Averagelevel = 0;
        for (MaplePartyCharacter bchr : this.members) {
            /*if (chr.getMapId() != bchr.getMapid()) {
                continue;
            }*/
            Averagelevel += bchr.getLevel();
        }
        return Averagelevel;
    }

    public MaplePartyCharacter getMemberByIndex(int index) {
        return (MaplePartyCharacter) this.members.get(index);
    }

    public Collection<MaplePartyCharacter> getMembers() {
        return new LinkedList(this.members);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MaplePartyCharacter getLeader() {
        return this.leader;
    }

    public void setLeader(MaplePartyCharacter nLeader) {
        this.leader = nLeader;
    }

    public int getExpeditionId() {
        return this.expeditionLink;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapleParty other = (MapleParty) obj;

        return this.id == other.id;
    }

    public boolean isDisbanded() {
        return this.disbanded;
    }

    public void disband() {
        this.disbanded = true;
    }

    public String getName() {
        return 组队名称;
    }

    public void setName(String name) {
        组队名称 = name;
    }

    public boolean is非公开组队() {
        return 非公开组队;
    }

    public void set非公开组队(boolean 非公开组队) {
        this.非公开组队 = 非公开组队;
    }

    public void givePartyBuff(int buffId, int applyfrom, int applyto) {
        if (partyBuffs.containsKey(buffId)) {
            if (partyBuffs.get(buffId).containsKey(applyfrom)) {
                if (!partyBuffs.get(buffId).keySet().isEmpty()) {
                    for (Integer from : partyBuffs.get(buffId).keySet()) {
                        if (partyBuffs.get(buffId).get(from).contains(applyto)) {
                            partyBuffs.get(buffId).get(from).remove(partyBuffs.get(buffId).get(from).indexOf(applyto));
                        }
                        if (partyBuffs.get(buffId).get(from).isEmpty()) {
                            partyBuffs.get(buffId).remove(from);
                        }
                    }
                }
                if (partyBuffs != null && !partyBuffs.get(buffId).get(applyfrom).contains(applyto)) {
                    partyBuffs.get(buffId).get(applyfrom).add(applyto);
                }
            } else {
                ArrayList applytos = new ArrayList();
                applytos.add(applyto);
                partyBuffs.get(buffId).put(applyfrom, applytos);
            }
        } else {
            Map<Integer, List<Integer>> hMap = new HashMap();
            ArrayList applytos = new ArrayList();
            applytos.add(applyto);
            hMap.put(applyfrom, applytos);
            partyBuffs.put(buffId, hMap);
        }
    }

    public int getPartyBuffs(int applyfrom) {
        ArrayList chrs = new ArrayList();
        for (Map<Integer, List<Integer>> buffs : partyBuffs.values()) {
            if (buffs.containsKey(applyfrom)) {
                for (List<Integer> applytos : buffs.values()) {
                    for (int i : applytos) {
                        if (!chrs.contains(i)) {
                            chrs.add(i);
                        }
                    }
                }
            }
        }
        return chrs.size();
    }

    public int cancelPartyBuff(int buffId, int cancelby) {
        if (partyBuffs.containsKey(buffId)) {
            if (partyBuffs.get(buffId).isEmpty()) {
                partyBuffs.remove(buffId);
            } else {
                for (Integer applyfrom : partyBuffs.get(buffId).keySet()) {
                    if (partyBuffs.get(buffId).get(applyfrom).isEmpty()) {
                        partyBuffs.get(buffId).remove(applyfrom);
                    } else if (partyBuffs.get(buffId).get(applyfrom).contains(cancelby)) {
                        partyBuffs.get(buffId).get(applyfrom).remove(partyBuffs.get(buffId).get(applyfrom).indexOf(cancelby));
                        return applyfrom;
                    }
                }
            }
        }
        return -1;
    }

    public void cancelAllPartyBuffsByChr(int cancelby) {
        if (partyBuffs.isEmpty()) {
            return;
        }
        try {
            for (Integer buffId : partyBuffs.keySet()) {
                if (partyBuffs.get(buffId).isEmpty()) {
                    partyBuffs.remove(buffId);
                } else {
                    for (Integer applyfrom : partyBuffs.get(buffId).keySet()) {
                        if (partyBuffs.get(buffId).get(applyfrom).isEmpty() || applyfrom == cancelby) {
                            partyBuffs.get(buffId).remove(applyfrom);
                            MapleCharacter chr = MapleCharacter.getOnlineCharacterById(applyfrom);
                            if (applyfrom == cancelby && chr != null) {
//                                MapleStatEffect.apply祈祷众生(chr);
                            }
                        } else if (partyBuffs.get(buffId).get(applyfrom).contains(cancelby)) {
                            partyBuffs.get(buffId).get(applyfrom).remove(partyBuffs.get(buffId).get(applyfrom).indexOf(cancelby));
                            MapleCharacter chr = MapleCharacter.getOnlineCharacterById(applyfrom);
                            if (chr != null) {
//                                MapleStatEffect.apply祈祷众生(chr);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
