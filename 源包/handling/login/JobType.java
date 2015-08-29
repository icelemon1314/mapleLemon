package handling.login;

/**
 * 新玩家出生地图和职业ID
 * @author 7
 */
public enum JobType {

    冒险家(1, 0, 4000000, 1, false, false, false);
    public int type;
    public int jobId, level;
    public int mapId;
    private final boolean 自由市场 = false;
    public boolean faceMark;
    public boolean cape;
    public boolean bottom;
    public boolean cap;

    private JobType(int type, int id, int map, int level, boolean faceMark, boolean cape, boolean bottom) {
        this.type = type;
        this.jobId = id;
        this.level = level;
        this.mapId = (this.自由市场 ? 910000000 : map);
        this.faceMark = faceMark;
        this.cape = cape;
        this.bottom = bottom;
        this.cap = false;
    }

    private JobType(int type, int id, int map, int level, boolean faceMark, boolean cape, boolean bottom, boolean cap) {
        this.type = type;
        this.jobId = id;
        this.level = level;
        this.mapId = (this.自由市场 ? 910000000 : map);
        this.faceMark = faceMark;
        this.cape = cape;
        this.bottom = bottom;
        this.cap = cap;
    }

    public static JobType getByType(int g) {
        for (JobType e : values()) {
            if (e.type == g) {
                return e;
            }
        }
        return null;
    }

    public static JobType getById(int wzNmae) {
        for (JobType e : values()) {
            if ((e.jobId == wzNmae) || ((wzNmae == 508) && (e.type == 13)) || ((wzNmae == 1) && (e.type == 8))) {
                return e;
            }
        }
        return 冒险家;
    }
}
