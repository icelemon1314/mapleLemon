package handling.mina;

import client.MapleClient;
import handling.RecvPacketOpcode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import server.ServerProperties;

import tools.*;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;

import java.util.List;

public class MaplePacketDecoder extends ByteToMessageDecoder {

    @SuppressWarnings("deprecation")
    public static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.valueOf(MaplePacketDecoder.class.getName() + ".STATE");

    @Override
    protected void decode(ChannelHandlerContext session, ByteBuf in, List<Object> out) throws Exception {
        DecoderState decoderState =  session.channel().attr(DECODER_STATE_KEY).get();
        MapleClient client = session.channel().attr(MapleClient.CLIENT_KEY).get();
        if (decoderState.packetlength == -1) {
            if (in.readableBytes() >= 4) {
                int packetHeader = in.readInt(); // 另外一种方式，getShort() xor getShort()
                decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
            } else {
                MapleLogger.info("没有足够的数据来解密封包.");
                return ;
            }
        }
        if (in.readableBytes() >= decoderState.packetlength) {
            byte[] decryptedPacket = new byte[decoderState.packetlength];
            in.readBytes(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            client.getReceiveCrypto().crypt(decryptedPacket);
            out.add(decryptedPacket);

            if (ServerProperties.ShowPacket()) {
                int packetLen = decryptedPacket.length;
                int pHeader = readFirstByte(decryptedPacket);
                boolean 记录 = true;
                for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                    if (recv.getValue() == pHeader) {
                        if ( !ServerProperties.ShowPacket() ? RecvPacketOpcode.isTempHeader(recv) : RecvPacketOpcode.isSpamHeader(recv)) {//暂时记录怪物和角色移动
                            记录 = false;
                        }
                        break;
                    }
                }
                if (!记录) {
                    return ;
                }
                String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
                String op = lookupSend(pHeader);
                String Send = "[客户端发送] " + op + "  [0x" + pHeaderStr + "]  (" + packetLen + "字节)  " + DateUtil.getNowTime() + "\r\n";
                if (packetLen <= 6000) {
//                    String SendTo = Send + HexTool.toString(decryptedPacket) + "\r\n" + HexTool.toStringFromAscii(decryptedPacket);
                    if (!ServerProperties.RecvPacket(op, pHeaderStr)) {
                        MapleLogger.info(Send);
                    }
                } else {
                    MapleLogger.info(Send + HexTool.toString(new byte[]{decryptedPacket[0], decryptedPacket[1]}) + "...\r\n");
                }
            }
            return ;
        }
        return ;
    }

    private String lookupSend(int val) {
        for (RecvPacketOpcode op : RecvPacketOpcode.values()) {
            if (op.getValue() == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private int readFirstByte(byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readByte();
    }

    public static class DecoderState {

        public int packetlength = -1;
    }
}
