package scripting.map;

import client.MapleClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import tools.EncodingDetect;
import tools.FileoutputUtil;

public class MapScriptManager {

    private static final MapScriptManager instance = new MapScriptManager();
    private final Map<String, MapScript> scripts = new HashMap();
    private static final ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();

    public static final MapScriptManager getInstance() {
        return instance;
    }

    public void getMapScript(MapleClient c, String scriptName, boolean firstUser) {
        if (this.scripts.containsKey(scriptName)) {
            ((MapScript) this.scripts.get(scriptName)).start(new MapScriptMethods(c));
            return;
        }
        String type = "onUserEnter/";
        if (firstUser) {
            type = "onFirstUserEnter/";
        }
        File scriptFile = new File("scripts/地图/" + type + scriptName + ".js");
        if (!scriptFile.exists()) {
            if (c.getPlayer().isShowPacket()) {
                c.getPlayer().dropMessage(5, "地图触发: 未找到 地图/" + type + " 文件中的 " + scriptName + ".js 文件.");
            }
            FileoutputUtil.log(FileoutputUtil.Map_ScriptEx_Log, "地图触发: 未找到 地图/" + type + " 文件中的 " + scriptName + ".js 文件. 在地图 " + c.getPlayer().getMapId() + " - " + c.getPlayer().getMap().getMapName());
            return;
        }
        BufferedReader bf = null;
        ScriptEngine map = sef.getScriptEngine();
        try {
            InputStream in = new FileInputStream(scriptFile);
            bf = new BufferedReader(new InputStreamReader(in, EncodingDetect.getJavaEncode(scriptFile)));
            CompiledScript compiled = ((Compilable) map).compile(bf);
            compiled.eval();
        } catch (FileNotFoundException | ScriptException | UnsupportedEncodingException e) {
            System.err.println("请检查(地图/" + type + " 文件中的 " + scriptName + ".js)的文件." + e);
            FileoutputUtil.log(FileoutputUtil.Map_ScriptEx_Log, "请检查(地图/" + type + " 文件中的 " + scriptName + ".js)的文件." + e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                }
            }
        }
        if (c.getPlayer().isShowPacket()) {
            c.getPlayer().dropMessage(5, "开始执行地图触发: 地图/" + type + " 文件中的 " + scriptName + ".js 文件.");
        }
        MapScript script = (MapScript) ((Invocable) map).getInterface(MapScript.class);
        this.scripts.put(scriptName, script);
        script.start(new MapScriptMethods(c));
    }

    public void clearScripts() {
        this.scripts.clear();
    }
}
