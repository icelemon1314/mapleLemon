package handling.login.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class ShowAccCash {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int accId = slea.readInt();
        if (c.getAccID() == accId) {
            if (c.getPlayer() != null) {
                c.sendPacket(MaplePacketCreator.showPlayerCash(c.getPlayer()));
            } else {
                Pair cashInfo = MapleCharacterUtil.getCashByAccId(accId);
                if (cashInfo == null) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
//                c.sendPacket(LoginPacket.ShowAccCash(((Integer) cashInfo.getLeft()), ((Integer) cashInfo.getRight())));
            }
        }
    }
}
