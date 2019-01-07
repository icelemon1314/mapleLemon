package server.squad;

import java.util.ArrayList;
import java.util.HashMap;
import tools.Pair;

public enum MapleSquadType {

    bossbalrog(2),
    zak(2),
    chaoszak(3),
    pinkzak(3),
    horntail(2),
    chaosht(3),
    pinkbean(3),
    nmm_squad(2),
    vergamot(2),
    dunas(2),
    nibergen_squad(2),
    dunas2(2),
    core_blaze(2),
    aufheben(2),
    cwkpq(10),
    tokyo_2095(2),
    vonleon(3),
    scartar(2),
    cygnus(3),
    chaospb(3),
    arkarium(3),
    hillah(2);

    public int i;
    public HashMap<Integer, ArrayList<Pair<String, String>>> queuedPlayers = new HashMap();
    public HashMap<Integer, ArrayList<Pair<String, Long>>> queue = new HashMap();

    private MapleSquadType(int i) {
        this.i = i;
    }
}
