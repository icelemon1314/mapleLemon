package tools.packet;

import client.BuddylistEntry;
import handling.SendPacketOpcode;
import java.util.Collection;
import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.data.output.MaplePacketLittleEndianWriter;

public class BuddyListPacket {

    private static final Logger log = Logger.getLogger(BuddyListPacket.class);

    public static byte[] buddylistMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] buddylistPrompt(int mode, String nameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(nameFrom);
        return mplew.getPacket();
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int characterid) {
        return updateBuddylist(buddylist, 0x11, false, characterid);
    }

    public static byte[] updateBuddylist(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //1F 00 00 00 00
        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(mode);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int mode, boolean delete, int characterid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(mode);
        if (delete) {
            mplew.write(0);
            mplew.writeInt(characterid);
        } else {
            mplew.write(buddylist.size());
            for (BuddylistEntry buddy : buddylist) {
                mplew.writeInt(buddy.getCharacterId());
                mplew.writeAsciiString(buddy.getName(), 0x13);
                mplew.write(buddy.isVisible() ? 0 : 1);
                mplew.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
            }
            for (int x = 0; x < buddylist.size(); x++) {
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] requestBuddylistAdd(int chrIdFrom, String nameFrom, int levelFrom, int jobFrom, int channel, int AccID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x9);
        mplew.writeInt(chrIdFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(levelFrom);
        mplew.writeAsciiString(nameFrom, 0x13);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(channel == -1 ? -1 : channel - 1);
        return mplew.getPacket();
    }

    public static byte[] requestBuddylistAdd(int chrIdFrom, String nameFrom, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x20);
        mplew.writeInt(chrIdFrom);
        mplew.writeAsciiString(nameFrom, 13);
        mplew.write(0);
        //mplew.writeInt(0);
        mplew.writeInt(channel == -1 ? -1 : channel - 1);
        mplew.writeAsciiString("未指定群组", 18);
        mplew.writeZero(277);
        return mplew.getPacket();
    }

    public static byte[] updateBuddyChannel(int chrId, int channel, int AccID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(chrId);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    public static byte[] updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);

        return mplew.getPacket();
    }
}
