package scripting.portal;

import client.MapleClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import server.MaplePortal;
import tools.EncodingDetect;
import tools.FileoutputUtil;
import tools.packet.NPCPacket;

public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private final Map<String, PortalScript> scripts = new HashMap();
    private static final ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private PortalScript getPortalScript(String scriptName) {
        if (this.scripts.containsKey(scriptName)) {
            return this.scripts.get(scriptName);
        }
        File scriptFile = new File("scripts/portals/" + scriptName + ".js");
        if (!scriptFile.exists()) {
            return null;
        }
        InputStream fr = null;
        ScriptEngine portal = sef.getScriptEngine();
        try {
            fr = new FileInputStream(scriptFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(fr, EncodingDetect.getJavaEncode(scriptFile)));
            CompiledScript compiled = ((Compilable) portal).compile(bf);
            compiled.eval();
        } catch (Exception e) {
            System.err.println("请检查传送点脚本名为:(" + scriptName + ".js)的文件." + e);
            FileoutputUtil.log(FileoutputUtil.Portal_ScriptEx_Log, "请检查传送点脚本名为:(" + scriptName + ".js)的文件. " + e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                }
            }
        }
        PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
        this.scripts.put(scriptName, script);
        return script;
    }

    public void executePortalScript(MaplePortal portal, MapleClient c) {
        PortalScript script = getPortalScript(portal.getScriptName());

        boolean err = false;
        if (script != null) {
            try {
                script.enter(new PortalPlayerInteraction(c, portal));
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "执行传送点脚本名为:(" + portal.getScriptName() + ".js)的文件 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
                }
            } catch (Exception e) {
                err = true;
                if (c.getPlayer().isShowPacket()) {
                    c.getPlayer().dropMessage(5, "执行地图脚本过程中发生错误.请检查传送点脚本名为:( " + portal.getScriptName() + ".js)的文件，错误信息：" + e);
                }
                FileoutputUtil.log(FileoutputUtil.Portal_ScriptEx_Log, "执行地图脚本过程中发生错误.请检查传送点脚本名为:( " + portal.getScriptName() + ".js)的文件.\r\n错误信息:" + e);
            }
        } else {
            err = true;
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "未找到传送点脚本名为:(" + portal.getScriptName() + ".js)的文件 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
            }
            FileoutputUtil.log(FileoutputUtil.Portal_ScriptEx_Log, "执行地图脚本过程中发生错误.未找到传送点脚本名为:(" + portal.getScriptName() + ".js)的文件 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
        }
        if (err) {
            c.getPlayer().卡图 = c.getPlayer().getMapId();
            c.getSession().write(NPCPacket.sendNPCSay(9010000, "你好像被卡在了奇怪的地方，这里有个东西未处理，请联系管理员反馈信息：" + portal.getScriptName() + "\r\n你现在可以点击 拍卖 或者输入 @卡图 来移动到射手村。"));
        }
    }

    public void clearScripts() {
        this.scripts.clear();
    }
}
