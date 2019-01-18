package handling;

import constants.ServerConstants;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import server.ServerProperties;
import tools.EncodingDetect;
import tools.MapleLogger;


public enum SendPacketOpcode implements WritableIntValueHolder {


    LOGIN_STATUS(0x01),
    CHOOSE_GENDER(0x02),
    GENDER_SET(0x03),
    SERVERSTATUS(0x04),
    SERVERLIST(0x05),
    CHARLIST(0x06),
    SERVER_IP(0x07),
    CHAR_NAME_RESPONSE(0x08),
    REGISTER_INFO(0x09),
    CHECK_ACCOUNT_INFO(0x0A),
    REGISTER_ACCOUNT(0x0B),
    ADD_NEW_CHAR_ENTRY(0x0C),
    DELETE_CHAR_RESPONSE(0x0D),
    CHANGE_CHANNEL(0x0E),
    PING(0x0F),
    MODIFY_INVENTORY_ITEM(0x17),
    UPDATE_INVENTORY_SLOT(0x18),
    UPDATE_STATS(0x19),
    GIVE_BUFF(0x1A),
    CANCEL_BUFF(0x1B),
    UPDATE_SKILLS(0x1D),
    FAME_RESPONSE(0x1F),
    SHOW_STATUS_INFO(0x20),
    SHOW_NOTES(0x21),
    TROCK_LOCATIONS(0x22),
    CHAR_INFO(0x24),
    PARTY_OPERATION(0x25),
    BUDDYLIST(0x26),
    SPAWN_PORTAL(0x27),
    SERVERMESSAGE(0x28),
    WARP_TO_MAP(0x2B),
    CS_CHAR(0x2C),
    MULTICHAT(0x32),
    WHISPER(0x33),
    MAP_EFFECT(0x35),
    FULLSCREEN_BLESS(0x36),
    CASH_SONG(0x37),
    GM_EFFECT(0x38),
    OX_QUIZ(0x39),
    CLOCK(0x3B),
    BOAT_EFFECT(0x3C),
    BOAT_STATE(0x3D),
    SPAWN_PLAYER(0x40),
    REMOVE_PLAYER_FROM_MAP(0x41),
    CHATTEXT(0x43),
    SPAWN_PET(0x47),
    MOVE_PET(0x48),
    PET_CHAT(0x49),
    PET_NAMECHANGE(0x4A),
    PET_COMMAND(0x4B),
    MOVE_PLAYER(0x56),
    CLOSE_RANGE_ATTACK(0x57),
    RANGED_ATTACK(0x58),
    MAGIC_ATTACK(0x59),
    SKILL_EFFECT(0x5A),
    CANCEL_SKILL_EFFECT(0x5B),
    DAMAGE_PLAYER(0x5C),
    FACIAL_EXPRESSION(0x5D),
    UPDATE_CHAR_LOOK(0x5E),
    SHOW_FOREIGN_EFFECT(0x5F),
    GIVE_FOREIGN_BUFF(0x60),
    CANCEL_FOREIGN_BUFF(0x61),
    UPDATE_PARTYMEMBER_HP(0x62),
    SHOW_CHAIR(0x65),
    SHOW_SPECIAL_EFFECT(0x66),
    MESOBAG_SUCCESS(0x6A),
    MESOBAG_FAILURE(0x6B),
    SPAWN_SUMMON(0x4E),
    REMOVE_SUMMON(0x4F),
    MOVE_SUMMON(0x50),
    SUMMON_ATTACK(0x51),
    DAMAGE_SUMMON(0x52),
    SPAWN_MONSTER(0x6F),
    KILL_MONSTER(0x70),
    SPAWN_MONSTER_CONTROL(0x71),
    MOVE_MONSTER(0x73),
    MOVE_MONSTER_RESPONSE(0x74),
    DAMAGE_MONSTER(0x7A),
    APPLY_MONSTER_STATUS(0x76),
    CANCEL_MONSTER_STATUS(0x77),
    SPAWN_NPC(0x80),
    REMOVE_NPC(0x81),
    SPAWN_NPC_REQUEST_CONTROLLER(0x82),
    NPC_ACTION(0x84),
    SKILL_EFFECT_MOB(0x7B),
    DROP_ITEM_FROM_MAPOBJECT(0x88),
    REMOVE_ITEM_FROM_MAP(0x89),
    MESSAGEBOX_ERROR(0x8C),
    SPAWN_LOVE(0x8D),
    REMOVE_LOVE(0x8E),
    SPAWN_DEFENDER(0x91),
    REMOVE_DEFENDER(0x92),
    SPAWN_DOOR(0x95),
    REMOVE_DOOR(0x96),
    REACTOR_HIT(0x99),
    REACTOR_SPAWN(0x9B),
    REACTOR_DESTROY(0x9C),
    NPC_TALK(0xA5),
    OPEN_NPC_SHOP(0xA8),
    CONFIRM_SHOP_TRANSACTION(0xA9),
    STORAGE_OPEN(0xAD),
    STORAGE_OPERATION(0xAE),
    CS_UPDATE(0xC0),
    CS_OPERATION(0xC1);

    private byte code = -2;
    public static boolean record = false;

    SendPacketOpcode(int code) {
        this.code = (byte)code;
    }

    @Override
    public void setValue(byte code) {
        this.code = code;
    }

    @Override
    public byte getValue() {
        if (ServerProperties.ShowPacket()) {
            if (isRecordHeader(this)) {
                record = true;
                MapleLogger.info("[服务端发送] " + name() + "  [0x" + code + "]  \r\n");
            } else {
                record = false;
            }
        }
        return this.code;
    }

    public short getValue(boolean show) {
        return code;
    }

    public boolean isRecordHeader(SendPacketOpcode opcode) {
        switch (opcode) {
//            case MOVE_MONSTER_RESPONSE:
//            case WARP_TO_MAP:
//            case GUILD_OPERATION:
//            case PARTY_OPERATION:
//            case GIVE_BUFF:
//            case SPAWN_PLAYER:
//                return true;
            default:
                return false;
        }
    }

    public static boolean isSpamHeader(SendPacketOpcode opcode) {
        switch (opcode) {
//            case MOVE_MONSTER_RESPONSE:
//            case SPAWN_MONSTER:
//            case SPAWN_MONSTER_CONTROL:
//            case NPC_ACTION:
//                return true;
            default:
                return false;
        }
    }
}
