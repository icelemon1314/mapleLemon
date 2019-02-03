package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DatabaseConnection;
import handling.MaplePacketHandler;
import handling.vo.recv.CheckCanRegisterRecvVO;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckCanRegisterHandler extends MaplePacketHandler<CheckCanRegisterRecvVO> {

    public void handlePacket(CheckCanRegisterRecvVO recvMsg, MapleClient c) {
        c.sendPacket(LoginPacket.RegisterInfo(true));
    }
}
