package handling.login.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginInformationProvider;
import handling.vo.recv.CheckCharNameRecvVO;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CheckCharNameHandler  extends MaplePacketHandler<CheckCharNameRecvVO> {

    public void handlePacket(CheckCharNameRecvVO recvMsg, MapleClient c) {
        String name = recvMsg.getCharName();
        c.sendPacket(LoginPacket.charNameResponse(name, (!MapleCharacterUtil.canCreateChar(name, c.isGm())) || ((LoginInformationProvider.getInstance().isForbiddenName(name)) && (!c.isGm()))));
    }
}


