package handling;

import constants.ServerConstants;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import tools.EncodingDetect;

public enum RecvPacketOpcode implements WritableIntValueHolder {

    PONG(false),
    CLIENT_HELLO(false),
    LOGIN_PASSWORD(false),
    CHARLIST_REQUEST,
    CHAR_SELECT,
    UPDATE_CHANNEL,
    PLAYER_LOGGEDIN(false),
    CHECK_REGISTER_ACCOUNT(false),
    CHECK_ACCOUNT(false),
    REGISTER_ACCOUNT(false),
    CHECK_CHAR_NAME,
    CREATE_CHAR,
    DELETE_CHAR,
    GET_SERVER(false),
    CREATE_ULTIMATE,
    CLIENT_ERROR(false),
    STRANGE_DATA,
    AUTH_SECOND_PASSWORD,
    SET_WORK,
    ENTER,
    LICENSE_REQUEST,
    SET_GENDER,
    CHAR_CARD,
    SET_ACC_CASH,
    LOAD_PLAYER_SCCUCESS,
    QUICK_BUY_CS_ITEM,
    SERVERSTATUS_REQUEST,
    SERVERLIST_REQUEST,
    SEND_ENCRYPTED(false),
    REDISPLAY_SERVERLIST,
    VIEW_ALL_CHAR,
    VIEW_REGISTER_PIC,
    VIEW_SELECT_PIC,
    PICK_ALL_CHAR,
    CHAR_SELECT_NO_PIC,
    VIEW_SERVERLIST,
    PACKET_ERROR(false),
    CLIENT_START(false),
    CLIENT_FAILED(false),
    CHANGE_MAP,
    CHANGE_CHANNEL,
    ENTER_CASH_SHOP,
    MOVE_PLAYER,
    CANCEL_CHAIR,
    USE_CHAIR,
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    PASSIVE_ATTACK,
    TAKE_DAMAGE,
    GENERAL_CHAT,
    CLOSE_CHALKBOARD,
    FACE_EXPRESSION,
    FACE_ANDROID,
    USE_ITEM_EFFECT,
    WHEEL_OF_FORTUNE,
    USE_TITLE_EFFECT,
    USE_UNK_EFFECT,
    NPC_TALK,
    REMOTE_STORE,
    NPC_TALK_MORE,
    NPC_SHOP,
    STORAGE,
    USE_HIRED_MERCHANT,
    MERCH_ITEM_STORE,
    DUEY_ACTION,
    MECH_CANCEL,
    USE_HOLY_FOUNTAIN,
    OWL,
    OWL_WARP,
    ITEM_SORT,
    ITEM_GATHER,
    ITEM_MOVE,
    MOVE_BAG,
    SWITCH_BAG,
    USE_ITEM,
    CANCEL_ITEM_EFFECT,
    USE_SUMMON_BAG,
    PET_FOOD,
    USE_MOUNT_FOOD,
    USE_SCRIPTED_NPC_ITEM,
    USE_RECIPE,
    USE_ALIEN_SOCKET,
    USE_ALIEN_SOCKET_RESPONSE,
    USE_CASH_ITEM,
    USE_ADDITIONAL_ITEM,
    ALLOW_PET_LOOT,
    ALLOW_PET_AOTO_EAT,
    USE_CATCH_ITEM,
    USE_SKILL_BOOK,
    USE_SP_RESET,
    USE_AP_RESET,
    POTION_POT_USE,
    POTION_POT_ADD,
    POTION_POT_MODE,
    POTION_POT_INCR,
    USE_OWL_MINERVA,
    USE_TELE_ROCK,
    USE_RETURN_SCROLL,
    USE_UPGRADE_SCROLL,
    USE_FLAG_SCROLL,
    USE_EQUIP_SCROLL,
    USE_POTENTIAL_SCROLL,
    USE_POTENTIAL_ADD_SCROLL,
    USE_SOULS_SCROLL,
    USE_SOUL_MARBLE,
    USE_BAG,
    USE_MAGNIFY_GLASS,
    USE_CARVED_SEAL,
    DISTRIBUTE_AP,
    AUTO_ASSIGN_AP,
    HEAL_OVER_TIME,
    TEACH_SKILL,
    DISTRIBUTE_SP,
    SPECIAL_SKILL,
    AFTER_SKILL,
    CANCEL_BUFF,
    SKILL_EFFECT,
    MESO_DROP,
    GIVE_FAME,
    CHAR_INFO_REQUEST,
    SPAWN_PET,
    CANCEL_DEBUFF,
    CHANGE_MAP_SPECIAL,
    UNK0A3,
    USE_INNER_PORTAL,
    TROCK_ADD_MAP,
    LIE_DETECTOR,
    LIE_DETECTOR_SKILL,
    LIE_DETECTOR_RESPONSE,
    LIE_DETECTOR_REFRESH,
    QUEST_ACTION,
    REISSUE_MEDAL,
    SPECIAL_ATTACK,
    REWARD_ITEM,
    ITEM_MAKER,
    REPAIR_ALL,
    REPAIR,
    SOLOMON,
    GACH_EXP,
    FOLLOW_REQUEST,
    FOLLOW_REPLY,
    AUTO_FOLLOW_REPLY,
    REPORT,
    PROFESSION_INFO,
    USE_POT,
    CLEAR_POT,
    FEED_POT,
    CURE_POT,
    REWARD_POT,
    USE_COSMETIC,
    USE_REDUCER,
    CHANGE_ZERO_LOOK,
    CHANGE_ZERO_LOOK_END,
    PARTYCHAT,
    WHISPER,
    MESSENGER,
    PLAYER_INTERACTION,
    PARTY_OPERATION,
    DENY_PARTY_REQUEST,
    ALLOW_PARTY_INVITE,
    GUILD_OPERATION,
    DENY_GUILD_REQUEST,
    JOIN_GUILD_REQUEST,
    JOIN_GUILD_CANCEL,
    ALLOW_GUILD_JOIN,
    DENY_GUILD_JOIN,
    ADMIN_COMMAND,
    ADMIN_LOG,
    BUDDYLIST_MODIFY,
    NOTE_ACTION,
    USE_DOOR,
    USE_MECH_DOOR,
    CHANGE_KEYMAP,
    RPS_GAME,
    RING_ACTION,
    ALLIANCE_OPERATION,
    DENY_ALLIANCE_REQUEST,
    REQUEST_FAMILY,
    OPEN_FAMILY,
    FAMILY_OPERATION,
    DELETE_JUNIOR,
    DELETE_SENIOR,
    ACCEPT_FAMILY,
    USE_FAMILY,
    FAMILY_PRECEPT,
    FAMILY_SUMMON,
    CYGNUS_SUMMON,
    ARAN_COMBO,
    LOST_ARAN_COMBO,
    CRAFT_DONE,
    CRAFT_EFFECT,
    CRAFT_MAKE,
    BBS_OPERATION,
    CHANGE_MARKET_MAP,
    CHANGE_PLAYER,
    MEMORY_SKILL_CHOOSE,
    MEMORY_SKILL_CHANGE,
    MEMORY_SKILL_OBTAIN,
    GAME_POLL,
    BUY_CROSS_ITEM,
    USE_JIANRENZHIBI,
    DISTRIBUTE_HYPER_SP,
    RESET_HYPER_SP,
    MOVE_PET,
    PET_CHAT,
    PET_COMMAND,
    PET_LOOT,
    PET_AUTO_POT,
    PET_EXCEPTION_LIST,
    PET_AOTO_EAT,
    MOVE_SUMMON,
    SUMMON_ATTACK,
    DAMAGE_SUMMON,
    SUB_SUMMON,
    REMOVE_SUMMON,
    MOVE_DRAGON,
    DRAGON_FLY,
    MOVE_ANDROID,
    QUICK_SLOT,
    PLAYER_VIEW_RANGE,
    OPEN_ROOT_NPC,
    SYSTEM_PROCESS_LIST,
    SHOW_LOVE_RANK,
    TRANSFORM_PLAYER,
    OPEN_AVATAR_RANDOM_BOX,
    ENTER_MTS,
    USE_TREASUER_CHEST,
    MACROSS_TICKET,
    PAM_SONG,
    SET_CHAR_CASH,
    MOVE_LIFE,
    AUTO_AGGRO,
    FRIENDLY_DAMAGE,
    MONSTER_BOMB,
    HYPNOTIZE_DMG,
    MOB_BOMB,
    MOB_NODE,
    DISPLAY_NODE,
    NPC_ACTION,
    ITEM_PICKUP,
    DAMAGE_REACTOR,
    TOUCH_REACTOR,
    MAKE_EXTRACTOR,
    SNOWBALL,
    LEFT_KNOCK_BACK,
    COCONUT,
    MONSTER_CARNIVAL,
    SHIP_OBJECT,
    PLAYER_UPDATE,
    PARTY_MEMBER_SEARCH,
    PARTY_SEARCH,
    START_HARVEST,
    STOP_HARVEST,
    QUICK_MOVE_SPECIAL,
    QUICK_MOVE,
    CS_UPDATE,
    BUY_CS_ITEM,
    COUPON_CODE,
    SEND_CS_GIFI,
    SEND_CS_HOT,
    MAPLETV,
    UPDATE_QUEST,
    QUEST_ITEM,
    USE_ITEM_QUEST,
    TOUCHING_MTS,
    MTS_TAB,
    CHANGE_SET,
    GET_BOOK_INFO,
    CLICK_REACTOR,
    USE_FAMILIAR,
    SPAWN_FAMILIAR,
    RENAME_FAMILIAR,
    MOVE_FAMILIAR,
    TOUCH_FAMILIAR,
    ATTACK_FAMILIAR,
    SIDEKICK_OPERATION,
    DENY_SIDEKICK_REQUEST,
    PVP_INFO,
    ENTER_PVP,
    ENTER_PVP_PARTY,
    LEAVE_PVP,
    PVP_RESPAWN,
    PVP_ATTACK,
    PVP_SUMMON,
    USE_HAMMER,
    HAMMER_RESPONSE,
    GUIDE_TRANSFER,//游戏向导
    EXIT_GAME,
    BOSS_PARTY_SEARCH_REQUEST,
    BOSS_PARTY_SEARCH,
    USE_CUBE,
    ARROWS_TURRET_ATTACK, //箭矢炮盘 射箭的包
    SPAWN_ARROWS_TURRET, //召唤 箭矢炮盘
    REST_INTERNAL_ABILITY,
    RETURN_CRAFT,
    POINT_POWER,
    VOID_PRESSURE,//虚空重压
    SUPER_SPECTRA,//光法师超级光谱
    GETMONOID,
    UPDATE_MAC_SKILL,
    MIST_ATTACK,
    OPEN_MAP,
    FlLAMES_TRACK,
    SPECIAL_MAGIC_ATTACK,
    RUNE_OPERATION,
    RUNE_RESPONSE,
    ES_OPERATION,
    FREE_TRANFSER,;

