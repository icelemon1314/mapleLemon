package scripting.quest;

import client.MapleClient;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import scripting.AbstractScriptManager;
import scripting.ScriptType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;

public class QuestScriptManager extends AbstractScriptManager {

    private static final QuestScriptManager instance = new QuestScriptManager();
    private final Map<MapleClient, QuestActionManager> qms = new WeakHashMap();

    public static final QuestScriptManager getInstance() {
        return instance;
    }

    public void startQuest(MapleClient c, int npcId, int questId) {
        try {
            if (this.qms.containsKey(c)) {
                FileoutputUtil.log("脚本任务挂了！！！！");
                dispose(c);
                return;
            }
            Invocable iv = getInvocable("quests/" + questId + ".js", c, true);
            FileoutputUtil.log("读取脚本任务完成！！！");
            if (iv == null) {
                //c.getPlayer().forceCompleteQuest(questId);
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "开始任务脚本不存在 NPC：" + npcId + " Quest：" + questId);
                }
                dispose(c);
                FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "开始任务脚本不存在 NPC：" + npcId + " Quest：" + questId);
                return;
            }
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "开始脚本任务 NPC：" + npcId + " Quest：" + questId);
            }
            ScriptEngine scriptengine = (ScriptEngine) iv;
            QuestActionManager qm = new QuestActionManager(c, npcId, questId, true, ScriptType.QUEST_START, iv);
            this.qms.put(c, qm);
            scriptengine.put("qm", qm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            iv.invokeFunction("start", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
        } catch (ScriptException | NoSuchMethodException e) {
            System.err.println("执行任务脚本失败 任务ID: (" + questId + ")..NPCID: " + npcId + ":" + e);
            FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "执行任务脚本失败 任务ID: (" + questId + ")..NPCID: " + npcId + ". \r\n错误信息: " + e);
            dispose(c);
            notice(c, questId);
        }
    }

    public void startAction(MapleClient c, byte mode, byte type, int selection) {
        QuestActionManager qm = (QuestActionManager) this.qms.get(c);
        if (qm == null) {
            return;
        }
        try {
            if (qm.pendingDisposal) {
                dispose(c);
            } else {
                c.setClickedNPC();
                qm.getIv().invokeFunction("start", new Object[]{mode, type, selection});
            }
        } catch (ScriptException | NoSuchMethodException e) {
            int npcId = qm.getNpc();
            int questId = qm.getQuest();
            System.err.println("执行任务脚本失败 任务ID: (" + questId + ")...NPC: " + npcId + ":" + e);
            FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "执行任务脚本失败 任务ID: (" + questId + ")..NPCID: " + npcId + ". \r\n错误信息: " + e);
            dispose(c);
            notice(c, questId);
        }
    }

    public void endQuest(MapleClient c, int npcId, int questId, boolean customEnd) {
        if ((!customEnd) && (!MapleQuest.getInstance(questId).canComplete(c.getPlayer()))) {
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(6, "不能完成这个任务 NPC：" + npcId + " Quest：" + questId);
            }
            return;
        }
        try {
            if ((!this.qms.containsKey(c)) && (c.canClickNPC())) {
                Invocable iv = getInvocable("任务/" + questId + ".js", c, true);
                if (iv == null) {
                    //c.getPlayer().forceCompleteQuest(questId);
//                    if (c.getPlayer().isAdmin()) {
//                        c.getPlayer().dropMessage(5, "完成任务脚本不存在 NPC：" + npcId + " Quest：" + questId);
//                    }
                    dispose(c);
                    FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "完成任务脚本不存在 NPC：" + npcId + " Quest：" + questId);
                    return;
                }
//                if (c.getPlayer().isAdmin()) {
//                    c.getPlayer().dropMessage(5, "完成脚本任务 NPC：" + npcId + " Quest：" + questId);
//                }
                ScriptEngine scriptengine = (ScriptEngine) iv;
                QuestActionManager qm = new QuestActionManager(c, npcId, questId, false, ScriptType.QUEST_END, iv);
                this.qms.put(c, qm);
                scriptengine.put("qm", qm);
                c.getPlayer().setConversation(1);
                c.setClickedNPC();
                iv.invokeFunction("end", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
            } else {
                dispose(c);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            System.err.println("执行任务脚本失败 任务ID: (" + questId + ")..NPCID: " + npcId + ":" + e);
            FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "执行任务脚本失败 任务ID: (" + questId + ")..NPCID: " + npcId + ". \r\n错误信息: " + e);
            dispose(c);
            notice(c, questId);
        }
    }

    public void endAction(MapleClient c, byte mode, byte type, int selection) {
        QuestActionManager qm = (QuestActionManager) this.qms.get(c);
        if (qm == null) {
            return;
        }
        try {
            if (qm.pendingDisposal) {
                dispose(c);
            } else {
                c.setClickedNPC();
                qm.getIv().invokeFunction("end", new Object[]{mode, type, selection});
            }
        } catch (ScriptException | NoSuchMethodException e) {
            int npcId = qm.getNpc();
            int questId = qm.getQuest();
            System.err.println("完成任务脚本失败 任务ID (" + questId + ")...NPC: " + npcId + ":" + e);
            FileoutputUtil.log(FileoutputUtil.Quest_ScriptEx_Log, "完成任务脚本失败 任务ID (" + questId + ")..NPCID: " + npcId + ". \r\n错误信息: " + e);
            dispose(c);
            notice(c, questId);
        }
    }

    public void dispose(MapleClient c) {
        QuestActionManager qm = (QuestActionManager) this.qms.get(c);
        if (qm != null) {
            this.qms.remove(c);
            c.removeScriptEngine("scripts/任务/" + qm.getQuest() + ".js");
        }
        if ((c.getPlayer() != null) && (c.getPlayer().getConversation() == 1)) {
            c.getPlayer().setConversation(0);
        }
    }

    public void dispose(QuestActionManager qm, MapleClient c) {
        if (qm != null) {
            this.qms.remove(c);
            c.removeScriptEngine("scripts/任务/" + qm.getQuest() + ".js");
        }
        if ((c.getPlayer() != null) && (c.getPlayer().getConversation() == 1)) {
            c.getPlayer().setConversation(0);
        }
    }

    public QuestActionManager getQM(MapleClient c) {
        return (QuestActionManager) this.qms.get(c);
    }

    private void notice(MapleClient c, int questId) {
        c.getPlayer().dropMessage(1, "这个任务脚本是错误的，请联系管理员修复它.任务ID: " + questId);
    }
}
