package scripting.npc;

import client.MapleClient;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import scripting.AbstractScriptManager;
import scripting.ScriptType;
import tools.FileoutputUtil;

public class NPCScriptManager extends AbstractScriptManager {

    private static final NPCScriptManager instance = new NPCScriptManager();
    private Map<MapleClient, NPCConversationManager> cms = new WeakHashMap();

    public static final NPCScriptManager getInstance() {
        return instance;
    }

    public void start(MapleClient c, int npcId) {
        start(c, npcId, null);
    }

    public void start(MapleClient c, int npcId, String npcMode) {
        try {
            if (c.getPlayer().isAdmin()) {
                c.getPlayer().dropMessage(5, "对话NPC：" + npcId + " 模式：" + npcMode);
            }
            if (this.cms.containsKey(c)) {
                dispose(c);
                return;
            }
            Invocable iv;
            if (npcMode == null) {
                iv = getInvocable("npc/" + npcId + ".js", c, true);
            } else {
                iv = getInvocable("特殊/" + npcMode + ".js", c, true);
            }
            ScriptEngine scriptengine = (ScriptEngine) iv;
            NPCConversationManager cm = new NPCConversationManager(c, npcId, npcMode, ScriptType.NPC, iv);
            if ((iv == null) || (getInstance() == null)) {
                if (iv == null) {
                    c.getPlayer().dropMessage(5,"找不到NPC脚本(ID:" + npcId + "), 特殊模式(" + npcMode + "),所在地图(ID:" + c.getPlayer().getMapId() + ")");
                }
                dispose(c);
                return;
            }
            this.cms.put(c, cm);
            scriptengine.put("cm", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            try {
                iv.invokeFunction("start", new Object[0]);
            } catch (NoSuchMethodException nsme) {
                iv.invokeFunction("action", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
            }
        } catch (NoSuchMethodException | ScriptException e) {
            System.err.println("NPC脚本出错（ID : " + npcId + "）模式：" + npcMode + "错误内容: " + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "NPC脚本出错（ID : " + npcId + "）模式" + npcMode + ".\r\n错误信息：" + e);
            dispose(c);
            notice(c, npcId, npcMode);
        }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) {
        if (mode != -1) {
            NPCConversationManager cm = this.cms.get(c);
            if (cm == null) {
                return;
            }
            try {
                if (cm.pendingDisposal) {
                    dispose(c);
                } else {
                    c.setClickedNPC();
                    cm.getIv().invokeFunction("action", new Object[]{mode, type, selection});
                }
            } catch (NoSuchMethodException | ScriptException e) {
                int npcId = cm.getNpc();
                String npcMode = cm.getScript();
                System.err.println("NPC脚本出错（ID : " + npcId + "）模式：" + npcMode + "  错误内容：" + e);
                FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "NPC脚本出错（ID : " + npcId + "）模式：" + npcMode + ". \r\n错误信息：" + e);
                dispose(c);
                notice(c, npcId, npcMode);
            }
        }
    }

    public void dispose(MapleClient c) {
        final NPCConversationManager npccm = cms.get(c);
        if (npccm != null) {
            cms.remove(c);
            if (npccm.getType() == ScriptType.NPC) {
                if (npccm.getScript() == null) {
                    c.removeScriptEngine("scripts/npc/" + npccm.getNpc() + ".js");
                } else {
                    c.removeScriptEngine("scripts/特殊/" + npccm.getScript() + ".js");
                }
            } else if (npccm.getType() == ScriptType.ON_USER_ENTER) {
                c.removeScriptEngine("scripts/地图/onUserEnter/" + npccm.getScript() + ".js");
            } else if (npccm.getType() == ScriptType.ON_FIRST_USER_ENTER) {
                c.removeScriptEngine("scripts/地图/onFirstUserEnter/" + npccm.getScript() + ".js");
            }
        }
        if (c.getPlayer() != null && c.getPlayer().getConversation() == 1) {
            c.getPlayer().setConversation(0);
        }
    }

    public void dispose(NPCConversationManager cm) {
        if (cm == null) {
            return;
        }
        MapleClient c = cm.getClient();
        final NPCConversationManager npccm = cms.get(c);
        if (npccm != null) {
            cms.remove(c);
            if (npccm.getType() == ScriptType.NPC) {
                if (cm.getScript() == null) {
                    c.removeScriptEngine("scripts/npc/" + cm.getNpc() + ".js");
                } else {
                    c.removeScriptEngine("scripts/特殊/" + cm.getScript() + ".js");
                }
            } else if (npccm.getType() == ScriptType.ON_USER_ENTER) {
                c.removeScriptEngine("scripts/地图/onUserEnter/" + npccm.getScript() + ".js");
            } else if (npccm.getType() == ScriptType.ON_FIRST_USER_ENTER) {
                c.removeScriptEngine("scripts/地图/onFirstUserEnter/" + npccm.getScript() + ".js");
            }
        }
        if (c.getPlayer() != null && c.getPlayer().getConversation() == 1) {
            c.getPlayer().setConversation(0);
        }
    }

    public NPCConversationManager getCM(MapleClient c) {
        return (NPCConversationManager) this.cms.get(c);
    }

    private void notice(MapleClient c, int npcId, String npcMode) {
        c.getPlayer().dropMessage(1, "脚本出错. NPCID: " + npcId + (npcMode != null ? "  模式:" + npcMode : "") + " \n\r当前地图：" + c.getPlayer().getMap().getMapName() + "(" + c.getPlayer().getMapId() + ")");
    }

    public final void onUserEnter(final MapleClient c, final String script) {
        try {
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "开始地图onUserEnter脚本：" + script + c.getPlayer().getMap().getMapName());
            }
            Invocable iv = getInvocable("地图/onUserEnter/" + script + ".js", c, true);
            ScriptEngine scriptengine = (ScriptEngine) iv;
            NPCConversationManager cm = new NPCConversationManager(c, 0, script, ScriptType.ON_USER_ENTER, iv);
            if (this.cms.containsValue(cm)) {
                FileoutputUtil.log("无法执行脚本:已有脚本執行-" + cms.containsKey(c) + "脚本名称：" + script + c.getPlayer().getMap().getMapName());
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "无法执行脚本:已有脚本執行-" + cms.containsKey(c));
                }
                dispose(c);
                return;
            }
            if ((iv == null) || (getInstance() == null)) {
                if (iv == null) {
                    FileoutputUtil.log("找不到onUserEnter脚本 :(" + script + "),所在地图(ID:" + c.getPlayer().getMapId() + ")");
                }
                dispose(c);
                return;
            }
            this.cms.put(c, cm);
            scriptengine.put("ms", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            try {
                iv.invokeFunction("start", new Object[0]);
                FileoutputUtil.log("开始执行onUserEnter脚本 :(" + script + "), 所在地图(ID:" + c.getPlayer().getMapId() + ")");
            } catch (NoSuchMethodException nsme) {
                iv.invokeFunction("action", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
            }
        } catch (NoSuchMethodException | ScriptException e) {
            System.err.println("执行地图onUserEnter脚本出錯 : " + script + ". 错误内容" + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "执行地图onUserEnter脚本出錯 : " + script + ".\r\n错误信息：" + e);
            dispose(c);
        }
    }

    public final void onFirstUserEnter(final MapleClient c, final String script) {
        try {
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "开始地图onFirstUserEnter脚本：" + script + c.getPlayer().getMap().getMapName());
            }
            if (this.cms.containsKey(c)) {
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "无法执行脚本:已有脚本執行-" + cms.containsKey(c));
                }
                dispose(c);
                return;
            }
            Invocable iv = getInvocable("地图/onFirstUserEnter/" + script + ".js", c, true);
            ScriptEngine scriptengine = (ScriptEngine) iv;
            NPCConversationManager cm = new NPCConversationManager(c, 0, script, ScriptType.ON_FIRST_USER_ENTER, iv);
            if ((iv == null) || (getInstance() == null)) {
                if (iv == null) {
                    FileoutputUtil.log("找不到onFirstUserEnter脚本 :" + script + ",所在地图(ID:" + c.getPlayer().getMapId() + ")");
                }
                dispose(c);
                return;
            }
            this.cms.put(c, cm);
            scriptengine.put("ms", cm);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            try {
                iv.invokeFunction("start", new Object[0]);
                FileoutputUtil.log("开始执行onFirstUserEnter脚本 :(" + script + "), 所在地图(ID:" + c.getPlayer().getMapId() + ")");
            } catch (NoSuchMethodException nsme) {
                iv.invokeFunction("action", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
            }
        } catch (NoSuchMethodException | ScriptException e) {
            System.err.println("执行地图onFirstUserEnter脚本出錯 : " + script + ". 错误内容" + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "执行地图onFirstUserEnter脚本出錯 : " + script + ".\r\n错误信息：" + e);
            dispose(c);
        }
    }
}
