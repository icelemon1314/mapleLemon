package client.inventory;

public enum ItemFlag {

    封印(0x1),
    鞋子防滑(0x2),
    披风防寒(0x4),
    不可交易(0x8),
    KARMA_USE(0x2);

    private final int i;

    private ItemFlag(int i) {
        this.i = i;
    }

    public int getValue() {
        return this.i;
    }

    public boolean check(int flag) {
        return (flag & this.i) == this.i;
    }
}
