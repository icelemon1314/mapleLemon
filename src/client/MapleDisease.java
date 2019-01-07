package client;

import handling.Buffstat;
import java.io.Serializable;
import server.Randomizer;

/**
 * 角色获得DEBUFF的基址
 *
 */
public enum MapleDisease implements Serializable, Buffstat {

    
    DISABLE_POTENTIAL(MapleBuffStat.DISABLE_POTENTIAL, 138),
    虚弱(MapleBuffStat.虚弱, 122),
    黑暗(MapleBuffStat.黑暗, 121),
    封印(MapleBuffStat.封印, 120),
    中毒(MapleBuffStat.中毒, 125),
    昏迷(MapleBuffStat.昏迷, 123),
    诅咒(MapleBuffStat.诅咒, 122 );
    private static final long serialVersionUID = 0L;
    private final int buffstat;
    private final int first;
    private final int disease;//技能代码

    private MapleDisease(int i, int first, int disease) {
        this.buffstat = i;
        this.first = first;
        this.disease = disease;
    }

    private MapleDisease(MapleBuffStat buffstat, int disease) {
        this.buffstat = buffstat.getValue();
        this.first = buffstat.getPosition();
        this.disease = disease;
    }

    @Override
    public int getPosition() {
        return this.first;
    }

    @Override
    public int getValue() {
        return this.buffstat;
    }

    public int getDisease() {
        return this.disease;
    }

    public static MapleDisease getRandom() {
        while (true) {
            for (MapleDisease dis : values()) {
                if (Randomizer.nextInt(values().length) == 0) {
                    return dis;
                }
            }
        }
    }

    public static MapleDisease getBySkill(int skill) {
        for (MapleDisease d : values()) {
            if (d.getDisease() == skill) {
                return d;
            }
        }
        return null;
    }
}
