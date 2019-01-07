package handling.login.handler;

import client.MapleClient;
import handling.SendPacketOpcode;
import tools.FileoutputUtil;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class PacketErrorHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() >= 6L) {
            slea.skip(6);
            short badPacketSize = slea.readShort();
            slea.skip(4);
            int pHeader = slea.readShort();
            String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
            pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
            String op = lookupRecv(pHeader);
            String from = "\r\n------------------------ " + CurrentReadable_Time() + " ------------------------\r\n";
            if (c.getPlayer() != null) {
                from += "角色: " + c.getPlayer().getName() + " 等级(" + c.getPlayer().getLevel() + ") 职业: " + c.getPlayer().getJobName() + " (" + c.getPlayer().getJob() + ") 地图: " + c.getPlayer().getMapId() + " \r\n";
            }
            String Recv = "" + op + " [" + pHeaderStr + "] (" + (badPacketSize - 4) + ")" + slea.toString();
            FileoutputUtil.packetLog(FileoutputUtil.Packet_Error, from + Recv);
        }
    }

    private static String lookupRecv(int val) {
        for (SendPacketOpcode op : SendPacketOpcode.values()) {
            if (op.getValue(false) == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }
}
