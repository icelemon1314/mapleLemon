package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.MapleSendPacketFactory;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import java.util.Calendar;

import handling.vo.MaplePacketRecvVO;
import handling.vo.recv.LoginPasswordRecvVO;
import handling.vo.send.LoginStatusSendVO;
import tools.DateUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class LoginPasswordHandler extends MaplePacketHandler<LoginPasswordRecvVO> {

    @Override
    public void handlePacket(LoginPasswordRecvVO recvVO, MapleClient c) {
        // 01 0C 00 69 63 65 6C 65 6D 6F 6E 31 33 31 34
        // 05 00 61 64 6D 69 6E
        // 00 00 24 7E DD AA F0 7E D6 03 00 00 00 00 00 53 9C 00 00 00 00

        String login = recvVO.getUsername();
        String pwd = recvVO.getPassowrd();

        int loginok = c.login(login, pwd);
        Calendar tempbannedTill = c.getTempBanCalendar();
        if (tempbannedTill != null && tempbannedTill.getTimeInMillis() > System.currentTimeMillis()) {
            //限时封号
            long tempban = DateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis());
            c.sendPacket(LoginPacket.getTempBan(tempban));
        } else if (loginok == LoginStatusSendVO.LOGIN_STATE_BANNED) {
            //永久封号
            c.sendPacket(LoginPacket.getTempBan(833438715));
        } else if (loginok != LoginStatusSendVO.LOGIN_STATE_OK) {
            //登录不成功
            c.sendPacket(LoginPacket.getLoginFailed(loginok));
        } else if (c.getGender() == 10) {
            //选择性别
            c.updateLoginState(MapleClient.ENTERING_PIN);
            c.sendPacket(LoginPacket.genderNeeded(c));
            return;
        } else if (LoginServer.isCheckMacs()) {
            c.sendPacket(MaplePacketCreator.serverMessageNotice("登陆标识符不正确。"));
            c.sendPacket(LoginPacket.getLoginFailed(16));
        } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
            return;
        }

        //清理连接信息
        c.clearInformation();
    }
}
