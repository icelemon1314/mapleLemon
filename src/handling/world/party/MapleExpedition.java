package handling.world.party;

import handling.world.WrodlPartyService;
import java.util.ArrayList;
import java.util.List;

public class MapleExpedition {

    private final List<Integer> parties;
    private final ExpeditionType type;
    private int leaderId;
    private final int id;

    public MapleExpedition(ExpeditionType etype, int leaderId, int id) {
        this.type = etype;
        this.id = id;
        this.leaderId = leaderId;
        this.parties = new ArrayList(etype.maxParty);
    }

    public ExpeditionType getType() {
        return this.type;
    }

    public int getLeader() {
        return this.leaderId;
    }

    public List<Integer> getParties() {
        return this.parties;
    }

    public int getId() {
        return this.id;
    }

    public int getAllMembers() {
        int ret = 0;
        for (int i = 0; i < this.parties.size(); i++) {
            MapleParty pp = WrodlPartyService.getInstance().getParty((this.parties.get(i)).intValue());
            if (pp == null) {
                this.parties.remove(i);
            } else {
                ret += pp.getMembers().size();
            }
        }
        return ret;
    }

    public int getFreeParty() {
        for (int i = 0; i < this.parties.size(); i++) {
            MapleParty party = WrodlPartyService.getInstance().getParty((this.parties.get(i)).intValue());
            if (party == null) {
                this.parties.remove(i);
            } else if (party.getMembers().size() < 6) {
                return party.getId();
            }
        }
        if (this.parties.size() < this.type.maxParty) {
            return 0;
        }
        return -1;
    }

    public int getIndex(int partyId) {
        for (int i = 0; i < this.parties.size(); i++) {
            if ((this.parties.get(i)) == partyId) {
                return i;
            }
        }
        return -1;
    }

    public void setLeader(int newLead) {
        this.leaderId = newLead;
    }
}
