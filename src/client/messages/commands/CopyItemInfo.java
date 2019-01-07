package client.messages.commands;

public class CopyItemInfo {

    public int itemId;
    public int chrId;
    public String name;
    public boolean first;

    public CopyItemInfo(int itemId, int chrId, String name) {
        this.itemId = itemId;
        this.chrId = chrId;
        this.name = name;
        this.first = true;
    }

    public boolean isFirst() {
        return this.first;
    }

    public void setFirst(boolean f) {
        this.first = f;
    }
}
