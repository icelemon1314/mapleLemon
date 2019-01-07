package server.movement;

public abstract interface LifeMovement extends LifeMovementFragment {

    public abstract int getNewstate();

    public abstract int getDuration();

    public abstract int getType();
}
