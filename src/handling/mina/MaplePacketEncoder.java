package handling.mina;

import client.MapleClient;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;

import java.io.File;
import java.util.concurrent.locks.Lock;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import server.ServerProperties;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;

public final class MaplePacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

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
                    String Recv = "[服务端发送] " + op + "  [0x" + pHeaderStr + "]  (" + packetLen + "字节)  " + FileoutputUtil.CurrentReadable_Time() + "\r\n";

                    if (packetLen <= 60000) {
                        String RecvTo = Recv + HexTool.toString(input) + "\r\n" + HexTool.toStringFromAscii(input);
                        FileoutputUtil.packetLog(FileoutputUtil.PacketLog, RecvTo);
                        System.out.print(Recv);

                        if (!ServerProperties.SendPacket(op, pHeaderStr)) {

                            String SendTos = "\r\n时间：" + FileoutputUtil.CurrentReadable_Time() + "\r\n";
                            if ((op.equals("GIVE_BUFF")) || (op.equals("CANCEL_BUFF"))) {
                                FileoutputUtil.packetLog(FileoutputUtil.SkillBuff, SendTos + RecvTo);
                            } else if (op.endsWith("PLAYER_INTERACTION")) {
                                FileoutputUtil.packetLog(FileoutputUtil.玩家互动封包, SendTos + RecvTo);
                            }
                        }
                    } else {
                        FileoutputUtil.log(Recv + HexTool.toString(new byte[]{input[0], input[1]}) + "...\r\n");
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
            out.write(IoBuffer.wrap(ret));

        } else {
            out.write(IoBuffer.wrap((byte[]) message));
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {
    }

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
