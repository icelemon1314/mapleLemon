package tools.packet;

import handling.SendPacketOpcode;
import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UIPacket {

    private static final Logger log = Logger.getLogger(UIPacket.class);

    public static byte[] EarnTitleMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.EARN_TITLE_MSG.getValue());
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getTopMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TOP_MSG.getValue());
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getMidMsg(String msg, boolean keep, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(index);
        mplew.writeMapleAsciiString(msg);
        mplew.write(keep ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] clearMidMsg() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.CLEAR_MID_MSG.getValue());

        return mplew.getPacket();
    }

    public static byte[] MapEff(String path) {
        return MaplePacketCreator.showEffect(path);
    }

    public static byte[] MapNameDisplay(int mapid) {
        return MaplePacketCreator.environmentChange("maplemap/enter/" + mapid, 4);
    }

    public static byte[] TutInstructionalBalloon(String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x17);
        mplew.writeMapleAsciiString(data);
//        mplew.writeInt(1);

        return mplew.getPacket();
    }

    public static byte[] ShowWZEffect(String data) {
        return ShowWZEffect(0x17, data);
    }

    public static byte[] ShowWZEffect(int type, String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(type);
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] playMovie(String data, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAY_MOVIE.getValue());
        mplew.writeMapleAsciiString(data);
        mplew.write(show ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] summonHelper(boolean summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SUMMON_HINT.getValue());
        mplew.write(summon ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] summonMessage(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(1);
        mplew.writeInt(type);
        mplew.writeInt(7000);

        return mplew.getPacket();
    }

    public static byte[] summonMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(200);
        mplew.writeInt(10000);

        return mplew.getPacket();
    }

    public static byte[] IntroLock(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.INTRO_LOCK.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionStatus(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_STATUS.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfo(int type, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(type);
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfo(String data, int value, int x, int y, int pro) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.writeShort(pro);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfo(String data, int value, int x, int y, int a, int b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.write(a);
        if (a > 0) {
            mplew.writeInt(0);
        }
        mplew.write(b);
        if (b > 1) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] getDirectionEffect(String data, int value, int x, int y) {
        return getDirectionEffect(data, value, x, y, 0);
    }

    public static byte[] getDirectionEffect(String data, int value, int x, int y, int npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(npc);
        mplew.write(npc == 0 ? 1 : 0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfoNew(byte x, int value, int a, int b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(5);
        mplew.write(x);
        mplew.writeInt(value);
//        if (x == 0) {
        mplew.writeInt(a);
        mplew.writeInt(b);
//        }

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfoNew(byte x, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(5);
        mplew.write(x);
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getDirectionInfoNew2(byte x, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(5);
        mplew.write(x);
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getDirectionEffect1(String data, int value, int x, int y, int npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.write(1);
        mplew.writeInt(npc);
        mplew.write(0);
        // Added for BeastTamer
        return mplew.getPacket();
    }

    public static byte[] get剧情弹幕(String data, int value, int s, boolean mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(8);
        mplew.writeShort(value);
        for (int i = 0; i < s; i++) {
            mplew.writeShort(2573);
        }
        mplew.writeAsciiString(data);
        mplew.write(mode ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getHidePlayer(String data, int value, int s, boolean mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(2);
        mplew.writeAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] getDIRECTION_INFO(String data, int value, int s) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_INFO.getValue());
        mplew.write(6);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(value);
        mplew.writeInt(0);
        mplew.writeInt(s);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] IntroEnableUI(int enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.INTRO_ENABLE_UI.getValue());
        mplew.write(enable > 0 ? 1 : 0);
        if (enable > 0) {
            mplew.writeShort(enable);
            mplew.write(0);//119+
        } else {
            mplew.write(enable < 0 ? 1 : 0);
        }

        return mplew.getPacket();
    }

    public static byte[] IntroDisableUI(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.INTRO_DISABLE_UI.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] fishingUpdate(byte type, int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FISHING_BOARD_UPDATE.getValue());
        mplew.write(type);
        mplew.writeInt(id);

        return mplew.getPacket();
    }

    public static byte[] fishingCaught(int chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.FISHING_CAUGHT.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }

    public static byte[] getDirectionFacialExpression(int expression, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.DIRECTION_FACIAL_EXPRESSION.getValue());
        mplew.writeInt(expression);
        mplew.writeInt(duration);
        mplew.write(0);

        /* Facial Expressions:
         * 0 - Normal 
         * 1 - F1
         * 2 - F2
         * 3 - F3
         * 4 - F4
         * 5 - F5
         * 6 - F6
         * 7 - F7
         * 8 - Vomit
         * 9 - Panic
         * 10 - Sweetness
         * 11 - Kiss
         * 12 - Wink
         * 13 - Ouch!
         * 14 - Goo goo eyes
         * 15 - Blaze
         * 16 - Star
         * 17 - Love
         * 18 - Ghost
         * 19 - Constant Sigh
         * 20 - Sleepy
         * 21 - Flaming hot
         * 22 - Bleh
         * 23 - No Face
         */
        return mplew.getPacket();
    }

    public static byte[] openUIOption(int type, int npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        mplew.write(SendPacketOpcode.OPEN_UI_OPTION.getValue());
        mplew.writeInt(type);
        mplew.writeInt(npc);
        return mplew.getPacket();
    }

    public static byte[] openMap() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.OPEN_MAP.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] openUI(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.write(SendPacketOpcode.OPEN_UI.getValue());
        mplew.writeInt(type);

        return mplew.getPacket();
    }
}
