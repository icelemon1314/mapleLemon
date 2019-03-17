package handling;

public enum RecvPacketOpcode implements WritableIntValueHolder {
    // The name must follow the rule:handleType_handleClass

    LOGIN_LOGIN_PASSWORD(0x01,false),
    LOGIN_SET_GENDER(0x02),
    LOGIN_SERVER_STATUS_REQUEST(0x03),
    LOGIN_CHARLIST_REQUEST(0x04),
    LOGIN_CHAR_SELECT(0x05),
    LOGIN_PLAYER_ENTER_GAME(0x06,false),
    LOGIN_CHECK_CHAR_NAME(0x07),
    LOGIN_CHECK_CAN_REGISTER(0x08),
    LOGIN_CHECK_ACCOUNT(0x09),
    LOGIN_REGISTER_ACCOUNT(0x0A,false),
    LOGIN_CREATE_CHAR(0x0B),
//    LOGIN_PONG(0x0D,false),
    LOGIN_CLIENT_ERROR(0x0E,false),
//
    LOGIN_PACKET_ERROR(0x12),

    CHANNEL_CHANGE_MAP(0x15),
    CHANNEL_CHANGE_CHANNEL(0x16),
    CHANNEL_ENTER_CASH_SHOP(0x17),
    CHANNEL_MOVE_PLAYER(0x18),
    CHANNEL_USE_CHAIR(0x19),
    CHANNEL_CLOSE_RANGE_ATTACK(0x1A),
    CHANNEL_RANGE_ATTACK(0x1B),
    CHANNEL_MAGIC_ATTACK(0x1C),
    CHANNEL_PLAYER_TAKE_DAMAGE(0x1E),
    CHANNEL_GENERAL_CHAT(0x1F),
    CHANNEL_FACE_EXPRESSION(0x20),

    CHANNEL_NPC_TALK(0x23),
    CHANNEL_NPC_TALK_MORE(0x24),
    CHANNEL_OPEN_NPC_SHOP(0x25),
    CHANNEL_OPEN_STORAGE(0x26),
    CHANNEL_ITEM_MOVE(0x27),
    CHANNEL_USE_ITEM(0x28),
    CHANNEL_USE_SUMMON_BAG(0x29),
    CHANNEL_USE_PET_FOOD(0x2A),
    CHANNEL_USE_CASH_ITEM(0x2B),
    CHANNEL_USE_RETURN_SCROLL(0x2C),
    CHANNEL_USE_UPGRADE_SCROLL(0x2D),
    CHANNEL_DISTRIBUTE_AP(0x2E),
    CHANNEL_HEAL_OVER_TIME(0x2F),
    CHANNEL_DISTRIBUTE_SP(0x30),
    CHANNEL_SPECIAL_SKILL(0x31),
    CHANNEL_CANCEL_BUFF(0x32),
    CHANNEL_SKILL_EFFECT(0x33),
    CHANNEL_MESO_DROP(0x34),
    CHANNEL_CHAR_INFO_REQUEST(0x37),
    CHANNEL_SPAWN_PET(0x38),
    CHANNEL_CHANGE_MAP_SPECIAL(0x3A),
    CHANNEL_TROCK_ADD_MAP(0x3B),
    CHANNEL_USE_DOOR(0x49),
    CHANNEL_PET_MOVE(0x4C),
    CHANNEL_PET_LOOT(0x4F),
    CHANNEL_SUMMON_MOVE(0x52),
    CHANNEL_SUMMON_ATTACK(0x53),
    CHANNEL_SUMMON_DAMAGE(0x54),
    CHANNEL_SUMMON_SUB(0x55),
    CHANNEL_SUMMON_REMOVE(0x55),
    CHANNEL_MOVE_LIFE(0x5A),
    CHANNEL_NPC_ACTION(0x5F),
    CHANNEL_ITEM_PICKUP(0x63),

    CASHSHOP_CS_UPDATE(0x72),
    CASHSHOP_BUY_CASH_ITEM(0x73),
    CASHSHOP_COUPON_CODE(0x74);

    private byte code = -2;
    private final boolean CheckState;
    private int handlerType;

    @Override
    public void setValue(byte code) {
        this.code = code;
    }

    @Override
    public byte getValue() {
        return code;
    }

    RecvPacketOpcode(int code) {
        this.code = (byte)code;
        this.CheckState = true;
    }

    RecvPacketOpcode(int code, boolean checkState) {
        this.code = (byte)code;
        this.CheckState = checkState;
    }

    public boolean NeedsChecking() {
        return CheckState;
    }

    public static boolean isTempHeader(RecvPacketOpcode header) {
        switch (header) {
//            case MOVE_LIFE:
//            case MOVE_PLAYER:
//                return false;
        }
        return true;
    }

    public static boolean isSpamHeader(RecvPacketOpcode header) {
        switch (header) {
//            case PONG:
            case CHANNEL_MOVE_LIFE:
            case CHANNEL_NPC_ACTION:
                return true;
        }
        return false;
    }
}
