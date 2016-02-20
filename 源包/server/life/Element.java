package server.life;

public enum Element {

    NEUTRAL(0), // 中立
    PHYSICAL(1),  //物理
    FIRE(2, true), // 火
    ICE(3, true),  // 冰
    LIGHTING(4),
    POISON(5), // 毒
    HOLY(6, true),  //神圣
    DARKNESS(7);  // 黑暗

    private int value;
    private boolean special = false;

    private Element(int v) {
        this.value = v;
    }

    private Element(int v, boolean special) {
        this.value = v;
        this.special = special;
    }

    public boolean isSpecial() {
        return this.special;
    }

    public static Element getFromChar(char c) {
        switch (Character.toUpperCase(c)) {
            case 'F':
                return FIRE;
            case 'I':
                return ICE;
            case 'L':
                return LIGHTING;
            case 'S':
                return POISON;
            case 'H':
                return HOLY;
            case 'P':
                return PHYSICAL;
            case 'D':
                return DARKNESS;
            case 'E':
            case 'G':
            case 'J':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'Q':
            case 'R':
        }
        throw new IllegalArgumentException("unknown elemnt char " + c);
    }

    public static Element getFromId(int c) {
        for (Element e : values()) {
            if (e.value == c) {
                return e;
            }
        }
        throw new IllegalArgumentException("unknown elemnt id " + c);
    }

    public int getValue() {
        return this.value;
    }
}
