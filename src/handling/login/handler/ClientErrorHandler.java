package handling.login.handler;

import client.MapleClient;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import handling.MaplePacketHandler;

import handling.vo.recv.CharlistRequestRecvVO;
import handling.vo.recv.ClientErrorRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;

public class ClientErrorHandler extends MaplePacketHandler<ClientErrorRecvVO> {

    @Override
    public void handlePacket(ClientErrorRecvVO recvMsg, MapleClient c) {
        String error = recvMsg.getErrorMsg();
        try {
            try (RandomAccessFile file = new RandomAccessFile("日志\\错误信息.txt", "rw")) {
                int num = (int) file.length();
                file.seek(num);
                file.writeBytes("\r\n------------------------ " + System.currentTimeMillis() + " ------------------------\r\n");
                file.write("错误信息：\r\n".getBytes());
                file.write((error + "\r\n").getBytes());
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientErrorHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
