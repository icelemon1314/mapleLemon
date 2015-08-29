package handling.world.party;

public enum ExpeditionType {

    Normal_Balrog(15, 2000, 50, 250),
    Zakum(30, 2002, 50, 250),
    Horntail(30, 2003, 80, 250),
    Pink_Bean(30, 2004, 140, 250),
    Chaos_Zakum(30, 2005, 100, 250),
    ChaosHT(30, 2006, 110, 250),
    Von_Leon(18, 2007, 120, 250),
    Cygnus(18, 2008, 170, 250),
    Akyrum(18, 2009, 120, 250),
    Hillah(6, 2010, 120, 250),
    Chaos_Pink_Bean(6, 2011, 170, 250),
    CWKPQ(30, 2011, 90, 250);

    public int maxMembers;
    public int maxParty;
    public int exped;
    public int minLevel;
    public int maxLevel;

    private ExpeditionType(int maxMembers, int exped, int minLevel, int maxLevel) {
        this.maxMembers = maxMembers;
        this.exped = exped;
        this.maxParty = (maxMembers / 2 + (maxMembers % 2 > 0 ? 1 : 0));
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public static ExpeditionType getById(int id) {
        for (ExpeditionType pst : values()) {
            if (pst.exped == id) {
                return pst;
            }
        }
        return null;
    }
}
