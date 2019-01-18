package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.RockPaperScissors;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ItemConstants;
import handling.SendPacketOpcode;
import handling.world.WorldBroadcastService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestScriptManager;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import server.ServerProperties;
import server.life.MapleNPC;
import server.maps.MapScriptMethods;
import server.maps.MapleQuickMove;
import server.quest.MapleQuest;
import server.shop.MapleShop;

import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.NPCPacket;

public class NPCHandler {

    public static void QuestAction(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte action = slea.readByte();
        int quest = slea.readUShort();

        if (chr == null) {
            return;
        }
        boolean 冰峰雪域的长老任务;
        switch (quest) {
            case 1430:
            case 1434:
            case 1438:
            case 1441:
            case 1444:
                冰峰雪域的长老任务 = true;
                break;
            default:
                冰峰雪域的长老任务 = false;
        }
        if (冰峰雪域的长老任务 && c.getPlayer().getQuestStatus(quest) != 1 && c.getPlayer().getMapId() != 211000001) {//冒险家3转传送
            final server.maps.MapleMap mapz = handling.channel.ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(211000001);
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }

        MapleQuest q = MapleQuest.getInstance(quest);
        switch (action) {
            case 0:
                slea.readInt();
                int itemid = slea.readInt();
                q.RestoreLostItem(chr, itemid);
                break;
            case 1:
                int npc = slea.readInt();
                if (q.hasStartScript()) {
                    break;
                }
                q.start(chr, npc);
                if (!chr.isShowPacket()) {
                    break;
                }
                chr.dropMessage(6, "开始系统任务 NPC: " + npc + " Quest：" + quest);
                break;
            case 2:
                npc = slea.readInt();

                slea.readInt();
                if (q.hasEndScript()) {
                    return;
                }
                if (slea.available() >= 4L) {
                    q.complete(chr, npc, slea.readInt());
                } else {
                    q.complete(chr, npc);
                }
                if (!chr.isShowPacket()) {
                    break;
                }
                chr.dropMessage(6, "完成系统任务 NPC: " + npc + " Quest: " + quest);
                break;
            case 3:
                if (GameConstants.canForfeit(q.getId())) {
                    q.forfeit(chr);
                    if (!chr.isShowPacket()) {
                        break;
                    }
                    chr.dropMessage(6, "放弃系统任务 Quest: " + quest);
                } else {
                    chr.dropMessage(1, "无法放弃这个任务.");
                }
                break;
            case 4:
                npc = slea.readInt();
                if (chr.hasBlockedInventory()) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }

                QuestScriptManager.getInstance().startQuest(c, npc, quest);
                if ((!chr.isAdmin()) || (!ServerProperties.ShowPacket())) {
                    break;
                }
                chr.dropMessage(6, "执行脚本任务 NPC：" + npc + " Quest: " + quest);
                break;
            case 5:
                npc = slea.readInt();
                if (chr.hasBlockedInventory()) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }

                QuestScriptManager.getInstance().endQuest(c, npc, quest, false);
//                c.sendPacket(MaplePacketCreator.showSpecialEffect(13));
//                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showSpecialEffect(chr.getId(), 13), false);
                if (!chr.isShowPacket()) {
                    break;
                }
                chr.dropMessage(6, "完成脚本任务 NPC：" + npc + " Quest: " + quest);
                break;
        }
    }
    public static void UpdateQuest(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleQuest quest = MapleQuest.getInstance(slea.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }

    public static void UseItemQuest(SeekableLittleEndianAccessor slea, MapleClient c) {
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
        int qid = slea.readInt();
        MapleQuest quest = MapleQuest.getInstance(qid);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Pair questItemInfo = null;
        boolean found = false;
        for (Item i : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
            if (i.getItemId() / 10000 == 422) {
                questItemInfo = ii.questItemInfo(i.getItemId());
                if ((questItemInfo != null) && (((Integer) questItemInfo.getLeft()) == qid) && (questItemInfo.getRight() != null) && (((List) questItemInfo.getRight()).contains(itemId))) {
                    found = true;
                    break;
                }
            }
        }
        if ((quest != null) && (found) && (item != null) && (item.getQuantity() > 0) && (item.getItemId() == itemId)) {
            int newData = slea.readInt();
            MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
            if ((stats != null) && (stats.getStatus() == 1)) {
                stats.setCustomData(String.valueOf(newData));
                c.getPlayer().updateQuest(stats, true);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short) 1, false);
            }
        }
    }

    public static void RPSGame(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((slea.available() == 0L) || (c.getPlayer() == null) || (c.getPlayer().getMap() == null) || (!c.getPlayer().getMap().containsNPC(9000019))) {
            if ((c.getPlayer() != null) && (c.getPlayer().getRPS() != null)) {
                c.getPlayer().getRPS().dispose(c);
            }
            return;
        }
        byte mode = slea.readByte();
        switch (mode) {
            case 0:
            case 5:
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().reward(c);
                }
                if (c.getPlayer().getMeso() >= 1000) {
                    c.getPlayer().setRPS(new RockPaperScissors(c, mode));
                } else {
//                    c.sendPacket(MaplePacketCreator.getRPSMode((byte) 8, -1, -1, -1));
                }
                break;
            case 1:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().answer(c, slea.readByte()))) {
                    break;
                }
//                c.sendPacket(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 2:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().timeOut(c))) {
                    break;
                }
//                c.sendPacket(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 3:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().nextRound(c))) {
                    break;
                }
//                c.sendPacket(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 4:
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
//                    c.sendPacket(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                }
        }
    }

}
