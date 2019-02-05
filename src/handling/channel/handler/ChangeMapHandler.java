package handling.channel.handler;

import client.*;
import handling.MaplePacketHandler;
import handling.channel.ChannelServer;
import handling.vo.recv.ChangeMapRecvVO;
import server.MaplePortal;
import server.maps.MapleMap;

import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapHandler extends MaplePacketHandler<ChangeMapRecvVO> {
    /**
     * 更换地图
     * @param recvMsg
     * @param c
     */
    @Override
    public void handlePacket(ChangeMapRecvVO recvMsg, MapleClient c) {
        // 15 00 00 00 00 00 00 00 00 00 角色回城
        // 15 08 00 00 00 00 00 00 00 00 死亡后回程
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int type = recvMsg.getType();
        int targetid = recvMsg.getTargetId();
        MapleLogger.info("换地图目标："+targetid);
        MaplePortal portal = chr.getMap().getPortal(recvMsg.getPortalName());
        if ((targetid != -1) && (!chr.isAlive())) { // 角色死亡
            chr.setStance(0);
            if ((chr.getEventInstance() != null) && (chr.getEventInstance().revivePlayer(chr)) && (chr.isAlive())) {
                MapleLogger.info("没有死亡但是要回程："+targetid);
                return;
            }
            chr.getStat().setHp(50);
            MapleMap to = chr.getMap().getReturnMap();
            MapleLogger.info("死亡后准备回程：："+to.getId());
            chr.changeMap(to, to.getPortal(0));
        } else if (targetid != -1 && c.getPlayer().isGM()) {
            MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
            MaplePortal pto = to.getPortal(0);
            chr.changeMap(to, pto);
        } else if ((targetid != -1) ) {
            MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
            chr.changeMap(to, to.getPortal(0));
        } else if ((portal != null) && (!chr.hasBlockedInventory())) {
            MapleLogger.info("执行传送口脚本："+portal.getScriptName());
            portal.enterPortal(c);
        } else {
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
}
