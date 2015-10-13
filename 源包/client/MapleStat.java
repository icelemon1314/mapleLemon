package client;

/**
 * 属性状态基址
 * @author iclemon1314
 */
public enum MapleStat {

    皮肤(0x1,1), // 人物属性基址，每个基址的长度
    脸型(0x2,4),
    发型(0x4,4),
    宠物(0x8,8),
    等级(0x10,1),
    职业(0x20,2),
    力量(0x40,2),
    敏捷(0x80,2),
    智力(0x100,2),
    运气(0x200,2),
    HP(0x400,2),
    MAXHP(0x800,2),
    MP(0x1000,2),
    MAXMP(0x2000,2),
    AVAILABLEAP(0x4000,2),
    AVAILABLESP(0x8000,2),
    经验(0x10000,4),
    人气(0x20000,2),
    金币(0x40000,4);

    private final long i; // 偏移
    private final int writeByte; // 数据长度

    private MapleStat(long i,int writeByte) {
        this.i = i;
        this.writeByte = writeByte;
    }

    public long getValue() {
        return this.i;
    }

    public int getWriteByte() {
        return this.writeByte;
    }

    public static MapleStat getByValue(long value) {
        for (MapleStat stat : values()) {
            if (stat.i == value) {
                return stat;
            }
        }
        return null;
    }

    public static enum Temp {

        力量(0x1),
        敏捷(0x2),
        智力(0x4),
        运气(0x8),
        物攻(0x10),
        魔攻(0x20),
        物防(0x40),
        魔防(0x80),
        命中(0x100),
        回避(0x200),
        速度(0x400),
        跳跃(0x800);

        private final int i;

        private Temp(int i) {
            this.i = i;
        }

        public int getValue() {
             return this.i;
        }
    }
}


