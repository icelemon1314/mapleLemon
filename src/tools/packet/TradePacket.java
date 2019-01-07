package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import handling.InteractionOpcode;
import handling.SendPacketOpcode;
import org.apache.log4j.Logger;
import server.MapleTrade;
import server.ServerProperties;
import tools.data.output.MaplePacketLittleEndianWriter;

public class TradePacket {

    private static final Logger log = Logger.getLogger(TradePacket.class);

    public static byte[] getTradeInvite(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.交易邀请.getValue());
        mplew.write(4);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.设置金币.getValue());
        mplew.write(number);
        mplew.writeLong(meso);

        return mplew.getPacket();
    }

    public static byte[] getTradeItemAdd(byte number, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.设置物品.getValue());
        mplew.write(number);
        mplew.write(item.getPosition());
        PacketHelper.addItemInfo(mplew, item);

        return mplew.getPacket();
    }

    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.房间.getValue());
        mplew.write(4);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            LoginPacket.addCharEntry(mplew, trade.getPartner().getChr());
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.writeShort(trade.getPartner().getChr().getJob());
        }
        mplew.write(number);
        LoginPacket.addCharEntry(mplew, c.getPlayer());
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.writeShort(c.getPlayer().getJob());
        mplew.write(0xFF);

        return mplew.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.确认交易.getValue());

        return mplew.getPacket();
    }

    public static byte[] TradeMessage(byte number, byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.退出.getValue());
        mplew.write(number);

        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] getTradeCancel(byte number, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(InteractionOpcode.退出.getValue());
        mplew.write(number);
        mplew.write(message == 0 ? 2 : 9);

        return mplew.getPacket();
    }
}
