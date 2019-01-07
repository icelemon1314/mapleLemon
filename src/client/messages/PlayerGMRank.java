package client.messages;

public enum PlayerGMRank {

    NORMAL(new char[]{'@'}, 0),
    DONATOR(new char[]{'@'}, 1),
    SUPERDONATOR(new char[]{'@'}, 2),
    INTERN(new char[]{'!', '！'}, 3),
    GM(new char[]{'!', '！'}, 4),
    SUPERGM(new char[]{'!', '！'}, 5),
    ADMIN(new char[]{'!', '！'}, 6);

    private final char[] commandPrefix;
    private final int level;

    private PlayerGMRank(char[] chs, int level) {
        commandPrefix = chs;
        this.level = level;
    }

    public char[] getCommandPrefix() {
        return commandPrefix;
    }

    public int getLevel() {
        return this.level;
    }

    public static PlayerGMRank getByLevel(int level) {
        for (PlayerGMRank i : PlayerGMRank.values()) {
            if (i.getLevel() == level) {
                return i;
            }
        }
        return PlayerGMRank.NORMAL;
    }
}
