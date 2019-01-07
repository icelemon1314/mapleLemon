package server;

public abstract interface ShutdownServerMBean extends Runnable {

    public abstract void shutdown();
}
