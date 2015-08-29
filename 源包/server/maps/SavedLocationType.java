package server.maps;

public enum SavedLocationType {

    FREE_MARKET(0),
    MULUNG_TC(1),
    WORLDTOUR(2),
    FLORINA(3),
    FISHING(4),
    RICHIE(5),
    DONGDONGCHIANG(6),
    EVENT(7),
    AMORIA(8),
    CHRISTMAS(9),
    ARDENTMILL(10),
    TURNEGG(11),
    PVP(12),
    GUILD(13),
    FAMILY(14),
    MonsterPark(15),
    ROOT(16),
    BPReturn(17);

    private final int index;

    private SavedLocationType(int index) {
        this.index = index;
    }

    public int getValue() {
        return this.index;
    }

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }
}
