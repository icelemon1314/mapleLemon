package client.messages;

public enum CommandType {

    NORMAL(0),
    TRADE(1),
    ;
    private final int level;

    private CommandType(int level) {
        this.level = level;
    }

    public int getType() {
        return this.level;
    }
}


