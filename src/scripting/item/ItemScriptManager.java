package scripting.item;

import client.MapleClient;
import client.inventory.Item;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import scripting.AbstractScriptManager;
import tools.FileoutputUtil;

public class ItemScriptManager extends AbstractScriptManager {

    private static final ItemScriptManager instance = new ItemScriptManager();
    private final Map<MapleClient, ItemActionManager> ims = new WeakHashMap();

    public static final ItemScriptManager getInstance() {
        return instance;
    }

    public void start(MapleClient c, int npc, Item item) {
        try {
            if (this.ims.containsKey(c)) {
                dispose(c);
                return;
            }
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "开始道具脚本 NPC：" + npc + " 道具Id：" + item.getItemId());
            }
            Invocable iv = getInvocable("道具/" + item.getItemId() + ".js", c, true);
            ScriptEngine scriptengine = (ScriptEngine) iv;
            ItemActionManager im = new ItemActionManager(c, npc, item, iv);
            if (iv == null) {
                im.sendOk("对不起，我并没有被管理员设置可使用，如果您觉得我应该工作的，那就请您汇报给管理员。\r\n我的信息: #b#i" + item.getItemId() + ":##z" + item.getItemId() + "##k ID: " + item.getItemId());
                dispose(c);
                return;
            }
            this.ims.put(c, im);
            scriptengine.put("im", im);
            c.getPlayer().setConversation(1);
            c.setClickedNPC();
            try {
                iv.invokeFunction("start", new Object[0]);
            } catch (NoSuchMethodException nsme) {
                iv.invokeFunction("action", new Object[]{(byte) 1, (byte) 0, (int) (byte) 0});
            }
        } catch (ScriptException | NoSuchMethodException e) {
            System.err.println("执行道具脚本失败 道具ID: (" + item.getItemId() + ")..NPCID: " + npc + ":" + e);
            FileoutputUtil.log(FileoutputUtil.Item_ScriptEx_Log, "执行道具脚本失败 道具ID: (" + item.getItemId() + ")..NPCID: " + npc + ". \r\n错误信息: " + e);
            dispose(c);
            notice(c, item.getItemId());
        }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) {
        if (mode != -1) {
            ItemActionManager im = (ItemActionManager) this.ims.get(c);
            if (im == null) {
                return;
            }
            try {
                if (im.pendingDisposal) {
                    dispose(c);
                } else {
                    c.setClickedNPC();
                    im.getIv().invokeFunction("action", new Object[]{mode, type, selection});
                }
            } catch (ScriptException | NoSuchMethodException e) {
                int npcId = im.getNpc();
                int itemId = im.getItemId();
                System.err.println("执行NPC脚本出错 NPC ID : " + npcId + " 道具ID: " + itemId + " 错误信息: " + e);
                FileoutputUtil.log(FileoutputUtil.Item_ScriptEx_Log, "执行NPC脚本出错 NPC ID : " + npcId + " 道具ID: " + itemId + ". \r\n错误信息: " + e);
                dispose(c);
                notice(c, itemId);
            }
        }
    }

    public void dispose(MapleClient c) {
        ItemActionManager im = (ItemActionManager) this.ims.get(c);
        if (im != null) {
            this.ims.remove(c);
            c.removeScriptEngine("scripts/道具/" + im.getItemId() + ".js");
        }
        if ((c.getPlayer() != null) && (c.getPlayer().getConversation() == 1)) {
            c.getPlayer().setConversation(0);
        }
    }

    public void dispose(ItemActionManager im, MapleClient c) {
        if (im != null) {
            this.ims.remove(c);
            c.removeScriptEngine("scripts/道具/" + im.getItemId() + ".js");
        }
        if ((c.getPlayer() != null) && (c.getPlayer().getConversation() == 1)) {
            c.getPlayer().setConversation(0);
        }
    }

    public ItemActionManager getIM(MapleClient c) {
        return (ItemActionManager) this.ims.get(c);
    }

    private void notice(MapleClient c, int itemId) {
        c.getPlayer().dropMessage(1, "这个道具脚本是错误的，请联系管理员修复它.道具ID: " + itemId);
    }
}
