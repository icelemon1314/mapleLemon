package handling.world.messenger;

public enum MessengerType {

    随机聊天(2, true),
    随机多人聊天(6, true),
    好友聊天(6, false);

    public int maxMembers;
    public boolean random;

    private MessengerType(int maxMembers, boolean random) {
        this.maxMembers = maxMembers;
        this.random = random;
    }

    public static MessengerType getMessengerType(int maxMembers, boolean random) {
        for (MessengerType mstype : values()) {
            if ((mstype.maxMembers == maxMembers) && (mstype.random == random)) {
                return mstype;
            }
        }
        return null;
    }
}
