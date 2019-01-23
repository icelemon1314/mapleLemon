package tools.packet;

import handling.SendPacketOpcode;
import server.ServerProperties;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UIPacket {

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
}
