package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.MesoDropRecvVO;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MesoDropHandler extends MaplePacketHandler<MesoDropRecvVO> {


    @Override
    public void handlePacket(MesoDropRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int meso = recvVO.getMeso();
        if ((!chr.isAlive()) || (meso < 10) || (meso > 50000) || (meso > chr.getMeso())) {
            chr.getClient().sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        chr.gainMeso(-meso, false, true);
        chr.getMap().spawnMesoDrop(meso, chr.getTruePosition(), chr, chr, true, (byte) 0);
        //chr.getCheatTracker().checkDrop(true);
    }
}
