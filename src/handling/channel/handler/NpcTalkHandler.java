package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcTalkHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleNPC npc = chr.getMap().getNPCByOid(slea.readInt());
        if (npc == null) {
            return;
        }
        if (chr.hasBlockedInventory()) {
            chr.dropMessage(5, "现在不能进行操作。");
            c.sendPacket(MaplePacketCreator.enableActions());
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
}
