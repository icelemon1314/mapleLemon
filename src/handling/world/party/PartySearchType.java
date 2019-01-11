package handling.world.party;

public enum PartySearchType {

    Kerning(20, 200, 1000, false),
    Ludi(30, 200, 1001, false),
    Orbis(50, 200, 1002, false),
    Pirate(60, 200, 1003, false),
    Magatia(70, 200, 1004, false),
    ElinForest(40, 200, 1005, false),
    Pyramid(40, 200, 1008, false),
    Dragonica(100, 200, 1009, false),
    Hoblin(80, 200, 1011, false),
    Henesys(10, 200, 1012, false),
    武陵道场(25, 200, 1013, false),
    Balrog_Normal(50, 250, 2000, true),
    Zakum(50, 250, 2002, true),
    Horntail(80, 250, 2003, true),
    PinkBean(140, 250, 2004, true),
    ChaosZakum(100, 250, 2005, true),
    ChaosHT(110, 250, 2006, true),
    VonLeon(120, 250, 2007, true),
    Cygnus(170, 250, 2008, true),
    Akyrum(120, 250, 2009, true),
    Hillah(120, 250, 2010, true),
    ChaosPB(170, 250, 2011, true),
    CWKPQ(90, 250, 2011, true);

    public int id;
    public int minLevel;
    public int maxLevel;
    public int timeLimit;
    public boolean exped;

    PartySearchType(int minLevel, int maxLevel, int value, boolean exped) {
        this.id = value;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.exped = exped;
        this.timeLimit = (exped ? 20 : 5);
    }

    public static PartySearchType getById(int id) {
        for (PartySearchType pst : values()) {
            if (pst.id == id) {
                return pst;
            }
        }
        return null;
    }
}
