package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DatabaseConnection;
import org.apache.log4j.Logger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterAccountHandler {
    private static final Logger log = Logger.getLogger(RegisterAccountHandler.class);

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(LoginPacket.RegisterInfo(true));
    }

    public static void CheckAccount(SeekableLittleEndianAccessor slea, MapleClient c) {
        String accountName = slea.readMapleAsciiString();
        c.getSession().write(LoginPacket.CheckAccount(accountName,c.isAccountNameUsed(accountName)));
    }

    /**
     * 注册帐号
     * @param slea
     * @param c
     */
    public static void RegisterAccount(SeekableLittleEndianAccessor slea, MapleClient c){
    /*
       * 0A
       * 0C 00 69 63 65 6C 65 6D 6F 6E 30 30 30 31 // 用户名
       * 05 00 31 31 31 31 31 // 密码
       * 04 00 31 31 31 31 // 真实姓名
       * 0A 00 32 30 30 30 2F 30 31 2F 30 31 // 生日
       * 0C 00 31 31 31 31 31 31 31 31 31 31 31 31 // 电话号码
       * 0A 00 31 31 31 31 31 31 31 31 31 31 // 问题1
       * 0A 00 33 33 33 33 33 33 33 33 33 33 // 答案1
       * 0A 00 32 32 32 32 32 32 32 32 32 32 // 问题2
       * 0A 00 34 34 34 34 34 34 34 34 34 34 //答案2
       * 0E 00 31 32 33 34 35 36 37 40 71 71 2E 63 6F 6D  电子邮箱
       * 0F 00 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 // 身份证
       * 0A 00 31 33 38 33 38 33 38 34 33 38 // 手机号码
       * 00 // 性别 0-男 1-女
       * 00 00 00 00 00
...icelemon0001..11111..1111..2000/01/01..111-11111111..1111111111..3333333333..2222222222..4444444444..1234567@qq.com..111111111111111..1383838438......
     *
     *
     */
        String accountName = slea.readMapleAsciiString();
        String password = slea.readMapleAsciiString();
        String realName = slea.readMapleAsciiString();
        String birthDay = slea.readMapleAsciiString();
        String homeNo = slea.readMapleAsciiString();
        String questionOne = slea.readMapleAsciiString();
        String answerOne = slea.readMapleAsciiString();
        String questionTwo = slea.readMapleAsciiString();
        String answerTwo = slea.readMapleAsciiString();
        String email = slea.readMapleAsciiString();
        String IDCard = slea.readMapleAsciiString();
        String telNo = slea.readMapleAsciiString();
        byte sex = slea.readByte();

        boolean result = false;
        if (!c.isAccountNameUsed(accountName)) {
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (`name`, password, birthday,email,gender) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, accountName);
                    ps.setString(2, LoginCrypto.hexSha1(password));
                    ps.setString(3, birthDay);
                    ps.setString(4, email);
                    ps.setByte(5, sex);
                    ps.executeUpdate();
                    ps.close();
                    result = true;
                }
            } catch (SQLException ex) {
                log.error("注册帐号失败！", ex);
            }
        }
        c.getSession().write(LoginPacket.RegisterAccount(result));
    }

}
