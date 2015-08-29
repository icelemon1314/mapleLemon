package client.inventory;

/**
 * 道具状态基址
 */
public enum EquipStats {

    可升级次数(0x1),
    已升级次数(0x2),
    力量(0x4),
    敏捷(0x8),
    智力(0x10),
    运气(0x20),
    Hp(0x40),
    Mp(0x80),
    物攻(0x100),
    魔攻(0x200),
    物防(0x400),
    魔防(0x800),
    命中(0x1000),
    回避(0x2000),
    手技(0x4000),
    速度(0x8000),
    跳跃(0x10000),
    状态(0x20000),
    技能(0x40000),
    道具等级(0x80000);

    private final int value;

    private EquipStats(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
