package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DaoFactory;
import database.DatabaseConnection;
import database.dao.AccountsDao;
import database.entity.AccountsPO;
import handling.MaplePacketHandler;
import handling.vo.recv.RegisterAccountRecvVO;
import tools.DateUtil;
import tools.MapleLogger;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class RegisterAccountHandler extends MaplePacketHandler<RegisterAccountRecvVO> {

    /**
     * 注册帐号
     * @param recvMsg
     * @param c
     */
    @Override
    public void handlePacket(RegisterAccountRecvVO recvMsg, MapleClient c){
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
        String accountName = recvMsg.getAccountName();
        String password = recvMsg.getPassword();
        String realName = recvMsg.getRealName();
        String birthDay = recvMsg.getBirthDay();
        String homeNo = recvMsg.getHomeNo();
        String questionOne = recvMsg.getQuestionOne();
        String answerOne = recvMsg.getAnswerOne();
        String questionTwo = recvMsg.getQuestionTwo();
        String answerTwo = recvMsg.getAnswerTwo();
        String email = recvMsg.getEmail();
        String IDCard = recvMsg.getIDCard();
        String telNo = recvMsg.getTelNo();
        byte sex = recvMsg.getSex();

        if (!c.isAccountNameUsed(accountName)) {

            Calendar birth = DateUtil.formatFromString(birthDay);
            if (birth == null) {
                c.sendPacket(LoginPacket.RegisterAccount(false));
                return ;
            }

            AccountsDao accDao = DaoFactory.getInstance().createDao(AccountsDao.class);
            AccountsPO acc = new AccountsPO();

            acc.setName(accountName);
            acc.setPassword(LoginCrypto.hexSha1(password));
            acc.setBirthday(birth);
            acc.setEmail(email);
            acc.setGender(sex);

            accDao.save(acc);
        }
        c.sendPacket(LoginPacket.RegisterAccount(true));
    }

}
