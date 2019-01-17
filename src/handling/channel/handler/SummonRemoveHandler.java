package handling.channel.handler;

import client.MapleClient;
import client.SkillFactory;
import handling.MaplePacketHandler;
import server.maps.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.SummonPacket;

public class SummonRemoveHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleSummon summon = c.getPlayer().getSummons().get(slea.readInt());
        if (summon ==null || (summon.getOwnerId() != c.getPlayer().getId()) || (summon.getSkillLevel() <= 0)) {
            c.getPlayer().dropMessage(5, "移除召唤兽出现错误.");
            return;
        }
        if (c.getPlayer().isShowPacket()) {
            c.getPlayer().dropSpouseMessage(10, "收到移除召唤兽信息 - 召唤兽技能ID: " + summon.getSkillId() + " 技能名字 " + SkillFactory.getSkillName(summon.getSkillId()));
        }
        c.getPlayer().getMap().broadcastMessage(SummonPacket.removeSummon(summon, false));
        c.getPlayer().getMap().removeMapObject(summon);
        c.getPlayer().removeVisibleMapObject(summon);
        c.getPlayer().removeSummon(summon.getSkillId());
        c.getPlayer().dispelSkill(summon.getSkillId());
    }
}
