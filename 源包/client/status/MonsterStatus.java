package client.status;

import client.MapleDisease;
import constants.GameConstants;
import handling.Buffstat;
import server.skill.冒险家.无影人;

import java.io.Serializable;

/**
 * 怪物获得的BUFF基址
 */
public enum MonsterStatus implements Serializable, Buffstat {

    物攻(0x1, 1), // ok
    物防(0x2, 1), // ok
    魔攻(0x4, 1),
    魔防(0x8, 1),
    命中(0x10, 1),
    回避(0x20, 1),
    速度(0x40, 1), // ok
    眩晕(0x80,1), // ok
    结冰(0x100, 1),//麻痹
    中毒(0x200, 1), // ok
    沉默(0x400, 1),
    挑衅(0x800, 1),
    恐慌(0x1000,1 ),
    物理防御无效(0x2000,1),
    魔法防御无效(0x4000,1),
    封印(0x8000,1), // ok

    影网(0x20000,1),
    物攻提升(0x10000,1),
    魔攻提升(0x20000,1),
    物防提升(0x40000,1),
    固定住不动(0x8000,1),
    魔防提升(0x80000,1 );
    static final long serialVersionUID = 0L;
    private final int i;
    private final int first;
    private final boolean end;

    private MonsterStatus(int i, int first) {
        this.i = i;
        this.first = first;
        this.end = false;
    }

    private MonsterStatus(int i, int first, boolean end) {
        this.i = i;
        this.first = first;
        this.end = end;
    }

    @Override
    public int getPosition() {
        return this.first;
    }

    public boolean isEmpty() {
        return this.end;
    }

    @Override
    public int getValue() {
        return this.i;
    }

    public static MonsterStatus getBySkill_Pokemon(int skill) {
        switch (skill) {
            case 120:
                return 沉默;
            case 123:
                return 眩晕;
            case 125:
                return 中毒;
            case 126:
                return 速度;
            case 137:
                return 结冰;
            case 122:
            case 124:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
        }
        return null;
    }

    public static MapleDisease getLinkedDisease(MonsterStatus stat) {
        switch (stat) {
            case 眩晕:
//            case 影网:
                return MapleDisease.昏迷;
            case 中毒:
//            case 心灵控制:
                return MapleDisease.中毒;
            case 沉默:
//            case 魔击无效:
                return MapleDisease.封印;
            case 结冰:
//                return MapleDisease.FREEZE;
//            case 反射物攻:
//                return MapleDisease.黑暗;
            case 速度:
//                return MapleDisease.缓慢;
        }
        return null;
    }

    public static int genericSkill(MonsterStatus stat) {
        switch (stat) {
            case 眩晕:
                return 90001001;
            case 速度:
                return 90001002;
            case 中毒:
                return 90001003;
//            case 反射物攻:
//                return 90001004;
            case 沉默:
                return 90001005;
            case 结冰:
                return 90001006;
//            case 魔击无效:
//                return 1111007;
            case 挑衅:
                return 4121003;
            case 影网:
                return 无影人.影网术;
//            case 烈焰喷射:
//                return 5211004;
//            case 巫毒:
//                return 2311005;
//            case 忍者伏击:
//                return 4121004;
            /*case 三角进攻:
             return 36110005;*/
        }

        return 0;
    }
}
