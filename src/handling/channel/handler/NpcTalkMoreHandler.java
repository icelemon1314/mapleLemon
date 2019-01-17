package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import scripting.item.ItemScriptManager;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestScriptManager;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcTalkMoreHandler extends MaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
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
}
