package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import handling.world.WorldGuildService;
import handling.world.guild.MapleGuild;
import handling.world.messenger.MessengerRankingWorker;
import org.apache.log4j.Logger;
import server.ServerProperties;
import tools.DateUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MessengerPacket {

    private static final Logger log = Logger.getLogger(MessengerPacket.class);

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true, chr.isZeroSecondLook());
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(position);
        mplew.writeInt(chr.getJob());

        return mplew.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(1);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(2);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerId, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.writeInt(messengerId);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] messengerChat(String text, String postxt) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(6);
        mplew.writeMapleAsciiString(text);
        if (postxt.length() > 0) {
            mplew.writeMapleAsciiString(postxt);
        }

        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(7);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true, chr.isZeroSecondLook());

        return mplew.getPacket();
    }

    public static byte[] giveLoveResponse(int mode, String charname, String targetname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(10);

        mplew.write(mode);
        mplew.writeMapleAsciiString(charname);
        mplew.writeMapleAsciiString(targetname);

        return mplew.getPacket();
    }

    public static byte[] messengerPlayerInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(11);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.write(chr.getLevel());
        mplew.writeInt(chr.getJob());
        mplew.writeInt(chr.getFame());
        mplew.writeInt(chr.getLove());
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("-");
            mplew.writeMapleAsciiString("");
        } else {
            MapleGuild guild = WorldGuildService.getInstance().getGuild(chr.getGuildId());
            if (guild != null) {
                mplew.writeMapleAsciiString(guild.getName());
                mplew.writeMapleAsciiString("");
            } else {
                mplew.writeMapleAsciiString("-");
                mplew.writeMapleAsciiString("");
            }
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] messengerWhisper(String namefrom, String chatText) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(14);
        mplew.writeMapleAsciiString(namefrom);
        mplew.writeMapleAsciiString(chatText);

        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);

        return mplew.getPacket();
    }

    public static byte[] updateLove(int love) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOVE_OPERATION.getValue());
        mplew.write(0);
        mplew.writeInt(love);
        mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis()));
        mplew.writeInt(3);

        return mplew.getPacket();
    }

    public static byte[] showLoveRank(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.LOVE_OPERATION.getValue());
        mplew.write(mode);
        MessengerRankingWorker rank = MessengerRankingWorker.getInstance();
        for (int i = 0; i < 2; i++) {
            MapleCharacter player = rank.getRankingPlayer(i);
            mplew.write(player != null ? 1 : 0);
            if (player != null) {
                mplew.writeInt(player.getId());
                mplew.writeInt(player.getLove());
                mplew.writeLong(DateUtil.getFileTimestamp(rank.getLastUpdateTime(i)));
                mplew.writeMapleAsciiString(player.getName());
                PacketHelper.addCharLook(mplew, player, false, false);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] Runemessenger(int type, String mesg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.TOP_MSG_WHITE.getValue());
        mplew.writeInt(type);//字体
        mplew.writeInt(0x11);//字号
        mplew.writeLong(0);
        mplew.writeMapleAsciiString(mesg);
        return mplew.getPacket();
    }
}
