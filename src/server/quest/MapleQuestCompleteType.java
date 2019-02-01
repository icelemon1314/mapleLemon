package server.quest;

public enum MapleQuestCompleteType {

    UNDEFINED(-1),
    job(0),
    item(1),
    quest(2),
    lvmin(3),
    lvmax(4),
    end(5),
    mob(6),
    npc(7),
    fieldEnter(8),
    interval(9),
    startscript(10),
    endscript(10),
    pet(11),
    pettamenessmin(12),
    mbmin(13),
    questComplete(14),
    pop(15),
    skill(16),
    mbcard(17),
    subJobFlags(18),
    dayByDay(19),
    normalAutoStart(20),
    partyQuest_S(21),
    charmMin(22),
    senseMin(23),
    craftMin(24),
    willMin(25),
    charismaMin(26),
    insightMin(27),
    exp(28);

    final byte type;

    public MapleQuestCompleteType getITEM() {
        return item;
    }

    MapleQuestCompleteType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return this.type;
    }

    public static MapleQuestCompleteType getByType(byte type) {
        for (MapleQuestCompleteType l : values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleQuestCompleteType getByWZName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
        }
        return UNDEFINED;
    }
}
