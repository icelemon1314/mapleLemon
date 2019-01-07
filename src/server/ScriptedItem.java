package server;

public class ScriptedItem {

    private final boolean runOnPickup;
    private final int npc;
    private final String script;

    public ScriptedItem(int npc, String script, boolean rop) {
        this.npc = npc;
        this.script = script;
        this.runOnPickup = rop;
    }

    public int getNpc() {
        return this.npc;
    }

    public String getScript() {
        return this.script;
    }

    public boolean runOnPickup() {
        return this.runOnPickup;
    }
}
