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
import org.apache.log4j.Logger;
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
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.NPCPacket;

public class NPCHandler {

    public static void NPCAnimation(SeekableLittleEndianAccessor slea, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(SendPacketOpcode.NPC_ACTION.getValue());
        int length = (int) slea.available();
        if (length == 6) {
            mplew.writeInt(slea.readInt());
            mplew.writeShort(slea.readShort());
        } else{
            mplew.write(slea.read(length));
        }
        c.getSession().write(mplew.getPacket());
    }

    /**
     * NPC 商店操作
     * @param slea
     * @param c
     * @param chr
     */
    public static void NPCShop(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 25 00 00 00 60 4A 0F 00 01 00
        // 25 00 01 00 80 84 1E 00 01 00
        // 25 03
        byte bmode = slea.readByte();
        if (chr == null) {
            return;
        }
        switch (bmode) {
            case 0://购买
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                short position = slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                shop.buy(c, itemId, quantity, position);
                break;
            case 1://出售
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) slea.readShort();
                itemId = slea.readInt();
                quantity = slea.readShort();
                shop.sell(c, ItemConstants.getInventoryType(itemId), slot, quantity);
                break;
            case 2://充值
                shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                slot = (byte) slea.readShort();
                shop.recharge(c, slot);
                break;
            default:
                chr.setConversation(0);break;
        }
    }

    public static void NPCTalk(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleNPC npc = chr.getMap().getNPCByOid(slea.readInt());
        if (npc == null) {
            return;
        }
        if (chr.hasBlockedInventory()) {
            chr.dropMessage(5, "现在不能进行操作。");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (npc.hasShop()) {
            chr.setConversation(1);
            npc.sendShop(c);
        } else if (npc.isStorage()) {
            c.getPlayer().getStorage().sendStorage(c, npc.getId());
        } else if (npc.hasQuest(chr)) { // 检查是否有任务可以开始
            MapleQuest quest = MapleQuest.getInstance(npc.getQuestId());
            if (quest == null) {
                chr.dropMessage(0,"未知的任务："+npc.getQuestId()+"！");
            } else {
                chr.addQuest(quest);
                chr.dropMessage(0,"恭喜开始任务："+quest.getName()+"！赶紧打开任务面板查看任务信息吧！");
            }
        } else if (npc.hasCompleteQuest(chr)) {

        } else {
            chr.dropMessage(5,"当前对话NPC:"+npc.getId());
            NPCScriptManager.getInstance().start(c, npc.getId());
        }
    }

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
                    c.getSession().write(MaplePacketCreator.enableActions());
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
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }

                QuestScriptManager.getInstance().endQuest(c, npc, quest, false);