    private byte code = -2;
    private final boolean CheckState;

    @Override
    public void setValue(byte code) {
        this.code = code;
    }

    @Override
    public byte getValue() {
        return code;
    }

    private RecvPacketOpcode() {
        CheckState = true;
    }

    private RecvPacketOpcode(boolean CheckState) {
        this.CheckState = CheckState;
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
            case PONG:
            case MOVE_LIFE:
            case NPC_ACTION:
//            case CRASH_INFO:
//            case AUTH_REQUEST:
//            case MOVE_PLAYER:
//            case SPECIAL_SKILL:
//            case MOVE_FAMILIAR:
//            case QUEST_ACTION:
//            case HEAL_OVER_TIME:
//            case CHANGE_KEYMAP:
//            case USE_INNER_PORTAL:
//            case MOVE_HAKU:
//            case FRIENDLY_DAMAGE:
//            case CLOSE_RANGE_ATTACK:
//            case RANGED_ATTACK:
//            case ARAN_COMBO:
//            case SPECIAL_STAT:
//            case UPDATE_HYPER:
//            case RESET_HYPER:
//            case ANGELIC_CHANGE:
//            case DRESSUP_TIME:
                return true;
        }
        return false;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("recvops.properties")) {
            props.load(new BufferedReader(new InputStreamReader(fileInputStream, EncodingDetect.getJavaEncode("recvops.properties"))));
        }
        return props;
    }

    public static void reloadValues() {
        try {
            if (ServerConstants.loadop) {
                Properties props = new Properties();
                props.load(RecvPacketOpcode.class.getClassLoader().getResourceAsStream("recvops.ini"));
                ExternalCodeTableGetter.populateValues(props, values());
            } else {
                ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
            }
        } catch (IOException e) {
            throw new RuntimeException("加载 recvops.properties 文件出现错误", e);
        }
    }

    static {
        reloadValues();
    }
}
