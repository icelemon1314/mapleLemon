package client;

public enum MapleExpStat {

    活动奖励经验(0x1),
    特别经验(0x2),
    活动组队经验(0x4),
    组队经验(0x10),
    结婚奖励经验(0x20),
    ;
    private final long i;

    private MapleExpStat(long i) {
        this.i = i;
    }

    public long getValue() {
        return this.i;
    }
}
