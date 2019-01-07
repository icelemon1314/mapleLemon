package scripting;

import client.MapleClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import tools.EncodingDetect;
import tools.FileoutputUtil;

public abstract class AbstractScriptManager {

    private final ScriptEngineManager sem;

    protected AbstractScriptManager() {
        this.sem = new ScriptEngineManager();
    }

    protected Invocable getInvocable(String path, MapleClient c) {
        return getInvocable(path, c, false);
    }

    protected Invocable getInvocable(String path, MapleClient c, boolean npc) {
        try {
            path = "scripts/" + path;
            ScriptEngine engine = null;
            if (c != null) {
                engine = c.getScriptEngine(path);
            }
            if (engine == null) {
                File scriptFile = new File(path);
                if (!scriptFile.exists()) {
                    if (c != null && c.getPlayer() != null) {
                        c.getPlayer().dropMessage(1, "这个NPC脚本不存在(" + npc + ")，你可以使用问题反馈.");
                    }
                    return null;
                }
                engine = this.sem.getEngineByName("javascript");
                if (c != null) {
                    c.setScriptEngine(path, engine);
                }
                try (InputStream fr = new FileInputStream(scriptFile)) {
                    BufferedReader bf = new BufferedReader(new InputStreamReader(fr, EncodingDetect.getJavaEncode(scriptFile)));
                    engine.eval(bf);
                } 
            } else if (c != null && npc) {
                c.getPlayer().dropMessage(5, "现在还不能进行操作.");
            }
            return (Invocable) engine;
        } catch (Exception e) {
            System.err.println("Error executing script. Path: " + path + "\r\nException " + e);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing script. Path: " + path + "\r\nException " + e);
        }
        return null;
    }
}
