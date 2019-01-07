package tools;

import java.awt.Point;
import java.util.List;

public class AttackPair {

    public int objectid;
    public Point point;
    public List<Pair<Integer, Boolean>> attack;

    public AttackPair(int objectid, List<Pair<Integer, Boolean>> attack) {
        this.objectid = objectid;
        this.attack = attack;
    }

    public AttackPair(int objectid, Point point, List<Pair<Integer, Boolean>> attack) {
        this.objectid = objectid;
        this.point = point;
        this.attack = attack;
    }
}
