package server.maps;

import java.awt.Point;
import java.util.Objects;

public class MapleFoothold implements Comparable<MapleFoothold> {

    private final Point p1;
    private final Point p2;
    private final int id;
    private short next;
    private short prev;

    public MapleFoothold(Point p1, Point p2, int id) {
        this.p1 = p1;
        this.p2 = p2;
        this.id = id;
    }

    public boolean isWall() {
        return this.p1.x == this.p2.x;
    }

    public Point getPoint1() {
        return this.p1;
    }

    public Point getPoint2() {
        return this.p2;
    }

    public int getX1() {
        return this.p1.x;
    }

    public int getX2() {
        return this.p2.x;
    }

    public int getY1() {
        return this.p1.y;
    }

    public int getY2() {
        return this.p2.y;
    }

    @Override
    public int compareTo(MapleFoothold o) {
        MapleFoothold other = o;
        if (this.p2.y < other.getY1()) {
            return -1;
        }
        if (this.p1.y > other.getY2()) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapleFoothold)) {
            return false;
        }
        MapleFoothold oth = (MapleFoothold) o;
        return (oth.getY1() == this.p1.y) && (oth.getY2() == this.p2.y) && (oth.getX1() == this.p1.x) && (oth.getX2() == this.p2.x) && (this.id == oth.getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.p1);
        hash = 11 * hash + Objects.hashCode(this.p2);
        hash = 11 * hash + this.id;
        return hash;
    }

    public int getId() {
        return this.id;
    }

    public short getNext() {
        return this.next;
    }

    public void setNext(short next) {
        this.next = next;
    }

    public short getPrev() {
        return this.prev;
    }

    public void setPrev(short prev) {
        this.prev = prev;
    }
}