//                c.getSession().write(MaplePacketCreator.showSpecialEffect(13));
//                chr.getMap().broadcastMessage(chr, MaplePacketCreator.showSpecialEffect(chr.getId(), 13), false);
                if (!chr.isShowPacket()) {
                    break;
                }
                chr.dropMessage(6, "完成脚本任务 NPC：" + npc + " Quest: " + quest);
                break;
        }
    }

    /**
     * 仓库包处理
     * @param slea
     * @param c
     * @param chr
     */
    public static void Storage(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte mode = slea.readByte();
        if (chr == null) {
            return;
        }
        MapleStorage storage = chr.getStorage();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        switch (mode) {
            case 4://取出
                byte type = slea.readByte();
                byte slot = slea.readByte();  // 从0开始计算的
                slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
                Item item = storage.getItem(slot);
                if (item != null) {
                    if ((ii.isPickupRestricted(item.getItemId())) && (chr.getItemQuantity(item.getItemId(), true) > 0)) {
                        c.getSession().write(NPCPacket.getStorageError((byte) 9));
                        return;
                    }

                    long meso = (storage.getNpcId() == 9030100) || (storage.getNpcId() == 9031016) ? 1000 : 0;
                    if (chr.getMeso() < meso) {
                        c.getSession().write(NPCPacket.getStorageError((byte) 13));
                        return;
                    }

                    if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                        item = storage.takeOut(slot);
                        short flag = item.getFlag();
                        if (ItemFlag.KARMA_USE.check(flag)) {
                            item.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
                        }
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        if (meso > 0) {
                            chr.gainMeso(-meso, false);
                        }
                        storage.sendTakenOut(c, ItemConstants.getInventoryType(item.getItemId()));
                    } else {
                        c.getSession().write(NPCPacket.getStorageError((byte) 9));
                    }
                } else {
                    FileoutputUtil.log("[作弊] " + chr.getName() + " (等级 " + chr.getLevel() + ") 试图从仓库取出不存在的道具.");
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverMessageRedText("[GM 信息] 玩家: " + chr.getName() + " (等级 " + chr.getLevel() + ") 试图从仓库取出不存在的道具."));
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
                break;
            case 5://放入仓库
                slot = (byte) slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();

                if (quantity < 1) {
                    AutobanManager.getInstance().autoban(c, "试图存入到仓库的道具数量: " + quantity + " 道具ID: " + itemId);
                    return;
                }

                if (storage.isFull()) {
                    c.getSession().write(NPCPacket.getStorageError((byte) 9));
                    return;
                }

                MapleInventoryType type1 = ItemConstants.getInventoryType(itemId);
                if (chr.getInventory(type1).getItem((short) slot) == null) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }

                long meso = (storage.getNpcId() == 9030100) || (storage.getNpcId() == 9031016) ? 500 : 100;
                if (chr.getMeso() < meso) {
                    c.getSession().write(NPCPacket.getStorageError((byte) 13));
                    return;
                }

                item = chr.getInventory(type1).getItem((short) slot).copy();

                if (ItemConstants.isPet(item.getItemId())) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }

                if ((ii.isPickupRestricted(item.getItemId())) && (storage.findById(item.getItemId()) != null)) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if ((item.getItemId() == itemId) && ((item.getQuantity() >= quantity) || (ItemConstants.isRechargable(itemId)))) {
                    if (ItemConstants.isRechargable(itemId)) {
                        quantity = item.getQuantity();
                    }
                    chr.gainMeso(-meso, false, false);
                    MapleInventoryManipulator.removeFromSlot(c, type1, (short) slot, quantity, false);
                    item.setQuantity(quantity);
                    storage.store(item);
                    storage.sendStored(c, ItemConstants.getInventoryType(itemId));
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0L, "试图存入到仓库的道具: " + itemId + " 数量: " + quantity + " 当前玩家用道具: " + item.getItemId() + " 数量: " + item.getQuantity());
                }
                break;
            case 6:
                meso = slea.readInt();
                long storageMesos = storage.getMeso();
                long playerMesos = chr.getMeso();
                if (((meso > 0) && (storageMesos >= meso)) || ((meso < 0) && (playerMesos >= -meso))) {
                    if ((meso < 0) && (storageMesos - meso < 0)) {
                        meso = -(9999999999L - storageMesos);
                        if (-meso > playerMesos) {
                            return;
                        }
                    } else if ((meso > 0) && (playerMesos + meso < 0)) {
                        meso = 9999999999L - playerMesos;
                        if (meso > storageMesos) {
                            return;
                        }
                    }
                    storage.setMeso(storageMesos - meso);
                    chr.gainMeso(meso, false, false);
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0L, "Trying to store or take out unavailable amount of mesos (" + meso + "/" + storage.getMeso() + "/" + c.getPlayer().getMeso() + ")");
                    return;
                }
                storage.sendMeso(c);
                break;
            case 7:
                storage.close();
                chr.setConversation(0);
                break;
            default:
                FileoutputUtil.log("Unhandled Storage mode : " + mode);
        }
    }

    /**
     * NPC第二次对话
     * @param slea
     * @param c
     */
    public static void NPCMoreTalk(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            return;
        }
        final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
        byte lastMsg = slea.readByte();
        byte action = slea.readByte();

        if (player.getConversation() != 1) {
            return;
        }

        if (lastMsg == 3) { // 数字框
            int selection = -1;
            if (slea.available() >= 4L) {
                selection = slea.readInt();
            } else if (slea.available() > 0L) {
                selection = slea.readByte();
            }
            if ((!player.isShowPacket()) || ((selection >= -1) && (action != -1))) {
                if (c.getQM() != null) {
                    if (c.getQM().isStart()) {
                        QuestScriptManager.getInstance().startAction(c, action, lastMsg, selection);
                    } else {
                        QuestScriptManager.getInstance().endAction(c, action, lastMsg, selection);
                    }
                } else if (c.getIM() != null) {
                    ItemScriptManager.getInstance().action(c, action, lastMsg, selection);
                } else if (c.getCM() != null) {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                }
            } else {
                if (c.getQM() != null) {
                    c.getQM().dispose();
                }
                if (c.getIM() != null) {
                    c.getIM().dispose();
                }
                if (c.getCM() != null) {
                    c.getCM().dispose();
                }
            }
        } else {
            int selection = -1;
            if (slea.available() >= 4L) {
                selection = slea.readInt();
            } else if (slea.available() > 0L) {
                selection = slea.readByte();
            }
            if ((!player.isShowPacket()) || ((selection >= -1) && (action != -1))) {
                if (c.getQM() != null) {
                    if (c.getQM().isStart()) {
                        QuestScriptManager.getInstance().startAction(c, action, lastMsg, selection);
                    } else {
                        QuestScriptManager.getInstance().endAction(c, action, lastMsg, selection);
                    }
                } else if (c.getIM() != null) {
                    ItemScriptManager.getInstance().action(c, action, lastMsg, selection);
                } else if (c.getCM() != null) {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                }
            } else {
                if (c.getQM() != null) {
                    c.getQM().dispose();
                }
                if (c.getIM() != null) {
                    c.getIM().dispose();
                }
                if (c.getCM() != null) {
                    c.getCM().dispose();
                }
            }
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
                    c.getSession().write(MaplePacketCreator.getRPSMode((byte) 8, -1, -1, -1));
                }
                break;
            case 1:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().answer(c, slea.readByte()))) {
                    break;
                }
                c.getSession().write(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 2:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().timeOut(c))) {
                    break;
                }
                c.getSession().write(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 3:
                if ((c.getPlayer().getRPS() != null) && (c.getPlayer().getRPS().nextRound(c))) {
                    break;
                }
                c.getSession().write(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                break;
            case 4:
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
                    c.getSession().write(MaplePacketCreator.getRPSMode((byte) 13, -1, -1, -1));
                }
        }
    }

}
