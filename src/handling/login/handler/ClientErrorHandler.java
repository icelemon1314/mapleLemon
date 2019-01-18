package handling.login.handler;

import client.MapleClient;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import handling.MaplePacketHandler;

import tools.data.input.SeekableLittleEndianAccessor;

public class ClientErrorHandler extends MaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String error = slea.readMapleAsciiString();
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
