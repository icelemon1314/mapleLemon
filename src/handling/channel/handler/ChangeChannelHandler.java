package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.vo.recv.ChangeChannelRecvVO;
import handling.world.World;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeChannelHandler extends MaplePacketHandler<ChangeChannelRecvVO> {

    @Override
    public void handlePacket(ChangeChannelRecvVO recvVO, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getEventInstance() != null) || (chr.getMap() == null) || (chr.isInBlockedMap()) || (FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()))) {
//            chr.dropMessage(5, "FieldLimitType！");
//            c.sendPacket(MaplePacketCreator.enableActions());
//            return;
//        }
            if (World.getPendingCharacterSize() >= 10) {
                chr.dropMessage(1, "服务器忙，请稍后在试。");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            int chc = recvVO.getChannelId();
            if (!World.isChannelAvailable(chc)) {
                chr.dropMessage(1, "该频道玩家已满，请切换到其它频道进行游戏。");
                c.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            chr.changeChannel(chc);
        }
    }
}
