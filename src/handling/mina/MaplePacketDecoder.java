package handling.mina;

import client.MapleClient;
import handling.RecvPacketOpcode;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import server.ServerProperties;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;

public final class MaplePacketDecoder extends CumulativeProtocolDecoder {

    public static final String DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        MapleClient client = (MapleClient) session.getAttribute("CLIENT");
        if (decoderState.packetlength == -1) {
            if (in.remaining() >= 4) {
                int packetHeader = in.getInt(); // 另外一种方式，getShort() xor getShort()
                decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
            } else {
                FileoutputUtil.log("没有足够的数据来解密封包.");
                return false;
            }
        }
        if (in.remaining() >= decoderState.packetlength) {
            byte[] decryptedPacket = new byte[decoderState.packetlength];
            in.get(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            client.getReceiveCrypto().crypt(decryptedPacket);
            out.write(decryptedPacket);

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
                    return true;
                }
                String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
                String op = lookupSend(pHeader);
                String Send = "[客户端发送] " + op + "  [0x" + pHeaderStr + "]  (" + packetLen + "字节)  " + FileoutputUtil.CurrentReadable_Time() + "\r\n";
                if (packetLen <= 6000) {
                    String SendTo = Send + HexTool.toString(decryptedPacket) + "\r\n" + HexTool.toStringFromAscii(decryptedPacket);
                    if (!ServerProperties.RecvPacket(op, pHeaderStr)) {
                        String SendTos = "\r\n时间：" + FileoutputUtil.CurrentReadable_Time() + "  ";

                        FileoutputUtil.packetLog(FileoutputUtil.PacketLog, SendTo);
                        System.out.print(Send);
                        if ((op.equals("CLOSE_RANGE_ATTACK")) || (op.equals("RANGED_ATTACK")) || (op.equals("SUMMON_ATTACK")) || (op.equals("MAGIC_ATTACK"))) {
                            FileoutputUtil.packetLog(FileoutputUtil.AttackLog, SendTos + SendTo);
                        } else if (op.endsWith("PLAYER_INTERACTION")) {
                            FileoutputUtil.packetLog(FileoutputUtil.玩家互动封包, SendTos + SendTo);
                        } else if (op.equals("UNKNOWN")) {
                            FileoutputUtil.packetLog(FileoutputUtil.Packet_Unk, SendTos + SendTo);
                        }
                    }
                } else {
                    FileoutputUtil.log(Send + HexTool.toString(new byte[]{decryptedPacket[0], decryptedPacket[1]}) + "...\r\n");
                }
            }
            return true;
        }
        return false;
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
