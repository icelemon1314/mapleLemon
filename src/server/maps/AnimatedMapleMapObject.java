package server.maps;

public abstract class AnimatedMapleMapObject extends MapleMapObject {

    private int stance;

    public int getStance() {
        return this.stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public boolean isFacingLeft() {
        return getStance() % 2 != 0;
    }

    public int getFacingDirection() {
        return Math.abs(getStance() % 2);
    }
}
