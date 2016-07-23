package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import java.util.Calendar;
import tools.DateUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class LoginPasswordHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 01 0C 00 69 63 65 6C 65 6D 6F 6E 31 33 31 34
        // 05 00 61 64 6D 69 6E
        // 00 00 24 7E DD AA F0 7E D6 03 00 00 00 00 00 53 9C 00 00 00 00
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();


        int loginok = c.login(login, pwd);
        Calendar tempbannedTill = c.getTempBanCalendar();
        //int 登录器判定 = LandersClient.Landerslogin(c.getSession().getRemoteAddress().toString());
        /*登录操作码
         0 - 成功登录
         1 - 刷新
         2 - 封号
         3 - 屏蔽了账号登录功能或者已经被删除、终止的账号
         4 - 屏蔽了静态密码或密码输入错误
         5 - 未登录的账号
         6 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         7 - 正在登录中的账号
         8 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         9 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         10 - 目前因链接邀请过多 服务器未能处理。
         */
        if (tempbannedTill != null && tempbannedTill.getTimeInMillis() > System.currentTimeMillis()) {
            //限时封号
            long tempban = DateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis());
            c.getSession().write(LoginPacket.getTempBan(tempban, c.getBanReason()));
        } else if (loginok == 3) {
            //永久封号
            c.getSession().write(LoginPacket.getTempBan(833438715));
        } else if (loginok != 0) {
            //登录不成功
            c.getSession().write(LoginPacket.getLoginFailed(loginok));
        } else if (c.getGender() == 10) {
            //选择性别
            c.updateLoginState(MapleClient.ENTERING_PIN);
            c.getSession().write(LoginPacket.genderNeeded(c));
            return;
        } else if (LoginServer.isCheckMacs()) {
            c.getSession().write(MaplePacketCreator.serverMessageNotice("登陆标识符不正确。"));
            c.getSession().write(LoginPacket.getLoginFailed(16));
        } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
            return;
        }

        //清理连接信息
        c.clearInformation();
    }
}
