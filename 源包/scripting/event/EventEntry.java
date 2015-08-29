package scripting.event;

import javax.script.Invocable;

public class EventEntry {

    public String script;
    public Invocable iv;
    public EventManager em;

    public EventEntry(String script, Invocable iv, EventManager em) {
        this.script = script;
        this.iv = iv;
        this.em = em;
    }
}
