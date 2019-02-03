package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DatabaseConnection;
import handling.MaplePacketHandler;
import handling.vo.recv.CheckAccountRecvVO;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckAccountHandler extends MaplePacketHandler<CheckAccountRecvVO> {

    @Override
    public void handlePacket(CheckAccountRecvVO recvMsg, MapleClient c) {
        String accountName = recvMsg.getAccountName();
        c.sendPacket(LoginPacket.CheckAccount(accountName,c.isAccountNameUsed(accountName)));
    }
}
