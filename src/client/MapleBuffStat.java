package client;

import constants.GameConstants;
import handling.Buffstat;
import java.io.Serializable;

public enum MapleBuffStat implements Serializable, Buffstat {
    物理攻击力(0x1, 1),//ok
    增加物理防御(0x2, 1),//ok
    魔法攻击力(0x4, 1), //ok
    魔法防御力(0x8, 1),// ok
    命中率(0x10, 1),//acc ok
    回避率(0x20, 1),//eva ok
    手技(0x40, 1), // ok
    移动速度(0x80, 1),//speed ok
    跳跃力(0x100, 1),//jump ok
    魔法盾(0x200, 1), // ok
    隐身术(0x400, 1),
    攻击加速(0x800, 1),
    伤害反击(0x1000, 1),
    神圣之火_最大体力百分比(0x2000, 1),//神圣之火.x(不被其他最大体力百分比覆盖)
    神圣之火_最大魔力百分比(0x4000, 1),//神圣之火.y(不被其他最大魔力百分比覆盖)
    神之保护(0x8000, 1),
    无形箭弩(0x10000, 1),
    召唤兽(0x20000, 1),
    POISON(0x00040000,1),
    SEAL(0x00080000,1),
    DARKNESS(0x00100000,1),
    斗气集中(0x200000, 1), // ok
    HP减少无效(0x400000,1), // 地图减少HP无效
    龙之力(0x800000,1),    // ok
    神圣祈祷(0x1000000, 1),
    聚财术(0x2000000, 1),
    影分身(0x4000000, 1),
    金钱护盾(0x8000000, 1),
    替身(0x8000000,1),
    敛财术(0x8000000, 1),
    STUN(0x00020000,1),
    WEAKNESS(0x40000000,1),
    WK_CHARGE(0x00400000,1),

    // ==========================

    冰骑士(0x10000, 1),
    压制术(0x20000, 1),
    牧师祝福(0x100000, 1),
    龙卷风(0x200000, 1),
    GIANT_POTION(0x1000000, 1),
    DISABLE_POTENTIAL(0x2000000, 1),
    灵魂助力(0x4000000, 1),
    战斗命令(0x8000000, 1),
    快速移动精通(0x10000000, 1),
    攻击力增加百分比(0x20000000, 1),//damR%
    祝福护甲(0x40000000, 1),
    黑暗(0x4000000, 1),
    封印(0x8000000, 1),
    中毒(0x10000000, 1),
    昏迷(0x20000000, 1),
    无形箭弩_2(0x40000000, 1),

    BOSS攻击力(0x2000000, 1),
    百分比无视防御(0x10000000, 1),
    无视防御(0x10000000, 1),
    最大暴击伤害(0x20000000,1),
    抗震防御_防御力(0x40000000, 1),//抗震防御.z
    爆击概率提升(0x80000000, 1),//未知
    暴击概率增加(0x80000000, 1), //  应该是有true
    所有属性抗性(0x1, 1),
    状态异常抗性(0x2, 1),
    伤害最大值(0x4, 1),//indieMaxDamageOver
    反制攻击(0x20, 1),
    伤害增加(0x20, 1),//indieDamR - 总伤
    天使复仇(0x10000, 1),
    攻击速度提升(0x10000, 1),
    命中值增加(0x800000, 1),//indieAcc
    最大魔力百分比(0x1000000, 1),//indieMmpR
    最大魔力(0x2000000, 1),//indieMaxMp
    最大体力百分比(0x4000000, 1),//indieMhpR
    最大体力(0x8000000, 1),//indieMaxHp
    魔法防御增加(0x10000000, 1),
    魔法攻击力增加(0x40000000, 1),//indieMad
    攻击力增加(0x80000000, 1),//indiePad
    骑兽技能(0x80000000, 1 ),
    子弹数量(0xFFFFF,1 ),
    属性攻击(0x1,1 ), 诅咒(0x1, 1), 虚弱(0x1, 1), 时空门(0x1, 1), 恢复效果(0x1, 1), 变身术(0x1, 1);

    private static final long serialVersionUID = 0L;
    private final int buffstat;
    private final int first;

    private MapleBuffStat(int buffstat, int first) {
        this.buffstat = buffstat;
        this.first = first;
    }

    @Override
    public int getPosition() {
        return this.first;
    }

    public int getPosition(boolean fromZero) {
        if (!fromZero) {
            return this.first;
        }
        if ((this.first > 0) && (this.first <= GameConstants.MAX_BUFFSTAT)) {
            return GameConstants.MAX_BUFFSTAT - this.first;
        }
        return 0;
    }

    @Override
    public int getValue() {
        return this.buffstat;
    }
}
