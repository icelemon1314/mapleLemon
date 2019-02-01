package handling.mina;

import client.MapleClient;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;

import java.io.File;
import java.util.concurrent.locks.Lock;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.ServerProperties;

import tools.DateUtil;
import tools.HexTool;
import tools.MapleLogger;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;

public class MaplePacketEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext session, Object message, ByteBuf out) throws Exception {
        MapleClient client =  session.channel().attr(MapleClient.CLIENT_KEY).get();

        if (client != null) {
            byte[] input = (byte[]) message;

            if (ServerProperties.ShowPacket()) {
                int packetLen = input.length;
                int pHeader = readFirstByte(input);
                boolean 记录 = true;
                for (final SendPacketOpcode recv : SendPacketOpcode.values()) {
                    if (recv.getValue() == pHeader) {
                        if (SendPacketOpcode.isSpamHeader(recv)) {//暂时记录怪物和角色移动
                            记录 = false;
                        }
                        break;
                    }
                }
                if (记录 ) {
                    String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                    pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
                    String op = lookupRecv(pHeader);
                    String Recv = "[服务端发送] " + op + "  [0x" + pHeaderStr + "]  (" + packetLen + "字节)  " + DateUtil.getNowTime() + "\r\n";

                    if (packetLen <= 60000) {
                        String RecvTo = Recv + HexTool.toString(input) + "\r\n" + HexTool.toStringFromAscii(input);
                        System.out.print(Recv);

                        if (!ServerProperties.SendPacket(op, pHeaderStr)) {
                            String SendTos = "\r\n时间：" + System.currentTimeMillis() + "\r\n";
                        }
                    } else {
                        MapleLogger.info(Recv + HexTool.toString(new byte[]{input[0], input[1]}) + "...\r\n");
                    }
                }
            }

            byte[] unencrypted = new byte[input.length];
            System.arraycopy(input, 0, unencrypted, 0, input.length);
            byte[] ret = new byte[unencrypted.length + 4];
            Lock mutex = client.getLock();
            mutex.lock();
            try {
                byte[] header = client.getSendCrypto().getPacketHeader(unencrypted.length);
                client.getSendCrypto().crypt(unencrypted);
                System.arraycopy(header, 0, ret, 0, 4);
            } finally {
                mutex.unlock();
            }
            System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
            out.writeBytes(ret);

        } else {
            out.writeBytes((byte[]) message);
        }
    }

//    @Override
//    public void dispose(IoSession session) throws Exception {
//    }

    private String lookupRecv(int val) {
        for (SendPacketOpcode op : SendPacketOpcode.values()) {
            if (op.getValue(false) == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private int readFirstByte(byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readByte();
    }
}
