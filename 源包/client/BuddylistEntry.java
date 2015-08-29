package client;

public class BuddylistEntry {

    private final String name;
    private String group;
    private final int cid;
    private int channel;
    private boolean visible;

    public BuddylistEntry(String name, int characterId, String group, int channel, boolean visible) {
        this.name = name;
        this.cid = characterId;
        this.group = group;
        this.channel = channel;
        this.visible = visible;
    }

    public int getChannel() {
        return this.channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isOnline() {
        return this.channel >= 0;
    }

    public void setOffline() {
        this.channel = -1;
    }

    public String getName() {
        return this.name;
    }

    public int getCharacterId() {
        return this.cid;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String g) {
        this.group = g;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.cid;
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
         BuddylistEntry other = (BuddylistEntry) obj;

         return this.cid == other.cid;
    }
}

