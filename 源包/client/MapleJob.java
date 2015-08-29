package client;

/**
 * 职业代码
 *
 * @author 7
 */
public enum MapleJob {

    新手(0),
    战士(100),
    剑客(110), 勇士(111), 英雄(112),
    准骑士(120), 骑士(121), 圣骑士(122),
    枪战士(130), 龙骑士(131), 黑骑士(132),
    魔法师(200),
    火毒法师(210), 火毒巫师(211), 火毒魔导师(212),
    冰雷法师(220), 冰雷巫师(221), 冰雷魔导师(222),
    牧师(230), 祭祀(231), 主教(232),
    弓箭手(300),
    猎人(310), 射手(311), 神射手(312),
    弩弓手(320), 游侠(321), 箭神(322),
    飞侠(400),
    刺客(410), 无影人(411), 隐士(412),
    侠客(420), 独行客(421), 侠盗(422),
    管理者(800), 管理员(900);

    private final int jobid;

    private MapleJob(int id) {
        this.jobid = id;
    }

    public int getId() {
        return this.jobid;
    }

    public static String getName(MapleJob basejob) {
        return basejob.name();
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static String getJobName(int jobid) {
        return getName(getById(jobid));
    }

    public static boolean isExist(int id) {
        for (MapleJob job : values()) {
            if (job.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
