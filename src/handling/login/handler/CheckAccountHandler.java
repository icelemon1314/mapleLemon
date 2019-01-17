package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DatabaseConnection;
import handling.MaplePacketHandler;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckAccountHandler extends MaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String accountName = slea.readMapleAsciiString();
        c.sendPacket(LoginPacket.CheckAccount(accountName,c.isAccountNameUsed(accountName)));
    }
}
