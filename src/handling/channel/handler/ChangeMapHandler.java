package handling.channel.handler;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.GameConstants;
import constants.ItemConstants;
import handling.MaplePacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball.MapleSnowballs;
import server.maps.FieldLimitType;
import server.maps.MapleArrowsTurret;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.skill.冒险家.勇士;
import server.skill.冒险家.独行客;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.SkillPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static client.MapleJob.getJobName;
import static handling.channel.handler.DamageParse.NotEffectforAttack;

public class ChangeMapHandler extends MaplePacketHandler {
    /**
     * 更换地图
     * @param slea
     * @param c
     */
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 15 00 00 00 00 00 00 00 00 00 角色回城
        // 15 08 00 00 00 00 00 00 00 00 死亡后回程
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        if (slea.available() != 0L) {
            int type = slea.readByte();
            int targetid = slea.readInt();
            FileoutputUtil.log("换地图目标："+targetid);
            MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
            if ((targetid != -1) && (!chr.isAlive())) { // 角色死亡
                chr.setStance(0);
                if ((chr.getEventInstance() != null) && (chr.getEventInstance().revivePlayer(chr)) && (chr.isAlive())) {
                    FileoutputUtil.log("没有死亡但是要回程："+targetid);
                    return;
                }
                chr.getStat().setHp(50);
                MapleMap to = chr.getMap().getReturnMap();
                FileoutputUtil.log("死亡后准备回程：："+to.getId());
                chr.changeMap(to, to.getPortal(0));
            } else if (targetid != -1 && c.getPlayer().isGM()) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                MaplePortal pto = to.getPortal(0);
                chr.changeMap(to, pto);
            } else if ((targetid != -1) ) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                chr.changeMap(to, to.getPortal(0));
            } else if ((portal != null) && (!chr.hasBlockedInventory())) {
                FileoutputUtil.log("执行传送口脚本："+portal.getScriptName());
                portal.enterPortal(c);
            } else {
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        }
    }
}
