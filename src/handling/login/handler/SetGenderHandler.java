package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginWorker;
import handling.vo.recv.SetGenderRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class SetGenderHandler extends MaplePacketHandler<SetGenderRecvVO> {

    public void handlePacket(SetGenderRecvVO recvMsg, MapleClient c) {
        byte gender = recvMsg.getGender();
        String username = recvMsg.getUserName();
        if (c.getAccountName().equals(username) && c.getLoginState() == MapleClient.ENTERING_PIN) {
            c.changeGender(gender);
//            c.sendPacket(LoginPacket.genderChanged(c));
//            c.sendPacket(LoginPacket.getAuthSuccessRequest(c));
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            LoginWorker.registerClient(c);
        } else {
            c.getSession().close();
        }
    }
}
