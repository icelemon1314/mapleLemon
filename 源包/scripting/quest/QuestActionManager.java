package scripting.quest;

import client.MapleClient;
import java.awt.Point;
import javax.script.Invocable;
import scripting.ScriptType;
import scripting.npc.NPCConversationManager;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

public class QuestActionManager extends NPCConversationManager {

    private final int quest;
    private final boolean start;
    private final ScriptType type;

    public QuestActionManager(MapleClient c, int npc, int quest, boolean start, ScriptType type, Invocable iv) {
        super(c, npc, String.valueOf(quest), type, iv);
        this.quest = quest;
        this.start = start;
        this.type = type;
    }

    public int getQuest() {
        return this.quest;
    }

    public boolean isStart() {
        return this.start;
    }

    @Override
    public void dispose() {
        QuestScriptManager.getInstance().dispose(this, getClient());
    }

    public void forceStartQuest() {

        MapleQuest.getInstance(this.quest).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(String customData) {
        MapleQuest.getInstance(this.quest).forceStart(getPlayer(), getNpc(), customData);
    }

    /**
     * 完成任务了
     */
    public void forceCompleteQuest() {
            MapleQuest.getInstance(this.quest).complete(getPlayer(), getNpc());
    }

    public String getQuestCustomData() {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(this.quest)).getCustomData();
    }

    /**
     * 检查是否能够完成任务
     * @return
     */
    public boolean canCompleteQuest(){
        if (MapleQuest.getInstance(this.quest).canComplete(getPlayer())) {
           return true;
        }
        return false;
    }

    public void setQuestCustomData(String customData) {
        this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(this.quest)).setCustomData(customData);
    }

    public final void spawnNpcForPlayer(final int npcId, final int x, final int y) {
        c.getPlayer().getMap().spawnNpcForPlayer(c, npcId, new Point(x, y));
    }

    public void showCompleteQuestEffect() {
        this.c.getPlayer().getClient().getSession().write(MaplePacketCreator.showSpecialEffect(0x0E));
        this.c.getPlayer().getMap().broadcastMessage(this.c.getPlayer(), MaplePacketCreator.showSpecialEffect(this.c.getPlayer().getId(), 0x0E), false);
    }
}
