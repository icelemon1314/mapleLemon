package scripting.event;

import handling.channel.ChannelServer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import scripting.AbstractScriptManager;
import tools.FileoutputUtil;

public class EventScriptManager extends AbstractScriptManager {

    private final Map<String, EventEntry> events = new LinkedHashMap();
    private static final AtomicInteger runningInstanceMapId = new AtomicInteger(0);

    public static int getNewInstanceMapId() {
        return runningInstanceMapId.addAndGet(1);
    }

    public EventScriptManager(ChannelServer cserv, String[] scripts) {
        for (String script : scripts) {
            if (!script.equals("")) {
                Invocable iv = getInvocable("events/" + script + ".js", null);
                if (iv != null) {
                    this.events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
                }
            }
        }
    }

    public EventManager getEventManager(String event) {
        EventEntry entry = this.events.get(event);
        if (entry == null) {
            return null;
        }
        return entry.em;
    }

    public void init() {
        for (EventEntry entry : this.events.values()) {
            try {
                ((ScriptEngine) entry.iv).put("em", entry.em);
                entry.iv.invokeFunction("init", new Object[]{(Object) null});
            } catch (ScriptException | NoSuchMethodException ex) {
                FileoutputUtil.log("Error initiating event: " + entry.script + ":" + ex);
                FileoutputUtil.log(FileoutputUtil.Event_ScriptEx_Log, "Error initiating event: " + entry.script + ":" + ex);
            }
        }
    }

    public void cancel() {
        for (EventEntry entry : this.events.values()) {
            entry.em.cancel();
        }
    }
}
