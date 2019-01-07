package handling.world.messenger;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.WorldFindService;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class MapleMessenger implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final MapleMessengerCharacter[] members;
    private final String[] silentLink;
    private int id;
    private final MessengerType type;
    private final boolean hide;

    public MapleMessenger(int id, MapleMessengerCharacter chrfor, MessengerType type, boolean gm) {
        this.id = id;
        this.type = type;
        this.hide = gm;
        this.members = new MapleMessengerCharacter[type.maxMembers];
        this.silentLink = new String[type.maxMembers];
        this.members[0] = chrfor;
    }

    public MessengerType getType() {
        return this.type;
    }

    public boolean isHide() {
        return this.hide;
    }

    public void addMembers(int pos, MapleMessengerCharacter chrfor) {
        if (this.members[pos] != null) {
            return;
        }
        this.members[pos] = chrfor;
    }

    public boolean containsMembers(MapleMessengerCharacter member) {
        return getPositionByName(member.getName()) != -1;
    }

    public void addMember(MapleMessengerCharacter member) {
        int position = getLowestPosition();
        if (position != -1) {
            addMembers(position, member);
        }
    }

    public void removeMember(MapleMessengerCharacter member) {
        int position = getPositionByName(member.getName());
        if (position != -1) {
            this.members[position] = null;
        }
    }

    public void silentRemoveMember(MapleMessengerCharacter member) {
        int position = getPositionByName(member.getName());
        if (position != -1) {
            this.members[position] = null;
            this.silentLink[position] = member.getName();
        }
    }

    public void silentAddMember(MapleMessengerCharacter member) {
        for (int i = 0; i < this.silentLink.length; i++) {
            if ((this.silentLink[i] != null) && (this.silentLink[i].equalsIgnoreCase(member.getName()))) {
                addMembers(i, member);
                this.silentLink[i] = null;
                return;
            }
        }
    }

    public void updateMember(MapleMessengerCharacter member) {
        for (int i = 0; i < this.members.length; i++) {
            MapleMessengerCharacter chr = this.members[i];
            if ((chr != null) && (chr.equals(member))) {
                this.members[i] = null;
                addMembers(i, member);
                return;
            }
        }
    }

    public int getMemberSize() {
        int ret = 0;
        for (MapleMessengerCharacter member : this.members) {
            if (member != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getLowestPosition() {
        for (int i = 0; i < this.members.length; i++) {
            if (this.members[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByName(String name) {
        for (int i = 0; i < this.members.length; i++) {
            MapleMessengerCharacter messengerchar = this.members[i];
            if ((messengerchar != null) && (messengerchar.getName().equalsIgnoreCase(name))) {
                return i;
            }
        }
        return -1;
    }

    public MapleMessengerCharacter getMemberByPos(int pos) {
        return this.members[pos];
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 31 + this.id;
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
        MapleMessenger other = (MapleMessenger) obj;

        return this.id == other.id;
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Arrays.asList(this.members);
    }

    public boolean isMonitored() {
        int ch = -1;
        for (MapleMessengerCharacter member : this.members) {
            if (member != null) {
                ch = WorldFindService.getInstance().findChannel(member.getName());
                if (ch != -1) {
                    MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(member.getName());
                    if ((player != null) && (player.getClient() != null) && (player.getClient().isMonitored())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getMemberNamesDEBUG() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.members.length; i++) {
            if (this.members[i] != null) {
                sb.append(this.members[i].getName());
                if (i != this.members.length - 1) {
                    sb.append(',');
                }
            }
        }
        return sb.toString();
    }
}
